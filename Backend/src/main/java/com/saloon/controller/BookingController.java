package com.saloon.controller;

import com.saloon.dto.request.BookingRequest;
import com.saloon.dto.response.ApiResponse;
import com.saloon.dto.response.BookingResponse;
import com.saloon.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking management endpoints")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create a new booking")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
        @Valid @RequestBody BookingRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
            bookingService.createBooking(request, userDetails.getUsername()), "Booking created"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking details")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
            bookingService.getBooking(id, userDetails.getUsername())));
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get current customer's booking history")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getMyBookings(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
            bookingService.getCustomerBookings(userDetails.getUsername(), page, size)));
    }

    @GetMapping("/salon/{salonId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get all bookings for a salon")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getSalonBookings(
        @PathVariable Long salonId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
            bookingService.getSalonBookings(salonId, page, size)));
    }

    @GetMapping("/barber/{barberId}")
    @PreAuthorize("hasAnyRole('BARBER', 'OWNER', 'ADMIN')")
    @Operation(summary = "Get all bookings for a barber")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getBarberBookings(
        @PathVariable Long barberId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
            bookingService.getBarberBookings(barberId, page, size)));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('OWNER', 'BARBER', 'ADMIN')")
    @Operation(summary = "Approve a booking")
    public ResponseEntity<ApiResponse<BookingResponse>> approve(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
            bookingService.approveBooking(id, userDetails.getUsername()), "Booking approved"));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('OWNER', 'BARBER', 'ADMIN')")
    @Operation(summary = "Reject a booking")
    public ResponseEntity<ApiResponse<BookingResponse>> reject(
        @PathVariable Long id,
        @RequestParam(required = false) String reason,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
            bookingService.rejectBooking(id, reason, userDetails.getUsername()), "Booking rejected"));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('OWNER', 'BARBER', 'ADMIN')")
    @Operation(summary = "Mark booking as completed")
    public ResponseEntity<ApiResponse<BookingResponse>> complete(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
            bookingService.completeBooking(id, userDetails.getUsername()), "Booking completed"));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<ApiResponse<BookingResponse>> cancel(
        @PathVariable Long id,
        @RequestParam(required = false) String reason,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
            bookingService.cancelBooking(id, reason, userDetails.getUsername()), "Booking cancelled"));
    }
}
