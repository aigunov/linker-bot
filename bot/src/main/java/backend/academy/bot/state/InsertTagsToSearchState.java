package backend.academy.bot.state;

import backend.academy.bot.exception.TelegramApiException;
import backend.academy.bot.service.ListRequestService;
import backend.academy.bot.service.Validator;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component("insert-tags-to-search-state")
public class InsertTagsToSearchState extends StateImpl {
    private final ListRequestService listRequestService;

    private static final String next_button = "Далее";
    private static final String message =
        """
    Добавьте теги для поиска (опционально):

    🔖 Введите теги через пробел (например: работа учеба проекты)
    🔍 Так мы сможем вывести только те ссылки, которые соответствуют переданным тегам!

    Если теги не нужны, просто нажмите "Далее".
    """;

    @Autowired
    public InsertTagsToSearchState(ListRequestService listRequestService) {
        super(ChatState.INSERT_TAGS_TO_SEARCH, message);
        this.listRequestService = listRequestService;
    }

    @Override
    public void show(long chatId) {
        log.info("Current state: {}", state);
        try {
            bot.execute(new SendMessage(chatId, message)
                .replyMarkup(keyboardFactory.getNextAndBackButtonKeyboard())
                .parseMode(ParseMode.HTML));
//            listRequestService.createListRequest(chatId);
        } catch (TelegramApiException e) {
            log.info("Error while sending feedback request message: {}", e.getMessage());
        }
    }

    @Override
    public void handle(Update update) {
        if (update.message().text() != null) {
            var message = update.message().text();
            switch (message) {
                case next_button -> continueWithoutTags(update);
                case back_button -> cancelListRequest(update);
                default -> addTagsToLink(update, message);
            }
        } else {
            showUnsupportedActionMessage(update);
        }
    }

    private void cancelListRequest(Update update) {
        var chatId = update.message().chat().id();
        log.info("Cancelling link insertion: {}", chatId);
        bot.execute(new SendMessage(chatId, "Отмена запроса списка ссылок").parseMode(ParseMode.HTML));
        listRequestService.clearLinkRequest(chatId);
        stateManager.navigate(update, ChatState.MENU);
    }

    private void continueWithoutTags(Update update) {
        log.info("Link will be tracked without tags");
        stateManager.navigate(update, ChatState.LIST);
    }

    private void addTagsToLink(Update update, String message) {
        if (!Validator.isValidTag(message)){
            var errorMessage = String.format("Используются запрещенные символы в тегах: %s", message);
            log.error(errorMessage);
            validatorChecker(errorMessage, update.message().chat().id());
            cancelListRequest(update);
            backToMenu(update);
            return;
        }
        var chatId = update.message().chat().id();
        log.info("Adding tags {}", message);
        listRequestService.createListRequest(chatId, message);
        stateManager.navigate(update, ChatState.LIST);
    }
}
