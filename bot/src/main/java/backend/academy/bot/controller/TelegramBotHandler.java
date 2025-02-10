package backend.academy.bot.controller;

import backend.academy.bot.Handler;
import com.pengrad.telegrambot.model.Update;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;


/**
 * Класс контроллер для телеграм бота
 */
@Slf4j
@Controller
public class TelegramBotHandler {

    @Handler("/menu")
    public void handleMenuCommand(Long chatId, Update update){}

    @Handler("/track")
    public void handleTrackCommand(Long chatId, Update update){}

    @Handler("/tags")
    public void handleTagsCommand(Long chatId, Update update){}

    @Handler("/filters")
    public void handleFiltersCommand(Long chatId, Update update){}

    @Handler("/untrack")
    public void handleUntrackCommand(Long chatId, Update update){    }

    @Handler("/list")
    public void handleUpdatesCommand(Long chatId, Update update){}

    @Handler("/help")
    public void handleHelpCommand(Long chatId, Update update){}

}
