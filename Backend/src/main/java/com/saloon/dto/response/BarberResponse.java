package com.saloon.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BarberResponse {
    private Long id;
    private Long salonId;
    private String salonName;
    private Long userId;
    private String name;
    private String email;
    private String bio;
    private Integer experienceYears;
    private List<String> specializations;
    private Double rating;
    private Integer totalReviews;
    private boolean isAvailable;
    private String profilePicture;
    private LocalDateTime createdAt;
}
