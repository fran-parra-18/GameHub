package com.gamehub.controller;

import com.gamehub.dto.CommentDTO;
import com.gamehub.dto.CommentRequest;
import com.gamehub.security.CurrentUser;
import com.gamehub.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/games/{gameId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<CommentDTO> list(@PathVariable Long gameId) {
        return commentService.getComments(gameId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDTO create(@PathVariable Long gameId, @Valid @RequestBody CommentRequest request) {
        return commentService.addComment(gameId, request, CurrentUser.require());
    }
}
