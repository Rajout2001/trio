package fr.orleans.m1miage.trioweb.client;

import fr.orleans.m1miage.trioweb.controller.AuthController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SessionBearerRequestInterceptorTest {

    private final SessionBearerRequestInterceptor interceptor = new SessionBearerRequestInterceptor();

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void intercept_shouldAddBearerHeader_whenJwtIsPresentInCurrentSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_TOKEN, "fake-jwt-token");

        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setSession(session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));

        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://localhost/api/parties"));
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(execution.execute(any(), any(byte[].class))).thenReturn(response);

        interceptor.intercept(request, new byte[0], execution);

        assertEquals("Bearer fake-jwt-token", request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void intercept_shouldNotAddAuthorizationHeader_whenJwtIsMissingFromCurrentSession() throws Exception {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));

        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://localhost/api/parties"));
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(execution.execute(any(), any(byte[].class))).thenReturn(response);

        interceptor.intercept(request, new byte[0], execution);

        assertNull(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
    }
}
