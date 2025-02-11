package backend.academy.bot.controller;

import backend.academy.bot.state.Handler;
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
    public void handleMenuCommand(Long chatId, Update update){
        log.info("handling command /menu from chatId: {}", chatId);
    }

    @Handler("/track")
    public void handleTrackCommand(Long chatId, Update update){
        log.info("handling command /track from chatId: {}", chatId);
    }

    @Handler("/tags")
    public void handleTagsCommand(Long chatId, Update update){
        log.info("handling command /tags from chatId: {}", chatId);
    }

    @Handler("/filters")
    public void handleFiltersCommand(Long chatId, Update update){
        log.info("handling command /filters from chatId: {}", chatId);
    }

    @Handler("/untrack")
    public void handleUntrackCommand(Long chatId, Update update){
        log.info("handling command /untrack from chatId: {}", chatId);
    }

    @Handler("/list")
    public void handleListCommand(Long chatId, Update update){
        log.info("handling command /list from chatId: {}", chatId);
    }

    @Handler("/help")
    public void handleHelpCommand(Long chatId, Update update){
        log.info("handling command /help from chatId: {}", chatId);
    }


    /**
     * This method is required for create User entity and registry him in system
     */
    @Handler("/start")
    public void handleStartCommand(Long chatId, Update update){
        log.info("handling command /start from chatId: {}", chatId);

    }

}

