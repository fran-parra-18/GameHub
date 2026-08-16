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
        if (StringUtils.hasText(category)) {
            return gameRepository.findByGenreIgnoreCase(category);
        }
        if (StringUtils.hasText(platform)) {
            return gameRepository.findByPlatformContainingIgnoreCase(platform);
        }
        return gameRepository.findAll();
    }

    public Game getGame(Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Game not found with id: " + id));
    }
}
