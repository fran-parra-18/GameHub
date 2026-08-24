package com.gamehub.integration;

import java.util.List;
import java.util.Map;

public record GeminiRequest(List<Content> contents, GenerationConfig generationConfig) {

    public record Content(List<Part> parts) {
    }

    public record Part(String text) {
    }

    public record GenerationConfig(String responseMimeType, Map<String, Object> responseSchema) {

        public static GenerationConfig json() {
            Map<String, Object> itemSchema = Map.of(
                    "type", "OBJECT",
                    "properties", Map.of(
                            "gameId", Map.of("type", "INTEGER"),
                            "reason", Map.of("type", "STRING")
                    ),
                    "required", List.of("gameId", "reason")
            );
            return new GenerationConfig("application/json", Map.of(
                    "type", "OBJECT",
                    "properties", Map.of("recommendations", Map.of(
                            "type", "ARRAY",
                            "items", itemSchema,
                            "maxItems", 5
                    )),
                    "required", List.of("recommendations")
            ));
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
