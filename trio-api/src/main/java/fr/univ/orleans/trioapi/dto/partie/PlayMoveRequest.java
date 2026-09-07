package fr.univ.orleans.trioapi.dto.partie;

import jakarta.validation.constraints.NotNull;

public class PlayMoveRequest {

    @NotNull
    private MoveType type;
    private Long centerCardId;
    private String targetPlayerPseudo;

    public MoveType getType() {
        return type;
    }

    public void setType(MoveType type) {
        this.type = type;
    }

    public Long getCenterCardId() {
        return centerCardId;
    }

    public void setCenterCardId(Long centerCardId) {
        this.centerCardId = centerCardId;
    }

    public String getTargetPlayerPseudo() {
        return targetPlayerPseudo;
    }

    public void setTargetPlayerPseudo(String targetPlayerPseudo) {
        this.targetPlayerPseudo = targetPlayerPseudo;
    }
}
