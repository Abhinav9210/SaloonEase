package com.saloon.controller;

import com.saloon.dto.request.BarberRequest;
import com.saloon.dto.response.ApiResponse;
import com.saloon.dto.response.BarberResponse;
import com.saloon.service.BarberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/barbers")
@RequiredArgsConstructor
@Tag(name = "Barbers", description = "Barber management endpoints")
public class BarberController {

    private final BarberService barberService;

    @GetMapping("/my-profile")
    @PreAuthorize("hasRole('BARBER')")
    @Operation(summary = "Get current barber's profile")
    public ResponseEntity<ApiResponse<BarberResponse>> getMyProfile(
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
            barberService.getBarberByUserEmail(userDetails.getUsername())));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Add a barber to a salon")
    public ResponseEntity<ApiResponse<BarberResponse>> addBarber(
        @Valid @RequestBody BarberRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
            barberService.addBarber(request, userDetails.getUsername()), "Barber added"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Update barber details")
    public ResponseEntity<ApiResponse<BarberResponse>> updateBarber(
        @PathVariable Long id,
        @Valid @RequestBody BarberRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
            barberService.updateBarber(id, request, userDetails.getUsername())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get barber by ID")
    public ResponseEntity<ApiResponse<BarberResponse>> getBarber(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(barberService.getBarber(id)));
    }

    @PatchMapping("/{id}/toggle-availability")
    @PreAuthorize("hasAnyRole('BARBER', 'OWNER', 'ADMIN')")
    @Operation(summary = "Toggle barber availability")
    public ResponseEntity<ApiResponse<Void>> toggleAvailability(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails) {
        barberService.toggleAvailability(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "Availability updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Remove a barber from a salon")
    public ResponseEntity<ApiResponse<Void>> deleteBarber(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails) {
        barberService.deleteBarber(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "Barber removed"));
    }
}
