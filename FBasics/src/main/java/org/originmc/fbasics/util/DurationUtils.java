package org.originmc.fbasics.util;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.*;

public final class DurationUtils {

    /**
     * Get a worded duration from milliseconds.
     *
     * @param millis amount of milliseconds.
     * @return worded duration format.
     */
    public static String format(long millis) {
        List<String> parts = new ArrayList<>();
        long days = millis / DAYS.toMillis(1);
        if (days > 0) {
            millis -= DAYS.toMillis(days);
            parts.add(days + (days != 1 ? " days" : " day"));
        }

        long hours = millis / HOURS.toMillis(1);
        if (hours > 0) {
            millis -= HOURS.toMillis(hours);
            parts.add(hours + (hours != 1 ? " hours" : " hour"));
        }

        long minutes = millis / MINUTES.toMillis(1);
        if (minutes > 0) {
            millis -= MINUTES.toMillis(minutes);
            parts.add(minutes + (minutes != 1 ? " minutes" : " minute"));
        }

        long seconds = millis / SECONDS.toMillis(1);
        if (parts.isEmpty() || seconds != 0) {
            parts.add(seconds + (seconds != 1 ? " seconds" : " second"));
        }

        String formatted = StringUtils.join(parts, ", ");
        if (formatted.contains(", ")) {
            int index = formatted.lastIndexOf(", ");
            StringBuilder builder = new StringBuilder(formatted);
            formatted = builder.replace(index, index + 2, " and ").toString();
        }

        return formatted;
    }

    /**
     * Used for calculating the time remaining of a cooldown.
     *
     * @param end when the cooldown ends.
     * @return the time remaining in milliseconds.
     */
    public static long calculateRemaining(long end) {
        return end - System.currentTimeMillis();
    }

}
