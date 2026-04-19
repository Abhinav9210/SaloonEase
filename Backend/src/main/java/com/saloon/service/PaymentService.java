package com.saloon.service;

import com.saloon.dto.request.PaymentRequest;
import com.saloon.dto.response.PaymentResponse;
import com.saloon.entity.Booking;
import com.saloon.entity.Payment;
import com.saloon.entity.enums.PaymentStatus;
import com.saloon.exception.BusinessException;
import com.saloon.exception.ResourceNotFoundException;
import com.saloon.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request, String email) {
        Booking booking = bookingService.getBookingEntity(request.getBookingId());
        if (!booking.getCustomer().getEmail().equals(email)) {
            throw new BusinessException("This booking does not belong to you");
        }
        // Mock payment processing — in production, call Razorpay/Stripe here
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        boolean paymentSuccess = simulatePayment(request);
        Payment payment = Payment.builder()
            .booking(booking)
            .amount(request.getAmount())
            .method(request.getMethod())
            .status(paymentSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
            .transactionId(transactionId)
            .gatewayResponse(paymentSuccess ? "Payment successful (mock)" : "Payment failed (mock)")
            .paidAt(paymentSuccess ? LocalDateTime.now() : null)
            .build();
        payment = paymentRepository.save(payment);
        log.info("Payment {} for booking #{}: {}", transactionId, booking.getBookingReference(), payment.getStatus());
        return mapToResponse(payment);
    }

    public List<PaymentResponse> getBookingPayments(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId).stream()
            .map(this::mapToResponse).collect(Collectors.toList());
    }

    private boolean simulatePayment(PaymentRequest request) {
        // Mock: always succeed unless amount <= 0
        return request.getAmount() > 0;
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
            .id(payment.getId())
            .bookingId(payment.getBooking().getId())
            .bookingReference(payment.getBooking().getBookingReference())
            .amount(payment.getAmount())
            .method(payment.getMethod())
            .status(payment.getStatus())
            .transactionId(payment.getTransactionId())
            .paidAt(payment.getPaidAt())
            .createdAt(payment.getCreatedAt())
            .build();
    }
}
