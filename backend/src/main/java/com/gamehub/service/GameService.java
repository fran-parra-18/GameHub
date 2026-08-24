package com.gamehub.service;

import com.gamehub.entity.Game;
import com.gamehub.exception.NotFoundException;
import com.gamehub.repository.GameRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class GameService {

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

    public Game getGame(Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Game not found with id: " + id));
    }
}
