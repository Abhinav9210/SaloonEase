package com.saloon.controller;

import com.saloon.dto.request.SlotGenerationRequest;
import com.saloon.dto.response.ApiResponse;
import com.saloon.dto.response.TimeSlotResponse;
import com.saloon.service.SlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
@Tag(name = "Slots", description = "Time slot management endpoints")
public class SlotController {

    private final SlotService slotService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Generate time slots for a barber on a specific date")
    public ResponseEntity<ApiResponse<List<TimeSlotResponse>>> generateSlots(
        @Valid @RequestBody SlotGenerationRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
            slotService.generateSlots(request, userDetails.getUsername()), "Slots generated"));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available slots for a barber on a date")
    public ResponseEntity<ApiResponse<List<TimeSlotResponse>>> getAvailableSlots(
        @RequestParam Long barberId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(slotService.getAvailableSlots(barberId, date)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get all slots for a barber on a date")
    public ResponseEntity<ApiResponse<List<TimeSlotResponse>>> getAllSlots(
        @RequestParam Long barberId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(slotService.getSlotsByBarberAndDate(barberId, date)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Delete an unbooked slot")
    public ResponseEntity<ApiResponse<Void>> deleteSlot(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails) {
        slotService.deleteSlot(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "Slot deleted"));
    }
}
