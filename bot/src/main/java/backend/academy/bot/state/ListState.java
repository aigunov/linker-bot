package backend.academy.bot.state;

import backend.academy.bot.exception.TelegramApiException;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import dto.ApiErrorResponse;
import dto.LinkResponse;
import dto.ListLinkResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("list-state")
@SuppressFBWarnings({"POTENTIAL_XML_INJECTION", "VA_FORMAT_STRING_USES_NEWLINE"})
public class ListState extends StateImpl {

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
            bot.execute(new SendMessage(chatId, message).parseMode(ParseMode.HTML));
        } catch (TelegramApiException e) {
            log.info("Error while sending feedback request message: {}", e.getMessage());
        }
    }

    @Override
    public void handle(Update update) {
        if (update.message().text() != null && update.message().text().equals(back_button)) {
            stateManager.navigate(update, ChatState.MENU);
        } else {
            showUnsupportedActionMessage(update);
        }
    }

    public String handleScrapperResponse(Object trackingLinks) {
        return switch (trackingLinks) {
            case ListLinkResponse links -> formatLinks((ListLinkResponse) links);
            case ApiErrorResponse error -> formatErrorResponse((ApiErrorResponse) error);
            default -> throw new TelegramApiException("Неизвестный тип");
        };
    }

    public String formatLinks(ListLinkResponse linkResponse) {
        if (linkResponse.linkResponses().isEmpty()) {
            return "Вы пока не добавили ни одну ссылку для отслеживания";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("🔗 <b>Отслеживаемые ссылки:</b>\n\n");
        for (LinkResponse link : linkResponse.linkResponses()) {
            sb.append("🌐 <b>URL:</b> ").append(link.url()).append("\n");
            if (!link.tags().isEmpty()) {
                sb.append("🏷 <b>Теги:</b> ")
                        .append(String.join(", ", link.tags()))
                        .append("\n");
            } else {
                sb.append("🏷 <i>Теги отсутствуют</i>\n");
            }
            if (!link.filters().isEmpty()) {
                sb.append("🔍 <b>Фильтры:</b> ")
                        .append(String.join(", ", link.filters()))
                        .append("\n");
            } else {
                sb.append("🔍 <i>Фильтры отсутствуют</i>\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public String formatErrorResponse(ApiErrorResponse error) {

        return String.format(
                """
                    ❗ <b>Ошибка при выполнении запроса:</b>
                    📝 <b>Описание:</b>  %s
                    📋 <b>Код ошибки:</b> %s
                    🚨 <b>Тип исключения:</b> %s
                    💥 <b>Сообщение исключения:</b> %s
                    """,
                error.description(), error.code(), error.exceptionName(), error.exceptionMessage());
    }
}
