package backend.academy.bot.service;

import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Validator {
    private static final String FILTER_PATTERN =
            "^(?:[a-zA-Z0-9_-]+:[a-zA-Z0-9_.,-]+(?: [a-zA-Z0-9_-]+:[a-zA-Z0-9_.,-]+)*)?$";
    private static final Pattern FILTER_REGEX = Pattern.compile(FILTER_PATTERN);

    private static final String TAG_PATTERN = "^[a-zA-Z0-9_]+(?: [a-zA-Z0-9_]+)*$";
    private static final Pattern TAG_REGEX = Pattern.compile(TAG_PATTERN);

    public static boolean isValidFilters(final String filters) {
        if (filters == null || filters.isEmpty()) {
            return false;
        }

        var matcher = FILTER_REGEX.matcher(filters);
        return matcher.matches();
    }

    public static boolean isValidTag(final String tag) {
        if (tag == null || tag.isEmpty()) {
            return false;
        }

        var matcher = TAG_REGEX.matcher(tag);
        return matcher.matches();
    }
}
