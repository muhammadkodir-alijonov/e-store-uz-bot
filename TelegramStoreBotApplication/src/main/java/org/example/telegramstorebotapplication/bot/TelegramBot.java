package org.example.telegramstorebotapplication.bot;

import lombok.extern.slf4j.Slf4j;
import org.example.telegramstorebotapplication.config.BotConfig;
import org.example.telegramstorebotapplication.model.AuthResponse;
import org.example.telegramstorebotapplication.model.UserSession;
import org.example.telegramstorebotapplication.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Slf4j
public class TelegramBot extends TelegramWebhookBot {

    private final BotConfig botConfig;
    private final AuthService authService;
    private final SessionService sessionService;
    private final MenuService menuService;
    private final StoreApiService storeApiService;
    private final MessageFormatterService messageFormatterService;


    public TelegramBot(BotConfig botConfig,
                       AuthService authService,
                       SessionService sessionService,
                       MenuService menuService,
                       StoreApiService storeApiService,
                       MessageFormatterService messageFormatterService) {
        super(botConfig.getBotToken());
        this.botConfig = botConfig;
        this.authService = authService;
        this.sessionService = sessionService;
        this.menuService = menuService;
        this.storeApiService = storeApiService;
        this.messageFormatterService = messageFormatterService;
    }


    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                return handleTextMessage(update);
            } else if (update.hasCallbackQuery()) {
                return handleCallbackQuery(update);
            }
        } catch (Exception e) {
            log.error("Error handling update: {}", e.getMessage());
        }
        return null;
    }

    private BotApiMethod<?> handleTextMessage(Update update) {
        String messageText = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        Integer messageId = update.getMessage().getMessageId();

        log.info("Received message: '{}' from chat ID: {}", messageText, chatId);

        // Check if user is authenticated
        if (!authService.isAuthenticated(chatId)) {
            return handleUnauthenticatedUser(chatId, messageText);
        } else {
            // User is authenticated, handle commands
            if (messageText.equals("/start")) {
                return menuService.createMainMenu(chatId);
            } else {
                SendMessage message = new SendMessage();
                message.setChatId(chatId.toString());
                message.setText("Please use the menu to navigate. Type /start to see the main menu.");
                return message;
            }
        }
    }

    private BotApiMethod<?> handleUnauthenticatedUser(Long chatId, String messageText) {
        UserSession.AuthState authState = authService.getAuthState(chatId);

        if (messageText.equals("/start") || authState == UserSession.AuthState.NONE) {
            authService.setAuthState(chatId, UserSession.AuthState.AWAITING_USERNAME);
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText("Welcome to the Store Statistics Bot! Please enter your username to login:");
            return message;
        } else if (authState == UserSession.AuthState.AWAITING_USERNAME) {
            authService.setUsername(chatId, messageText);
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText("Thank you! Now please enter your password:");
            return message;
        } else if (authState == UserSession.AuthState.AWAITING_PASSWORD) {
            authService.setPassword(chatId, messageText);

            // Attempt to authenticate asynchronously
            authService.authenticate(chatId)
                    .subscribe(
                            response -> handleAuthenticationResponse(chatId, response),
                            error -> handleAuthenticationError(chatId, error)
                    );

            // Return a temporary message while authentication is in progress
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText("Authenticating...");
            return message;
        }

        return null;
    }

    private void handleAuthenticationResponse(Long chatId, AuthResponse response) {
        if (response.getErrors() == null) {
            SendMessage successMessage = new SendMessage();
            successMessage.setChatId(chatId.toString());
            successMessage.setText("Thank you! You have been successfully authenticated.");

            try {
                execute(successMessage);
                execute(menuService.createMainMenu(chatId));
            } catch (TelegramApiException e) {
                log.error("Error sending authentication success message: {}", e.getMessage());
            }
        } else {
            SendMessage failureMessage = new SendMessage();
            failureMessage.setChatId(chatId.toString());
            failureMessage.setText("Authentication failed: " + response.getMessage() +
                    "\nPlease try again. Enter your username:");

            authService.setAuthState(chatId, UserSession.AuthState.AWAITING_USERNAME);

            try {
                execute(failureMessage);
            } catch (TelegramApiException e) {
                log.error("Error sending authentication failure message: {}", e.getMessage());
            }
        }
    }

    private void handleAuthenticationError(Long chatId, Throwable error) {
        log.error("Error during authentication: {}", error.getMessage());
        SendMessage errorMessage = new SendMessage();
        errorMessage.setChatId(chatId.toString());
        errorMessage.setText("An error occurred during authentication. Please try again later.");

        try {
            execute(errorMessage);
        } catch (TelegramApiException e) {
            log.error("Error sending error message: {}", e.getMessage());
        }
    }

    private BotApiMethod<?> handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getMessage().getMessageId();

        log.info("Received callback query: '{}' from chat ID: {}", callbackData, chatId);

        if (!authService.isAuthenticated(chatId)) {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText("You need to authenticate first. Please enter your username:");
            authService.setAuthState(chatId, UserSession.AuthState.AWAITING_USERNAME);
            return message;
        }

        if (callbackData.equals("main_menu")) {
            return menuService.createMainMenu(chatId);
        } else if (callbackData.equals("daily_stats")) {
            return menuService.createStatisticsSubMenu(chatId, UserSession.MenuState.DAILY_STATISTICS);
        } else if (callbackData.equals("weekly_stats")) {
            return menuService.createStatisticsSubMenu(chatId, UserSession.MenuState.WEEKLY_STATISTICS);
        } else if (callbackData.equals("monthly_stats")) {
            return menuService.createStatisticsSubMenu(chatId, UserSession.MenuState.MONTHLY_STATISTICS);
        } else if (callbackData.equals("yearly_stats")) {
            return menuService.createStatisticsSubMenu(chatId, UserSession.MenuState.YEARLY_STATISTICS);
        } else if (callbackData.startsWith("daily_") ||
                callbackData.startsWith("weekly_") ||
                callbackData.startsWith("monthly_") ||
                callbackData.startsWith("yearly_")) {
            return handleStatisticsRequest(chatId, callbackData);
        }

        return null;
    }

    private BotApiMethod<?> handleStatisticsRequest(Long chatId, String callbackData) {
        String[] parts = callbackData.split("_");
        String period = parts[0];
        String type = parts[1];

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Fetching " + period + " " + type + " statistics...");
        message.enableMarkdown(true);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending initial statistics message: {}", e.getMessage());
        }

        Mono<Map> statisticsMono;

        switch (period) {
            case "daily":
                statisticsMono = storeApiService.getDailyStatistics(chatId, type.equals("general") ? null : type);
                break;
            case "weekly":
                statisticsMono = storeApiService.getWeeklyStatistics(chatId, type.equals("general") ? null : type);
                break;
            case "monthly":
                statisticsMono = storeApiService.getMonthlyStatistics(chatId, type.equals("general") ? null : type);
                break;
            case "yearly":
                statisticsMono = storeApiService.getYearlyStatistics(chatId, type.equals("general") ? null : type);
                break;
            default:
                message.setText("Invalid period specified.");
                return message;
        }

        statisticsMono.subscribe(
                statistics -> {
                    String formattedMessage;

                    switch (type) {
                        case "general":
                            formattedMessage = messageFormatterService.formatStatistics(statistics, period);
                            break;
                        case "product":
                            formattedMessage = messageFormatterService.formatProductStatistics(statistics, period);
                            break;
                        case "order":
                            formattedMessage = messageFormatterService.formatOrderStatistics(statistics, period);
                            break;
                        default:
                            formattedMessage = "Invalid statistics type specified.";
                    }

                    SendMessage resultMessage = new SendMessage();
                    resultMessage.setChatId(chatId.toString());
                    resultMessage.setText(formattedMessage);
                    resultMessage.enableMarkdown(true);

                    try {
                        execute(resultMessage);
                    } catch (TelegramApiException e) {
                        log.error("Error sending statistics result message: {}", e.getMessage());
                    }
                },
                error -> {
                    log.error("Error fetching statistics: {}", error.getMessage());
                    SendMessage errorMessage = new SendMessage();
                    errorMessage.setChatId(chatId.toString());
                    errorMessage.setText("An error occurred while fetching statistics: " + error.getMessage());

                    try {
                        execute(errorMessage);
                    } catch (TelegramApiException e) {
                        log.error("Error sending error message: {}", e.getMessage());
                    }
                }
        );

        return null;
    }

    @Override
    public String getBotPath() {
        return botConfig.getWebhookPath();
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotUsername();
    }
}