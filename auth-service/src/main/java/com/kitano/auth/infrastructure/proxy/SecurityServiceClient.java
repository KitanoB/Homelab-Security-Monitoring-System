package com.kitano.auth.infrastructure.proxy;

import com.kitano.core.model.SystemEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class SecurityServiceClient {

    private final WebClient webClient;

    public SecurityServiceClient(@Value("${services.security.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public List<SystemEvent> getEventsForUser(String userId) {
        return webClient.get()
                .uri("/api/events/{id}/events", userId)
                .retrieve()
                .bodyToFlux(SystemEvent.class)
                .collectList()
                .block(); // or return Mono<List<SystemEvent>> for async support
    }

    public List<SystemEvent> getAllEvents() {
        return webClient.get()
                .uri("/api/events/all")
                .retrieve()
                .bodyToFlux(SystemEvent.class)
                .collectList()
                .block(); // or return Mono<List<SystemEvent>> for async support
    }
}