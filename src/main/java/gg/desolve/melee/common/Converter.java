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

}
