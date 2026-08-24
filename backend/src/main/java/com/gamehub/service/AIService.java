package com.gamehub.service;

import com.gamehub.dto.AIRecommendation;
import com.gamehub.dto.AIResponse;
import com.gamehub.dto.GameDTO;
import com.gamehub.entity.Game;
import com.gamehub.integration.GeminiClient;
import com.gamehub.repository.GameRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private static final int MAX_RECOMMENDATIONS = 5;
    private static final int DESCRIPTION_LIMIT = 240;

    private final GeminiClient geminiClient;
    private final GameRepository gameRepository;
    private final ObjectMapper objectMapper;

    public AIService(GeminiClient geminiClient, GameRepository gameRepository, ObjectMapper objectMapper) {
        this.geminiClient = geminiClient;
        this.gameRepository = gameRepository;
        this.objectMapper = objectMapper;
    }

    public AIResponse find(String query) {
        String normalizedQuery = query.trim();
        List<Game> catalog = loadCatalog();
        if (catalog.isEmpty()) {
            return new AIResponse(List.of());
        }
        AiRawResponse response = geminiClient.recommend(buildPrompt(normalizedQuery, catalog));
        return new AIResponse(resolve(response, catalog));
    }

    private List<Game> loadCatalog() {
        return gameRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    private String buildPrompt(String query, List<Game> catalog) {
        List<CatalogItem> compactCatalog = catalog.stream().map(game -> new CatalogItem(
                game.getId(), game.getTitle(), game.getGenre(), game.getPlatform(),
                truncate(game.getDescription()))).toList();
        try {
            return "Select at most 5 games that best match the user's request. "
                    + "Only use gameId values present in the catalog. Give a concise reason for each.\n"
                    + "User request: " + objectMapper.writeValueAsString(query) + "\n"
                    + "Catalog: " + objectMapper.writeValueAsString(compactCatalog);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize local game catalog", e);
        }
    }

    private List<AIRecommendation> resolve(AiRawResponse response, List<Game> catalog) {
        List<AIRecommendation> result = new ArrayList<>();
        Map<Long, Game> gamesById = new HashMap<>();
        catalog.forEach(game -> gamesById.put(game.getId(), game));
        Set<Long> acceptedIds = new HashSet<>();
        for (AiRawResponse.RawRecommendation rec : response.recommendations()) {
            if (result.size() >= MAX_RECOMMENDATIONS || rec == null || rec.gameId() == null
                    || !StringUtils.hasText(rec.reason()) || !acceptedIds.add(rec.gameId())) {
                continue;
            }
            Game game = gamesById.get(rec.gameId());
            if (game != null) {
                result.add(new AIRecommendation(GameDTO.from(game), rec.reason().trim()));
            }
        }
        return result;
    }

    private String truncate(String value) {
        if (value == null || value.length() <= DESCRIPTION_LIMIT) return value;
        return value.substring(0, DESCRIPTION_LIMIT);
    }

    private record CatalogItem(Long id, String title, String genre, String platform, String description) {}
}
