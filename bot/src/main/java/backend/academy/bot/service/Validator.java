package backend.academy.bot.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class Validator {
    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm");
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

    public static Optional<LocalTime> parseTime(String timeStr) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm");
        try {
            return Optional.of(LocalTime.parse(timeStr.trim(), format));
        } catch (DateTimeParseException e) {
            log.warn("Invalid time format: {}", timeStr);
            return Optional.empty();
        }
    }
}
