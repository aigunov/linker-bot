package backend.academy.bot.state;

import backend.academy.bot.exception.TelegramApiException;
import backend.academy.bot.service.ListRequestService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("menu-state")
public class MenuState extends StateImpl {
    private static final String track_button = "/track";
    private static final String untrack_button = "/untrack";
    private static final String list_button = "/list";
    private static final String help_button = "/help";
    private static final String tag_button = "/tags";
    private static final String notification_button = "/notifications";
    private static final List<String> buttons =
            List.of(track_button, untrack_button, list_button, help_button, tag_button, notification_button);
    private final ListRequestService listRequestService;

    @Autowired
    public MenuState(ListRequestService listRequestService) {
        super(ChatState.MENU, "Главное меню");
        this.listRequestService = listRequestService;
    }

    @Override
    public void show(long chatId) {
        log.info("Current state: {}", state);
        try {
            bot.execute(new SendMessage(chatId, "Добро пожаловать в TG-Bot-Link-Tracker")
                    .replyMarkup(keyboardFactory.getMainMenuKeyboard())
                    .parseMode(ParseMode.HTML));
            bot.execute(new SendMessage(chatId, message));
        } catch (TelegramApiException e) {
            log.info("Error while sending feedback request message: {}", e.getMessage());
        }
    }

    @Override
    public void handle(final Update update) {
        if (buttons.contains(update.message().text())) {
            var button = update.message().text();
            switch (button) {
                case track_button -> stateManager.navigate(update, ChatState.TRACK);
                case untrack_button -> stateManager.navigate(update, ChatState.UNTRACKED);
                case list_button -> {
                    listRequestService.createListRequest(update.message().chat().id());
                    stateManager.navigate(update, ChatState.INSERT_TAGS_TO_SEARCH);
                }
                case help_button -> stateManager.navigate(update, ChatState.HELP);
                case tag_button -> stateManager.navigate(update, ChatState.TAGS);
                case notification_button -> stateManager.navigate(update, ChatState.NOTIFICATION);
                default -> {
                    log.warn("Unknown button pressed: {}", button);
                    showUnsupportedActionMessage(update);
                }
            }
        } else {
            showUnsupportedActionMessage(update);
        }
    }
}
