package gg.desolve.melee.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

public class Converter {

    public static String generateId() {
        String randomId = UUID.randomUUID().toString()
                .replaceAll("-", "")
                .toLowerCase();
        return randomId.substring(0, Math.min(10, randomId.length()));
    }

    public static String millisToDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/London"));
        return sdf.format(new Date(millis));
    }

    public static long millisToHours(long millis) {
        return millis / (1000 * 60 * 60);
    }

    public static long millisToSeconds(long millis) {
        return millis / 1000;
    }

    public static String millisToTime(long millis) {
        millis += 1L;

        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        long days = hours / 24L;
        long weeks = days / 7L;
        long months = weeks / 4L;
        long years = months / 12L;

        if (years > 0) {
            return years + " year" + (years == 1 ? "" : "s") +
                    (months % 12 > 0 ? ", " + (months % 12) + " month" + (months % 12 == 1 ? "" : "s") : "");
        } else if (months > 0) {
            return months + " month" + (months == 1 ? "" : "s") +
                    (weeks % 4 > 0 ? ", " + (weeks % 4) + " week" + (weeks % 4 == 1 ? "" : "s") : "");
        } else if (weeks > 0) {
            return weeks + " week" + (weeks == 1 ? "" : "s") +
                    (days % 7 > 0 ? ", " + (days % 7) + " day" + (days % 7 == 1 ? "" : "s") : "");
        } else if (days > 0) {
            return days + " day" + (days == 1 ? "" : "s") +
                    (hours % 24 > 0 ? ", " + (hours % 24) + " hour" + (hours % 24 == 1 ? "" : "s") : "");
        } else if (hours > 0) {
            return hours + " hour" + (hours == 1 ? "" : "s") +
                    (minutes % 60 > 0 ? ", " + (minutes % 60) + " minute" + (minutes % 60 == 1 ? "" : "s") : "");
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes == 1 ? "" : "s");
        } else {
            return seconds + " second" + (seconds == 1 ? "" : "s");
        }
    }

}
