package com.gamehub.repository;

import com.gamehub.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    Optional<Game> findByExternalId(Integer externalId);

    boolean existsByExternalId(Integer externalId);

    List<Game> findByGenreIgnoreCase(String genre);

    List<Game> findByPlatformContainingIgnoreCase(String platform);

    List<Game> findByGenreIgnoreCaseAndPlatformContainingIgnoreCase(String genre, String platform);
}
