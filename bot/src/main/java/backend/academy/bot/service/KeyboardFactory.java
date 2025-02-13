package backend.academy.bot.service;

import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class KeyboardFactory {

    public ReplyKeyboardMarkup getMainMenuKeyboard() {
        List<KeyboardButton[]> rows = new ArrayList<>();

        rows.add(new KeyboardButton[]{
            new KeyboardButton("Добавить ссылку"),
            new KeyboardButton("Отменить отслеживание")
        });

        rows.add(new KeyboardButton[]{
            new KeyboardButton("Уведомления"),
            new KeyboardButton("Список команды")
        });
        return new ReplyKeyboardMarkup(rows.toArray(new KeyboardButton[0][]))
            .resizeKeyboard(true)
            .oneTimeKeyboard(true)
            .selective(true);
    }
}
