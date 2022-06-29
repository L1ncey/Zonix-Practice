package us.zonix.practice.util;

import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

public class TimeUtil
{
    private static Pattern timePattern;
    
    public static String convertToFormat(final long delay) {
        if (delay == 0L) {
            return "None";
        }
        return formatTime(delay - System.currentTimeMillis());
    }
    
    public static String removeTimePattern(final String input) {
        return TimeUtil.timePattern.matcher(input).replaceFirst("").trim();
    }
    
    public static long parseDateDiff(final String time, final boolean future) throws Exception {
        final Matcher m = TimeUtil.timePattern.matcher(time);
        int years = 0;
        int months = 0;
        int weeks = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        boolean found = false;
        while (m.find()) {
            if (m.group() != null && !m.group().isEmpty()) {
                for (int c = 0; c < m.groupCount(); ++c) {
                    if (m.group(c) != null && !m.group(c).isEmpty()) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    continue;
                }
                if (m.group(1) != null && !m.group(1).isEmpty()) {
                    years = Integer.parseInt(m.group(1));
                }
                if (m.group(2) != null && !m.group(2).isEmpty()) {
                    months = Integer.parseInt(m.group(2));
                }
                if (m.group(3) != null && !m.group(3).isEmpty()) {
                    weeks = Integer.parseInt(m.group(3));
                }
                if (m.group(4) != null && !m.group(4).isEmpty()) {
                    days = Integer.parseInt(m.group(4));
                }
                if (m.group(5) != null && !m.group(5).isEmpty()) {
                    hours = Integer.parseInt(m.group(5));
                }
                if (m.group(6) != null && !m.group(6).isEmpty()) {
                    minutes = Integer.parseInt(m.group(6));
                }
                if (m.group(7) != null && !m.group(7).isEmpty()) {
                    seconds = Integer.parseInt(m.group(7));
                    break;
                }
                break;
            }
        }
        if (!found) {
            throw new Exception("Illegal Date");
        }
        final GregorianCalendar var13 = new GregorianCalendar();
        if (years > 0) {
            var13.add(1, years * (future ? 1 : -1));
        }
        if (months > 0) {
            var13.add(2, months * (future ? 1 : -1));
        }
        if (weeks > 0) {
            var13.add(3, weeks * (future ? 1 : -1));
        }
        if (days > 0) {
            var13.add(5, days * (future ? 1 : -1));
        }
        if (hours > 0) {
            var13.add(11, hours * (future ? 1 : -1));
        }
        if (minutes > 0) {
            var13.add(12, minutes * (future ? 1 : -1));
        }
        if (seconds > 0) {
            var13.add(13, seconds * (future ? 1 : -1));
        }
        final GregorianCalendar max = new GregorianCalendar();
        max.add(1, 10);
        return var13.after(max) ? max.getTimeInMillis() : var13.getTimeInMillis();
    }
    
    static int dateDiff(final int type, final Calendar fromDate, final Calendar toDate, final boolean future) {
        int diff = 0;
        long savedDate = fromDate.getTimeInMillis();
        while ((future && !fromDate.after(toDate)) || (!future && !fromDate.before(toDate))) {
            savedDate = fromDate.getTimeInMillis();
            fromDate.add(type, future ? 1 : -1);
            ++diff;
        }
        --diff;
        fromDate.setTimeInMillis(savedDate);
        return diff;
    }
    
    public static String formatDateDiff(final long date) {
        final GregorianCalendar c = new GregorianCalendar();
        c.setTimeInMillis(date);
        final GregorianCalendar now = new GregorianCalendar();
        return formatDateDiff(now, c);
    }
    
    public static String formatDateDiff(final Calendar fromDate, final Calendar toDate) {
        boolean future = false;
        if (toDate.equals(fromDate)) {
            return "now";
        }
        if (toDate.after(fromDate)) {
            future = true;
        }
        final StringBuilder sb = new StringBuilder();
        final int[] types = { 1, 2, 5, 11, 12, 13 };
        final String[] names = { "year", "years", "month", "months", "day", "days", "hour", "hours", "minute", "minutes", "second", "seconds" };
        for (int accuracy = 0, i = 0; i < types.length && accuracy <= 2; ++i) {
            final int diff = dateDiff(types[i], fromDate, toDate, future);
            if (diff > 0) {
                ++accuracy;
                sb.append(" ").append(diff).append(" ").append(names[i * 2 + ((diff > 1) ? 1 : 0)]);
            }
        }
        return (sb.length() == 0) ? "now" : sb.toString().trim();
    }
    
