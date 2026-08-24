package com.gamehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AIRequest(
        @NotBlank(message = "query is required")
        @Size(max = 500, message = "query must not exceed 500 characters")
        String query
) {
}
