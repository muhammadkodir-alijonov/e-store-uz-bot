package org.example.telegramstorebotapplication.model;


import lombok.Data;

@Data
public class UserSession {
    private Long chatId;
    private Integer messageId; // Track last message ID
    private boolean authenticated;
    private String token;
    private AuthState authState;
    private MenuState menuState;
    private String username;
    private String password;
    private MenuState currentMenu;

    public UserSession(Long chatId) {
        this.chatId = chatId;
        this.authenticated = false;
        this.authState = AuthState.NONE;
        this.menuState = MenuState.NONE;
    }

    public enum AuthState {
        NONE,
        AWAITING_USERNAME,
        AWAITING_PASSWORD,
        AUTHENTICATED
    }

    public enum MenuState {
        NONE,
        MAIN_MENU,
        DAILY_STATISTICS,
        WEEKLY_STATISTICS,
        MONTHLY_STATISTICS,
        YEARLY_STATISTICS
    }
}


