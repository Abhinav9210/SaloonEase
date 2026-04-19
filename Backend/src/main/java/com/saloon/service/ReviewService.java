package com.saloon.service;

import com.saloon.dto.request.ReviewRequest;
import com.saloon.dto.response.ReviewResponse;
import com.saloon.entity.*;
import com.saloon.exception.BusinessException;
import com.saloon.exception.ResourceNotFoundException;
import com.saloon.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final SalonRepository salonRepository;
    private final BarberRepository barberRepository;
    private final BookingRepository bookingRepository;
    private final SalonService salonService;
    private final BarberService barberService;

    @Transactional
    public ReviewResponse createReview(ReviewRequest request, String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Booking booking = bookingRepository.findById(request.getBookingId())
            .orElseThrow(() -> new ResourceNotFoundException("Booking", request.getBookingId()));
        if (!booking.getCustomer().getEmail().equals(customerEmail)) {
            throw new BusinessException("You can only review your own bookings");
        }
        if (reviewRepository.existsByCustomerIdAndBookingId(customer.getId(), request.getBookingId())) {
            throw new BusinessException("You have already reviewed this booking");
        }

        Salon salon = request.getSalonId() != null ? salonRepository.findById(request.getSalonId()).orElse(null) : null;
        Barber barber = request.getBarberId() != null ? barberRepository.findById(request.getBarberId()).orElse(null) : null;

        Review review = Review.builder()
            .customer(customer)
            .salon(salon)
            .barber(barber)
            .booking(booking)
            .rating(request.getRating())
            .comment(request.getComment())
            .build();
        review = reviewRepository.save(review);

        // Update ratings
        if (salon != null) {
            Double avgRating = reviewRepository.avgRatingBySalon(salon.getId());
            long count = reviewRepository.countBySalon(salon.getId());
            salonService.updateRating(salon.getId(), avgRating != null ? avgRating : 0.0, (int) count);
        }
        if (barber != null) {
            Double avgRating = reviewRepository.avgRatingByBarber(barber.getId());
            long count = reviewRepository.findByBarberId(barber.getId(), PageRequest.of(0, 1)).getTotalElements();
            barberService.updateRating(barber.getId(), avgRating != null ? avgRating : 0.0, (int) count);
        }

        return mapToResponse(review);
    }

    public Page<ReviewResponse> getSalonReviews(Long salonId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findBySalonId(salonId, pageable).map(this::mapToResponse);
    }

    public Page<ReviewResponse> getBarberReviews(Long barberId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findByBarberId(barberId, pageable).map(this::mapToResponse);
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
            .id(review.getId())
            .customerId(review.getCustomer().getId())
            .customerName(review.getCustomer().getName())
            .customerProfilePic(review.getCustomer().getProfilePicture())
            .salonId(review.getSalon() != null ? review.getSalon().getId() : null)
            .barberId(review.getBarber() != null ? review.getBarber().getId() : null)
            .rating(review.getRating())
            .comment(review.getComment())
            .createdAt(review.getCreatedAt())
            .build();
    }
}
