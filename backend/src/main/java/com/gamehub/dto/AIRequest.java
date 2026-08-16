package com.gamehub.dto;

import jakarta.validation.constraints.NotBlank;

public record AIRequest(
        @NotBlank(message = "query is required")
        String query
) {
}
