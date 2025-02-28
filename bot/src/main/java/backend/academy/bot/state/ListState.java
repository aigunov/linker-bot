package backend.academy.bot.state;

import backend.academy.bot.exception.TelegramApiException;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import dto.ApiErrorResponse;
import dto.LinkResponse;
import dto.ListLinkResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("list-state")
public class ListState extends StateImpl{

    private final Integer returningDeep = 1;
    private static final String message = "Список отслеживаемых ссылок: ";

    public ListState() {
        super(ChatState.LIST, message);
    }

    @Override
    public void show(long chatId) {
        log.info("Current state: {}", state);
        try {
            bot.execute(new SendMessage(chatId, message)
                .replyMarkup(keyboardFactory.getBackStateKeyboard())
                .parseMode(ParseMode.HTML));
            var message = handleScrapperResponse(botService.getTrackingLinks(chatId));
            bot.execute(new SendMessage(chatId, message)
                .parseMode(ParseMode.HTML));
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

    private String handleScrapperResponse(Object trackingLinks) {
        return switch (trackingLinks) {
            case ListLinkResponse links -> formatLinks((ListLinkResponse) links);
            case ApiErrorResponse error -> formatErrorResponse((ApiErrorResponse) error);
            default -> throw new TelegramApiException("Неизвестный тип");
        };
    }

    private String formatLinks(ListLinkResponse linkResponse) {
        StringBuilder sb = new StringBuilder();
        sb.append("🔗 <b>Отслеживаемые ссылки:</b>\n\n");
        for (LinkResponse link : linkResponse.linkResponses()) {
            sb.append("🌐 <b>URL:</b> ").append(link.url()).append("\n");
            if (!link.tags().isEmpty()) {
                sb.append("🏷 <b>Теги:</b> ").append(String.join(", ", link.tags())).append("\n");
            } else {
                sb.append("🏷 <i>Теги отсутствуют</i>\n");
            }
            if (!link.filters().isEmpty()) {
                sb.append("🔍 <b>Фильтры:</b> ").append(String.join(", ", link.filters())).append("\n");
            } else {
                sb.append("🔍 <i>Фильтры отсутствуют</i>\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String formatErrorResponse(ApiErrorResponse error) {
        StringBuilder sb = new StringBuilder();
        sb.append("❗ <b>Ошибка при выполнении запроса:</b>\n\n");

        sb.append("📝 <b>Описание:</b> ").append(error.description()).append("\n");

        sb.append("📋 <b>Код ошибки:</b> ").append(error.code()).append("\n");

        sb.append("🚨 <b>Тип исключения:</b> ").append(error.exceptionName()).append("\n");

        sb.append("💥 <b>Сообщение исключения:</b> ").append(error.exceptionMessage()).append("\n");

        sb.append("\n🔍 <b>Stacktrace:</b>\n");

        return sb.toString();
    }
}
