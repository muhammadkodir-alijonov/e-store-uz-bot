package org.example.telegramstorebotapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.telegramstorebotapplication.model.UserSession;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreApiService {

    private final WebClient webClient;
    private final SessionService sessionService;

    public Mono<Map> getDailyStatistics(Long chatId, String type) {
        UserSession session = sessionService.getSession(chatId);
        String endpoint = determineEndpoint("/getStatisticForDay", type);

        return fetchStatistics(session, endpoint);
    }

    public Mono<Map> getWeeklyStatistics(Long chatId, String type) {
        UserSession session = sessionService.getSession(chatId);
        String endpoint = determineEndpoint("/getStatisticForWeek", type);

        return fetchStatistics(session, endpoint);
    }

    public Mono<Map> getMonthlyStatistics(Long chatId, String type) {
        UserSession session = sessionService.getSession(chatId);
        String endpoint = determineEndpoint("/getStatisticForMonth", type);

        return fetchStatistics(session, endpoint);
    }

    public Mono<Map> getYearlyStatistics(Long chatId, String type) {
        UserSession session = sessionService.getSession(chatId);
        String endpoint = determineEndpoint("/getStatisticForYear", type);

        return fetchStatistics(session, endpoint);
    }

    private String determineEndpoint(String baseEndpoint, String type) {
        if (type != null) {
            return baseEndpoint + "/" + type;
        }
        return baseEndpoint;
    }

    private Mono<Map> fetchStatistics(UserSession session, String endpoint) {
        if (!session.isAuthenticated() || session.getToken() == null) {
            log.error("User not authenticated or token is missing for chat ID: {}", session.getChatId());
            return Mono.error(new IllegalStateException("User not authenticated"));
        }

        return webClient.get()
                .uri(endpoint)
                .header("Authorization", "Bearer " + session.getToken())
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(error -> log.error("Error fetching statistics: {}", error.getMessage()));
    }
}

