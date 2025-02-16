package backend.academy.bot.state;

import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("filters-state")
public class FiltersState extends StateImpl{
    public FiltersState() {
        super(ChatState.FILTERS, "Добавьте фильтры к отслеживаемой ссылке(опционально):");
    }

    @Override
    public void show(long chatId) {
        log.info("Current state: {}", state);
        bot.execute(new SendMessage(chatId, message));
    }
}
