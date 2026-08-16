package com.gamehub.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FreeToGameItem(
        Integer id,
        String title,
        String thumbnail,
        String short_description,
        String game_url,
        String genre,
        String platform,
        String publisher,
        String developer
) {
}
