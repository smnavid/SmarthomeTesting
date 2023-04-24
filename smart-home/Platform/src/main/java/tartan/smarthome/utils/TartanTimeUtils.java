package tartan.smarthome.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TartanTimeUtils {
    private static DateTimeFormatter localTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public static Date localTimeToDate(LocalTime lt) {
        Instant in = lt.atDate(LocalDate.of(1970, 1, 1))
                .atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(in);
    }

    public static LocalTime dateToLocalTime(Date d) {
        Instant instant = Instant.ofEpochMilli(d.getTime());
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime();
    }

    public static boolean isBetween(LocalTime t, LocalTime start, LocalTime end) {
        if (start.isBefore(end)) {
            // e.g start is 1PM and end is 3PM
            return t.isAfter(start) && t.isBefore(end);
        } else {
            // e.g start is 9PM and end is 9AM
            return t.isAfter(start) || t.isBefore(end);
        }
    }

    public static String localTimeToString(LocalTime t) {
        return t.format(localTimeFormatter);
    }

    public static LocalTime localTimeFromString(String timeString) {
        return LocalTime.parse(timeString, localTimeFormatter);
    }
}
