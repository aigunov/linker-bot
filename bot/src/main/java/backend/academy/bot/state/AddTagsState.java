package backend.academy.bot.state;

import backend.academy.bot.service.AddLinkRequestService;
import backend.academy.bot.service.Validator;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("add-tags-state")
public class AddTagsState extends StateImpl {
    private final AddLinkRequestService trackLinkService;

    private static final String next_button = "Далее";
    private static final String message =
            """
        Добавьте теги к ссылке для кластеризации (опционально):

        🔖 Введите теги через пробел (например: работа учеба проекты)
        🔍 Это поможет быстрее находить и фильтровать ваши ссылки по темам!

        Если теги не нужны, просто нажмите "Далее".
        """;

    @Autowired
    public AddTagsState(AddLinkRequestService trackLinkService) {
        super(ChatState.ADD_TAGS, message);
        this.trackLinkService = trackLinkService;
    }

    @Override
    public void handle(Update update) {
        if (update.message().text() != null) {
            switch (update.message().text()) {
                case next_button -> continueWithoutTags(update);
                case back_button -> cancelLinkInsertion(update);
                default -> addTagsToLink(update, update.message().text());
            }
        } else {
            showUnsupportedActionMessage(update);
        }
    }

    private void addTagsToLink(Update update, String message) {
        if (!Validator.isValidTag(message)) {
            var errorMessage = String.format("Используются запрещенные символы в тегах: %s", message);
            log.error("Ошибка валидации тегов: {}", errorMessage);
            validatorChecker(errorMessage, update.message().chat().id());
            cancelLinkInsertion(update);
            backToMenu(update);
            return;
        }
        var chatId = update.message().chat().id();
        log.info("Adding tags {}", message);
        trackLinkService.updateLinkRequestTags(chatId, message);
        stateManager.navigate(update, ChatState.ADD_FILTERS);
    }

    private void cancelLinkInsertion(Update update) {
        var chatId = update.message().chat().id();
        log.info("Cancelling link insertion: {}", chatId);
        bot.execute(new SendMessage(chatId, "Ранее отправленная ссылка будет удалена").parseMode(ParseMode.HTML));
        trackLinkService.clearLinkRequest(chatId);
        stateManager.navigate(update, ChatState.MENU);
    }

    private void continueWithoutTags(Update update) {
        log.info("Link will be tracked without tags");
        stateManager.navigate(update, ChatState.ADD_FILTERS);
    }
}
