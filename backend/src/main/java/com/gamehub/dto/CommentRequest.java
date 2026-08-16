package com.gamehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @NotBlank(message = "content is required")
        @Size(max = 2000, message = "content must not exceed 2000 characters")
        String content
) {
}
