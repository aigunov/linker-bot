package backend.academy.bot.state;

import backend.academy.bot.ChatState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("untracked-state")
public class UntrackedState {
    private final ChatState state = ChatState.UNTRACKED;
}
