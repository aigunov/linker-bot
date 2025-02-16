package backend.academy.bot.state;

import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("list-state")
public class ListState extends StateImpl{

    public ListState() {
        super(ChatState.LIST, "Список обновлений уведомлений");
    }

    @Override
    public void show(long chatId) {
        log.info("Current state: {}", state);
        bot.execute(new SendMessage(chatId, message));
    }
}
