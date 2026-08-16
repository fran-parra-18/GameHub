package com.gamehub.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "email or username is required")
        String email,
        @NotBlank(message = "password is required")
        String password
) {
}
