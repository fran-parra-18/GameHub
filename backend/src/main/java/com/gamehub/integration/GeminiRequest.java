package com.gamehub.integration;

import java.util.List;
import java.util.Map;

public record GeminiRequest(List<Content> contents, GenerationConfig generationConfig) {

    public record Content(List<Part> parts) {
    }

    public record Part(String text) {
    }

    public record GenerationConfig(String responseMimeType) {

        public static GenerationConfig json() {
            return new GenerationConfig("application/json");
        }
    }

    public static GeminiRequest of(String text) {
        return new GeminiRequest(
                List.of(new Content(List.of(new Part(text)))),
                GenerationConfig.json()
        );
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "contents", contents,
                "generationConfig", generationConfig
        );
    }
}
