package com.saloon.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    private Long salonId;
    private Long barberId;

    @Min(1) @Max(5)
    @NotNull(message = "Rating is required")
    private Integer rating;

    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String comment;
}
