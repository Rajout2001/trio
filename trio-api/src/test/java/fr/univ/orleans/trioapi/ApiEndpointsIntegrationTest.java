package fr.univ.orleans.trioapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.univ.orleans.trioapi.model.AppUser;
import fr.univ.orleans.trioapi.model.CartePartie;
import fr.univ.orleans.trioapi.model.Partie;
import fr.univ.orleans.trioapi.model.VariantePartie;
import fr.univ.orleans.trioapi.repository.AppUserRepository;
import fr.univ.orleans.trioapi.repository.CartePartieRepository;
import fr.univ.orleans.trioapi.repository.PartieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:trio-test-db;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    }
)
class ApiEndpointsIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PartieRepository partieRepository;

    @Autowired
    private CartePartieRepository cartePartieRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = webAppContextSetup(context).apply(springSecurity()).build();
        cartePartieRepository.deleteAll();
        partieRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void homeEndpoint_shouldReturnServiceStatus() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.service").value("trio-api"))
            .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void registerEndpoint_shouldCreateUserAndReturnToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "email", "register@test.com",
                    "pseudo", "registerUser",
                    "password", "Password123"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isString())
            .andExpect(jsonPath("$.expiresIn").isNumber())
            .andExpect(jsonPath("$.refreshToken").isString())
            .andExpect(jsonPath("$.refreshExpiresIn").isNumber())
            .andExpect(jsonPath("$.email").value("register@test.com"))
            .andExpect(jsonPath("$.pseudo").value("registerUser"));
    }

    @Test
    void registerEndpoint_shouldRejectPseudoAlreadyUsedIgnoringCase() throws Exception {
        register("pseudo1@test.com", "SamePseudo", "Password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "email", "pseudo2@test.com",
                    "pseudo", "samepseudo",
                    "password", "Password123"
                ))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Pseudo already in use"));
    }

    @Test
    void loginEndpoint_shouldAuthenticateAndReturnToken() throws Exception {
        register("login@test.com", "loginUser", "Password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "email", "login@test.com",
                    "password", "Password123"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isString())
            .andExpect(jsonPath("$.expiresIn").isNumber())
            .andExpect(jsonPath("$.refreshToken").isString())
            .andExpect(jsonPath("$.refreshExpiresIn").isNumber())
            .andExpect(jsonPath("$.email").value("login@test.com"))
            .andExpect(jsonPath("$.pseudo").value("loginUser"));
    }

    @Test
    void refreshEndpoint_shouldIssueNewTokensForValidRefreshToken() throws Exception {
        JsonNode auth = registerAuth("refresh-valid@test.com", "refreshValid", "Password123");
        String refreshToken = auth.get("refreshToken").asText();

        MvcResult refreshed = mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("refreshToken", refreshToken))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isString())
            .andExpect(jsonPath("$.refreshToken").isString())
            .andExpect(jsonPath("$.email").value("refresh-valid@test.com"))
            .andExpect(jsonPath("$.pseudo").value("refreshValid"))
            .andReturn();

        JsonNode refreshedAuth = objectMapper.readTree(refreshed.getResponse().getContentAsString());
        mockMvc.perform(get("/api/parties")
                .header("Authorization", "Bearer " + refreshedAuth.get("accessToken").asText()))
            .andExpect(status().isOk());
    }

    @Test
    void refreshEndpoint_shouldRejectInvalidRefreshToken() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("refreshToken", "invalid-refresh-token"))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshEndpoint_shouldRejectAccessTokenUsedAsRefreshToken() throws Exception {
        JsonNode auth = registerAuth("refresh-access@test.com", "refreshAccess", "Password123");

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("refreshToken", auth.get("accessToken").asText()))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_shouldRejectRefreshTokenUsedAsBearer() throws Exception {
        JsonNode auth = registerAuth("bearer-refresh@test.com", "bearerRefresh", "Password123");

        mockMvc.perform(get("/api/parties")
                .header("Authorization", "Bearer " + auth.get("refreshToken").asText()))
            .andExpect(status().isForbidden());
    }

    @Test
    void logoutEndpoint_shouldRevokeAccessTokenAndRefreshToken() throws Exception {
        JsonNode auth = registerAuth("logout-revoke@test.com", "logoutRevoke", "Password123");
        String accessToken = auth.get("accessToken").asText();
        String refreshToken = auth.get("refreshToken").asText();

        mockMvc.perform(get("/api/parties")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("refreshToken", refreshToken))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Logged out"));

        mockMvc.perform(get("/api/parties")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("refreshToken", refreshToken))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void updateCredentialsEndpoint_shouldUpdateEmailAndPassword() throws Exception {
        String token = register("old@test.com", "oldUser", "Password123");

        mockMvc.perform(patch("/api/auth/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "currentPassword", "Password123",
                    "newEmail", "new@test.com",
                    "newPassword", "Password456"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Credentials updated"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "email", "new@test.com",
                    "password", "Password456"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("new@test.com"));
    }

    @Test
    void getPartiesEndpoint_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/parties"))
            .andExpect(status().isForbidden());
    }

    @Test
    void getMyStatsEndpoint_shouldReturnZerosWhenPlayerHasNoFinishedGame() throws Exception {
        String playerToken = register("stats0@test.com", "stats0", "Password123");
        register("stats0other@test.com", "stats0other", "Password123");

        AppUser player = appUserRepository.findByEmail("stats0@test.com").orElseThrow();
        AppUser other = appUserRepository.findByEmail("stats0other@test.com").orElseThrow();

        Partie pendingVariantA = new Partie(VariantePartie.A, 3, player);
        pendingVariantA.ajouterJoueur(other);
        Partie pendingVariantB = new Partie(VariantePartie.B, 3, other);
        pendingVariantB.ajouterJoueur(player);
        partieRepository.saveAll(List.of(pendingVariantA, pendingVariantB));

        assertStats(playerToken, "stats0@test.com", "stats0", 0, 0, 0, 0, 0, 0);
    }

    @Test
    void getMyStatsEndpoint_shouldReturnFinishedGamesAndWinsByVariant() throws Exception {
        String playerToken = register("stats1@test.com", "stats1", "Password123");
        register("stats2@test.com", "stats2", "Password123");
        register("stats3@test.com", "stats3", "Password123");

        AppUser player = appUserRepository.findByEmail("stats1@test.com").orElseThrow();
        AppUser otherA = appUserRepository.findByEmail("stats2@test.com").orElseThrow();
        AppUser otherB = appUserRepository.findByEmail("stats3@test.com").orElseThrow();

        Partie wonVariantA = new Partie(VariantePartie.A, 3, player);
        wonVariantA.ajouterJoueur(otherA);
        wonVariantA.terminer(player, "TROIS_TRIOS");

        Partie lostVariantA = new Partie(VariantePartie.A, 3, otherA);
        lostVariantA.ajouterJoueur(player);
        lostVariantA.terminer(otherA, "TROIS_TRIOS");

        Partie wonVariantB = new Partie(VariantePartie.B, 3, player);
        wonVariantB.ajouterJoueur(otherB);
        wonVariantB.terminer(player, "DEUX_TRIOS_LIES");

        Partie lostVariantB = new Partie(VariantePartie.B, 3, otherB);
        lostVariantB.ajouterJoueur(player);
        lostVariantB.terminer(otherB, "DEUX_TRIOS_LIES");

        Partie pendingVariantA = new Partie(VariantePartie.A, 3, player);
        Partie pendingVariantB = new Partie(VariantePartie.B, 3, player);

        partieRepository.saveAll(List.of(
            wonVariantA,
            lostVariantA,
            wonVariantB,
            lostVariantB,
            pendingVariantA,
            pendingVariantB
        ));

        assertStats(playerToken, "stats1@test.com", "stats1", 4, 2, 2, 1, 2, 1);
    }

    @Test
    void createPartieEndpoint_shouldCreatePartie() throws Exception {
        String token = register("create@test.com", "creator", "Password123");

        mockMvc.perform(post("/api/parties")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "variante", "A",
                    "nombreJoueurs", 3
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.variante").value("A"))
            .andExpect(jsonPath("$.statut").value("EN_ATTENTE"))
            .andExpect(jsonPath("$.nombreJoueursActuels").value(1))
            .andExpect(jsonPath("$.nombreJoueursMax").value(3));
    }

    @Test
    void getPartieEndpoint_shouldReturnPartieById() throws Exception {
        String token = register("get@test.com", "getUser", "Password123");
        long partieId = createPartie(token, "B", 4);

        mockMvc.perform(get("/api/parties/{partieId}", partieId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(partieId))
            .andExpect(jsonPath("$.variante").value("B"));
    }

    @Test
    void joinPartieEndpoint_shouldAllowAnotherPlayerToJoin() throws Exception {
        String creatorToken = register("creator@test.com", "creatorUser", "Password123");
        String joinerToken = register("joiner@test.com", "joinerUser", "Password123");
        long partieId = createPartie(creatorToken, "A", 3);

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + joinerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombreJoueursActuels").value(2));
    }

    @Test
    void startPartieEndpoint_shouldStartGameWhenFullAndCreatorStarts() throws Exception {
        String creatorToken = register("c1@test.com", "usr1", "Password123");
        String p2Token = register("c2@test.com", "usr2", "Password123");
        String p3Token = register("c3@test.com", "usr3", "Password123");
        long partieId = createPartie(creatorToken, "A", 3);

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p2Token))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p3Token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombreJoueursActuels").value(3));

        mockMvc.perform(post("/api/parties/{partieId}/start", partieId)
                .header("Authorization", "Bearer " + creatorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statut").value("EN_COURS"));
    }

    @Test
    void distributionEndpoint_shouldReturnCorrectCardCountsAfterStart() throws Exception {
        String creatorToken = register("dist1@test.com", "dst1", "Password123");
        String p2Token = register("dist2@test.com", "dst2", "Password123");
        String p3Token = register("dist3@test.com", "dst3", "Password123");
        long partieId = createPartie(creatorToken, "A", 3);

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p2Token))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p3Token))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/parties/{partieId}/start", partieId)
                .header("Authorization", "Bearer " + creatorToken))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/parties/{partieId}/distribution/me", partieId)
                .header("Authorization", "Bearer " + creatorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.maMain.length()").value(9))
            .andExpect(jsonPath("$.nombreCartesCentreCachees").value(9))
            .andExpect(jsonPath("$.joueurCourant").value("dst1"))
            .andExpect(jsonPath("$.nombreCartesParJoueur.dst1").value(9))
            .andExpect(jsonPath("$.nombreCartesParJoueur.dst2").value(9))
            .andExpect(jsonPath("$.nombreCartesParJoueur.dst3").value(9));
    }

    @Test
    void distributionEndpoint_shouldRejectUserNotInPartie() throws Exception {
        String creatorToken = register("out1@test.com", "out1", "Password123");
        String p2Token = register("out2@test.com", "out2", "Password123");
        String outsiderToken = register("out3@test.com", "out3", "Password123");
        long partieId = createPartie(creatorToken, "A", 3);

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p2Token))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/parties/{partieId}/distribution/me", partieId)
                .header("Authorization", "Bearer " + outsiderToken))
            .andExpect(status().isForbidden());
    }

    @Test
    void distributionEndpoint_shouldRejectWhenGameNotStarted() throws Exception {
        String creatorToken = register("nostart1@test.com", "nostart1", "Password123");
        long partieId = createPartie(creatorToken, "A", 3);

        mockMvc.perform(get("/api/parties/{partieId}/distribution/me", partieId)
                .header("Authorization", "Bearer " + creatorToken))
            .andExpect(status().isConflict());
    }

    @Test
    void moveEndpoint_shouldRevealCardAndKeepTurnWhenSequenceNotResolved() throws Exception {
        String creatorToken = register("move1@test.com", "mv1", "Password123");
        String p2Token = register("move2@test.com", "mv2", "Password123");
        String p3Token = register("move3@test.com", "mv3", "Password123");
        long partieId = createPartie(creatorToken, "A", 3);

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p2Token))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p3Token))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/parties/{partieId}/start", partieId)
                .header("Authorization", "Bearer " + creatorToken))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/parties/{partieId}/moves", partieId)
                .header("Authorization", "Bearer " + creatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("type", "REVEAL_CENTER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("REVEAL_CENTER"))
            .andExpect(jsonPath("$.statut").value("EN_COURS"))
            .andExpect(jsonPath("$.joueur").value("mv1"))
            .andExpect(jsonPath("$.tourTermine").value(false))
            .andExpect(jsonPath("$.resolutionTour").value("EN_ATTENTE"))
            .andExpect(jsonPath("$.valeursReveleesTour.length()").value(1))
            .andExpect(jsonPath("$.joueurCourant").value("mv1"))
            .andExpect(jsonPath("$.numeroTour").value(1))
            .andExpect(jsonPath("$.cartesCentreCacheesRestantes").value(8));

        mockMvc.perform(get("/api/parties/{partieId}/distribution/me", partieId)
                .header("Authorization", "Bearer " + p2Token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cartesCentre.length()").value(9))
            .andExpect(jsonPath("$.cartesCentre[0].revelee").value(true))
            .andExpect(jsonPath("$.cartesCentre[0].valeur").isNumber())
            .andExpect(jsonPath("$.nombreCartesCentreCachees").value(8));
    }

    @Test
    void moveEndpoint_shouldRejectWhenNotCurrentPlayer() throws Exception {
        String creatorToken = register("turn1@test.com", "tr1", "Password123");
        String p2Token = register("turn2@test.com", "tr2", "Password123");
        String p3Token = register("turn3@test.com", "tr3", "Password123");
        long partieId = createPartie(creatorToken, "A", 3);

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p2Token))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p3Token))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/parties/{partieId}/start", partieId)
                .header("Authorization", "Bearer " + creatorToken))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/parties/{partieId}/moves", partieId)
                .header("Authorization", "Bearer " + p2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("type", "REVEAL_CENTER"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void moveEndpoint_shouldEndTurnAndRollbackWhenNumbersDiffer() throws Exception {
        String creatorToken = register("fail1@test.com", "fl1", "Password123");
        String p2Token = register("fail2@test.com", "fl2", "Password123");
        String p3Token = register("fail3@test.com", "fl3", "Password123");
        long partieId = createPartie(creatorToken, "A", 3);

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p2Token))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p3Token))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/parties/{partieId}/start", partieId)
                .header("Authorization", "Bearer " + creatorToken))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/parties/{partieId}/moves", partieId)
                .header("Authorization", "Bearer " + creatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "type", "REVEAL_PLAYER_MIN",
                    "targetPlayerPseudo", "fl1"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tourTermine").value(false))
            .andExpect(jsonPath("$.resolutionTour").value("EN_ATTENTE"))
            .andExpect(jsonPath("$.joueurCourant").value("fl1"))
            .andExpect(jsonPath("$.numeroTour").value(1));

        mockMvc.perform(post("/api/parties/{partieId}/moves", partieId)
                .header("Authorization", "Bearer " + creatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "type", "REVEAL_PLAYER_MAX",
                    "targetPlayerPseudo", "fl1"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tourTermine").value(true))
            .andExpect(jsonPath("$.resolutionTour").value("ECHEC_NUMEROS_DIFFERENTS"))
            .andExpect(jsonPath("$.joueurCourant").value("fl2"))
            .andExpect(jsonPath("$.numeroTour").value(2));
    }

    @Test
    void gameSimulation_shouldCreateJoinStartAndRejectLateJoin() throws Exception {
        String creatorToken = register("sim1@test.com", "sim1", "Password123");
        String p2Token = register("sim2@test.com", "sim2", "Password123");
        String p3Token = register("sim3@test.com", "sim3", "Password123");
        String lateToken = register("sim4@test.com", "sim4", "Password123");

        long partieId = createPartie(creatorToken, "B", 3);

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p2Token))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p3Token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombreJoueursActuels").value(3));

        mockMvc.perform(post("/api/parties/{partieId}/start", partieId)
                .header("Authorization", "Bearer " + creatorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statut").value("EN_COURS"));

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + lateToken))
            .andExpect(status().isConflict());
    }

    @Test
    void createPartieEndpoint_shouldRejectInvalidNombreJoueurs() throws Exception {
        String token = register("invalid@test.com", "invalidUser", "Password123");

        mockMvc.perform(post("/api/parties")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "variante", "A",
                    "nombreJoueurs", 2
                ))))
            .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/parties")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "variante", "A",
                    "nombreJoueurs", 7
                ))))
            .andExpect(status().isBadRequest());
    }

    @Test
    void joinPartieEndpoint_shouldRejectWhenPartieIsFull() throws Exception {
        String creatorToken = register("full1@test.com", "full1", "Password123");
        String p2Token = register("full2@test.com", "full2", "Password123");
        String p3Token = register("full3@test.com", "full3", "Password123");
        String lateToken = register("full4@test.com", "full4", "Password123");
        long partieId = createPartie(creatorToken, "A", 3);

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p2Token))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p3Token))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + lateToken))
            .andExpect(status().isConflict());
    }

    @Test
    void startPartieEndpoint_shouldRejectWhenNonCreatorStarts() throws Exception {
        String creatorToken = register("start1@test.com", "sta1", "Password123");
        String p2Token = register("start2@test.com", "sta2", "Password123");
        String p3Token = register("start3@test.com", "sta3", "Password123");
        long partieId = createPartie(creatorToken, "B", 3);

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p2Token))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p3Token))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/parties/{partieId}/start", partieId)
                .header("Authorization", "Bearer " + p2Token))
            .andExpect(status().isForbidden());
    }

    @Test
    void startPartieEndpoint_shouldRejectWhenPartieIsNotFull() throws Exception {
        String creatorToken = register("notfull1@test.com", "nf1", "Password123");
        String p2Token = register("notfull2@test.com", "nf2", "Password123");
        long partieId = createPartie(creatorToken, "A", 3);

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p2Token))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/parties/{partieId}/start", partieId)
                .header("Authorization", "Bearer " + creatorToken))
            .andExpect(status().isConflict());
    }

    @Test
    void moveEndpoint_shouldTerminateVariantAAfterThirdTrio() throws Exception {
        String creatorToken = register("varta1@test.com", "varta1", "Password123");
        String p2Token = register("varta2@test.com", "varta2", "Password123");
        String p3Token = register("varta3@test.com", "varta3", "Password123");
        long partieId = createPartie(creatorToken, "A", 3);

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p2Token))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p3Token))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/parties/{partieId}/start", partieId)
                .header("Authorization", "Bearer " + creatorToken))
            .andExpect(status().isOk());

        prepareVariantATerminalState(partieId);

        mockMvc.perform(post("/api/parties/{partieId}/moves", partieId)
                .header("Authorization", "Bearer " + creatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("type", "REVEAL_CENTER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statut").value("EN_COURS"))
            .andExpect(jsonPath("$.tourTermine").value(false));

        mockMvc.perform(post("/api/parties/{partieId}/moves", partieId)
                .header("Authorization", "Bearer " + creatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("type", "REVEAL_CENTER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statut").value("EN_COURS"))
            .andExpect(jsonPath("$.tourTermine").value(false));

        mockMvc.perform(post("/api/parties/{partieId}/moves", partieId)
                .header("Authorization", "Bearer " + creatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("type", "REVEAL_CENTER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resolutionTour").value("TRIO_GAGNE"))
            .andExpect(jsonPath("$.trioRemporte").value(true))
            .andExpect(jsonPath("$.statut").value("TERMINEE"))
            .andExpect(jsonPath("$.joueurCourant").isEmpty())
            .andExpect(jsonPath("$.gagnant").value("varta1"))
            .andExpect(jsonPath("$.conditionVictoire").value("TROIS_TRIOS"));

        mockMvc.perform(get("/api/parties/{partieId}", partieId)
                .header("Authorization", "Bearer " + creatorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statut").value("TERMINEE"))
            .andExpect(jsonPath("$.gagnant").value("varta1"))
            .andExpect(jsonPath("$.conditionVictoire").value("TROIS_TRIOS"));
    }

    @Test
    void moveEndpoint_shouldTerminateVariantBAfterLinkedTrios() throws Exception {
        String creatorToken = register("vartb1@test.com", "vartb1", "Password123");
        String p2Token = register("vartb2@test.com", "vartb2", "Password123");
        String p3Token = register("vartb3@test.com", "vartb3", "Password123");
        long partieId = createPartie(creatorToken, "B", 3);

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p2Token))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p3Token))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/parties/{partieId}/start", partieId)
                .header("Authorization", "Bearer " + creatorToken))
            .andExpect(status().isOk());

        prepareVariantBTerminalState(partieId);

        mockMvc.perform(post("/api/parties/{partieId}/moves", partieId)
                .header("Authorization", "Bearer " + creatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("type", "REVEAL_CENTER"))))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/parties/{partieId}/moves", partieId)
                .header("Authorization", "Bearer " + creatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("type", "REVEAL_CENTER"))))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/parties/{partieId}/moves", partieId)
                .header("Authorization", "Bearer " + creatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("type", "REVEAL_CENTER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resolutionTour").value("TRIO_GAGNE"))
            .andExpect(jsonPath("$.trioRemporte").value(true))
            .andExpect(jsonPath("$.statut").value("TERMINEE"))
            .andExpect(jsonPath("$.joueurCourant").isEmpty())
            .andExpect(jsonPath("$.gagnant").value("vartb1"))
            .andExpect(jsonPath("$.conditionVictoire").value("DEUX_TRIOS_LIES"));

        mockMvc.perform(get("/api/parties/{partieId}", partieId)
                .header("Authorization", "Bearer " + creatorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statut").value("TERMINEE"))
            .andExpect(jsonPath("$.gagnant").value("vartb1"))
            .andExpect(jsonPath("$.conditionVictoire").value("DEUX_TRIOS_LIES"));
    }

    @Test
    void fullGameSimulation_variantA_shouldCoverLifecycleAndRejectPostEndActions() throws Exception {
        String creatorToken = register("sima1@test.com", "sima1", "Password123");
        String p2Token = register("sima2@test.com", "sima2", "Password123");
        String p3Token = register("sima3@test.com", "sima3", "Password123");
        String lateToken = register("sima4@test.com", "sima4", "Password123");
        long partieId = createPartie(creatorToken, "A", 3);

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p2Token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombreJoueursActuels").value(2));
        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p3Token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombreJoueursActuels").value(3));
        mockMvc.perform(post("/api/parties/{partieId}/start", partieId)
                .header("Authorization", "Bearer " + creatorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statut").value("EN_COURS"))
            .andExpect(jsonPath("$.joueurCourant").value("sima1"));

        prepareVariantATerminalState(partieId);

        playCenter(partieId, creatorToken).andExpect(jsonPath("$.tourTermine").value(false));
        playCenter(partieId, creatorToken).andExpect(jsonPath("$.tourTermine").value(false));
        playCenter(partieId, creatorToken)
            .andExpect(jsonPath("$.tourTermine").value(true))
            .andExpect(jsonPath("$.statut").value("TERMINEE"))
            .andExpect(jsonPath("$.gagnant").value("sima1"))
            .andExpect(jsonPath("$.conditionVictoire").value("TROIS_TRIOS"));

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + lateToken))
            .andExpect(status().isConflict());

        mockMvc.perform(post("/api/parties/{partieId}/moves", partieId)
                .header("Authorization", "Bearer " + p2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("type", "REVEAL_CENTER"))))
            .andExpect(status().isConflict());

        assertStats(creatorToken, "sima1@test.com", "sima1", 1, 1, 1, 1, 0, 0);
        assertStats(p2Token, "sima2@test.com", "sima2", 1, 0, 1, 0, 0, 0);
    }

    @Test
    void fullGameSimulation_variantB_shouldRotateTurnOnFailureThenEndWithLinkedTrios() throws Exception {
        String creatorToken = register("simb1@test.com", "simb1", "Password123");
        String p2Token = register("simb2@test.com", "simb2", "Password123");
        String p3Token = register("simb3@test.com", "simb3", "Password123");
        long partieId = createPartie(creatorToken, "B", 3);

        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p2Token))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/parties/{partieId}/join", partieId)
                .header("Authorization", "Bearer " + p3Token))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/parties/{partieId}/start", partieId)
                .header("Authorization", "Bearer " + creatorToken))
            .andExpect(status().isOk());

        prepareVariantBFailureThenWinState(partieId, "simb2");

        mockMvc.perform(post("/api/parties/{partieId}/moves", partieId)
                .header("Authorization", "Bearer " + creatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "type", "REVEAL_PLAYER_MIN",
                    "targetPlayerPseudo", "simb1"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tourTermine").value(false))
            .andExpect(jsonPath("$.joueurCourant").value("simb1"));

        mockMvc.perform(post("/api/parties/{partieId}/moves", partieId)
                .header("Authorization", "Bearer " + creatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "type", "REVEAL_PLAYER_MAX",
                    "targetPlayerPseudo", "simb1"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tourTermine").value(true))
            .andExpect(jsonPath("$.resolutionTour").value("ECHEC_NUMEROS_DIFFERENTS"))
            .andExpect(jsonPath("$.joueurCourant").value("simb2"));

        mockMvc.perform(post("/api/parties/{partieId}/moves", partieId)
                .header("Authorization", "Bearer " + p2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("type", "REVEAL_CENTER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tourTermine").value(false))
            .andExpect(jsonPath("$.joueurCourant").value("simb2"));

        mockMvc.perform(post("/api/parties/{partieId}/moves", partieId)
                .header("Authorization", "Bearer " + p2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("type", "REVEAL_CENTER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tourTermine").value(false))
            .andExpect(jsonPath("$.joueurCourant").value("simb2"));

        mockMvc.perform(post("/api/parties/{partieId}/moves", partieId)
                .header("Authorization", "Bearer " + p2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("type", "REVEAL_CENTER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tourTermine").value(true))
            .andExpect(jsonPath("$.resolutionTour").value("TRIO_GAGNE"))
            .andExpect(jsonPath("$.statut").value("TERMINEE"))
            .andExpect(jsonPath("$.gagnant").value("simb2"))
            .andExpect(jsonPath("$.conditionVictoire").value("DEUX_TRIOS_LIES"));

        mockMvc.perform(post("/api/parties/{partieId}/moves", partieId)
                .header("Authorization", "Bearer " + p3Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("type", "REVEAL_CENTER"))))
            .andExpect(status().isConflict());

        assertStats(p2Token, "simb2@test.com", "simb2", 1, 1, 0, 0, 1, 1);
        assertStats(creatorToken, "simb1@test.com", "simb1", 1, 0, 0, 0, 1, 0);
    }

    private void assertStats(
        String token,
        String email,
        String pseudo,
        int partiesJouees,
        int victoires,
        int partiesJoueesVariantA,
        int victoiresVariantA,
        int partiesJoueesVariantB,
        int victoiresVariantB
    ) throws Exception {
        mockMvc.perform(get("/api/stats/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.pseudo").value(pseudo))
            .andExpect(jsonPath("$.partiesJouees").value(partiesJouees))
            .andExpect(jsonPath("$.victoires").value(victoires))
            .andExpect(jsonPath("$.variantes[0].variante").value("A"))
            .andExpect(jsonPath("$.variantes[0].partiesJouees").value(partiesJoueesVariantA))
            .andExpect(jsonPath("$.variantes[0].victoires").value(victoiresVariantA))
            .andExpect(jsonPath("$.variantes[1].variante").value("B"))
            .andExpect(jsonPath("$.variantes[1].partiesJouees").value(partiesJoueesVariantB))
            .andExpect(jsonPath("$.variantes[1].victoires").value(victoiresVariantB));
    }

    private long createPartie(String token, String variante, int nombreJoueurs) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/parties")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "variante", variante,
                    "nombreJoueurs", nombreJoueurs
                ))))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("id").asLong();
    }

    private String register(String email, String pseudo, String password) throws Exception {
        return registerAuth(email, pseudo, password).get("accessToken").asText();
    }

    private JsonNode registerAuth(String email, String pseudo, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "email", email,
                    "pseudo", pseudo,
                    "password", password
                ))))
            .andExpect(status().isOk())
            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String json(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    private void prepareVariantATerminalState(long partieId) {
        Partie partie = partieRepository.findById(partieId).orElseThrow();
        AppUser createur = partie.getCreateur();

        cartePartieRepository.deleteAll(cartePartieRepository.findByPartieId(partieId));

        List<CartePartie> cartes = List.of(
            trioGagne(partie, createur, 1),
            trioGagne(partie, createur, 1),
            trioGagne(partie, createur, 1),
            trioGagne(partie, createur, 2),
            trioGagne(partie, createur, 2),
            trioGagne(partie, createur, 2),
            CartePartie.pourCentreCache(partie, 3),
            CartePartie.pourCentreCache(partie, 3),
            CartePartie.pourCentreCache(partie, 3)
        );

        cartePartieRepository.saveAll(cartes);
    }

    private void prepareVariantBTerminalState(long partieId) {
        Partie partie = partieRepository.findById(partieId).orElseThrow();
        AppUser createur = partie.getCreateur();

        cartePartieRepository.deleteAll(cartePartieRepository.findByPartieId(partieId));

        List<CartePartie> cartes = List.of(
            trioGagne(partie, createur, 1),
            trioGagne(partie, createur, 1),
            trioGagne(partie, createur, 1),
            CartePartie.pourCentreCache(partie, 6),
            CartePartie.pourCentreCache(partie, 6),
            CartePartie.pourCentreCache(partie, 6)
        );

        cartePartieRepository.saveAll(cartes);
    }

    private CartePartie trioGagne(Partie partie, AppUser gagnant, int valeur) {
        CartePartie carte = CartePartie.pourMainJoueur(partie, gagnant, valeur);
        carte.attribuerAuTrio(gagnant);
        return carte;
    }

    private org.springframework.test.web.servlet.ResultActions playCenter(long partieId, String token) throws Exception {
        return mockMvc.perform(post("/api/parties/{partieId}/moves", partieId)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("type", "REVEAL_CENTER"))))
            .andExpect(status().isOk());
    }

    private void prepareVariantBFailureThenWinState(long partieId, String joueur2Pseudo) {
        Partie partie = partieRepository.findById(partieId).orElseThrow();
        AppUser createur = partie.getCreateur();
        AppUser joueur2 = partie.getJoueurs().stream()
            .filter(j -> joueur2Pseudo.equals(j.getPseudo()))
            .findFirst()
            .orElseThrow();

        cartePartieRepository.deleteAll(cartePartieRepository.findByPartieId(partieId));

        List<CartePartie> cartes = List.of(
            trioGagne(partie, joueur2, 1),
            trioGagne(partie, joueur2, 1),
            trioGagne(partie, joueur2, 1),
            CartePartie.pourMainJoueur(partie, createur, 2),
            CartePartie.pourMainJoueur(partie, createur, 9),
            CartePartie.pourCentreCache(partie, 6),
            CartePartie.pourCentreCache(partie, 6),
            CartePartie.pourCentreCache(partie, 6)
        );
        cartePartieRepository.saveAll(cartes);
    }
}
