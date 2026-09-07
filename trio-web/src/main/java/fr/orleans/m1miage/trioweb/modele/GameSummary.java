package fr.orleans.m1miage.trioweb.modele;

import java.time.LocalDateTime;

public class GameSummary {
    private final long id;
    private final GameVariant variant;
    private final int maxPlayers;
    private int currentPlayers;
    private final LocalDateTime createdAt;

    public GameSummary(long id, GameVariant variant, int maxPlayers, int currentPlayers, LocalDateTime createdAt) {
        this.id = id;
        this.variant = variant;
        this.maxPlayers = maxPlayers;
        this.currentPlayers = currentPlayers;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public GameVariant getVariant() { return variant; }
    public int getMaxPlayers() { return maxPlayers; }
    public int getCurrentPlayers() { return currentPlayers; }
    public void setCurrentPlayers(int currentPlayers) { this.currentPlayers = currentPlayers; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
