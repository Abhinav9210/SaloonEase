package com.saloon.controller;

import com.saloon.dto.response.*;
import com.saloon.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin management endpoints")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/analytics")
    @Operation(summary = "Get platform-wide analytics")
    public ResponseEntity<ApiResponse<DashboardAnalyticsResponse>> getAnalytics() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getPlatformAnalytics()));
    }

    @GetMapping("/salons")
    @Operation(summary = "Get all salons")
    public ResponseEntity<ApiResponse<Page<SalonResponse>>> getAllSalons(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllSalons(page, size)));
    }

    @GetMapping("/salons/pending")
    @Operation(summary = "Get pending salon registrations")
    public ResponseEntity<ApiResponse<Page<SalonResponse>>> getPendingSalons(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getPendingSalons(page, size)));
    }

    @PatchMapping("/salons/{id}/approve")
    @Operation(summary = "Approve a salon registration")
    public ResponseEntity<ApiResponse<SalonResponse>> approveSalon(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.approveSalon(id), "Salon approved"));
    }

    @PatchMapping("/salons/{id}/reject")
    @Operation(summary = "Reject a salon registration")
    public ResponseEntity<ApiResponse<SalonResponse>> rejectSalon(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.rejectSalon(id), "Salon rejected"));
    }

    @GetMapping("/users")
    @Operation(summary = "Get all platform users")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllUsers(page, size)));
    }

    @PatchMapping("/users/{id}/toggle-status")
    @Operation(summary = "Toggle user active/inactive status")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.toggleUserStatus(id)));
    }

    @DeleteMapping("/salons/{id}")
    @Operation(summary = "Delete a salon completely")
    public ResponseEntity<ApiResponse<Void>> deleteSalon(@PathVariable Long id) {
        adminService.deleteSalon(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Salon successfully deleted"));
    }
}
