package fr.orleans.m1miage.trioweb.security;

import fr.orleans.m1miage.trioweb.controller.AuthController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String path = request.getRequestURI();

        // Routes publiques
        if (path.equals("/") || path.equals("/accueil") || path.equals("/login") || path.equals("/register") || path.equals("/logout") ||
                path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        boolean loggedIn = (session != null && session.getAttribute(AuthController.SESSION_TOKEN) != null);

        if (!loggedIn) {
            response.sendRedirect("/login");
            return false;
        }

        return true;
    }
}
