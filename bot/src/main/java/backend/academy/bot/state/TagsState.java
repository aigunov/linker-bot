package backend.academy.bot.state;

import backend.academy.bot.exception.TelegramApiException;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import dto.ApiErrorResponse;
import dto.GetTagsResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@SuppressWarnings(value = {"POTENTIAL_XML_INJECTION", "VA_FORMAT_STRING_USES_NEWLINE"})
@SuppressFBWarnings(value = {"POTENTIAL_XML_INJECTION", "VA_FORMAT_STRING_USES_NEWLINE"})
@Slf4j
@Component("tags-state")
public class TagsState extends StateImpl {
    public TagsState() {
        super(ChatState.TAGS, "Список используемых тегов:");
    }

    @Override
    public void show(long chatId) {
        log.info("Current state: {}", state);
        try {
            bot.execute(new SendMessage(chatId, message).parseMode(ParseMode.HTML));
            bot.execute(new SendMessage(chatId, handleTagsList(botService.getTags(chatId)))
                    .replyMarkup(keyboardFactory.getBackStateKeyboard())
                    .parseMode(ParseMode.HTML));
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

    public String handleTagsList(Object chatTags) {
        return switch (chatTags) {
            case GetTagsResponse links -> formatTags(links);
            case ApiErrorResponse error -> formatErrorResponse(error);
            default -> throw new TelegramApiException("Неизвестный тип ответа для списка тегов.");
        };
    }

    private String formatTags(GetTagsResponse tags) {
        if (tags.tags().isEmpty()) {
            return "🏷 <i>Теги отсутствуют</i>\nВы пока не добавили ни одного тега.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<b>Используемы теги:</b>\n\n");
        for (String tag : tags.tags()) {
            sb.append("🏷 <i>").append(tag).append("</i>\n");
        }
        return sb.toString();
    }

    public String formatErrorResponse(ApiErrorResponse error) {
        return String.format(
                """
                ❗ <b>Ошибка при выполнении запроса:</b>%n
                📝 <b>Описание:</b>  %s%n
                📋 <b>Код ошибки:</b> %s%n
                🚨 <b>Тип исключения:</b> %s%n
                💥 <b>Сообщение исключения:</b> %s
                """,
                error.description(), error.code(), error.exceptionName(), error.exceptionMessage());
    }
}
