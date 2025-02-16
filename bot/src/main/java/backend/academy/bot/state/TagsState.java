package backend.academy.bot.state;

import backend.academy.bot.exception.TelegramApiException;
import backend.academy.bot.service.AddLinkRequestService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("tags-state")
public class TagsState extends StateImpl{
    private final AddLinkRequestService trackLinkService;

    private final Integer returningDeep = 2;
    private static final String next_button = "Далее";
    private static final String message = """
        Добавьте теги к ссылке для кластеризации (опционально):

        🔖 Введите теги через пробел (например: работа учеба проекты)
        🔍 Это поможет быстрее находить и фильтровать ваши ссылки по темам!

        Если теги не нужны, просто нажмите "Далее".
        """;

    @Autowired
    public TagsState(AddLinkRequestService trackLinkService) {
        super(ChatState.TAGS, message);
        this.trackLinkService = trackLinkService;
    }

    @Override
    public void show(long chatId) {
        log.info("Current state: {}", state);
        try {
            bot.execute(new SendMessage(chatId, message)
                .replyMarkup(keyboardFactory.getNextAndBackButtonKeyboard())
                .parseMode(ParseMode.HTML));
        } catch (TelegramApiException e) {
            log.info("Error while sending feedback request message: {}", e.getMessage());
        }
    }

    @Override
    public void handle(Update update) {
        if (update.message().text() != null) {
            var message = update.message().text();
            var chatId = update.message().chat().id();
            switch (message) {
                case next_button -> continueWithoutTags(chatId);
                case back_button -> cancelLinkInsertion(chatId);
                default -> addTagsToLink(chatId, message);
            }
        } else {
            showUnsupportedActionMessage(update);
        }
    }

    private void addTagsToLink(Long chatId, String message) {
        log.info("Adding tags {}", message);
        trackLinkService.updateLinkRequestTags(chatId, message);
        stateManager.navigate(chatId, ChatState.FILTERS);
    }

    private void cancelLinkInsertion(Long chatId) {
        log.info("Cancelling link insertion: {}", chatId);
        bot.execute(new SendMessage(chatId, "Ранее отправленная ссылка будет удалена")
            .parseMode(ParseMode.HTML));
        trackLinkService.clearLinkRequest(chatId);
        stateManager.navigate(chatId, ChatState.MENU);
    }

    private void continueWithoutTags(Long chatId) {
        log.info("Link will be tracked without tags");
        stateManager.navigate(chatId, ChatState.FILTERS);
    }
}
