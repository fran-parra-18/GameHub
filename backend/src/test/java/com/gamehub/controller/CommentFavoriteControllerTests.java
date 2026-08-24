package com.gamehub.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamehub.entity.Game;
import com.gamehub.integration.FreeToGameClient;
import com.gamehub.repository.CommentRepository;
import com.gamehub.repository.FavoriteRepository;
import com.gamehub.repository.GameRepository;
import com.gamehub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CommentFavoriteControllerTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CommentRepository commentRepository;
    @Autowired FavoriteRepository favoriteRepository;
    @Autowired GameRepository gameRepository;
    @Autowired UserRepository userRepository;

    @MockitoBean FreeToGameClient freeToGameClient;

    private Game firstGame;
    private Game secondGame;
    private String firstToken;
    private String secondToken;

    @BeforeEach
    void setUp() throws Exception {
        commentRepository.deleteAll();
        favoriteRepository.deleteAll();
        userRepository.deleteAll();
        gameRepository.deleteAll();
        firstGame = saveGame(9101, "First Game");
        secondGame = saveGame(9102, "Second Game");
        firstToken = register("first-user", "first@example.com");
        secondToken = register("second-user", "second@example.com");
    }

    @Test
    void commentsArePublicScopedToTheirGameAndContainOnlySafeUserData() throws Exception {
        createComment(firstGame.getId(), firstToken, "older comment", null).andExpect(status().isCreated());
        createComment(firstGame.getId(), firstToken, "newer comment", null).andExpect(status().isCreated());
        createComment(secondGame.getId(), secondToken, "other game", null).andExpect(status().isCreated());

        mockMvc.perform(get("/api/games/{gameId}/comments", firstGame.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].content").value("newer comment"))
                .andExpect(jsonPath("$[0].gameId").value(firstGame.getId()))
                .andExpect(jsonPath("$[0].username").value("first-user"))
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());

        mockMvc.perform(get("/api/games/{gameId}/comments", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void authenticatedCommentUsesJwtUserAndCannotBeImpersonated() throws Exception {
        Long secondUserId = userRepository.findByUsernameIgnoreCase("second-user").orElseThrow().getId();
        Long firstUserId = userRepository.findByUsernameIgnoreCase("first-user").orElseThrow().getId();
        createComment(firstGame.getId(), firstToken, "  JWT owns this  ", secondUserId)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("JWT owns this"))
                .andExpect(jsonPath("$.username").value("first-user"))
                .andExpect(jsonPath("$.userId").value(
                        firstUserId));

        assertThat(commentRepository.findAll()).singleElement()
                .satisfies(comment -> assertThat(comment.getUser().getId()).isEqualTo(firstUserId));
    }

    @Test
    void commentValidationUnknownGameAndAuthenticationErrorsAreHandled() throws Exception {
        createComment(firstGame.getId(), firstToken, "   ", null).andExpect(status().isBadRequest());
        createComment(999999L, firstToken, "valid", null).andExpect(status().isNotFound());
        createComment(firstGame.getId(), null, "valid", null).andExpect(status().isUnauthorized());
        createComment(firstGame.getId(), "malformed", "valid", null).andExpect(status().isUnauthorized());
    }

    @Test
    void favoriteAddIsIdempotentAndDatabaseHasOneRow() throws Exception {
        addFavorite(firstGame.getId(), firstToken).andExpect(status().isCreated());
        addFavorite(firstGame.getId(), firstToken).andExpect(status().isCreated());

        Long userId = userRepository.findByUsernameIgnoreCase("first-user").orElseThrow().getId();
        assertThat(favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId)).hasSize(1);
        mockMvc.perform(get("/api/users/me/favorites").header("Authorization", bearer(firstToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("First Game"))
                .andExpect(jsonPath("$[0].game").doesNotExist());
    }

    @Test
    void favoritesAreIsolatedByJwtUserAndDeleteIsIdempotent() throws Exception {
        addFavorite(firstGame.getId(), firstToken).andExpect(status().isCreated());
        addFavorite(secondGame.getId(), secondToken).andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/me/favorites").header("Authorization", bearer(firstToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("First Game"));
        mockMvc.perform(get("/api/users/me/favorites").header("Authorization", bearer(secondToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Second Game"));

        removeFavorite(firstGame.getId(), firstToken).andExpect(status().isOk());
        removeFavorite(firstGame.getId(), firstToken).andExpect(status().isOk());
        mockMvc.perform(get("/api/users/me/favorites").header("Authorization", bearer(firstToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void favoriteUnknownGameAndAuthenticationErrorsAreHandled() throws Exception {
        addFavorite(999999L, firstToken).andExpect(status().isNotFound());
        removeFavorite(999999L, firstToken).andExpect(status().isNotFound());
        addFavorite(firstGame.getId(), null).andExpect(status().isUnauthorized());
        addFavorite(firstGame.getId(), "malformed").andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/users/me/favorites")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/users/me/favorites").header("Authorization", bearer("malformed")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void phaseThreeAndPublicCatalogRemainAccessible() throws Exception {
        mockMvc.perform(get("/api/users/me").header("Authorization", bearer(firstToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username").value("first-user"));
        mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
    }

    private Game saveGame(int externalId, String title) {
        Game game = new Game();
        game.setExternalId(externalId);
        game.setTitle(title);
        return gameRepository.save(game);
    }

    private String register(String username, String email) throws Exception {
        String body = objectMapper.writeValueAsString(new RegisterBody(username, email, "secret123"));
        String response = mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    private org.springframework.test.web.servlet.ResultActions createComment(
            Long gameId, String token, String content, Long claimedUserId) throws Exception {
        JsonNode body = objectMapper.valueToTree(new CommentBody(content, claimedUserId));
        var request = post("/api/games/{gameId}/comments", gameId)
                .contentType(MediaType.APPLICATION_JSON).content(body.toString());
        if (token != null) request.header("Authorization", bearer(token));
        return mockMvc.perform(request);
    }

    private org.springframework.test.web.servlet.ResultActions addFavorite(Long gameId, String token) throws Exception {
        var request = post("/api/games/{gameId}/favorite", gameId);
        if (token != null) request.header("Authorization", bearer(token));
        return mockMvc.perform(request);
    }

    private org.springframework.test.web.servlet.ResultActions removeFavorite(Long gameId, String token) throws Exception {
        var request = delete("/api/games/{gameId}/favorite", gameId);
        if (token != null) request.header("Authorization", bearer(token));
        return mockMvc.perform(request);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record RegisterBody(String username, String email, String password) {}
    private record CommentBody(String content, Long userId) {}
}
