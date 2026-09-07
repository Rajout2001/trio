package fr.orleans.m1miage.trioweb.dtos.api;

public class AuthResponseDto {
    private String accessToken;
    private String email;
    private String pseudo;

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPseudo() { return pseudo; }
    public void setPseudo(String pseudo) { this.pseudo = pseudo; }
}
