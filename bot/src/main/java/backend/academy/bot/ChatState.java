package backend.academy.bot;

import jakarta.annotation.Nullable;

public enum ChatState {
    MENU("menu-state"),
    TRACK("track-state"),
    TAGS("tags-state"),
    FILTERS("filters-state"),
    LIST("list-state"),
    UPDATES("updates-state"),
    HELP("help-state"),
    REGISTER("register-state"),
    ;


    private final String id;

    ChatState(String id) {
        this.id = id;
    }

    @Nullable
    public ChatState fromId(String id) {
        for(var state: ChatState.values()){
            if(state.id.equals(id)){
                return state;
            }
        }
        return null;
    }

}
