package com.saloon.dto.request;

import com.saloon.entity.enums.PaymentMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequest {
    @NotNull(message = "Barber ID is required")
    private Long barberId;

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Slot ID is required")
    private Long slotId;

    @NotNull(message = "Salon ID is required")
    private Long salonId;

    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode;

    private String notes;
    private String couponCode;
}
