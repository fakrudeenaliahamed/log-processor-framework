package com.logframework.filter;

import com.logframework.model.LogEntry;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

@Description("Filters log entries to only include those within a specified time range.")
public class TimeRangeFilter implements LogFilter {
    private static final Logger logger = Logger.getLogger(TimeRangeFilter.class.getName());

    @Parameter("Start time (inclusive) in ISO-8601 format, e.g., 2025-09-18T16:00:00Z")
    private String startTime;

    @Parameter("End time (inclusive) in ISO-8601 format, e.g., 2025-09-18T18:00:00Z")
    private String endTime;

    private OffsetDateTime start;
    private OffsetDateTime end;

    public void setStartTime(String startTime) {
        logger.info("Setting startTime to: " + startTime);
        this.startTime = startTime;
        if (startTime != null && !startTime.isEmpty()) {
            this.start = OffsetDateTime.parse(startTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
    }

    public void setEndTime(String endTime) {
        logger.info("Setting endTime to: " + endTime);
        this.endTime = endTime;
        if (endTime != null && !endTime.isEmpty()) {
            this.end = OffsetDateTime.parse(endTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
    }

    @Override
    public boolean matches(LogEntry entry) {
        OffsetDateTime ts = entry.getTimestamp();
        if (ts == null) {
            logger.fine("LogEntry timestamp is null, skipping.");
            return false;
        }
        boolean afterStart = (start == null) || !ts.isBefore(start);
        boolean beforeEnd = (end == null) || !ts.isAfter(end);
        boolean result = afterStart && beforeEnd;
        logger.finer("Filtering entry: timestamp=" + ts + ", afterStart=" + afterStart + ", beforeEnd=" + beforeEnd + ", matches=" + result);
        return result;
    }
}
