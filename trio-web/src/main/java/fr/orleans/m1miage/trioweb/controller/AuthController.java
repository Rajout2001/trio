package fr.orleans.m1miage.trioweb.controller;

import fr.orleans.m1miage.trioweb.dtos.LoginForm;
import fr.orleans.m1miage.trioweb.dtos.RegisterForm;
import fr.orleans.m1miage.trioweb.dtos.api.AuthResponseDto;
import fr.orleans.m1miage.trioweb.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    public static final String SESSION_TOKEN = "JWT_TOKEN";
    public static final String SESSION_PSEUDO = "PLAYER_PSEUDO";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "auth/login";
    }

    @PostMapping("/login")
    public String loginSubmit(
            @Valid @ModelAttribute("loginForm") LoginForm loginForm,
            BindingResult result,
            HttpSession session,
            Model model
    ) {
        if (result.hasErrors()) {
            return "auth/login";
        }
        try {
            AuthResponseDto auth = authService.login(loginForm.getEmail(), loginForm.getPassword());
            storeAuthSession(session, auth);
            return "redirect:/accueil";
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("erreur", e.getMessage());
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(
            @Valid @ModelAttribute("registerForm") RegisterForm registerForm,
            BindingResult result,
            HttpSession session,
            Model model
    ) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            AuthResponseDto auth = authService.register(registerForm.getEmail(), registerForm.getPseudo(), registerForm.getPassword());
            storeAuthSession(session, auth);
            return "redirect:/accueil";
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("erreur", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    private void storeAuthSession(HttpSession session, AuthResponseDto auth) {
        session.setAttribute(SESSION_TOKEN, auth.getAccessToken());
        session.setAttribute(SESSION_PSEUDO, auth.getPseudo());
    }
}
