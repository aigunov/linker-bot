package backend.academy.bot.controller;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component("botListener")
public class BotListener implements UpdatesListener {

    private final UpdateHandler updateHandler;

    @Override
    public int process(List<Update> list) {
        log.info("Processing updates: {}", list.size());
        for (Update update : list) {
            updateHandler.handleUpdate(update);
        }
        return CONFIRMED_UPDATES_ALL;
    }
}
