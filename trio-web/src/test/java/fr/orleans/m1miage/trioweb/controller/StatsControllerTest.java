package fr.orleans.m1miage.trioweb.controller;

import fr.orleans.m1miage.trioweb.dtos.api.PlayerStatsDto;
import fr.orleans.m1miage.trioweb.service.StatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class StatsControllerTest {

    @Mock
    private StatsService statsService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new StatsController(statsService)).build();
    }

    @Test
    void stats_shouldRenderPersonalStatsFromService() throws Exception {
        PlayerStatsDto stats = new PlayerStatsDto();
        stats.setEmail("player@test.com");
        stats.setPseudo("player");
        stats.setPartiesJouees(2);
        stats.setVictoires(1);

        when(statsService.getMyStats()).thenReturn(stats);

        mockMvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andExpect(view().name("stats/index"))
                .andExpect(model().attribute("stats", stats));

        verify(statsService).getMyStats();
    }

    @Test
    void stats_shouldRenderStatsView_whenPlayerHasNoVariantStats() throws Exception {
        PlayerStatsDto stats = new PlayerStatsDto();
        stats.setEmail("player@test.com");
        stats.setPseudo("player");
        stats.setPartiesJouees(0);
        stats.setVictoires(0);
        stats.setVariantes(List.of());

        when(statsService.getMyStats()).thenReturn(stats);

        mockMvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andExpect(view().name("stats/index"))
                .andExpect(model().attribute("stats", stats));

        verify(statsService).getMyStats();
    }
}
