package com.saloon.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BarberRequest {
    @NotNull(message = "Salon ID is required")
    private Long salonId;

    private Long userId;
    private String bio;
    private Integer experienceYears;
    private List<String> specializations;
    
    @jakarta.validation.constraints.NotBlank(message = "Name is required")
    private String name;
    
    @jakarta.validation.constraints.NotBlank(message = "Email is required")
    @jakarta.validation.constraints.Email(message = "Valid email is required")
    private String email;
    
    @jakarta.validation.constraints.NotBlank(message = "Password is required")
    private String password;
    
    private String profilePicture;
}
