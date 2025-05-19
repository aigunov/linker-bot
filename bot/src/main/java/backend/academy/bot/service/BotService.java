package backend.academy.bot.service;

import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.configs.TelegramBot;
import backend.academy.bot.exception.FailedIncomingUpdatesHandleException;
import backend.academy.bot.exception.TelegramApiException;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import dto.AddLinkRequest;
import dto.ApiErrorResponse;
import dto.Digest;
import dto.GetLinksRequest;
import dto.LinkUpdate;
import dto.NotificationTimeRequest;
import dto.RegisterChatRequest;
import dto.RemoveLinkRequest;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotService {

    private final ScrapperClient client;
    private final AddLinkRequestService addLinkRequestService;
    private final ListRequestService listRequestService;
    private TelegramBot telegramBot;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Autowired
    public void setTelegramBot(@Lazy TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    // todo: имя кэша конфигурируемое
    @NotNull
    @Cacheable(value = "trackedLinks", key = "#chatId", unless = "#result instanceof T(dto.ApiErrorResponse)")
    public Object getAllLinks(Long chatId) {
        try {
            log.info("Fetching tracked links for chatId: {}", chatId);
            var requestBody = listRequestService.getListRequest(chatId);
            listRequestService.clearLinkRequest(chatId);
            var responseEntity = client.getAllLinks(
                    chatId, requestBody == null ? GetLinksRequest.builder().build() : requestBody);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully received tracked links for chatId: {}", chatId);
            } else {
                log.warn(
                        "Failed to fetch tracked links for chatId: {}. Status code: {}",
                        chatId,
                        responseEntity.getStatusCode());
            }
            return responseEntity.getBody();
        } catch (Exception ex) {
            log.error("Unexpected error while fetching tracked links for chatId: {}", chatId, ex);
            throw new TelegramApiException("Произошла ошибка взаимодействия с сервисом Scrapper. Попробуйте позже.");
        }
    }

    @NotNull
    public Object getTags(long chatId) {
        try {
            log.info("Fetching tags for chatId: {}", chatId);
            var responseEntity = client.getAllTags(chatId);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully received tags for chatId: {}", chatId);
            } else {
                log.warn(
                        "Failed to fetch tags for chatId: {}. Status code: {}", chatId, responseEntity.getStatusCode());
            }
            return responseEntity.getBody();
        } catch (Exception ex) {
            log.error("Unexpected error while fetching tracked links for chatId: {}", chatId, ex);
            throw new TelegramApiException("Произошла ошибка взаимодействия с сервисом Scrapper. Попробуйте позже.");
        }
    }

    @NotNull
    public Object chatRegistration(Update update) {
        var message = update.message();
        var tgUser = message.from();
        var chatId = update.message().chat().id();
        var username = tgUser.username() != null ? "@" + tgUser.username() : tgUser.firstName();

        var registerChat =
                RegisterChatRequest.builder().chatId(chatId).name(username).build();

        try {
            var response = client.registerChat(registerChat);
            return response.getBody();
        } catch (Exception ex) {
            log.info("Bot service, register user. Пук, крях, чото странное мы больше не работаем: {}", ex.getMessage());
            return ApiErrorResponse.builder()
                    .exceptionMessage(ex.getMessage())
                    .stacktrace(convertStackTraceToList(ex.getStackTrace()))
                    .code(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                    .description(ex.getClass().getSimpleName())
                    .build();
        }
    }

    @NotNull
    @CacheEvict(value = "trackedLinks", key = "#chatId")
    public Object commitLinkTracking(Long chatId) {
        try {
            AddLinkRequest linkRequest = addLinkRequestService.getLinkRequest(chatId);
            addLinkRequestService.clearLinkRequest(chatId);
            if (linkRequest == null) {
                throw new IllegalArgumentException("Link request is null");
            }

            var responseEntity = client.addTrackedLink(chatId, linkRequest);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully track link for chatId: {}", chatId);
            } else {
                log.warn(
                        "Failed to track link for chatId: {}. Status code: {}", chatId, responseEntity.getStatusCode());
            }
            return responseEntity.getBody();
        } catch (Exception ex) {
            log.error("Unexpected error while fetching tracked links for chatId: {}", chatId, ex);
            throw new TelegramApiException("Произошла ошибка взаимодействия с сервисом Scrapper. Попробуйте позже.");
        }
    }

    @NotNull
    public Object changeDigestTime(Long chatId, LocalTime time) {
        try {
            var responseEntity = client.setNotificationTime(chatId, new NotificationTimeRequest(time));

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully changed time for chatId: {}", chatId);
            } else {
                log.warn(
                        "Failed to change time for chatId: {}. Status code: {}",
                        chatId,
                        responseEntity.getStatusCode());
            }
            return responseEntity.getBody();
        } catch (Exception ex) {
            log.error("Unexpected error while fetching tracked links for chatId: {}", chatId, ex);
            throw new TelegramApiException("Произошла ошибка взаимодействия с сервисом Scrapper. Попробуйте позже.");
        }
    }

    @NotNull
    @CacheEvict(value = "trackedLinks", key = "#chatId")
    public Object commitLinkUntrack(Long chatId, String message) {
        try {
            var responseEntity = client.removeTrackedLink(chatId, new RemoveLinkRequest(message));
            log.info("Remove link: {}", responseEntity);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully untrack link for chatId: {}", chatId);
            } else {
                log.warn(
                        "Failed to untrack tracked link for chatId: {}. Status code: {}",
                        chatId,
                        responseEntity.getStatusCode());
            }
            return responseEntity.getBody();
        } catch (Exception ex) {
            log.error("Unexpected error while fetching tracked links for chatId: {}", chatId, ex);
            throw new TelegramApiException("Произошла ошибка взаимодействия с сервисом Scrapper. Попробуйте позже.");
        }
    }

    public void processUpdate(LinkUpdate update) {
        for (Long chatId : update.tgChatIds()) {
            executorService.submit(() -> sendUpdateToChat(update, chatId));
        }
    }

    private void sendUpdateToChat(LinkUpdate update, Long chatId) {
        var message = formatUpdateMessage(update);
        try {
            telegramBot.execute(new SendMessage(chatId, message));
            log.info("Update sent to chat {}: {}", chatId, update);
        } catch (TelegramApiException e) {
            log.error("Failed to send update to chat {}: {}", chatId, update, e);
            throw new FailedIncomingUpdatesHandleException("Failed to send update to chat " + chatId, e);
        }
    }

    private String formatUpdateMessage(LinkUpdate update) {
        return "Link updated:\n" + "URL: " + update.url() + "\n" + "Description: " + update.message();
    }

    private List<String> convertStackTraceToList(StackTraceElement[] stackTrace) {
        return Arrays.stream(stackTrace).map(StackTraceElement::toString).collect(Collectors.toList());
    }

    public void processDigests(Digest digest) {
        try {
            telegramBot.execute(new SendMessage(digest.tgId(), formatDigestMessage(digest)).parseMode(ParseMode.HTML));
            log.info("Update sent to chat {}", digest.tgId());
        } catch (TelegramApiException e) {
            throw new FailedIncomingUpdatesHandleException("Failed to send update to chat " + digest.tgId(), e);
        }
    }

    private String formatDigestMessage(Digest digest) {
        if (digest.updates() == null || digest.updates().isEmpty()) {
            return "Сегодня не было обновлений по вашим отслеживаемым ссылкам.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📰 Обновления за сегодня:\n\n");

        int count = 1;
        for (LinkUpdate update : digest.updates()) {
            sb.append(count++)
                    .append(". 🔗 <b>")
                    .append(update.url())
                    .append("</b>\n")
                    .append(update.message())
                    .append("\n\n");
        }

        return sb.toString().trim();
    }
}
