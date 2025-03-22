package backend.academy.bot.state;

import backend.academy.bot.exception.TelegramApiException;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import dto.ApiErrorResponse;
import dto.LinkResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("untracked-state")
public class UntrackedState extends StateImpl {
    private static final String message = "Введите ссылку чтобы отменить отслеживание";

    public UntrackedState() {
        super(ChatState.UNTRACKED, message);
    }

    @Override
    public void show(long chatId) {
        log.info("Current state: UntrackedState");
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
            var chatId = update.message().chat().id();
            if (back_button.equals(message)) {
                stateManager.navigate(update, ChatState.MENU);
            } else {
                untrackLink(message, chatId);
                stateManager.navigate(update, ChatState.MENU);
            }
        } else {
            showUnsupportedActionMessage(update);
        }
    }

    private void untrackLink(String message, Long chatId) {
        if (isValidURL(message)) {
            log.info("Link {} will be untracked in chat {}", message, chatId);
            var response = botService.commitLinkUntrack(chatId, message);
            switch (response) {
                case LinkResponse link -> {
                    bot.execute(
                            new SendMessage(chatId, String.format("Ссылка %s была отменена пользователем", link.url()))
                                    .parseMode(ParseMode.HTML));
                }
                case ApiErrorResponse error -> {
                    bot.execute(new SendMessage(
                                    chatId,
                                    String.format(
                                            "У нас не получилось отменить отслеживание ссылки %s по причине: %s",
                                            message, error.description()))
                            .parseMode(ParseMode.HTML));
                }
                default -> throw new TelegramApiException("Неизвестный тип");
            }
        } else {
            bot.execute(new SendMessage(chatId, "Неверный формат ссылки.").parseMode(ParseMode.HTML));
            log.error("Unsupported link format {} inserted into chat {}", message, chatId);
        }
    }
}
