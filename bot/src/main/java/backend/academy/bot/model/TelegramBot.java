package backend.academy.bot.model;

import backend.academy.bot.configs.BotConfig;
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
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Component("telegramBot")
public class TelegramBot {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile boolean canSend = true;


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
        if (request instanceof AbstractSendRequest) {
            acquireRequest();
        }
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


    public void removeKeyboard(Long chatId, int messageId) {
        try {
            execute(new EditMessageReplyMarkup(chatId, messageId));
        } catch (TelegramApiException e) {
            log.warn("Unable to remove keyboard: {}", e.getMessage());
        }
    }

    private void acquireRequest() {
        long start = System.currentTimeMillis();
        while (!canSend) {
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        canSend = false; // Запрещаем отправку, пока не пройдет 33 мс

        long elapsed = System.currentTimeMillis() - start;
        if (elapsed > 1000) {
            log.warn("Too many sending requests detected (sleep {} ms)", elapsed);
        }
    }

}
