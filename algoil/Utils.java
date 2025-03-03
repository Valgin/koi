package ru.effectivegroup.client.algoil;

import java.time.LocalTime;
import java.util.Objects;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/Utils.class */
public class Utils {
    public static boolean IsNullOrEmpty(String string) {
        return string == null || string.isEmpty() || string.trim().isEmpty();
    }

    public static LocalTime convertToLocalTime(String time) {
        int hours = ParseHours(time);
        int minutes = ParseMinutes(time);
        return LocalTime.of(hours, minutes);
    }

    public static String validateTimeString(String value, boolean canBeZero) {
        if (canBeZero && Objects.equals(value, "0")) {
            return "0";
        }
        int hours = ParseHours(value);
        int minutes = ParseMinutes(value);
        int hours2 = Math.min(Math.max(hours, 10), 23);
        String minutesValue = FormatMinutesValue(Math.min(Math.max(minutes, 0), 59));
        return String.valueOf(hours2).concat(minutesValue);
    }

    public static String FormatMinutesValue(int minutes) {
        if (minutes < 10) {
            return "0" + minutes;
        }
        return String.valueOf(minutes);
    }

    public static String LocalTimeToReadable(LocalTime time, boolean isSecondsRequired) {
        if (isSecondsRequired) {
            return String.format("%d:%s:00", Integer.valueOf(time.getHour()), FormatMinutesValue(time.getMinute()));
        }
        return String.format("%d:%s", Integer.valueOf(time.getHour()), FormatMinutesValue(time.getMinute()));
    }

    private static int ParseHours(String timeString) {
        return Integer.parseInt(timeString.substring(0, 2));
    }

    private static int ParseMinutes(String timeString) {
        return Integer.parseInt(timeString.substring(2, 4));
    }
}
