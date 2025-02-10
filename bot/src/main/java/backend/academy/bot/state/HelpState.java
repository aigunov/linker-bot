package backend.academy.bot.state;

import backend.academy.bot.ChatState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("help-state")
public class HelpState extends StateImpl{
    private final ChatState state = ChatState.HELP;
}
