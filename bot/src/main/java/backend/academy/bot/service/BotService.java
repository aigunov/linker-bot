package backend.academy.bot.service;

import backend.academy.bot.configs.TelegramBot;
import backend.academy.bot.model.AddLinkRequest;
import backend.academy.bot.model.LinkUpdate;
import backend.academy.bot.model.RegisterChatRequest;
import backend.academy.bot.state.ChatState;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotService {

    //    private final ScrapperClient client;
    private final ChatStateService stateService;
    private final KeyboardFactory keyboardFactory;
    private TelegramBot telegramBot;

    @Autowired
    public void setTelegramBot(@Lazy TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    //TODO: метод для отображения уведомлений в бот тг=
    public void processUpdate(LinkUpdate update) {
    }


    public void menuStateHandle(Long chatId, Update update, ChatState state) {
        if (update.message().text() != null) {
            stateService.setChatState(String.valueOf(chatId), state);
            telegramBot.execute(new SendMessage(chatId, "Добро пожаловать в TG-Bot-Link-Tracker")
                    .replyMarkup(keyboardFactory.getMainMenuKeyboard())
                    .parseMode(ParseMode.HTML));
            state.getState().show(chatId);
        } else {
            telegramBot.execute(new SendMessage(chatId, "Извините мне не знакома такая операция"));
        }
    }

    private void listUpdates(Long chatId, Update update, ChatState chatState) {

    }

    public void untrackLink(Long chatId, Update update, ChatState state) {
    }

    public void helpUser(Long chatId, Update update, ChatState state) {
        stateService.setChatState(String.valueOf(chatId), ChatState.HELP);
    }

    public void chatRegistration(Long chatId, Update update, ChatState state) {
        var message = update.message();
        var tgUser = message.from();
        var username = tgUser.username() != null ?  "@" + tgUser.username() : tgUser.firstName();

        state.getState().show(chatId);

        var registerChat = RegisterChatRequest.builder()
            .id(chatId)
            .name(username)
            .build();

//        try {
//            var response = client.registerChat(registerChat);
        var response = new ResponseEntity<RegisterChatRequest>(registerChat, HttpStatusCode.valueOf(200));
        if (response.getStatusCode().is2xxSuccessful()) {
             telegramBot.execute(new SendMessage(chatId, "Вы успешно зарегистрированы"));

        } else {
            telegramBot.execute(new SendMessage(chatId, "Непредвиденная ошибка при запросе к сервису. Попробуйте позже"));
        }
//        } catch (Exception ex) {
//            log.info("Bot service, register user. Пук, крях, чото странное мы больше не работаем: {}", ex.getMessage());
//            telegramBot.execute(new SendMessage(chatId, "Пук, крях, чото странное мы больше не работаем"));
//        }

    }

    public void insertLinkToTrack(Long chatId, Update update, ChatState state){
//        stateService.setChatState(String.valueOf(chatId), state);
//        if (update.message().text() != null) {
//            var link = update.message().text();
//            state.getState().show(chatId);
//            var linkRequest = AddLinkRequest.builder()
//                .uri(link)
//                .build();
//            stateService.setChatState(String.valueOf(chatId), ChatState.TAGS);
//        }else {
//            telegramBot.execute(new SendMessage(chatId, "Непредвиденная ошибка при запросе к сервису. Попробуйте позже"));
//        }

        String link = update.message().text();

        stateService.createLinkRequest(chatId, link);

        telegramBot.execute(new SendMessage(chatId, "Введите теги (через пробел) или нажмите 'Далее' для пропуска:")
            .replyMarkup(keyboardFactory.getTagsKeyboard())
            .parseMode(ParseMode.HTML));

        stateService.setChatState(String.valueOf(chatId), ChatState.TAGS);
    }

    public void insertTagsToLink(Long chatId, Update update, ChatState state){
        String tags = update.message().text();

        // Сохраняем теги в linkRequest пользователя
        stateService.updateLinkRequestTags(chatId, tags);

        telegramBot.execute(new SendMessage(chatId, "Введите фильтры (через пробел) или нажмите 'Далее' для пропуска:")
            .replyMarkup(keyboardFactory.getFiltersKeyboard())
            .parseMode(ParseMode.HTML));

        // Устанавливаем состояние FILTERS
        stateService.setChatState(String.valueOf(chatId), ChatState.FILTERS);
    }

    public void insertFiltersToLink(Long chatId, Update update, ChatState state){
        String filters = update.message().text();

        // Сохраняем фильтры в linkRequest пользователя
        stateService.updateLinkRequestFilters(chatId, filters);

        // Подтверждаем отслеживание ссылки
        commitLinkTracking(chatId);
    }


    public void commitLinkTracking(Long chatId){
        // Получаем ссылку пользователя
        AddLinkRequest linkRequest = stateService.getLinkRequest(chatId);
        if (linkRequest == null) {
            telegramBot.execute(new SendMessage(chatId, "Произошла ошибка. Попробуйте снова."));
            return;
        }

        // TODO: отправить запрос в Scrapper и сохранить ссылку
        telegramBot.execute(new SendMessage(chatId, "Ссылка успешно добавлена в отслеживание!"));

        // Очищаем состояние и linkRequest
        stateService.clearLinkRequest(chatId);
        stateService.setChatState(String.valueOf(chatId), ChatState.MENU);
    }


    public void backButtonCLick(Long chatId){
        stateService.setChatState(String.valueOf(chatId), ChatState.MENU);
        telegramBot.execute(new SendMessage(chatId, "Вы вернулись в меню.")
            .replyMarkup(keyboardFactory.getMainMenuKeyboard()));
    }

    public void nextButtonClick(Long chatId, Update update, ChatState state){
        if (state == ChatState.TAGS) {
            insertFiltersToLink(chatId, update, ChatState.FILTERS);
        } else if (state == ChatState.FILTERS) {
            commitLinkTracking(chatId);
        }
    }
}
