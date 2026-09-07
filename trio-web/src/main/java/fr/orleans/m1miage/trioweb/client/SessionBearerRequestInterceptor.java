package fr.orleans.m1miage.trioweb.client;

import fr.orleans.m1miage.trioweb.controller.AuthController;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

@Component
public class SessionBearerRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String token = resolveTokenFromCurrentSession();

        if (token != null && !token.isBlank() && request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION) == null) {
            request.getHeaders().setBearerAuth(token);
        }

        return execution.execute(request, body);
    }

    private String resolveTokenFromCurrentSession() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return null;
        }

        HttpSession session = servletRequestAttributes.getRequest().getSession(false);
        if (session == null) {
            return null;
        }

        Object token = session.getAttribute(AuthController.SESSION_TOKEN);
        return token instanceof String jwt ? jwt : null;
    }
}
