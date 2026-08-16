package com.gamehub.dto;

import com.gamehub.entity.Comment;

import java.time.Instant;

public record CommentDTO(
        Long id,
        Long userId,
        String username,
        Long gameId,
        String content,
        Instant createdAt
) {

    public static CommentDTO from(Comment comment) {
        return new CommentDTO(
                comment.getId(),
                comment.getUser().getId(),
                comment.getUser().getUsername(),
                comment.getGame().getId(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
