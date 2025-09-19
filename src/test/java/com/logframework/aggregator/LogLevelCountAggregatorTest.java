package com.logframework.aggregator;

import com.logframework.model.LogEntry;
import com.logframework.dto.ResultDTO;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

public class LogLevelCountAggregatorTest {

    @Test
    public void testLogLevelCounting() {
        LogLevelCountAggregator aggregator = new LogLevelCountAggregator();

        LogEntry entry1 = new LogEntry();
        entry1.setLevel("INFO");
        aggregator.process(entry1);

        LogEntry entry2 = new LogEntry();
        entry2.setLevel("ERROR");
        aggregator.process(entry2);

        LogEntry entry3 = new LogEntry();
        entry3.setLevel("INFO");
        aggregator.process(entry3);

        ResultDTO result = aggregator.getResult();
        assertEquals(result.getTitle(), "Log Level Counts");
        List<List<String>> data = result.getData();
        assertEquals(data.size(), 2);

        // Find INFO and ERROR counts
        boolean foundInfo = false, foundError = false;
        for (List<String> row : data) {
            if (row.get(0).equals("INFO")) {
                assertEquals(row.get(1), "2");
                foundInfo = true;
            }
            if (row.get(0).equals("ERROR")) {
                assertEquals(row.get(1), "1");
                foundError = true;
            }
        }
        assertTrue(foundInfo, "INFO level should be present");
        assertTrue(foundError, "ERROR level should be present");
    }

    @Test
    public void testNoEntries() {
        LogLevelCountAggregator aggregator = new LogLevelCountAggregator();
        ResultDTO result = aggregator.getResult();
        assertEquals(result.getData().size(), 0);
    }

    @Test
    public void testNullLevelIgnored() {
        LogLevelCountAggregator aggregator = new LogLevelCountAggregator();

        LogEntry entry = new LogEntry();
        entry.setLevel(null);
        aggregator.process(entry);

        ResultDTO result = aggregator.getResult();
        assertEquals(result.getData().size(), 0);
    }
}