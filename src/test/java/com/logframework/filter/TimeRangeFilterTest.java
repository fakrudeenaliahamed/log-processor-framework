package com.logframework.filter;

import com.logframework.model.LogEntry;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;

import static org.testng.Assert.*;

public class TimeRangeFilterTest {

    @Test
    public void testMatchesWithinRange() {
        TimeRangeFilter filter = new TimeRangeFilter();
        filter.setStartTime("2025-09-18T16:00:00Z");
        filter.setEndTime("2025-09-18T18:00:00Z");

        LogEntry entry = new LogEntry();
        entry.setTimestamp(OffsetDateTime.parse("2025-09-18T17:00:00Z"));

        assertTrue(filter.matches(entry));
    }

    @Test
    public void testDoesNotMatchBeforeStart() {
        TimeRangeFilter filter = new TimeRangeFilter();
        filter.setStartTime("2025-09-18T16:00:00Z");
        filter.setEndTime("2025-09-18T18:00:00Z");

        LogEntry entry = new LogEntry();
        entry.setTimestamp(OffsetDateTime.parse("2025-09-18T15:59:59Z"));

        assertFalse(filter.matches(entry));
    }

    @Test
    public void testDoesNotMatchAfterEnd() {
        TimeRangeFilter filter = new TimeRangeFilter();
        filter.setStartTime("2025-09-18T16:00:00Z");
        filter.setEndTime("2025-09-18T18:00:00Z");

        LogEntry entry = new LogEntry();
        entry.setTimestamp(OffsetDateTime.parse("2025-09-18T18:00:01Z"));

        assertFalse(filter.matches(entry));
    }

    @Test
    public void testMatchesWhenNoStartTime() {
        TimeRangeFilter filter = new TimeRangeFilter();
        filter.setEndTime("2025-09-18T18:00:00Z");

        LogEntry entry = new LogEntry();
        entry.setTimestamp(OffsetDateTime.parse("2025-09-18T17:00:00Z"));

        assertTrue(filter.matches(entry));
    }

    @Test
    public void testMatchesWhenNoEndTime() {
        TimeRangeFilter filter = new TimeRangeFilter();
        filter.setStartTime("2025-09-18T16:00:00Z");

        LogEntry entry = new LogEntry();
        entry.setTimestamp(OffsetDateTime.parse("2025-09-18T20:00:00Z"));

        assertTrue(filter.matches(entry));
    }

    @Test
    public void testDoesNotMatchWhenTimestampIsNull() {
        TimeRangeFilter filter = new TimeRangeFilter();
        filter.setStartTime("2025-09-18T16:00:00Z");
        filter.setEndTime("2025-09-18T18:00:00Z");

        LogEntry entry = new LogEntry();
        entry.setTimestamp(null);

        assertFalse(filter.matches(entry));
    }
}