package com.gamehub.service;

import com.gamehub.integration.FreeToGameClient;
import com.gamehub.integration.FreeToGameItem;
import com.gamehub.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import(FreeToGameService.class)
class FreeToGameServiceTests {

    @Autowired
    private FreeToGameService service;

    @Autowired
    private GameRepository gameRepository;

    @MockitoBean
    private FreeToGameClient client;

    @Test
    void synchronizesValidRecordsAndSkipsMalformedOnes() {
        when(client.fetchGames()).thenReturn(List.of(
                item(10, "  Alpha  ", "Action", "PC (Windows)"),
                item(11, "Beta", null, null),
                item(null, "Missing id", "Action", "PC"),
                item(12, "  ", "Action", "PC"),
                item(10, "Duplicate payload id", "Action", "PC")
        ));

        var result = service.syncGames();

        assertThat(result.received()).isEqualTo(5);
        assertThat(result.created()).isEqualTo(2);
        assertThat(result.updated()).isZero();
        assertThat(result.skipped()).isEqualTo(3);
        assertThat(gameRepository.findAll()).hasSize(2);
        assertThat(gameRepository.findByExternalId(10).orElseThrow().getTitle()).isEqualTo("Alpha");
    }

    @Test
    void runningSyncTwiceUpdatesInsteadOfCreatingDuplicates() {
        when(client.fetchGames())
                .thenReturn(List.of(item(20, "Original", "Shooter", "PC")))
                .thenReturn(List.of(item(20, "Updated", "Shooter", "PC")));

        var first = service.syncGames();
        var second = service.syncGames();

        assertThat(first.created()).isEqualTo(1);
        assertThat(second.updated()).isEqualTo(1);
        assertThat(second.created()).isZero();
        assertThat(gameRepository.count()).isEqualTo(1);
        assertThat(gameRepository.findByExternalId(20).orElseThrow().getTitle()).isEqualTo("Updated");
    }

    private FreeToGameItem item(Integer id, String title, String genre, String platform) {
        return new FreeToGameItem(id, title, null, "Description", null,
                genre, platform, null, null);
    }
}
