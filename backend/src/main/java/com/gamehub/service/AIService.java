package com.gamehub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamehub.dto.AIRecommendation;
import com.gamehub.dto.AIResponse;
import com.gamehub.entity.Game;
import com.gamehub.exception.ExternalServiceException;
import com.gamehub.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * AI Game Finder.
 *
 * The AI only ever recommends games that actually exist in our database:
 * the available catalog is sent to Gemini, which returns candidate game IDs,
 * and every ID is re-validated against the database before being returned.
 * This prevents Gemini from inventing games that are not in GameHub.
 */
@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);
    private static final int MAX_CATALOG_SENT = 300;
    private static final int MAX_RECOMMENDATIONS = 5;

    private final GeminiService geminiService;
    private final GameRepository gameRepository;
    private final ObjectMapper objectMapper;

    public AIService(GeminiService geminiService, GameRepository gameRepository, ObjectMapper objectMapper) {
        this.geminiService = geminiService;
        this.gameRepository = gameRepository;
        this.objectMapper = objectMapper;
    }

    public AIResponse find(String query) {
        List<Game> catalog = loadCatalog();
        String prompt = buildPrompt(query, catalog);

        String raw = geminiService.generateText(prompt);
        List<AIRecommendation> recommendations = resolve(raw, catalog);

        return new AIResponse(query, recommendations);
    }

    private List<Game> loadCatalog() {
        List<Game> all = gameRepository.findAll();
        return all.size() > MAX_CATALOG_SENT ? all.subList(0, MAX_CATALOG_SENT) : all;
    }

    private String buildPrompt(String query, List<Game> catalog) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a game recommendation assistant for the GameHub catalog.\n");
        sb.append("The user is looking for a game. Their request: \"").append(query).append("\"\n\n");
        sb.append("Here is the list of available games in the catalog (id, title, genre, platform):\n");
        for (Game g : catalog) {
            sb.append("- id=").append(g.getId())
                    .append(", title=").append(nullSafe(g.getTitle()))
                    .append(", genre=").append(nullSafe(g.getGenre()))
                    .append(", platform=").append(nullSafe(g.getPlatform()))
                    .append("\n");
        }
        sb.append("\nRecommend up to ").append(MAX_RECOMMENDATIONS)
                .append(" games from this catalog that best match the user's request.\n");
        sb.append("You MUST ONLY recommend games that appear in the list above.\n");
        sb.append("Do NOT invent, guess, or fabricate game ids or titles that are not in the list.\n");
        sb.append("Return JSON only, with no markdown and no extra text, in this exact format:\n");
        sb.append("{\"recommendations\":[{\"gameId\": <integer>, \"reason\": \"<short reason>\"}]}");
        return sb.toString();
    }

    private List<AIRecommendation> resolve(String rawJson, List<Game> catalog) {
        List<AIRecommendation> result = new ArrayList<>();
        AiRawResponse parsed;
        try {
            parsed = objectMapper.readValue(rawJson, AiRawResponse.class);
        } catch (Exception e) {
            log.warn("Could not parse Gemini JSON output: {}", rawJson);
            throw new ExternalServiceException("AI returned an unreadable response. Please try again.");
        }

        if (parsed == null || parsed.recommendations() == null) {
            return result;
        }

        for (AiRawResponse.RawRecommendation rec : parsed.recommendations()) {
            if (rec.gameId() == null || result.size() >= MAX_RECOMMENDATIONS) {
                continue;
            }
            // Re-validate every ID against the real catalog before accepting it.
            boolean exists = catalog.stream().anyMatch(g -> g.getId().equals(rec.gameId()));
            if (exists) {
                result.add(new AIRecommendation(rec.gameId(), rec.reason()));
            } else {
                log.warn("Gemini suggested non-existent gameId {}; ignoring", rec.gameId());
            }
        }
        return result;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
