package backend.academy.bot.state;

import backend.academy.bot.exception.TelegramApiException;
import backend.academy.bot.service.AddLinkRequestService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("track-state")
public class TrackState extends StateImpl {
    private final AddLinkRequestService trackLinkService;
    private static final String message = "Введите ссылку для отслеживания";

    @Autowired
    public TrackState(AddLinkRequestService trackLinkService) {
        super(ChatState.TRACK, message);
        this.trackLinkService = trackLinkService;
    }

    @Override
    public void show(long chatId) {
        log.info("Current state: {}", state);
        try {
            bot.execute(new SendMessage(chatId, message)
                    .replyMarkup(keyboardFactory.getBackStateKeyboard())
                    .parseMode(ParseMode.HTML));
        } catch (TelegramApiException e) {
            log.info("Error while sending feedback request message: {}", e.getMessage());
        }
    }

    @Override
    public void handle(Update update) {
        if (update.message().text() != null) {
            var message = update.message().text();
            if (back_button.equals(message)) {
                stateManager.navigate(update, ChatState.MENU);
            } else {
                insertLinkHandle(update, message);
            }
        } else {
            showUnsupportedActionMessage(update);
        }
    }

    private void insertLinkHandle(Update update, String message) {
        var chatId = update.message().chat().id();
        if (isValidURL(message)) {
            log.info("Link {} inserted into chat {}", message, chatId);
            trackLinkService.createLinkRequest(chatId, message);
            stateManager.navigate(update, ChatState.TAGS);
        } else {
            bot.execute(new SendMessage(chatId, "Неверный формат ссылки.").parseMode(ParseMode.HTML));
            log.error("Unsupported link format {} inserted into chat {}", message, chatId);
        }
    }
}
