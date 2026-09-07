package fr.orleans.m1miage.trioweb.controller;

import fr.orleans.m1miage.trioweb.dtos.api.CreatePartieRequestDto;
import fr.orleans.m1miage.trioweb.dtos.api.PartieDto;
import fr.orleans.m1miage.trioweb.service.PartieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class GamesControllerTest {

    @Mock
    private PartieService partieService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new GamesController(partieService))
                .setValidator(validator)
                .build();
    }

    @Test
    void lobby_shouldRenderGamesFromService() throws Exception {
        PartieDto partie = partie(7L, "A", "EN_ATTENTE", 4, 1, "alice");
        when(partieService.list()).thenReturn(List.of(partie));

        mockMvc.perform(get("/games"))
                .andExpect(status().isOk())
                .andExpect(view().name("games/lobby"))
                .andExpect(model().attribute("games", List.of(partie)));

        verify(partieService).list();
    }

    @Test
    void finished_shouldRenderFinishedGamesFromService() throws Exception {
        PartieDto partie = partie(8L, "B", "TERMINEE", 4, 4, "alice");
        when(partieService.listFinished()).thenReturn(List.of(partie));

        mockMvc.perform(get("/games/finished"))
                .andExpect(status().isOk())
                .andExpect(view().name("games/finished"))
                .andExpect(model().attribute("games", List.of(partie)));

        verify(partieService).listFinished();
    }

    @Test
    void lobbyStateVersion_shouldReturnVersionFromCurrentGames() throws Exception {
        PartieDto partie = partie(7L, "A", "EN_ATTENTE", 4, 2, "alice");
        when(partieService.list()).thenReturn(List.of(partie));

        mockMvc.perform(get("/games/state-version"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.blankOrNullString())));

        verify(partieService).list();
    }

    @Test
    void createForm_shouldRenderCreatePageWithFormAndVariants() throws Exception {
        mockMvc.perform(get("/games/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("games/create"))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attributeExists("variants"));
    }

    @Test
    void createSubmit_shouldRedirectToLobbyWithCreatedId_whenCreationSucceeds() throws Exception {
        when(partieService.create(any(CreatePartieRequestDto.class)))
                .thenReturn(partie(42L, "A", "EN_ATTENTE", 4, 1, "alice"));

        mockMvc.perform(post("/games/create")
                        .param("variant", "A")
                        .param("maxPlayers", "4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/games?created=42"));

        ArgumentCaptor<CreatePartieRequestDto> captor = ArgumentCaptor.forClass(CreatePartieRequestDto.class);
        verify(partieService).create(captor.capture());
        assertThat(captor.getValue().getVariante()).isEqualTo("A");
        assertThat(captor.getValue().getNombreJoueurs()).isEqualTo(4);
    }

    @Test
    void createSubmit_shouldRenderCreatePageWithFormAndVariants_whenFormIsInvalid() throws Exception {
        mockMvc.perform(post("/games/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("games/create"))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attributeExists("variants"));

        verifyNoInteractions(partieService);
    }

    @Test
    void join_shouldRedirectToGame_whenJoinSucceeds() throws Exception {
        when(partieService.join(42L, null)).thenReturn(partie(42L, "A", "EN_ATTENTE", 4, 2, "alice"));

        mockMvc.perform(post("/games/42/join"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/games/42"));

        verify(partieService).join(42L, null);
    }

    private PartieDto partie(
            Long id,
            String variante,
            String statut,
            Integer nombreJoueursMax,
            Integer nombreJoueursActuels,
            String createur
    ) {
        PartieDto partie = new PartieDto();
        partie.setId(id);
        partie.setVariante(variante);
        partie.setStatut(statut);
        partie.setNombreJoueursMax(nombreJoueursMax);
        partie.setNombreJoueursActuels(nombreJoueursActuels);
        partie.setCreateur(createur);
        return partie;
    }
}
