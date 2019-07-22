package io.github.wztlei.wathub.utils;

import android.content.Context;
import android.content.res.Resources;

import io.github.wztlei.wathub.R;

import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Instant;
import org.joda.time.Minutes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final DateFormat sDateFormat =
            new SimpleDateFormat("MMM dd, yyyy", Locale.CANADA);
    private static final DateFormat sDateTimeFormat =
            new SimpleDateFormat("hh:mm aa, MMM dd", Locale.CANADA);

    private DateUtils() {
        throw new UnsupportedOperationException();
    }

    public static String formatDateTime(final Date date) {
        return sDateTimeFormat.format(date);
    }

    public static String formatDate(final Date date) {
        return sDateFormat.format(date);
    }

    public static String formatDate(final Context context, final Date date) {
        return android.text.format.DateFormat.getMediumDateFormat(context).format(date);
    }

    public static String formatTime(final Context context, final Date date) {
        return android.text.format.DateFormat.getTimeFormat(context).format(date);
    }

    public static String getTimeDifference(final Resources resources, final long millis) {
        final long now = System.currentTimeMillis();
        final Instant instantNow = new Instant(now);
        final Instant instantMillis = new Instant(millis);

        final int days = Math.abs(Days.daysBetween(instantMillis, instantNow).getDays());
        final int hours = Math.abs(Hours.hoursBetween(instantMillis, instantNow).getHours());
        final int minutes = Math.abs(Minutes.minutesBetween(instantMillis, instantNow).getMinutes());

        boolean isFormattedAsNow = false;
        final String prettyDuration;
        if (days != 0) {
            prettyDuration = days + " " + resources.getQuantityString(R.plurals.duration_days, days);

        } else if (hours != 0) {
            prettyDuration = hours + " " + resources.getQuantityString(R.plurals.duration_hours, hours);

        } else if (minutes != 0) {
            prettyDuration = minutes + " " + resources.getQuantityString(R.plurals.duration_minutes,
                    minutes);

        } else {
            prettyDuration = resources.getString(R.string.duration_instant);
            isFormattedAsNow = true;
        }

        final String fullDuration;
        if (isFormattedAsNow) {
            fullDuration = prettyDuration;
        } else if (instantMillis.isBefore(instantNow)) {
            fullDuration = prettyDuration + " " + resources.getString(R.string.duration_ago);
        } else {
            fullDuration = resources.getString(R.string.duration_until) + " " + prettyDuration;
        }

        return fullDuration;
    }

    /**
     * Returns a string that is a time in 12h format from a time in 24h format.
     *
     * @param hour  the hour of the time in 24h format
     * @param min   the minute of the time
     * @return      the time in 12h format
     */
    public static String format12hTime(int hour, int min) {
        // Ensure that the hour and minute are within the bounds of a valid time
        if (hour < 0 || hour > 23 || min < 0 || min > 60) {
            throw new IllegalArgumentException();
        }

        // Format the minute by adding 0s as padding if necessary
        String minStr = formatMin(min);

        // Determine the hour of the day to add AM or PM or subtract 12 from the hour accordingly
        if (hour == 0) {
            return String.format("12:%s AM", minStr);
        } else if (hour <= 11) {
            String hourStr = Integer.toString(hour);
            return String.format("%s:%s AM", hourStr, minStr);
        } else if (hour == 12) {
            return String.format("12:%s PM", minStr);
        } else {
            String hourStr = Integer.toString(hour - 12);
            return String.format("%s:%s PM", hourStr, minStr);
        }
    }

    /**
     * Returns a string with an integer representing a minute so it has at least 2 digits,
     * by adding 0s to the left as padding if necessary.
     *
     * @param min the minute of a time
     * @return          the minute formatted with 0s as left padding
     */
    private static String formatMin(int min) {
        return String.format(Locale.CANADA, "%02d", min);
    }
}
