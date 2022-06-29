package us.zonix.practice.board;

import org.apache.commons.lang.time.DurationFormatUtils;
import java.text.DecimalFormat;

public class BoardTimer
{
    private static final DecimalFormat SECONDS_FORMATTER;
    private final Board board;
    private final String id;
    private final double duration;
    private final long end;
    
    public BoardTimer(final Board board, final String id, final double duration) {
        this.board = board;
        this.id = id;
        this.duration = duration;
        this.end = (long)(System.currentTimeMillis() + duration * 1000.0);
        if (board != null) {
            board.getTimers().add(this);
        }
    }
    
    public String getFormattedString(final TimerType format) {
        if (format == TimerType.SECONDS) {
            return BoardTimer.SECONDS_FORMATTER.format((this.end - System.currentTimeMillis()) / 1000.0f);
        }
        return DurationFormatUtils.formatDuration(this.end - System.currentTimeMillis(), "mm:ss");
    }
    
    public void cancel() {
        if (this.board != null) {
            this.board.getTimers().remove(this);
        }
    }
    
    public Board getBoard() {
        return this.board;
    }
    
    public String getId() {
        return this.id;
    }
    
    public double getDuration() {
        return this.duration;
    }
    
    public long getEnd() {
        return this.end;
    }
    
    static {
        SECONDS_FORMATTER = new DecimalFormat("#0.0");
    }
    
    public enum TimerType
    {
        SECONDS, 
        MINUTES, 
        HOURS;
    }
}
