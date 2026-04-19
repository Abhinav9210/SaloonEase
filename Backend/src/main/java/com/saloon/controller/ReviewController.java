package com.saloon.controller;

import com.saloon.dto.request.ReviewRequest;
import com.saloon.dto.response.ApiResponse;
import com.saloon.dto.response.ReviewResponse;
import com.saloon.service.ReviewService;
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
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Review and rating endpoints")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Submit a review for a salon or barber")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
        @Valid @RequestBody ReviewRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
            reviewService.createReview(request, userDetails.getUsername()), "Review submitted"));
    }

    @GetMapping("/salon/{salonId}")
    @Operation(summary = "Get reviews for a salon")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getSalonReviews(
        @PathVariable Long salonId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getSalonReviews(salonId, page, size)));
    }

    @GetMapping("/barber/{barberId}")
    @Operation(summary = "Get reviews for a barber")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getBarberReviews(
        @PathVariable Long barberId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getBarberReviews(barberId, page, size)));
    }
}
