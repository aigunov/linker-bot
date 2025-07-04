package backend.academy.bot.state;

import backend.academy.bot.config.TelegramBot;
import backend.academy.bot.exception.TelegramApiException;
import backend.academy.bot.service.BotService;
import backend.academy.bot.service.ChatStateService;
import backend.academy.bot.service.KeyboardFactory;
import backend.academy.bot.service.StateManager;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@Getter
@Slf4j
public abstract class StateImpl implements State {
    protected static final String back_button = "Назад";
    protected final ChatState state;
    protected final String message;
    protected TelegramBot bot;

    @Autowired
    protected StateManager stateManager;

    @Autowired
    protected ChatStateService chatStateService;

    @Autowired
    protected KeyboardFactory keyboardFactory;

    @Autowired
    protected BotService botService;

    @Autowired
    public void setTelegramBot(@Lazy TelegramBot bot) {
        this.bot = bot;
    }

    public StateImpl(ChatState state, String message) {
        this.state = state;
        this.message = message;
    }

    @Override
    public void show(long chatId) {
        log.info("Current state: {}", state);
        try {
            bot.execute(new SendMessage(chatId, message)
                    .replyMarkup(keyboardFactory.getNextAndBackButtonKeyboard())
                    .parseMode(ParseMode.HTML));
        } catch (TelegramApiException e) {
            log.info("Error while sending feedback request message: {}", e.getMessage());
        }
    }

    @Override
    public void handle(Update update) {
        if (update.message() != null && update.message().text() != null) {
            handleTextInput(update);
        }
    }

    protected void showUnsupportedActionMessage(Update update) {
        log.error("Unsupported action message: {}", update.message().text());
        bot.execute(new SendMessage(update.message().chat().id(), "Неподдерживаемая функциональность")
                .parseMode(ParseMode.HTML));
        stateManager.navigate(update, ChatState.MENU);
    }

    protected void handleTextInput(Update update) {
        String message = null;
        try {
            bot.execute(new SendMessage(0, message).parseMode(ParseMode.HTML));
        } catch (TelegramApiException e) {
            log.error("Error handling free text from update '{}': {}", update.toString(), e.getMessage());
        }
    }

    protected boolean isValidURL(String urlString) {
        try {
            new URL(urlString).toURI();
            return true;
        } catch (MalformedURLException | IllegalArgumentException | URISyntaxException e) {
            return false;
        }
    }

    protected void backToMenu(Update update) {
        stateManager.navigate(update, ChatState.MENU);
    }

    protected void validatorChecker(final String message, final Long chatId) {
        try {
            bot.execute(new SendMessage(chatId, "Опечатка. \n " + message).parseMode(ParseMode.HTML));
        } catch (TelegramApiException e) {
            log.error("Error sending validatorChecker message: {}", e.getMessage());
        }
    }
}
