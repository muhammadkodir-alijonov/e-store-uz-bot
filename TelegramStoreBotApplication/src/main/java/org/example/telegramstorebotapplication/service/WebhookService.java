package org.example.telegramstorebotapplication.service;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class WebhookService {
    private final String TELEGRAM_API_URL = "https://api.telegram.org/bot";
    private final String BOT_TOKEN = "7966549064:AAEwGx3HFD5j07f0am843avw39c9N_6nv_4";

    public void setWebhook(String webhookUrl) {
        String url = TELEGRAM_API_URL + BOT_TOKEN + "/setWebhook?url=" + webhookUrl;
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        System.out.println("Webhook response: " + response.getBody());
    }
}
