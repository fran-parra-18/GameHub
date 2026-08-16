package com.gamehub.repository;

import com.gamehub.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<Comment> findByGameIdOrderByCreatedAtDesc(Long gameId);

    long countByGameId(Long gameId);
}
