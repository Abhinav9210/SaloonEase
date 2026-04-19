package com.saloon.service;

import com.saloon.dto.request.ServiceRequest;
import com.saloon.dto.response.ServiceResponse;
import com.saloon.entity.Salon;
import com.saloon.exception.ResourceNotFoundException;
import com.saloon.exception.UnauthorizedException;
import com.saloon.repository.ServiceRepository;
import com.saloon.repository.SalonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceManagementService {

    private final ServiceRepository serviceRepository;
    private final SalonRepository salonRepository;

    @Transactional
    public ServiceResponse addService(Long salonId, ServiceRequest request, String ownerEmail) {
        Salon salon = getSalonAndVerifyOwner(salonId, ownerEmail);
        com.saloon.entity.Service service = com.saloon.entity.Service.builder()
            .salon(salon)
            .name(request.getName())
            .description(request.getDescription())
            .durationMinutes(request.getDurationMinutes())
            .price(request.getPrice())
            .build();
        return mapToResponse(serviceRepository.save(service));
    }

    @Transactional
    public ServiceResponse updateService(Long serviceId, ServiceRequest request, String ownerEmail) {
        com.saloon.entity.Service service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Service", serviceId));
        if (!service.getSalon().getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("Not authorized");
        }
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setDurationMinutes(request.getDurationMinutes());
        service.setPrice(request.getPrice());
        return mapToResponse(serviceRepository.save(service));
    }

    public List<ServiceResponse> getServicesBySalon(Long salonId) {
        return serviceRepository.findBySalonIdAndIsActive(salonId, true).stream()
            .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteService(Long serviceId, String ownerEmail) {
        com.saloon.entity.Service service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Service", serviceId));
        if (!service.getSalon().getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("Not authorized");
        }
        service.setActive(false);
        serviceRepository.save(service);
    }

    private Salon getSalonAndVerifyOwner(Long salonId, String ownerEmail) {
        Salon salon = salonRepository.findById(salonId)
            .orElseThrow(() -> new ResourceNotFoundException("Salon", salonId));
        if (!salon.getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("You don't own this salon");
        }
        return salon;
    }

    private ServiceResponse mapToResponse(com.saloon.entity.Service service) {
        return ServiceResponse.builder()
            .id(service.getId())
            .salonId(service.getSalon().getId())
            .salonName(service.getSalon().getName())
            .name(service.getName())
            .description(service.getDescription())
            .durationMinutes(service.getDurationMinutes())
            .price(service.getPrice())
            .isActive(service.isActive())
            .createdAt(service.getCreatedAt())
            .build();
    }
}
