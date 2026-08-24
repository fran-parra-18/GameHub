package com.gamehub.service;

import com.gamehub.entity.Game;
import com.gamehub.dto.GameSyncDTO;
import com.gamehub.integration.FreeToGameClient;
import com.gamehub.integration.FreeToGameItem;
import com.gamehub.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Consumes the FreeToGame API and upserts games into PostgreSQL/H2.
 * The browser never talks to FreeToGame directly - only the backend does.
 */
@Service
public class FreeToGameService {

    private static final Logger log = LoggerFactory.getLogger(FreeToGameService.class);
    private final FreeToGameClient freeToGameClient;
    private final GameRepository gameRepository;

    public FreeToGameService(FreeToGameClient freeToGameClient, GameRepository gameRepository) {
        this.freeToGameClient = freeToGameClient;
        this.gameRepository = gameRepository;
    }

    @Transactional
    public GameSyncDTO syncGames() {
        List<FreeToGameItem> items = freeToGameClient.fetchGames();
        Map<Integer, Game> existingByExternalId = new HashMap<>();
        gameRepository.findAll().forEach(game -> existingByExternalId.put(game.getExternalId(), game));

        List<Game> gamesToSave = new ArrayList<>();
        Set<Integer> processedIds = new HashSet<>();
        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (FreeToGameItem item : items) {
            if (!isValid(item) || !processedIds.add(item.id())) {
                skipped++;
                continue;
            }

            Game game = existingByExternalId.get(item.id());
            if (game == null) {
                game = new Game();
                game.setExternalId(item.id());
                existingByExternalId.put(item.id(), game);
                created++;
            } else {
                updated++;
            }
            apply(item, game);
            gamesToSave.add(game);
        }

        gameRepository.saveAll(gamesToSave);
        log.info("FreeToGame sync completed: {} created, {} updated, {} skipped", created, updated, skipped);
        return new GameSyncDTO(items.size(), created, updated, skipped);
    }

    private boolean isValid(FreeToGameItem item) {
        return item != null && item.id() != null && item.id() > 0 && StringUtils.hasText(item.title());
    }

    private void apply(FreeToGameItem item, Game game) {
        game.setTitle(item.title().trim());
        game.setDescription(normalize(item.short_description()));
        game.setGenre(normalize(item.genre()));
        game.setPlatform(normalize(item.platform()));
        game.setDeveloper(normalize(item.developer()));
        game.setPublisher(normalize(item.publisher()));
        game.setThumbnailUrl(normalize(item.thumbnail()));
        game.setGameUrl(normalize(item.game_url()));
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
