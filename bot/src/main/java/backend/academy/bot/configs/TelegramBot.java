package backend.academy.bot.configs;

import backend.academy.bot.exception.TelegramApiException;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component("telegramBot")
public class TelegramBot {

    private final UpdatesListener updatesListener;
    private final BotConfig botProperties;
    private com.pengrad.telegrambot.TelegramBot bot;

    @EventListener(ApplicationStartedEvent.class)
    private void initialize() {
        Objects.requireNonNull(botProperties.telegramToken(), "token is null");
        bot = new com.pengrad.telegrambot.TelegramBot(botProperties.telegramToken());
        bot.setUpdatesListener(updatesListener);
        registerCommands();
        log.info("TelegramBot initialized and commands registered");
    }

    public <T extends BaseRequest<T, R>, R extends BaseResponse> R execute(BaseRequest<T, R> request) {
        R response = bot.execute(request);
        if (response.errorCode() == 403 || response.errorCode() == 429) {
            log.warn(response.description());
            return response;
        }
        if (!response.isOk()) {
            throw new TelegramApiException(String.format("%d %s", response.errorCode(), response.description()));
        }
        return response;
    }

    private void registerCommands() {
        BotCommand[] commands = {
            new BotCommand("/start", "Регистрация"),
            new BotCommand("/menu", "Перейти в главное меню"),
            new BotCommand("/help", "Получить справку"),
            new BotCommand("/track", "Добавить ссылку для отслеживания"),
            new BotCommand("/untrack", "Удалить ссылку из отслеживания"),
            new BotCommand("/list", "Получить список отслеживаемых ссылок")
        };

        com.pengrad.telegrambot.request.SetMyCommands request =
                new com.pengrad.telegrambot.request.SetMyCommands(commands);
        bot.execute(request);
    }
}
