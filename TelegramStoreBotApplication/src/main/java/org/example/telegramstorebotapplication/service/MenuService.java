package org.example.telegramstorebotapplication.service;

import lombok.RequiredArgsConstructor;
import org.example.telegramstorebotapplication.model.UserSession;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final SessionService sessionService;

    public SendMessage createMainMenu(Long chatId) {
        UserSession session = sessionService.getSession(chatId);
        session.setMenuState(UserSession.MenuState.MAIN_MENU);
        sessionService.saveSession(session);

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Welcome to the Store Statistics Bot! Please select an option:");
        message.setReplyMarkup(getMainMenuKeyboard());

        return message;
    }

    public SendMessage createStatisticsSubMenu(Long chatId, UserSession.MenuState menuState) {
        UserSession session = sessionService.getSession(chatId);
        session.setMenuState(menuState);
        sessionService.saveSession(session);

        String periodText = getPeriodText(menuState);

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Please select the type of " + periodText + " statistics you want to view:");
        message.setReplyMarkup(getStatisticsSubMenuKeyboard(menuState));

        return message;
    }

    private String getPeriodText(UserSession.MenuState menuState) {
        switch (menuState) {
            case DAILY_STATISTICS:
                return "daily";
            case WEEKLY_STATISTICS:
                return "weekly";
            case MONTHLY_STATISTICS:
                return "monthly";
            case YEARLY_STATISTICS:
                return "yearly";
            default:
                return "";
        }
    }

    private InlineKeyboardMarkup getMainMenuKeyboard() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // First row
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("Daily Statistics", "daily_stats"));
        rowsInline.add(row1);

        // Second row
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createButton("Weekly Statistics", "weekly_stats"));
        rowsInline.add(row2);

        // Third row
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createButton("Monthly Statistics", "monthly_stats"));
        rowsInline.add(row3);

        // Fourth row
        List<InlineKeyboardButton> row4 = new ArrayList<>();
        row4.add(createButton("Yearly Statistics", "yearly_stats"));
        rowsInline.add(row4);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    private InlineKeyboardMarkup getStatisticsSubMenuKeyboard(UserSession.MenuState menuState) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        String prefix = getCallbackPrefix(menuState);

        // First row
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("General Statistics", prefix + "_general"));
        rowsInline.add(row1);

        // Second row
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createButton("Product Statistics", prefix + "_product"));
        rowsInline.add(row2);

        // Third row
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createButton("Order Statistics", prefix + "_order"));
        rowsInline.add(row3);

        // Back button
        List<InlineKeyboardButton> row4 = new ArrayList<>();
        row4.add(createButton("Back to Main Menu", "main_menu"));
        rowsInline.add(row4);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    private String getCallbackPrefix(UserSession.MenuState menuState) {
        switch (menuState) {
            case DAILY_STATISTICS:
                return "daily";
            case WEEKLY_STATISTICS:
                return "weekly";
            case MONTHLY_STATISTICS:
                return "monthly";
            case YEARLY_STATISTICS:
                return "yearly";
            default:
                return "";
        }
    }

    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }
}

