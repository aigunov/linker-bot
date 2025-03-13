package backend.academy.scrapper.data.enums;

import lombok.Getter;

@Getter
public enum LinkingServices {
    GITHUB("https://api.github.com/repos"),
    STACKOVERFLOW("https://api.stackexchange.com/2.3/questions"),
    ;
    private final String APi_URL;

    LinkingServices(String aPiUrl) {
        APi_URL = aPiUrl;
    }
}
