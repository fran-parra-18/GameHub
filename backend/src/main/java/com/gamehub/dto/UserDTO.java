package com.gamehub.dto;

import com.gamehub.entity.User;

import java.time.Instant;

public record UserDTO(Long id, String username, String email, Instant createdAt) {

    public static UserDTO from(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getCreatedAt());
    }
}
