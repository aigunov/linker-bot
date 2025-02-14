package backend.academy.bot.service;

import backend.academy.bot.state.ChatState;
import backend.academy.bot.state.State;
import backend.academy.bot.state.StateImpl;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StateFactory {
    private final Map<ChatState, State> stateMap = new HashMap<>();
    private final Set<? extends StateImpl> states;


    @Autowired
    public StateFactory(Set<? extends StateImpl> states) {
        this.states = states;
    }

    @PostConstruct
    public void init() {
        for (State state : states) {
            if (state instanceof StateImpl stateImpl) {
                stateMap.put(stateImpl.state(), stateImpl);
            }
        }
    }

    public State getState(ChatState chatState) {
        return stateMap.get(chatState);
    }
}
