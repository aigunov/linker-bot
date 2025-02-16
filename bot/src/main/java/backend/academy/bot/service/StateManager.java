package backend.academy.bot.service;

import backend.academy.bot.state.ChatState;
import backend.academy.bot.state.State;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StateManager {
    private final ChatStateService stateService;


    public <S extends State> void navigate(Long chatId, ChatState chatState) {
        stateService.setChatState(String.valueOf(chatId), chatState);
        var state = chatState.getState();
        log.info("Navigating state from {} to {} on chat {}", chatState.getState(), state, chatId);
        state.show(chatId);
    }

    //TODO: Реализовать переход с одного стейта в предыдущий и удаление текущего из очереди
    public void navigateReturning(Long chatId, ChatState chatState, final Integer deep) {

    }
}
