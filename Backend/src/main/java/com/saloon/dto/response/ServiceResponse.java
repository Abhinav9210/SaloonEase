package com.saloon.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ServiceResponse {
    private Long id;
    private Long salonId;
    private String salonName;
    private String name;
    private String description;
    private Integer durationMinutes;
    private Double price;
    private boolean isActive;
    private LocalDateTime createdAt;
}
