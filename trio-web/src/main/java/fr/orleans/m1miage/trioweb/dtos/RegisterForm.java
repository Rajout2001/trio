package fr.orleans.m1miage.trioweb.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterForm {

    @NotBlank(message = "L'email est obligatoire.")
    @Email(message = "Email invalide.")
    private String email;

    @NotBlank(message = "Le pseudo est obligatoire.")
    @Size(min = 3, max = 40, message = "Le pseudo doit faire entre 3 et 40 caractères.")
    private String pseudo;

    @NotBlank(message = "Le mot de passe est obligatoire.")
    @Size(min = 8, max = 100, message = "Le mot de passe doit faire au moins 8 caractères.")
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPseudo() { return pseudo; }
    public void setPseudo(String pseudo) { this.pseudo = pseudo; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}