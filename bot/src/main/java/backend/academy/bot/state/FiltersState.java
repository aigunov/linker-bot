package backend.academy.bot.state;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("filters-state")
public class FiltersState extends StateImpl{
    public FiltersState() {
        super(ChatState.FILTERS, "Добавьте фильтры к отслеживаемой ссылке(опционально):");
    }
}
