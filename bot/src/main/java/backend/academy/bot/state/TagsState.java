package backend.academy.bot.state;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("tags-state")
public class TagsState extends StateImpl{
    private final ChatState state = ChatState.TAGS;
}
