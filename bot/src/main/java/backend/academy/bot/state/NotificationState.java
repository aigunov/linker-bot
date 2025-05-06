package backend.academy.bot.state;

import backend.academy.bot.exception.TelegramApiException;
import backend.academy.bot.service.Validator;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import dto.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationState extends StateImpl {
    private static final String byDefault = "сразу";
    private static final String wrongTimeFormatMessage = """
        Неверный формат времени.
        Должен быть hh:mm.
        Попробуйте снова или вернитесь назад""";
    private static final String cancelText = """
        Пользователь решил не изменять настройки нового времени дайджеста.
        Предыдущее значение останется в силе.
        """;
    private static final String message = """
        Введите время в которое вы бы хотели получать ежедневный дайджест
        По умолчанию обновления отображаются сразу как только о них станет известно.
        Вы можете задать время для ежедневного отображения всех обновлений по вашим ссылкам сразу.

        Введите время в формате hh:mm по 24 часовому формату
        Введите или нажмите кнопку "сразу", чтобы выбрать вариант с моментальным отображением обновлений.
        Нажмите "Назад" если не хотите изменить текущий формат времени.
        """;


    public NotificationState() {
        super(ChatState.NOTIFICATION, message);
    }

    @Override
    public void show(long chatId) {
        log.info("Current state: NotificationState");
        try {
            bot.execute(new SendMessage(chatId, message)
                .replyMarkup(keyboardFactory.getNotificationTimeSetKeyboard())
                .parseMode(ParseMode.HTML));
        } catch (TelegramApiException e) {
            log.info("Error while sending feedback request message: {}", e.getMessage());
        }
    }

    @Override
    public void handle(Update update) {
        if (update.message().text() == null) {
            showUnsupportedActionMessage(update);
            return;
        }

        var chatId = update.message().chat().id();
        var text = update.message().text();

        if (back_button.equals(text)) {
            stateManager.navigate(update, ChatState.MENU);
            return;
        }

        changeTime(text, chatId);
        stateManager.navigate(update, ChatState.MENU);
    }

    private void changeTime(String message, Long chatId) {
        message = message.trim().toLowerCase();
        if (byDefault.equals(message)) {
            bot.execute(new SendMessage(chatId, cancelText).parseMode(ParseMode.HTML));
            log.info("TgChat: {} cancel digest time changing", chatId);
            return;
        }

        var parsedTimeOpt = Validator.parseTime(message);
        if (parsedTimeOpt.isEmpty()) {
            validatorChecker(wrongTimeFormatMessage, chatId);
            return;
        }
        var time = parsedTimeOpt.get();
        log.info("TgChat: {} change digest time", chatId);
        var response = botService.changeDigestTime(time);
        if (response instanceof ApiErrorResponse error) {
            String failChangeTimeMessage = String.format("""
                К сожалению у нас не получилось поменять время дайджеста.
                Попробуйте позже.
                Причина: %s""", error.description());
            bot.execute(new SendMessage(
                chatId,
                failChangeTimeMessage)
                .parseMode(ParseMode.HTML));
        } else {
            String successfulChangeTimeMessage = String.format("""
                Время для дайджеста теперь обновлено.
                Теперь список обновлений будет отображаться вам каждый день в %s
                """, time);
            bot.execute(new SendMessage(chatId, successfulChangeTimeMessage).parseMode(ParseMode.HTML));
        }
    }

}
