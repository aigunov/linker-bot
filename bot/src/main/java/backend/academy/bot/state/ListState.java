package backend.academy.bot.state;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("list-state")
public class ListState extends StateImpl{
    private final ChatState state = ChatState.LIST;
}
