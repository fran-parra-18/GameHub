package com.gamehub.controller;

import com.gamehub.entity.Game;
import com.gamehub.exception.ExternalServiceException;
import com.gamehub.integration.FreeToGameClient;
import com.gamehub.integration.FreeToGameItem;
import com.gamehub.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GameControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameRepository gameRepository;

    @MockitoBean
    private FreeToGameClient freeToGameClient;

    private Game actionPc;

    @BeforeEach
    void setUp() {
        gameRepository.deleteAll();
        actionPc = save(101, "Action PC", "Action", "PC (Windows)");
        save(102, "Action Browser", "Action", "Web Browser");
        save(103, "Racing PC", "Racing", "PC (Windows)");
    }

    @Test
    void listsGamesAsDtos() throws Exception {
        mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
    }

    @Test
    void getsOneGameAndReturns404ForUnknownId() throws Exception {
        mockMvc.perform(get("/api/games/{id}", actionPc.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Action PC"))
                .andExpect(jsonPath("$.externalId").value(101));

        mockMvc.perform(get("/api/games/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void filtersByCategoryPlatformAndBothTogether() throws Exception {
        mockMvc.perform(get("/api/games").param("category", "action"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/games").param("platform", "windows"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/games")
                        .param("category", "action")
                        .param("platform", "browser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Action Browser"));
    }

    @Test
    void syncsThroughTheExternalBoundary() throws Exception {
        when(freeToGameClient.fetchGames()).thenReturn(List.of(
                new FreeToGameItem(200, "Synced Game", null, null, null,
                        "Strategy", "Web Browser", null, null)));

        mockMvc.perform(post("/api/games/sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.received").value(1))
                .andExpect(jsonPath("$.created").value(1));
    }

    @Test
    void externalFailureReturns502AndPreservesTheCatalog() throws Exception {
        when(freeToGameClient.fetchGames())
                .thenThrow(new ExternalServiceException("FreeToGame is temporarily unavailable"));

        mockMvc.perform(post("/api/games/sync"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("FreeToGame is temporarily unavailable"))
                .andExpect(jsonPath("$.fieldErrors").doesNotExist());

        mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    private Game save(int externalId, String title, String genre, String platform) {
        Game game = new Game();
        game.setExternalId(externalId);
        game.setTitle(title);
        game.setGenre(genre);
        game.setPlatform(platform);
        return gameRepository.save(game);
    }
}
