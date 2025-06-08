package backend.academy.bot.state;

import backend.academy.bot.service.StateFactory;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@SuppressWarnings({"UnnecessaryFullyQualifiedName"})
@ToString(of = {"id"})
@Getter
@RequiredArgsConstructor
public enum ChatState {
    REGISTER("register-state"),

    MENU("menu-state"),

    TRACK("track-state"),
    ADD_TAGS("add-tags-state"),
    ADD_FILTERS("filters-state"),

    UNTRACKED("untracked-state"),

    LIST("list-state"),
    INSERT_TAGS_TO_SEARCH("insert-tags-to-search-state"),
    INSERT_FILTERS_TO_SEARCH("insert-filters-to-search-state"),

    HELP("help-state"),

    TAGS("tags-state"),

    NOTIFICATION("notification-state");

    private final String id;

    private static StateFactory stateFactory;

    public static void setStateFactory(StateFactory factory) {
        stateFactory = factory;
    }

    public State getState() {
        return stateFactory.getState(this);
    }

    public static ChatState fromId(String id) {
        return Arrays.stream(values())
                .filter(state -> state.id.equals(id))
                .findFirst()
                .orElse(null);
    }
}
