package com.saloon.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class SlotGenerationRequest {
    @NotNull(message = "Barber ID is required")
    private Long barberId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Slot duration in minutes is required")
    private Integer slotDurationMinutes;

    private Integer breakDurationMinutes = 0;
}
