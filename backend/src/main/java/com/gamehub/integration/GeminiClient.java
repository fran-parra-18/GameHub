package com.gamehub.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamehub.exception.ExternalServiceException;
import com.gamehub.exception.ServiceUnavailableException;
import com.gamehub.service.AiRawResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public GeminiClient(RestClient.Builder builder, ObjectMapper objectMapper,
                         @Value("${gemini.api-key}") String apiKey,
                         @Value("${gemini.model}") String model) {
        this.restClient = builder.baseUrl("https://generativelanguage.googleapis.com").build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    public AiRawResponse recommend(String prompt) {
        if (!StringUtils.hasText(apiKey)) {
            throw new ServiceUnavailableException("AI Game Finder is not configured");
        }

        try {
            GeminiResponse response = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(GeminiRequest.of(prompt))
                    .retrieve()
                    .body(GeminiResponse.class);
            String json = response == null ? null : response.firstText();
            if (!StringUtils.hasText(json)) {
                throw unusableResponse();
            }
            AiRawResponse parsed = objectMapper.readValue(json, AiRawResponse.class);
            if (parsed == null || parsed.recommendations() == null) {
                throw unusableResponse();
            }
            return parsed;
        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Gemini request failed: {}", e.getClass().getSimpleName());
            throw new ExternalServiceException("AI Game Finder is temporarily unavailable");
        }
    }

    private ExternalServiceException unusableResponse() {
        return new ExternalServiceException("AI returned an unusable response");
    }
}
