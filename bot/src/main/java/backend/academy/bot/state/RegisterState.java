package backend.academy.bot.state;

import backend.academy.bot.exception.TelegramApiException;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import dto.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("register-state")
public class RegisterState extends StateImpl{
    private static final String message = "Регистрация нового пользователя в системе";

    public RegisterState() {
        super(ChatState.REGISTER, message);
    }

    @Override
    public void show(long chatId) {
    }

    public void show(Update update) {
        log.info("Current state: {}", state);
        try {
            bot.execute(new SendMessage(update.message().chat().id(), message)
                .parseMode(ParseMode.HTML));
            handle(update);
        } catch (TelegramApiException e) {
            log.info("Error while sending feedback request message: {}", e.getMessage());
        }
    }

    @Override
    public void handle(Update update) {
        log.info("Registration new user begin");
        var message = botService.chatRegistration(update);
        if (message instanceof ApiErrorResponse error){
            log.error(error.toString());
            bot.execute(new SendMessage(update.message().chat().id(), error.description())
                .parseMode(ParseMode.HTML));
        }else{
            bot.execute(new SendMessage(update.message().chat().id(), (String) message)
                .parseMode(ParseMode.HTML));
            stateManager.navigate(update, ChatState.MENU);
        }
    }
}
