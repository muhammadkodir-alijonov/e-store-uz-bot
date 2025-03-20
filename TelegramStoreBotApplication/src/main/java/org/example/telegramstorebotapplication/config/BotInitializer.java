package org.example.telegramstorebotapplication.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.telegramstorebotapplication.bot.TelegramBot;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
@Slf4j
@RequiredArgsConstructor
public class BotInitializer {

    private final TelegramBot telegramBot;
    private final BotConfig botConfig;

    @EventListener({ContextRefreshedEvent.class})
    public void init() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);

            // Create SetWebhook object
            SetWebhook setWebhook = SetWebhook.builder()
                    .url(botConfig.getWebhookPath())
                    .build();

            // Register bot with webhook
            telegramBotsApi.registerBot(telegramBot, setWebhook);

            log.info("Telegram bot registered successfully with webhook: {}", botConfig.getWebhookPath());
        } catch (TelegramApiException e) {
            log.error("Error registering Telegram bot: {}", e.getMessage());
        }
    }
}


