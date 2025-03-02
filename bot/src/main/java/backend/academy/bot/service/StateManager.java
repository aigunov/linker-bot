package backend.academy.bot.service;

import backend.academy.bot.state.ChatState;
import backend.academy.bot.state.RegisterState;
import backend.academy.bot.state.State;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StateManager {
    private final ChatStateService stateService;

    public <S extends State> void navigate(Update update, ChatState chatState) {
        if (chatState.equals(ChatState.REGISTER)) {
            var register = (RegisterState) ChatState.REGISTER.getState();
            register.show(update);
        } else {
            var chatId = update.message().chat().id();
            stateService.setChatState(String.valueOf(chatId), chatState);
            var state = chatState.getState();
            log.info("Navigating state from {} to {} on chat {}", chatState.getState(), state, chatId);
            state.show(chatId);
        }
    }
}
