package com.gamehub.integration;

import com.gamehub.exception.ExternalServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FreeToGameClientTests {

    private static final String API_URL = "https://www.freetogame.com/api/games";

    @Test
    void mapsTheExternalJsonResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FreeToGameClient client = new FreeToGameClient(builder.build());

        server.expect(once(), requestTo(API_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [{
                          "id": 42,
                          "title": "Sample Game",
                          "thumbnail": "https://example.test/game.jpg",
                          "short_description": "A sample",
                          "game_url": "https://example.test/play",
                          "genre": "Action",
                          "platform": "PC (Windows)",
                          "publisher": "Publisher",
                          "developer": "Developer",
                          "unexpected_field": "ignored"
                        }]
                        """, MediaType.APPLICATION_JSON));

        var games = client.fetchGames();

        assertThat(games).hasSize(1);
        assertThat(games.getFirst())
                .extracting(FreeToGameItem::id, FreeToGameItem::title,
                        FreeToGameItem::short_description, FreeToGameItem::game_url)
                .containsExactly(42, "Sample Game", "A sample", "https://example.test/play");
        server.verify();
    }

    @Test
    void translatesExternalFailuresWithoutLeakingDetails() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FreeToGameClient client = new FreeToGameClient(builder.build());
        server.expect(requestTo(API_URL)).andRespond(withServerError());

        assertThatThrownBy(client::fetchGames)
                .isInstanceOf(ExternalServiceException.class)
                .hasMessage("FreeToGame is temporarily unavailable");
    }
}
