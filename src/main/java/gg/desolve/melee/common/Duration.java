package gg.desolve.melee.common;

import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class Duration {

    private final long value;

    public Duration(long value) {
        this.value = value;
    }

    public static Duration fromString(String source) {
        if (source.equalsIgnoreCase("perm") || source.equalsIgnoreCase("permanent")) {
            return new Duration(Integer.MAX_VALUE);
        }

        long time = 0L;
        Matcher matcher = Pattern.compile("(\\d+)([smhdwMy])").matcher(source);

        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            String type = matcher.group(2);

            switch (type) {
                case "s":
                    time += value;
                    break;
                case "m":
                    time += value * 60;
                    break;
                case "h":
                    time += value * 60 * 60;
                    break;
                case "d":
                    time += value * 60 * 60 * 24;
                    break;
                case "w":
                    time += value * 60 * 60 * 24 * 7;
                    break;
                case "M":
                    time += value * 60 * 60 * 24 * 30;
                    break;
                case "y":
                    time += value * 60 * 60 * 24 * 365;
                    break;
            }
        }

        return time == 0 ? null : new Duration(time * 1000);
    }
}
