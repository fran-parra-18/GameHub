package com.gamehub.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamehub.exception.ExternalServiceException;
import com.gamehub.exception.ServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeminiClientTests {

    @Test
    void mapsStructuredGeminiJsonAndRequestsJsonResponseMode() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(method(HttpMethod.POST))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("application/json")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("responseSchema")))
                .andRespond(withSuccess("""
                        {"candidates":[{"content":{"parts":[{"text":"{\\\"recommendations\\\":[{\\\"gameId\\\":42,\\\"reason\\\":\\\"Good fit\\\"}]}"}]}}]}
                        """, MediaType.APPLICATION_JSON));

        GeminiClient client = new GeminiClient(builder, new ObjectMapper(), "test-key", "test-model");
        var result = client.recommend("prompt");

        assertThat(result.recommendations()).singleElement().satisfies(item -> {
            assertThat(item.gameId()).isEqualTo(42L);
            assertThat(item.reason()).isEqualTo("Good fit");
        });
        server.verify();
    }

    @Test
    void missingKeyReturnsServiceUnavailableWithoutHttpCall() {
        GeminiClient client = new GeminiClient(RestClient.builder(), new ObjectMapper(), "", "test-model");
        assertThatThrownBy(() -> client.recommend("prompt"))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessage("AI Game Finder is not configured");
    }

    @Test
    void malformedModelJsonReturnsGracefulExternalServiceError() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(method(HttpMethod.POST)).andRespond(withSuccess("""
                {"candidates":[{"content":{"parts":[{"text":"not-json"}]}}]}
                """, MediaType.APPLICATION_JSON));
        GeminiClient client = new GeminiClient(builder, new ObjectMapper(), "test-key", "test-model");

        assertThatThrownBy(() -> client.recommend("prompt"))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessage("AI Game Finder is temporarily unavailable");
        server.verify();
    }
}
