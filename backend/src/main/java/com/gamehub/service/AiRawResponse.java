package com.gamehub.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Intermediate model used to parse Gemini's JSON output before resolving against the DB. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AiRawResponse(List<RawRecommendation> recommendations) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawRecommendation(Long gameId, String reason) {
    }
}
