package org.example.telegramstorebotapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.telegramstorebotapplication.model.AuthRequest;
import org.example.telegramstorebotapplication.model.AuthResponse;
import org.example.telegramstorebotapplication.model.UserSession;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final WebClient webClient;
    private final SessionService sessionService;

    public Mono<AuthResponse> authenticate(Long chatId) {
        UserSession session = sessionService.getSession(chatId);

        if (session.getUsername() == null || session.getPassword() == null) {
            log.error("Username or password is null for chat ID: {}", chatId);
            return Mono.just(new AuthResponse(null, null, "Username or password is missing"));
        }

        String[] parts = session.getUsername().split(":");
        if (parts.length < 2) {
            System.out.println("Invalid format");
        }

        String fullDomain = parts[1];

        int firstDot = fullDomain.indexOf('.');

        String subdomain = (firstDot != -1) ? fullDomain.substring(0, firstDot) : fullDomain;
        String username = parts[0];
        System.out.println("The username "+ username);
        System.out.println("The subdomain: " + subdomain);
        AuthRequest authRequest = new AuthRequest(username, session.getPassword());
        return webClient.post()
                .uri("/api/v1/auth/login")
                .header("X-Tenant-Id", subdomain)
                .bodyValue(authRequest)
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .doOnSuccess(  response -> {
                    if (response.getData() != null && !response.getData().isEmpty()) {
                        session.setAuthenticated(true);
                        session.setToken(response.getData());
                        session.setAuthState(UserSession.AuthState.AUTHENTICATED);
                        sessionService.saveSession(session);
                        log.info("User authenticated successfully: {}", chatId);
                    } else {
                        log.warn("Authentication failed for chat ID: {}", chatId);
                    }
                })

                .doOnError(error -> log.error("Error during authentication: {}", error.getMessage()));
    }

    public boolean isAuthenticated(Long chatId) {
        UserSession session = sessionService.getSession(chatId);
        return session.isAuthenticated();
    }

    public void setUsername(Long chatId, String username) {
        UserSession session = sessionService.getSession(chatId);
        session.setUsername(username);
        session.setAuthState(UserSession.AuthState.AWAITING_PASSWORD);
        sessionService.saveSession(session);
    }

    public void setPassword(Long chatId, String password) {
        UserSession session = sessionService.getSession(chatId);
        session.setPassword(password);
        sessionService.saveSession(session);
    }

    public UserSession.AuthState getAuthState(Long chatId) {
        return sessionService.getSession(chatId).getAuthState();
    }

    public void setAuthState(Long chatId, UserSession.AuthState authState) {
        UserSession session = sessionService.getSession(chatId);
        session.setAuthState(authState);
        sessionService.saveSession(session);
    }
}
