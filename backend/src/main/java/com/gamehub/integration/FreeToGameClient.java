package com.gamehub.integration;

import com.gamehub.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

@Component
public class FreeToGameClient {

    private static final Logger log = LoggerFactory.getLogger(FreeToGameClient.class);
    private static final String API_URL = "https://www.freetogame.com/api/games";

    private final RestClient restClient;

    @Autowired
    public FreeToGameClient(RestClient.Builder restClientBuilder) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(30));
        this.restClient = restClientBuilder.requestFactory(requestFactory).build();
    }

    FreeToGameClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<FreeToGameItem> fetchGames() {
        try {
            List<FreeToGameItem> games = restClient.get()
                    .uri(API_URL)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            if (games == null) {
                throw new ExternalServiceException("FreeToGame returned an invalid response");
            }
            return games;
        } catch (ExternalServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("FreeToGame request failed: {}", ex.getClass().getSimpleName());
            throw new ExternalServiceException("FreeToGame is temporarily unavailable");
        }
    }
}
