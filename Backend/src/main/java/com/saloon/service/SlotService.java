package com.saloon.service;

import com.saloon.dto.request.SlotGenerationRequest;
import com.saloon.dto.response.TimeSlotResponse;
import com.saloon.entity.Barber;
import com.saloon.entity.TimeSlot;
import com.saloon.exception.BusinessException;
import com.saloon.exception.ResourceNotFoundException;
import com.saloon.exception.UnauthorizedException;
import com.saloon.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlotService {

    private final TimeSlotRepository slotRepository;
    private final BarberService barberService;

    @Transactional
    public List<TimeSlotResponse> generateSlots(SlotGenerationRequest request, String ownerEmail) {
        Barber barber = barberService.getBarberEntity(request.getBarberId());
        if (!barber.getSalon().getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("Not authorized to generate slots for this barber");
        }
        if (request.getDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Cannot generate slots for past dates");
        }
        List<TimeSlot> existingSlots = slotRepository.findByBarberIdAndDate(barber.getId(), request.getDate());
        List<TimeSlot> unbookedSlots = existingSlots.stream().filter(s -> !s.isBooked()).collect(Collectors.toList());
        slotRepository.deleteAll(unbookedSlots);

        List<TimeSlot> generated = new ArrayList<>();
        LocalTime current = request.getStartTime();
        int slotDuration = request.getSlotDurationMinutes();
        int breakDuration = request.getBreakDurationMinutes() != null ? request.getBreakDurationMinutes() : 0;

        while (current.plusMinutes(slotDuration).compareTo(request.getEndTime()) <= 0) {
            LocalTime slotEnd = current.plusMinutes(slotDuration);
            boolean hasConflict = slotRepository.existsConflictingSlot(
                barber.getId(), request.getDate(), current, slotEnd);
            if (!hasConflict) {
                TimeSlot slot = TimeSlot.builder()
                    .barber(barber)
                    .date(request.getDate())
                    .startTime(current)
                    .endTime(slotEnd)
                    .build();
                generated.add(slot);
            }
            current = slotEnd.plusMinutes(breakDuration);
        }
        List<TimeSlot> saved = slotRepository.saveAll(generated);
        log.info("Generated {} slots for barber {} on {}", saved.size(), barber.getId(), request.getDate());
        return saved.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<TimeSlotResponse> getAvailableSlots(Long barberId, LocalDate date) {
        return slotRepository.findAvailableSlots(barberId, date).stream()
            .map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<TimeSlotResponse> getSlotsByBarberAndDate(Long barberId, LocalDate date) {
        return slotRepository.findByBarberIdAndDate(barberId, date).stream()
            .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteSlot(Long slotId, String ownerEmail) {
        TimeSlot slot = slotRepository.findById(slotId)
            .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", slotId));
        if (slot.isBooked()) {
            throw new BusinessException("Cannot delete a booked slot");
        }
        if (!slot.getBarber().getSalon().getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("Not authorized");
        }
        slotRepository.delete(slot);
    }

    public TimeSlot getSlotEntity(Long slotId) {
        return slotRepository.findById(slotId)
            .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", slotId));
    }

    private TimeSlotResponse mapToResponse(TimeSlot slot) {
        return TimeSlotResponse.builder()
            .id(slot.getId())
            .barberId(slot.getBarber().getId())
            .barberName(slot.getBarber().getUser() != null ? slot.getBarber().getUser().getName() : (slot.getBarber().getName() != null ? slot.getBarber().getName() : "Barber #" + slot.getBarber().getId()))
            .date(slot.getDate())
            .startTime(slot.getStartTime())
            .endTime(slot.getEndTime())
            .isBooked(slot.isBooked())
            .isAvailable(slot.isAvailable())
            .build();
    }
}
