package fr.orleans.m1miage.trioweb.dtos.api;

public class PlayMoveRequestDto {

    private String type;
    private Long centerCardId;
    private String targetPlayerPseudo;

    public PlayMoveRequestDto() {}

    public PlayMoveRequestDto(String type, Long centerCardId, String targetPlayerPseudo) {
        this.type = type;
        this.centerCardId = centerCardId;
        this.targetPlayerPseudo = targetPlayerPseudo;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getCenterCardId() { return centerCardId; }
    public void setCenterCardId(Long centerCardId) { this.centerCardId = centerCardId; }

    public String getTargetPlayerPseudo() { return targetPlayerPseudo; }
    public void setTargetPlayerPseudo(String targetPlayerPseudo) { this.targetPlayerPseudo = targetPlayerPseudo; }
}