package backend.academy.bot.state;

import com.pengrad.telegrambot.model.Update;

public interface State {

    void show();

    void handle(Update update);
}
