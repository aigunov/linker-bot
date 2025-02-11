package backend.academy.bot.service;

import backend.academy.bot.model.LinkUpdate;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotService {

    private final StateManager stateManager;

    //TODO: метод для отображения уведомлений в бот тг=
    public void processUpdate(LinkUpdate update) {
    }


    public void showMenu(Long chatId, Update update){}

    public void untrackLink(Long chatId, Update update){}

    public void helpUser(Long chatId, Update update){}

    public void startUser(Long chatId, Update update){}

    public void insertLinkToTrack(Long chatId, Update update){}

    public void insertTagsToLink(Long chatId, Update update){}

    public void insertFiltersToLink(Long chatId, Update update){}


    public void trackLink(){}
}
