package com.gamehub.service;

import com.gamehub.entity.Game;
import com.gamehub.exception.NotFoundException;
import com.gamehub.repository.GameRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class GameService {

    /** externalId reservado (negativo) para el juego local; FreeToGame solo usa ids positivos. */
    public static final int ORIGINAL_EXTERNAL_ID = -1;

    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public List<Game> getGames(String category, String platform) {
        boolean hasCategory = StringUtils.hasText(category);
        boolean hasPlatform = StringUtils.hasText(platform);
        if (hasCategory && hasPlatform) {
            return gameRepository.findByGenreIgnoreCaseAndPlatformContainingIgnoreCase(
                    category.trim(), platform.trim());
        }
        if (hasCategory) {
            return gameRepository.findByGenreIgnoreCase(category.trim());
        }
        if (hasPlatform) {
            return gameRepository.findByPlatformContainingIgnoreCase(platform.trim());
        }
        return gameRepository.findAll();
    }

    /**
     * Devuelve el juego local "Connect Four" (el juego original de GameHub),
     * creandolo la primera vez. Asi tiene id propio y puede recibir comentarios y favoritos.
     */
    public Game getOrCreateOriginal() {
        return gameRepository.findByExternalId(ORIGINAL_EXTERNAL_ID).orElseGet(() -> {
            Game game = new Game();
            game.setExternalId(ORIGINAL_EXTERNAL_ID);
            game.setTitle("Connect Four: Deadpool vs Wolverine");
            game.setGenre("GameHub Original");
            game.setPlatform("Web Browser");
            game.setDeveloper("GameHub");
            game.setPublisher("GameHub");
            game.setDescription("4 en línea con Deadpool y Wolverine. Dos jugadores se turnan para "
                    + "alinear cuatro fichas antes que su oponente. Juego local de GameHub.");
            game.setThumbnailUrl("Images/cardDeadpool.jpg");
            game.setGameUrl("game.html");
            return gameRepository.save(game);
        });
    }

    public Game getGame(Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Game not found with id: " + id));
    }
}
