package fr.univ.orleans.trioapi.dto.auth;

public class MessageResponse {

    private final String message;

    public MessageResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
