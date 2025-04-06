package backend.academy.bot.state;

import backend.academy.bot.exception.TelegramApiException;
import backend.academy.bot.service.AddLinkRequestService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import dto.ApiErrorResponse;
import dto.LinkResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("add-filters-state")
public class AddFiltersState extends StateImpl {
    private final AddLinkRequestService trackLinkService;

    private static final String next_button = "Далее";
    private static final String message =
            """
        Добавьте фильтры к отслеживаемой ссылке (опционально):

        Фильтры помогают вам сузить область отслеживания и получать только те обновления,
        которые действительно важны. Например, вы можете настроить фильтры так,
        чтобы получать только комментарии, изменения в коде или новые релизы.

        Введите фильтры через пробел (например: <i>user:admin</i> <i>type:comment</i>),
        или нажмите <b>"Далее"</b>, чтобы пропустить этот шаг.
        """;

    @Autowired
    public AddFiltersState(AddLinkRequestService trackLinkService) {
        super(ChatState.ADD_FILTERS, message);
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
        try {
            if (update.message().text() != null) {
                var message = update.message().text();
                var chatId = update.message().chat().id();
                switch (message) {
                    case back_button -> cancelLinkInsertion(update);
                    case next_button -> {
                        log.info("Link will be tracked without filters");
                        commitTracking(chatId);
                        backToMenu(update);
                    }
                    default -> {
                        addFiltersToLink(update, message);
                        commitTracking(chatId);
                        backToMenu(update);
                    }
                }
            } else {
                showUnsupportedActionMessage(update);
            }
        } catch (Exception ex) {
            bot.execute(new SendMessage(update.message().chat().id(), "Непредвиденная ошибка" + ex.getMessage()));
        }
    }

    private void commitTracking(Long chatId) {
        log.info("Link will be committed: {}", chatId);
        var response = botService.commitLinkTracking(chatId);
        switch (response) {
            case LinkResponse link -> {
                bot.execute(new SendMessage(chatId, "Ссылка успешно добавлена в отслеживание!"));
            }
            case ApiErrorResponse error -> {
                bot.execute(new SendMessage(
                                chatId,
                                String.format(
                                        "У нас не получилось добавить к отслеживанию ссылку по причине: %s",
                                        error.description()))
                        .parseMode(ParseMode.HTML));
            }
            default -> {
                log.error("Undefined response type: {}", response.getClass().getSimpleName());
                throw new TelegramApiException("Неизвестный тип");
            }
        }
    }

    private void addFiltersToLink(Update update, String message) {
        var chatId = update.message().chat().id();
        log.info("Adding filters {}", message);
        trackLinkService.updateLinkRequestFilters(chatId, message);
    }

    private void backToMenu(Update update){
        stateManager.navigate(update, ChatState.MENU);
    }

    private void cancelLinkInsertion(Update update) {
        var chatId = update.message().chat().id();
        log.info("Cancelling link insertion: {}", chatId);
        bot.execute(new SendMessage(chatId, "Ранее отправленная ссылка будет удалена").parseMode(ParseMode.HTML));
        trackLinkService.clearLinkRequest(chatId);
        stateManager.navigate(update, ChatState.MENU);
    }
}
