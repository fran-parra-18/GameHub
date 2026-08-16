package com.gamehub.service;

import com.gamehub.dto.CommentDTO;
import com.gamehub.dto.CommentRequest;
import com.gamehub.entity.Comment;
import com.gamehub.entity.Game;
import com.gamehub.entity.User;
import com.gamehub.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final GameService gameService;

    public CommentService(CommentRepository commentRepository, GameService gameService) {
        this.commentRepository = commentRepository;
        this.gameService = gameService;
    }

    public List<CommentDTO> getComments(Long gameId) {
        return commentRepository.findByGameIdOrderByCreatedAtDesc(gameId)
                .stream().map(CommentDTO::from).toList();
    }

    public CommentDTO addComment(Long gameId, CommentRequest request, User user) {
        Game game = gameService.getGame(gameId);
        Comment comment = new Comment();
        comment.setGame(game);
        comment.setUser(user);
        comment.setContent(request.content().trim());
        return CommentDTO.from(commentRepository.save(comment));
    }

    public long countByGame(Long gameId) {
        return commentRepository.countByGameId(gameId);
    }
}
