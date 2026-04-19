package com.saloon.service;

import com.saloon.dto.request.SalonRequest;
import com.saloon.dto.response.SalonResponse;
import com.saloon.entity.Salon;
import com.saloon.entity.User;
import com.saloon.entity.enums.SalonStatus;
import com.saloon.exception.ResourceNotFoundException;
import com.saloon.exception.UnauthorizedException;
import com.saloon.repository.SalonRepository;
import com.saloon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalonService {

    private final SalonRepository salonRepository;
    private final UserRepository userRepository;

    @Transactional
    public SalonResponse createSalon(SalonRequest request, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Salon salon = Salon.builder()
            .owner(owner)
            .name(request.getName())
            .description(request.getDescription())
            .address(request.getAddress())
            .city(request.getCity())
            .state(request.getState())
            .pincode(request.getPincode())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .phone(request.getPhone())
            .openTime(request.getOpenTime())
            .closeTime(request.getCloseTime())
            .minimumBookingFee(request.getMinimumBookingFee() != null ? request.getMinimumBookingFee() : 100.0)
            .images(request.getImages() != null ? request.getImages() : List.of())
            .workingDays(request.getWorkingDays() != null ? request.getWorkingDays() : List.of())
            .build();
        salon = salonRepository.save(salon);
        log.info("Salon created: {} by {}", salon.getName(), ownerEmail);
        return mapToResponse(salon);
    }

    @Transactional
    public SalonResponse updateSalon(Long salonId, SalonRequest request, String ownerEmail) {
        Salon salon = getSalonById(salonId);
        if (!salon.getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("You don't own this salon");
        }
        salon.setName(request.getName());
        salon.setDescription(request.getDescription());
        salon.setAddress(request.getAddress());
        salon.setCity(request.getCity());
        salon.setState(request.getState());
        salon.setPincode(request.getPincode());
        salon.setLatitude(request.getLatitude());
        salon.setLongitude(request.getLongitude());
        salon.setPhone(request.getPhone());
        salon.setOpenTime(request.getOpenTime());
        salon.setCloseTime(request.getCloseTime());
        if (request.getMinimumBookingFee() != null) salon.setMinimumBookingFee(request.getMinimumBookingFee());
        if (request.getImages() != null) salon.setImages(request.getImages());
        if (request.getWorkingDays() != null) salon.setWorkingDays(request.getWorkingDays());
        return mapToResponse(salonRepository.save(salon));
    }

    public SalonResponse getSalon(Long id) {
        return mapToResponse(getSalonById(id));
    }

    public Page<SalonResponse> getApprovedSalons(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        return salonRepository.findByStatus(SalonStatus.APPROVED, pageable).map(this::mapToResponse);
    }

    public Page<SalonResponse> searchSalons(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return salonRepository.searchApprovedSalons(query, pageable).map(this::mapToResponse);
    }

    public List<SalonResponse> getOwnerSalons(String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return salonRepository.findByOwnerId(owner.getId()).stream()
            .map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<SalonResponse> getTopRatedSalons() {
        return salonRepository.findTopRatedApproved(PageRequest.of(0, 10)).stream()
            .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public SalonResponse updateStatus(Long salonId, SalonStatus status) {
        Salon salon = getSalonById(salonId);
        salon.setStatus(status);
        return mapToResponse(salonRepository.save(salon));
    }

    @Transactional
    public void updateRating(Long salonId, Double newRating, Integer totalReviews) {
        Salon salon = getSalonById(salonId);
        salon.setRating(newRating);
        salon.setTotalReviews(totalReviews);
        salonRepository.save(salon);
    }

    public Salon getSalonById(Long id) {
        return salonRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Salon", id));
    }

    private SalonResponse mapToResponse(Salon salon) {
        return SalonResponse.builder()
            .id(salon.getId())
            .ownerId(salon.getOwner().getId())
            .ownerName(salon.getOwner().getName())
            .name(salon.getName())
            .description(salon.getDescription())
            .address(salon.getAddress())
            .city(salon.getCity())
            .state(salon.getState())
            .pincode(salon.getPincode())
            .latitude(salon.getLatitude())
            .longitude(salon.getLongitude())
            .rating(salon.getRating())
            .totalReviews(salon.getTotalReviews())
            .status(salon.getStatus())
            .phone(salon.getPhone())
            .openTime(salon.getOpenTime())
            .closeTime(salon.getCloseTime())
            .minimumBookingFee(salon.getMinimumBookingFee())
            .images(salon.getImages())
            .workingDays(salon.getWorkingDays())
            .createdAt(salon.getCreatedAt())
            .build();
    }
}
