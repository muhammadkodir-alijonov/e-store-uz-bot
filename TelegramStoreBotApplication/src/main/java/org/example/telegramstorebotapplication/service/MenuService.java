package org.example.telegramstorebotapplication.service;

import lombok.RequiredArgsConstructor;
import org.example.telegramstorebotapplication.model.UserSession;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final SessionService sessionService;

    private static final Map<UserSession.MenuState, String> MENU_TEXTS = Map.of(
            UserSession.MenuState.DAILY_STATISTICS, "daily",
            UserSession.MenuState.WEEKLY_STATISTICS, "weekly",
            UserSession.MenuState.MONTHLY_STATISTICS, "monthly",
            UserSession.MenuState.YEARLY_STATISTICS, "yearly"
    );

    public SendMessage createMainMenu(Long chatId) {
        return buildSendMessage(chatId, "Welcome to the Store Statistics Bot! Please select an option:", getMainMenuKeyboard());
    }

    public EditMessageText createStatisticsSubMenu(CallbackQuery callbackQuery, UserSession.MenuState menuState) {
        return buildEditMessage(callbackQuery, "Please select the type of " + MENU_TEXTS.get(menuState) + " statistics you want to view:", getStatisticsSubMenuKeyboard(menuState));
    }

    public EditMessageText handleBackToMainMenu(CallbackQuery callbackQuery) {
        return buildEditMessage(
                callbackQuery,
                "🏪 Store Statistics Botga Xush Kelibsiz! Quyidagi variantlardan birini tanlang:",
                getMainMenuKeyboard()
        );
    }

    private SendMessage buildSendMessage(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(keyboard);
        return message;
    }

    private EditMessageText buildEditMessage(CallbackQuery callbackQuery, String text, InlineKeyboardMarkup keyboard) {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(callbackQuery.getMessage().getChatId().toString());
        editMessage.setMessageId(callbackQuery.getMessage().getMessageId());
        editMessage.setText(text);
        editMessage.setReplyMarkup(keyboard);
        return editMessage;
    }

    private InlineKeyboardMarkup getMainMenuKeyboard() {
        return buildKeyboard(
                List.of(
                        "📊 Daily Statistics", "daily_stats",
                        "📈 Weekly Statistics", "weekly_stats",
                        "📆 Monthly Statistics", "monthly_stats",
                        "🗓 Yearly Statistics", "yearly_stats"
                )
        );
    }

    private InlineKeyboardMarkup getStatisticsSubMenuKeyboard(UserSession.MenuState menuState) {
        String prefix = MENU_TEXTS.get(menuState);
        return buildKeyboard(
                List.of(
                        "🟢 General Statistics", prefix + "_general",
                        "📦 Product Statistics", prefix + "_product",
                        "📥 Order Statistics", prefix + "_order",
                        "🔙 Back to Main Menu", "main_menu"
                )
        );
    }

    private InlineKeyboardMarkup buildKeyboard(List<String> buttonData) {
        List<List<InlineKeyboardButton>> rowsInline = Stream.iterate(0, n -> n + 2)
                .limit(buttonData.size() / 2)
                .map(i -> List.of(createButton(buttonData.get(i), buttonData.get(i + 1))))
                .collect(Collectors.toList());

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }
}