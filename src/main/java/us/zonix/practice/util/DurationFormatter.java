package us.zonix.practice.util;

import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.time.DurationFormatUtils;
import java.text.DecimalFormat;

public class DurationFormatter
{
    private static final long MINUTE;
    private static final long HOUR;
    
    public static String getRemaining(final long millis, final boolean milliseconds) {
        return getRemaining(millis, milliseconds, true);
    }
    
    public static String getRemaining(final long duration, final boolean milliseconds, final boolean trail) {
        if (milliseconds && duration < DurationFormatter.MINUTE) {
            return (trail ? DateTimeFormats.REMAINING_SECONDS_TRAILING : DateTimeFormats.REMAINING_SECONDS).get().format(duration * 0.001) + 's';
        }
        return DurationFormatUtils.formatDuration(duration, ((duration >= DurationFormatter.HOUR) ? "HH:" : "") + "mm:ss");
    }
    
    static {
        MINUTE = TimeUnit.MINUTES.toMillis(1L);
        HOUR = TimeUnit.HOURS.toMillis(1L);
    }
}
