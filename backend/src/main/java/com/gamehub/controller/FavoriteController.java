package com.gamehub.controller;

import com.gamehub.dto.GameDTO;
import com.gamehub.security.CurrentUser;
import com.gamehub.service.FavoriteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/api/games/{gameId}/favorite")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Boolean> add(@PathVariable Long gameId) {
        favoriteService.addFavorite(gameId, CurrentUser.require());
        return Map.of("favorite", true);
    }

    @DeleteMapping("/api/games/{gameId}/favorite")
    public Map<String, Boolean> remove(@PathVariable Long gameId) {
        favoriteService.removeFavorite(gameId, CurrentUser.require());
        return Map.of("favorite", false);
    }

    @GetMapping("/api/users/me/favorites")
    public List<GameDTO> myFavorites() {
        return favoriteService.getFavorites(CurrentUser.require());
    }
}
