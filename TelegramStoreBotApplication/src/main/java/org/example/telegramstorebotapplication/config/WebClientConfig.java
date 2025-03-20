package org.example.telegramstorebotapplication.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(BotConfig botConfig) {
        return WebClient.builder()
                .baseUrl(botConfig.getStoreApiBaseUrl())
                .build();
    }
}

