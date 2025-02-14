package backend.academy.bot.service;

import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.configs.TelegramBot;
import backend.academy.bot.model.LinkUpdate;
import backend.academy.bot.model.RegisterChatRequest;
import backend.academy.bot.state.ChatState;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotService {

    private final ScrapperClient client;
    private final ChatStateService stateService;

    private TelegramBot telegramBot;

    @Autowired
    public void setTelegramBot(@Lazy TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    //TODO: метод для отображения уведомлений в бот тг=
    public void processUpdate(LinkUpdate update) {
    }


    public void showMenu(Long chatId, Update update, ChatState menu){

    }

    public void untrackLink(Long chatId, Update update){}

    public void helpUser(Long chatId, Update update){}

    public void startUser(Long chatId, Update update, ChatState menu){
        var message = update.message();
        var tgUser = message.from();
        var username = tgUser.username() != null ?  "@" + tgUser.username() : tgUser.firstName();



        var registerChat = RegisterChatRequest.builder()
            .id(chatId)
            .name(username)
            .build();

        var response = client.registerChat(registerChat);
        if (response.getStatusCode().is2xxSuccessful()) {
            telegramBot.execute(new SendMessage(chatId, "Вы успешно зарегистрированы"));
            stateService.setChatStates(String.valueOf(chatId), ChatState.MENU);
        }

    }

    public void insertLinkToTrack(Long chatId, Update update){}

    public void insertTagsToLink(Long chatId, Update update){}

    public void insertFiltersToLink(Long chatId, Update update){}


    public void trackLink(){}
}
