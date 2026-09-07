package fr.orleans.m1miage.trioweb.dtos;

public class PlayMoveForm {

    private String type;
    private Long centerCardId;
    private String targetPlayerPseudo;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getCenterCardId() { return centerCardId; }
    public void setCenterCardId(Long centerCardId) { this.centerCardId = centerCardId; }

    public String getTargetPlayerPseudo() { return targetPlayerPseudo; }
    public void setTargetPlayerPseudo(String targetPlayerPseudo) { this.targetPlayerPseudo = targetPlayerPseudo; }
}