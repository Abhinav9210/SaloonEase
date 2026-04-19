package com.saloon.controller;

import com.saloon.dto.request.PaymentRequest;
import com.saloon.dto.response.ApiResponse;
import com.saloon.dto.response.PaymentResponse;
import com.saloon.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Process payment for a booking")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
        @Valid @RequestBody PaymentRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
            paymentService.processPayment(request, userDetails.getUsername()), "Payment processed"));
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get payment history for a booking")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getBookingPayments(@PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getBookingPayments(bookingId)));
    }
}
