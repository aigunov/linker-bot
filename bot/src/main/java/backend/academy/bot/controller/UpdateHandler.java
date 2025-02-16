package backend.academy.bot.controller;

import backend.academy.bot.service.ChatStateService;
import backend.academy.bot.service.StateManager;
import backend.academy.bot.state.ChatState;
import backend.academy.bot.state.State;
import com.pengrad.telegrambot.model.Update;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UpdateHandler {

    private final ChatStateService chatStateService;
    private final Set<? extends State> states;
    private final StateManager stateManager;

    @Autowired
    public UpdateHandler(ChatStateService chatStateService, Set<? extends State> states, StateManager stateManager) {
        this.chatStateService = chatStateService;
        this.states = states;
        this.stateManager = stateManager;
    }


    public void handleUpdate(Update update) {
        if (update.message() != null && update.message().text() != null) {

            var chatId = update.message().chat().id();
            if (update.message().text().equals("/start")) {
                stateManager.navigate(update.message().chat().id(), ChatState.REGISTER);
                return;
            }
            ChatState currentState = chatStateService.peekLastChatState(String.valueOf(chatId));

            for (var state : states) {
                if (currentState.getState().equals(state)) {
                    state.handle(update);
                }
            }
        }
    }


}
