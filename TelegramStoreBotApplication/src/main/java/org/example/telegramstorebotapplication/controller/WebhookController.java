package org.example.telegramstorebotapplication.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.telegramstorebotapplication.bot.TelegramBot;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/telegram")
public class WebhookController {

    private final TelegramBot telegramBot;

    @PostMapping
    public ResponseEntity<BotApiMethod<?>> onUpdateReceived(@RequestBody Update update) {
        log.info("Received update: {}", update);
        return ResponseEntity.ok(telegramBot.onWebhookUpdateReceived(update));
    }
}


