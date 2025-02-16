package backend.academy.bot.state;

import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("tags-state")
public class TagsState extends StateImpl{
    public TagsState() {
        super(ChatState.TAGS, "Добавьте теги к ссылке для кластеризации(опционально):");
    }

    @Override
    public void show(long chatId) {
        log.info("Current state: {}", state);
        bot.execute(new SendMessage(chatId, message));
    }
}
