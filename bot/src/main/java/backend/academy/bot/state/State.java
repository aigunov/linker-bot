package backend.academy.bot.state;

import com.pengrad.telegrambot.model.Update;

public interface State {

    void show(long chatId);

    void handle(Update update);
}
