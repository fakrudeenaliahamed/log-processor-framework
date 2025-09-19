package com.logframework.parser;

import com.logframework.model.LogEntry;

import java.time.format.DateTimeFormatter;

public interface LogParser {
    LogEntry parse(String logContent);
    boolean canParse(String logContent);
    String getParserName();
    DateTimeFormatter getDateTimeFormatter();


    default String getStartPattern() {
        return null; // Default: single-line parsing
    }
    
    /**
     * Indicates whether this parser expects multi-line log entries.
     */
    default boolean isMultiLine() {
        return getStartPattern() != null;
    }


}
