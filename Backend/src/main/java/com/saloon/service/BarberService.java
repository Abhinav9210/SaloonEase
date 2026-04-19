package com.saloon.service;

import com.saloon.dto.request.BarberRequest;
import com.saloon.dto.response.BarberResponse;
import com.saloon.entity.Barber;
import com.saloon.entity.Salon;
import com.saloon.entity.User;
import com.saloon.exception.BusinessException;
import com.saloon.exception.ResourceNotFoundException;
import com.saloon.exception.UnauthorizedException;
import com.saloon.repository.BarberRepository;
import com.saloon.repository.SalonRepository;
import com.saloon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
@Slf4j
public class BarberService {

    private final BarberRepository barberRepository;
    private final SalonRepository salonRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public BarberResponse addBarber(BarberRequest request, String ownerEmail) {
        Salon salon = salonRepository.findById(request.getSalonId())
            .orElseThrow(() -> new ResourceNotFoundException("Salon", request.getSalonId()));
        if (!salon.getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("You don't own this salon");
        }
        User barberUser = null;
        if (request.getUserId() != null) {
            barberUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));
            if (barberRepository.existsByUserIdAndSalonId(request.getUserId(), request.getSalonId())) {
                throw new BusinessException("This user is already a barber in this salon");
            }
        } else {
            Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent()) {
                barberUser = existingUser.get();
                if (barberRepository.existsByUserIdAndSalonId(barberUser.getId(), request.getSalonId())) {
                    throw new BusinessException("This user is already a barber in this salon");
                }
            } else {
                barberUser = User.builder()
                        .name(request.getName())
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .role(com.saloon.entity.enums.Role.BARBER)
                        .isActive(true)
                        .isEmailVerified(true) // auto verify for internal creation
                        .build();
                barberUser = userRepository.save(barberUser);
            }
        }
        Barber barber = Barber.builder()
            .salon(salon)
            .user(barberUser)
            .name(request.getName())
            .email(request.getEmail())
            .bio(request.getBio())
            .experienceYears(request.getExperienceYears() != null ? request.getExperienceYears() : 0)
            .specializations(request.getSpecializations() != null ? request.getSpecializations() : List.of())
            .profilePicture(request.getProfilePicture())
            .build();
        barber = barberRepository.save(barber);
        log.info("Barber added: {} to salon: {}", barber.getId(), salon.getName());
        return mapToResponse(barber);
    }

    @Transactional
    public BarberResponse updateBarber(Long barberId, BarberRequest request, String ownerEmail) {
        Barber barber = getBarberEntity(barberId);
        if (!barber.getSalon().getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("Not authorized to update this barber");
        }
        if (request.getName() != null) barber.setName(request.getName());
        if (request.getEmail() != null) barber.setEmail(request.getEmail());
        barber.setBio(request.getBio());
        barber.setExperienceYears(request.getExperienceYears());
        if (request.getSpecializations() != null) barber.setSpecializations(request.getSpecializations());
        if (request.getProfilePicture() != null) barber.setProfilePicture(request.getProfilePicture());
        return mapToResponse(barberRepository.save(barber));
    }

    public List<BarberResponse> getBarbersBySalon(Long salonId) {
        return barberRepository.findBySalonIdOrderByRatingDesc(salonId).stream()
            .map(this::mapToResponse).collect(Collectors.toList());
    }

    public BarberResponse getBarber(Long barberId) {
        return mapToResponse(getBarberEntity(barberId));
    }

    public BarberResponse getBarberByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        Barber barber = barberRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Barber profile not found for user: " + email));
        return mapToResponse(barber);
    }

    @Transactional
    public void toggleAvailability(Long barberId, String email) {
        Barber barber = getBarberEntity(barberId);
        boolean isOwner = barber.getSalon().getOwner().getEmail().equals(email);
        boolean isSelf = barber.getUser() != null && barber.getUser().getEmail().equals(email);
        if (!isOwner && !isSelf) {
            throw new UnauthorizedException("Not authorized");
        }
        barber.setAvailable(!barber.isAvailable());
        barberRepository.save(barber);
    }

    @Transactional
    public void deleteBarber(Long barberId, String ownerEmail) {
        Barber barber = getBarberEntity(barberId);
        if (!barber.getSalon().getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("Not authorized to remove this barber");
        }
        barberRepository.delete(barber);
    }

    @Transactional
    public void updateRating(Long barberId, Double rating, Integer totalReviews) {
        Barber barber = getBarberEntity(barberId);
        barber.setRating(rating);
        barber.setTotalReviews(totalReviews);
        barberRepository.save(barber);
    }

    public Barber getBarberEntity(Long id) {
        return barberRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Barber", id));
    }

    private BarberResponse mapToResponse(Barber barber) {
        return BarberResponse.builder()
            .id(barber.getId())
            .salonId(barber.getSalon().getId())
            .salonName(barber.getSalon().getName())
            .userId(barber.getUser() != null ? barber.getUser().getId() : null)
            .name(barber.getUser() != null ? barber.getUser().getName() : (barber.getName() != null ? barber.getName() : "Barber #" + barber.getId()))
            .email(barber.getUser() != null ? barber.getUser().getEmail() : barber.getEmail())
            .bio(barber.getBio())
            .experienceYears(barber.getExperienceYears())
            .specializations(barber.getSpecializations())
            .rating(barber.getRating())
            .totalReviews(barber.getTotalReviews())
            .isAvailable(barber.isAvailable())
            .profilePicture(barber.getProfilePicture())
            .createdAt(barber.getCreatedAt())
            .build();
    }
}
