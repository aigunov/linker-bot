package backend.academy.bot.configs;

import backend.academy.bot.exception.TelegramApiException;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.AbstractSendRequest;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor()
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
        log.info("TelegramBot initialized");
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

}
