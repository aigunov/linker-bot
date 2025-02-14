package backend.academy.bot.controller;

import backend.academy.bot.service.BotService;
import backend.academy.bot.state.ChatState;
import backend.academy.bot.state.Handler;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;


/**
 * Класс контроллер для телеграм бота
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class TelegramBotHandler {

    private final BotService botService;

    @Handler(value = "/menu", state = ChatState.MENU)
    public void handleMenuCommand(Long chatId, Update update){
        log.info("handling command /menu from chatId: {}", chatId);
        botService.showMenu(chatId, update, ChatState.MENU);
    }

    @Handler(value = "/track", state = ChatState.TRACK)
    public void handleTrackCommand(Long chatId, Update update){
        log.info("handling command /track from chatId: {}", chatId);
    }

    @Handler(value = "/tags", state = ChatState.TAGS)
    public void handleTagsCommand(Long chatId, Update update){
        log.info("handling command /tags from chatId: {}", chatId);
    }

    @Handler(value = "/filters", state = ChatState.FILTERS)
    public void handleFiltersCommand(Long chatId, Update update){
        log.info("handling command /filters from chatId: {}", chatId);
    }

    @Handler(value = "/untrack", state = ChatState.UNTRACKED)
    public void handleUntrackCommand(Long chatId, Update update){
        log.info("handling command /untrack from chatId: {}", chatId);
    }

    @Handler(value = "/list", state = ChatState.LIST)
    public void handleListCommand(Long chatId, Update update){
        log.info("handling command /list from chatId: {}", chatId);
    }

    @Handler(value = "/help", state = ChatState.HELP)
    public void handleHelpCommand(Long chatId, Update update){
        log.info("handling command /help from chatId: {}", chatId);
    }


    /**
     * This method is required for create User entity and registry him in system
     */
    @Handler(value = "/start", state = ChatState.REGISTER)
    public void handleStartCommand(Long chatId, Update update){
        log.info("handling command /start from chatId: {}", chatId);
        botService.startUser(chatId, update, ChatState.REGISTER);
    }

}

