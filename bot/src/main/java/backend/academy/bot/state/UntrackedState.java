package backend.academy.bot.state;

import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("untracked-state")
public class UntrackedState extends StateImpl{
    public UntrackedState() {
        super(ChatState.UNTRACKED, "Введите ссылку чтобы отменить отслеживание");
    }

    @Override
    public void show(long chatId) {
        log.info("Current state: {}", state);
        bot.execute(new SendMessage(chatId, message));
    }
}
