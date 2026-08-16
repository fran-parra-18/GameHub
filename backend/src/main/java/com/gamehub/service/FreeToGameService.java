package com.gamehub.service;

import com.gamehub.entity.Game;
import com.gamehub.exception.ExternalServiceException;
import com.gamehub.integration.FreeToGameItem;
import com.gamehub.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Consumes the FreeToGame API and upserts games into PostgreSQL/H2.
 * The browser never talks to FreeToGame directly - only the backend does.
 */
@Service
public class FreeToGameService {

    private static final Logger log = LoggerFactory.getLogger(FreeToGameService.class);
    private static final String API_URL = "https://www.freetogame.com/api/games";

    private final RestClient restClient;
    private final GameRepository gameRepository;

    public FreeToGameService(RestClient.Builder restClientBuilder, GameRepository gameRepository) {
        this.restClient = restClientBuilder.build();
        this.gameRepository = gameRepository;
    }

    /** Fetches games from FreeToGame and upserts them, avoiding duplicates via externalId. */
    public List<Game> syncGames() {
        List<FreeToGameItem> items = fetch();
        List<Game> synced = new ArrayList<>();
        int created = 0;
        int updated = 0;

        for (FreeToGameItem item : items) {
            if (item.id() == null) {
                continue;
            }
            Optional<Game> existing = gameRepository.findByExternalId(item.id());
            Game game;
            if (existing.isPresent()) {
                game = existing.get();
                apply(item, game);
                updated++;
            } else {
                game = new Game();
                game.setExternalId(item.id());
                apply(item, game);
                created++;
            }
            synced.add(gameRepository.save(game));
        }

        log.info("FreeToGame sync completed: {} created, {} updated", created, updated);
        return synced;
    }

    private List<FreeToGameItem> fetch() {
        try {
            List<FreeToGameItem> items = restClient.get()
                    .uri(API_URL)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<>() {
                    });
            if (items == null) {
                throw new ExternalServiceException("FreeToGame returned an empty response");
            }
            return items;
        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch games from FreeToGame", e);
            throw new ExternalServiceException("Could not reach FreeToGame API. Please try again later.");
        }
    }

    private void apply(FreeToGameItem item, Game game) {
        game.setTitle(item.title());
        game.setDescription(item.short_description());
        game.setGenre(item.genre());
        game.setPlatform(item.platform());
        game.setDeveloper(item.developer());
        game.setPublisher(item.publisher());
        game.setThumbnailUrl(item.thumbnail());
        game.setGameUrl(item.game_url());
    }
}
