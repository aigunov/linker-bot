package backend.academy.bot.state;

import backend.academy.bot.exception.TelegramApiException;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
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
        log.info("Current state: {}", state);
        try {
            bot.execute(new SendMessage(chatId, message)
                .parseMode(ParseMode.HTML));
        } catch (TelegramApiException e) {
            log.info("Error while sending feedback request message: {}", e.getMessage());
        }
    }

    @Override
    public void handle(Update update) {
        log.info("Registration new user");
        botService.chatRegistration(update);
        stateManager.navigate(update.message().chat().id(), ChatState.MENU);
    }
}
