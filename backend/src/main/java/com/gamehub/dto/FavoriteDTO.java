package com.gamehub.dto;

import com.gamehub.entity.Favorite;

import java.time.Instant;

public record FavoriteDTO(Long id, GameDTO game, Instant createdAt) {

    public static FavoriteDTO from(Favorite favorite) {
        return new FavoriteDTO(
                favorite.getId(),
                GameDTO.from(favorite.getGame()),
                favorite.getCreatedAt()
        );
    }
}
