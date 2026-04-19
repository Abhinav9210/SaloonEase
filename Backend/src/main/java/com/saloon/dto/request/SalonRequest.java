package com.saloon.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class SalonRequest {
    @NotBlank(message = "Salon name is required")
    private String name;

    private String description;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    private String state;
    private String pincode;
    private Double latitude;
    private Double longitude;
    private String phone;
    private String openTime;
    private String closeTime;
    private Double minimumBookingFee;
    private List<String> images;
    private List<String> workingDays;
}
