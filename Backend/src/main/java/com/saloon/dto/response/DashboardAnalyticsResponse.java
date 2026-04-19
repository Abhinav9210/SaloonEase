package com.saloon.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardAnalyticsResponse {
    private Long totalBookings;
    private Long pendingBookings;
    private Long completedBookings;
    private Long cancelledBookings;
    private Double totalEarnings;
    private Double weeklyEarnings;
    private Double dailyEarnings;
    private Long totalBarbers;
    private Long totalServices;
    private Double averageRating;
}
