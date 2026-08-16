package com.gamehub.service;

import com.gamehub.exception.ExternalServiceException;
import com.gamehub.integration.GeminiRequest;
import com.gamehub.integration.GeminiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/**
 * Thin wrapper around the Google Gemini REST API. Returns the raw JSON text
 * produced by the model. Never exposes the API key to the frontend.
 */
@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiService(RestClient.Builder restClientBuilder,
                         @Value("${gemini.api-key}") String apiKey,
                         @Value("${gemini.model}") String model) {
        this.restClient = restClientBuilder.build();
        this.apiKey = apiKey;
        this.model = model;
    }

    /** Sends a prompt and returns the model's text response (expected to be JSON). */
    public String generateText(String prompt) {
        if (!StringUtils.hasText(apiKey)) {
            log.warn("GEMINI_API_KEY is not configured");
            throw new ExternalServiceException("AI Game Finder is not configured yet. Set GEMINI_API_KEY to enable it.");
        }

        String url = String.format(BASE_URL, model) + "?key=" + apiKey;
        try {
            GeminiResponse response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(GeminiRequest.of(prompt).toMap())
                    .retrieve()
                    .body(GeminiResponse.class);

            String text = response == null ? null : response.firstText();
            if (!StringUtils.hasText(text)) {
                throw new ExternalServiceException("Gemini returned an empty response");
            }
            return text;
        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Gemini API call failed", e);
            throw new ExternalServiceException("AI Game Finder is temporarily unavailable. Please try again later.");
        }
    }
}
