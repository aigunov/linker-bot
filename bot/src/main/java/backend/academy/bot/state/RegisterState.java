package backend.academy.bot.state;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("register-state")
public class RegisterState extends StateImpl{
    public RegisterState() {
        super(ChatState.REGISTER, "Регистрация нового пользователя в системе");
    }
}
