package com.logframework.aggregator;

import com.logframework.dto.ResultDTO;
import com.logframework.model.LogEntry;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.testng.Assert.*;

public class ErrorRateOverTimeAggregatorTest {

    @Test
    public void testErrorRateCalculation() {
        ErrorRateOverTimeAggregator aggregator = new ErrorRateOverTimeAggregator();
        aggregator.setBucketDuration("uuuu-MM-dd'T'HH:mm");

        LogEntry entry1 = new LogEntry();
        entry1.setTimestamp(OffsetDateTime.parse("2025-09-18T16:15:00Z"));
        entry1.setLevel("INFO");
        aggregator.process(entry1);

        LogEntry entry2 = new LogEntry();
        entry2.setTimestamp(OffsetDateTime.parse("2025-09-18T16:15:30Z"));
        entry2.setLevel("ERROR");
        aggregator.process(entry2);

        LogEntry entry3 = new LogEntry();
        entry3.setTimestamp(OffsetDateTime.parse("2025-09-18T16:15:45Z"));
        entry3.setLevel("ERROR");
        aggregator.process(entry3);

        ResultDTO result = aggregator.getResult();
        assertEquals(result.getTitle(), "Error Rate Over Time");
        List<List<String>> data = result.getData();
        assertEquals(data.size(), 1);
        assertEquals(data.get(0).get(0), "2025-09-18T16:15");
        assertEquals(data.get(0).get(1), "3"); // total
        assertEquals(data.get(0).get(2), "2"); // errors
        assertEquals(data.get(0).get(3), "66.67"); // error rate
    }

    @Test
    public void testNoEntries() {
        ErrorRateOverTimeAggregator aggregator = new ErrorRateOverTimeAggregator();
        aggregator.setBucketDuration("uuuu-MM-dd'T'HH:mm");
        ResultDTO result = aggregator.getResult();
        assertEquals(result.getData().size(), 0);
    }

    @Test
    public void testDifferentBuckets() {
        ErrorRateOverTimeAggregator aggregator = new ErrorRateOverTimeAggregator();
        aggregator.setBucketDuration("uuuu-MM-dd'T'HH:mm");

        LogEntry entry1 = new LogEntry();
        entry1.setTimestamp(OffsetDateTime.parse("2025-09-18T16:15:00Z"));
        entry1.setLevel("ERROR");
        aggregator.process(entry1);

        LogEntry entry2 = new LogEntry();
        entry2.setTimestamp(OffsetDateTime.parse("2025-09-18T16:16:00Z"));
        entry2.setLevel("INFO");
        aggregator.process(entry2);

        ResultDTO result = aggregator.getResult();
        assertEquals(result.getData().size(), 2);
        assertEquals(result.getData().get(0).get(0), "2025-09-18T16:15");
        assertEquals(result.getData().get(0).get(2), "1");
        assertEquals(result.getData().get(1).get(0), "2025-09-18T16:16");
        assertEquals(result.getData().get(1).get(2), "0");
    }
}