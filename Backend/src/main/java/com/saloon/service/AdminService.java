package com.saloon.service;

import com.saloon.dto.response.*;
import com.saloon.entity.User;
import com.saloon.entity.enums.BookingStatus;
import com.saloon.entity.enums.SalonStatus;
import com.saloon.exception.ResourceNotFoundException;
import com.saloon.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final SalonRepository salonRepository;
    private final BookingRepository bookingRepository;
    private final SalonService salonService;

    public DashboardAnalyticsResponse getPlatformAnalytics() {
        long totalBookings = bookingRepository.count();
        long pendingBookings = bookingRepository.countByStatus(BookingStatus.PENDING);
        long completedBookings = bookingRepository.countByStatus(BookingStatus.COMPLETED);
        long cancelledBookings = bookingRepository.countByStatus(BookingStatus.CANCELLED);
        Double totalRevenue = bookingRepository.sumTotalRevenue();

        return DashboardAnalyticsResponse.builder()
            .totalBookings(totalBookings)
            .pendingBookings(pendingBookings)
            .completedBookings(completedBookings)
            .cancelledBookings(cancelledBookings)
            .totalEarnings(totalRevenue != null ? totalRevenue : 0.0)
            .totalBarbers(0L)
            .build();
    }

    public Page<SalonResponse> getPendingSalons(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return salonRepository.findByStatus(SalonStatus.PENDING, pageable)
            .map(s -> salonService.getSalon(s.getId()));
    }

    @Transactional
    public SalonResponse approveSalon(Long salonId) {
        return salonService.updateStatus(salonId, SalonStatus.APPROVED);
    }

    @Transactional
    public SalonResponse rejectSalon(Long salonId) {
        return salonService.updateStatus(salonId, SalonStatus.REJECTED);
    }

    public Page<UserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return userRepository.findAll(pageable).map(this::mapUserToResponse);
    }

    @Transactional
    public UserResponse toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setActive(!user.isActive());
        user = userRepository.save(user);
        return mapUserToResponse(user);
    }

    public Page<SalonResponse> getAllSalons(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return salonRepository.findAll(pageable).map(s -> salonService.getSalon(s.getId()));
    }

    private final jakarta.persistence.EntityManager entityManager;

    @Transactional
    public void deleteSalon(Long salonId) {
        if (!salonRepository.existsById(salonId)) {
            throw new ResourceNotFoundException("Salon", salonId);
        }
        
        // Due to foreign key constraints, we must delete child entities first manually
        entityManager.createNativeQuery("DELETE FROM payments WHERE booking_id IN (SELECT id FROM bookings WHERE salon_id = :salonId)").setParameter("salonId", salonId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM bookings WHERE salon_id = :salonId").setParameter("salonId", salonId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM time_slots WHERE barber_id IN (SELECT id FROM barbers WHERE salon_id = :salonId)").setParameter("salonId", salonId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM barber_specializations WHERE barber_id IN (SELECT id FROM barbers WHERE salon_id = :salonId)").setParameter("salonId", salonId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM barbers WHERE salon_id = :salonId").setParameter("salonId", salonId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM services WHERE salon_id = :salonId").setParameter("salonId", salonId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM coupons WHERE salon_id = :salonId").setParameter("salonId", salonId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM reviews WHERE salon_id = :salonId").setParameter("salonId", salonId).executeUpdate();
        
        salonRepository.deleteById(salonId);
    }

    private UserResponse mapUserToResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .profilePicture(user.getProfilePicture())
            .role(user.getRole())
            .isActive(user.isActive())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
