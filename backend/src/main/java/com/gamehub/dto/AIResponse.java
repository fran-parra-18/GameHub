package com.gamehub.dto;

import java.util.List;

public record AIResponse(String query, List<AIRecommendation> recommendations) {
}
