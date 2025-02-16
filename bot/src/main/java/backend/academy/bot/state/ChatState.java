package backend.academy.bot.state;

import backend.academy.bot.service.StateFactory;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString(of = {"id"})
@Getter
@RequiredArgsConstructor
public enum ChatState {
    MENU("menu-state"),
    TRACK("track-state"),
    TAGS("tags-state"),
    FILTERS("filters-state"),
    UNTRACKED("untracked-state"),
    LIST("list-state"),
    HELP("help-state"),
    REGISTER("register-state"),
    NONE("none-state")
    ;

    private final String id;

    private static StateFactory stateFactory;

    public static void setStateFactory(StateFactory factory) {
        stateFactory = factory;
    }

    public State getState() {
        return stateFactory.getState(this);
    }

    public static ChatState fromId(String id) {
        return Arrays.stream(ChatState.values())
            .filter(state -> state.id.equals(id))
            .findFirst()
            .orElse(null);
    }
}
