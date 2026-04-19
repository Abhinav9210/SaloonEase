package com.saloon.service;

import com.saloon.dto.response.DashboardAnalyticsResponse;
import com.saloon.entity.enums.BookingStatus;
import com.saloon.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OwnerDashboardService {

    private final BookingRepository bookingRepository;
    private final BarberRepository barberRepository;
    private final ServiceRepository serviceRepository;
    private final SalonRepository salonRepository;
    private final ReviewRepository reviewRepository;

    public DashboardAnalyticsResponse getOwnerAnalytics(Long salonId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = dayStart.minusDays(7);

        Double totalEarnings = bookingRepository.sumEarningsBySalonAndDateRange(salonId,
            LocalDateTime.of(2020, 1, 1, 0, 0), now);
        Double weeklyEarnings = bookingRepository.sumEarningsBySalonAndDateRange(salonId, weekStart, now);
        Double dailyEarnings = bookingRepository.sumEarningsBySalonAndDateRange(salonId, dayStart, now);

        long totalBookings = bookingRepository.countBookingsBySalonAndDateRange(salonId,
            LocalDateTime.of(2020, 1, 1, 0, 0), now);
        long pendingBookings = bookingRepository.findBySalonIdAndStatus(salonId, BookingStatus.PENDING,
            org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
        long completedBookings = bookingRepository.findBySalonIdAndStatus(salonId, BookingStatus.COMPLETED,
            org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
        long cancelledBookings = bookingRepository.findBySalonIdAndStatus(salonId, BookingStatus.CANCELLED,
            org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();

        long totalBarbers = barberRepository.findBySalonId(salonId).size();
        long totalServices = serviceRepository.findBySalonIdAndIsActive(salonId, true).size();

        Double avgRating = reviewRepository.avgRatingBySalon(salonId);

        return DashboardAnalyticsResponse.builder()
            .totalBookings(totalBookings)
            .pendingBookings(pendingBookings)
            .completedBookings(completedBookings)
            .cancelledBookings(cancelledBookings)
            .totalEarnings(totalEarnings != null ? totalEarnings : 0.0)
            .weeklyEarnings(weeklyEarnings != null ? weeklyEarnings : 0.0)
            .dailyEarnings(dailyEarnings != null ? dailyEarnings : 0.0)
            .totalBarbers(totalBarbers)
            .totalServices(totalServices)
            .averageRating(avgRating != null ? avgRating : 0.0)
            .build();
    }
}
