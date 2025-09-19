package com.logframework.aggregator;

import com.logframework.model.LogEntry;
import com.logframework.dto.ResultDTO;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class TopEndpointsAggregatorTest {

    @Test
    public void testTopEndpointsBasic() {
        TopEndpointsAggregator aggregator = new TopEndpointsAggregator();
        aggregator.setTopN(2);

        LogEntry entry1 = new LogEntry();
        entry1.addAttribute("path", "/api/users");
        aggregator.process(entry1);

        LogEntry entry2 = new LogEntry();
        entry2.addAttribute("path", "/api/orders");
        aggregator.process(entry2);

        LogEntry entry3 = new LogEntry();
        entry3.addAttribute("path", "/api/users");
        aggregator.process(entry3);

        ResultDTO result = aggregator.getResult();
        assertEquals(result.getTitle(), "Top 2 Endpoints");
        assertEquals(result.getHeaders().size(), 2);
        assertEquals(result.getHeaders().get(0), "Endpoint");
        assertEquals(result.getHeaders().get(1), "Count");
        assertEquals(result.getData().size(), 2);

        // The first row should be /api/users with count 2
        assertEquals(result.getData().get(0).get(0), "/api/users");
        assertEquals(result.getData().get(0).get(1), "2");

        // The second row should be /api/orders with count 1
        assertEquals(result.getData().get(1).get(0), "/api/orders");
        assertEquals(result.getData().get(1).get(1), "1");
    }

    @Test
    public void testTopNMoreThanUniqueEndpoints() {
        TopEndpointsAggregator aggregator = new TopEndpointsAggregator();
        aggregator.setTopN(5);

        LogEntry entry1 = new LogEntry();
        entry1.addAttribute("path", "/a");
        aggregator.process(entry1);

        LogEntry entry2 = new LogEntry();
        entry2.addAttribute("path", "/b");
        aggregator.process(entry2);

        ResultDTO result = aggregator.getResult();
        assertEquals(result.getData().size(), 2);
    }

    @Test
    public void testNoEndpoints() {
        TopEndpointsAggregator aggregator = new TopEndpointsAggregator();
        aggregator.setTopN(3);

        ResultDTO result = aggregator.getResult();
        assertEquals(result.getData().size(), 0);
    }
}