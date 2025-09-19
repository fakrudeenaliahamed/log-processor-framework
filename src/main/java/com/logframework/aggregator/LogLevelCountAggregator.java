package com.logframework.aggregator;

import com.logframework.model.LogEntry;
import java.util.*;

import com.logframework.dto.ResultDTO;
import com.logframework.filter.Description;

@Description(
    "Aggregates and counts the number of log entries for each log level (e.g., INFO, ERROR, DEBUG). ")
    
public class LogLevelCountAggregator implements LogAggregator {
    private Map<String, Long> levelCounts = new HashMap<>();

    @Override
    public void process(LogEntry entry) {
        if (entry.getLevel() != null) {
            levelCounts.merge(entry.getLevel(), 1L, Long::sum);
        }
    }

    @Override
    public ResultDTO getResult() {
        List<String> headers = Arrays.asList("Log Level", "Count");
        List<List<String>> data = new ArrayList<>();
        for (Map.Entry<String, Long> e : levelCounts.entrySet()) {
            data.add(Arrays.asList(e.getKey(), e.getValue().toString()));
        }
        return new ResultDTO("Log Level Counts", headers, data);
    }

}
