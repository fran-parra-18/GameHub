package com.gamehub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamehub.entity.Game;
import com.gamehub.exception.ExternalServiceException;
import com.gamehub.integration.FreeToGameClient;
import com.gamehub.integration.GeminiClient;
import com.gamehub.repository.CommentRepository;
import com.gamehub.repository.FavoriteRepository;
import com.gamehub.repository.GameRepository;
import com.gamehub.repository.UserRepository;
import com.gamehub.service.AiRawResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AIGameFinderTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired GameRepository gameRepository;
    @Autowired CommentRepository commentRepository;
    @Autowired FavoriteRepository favoriteRepository;
    @Autowired UserRepository userRepository;

    @MockitoBean GeminiClient geminiClient;
    @MockitoBean FreeToGameClient freeToGameClient;

    private List<Game> games;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        favoriteRepository.deleteAll();
        userRepository.deleteAll();
        gameRepository.deleteAll();
        games = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            Game game = new Game();
            game.setExternalId(9500 + i);
            game.setTitle("AI Game " + i);
            game.setGenre(i % 2 == 0 ? "Strategy" : "Casual");
            game.setPlatform("Web Browser");
            game.setDescription("A useful local catalog description " + i);
            games.add(gameRepository.save(game));
        }
    }

    @Test
    void validPublicRequestTrimsQueryResolvesLocalGamesDeduplicatesAndLimitsToFive() throws Exception {
        List<AiRawResponse.RawRecommendation> choices = new ArrayList<>();
        choices.add(new AiRawResponse.RawRecommendation(999999L, "hallucinated"));
        choices.add(new AiRawResponse.RawRecommendation(games.get(0).getId(), " first reason "));
        choices.add(new AiRawResponse.RawRecommendation(games.get(0).getId(), "duplicate"));
        choices.add(new AiRawResponse.RawRecommendation(null, "malformed"));
        choices.add(new AiRawResponse.RawRecommendation(games.get(1).getId(), "reason 2"));
        choices.add(new AiRawResponse.RawRecommendation(games.get(2).getId(), "reason 3"));
        choices.add(new AiRawResponse.RawRecommendation(games.get(3).getId(), "reason 4"));
        choices.add(new AiRawResponse.RawRecommendation(games.get(4).getId(), "reason 5"));
        choices.add(new AiRawResponse.RawRecommendation(games.get(5).getId(), "reason 6"));
        when(geminiClient.recommend(anyString())).thenReturn(new AiRawResponse(choices));

        mockMvc.perform(post("/api/ai/find")
                        .header("Authorization", "Bearer malformed-but-irrelevant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new QueryBody("  relaxing strategy  "))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations.length()").value(5))
                .andExpect(jsonPath("$.recommendations[0].game.id").value(games.get(0).getId()))
                .andExpect(jsonPath("$.recommendations[0].game.title").value("AI Game 1"))
                .andExpect(jsonPath("$.recommendations[0].reason").value("first reason"))
                .andExpect(jsonPath("$.recommendations[0].game.password").doesNotExist());

        ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);
        verify(geminiClient).recommend(prompt.capture());
        assertThat(prompt.getValue()).contains("User request: \"relaxing strategy\"")
                .contains("\"id\":" + games.get(0).getId())
                .contains("useful local catalog description");
    }

    @Test
    void blankAndOverlongQueriesReturn400WithoutCallingGemini() throws Exception {
        request("   ").andExpect(status().isBadRequest());
        request("x".repeat(501)).andExpect(status().isBadRequest());
        verify(geminiClient, never()).recommend(anyString());
    }

    @Test
    void geminiFailureReturnsGracefulBadGateway() throws Exception {
        when(geminiClient.recommend(anyString()))
                .thenThrow(new ExternalServiceException("AI Game Finder is temporarily unavailable"));
        request("strategy").andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("AI Game Finder is temporarily unavailable"));
    }

    @Test
    void emptyCatalogReturnsEmptyResultWithoutCallingGemini() throws Exception {
        gameRepository.deleteAll();
        request("anything").andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations.length()").value(0));
        verify(geminiClient, never()).recommend(anyString());
    }

    private org.springframework.test.web.servlet.ResultActions request(String query) throws Exception {
        return mockMvc.perform(post("/api/ai/find")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new QueryBody(query))));
    }

    private record QueryBody(String query) {}
}
