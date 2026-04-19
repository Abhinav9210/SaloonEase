package com.saloon.dto.response;

import com.saloon.entity.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String profilePicture;
    private Role role;
    private boolean isActive;
    private LocalDateTime createdAt;
}
