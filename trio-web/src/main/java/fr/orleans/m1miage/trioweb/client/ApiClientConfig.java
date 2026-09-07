package fr.orleans.m1miage.trioweb.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ApiClientConfig {

    private final SessionBearerRequestInterceptor sessionBearerRequestInterceptor;

    public ApiClientConfig(SessionBearerRequestInterceptor sessionBearerRequestInterceptor) {
        this.sessionBearerRequestInterceptor = sessionBearerRequestInterceptor;
    }

    @Bean
    RestClient trioApiRestClient(@Value("${trio.api.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor(sessionBearerRequestInterceptor)
                .build();
    }
}
