package com.saloon.controller;

import com.saloon.dto.request.ServiceRequest;
import com.saloon.dto.response.ApiResponse;
import com.saloon.dto.response.DashboardAnalyticsResponse;
import com.saloon.dto.response.ServiceResponse;
import com.saloon.service.OwnerDashboardService;
import com.saloon.service.ServiceManagementService;
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
@RequestMapping("/api/owner")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
@Tag(name = "Owner Dashboard", description = "Owner dashboard and service management")
public class OwnerController {

    private final OwnerDashboardService ownerDashboardService;
    private final ServiceManagementService serviceManagementService;

    @GetMapping("/analytics/{salonId}")
    @Operation(summary = "Get salon analytics dashboard")
    public ResponseEntity<ApiResponse<DashboardAnalyticsResponse>> getAnalytics(@PathVariable Long salonId) {
        return ResponseEntity.ok(ApiResponse.success(ownerDashboardService.getOwnerAnalytics(salonId)));
    }

    @PostMapping("/salons/{salonId}/services")
    @Operation(summary = "Add a service to salon")
    public ResponseEntity<ApiResponse<ServiceResponse>> addService(
        @PathVariable Long salonId,
        @Valid @RequestBody ServiceRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
            serviceManagementService.addService(salonId, request, userDetails.getUsername()), "Service added"));
    }

    @PutMapping("/services/{serviceId}")
    @Operation(summary = "Update a service")
    public ResponseEntity<ApiResponse<ServiceResponse>> updateService(
        @PathVariable Long serviceId,
        @Valid @RequestBody ServiceRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
            serviceManagementService.updateService(serviceId, request, userDetails.getUsername())));
    }

    @DeleteMapping("/services/{serviceId}")
    @Operation(summary = "Delete (soft) a service")
    public ResponseEntity<ApiResponse<Void>> deleteService(
        @PathVariable Long serviceId,
        @AuthenticationPrincipal UserDetails userDetails) {
        serviceManagementService.deleteService(serviceId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "Service deleted"));
    }
}
