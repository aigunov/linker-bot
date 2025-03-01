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
            new KeyboardButton("/track"),
            new KeyboardButton("/untrack")
        });

        rows.add(new KeyboardButton[]{
            new KeyboardButton("/list"),
            new KeyboardButton("/help")
        });
        return new ReplyKeyboardMarkup(rows.toArray(new KeyboardButton[0][]))
            .resizeKeyboard(true)
            .oneTimeKeyboard(true)
            .selective(true);
    }

    public ReplyKeyboardMarkup getBackStateKeyboard() {
        List<KeyboardButton[]> rows = new ArrayList<>();

        rows.add(new KeyboardButton[]{
            new KeyboardButton("Назад")
        });

        return new ReplyKeyboardMarkup(rows.toArray(new KeyboardButton[0][]))
            .resizeKeyboard(true)
            .oneTimeKeyboard(true)
            .selective(true);
    }

    public ReplyKeyboardMarkup getNextAndBackButtonKeyboard() {
        List<KeyboardButton[]> rows = new ArrayList<>();

        rows.add(new KeyboardButton[]{
            new KeyboardButton("Назад"),
            new KeyboardButton("Далее")
        });

        return new ReplyKeyboardMarkup(rows.toArray(new KeyboardButton[0][]))
            .resizeKeyboard(true)
            .oneTimeKeyboard(true)
            .selective(true);
    }
}
