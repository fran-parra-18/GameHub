package com.gamehub.service;

import com.gamehub.dto.GameDTO;
import com.gamehub.entity.Favorite;
import com.gamehub.entity.Game;
import com.gamehub.entity.User;
import com.gamehub.repository.FavoriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final GameService gameService;

    public FavoriteService(FavoriteRepository favoriteRepository, GameService gameService) {
        this.favoriteRepository = favoriteRepository;
        this.gameService = gameService;
    }

    public void addFavorite(Long gameId, User user) {
        Game game = gameService.getGame(gameId);
        if (!favoriteRepository.existsByUserIdAndGameId(user.getId(), gameId)) {
            favoriteRepository.save(new Favorite(user, game));
        }
    }

    @Transactional
    public void removeFavorite(Long gameId, User user) {
        gameService.getGame(gameId);
        favoriteRepository.deleteByUserIdAndGameId(user.getId(), gameId);
    }

    public List<GameDTO> getFavorites(User user) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(favorite -> GameDTO.from(favorite.getGame())).toList();
    }

    public boolean isFavorite(Long gameId, User user) {
        return favoriteRepository.existsByUserIdAndGameId(user.getId(), gameId);
    }
}
