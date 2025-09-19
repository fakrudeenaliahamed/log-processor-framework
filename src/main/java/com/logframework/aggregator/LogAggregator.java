package com.logframework.aggregator;

import com.logframework.dto.ResultDTO;
import com.logframework.model.LogEntry;

public interface LogAggregator {
    void process(LogEntry entry); // NEW: single entry
    ResultDTO getResult();
}
