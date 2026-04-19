package com.saloon.service;

import com.saloon.dto.request.BookingRequest;
import com.saloon.dto.response.BookingResponse;
import com.saloon.entity.*;
import com.saloon.entity.enums.BookingStatus;
import com.saloon.exception.*;
import com.saloon.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BarberRepository barberRepository;
    private final ServiceRepository serviceRepository;
    private final TimeSlotRepository slotRepository;
    private final SalonRepository salonRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${app.booking.auto-cancel-minutes:30}")
    private int autoCancelMinutes;

    @Transactional
    public BookingResponse createBooking(BookingRequest request, String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Salon salon = salonRepository.findById(request.getSalonId())
            .orElseThrow(() -> new ResourceNotFoundException("Salon", request.getSalonId()));
        Barber barber = barberRepository.findById(request.getBarberId())
            .orElseThrow(() -> new ResourceNotFoundException("Barber", request.getBarberId()));
        com.saloon.entity.Service service = serviceRepository.findById(request.getServiceId())
            .orElseThrow(() -> new ResourceNotFoundException("Service", request.getServiceId()));
        TimeSlot slot = slotRepository.findById(request.getSlotId())
            .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", request.getSlotId()));

        if (slot.isBooked()) {
            throw new BookingConflictException("This time slot is already booked. Please select another slot.");
        }
        if (!slot.isAvailable()) {
            throw new BookingConflictException("This time slot is not available.");
        }

        // Lock the slot
        slot.setBooked(true);
        slotRepository.save(slot);

        double bookingFee = salon.getMinimumBookingFee();
        Booking booking = Booking.builder()
            .customer(customer)
            .salon(salon)
            .barber(barber)
            .service(service)
            .timeSlot(slot)
            .status(BookingStatus.PENDING)
            .bookingFee(bookingFee)
            .totalAmount(service.getPrice())
            .paymentMode(request.getPaymentMode())
            .notes(request.getNotes())
            .expiresAt(LocalDateTime.now().plusMinutes(autoCancelMinutes))
            .build();
        booking = bookingRepository.save(booking);

        // Notify owner and customer
        notificationService.notifyBookingCreated(booking);
        // WebSocket push to owner
        messagingTemplate.convertAndSendToUser(
            salon.getOwner().getEmail(), "/queue/bookings",
            "New booking #" + booking.getBookingReference() + " received");

        log.info("Booking created: {} for customer: {}", booking.getBookingReference(), customerEmail);
        return mapToResponse(booking);
    }

    @Transactional
    public BookingResponse approveBooking(Long bookingId, String approverEmail) {
        Booking booking = getBookingEntity(bookingId);
        verifyApprover(booking, approverEmail);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("Booking is not in PENDING state");
        }
        booking.setStatus(BookingStatus.APPROVED);
        booking.setApprovedAt(LocalDateTime.now());
        booking = bookingRepository.save(booking);

        notificationService.notifyBookingApproved(booking);
        messagingTemplate.convertAndSendToUser(
            booking.getCustomer().getEmail(), "/queue/bookings",
            "Your booking #" + booking.getBookingReference() + " has been approved!");

        return mapToResponse(booking);
    }

    @Transactional
    public BookingResponse rejectBooking(Long bookingId, String reason, String approverEmail) {
        Booking booking = getBookingEntity(bookingId);
        verifyApprover(booking, approverEmail);
        booking.setStatus(BookingStatus.REJECTED);
        booking.setCancellationReason(reason);
        freeSlot(booking);
        booking = bookingRepository.save(booking);
        notificationService.notifyBookingRejected(booking);
        return mapToResponse(booking);
    }

    @Transactional
    public BookingResponse completeBooking(Long bookingId, String email) {
        Booking booking = getBookingEntity(bookingId);
        verifyApprover(booking, email);
        booking.setStatus(BookingStatus.COMPLETED);
        booking.setCompletedAt(LocalDateTime.now());
        booking = bookingRepository.save(booking);
        notificationService.notifyBookingCompleted(booking);
        return mapToResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, String reason, String userEmail) {
        Booking booking = getBookingEntity(bookingId);
        boolean isCustomer = booking.getCustomer().getEmail().equals(userEmail);
        boolean isOwner = booking.getSalon().getOwner().getEmail().equals(userEmail);
        if (!isCustomer && !isOwner) {
            throw new UnauthorizedException("Not authorized to cancel this booking");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Cannot cancel a " + booking.getStatus() + " booking");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(reason);
        booking.setCancelledAt(LocalDateTime.now());
        freeSlot(booking);
        booking = bookingRepository.save(booking);
        notificationService.notifyBookingCancelled(booking);
        return mapToResponse(booking);
    }

    public Page<BookingResponse> getCustomerBookings(String email, int page, int size) {
        User customer = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "bookedAt"));
        return bookingRepository.findByCustomerId(customer.getId(), pageable).map(this::mapToResponse);
    }

    public Page<BookingResponse> getSalonBookings(Long salonId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "bookedAt"));
        return bookingRepository.findBySalonId(salonId, pageable).map(this::mapToResponse);
    }

    public Page<BookingResponse> getBarberBookings(Long barberId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "bookedAt"));
        return bookingRepository.findByBarberId(barberId, pageable).map(this::mapToResponse);
    }

    public BookingResponse getBooking(Long bookingId, String email) {
        Booking booking = getBookingEntity(bookingId);
        boolean authorized = booking.getCustomer().getEmail().equals(email)
            || booking.getSalon().getOwner().getEmail().equals(email)
            || (booking.getBarber().getUser() != null && booking.getBarber().getUser().getEmail().equals(email));
        if (!authorized) {
            throw new UnauthorizedException("Not authorized to view this booking");
        }
        return mapToResponse(booking);
    }

    @Transactional
    public void autoExpireBookings() {
        var expiredBookings = bookingRepository.findByStatusAndExpiresAtBefore(
            BookingStatus.PENDING, LocalDateTime.now());
        expiredBookings.forEach(booking -> {
            booking.setStatus(BookingStatus.EXPIRED);
            freeSlot(booking);
            bookingRepository.save(booking);
            log.info("Auto-expired booking: {}", booking.getBookingReference());
        });
    }

    private void freeSlot(Booking booking) {
        TimeSlot slot = booking.getTimeSlot();
        slot.setBooked(false);
        slotRepository.save(slot);
    }

    private void verifyApprover(Booking booking, String email) {
        boolean isOwner = booking.getSalon().getOwner().getEmail().equals(email);
        boolean isBarber = booking.getBarber().getUser() != null &&
            booking.getBarber().getUser().getEmail().equals(email);
        if (!isOwner && !isBarber) {
            throw new UnauthorizedException("Not authorized to manage this booking");
        }
    }

    public Booking getBookingEntity(Long id) {
        return bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
    }

    BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
            .id(booking.getId())
            .bookingReference(booking.getBookingReference())
            .customerId(booking.getCustomer().getId())
            .customerName(booking.getCustomer().getName())
            .customerEmail(booking.getCustomer().getEmail())
            .salonId(booking.getSalon().getId())
            .salonName(booking.getSalon().getName())
            .barberId(booking.getBarber().getId())
            .barberName(booking.getBarber().getUser() != null ? booking.getBarber().getUser().getName() : (booking.getBarber().getName() != null ? booking.getBarber().getName() : "Barber #" + booking.getBarber().getId()))
            .serviceId(booking.getService().getId())
            .serviceName(booking.getService().getName())
            .serviceDurationMinutes(booking.getService().getDurationMinutes())
            .slotId(booking.getTimeSlot().getId())
            .bookingDate(booking.getTimeSlot().getDate())
            .startTime(booking.getTimeSlot().getStartTime())
            .endTime(booking.getTimeSlot().getEndTime())
            .status(booking.getStatus())
            .bookingFee(booking.getBookingFee())
            .totalAmount(booking.getTotalAmount())
            .paymentMode(booking.getPaymentMode())
            .notes(booking.getNotes())
            .cancellationReason(booking.getCancellationReason())
            .bookedAt(booking.getBookedAt())
            .approvedAt(booking.getApprovedAt())
            .completedAt(booking.getCompletedAt())
            .expiresAt(booking.getExpiresAt())
            .build();
    }
}
