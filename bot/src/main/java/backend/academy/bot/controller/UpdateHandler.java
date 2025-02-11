package backend.academy.bot.controller;

import backend.academy.bot.state.ChatState;
import backend.academy.bot.state.Handler;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class UpdateHandler {

    private final Map<String, Method> commandHandlers = new HashMap<>();
    private final Map<ChatState, Method> stateHandlers = new HashMap<>();
    private final TelegramBotHandler handler;

    @Autowired
    public UpdateHandler(TelegramBotHandler handler) {
        this.handler = handler;
        scanHandlers();
    }

    private void scanHandlers(){
        for (Method method : handler.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Handler.class)) {
                Handler annotation = method.getAnnotation(Handler.class);
                if (!annotation.value().isEmpty()){
                    commandHandlers.put(annotation.value(), method);
                }

            }
        }
    }

    public void handleUpdate(Update update) {
        if (update.message() != null){
            var message = update.message();
            var text = message.text();
            var chatId = message.chat().id();

            if (commandHandlers.containsKey(text)) {
                invokeHandler(commandHandlers.get(text), chatId, update);
                return;
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
}
