package com.logframework.filter;

import com.logframework.model.LogEntry;

public interface LogFilter {
    boolean matches(LogEntry entry);
}
