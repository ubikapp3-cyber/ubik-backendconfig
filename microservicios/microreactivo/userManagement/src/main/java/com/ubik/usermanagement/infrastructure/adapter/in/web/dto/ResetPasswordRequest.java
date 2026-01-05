package com.ubik.usermanagement.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Token is required")
        @Size(max = 500, message = "Token must not exceed 500 characters")
        String token,
        
        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String newPassword
) {
}
