package com.gamehub.controller;

import com.gamehub.dto.GameDTO;
import com.gamehub.entity.Game;
import com.gamehub.service.FreeToGameService;
import com.gamehub.service.GameService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;
    private final FreeToGameService freeToGameService;

    public GameController(GameService gameService, FreeToGameService freeToGameService) {
        this.gameService = gameService;
        this.freeToGameService = freeToGameService;
    }

    @GetMapping
    public List<GameDTO> list(@RequestParam(required = false) String category,
                              @RequestParam(required = false) String platform) {
        return gameService.getGames(category, platform).stream().map(GameDTO::from).toList();
    }

    @GetMapping("/{id}")
    public GameDTO get(@PathVariable Long id) {
        return GameDTO.from(gameService.getGame(id));
    }

    /** Triggers synchronization from the FreeToGame API into the database. */
    @PostMapping("/sync")
    public Map<String, Object> sync() {
        List<Game> games = freeToGameService.syncGames();
        return Map.of("synced", games.size());
    }
}
