package backend.academy.bot.state;

import backend.academy.bot.exception.TelegramApiException;
import backend.academy.bot.service.KeyboardFactory;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import dto.ApiErrorResponse;
import dto.GetTagsResponse;
import dto.ListLinkResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component("tags-state")
public class TagsState extends StateImpl{
    public TagsState() {
        super(ChatState.TAGS, "Список используемых тегов:");
    }

    @Override
    public void show(long chatId) {
        log.info("Current state: {}", state);
        try {
            bot.execute(new SendMessage(chatId, message).parseMode(ParseMode.HTML));
            var message = handleScrapperResponse(botService.getTags(chatId));
            bot.execute(new SendMessage(chatId, message)
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

    public String handleScrapperResponse(Object trackingLinks) {
        return switch (trackingLinks) {
            case GetTagsResponse links -> formatTags((GetTagsResponse) links);
            case ApiErrorResponse error -> formatErrorResponse((ApiErrorResponse) error);
            default -> throw new TelegramApiException("Неизвестный тип");
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
                ❗ <b>Ошибка при выполнении запроса:</b>
                📝 <b>Описание:</b>  %s
                📋 <b>Код ошибки:</b> %s
                🚨 <b>Тип исключения:</b> %s
                💥 <b>Сообщение исключения:</b> %s
                """,
            error.description(), error.code(), error.exceptionName(), error.exceptionMessage());
    }
}
