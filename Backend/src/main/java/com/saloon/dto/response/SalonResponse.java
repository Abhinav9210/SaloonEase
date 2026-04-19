package com.saloon.dto.response;

import com.saloon.entity.enums.SalonStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SalonResponse {
    private Long id;
    private Long ownerId;
    private String ownerName;
    private String name;
    private String description;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private Double latitude;
    private Double longitude;
    private Double rating;
    private Integer totalReviews;
    private SalonStatus status;
    private String phone;
    private String openTime;
    private String closeTime;
    private Double minimumBookingFee;
    private List<String> images;
    private List<String> workingDays;
    private LocalDateTime createdAt;
}
