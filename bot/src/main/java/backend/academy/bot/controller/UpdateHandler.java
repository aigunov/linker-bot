package backend.academy.bot.controller;

import backend.academy.bot.service.BotService;
import backend.academy.bot.service.ChatStateService;
import backend.academy.bot.state.ChatState;
import backend.academy.bot.state.Handler;
import com.pengrad.telegrambot.model.Update;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UpdateHandler {

    private final Map<String, Method> commandHandlers = new HashMap<>();
    private final Map<ChatState, Method> stateHandlers = new HashMap<>();
    private final TelegramBotHandler handler;
    private final ChatStateService chatStateService;
    private final BotService botService;

    @Autowired
    public UpdateHandler(TelegramBotHandler handler, ChatStateService chatStateService, BotService botService) {
        this.handler = handler;
        this.chatStateService = chatStateService;
        this.botService = botService;
        scanHandlers();
    }

    private void scanHandlers(){
        for (Method method : handler.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Handler.class)) {
                Handler annotation = method.getAnnotation(Handler.class);

                try {
                    if (!annotation.value().isEmpty()) {
                        commandHandlers.put(annotation.value(), method);
                    }
                    if (annotation.state() != ChatState.NONE) {
                        stateHandlers.put(annotation.state(), method);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to instantiate handler: ", e);
                }
            }
        }
    }

    public void handleUpdate(Update update) {
        if (update.message() != null && update.message().text() != null) {

            var chatId = update.message().chat().id();
            String messageText = update.message().text();
            ChatState currentState = chatStateService.peekLastChatState(String.valueOf(chatId));

            switch (messageText) {
                case "Назад":
                    botService.backButtonCLick(chatId);
                    break;
                case "Далее":
                    botService.nextButtonClick(chatId, update, currentState);
                    break;
                default:
                    if (messageText.equals("/start")) {
                        invokeHandler(commandHandlers.get("/start"), chatId, update);
                        return;
                    }

                    getCommandHandler(messageText).ifPresent(method -> {
                        invokeHandler(method, chatId, update);
                        return;
                    });

                    getStateHandler(currentState).ifPresent(method -> {
                        invokeHandler(method, chatId, update);
                        return;
                    });
            }
        }
    }

    private void invokeHandler(Method method, Long chatId, Update update) {
        try {
            method.invoke(handler, chatId, update);
        } catch (Exception e) {
            log.error("Ошибка обработки команды: {}", e.getMessage(), e);
        }
    }


    public Optional<Method> getCommandHandler(String command) {
        return Optional.ofNullable(commandHandlers.get(command));
    }

    public Optional<Method> getStateHandler(ChatState state) {
        return Optional.ofNullable(stateHandlers.get(state));
    }

}
