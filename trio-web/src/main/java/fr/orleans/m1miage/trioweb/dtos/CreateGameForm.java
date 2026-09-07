package fr.orleans.m1miage.trioweb.dtos;

import fr.orleans.m1miage.trioweb.modele.GameVariant;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreateGameForm {

    @NotNull(message = "Choisis une variante.")
    private GameVariant variant;

    @NotNull(message = "Choisis le nombre de joueurs.")
    @Min(value = 3, message = "Minimum 3 joueurs.")
    @Max(value = 6, message = "Maximum 6 joueurs.")
    private Integer maxPlayers;

    private boolean privateGame;

    private String password;

    public GameVariant getVariant() { return variant; }
    public void setVariant(GameVariant variant) { this.variant = variant; }

    public Integer getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(Integer maxPlayers) { this.maxPlayers = maxPlayers; }

    public boolean isPrivateGame() { return privateGame; }
    public void setPrivateGame(boolean privateGame) { this.privateGame = privateGame; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
