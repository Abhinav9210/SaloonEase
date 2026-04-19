package com.saloon.controller;

import com.saloon.dto.request.SalonRequest;
import com.saloon.dto.response.ApiResponse;
import com.saloon.dto.response.BarberResponse;
import com.saloon.dto.response.SalonResponse;
import com.saloon.dto.response.ServiceResponse;
import com.saloon.service.BarberService;
import com.saloon.service.SalonService;
import com.saloon.service.ServiceManagementService;
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

import java.util.List;

@RestController
@RequestMapping("/api/salons")
@RequiredArgsConstructor
@Tag(name = "Salons", description = "Salon management endpoints")
public class SalonController {

    private final SalonService salonService;
    private final BarberService barberService;
    private final ServiceManagementService serviceManagementService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Create a new salon")
    public ResponseEntity<ApiResponse<SalonResponse>> createSalon(
        @Valid @RequestBody SalonRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
            salonService.createSalon(request, userDetails.getUsername()), "Salon created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Update salon details")
    public ResponseEntity<ApiResponse<SalonResponse>> updateSalon(
        @PathVariable Long id,
        @Valid @RequestBody SalonRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
            salonService.updateSalon(id, request, userDetails.getUsername())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get salon by ID")
    public ResponseEntity<ApiResponse<SalonResponse>> getSalon(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(salonService.getSalon(id)));
    }

    @GetMapping("/approved")
    @Operation(summary = "Get all approved salons (paginated)")
    public ResponseEntity<ApiResponse<Page<SalonResponse>>> getApprovedSalons(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "rating") String sortBy) {
        return ResponseEntity.ok(ApiResponse.success(salonService.getApprovedSalons(page, size, sortBy)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search salons by name, city, or description")
    public ResponseEntity<ApiResponse<Page<SalonResponse>>> searchSalons(
        @RequestParam String query,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(salonService.searchSalons(query, page, size)));
    }

    @GetMapping("/top-rated")
    @Operation(summary = "Get top rated salons")
    public ResponseEntity<ApiResponse<List<SalonResponse>>> getTopRated() {
        return ResponseEntity.ok(ApiResponse.success(salonService.getTopRatedSalons()));
    }

    @GetMapping("/my-salons")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get salons owned by current user")
    public ResponseEntity<ApiResponse<List<SalonResponse>>> getMySalons(
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(salonService.getOwnerSalons(userDetails.getUsername())));
    }

    @GetMapping("/{id}/barbers")
    @Operation(summary = "Get barbers in a salon")
    public ResponseEntity<ApiResponse<List<BarberResponse>>> getSalonBarbers(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(barberService.getBarbersBySalon(id)));
    }

    @GetMapping("/{id}/services")
    @Operation(summary = "Get services offered by a salon")
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> getSalonServices(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(serviceManagementService.getServicesBySalon(id)));
    }
}
