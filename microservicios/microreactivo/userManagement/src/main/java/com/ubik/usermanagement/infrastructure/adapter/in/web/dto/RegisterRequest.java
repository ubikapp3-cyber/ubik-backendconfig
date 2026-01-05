package com.ubik.usermanagement.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,
        
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password,
        
        @Email(message = "Email must be valid")
        @NotBlank(message = "Email is required")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email,
        
        @NotNull boolean anonymous,
        
        @NotNull(message = "roleId is required")
        @Min(value = 1, message = "roleId must be a positive number")
        Integer roleId
) {
}
