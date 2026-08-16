package com.gamehub.dto;

import com.gamehub.entity.Game;

public record GameDTO(
        Long id,
        Integer externalId,
        String title,
        String description,
        String genre,
        String platform,
        String developer,
        String publisher,
        String thumbnailUrl,
        String gameUrl
) {

    public static GameDTO from(Game game) {
        return new GameDTO(
                game.getId(),
                game.getExternalId(),
                game.getTitle(),
                game.getDescription(),
                game.getGenre(),
                game.getPlatform(),
                game.getDeveloper(),
                game.getPublisher(),
                game.getThumbnailUrl(),
                game.getGameUrl()
        );
    }
}
