package com.gamehub.controller;

import com.gamehub.dto.AIRequest;
import com.gamehub.dto.AIResponse;
import com.gamehub.service.AIService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/find")
    public AIResponse find(@Valid @RequestBody AIRequest request) {
        return aiService.find(request.query());
    }
}
