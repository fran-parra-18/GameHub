package com.gamehub.repository;

import com.gamehub.entity.Comment;
import com.gamehub.entity.Favorite;
import com.gamehub.entity.Game;
import com.gamehub.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class PersistenceModelTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Test
    void persistsThePhaseOneModelAndRelationships() {
        User user = new User();
        user.setUsername("player-one");
        user.setEmail("player@example.com");
        user.setPasswordHash("bcrypt-hash");
        user = userRepository.saveAndFlush(user);

        Game game = new Game();
        game.setExternalId(1001);
        game.setTitle("Test Game");
        game = gameRepository.saveAndFlush(game);

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setGame(game);
        comment.setContent("Great game");
        commentRepository.saveAndFlush(comment);

        favoriteRepository.saveAndFlush(new Favorite(user, game));

        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(comment.getCreatedAt()).isNotNull();
        assertThat(commentRepository.findByGameIdOrderByCreatedAtDescIdDesc(game.getId()))
                .extracting(Comment::getContent)
                .containsExactly("Great game");
        assertThat(favoriteRepository.existsByUserIdAndGameId(user.getId(), game.getId())).isTrue();
    }

    @Test
    void rejectsDuplicateFavoritesForTheSameUserAndGame() {
        User user = new User();
        user.setUsername("player-two");
        user.setEmail("player2@example.com");
        user.setPasswordHash("bcrypt-hash");
        user = userRepository.saveAndFlush(user);

        Game game = new Game();
        game.setExternalId(1002);
        game.setTitle("Another Test Game");
        game = gameRepository.saveAndFlush(game);

        favoriteRepository.saveAndFlush(new Favorite(user, game));

        User savedUser = user;
        Game savedGame = game;
        assertThatThrownBy(() -> favoriteRepository.saveAndFlush(new Favorite(savedUser, savedGame)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
