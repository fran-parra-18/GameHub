package com.gamehub.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamehub.entity.Game;
import com.gamehub.entity.User;
import com.gamehub.integration.FreeToGameClient;
import com.gamehub.repository.GameRepository;
import com.gamehub.repository.UserRepository;
import com.gamehub.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserAuthenticationTests {

    private static final String JWT_SECRET = "dev-secret-gamehub-change-me-in-production-0123456789";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private FreeToGameClient freeToGameClient;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registrationReturnsJwtAndPersistsOnlyABcryptHash() throws Exception {
        String response = register("  PlayerOne  ", "PLAYER@Example.com", "secret123")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.username").value("PlayerOne"))
                .andExpect(jsonPath("$.user.email").value("player@example.com"))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        assertThat(json.get("token").asText().split("\\.")).hasSize(3);

        User saved = userRepository.findByUsernameIgnoreCase("playerone").orElseThrow();
        assertThat(saved.getPasswordHash())
                .isNotEqualTo("secret123")
                .startsWith("$2");
        assertThat(passwordEncoder.matches("secret123", saved.getPasswordHash())).isTrue();
    }

    @Test
    void duplicateUsernameAndEmailReturnConflictCaseInsensitively() throws Exception {
        register("PlayerOne", "player@example.com", "secret123")
                .andExpect(status().isCreated());

        register("playerone", "other@example.com", "secret123")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username is already taken"));

        register("OtherPlayer", "PLAYER@example.com", "secret123")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email is already registered"));
    }

    @Test
    void invalidRegistrationInputReturns400() throws Exception {
        register("", "not-an-email", "123")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.username").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void loginWorksWithUsernameOrEmailAndRejectsBadCredentials() throws Exception {
        register("PlayerOne", "player@example.com", "secret123")
                .andExpect(status().isCreated());

        login("playerone", "secret123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.username").value("PlayerOne"));

        login("PLAYER@EXAMPLE.COM", "secret123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        login("PlayerOne", "wrong-password")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));

        login("unknown", "secret123")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void validJwtCanAccessMeButMissingMalformedAndExpiredTokensCannot() throws Exception {
        String body = register("PlayerOne", "player@example.com", "secret123")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(body).get("token").asText();

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("PlayerOne"))
                .andExpect(jsonPath("$.email").value("player@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication required"));

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer malformed-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));

        User user = userRepository.findByUsernameIgnoreCase("PlayerOne").orElseThrow();
        String expiredToken = new JwtUtil(JWT_SECRET, -1).generateToken(user.getId());
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }

    @Test
    void phaseTwoGameEndpointsRemainPublic() throws Exception {
        gameRepository.deleteAll();
        Game game = new Game();
        game.setExternalId(7001);
        game.setTitle("Public Game");
        game = gameRepository.save(game);

        mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Public Game"));

        mockMvc.perform(get("/api/games/{id}", game.getId())
                        .header("Authorization", "Bearer malformed-but-ignored-on-public-route"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Public Game"));

        when(freeToGameClient.fetchGames()).thenReturn(List.of());
        mockMvc.perform(post("/api/games/sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.received").value(0));
    }

    private org.springframework.test.web.servlet.ResultActions register(
            String username, String email, String password) throws Exception {
        return mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegistrationBody(username, email, password))));
    }

    private org.springframework.test.web.servlet.ResultActions login(String login, String password) throws Exception {
        return mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginBody(login, password))));
    }

    private record RegistrationBody(String username, String email, String password) {
    }

    private record LoginBody(String email, String password) {
    }
}
