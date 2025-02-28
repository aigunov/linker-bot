package backend.academy.bot.state;

import backend.academy.bot.exception.TelegramApiException;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("help-state")
public class HelpState extends StateImpl{

    private final Integer returningDeep = 1;
    private static final String infoMenu = """
        Телеграм бот Link-Tracker поддерживает следующие команды:

        <b><i>/help</i></b> - выводит список поддерживаемых ботом команд

        <b><i>/link</i></b> - выводит список уведомлений по отслеживаемым ссылкам

        <b><i>/track</i></b> - добавить новую ссылку для отслеживания

        <b><i>/untrack</i></b> - снять ссылки отслеживание с уже добавленной ссылки
        """;

    public HelpState() {
        super(ChatState.HELP, "Список поддерживаемых команд:");
    }

    @Override
    public void show(long chatId) {
        log.info("Current state: {}", state);
        try {
            bot.execute(new SendMessage(chatId, message)
                .parseMode(ParseMode.HTML));
            bot.execute(new SendMessage(chatId, infoMenu)
                .parseMode(ParseMode.HTML)
                .replyMarkup(keyboardFactory.getBackStateKeyboard())
            );
        } catch (TelegramApiException e) {
            log.info("Error while sending feedback request message: {}", e.getMessage());
        }
    }

    @Override
    public void handle(Update update) {
        if (update.message().text() != null &&
            update.message().text().equals(back_button)) {
            var chatId = update.message().chat().id();
            stateManager.navigate(update, ChatState.MENU);
        } else {
            showUnsupportedActionMessage(update);
        }
    }
}
