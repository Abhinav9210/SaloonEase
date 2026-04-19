package com.saloon.dto.response;

import com.saloon.entity.enums.BookingStatus;
import com.saloon.entity.enums.PaymentMode;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class BookingResponse {
    private Long id;
    private String bookingReference;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private Long salonId;
    private String salonName;
    private Long barberId;
    private String barberName;
    private Long serviceId;
    private String serviceName;
    private Integer serviceDurationMinutes;
    private Long slotId;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BookingStatus status;
    private Double bookingFee;
    private Double totalAmount;
    private PaymentMode paymentMode;
    private String notes;
    private String cancellationReason;
    private LocalDateTime bookedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime completedAt;
    private LocalDateTime expiresAt;
}
