package fr.univ.orleans.trioapi.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateCredentialsRequest {

    @NotBlank
    private String currentPassword;

    @Email
    private String newEmail;

    @Size(min = 8, max = 100)
    private String newPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