    public static String formatSimplifiedDateDiff(final long date) {
        final GregorianCalendar c = new GregorianCalendar();
        c.setTimeInMillis(date);
        final GregorianCalendar now = new GregorianCalendar();
        return formatSimplifiedDateDiff(now, c);
    }
    
    public static String formatSimplifiedDateDiff(final Calendar fromDate, final Calendar toDate) {
        boolean future = false;
        if (toDate.equals(fromDate)) {
            return "now";
        }
        if (toDate.after(fromDate)) {
            future = true;
        }
        final StringBuilder sb = new StringBuilder();
        final int[] types = { 1, 2, 5, 11, 12, 13 };
        final String[] names = { "y", "y", "m", "m", "d", "d", "h", "h", "m", "m", "s", "s" };
        for (int accuracy = 0, i = 0; i < types.length && accuracy <= 2; ++i) {
            final int diff = dateDiff(types[i], fromDate, toDate, future);
            if (diff > 0) {
                ++accuracy;
                sb.append(" ").append(diff).append("").append(names[i * 2 + ((diff > 1) ? 1 : 0)]);
            }
        }
        return (sb.length() == 0) ? "now" : sb.toString().trim();
    }
    
    public static String readableTime(final long time) {
        final short SECOND = 1000;
        final int MINUTE = 60 * SECOND;
        final int HOUR = 60 * MINUTE;
        final int DAY = 24 * HOUR;
        long ms = time;
        final StringBuilder text = new StringBuilder("");
        if (time > DAY) {
            text.append(time / DAY).append(" days ");
            ms = time % DAY;
        }
        if (ms > HOUR) {
            text.append(ms / HOUR).append(" hours ");
            ms %= HOUR;
        }
        if (ms > MINUTE) {
            text.append(ms / MINUTE).append(" minutes ");
            ms %= MINUTE;
        }
        if (ms > SECOND) {
            text.append(ms / SECOND).append(" seconds ");
            final long n = ms % SECOND;
        }
        if (text.toString().trim().isEmpty()) {
            return "now";
        }
        return text.toString();
    }
    
    public static String readableTime(BigDecimal time) {
        final String text = "";
        if (time.doubleValue() <= 60.0) {
            time = time.add(BigDecimal.valueOf(0.1));
            return text + " " + time + "s";
        }
        if (time.doubleValue() <= 3600.0) {
            final int minutes = time.intValue() / 60;
            final int seconds = time.intValue() % 60;
            final DecimalFormat formatter = new DecimalFormat("00");
            return text + " " + formatter.format(minutes) + ":" + formatter.format(seconds) + "m";
        }
        return null;
    }
    
    public static String convertTime(long time) {
        if (time < 0L) {
            return null;
        }
        final long week = 0L;
        long day = 0L;
        long hour = 0L;
        long minute = 0L;
        long second = 0L;
        day = time / 86400L;
        time -= day * 86400L;
        hour = time / 3600L;
        time -= hour * 3600L;
        minute = time / 60L;
        time = (second = time - minute * 60L);
        String build = "";
        if (day > 0L) {
            build = build + day + "d, ";
        }
        if (hour > 0L) {
            build = build + hour + "h, ";
        }
        if (minute > 0L) {
            build = build + minute + "m, ";
        }
        if (second > 0L) {
            build = build + second + "s";
        }
        if (build.length() == 0) {
            build = "0s";
        }
        return build.trim();
    }
    
    public static String formatTime(final long millis) {
        final int sec = (int)(millis / 1000L % 60L);
        final int min = (int)(millis / 60000L % 60L);
        final int hr = (int)(millis / 3600000L % 24L);
        return ((hr > 0) ? String.format("%02d:", hr) : "") + String.format("%02d:%02d", min, sec);
    }
    
    static {
        TimeUtil.timePattern = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?(?:([0-9]+)\\s*(?:s[a-z]*)?)?", 2);
    }
}
