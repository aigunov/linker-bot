package backend.academy.bot.state;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("menu-state")
public class MenuState extends StateImpl{

    public MenuState() {
        super(ChatState.MENU, "Главное меню");
    }

}
