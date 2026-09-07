package fr.orleans.m1miage.trioweb.dtos.api;

public class RegisterRequestDto {

    private String email;
    private String pseudo;
    private String password;

    public RegisterRequestDto() {}

    public RegisterRequestDto(String email, String pseudo, String password) {
        this.email = email;
        this.pseudo = pseudo;
        this.password = password;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPseudo() { return pseudo; }
    public void setPseudo(String pseudo) { this.pseudo = pseudo; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}