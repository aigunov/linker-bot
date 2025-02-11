package backend.academy.bot.state;

import backend.academy.bot.configs.TelegramBot;
import backend.academy.bot.exception.TelegramApiException;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StateImpl implements State {

    private TelegramBot bot;

    @Override
    public void show() {
    }

    @Override
    public void handle(Update update) {
        if (update.message() != null && update.message().text() != null) {
            handleTextInput(update);
        }
    }

    //TODO: переделать
    protected void handleTextInput(Update update) {
        String message = null;
        try {
            bot.execute(new SendMessage(0, message)
                .parseMode(ParseMode.HTML));
        } catch (TelegramApiException e) {
            log.error("Error handling free text from update '{}': {}", update.toString(), e.getMessage());
        }
    }
}
}
