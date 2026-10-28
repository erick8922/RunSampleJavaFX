package util;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

/** Small helpers for showing database values nicely in the UI. */
public class Formats {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    public static String date(Date value) {
        return value == null ? "-" : value.toLocalDate().format(DATE);
    }

    public static String dateTime(Timestamp value) {
        return value == null ? "-" : value.toLocalDateTime().format(DATE_TIME);
    }

    /** Returns "-" for null or empty text. */
    public static String text(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    /** "pending" -> "Pending". Returns "-" for null or empty text. */
    public static String capitalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        String t = value.trim();
        return t.substring(0, 1).toUpperCase() + t.substring(1).toLowerCase();
    }
}
