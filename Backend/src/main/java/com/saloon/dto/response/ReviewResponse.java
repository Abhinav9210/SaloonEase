package com.saloon.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private String customerProfilePic;
    private Long salonId;
    private Long barberId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
