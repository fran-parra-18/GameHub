package com.gamehub.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamehub.dto.AIRecommendation;
import com.gamehub.dto.AIResponse;
import com.gamehub.dto.GameDTO;
import com.gamehub.entity.Game;
import com.gamehub.exception.ExternalServiceException;
import com.gamehub.exception.ServiceUnavailableException;
import com.gamehub.integration.GeminiClient;
import com.gamehub.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * AI Game Finder.
 *
 * Gemini only ever recommends games that actually exist in our database: a relevant
 * subset of the catalog is sent to Gemini, which returns candidate game IDs, and every ID
 * is re-validated against the full catalog before being returned.
 *
 * To keep prompts small (the catalog can have hundreds of games) only the most relevant
 * candidates are sent. If Gemini is not configured or fails, a simple local keyword
 * search is used so the search box keeps working.
 */
@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    private static final int MAX_RECOMMENDATIONS = 5;
    private static final int DESCRIPTION_LIMIT = 240;
    private static final int MAX_CANDIDATES = 60;
    private static final int MAX_RELEVANT_CANDIDATES = 40;

    private static final Set<String> STOPWORDS = Set.of(
            "que", "con", "para", "por", "una", "uno", "unos", "unas", "los", "las", "del", "algo",
            "como", "juego", "juegos", "jugar", "quiero", "tengo", "ganas", "busco", "the", "and",
            "game", "games", "play", "want", "something", "like", "some", "for", "with");

    /** Palabras comunes en español -> terminos en ingles (los datos de FreeToGame estan en ingles). */
    private static final Map<String, List<String>> SYNONYMS = Map.ofEntries(
            Map.entry("disparos", List.of("shooter")),
            Map.entry("disparar", List.of("shooter")),
            Map.entry("tiros", List.of("shooter")),
            Map.entry("carreras", List.of("racing")),
            Map.entry("autos", List.of("racing", "car")),
            Map.entry("coches", List.of("racing", "car")),
            Map.entry("conducir", List.of("racing", "driving")),
            Map.entry("estrategia", List.of("strategy")),
            Map.entry("deportes", List.of("sports")),
            Map.entry("deporte", List.of("sports")),
            Map.entry("futbol", List.of("sports", "soccer", "football")),
            Map.entry("baloncesto", List.of("sports", "basketball")),
            Map.entry("cartas", List.of("card")),
            Map.entry("pelea", List.of("fighting")),
            Map.entry("peleas", List.of("fighting")),
            Map.entry("lucha", List.of("fighting")),
            Map.entry("combate", List.of("fighting", "combat")),
            Map.entry("rol", List.of("rpg")),
            Map.entry("fantasia", List.of("fantasy")),
            Map.entry("multijugador", List.of("mmo", "multiplayer")),
            Map.entry("supervivencia", List.of("survival")),
            Map.entry("terror", List.of("horror")),
            Map.entry("miedo", List.of("horror")),
            Map.entry("zombies", List.of("zombie")),
            Map.entry("magia", List.of("magic", "fantasy")),
            Map.entry("mundo", List.of("world")),
            Map.entry("abierto", List.of("open")),
            Map.entry("cooperativo", List.of("co-op", "pve")),
            Map.entry("navegador", List.of("browser")),
            Map.entry("batalla", List.of("battle")),
            Map.entry("espacio", List.of("space")),
            Map.entry("anime", List.of("anime")),
            Map.entry("aventura", List.of("adventure")),
            Map.entry("aventuras", List.of("adventure")),
            Map.entry("accion", List.of("action")),
            Map.entry("construir", List.of("sandbox", "building")),
            Map.entry("tranquilo", List.of("casual")),
            Map.entry("relajante", List.of("casual")));

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
        Set<String> terms = extractTerms(normalizedQuery);
        List<Game> candidates = selectCandidates(catalog, terms);
        try {
            AiRawResponse response = geminiClient.recommend(buildPrompt(normalizedQuery, candidates));
            return new AIResponse(resolve(response, catalog));
        } catch (ServiceUnavailableException | ExternalServiceException e) {
            List<AIRecommendation> fallback = localSearch(catalog, terms);
            if (fallback.isEmpty()) {
                throw e;
            }
            log.info("AI Game Finder using local keyword search ({})", e.getMessage());
            return new AIResponse(fallback);
        }
    }

    private List<Game> loadCatalog() {
        return gameRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    // ---------------------------------------------------------------- prompt

    private String buildPrompt(String query, List<Game> catalog) {
        List<CatalogItem> compactCatalog = catalog.stream().map(game -> new CatalogItem(
                game.getId(), game.getTitle(), game.getGenre(), game.getPlatform(),
                truncate(game.getDescription()))).toList();
        try {
            return "Select at most 5 games that best match the user's request. "
                    + "The request may be written in any language (usually Spanish); write each reason in Spanish. "
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

    // ------------------------------------------------- candidate selection

    /** Keeps the prompt small: most relevant games first, padded with one game per genre in turn. */
    private List<Game> selectCandidates(List<Game> catalog, Set<String> terms) {
        if (catalog.size() <= MAX_CANDIDATES) {
            return catalog;
        }
        Map<Long, Integer> scores = new HashMap<>();
        for (Game game : catalog) {
            scores.put(game.getId(), score(game, terms));
        }
        List<Game> relevant = catalog.stream()
                .filter(game -> scores.get(game.getId()) > 0)
                .sorted(Comparator.comparingInt((Game game) -> scores.get(game.getId())).reversed()
                        .thenComparing(Game::getId))
                .limit(MAX_RELEVANT_CANDIDATES)
                .toList();

        Set<Game> selected = new LinkedHashSet<>(relevant);
        Map<String, Deque<Game>> byGenre = new LinkedHashMap<>();
        for (Game game : catalog) {
            if (!selected.contains(game)) {
                String genre = game.getGenre() == null ? "" : game.getGenre();
                byGenre.computeIfAbsent(genre, key -> new ArrayDeque<>()).add(game);
            }
        }
        while (selected.size() < MAX_CANDIDATES && !byGenre.isEmpty()) {
            List<String> exhausted = new ArrayList<>();
            for (Map.Entry<String, Deque<Game>> entry : byGenre.entrySet()) {
                if (selected.size() >= MAX_CANDIDATES) {
                    break;
                }
                Game next = entry.getValue().poll();
                if (next != null) {
                    selected.add(next);
                }
                if (entry.getValue().isEmpty()) {
                    exhausted.add(entry.getKey());
                }
            }
            exhausted.forEach(byGenre::remove);
        }
        return new ArrayList<>(selected);
    }

    // ------------------------------------------------------- local search

    private List<AIRecommendation> localSearch(List<Game> catalog, Set<String> terms) {
        if (terms.isEmpty()) {
            return List.of();
        }
        Map<Long, Integer> scores = new HashMap<>();
        for (Game game : catalog) {
            scores.put(game.getId(), score(game, terms));
        }
        return catalog.stream()
                .filter(game -> scores.get(game.getId()) > 0)
                .sorted(Comparator.comparingInt((Game game) -> scores.get(game.getId())).reversed()
                        .thenComparing(Game::getId))
                .limit(MAX_RECOMMENDATIONS)
                .map(game -> new AIRecommendation(GameDTO.from(game), localReason(game, terms)))
                .toList();
    }

    private String localReason(Game game, Set<String> terms) {
        String haystack = normalize(game.getTitle() + " " + game.getGenre() + " " + game.getPlatform()
                + " " + game.getDescription());
        List<String> matched = terms.stream().filter(haystack::contains).limit(3).toList();
        return "Coincide con tu búsqueda (" + String.join(", ", matched) + ").";
    }

    private int score(Game game, Set<String> terms) {
        String title = normalize(game.getTitle());
        String genre = normalize(game.getGenre());
        String platform = normalize(game.getPlatform());
        String description = normalize(game.getDescription());
        int total = 0;
        for (String term : terms) {
            if (title.contains(term)) total += 5;
            if (genre.contains(term)) total += 4;
            if (platform.contains(term)) total += 2;
            if (description.contains(term)) total += 1;
        }
        return total;
    }

    private Set<String> extractTerms(String query) {
        Set<String> terms = new LinkedHashSet<>();
        for (String token : normalize(query).split("[^a-z0-9]+")) {
            if (token.length() < 3 || STOPWORDS.contains(token)) {
                continue;
            }
            terms.add(token);
            List<String> translated = SYNONYMS.get(token);
            if (translated != null) {
                terms.addAll(translated);
            }
        }
        return terms;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }

    private record CatalogItem(Long id, String title, String genre, String platform, String description) {}
}
