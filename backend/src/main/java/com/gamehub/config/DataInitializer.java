package com.gamehub.config;

import com.gamehub.repository.GameRepository;
import com.gamehub.service.FreeToGameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * On startup, if the catalog is empty, try to populate it from FreeToGame.
 * This makes the app usable out of the box. If the external API is
 * unreachable, the app still starts and /api/games/sync can be called later.
 */
@Component
@ConditionalOnProperty(
        name = "gamehub.catalog.sync-on-startup",
        havingValue = "true"
)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final GameRepository gameRepository;
    private final FreeToGameService freeToGameService;

    public DataInitializer(GameRepository gameRepository, FreeToGameService freeToGameService) {
        this.gameRepository = gameRepository;
        this.freeToGameService = freeToGameService;
    }

    @Override
    public void run(String... args) {
        if (gameRepository.count() > 0) {
            log.info("Catalog already populated ({} games). Skipping initial sync.", gameRepository.count());
            return;
        }
        log.info("Catalog is empty. Attempting initial sync from FreeToGame...");
        try {
            freeToGameService.syncGames();
            log.info("Initial sync complete. {} games in catalog.", gameRepository.count());
        } catch (Exception e) {
            log.warn("Initial FreeToGame sync failed ({}). "
                    + "You can trigger it later via POST /api/games/sync.", e.getMessage());
        }
    }
}
