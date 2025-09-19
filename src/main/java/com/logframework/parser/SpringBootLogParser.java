package com.logframework.parser;

import com.logframework.model.LogEntry;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Simple parser for Spring Boot application logs with multi-line stack trace support.
 * Handles logs in format: YYYY-MM-DD HH:MM:SS [thread] LEVEL logger - message
 */
public class SpringBootLogParser implements LogParser {
    private static final Logger logger = Logger.getLogger(SpringBootLogParser.class.getName());

    // Only keep the detailed pattern for parsing individual fields
    // Updated pattern to handle variable spaces between LEVEL and logger
    private static final Pattern LOG_PARSE_PATTERN = Pattern.compile(
            "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}) \\[([^\\]]+)\\] (\\w+)\\s+([\\w\\.]+) - (.*)$"
    );

    // DateTimeFormatter for parsing timestamps
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SpringBootLogParser() {
        logger.info("SpringBootLogParser initialized with pattern support for multi-line logs");
    }

    @Override
    public LogEntry parse(String logContent) {
        if (logContent == null || logContent.trim().isEmpty()) {
            logger.warning("Received null or empty log content");
            return null;
        }

        logger.fine("Attempting to parse Spring Boot log content with " + 
                   logContent.split(System.lineSeparator()).length + " line(s)");

        String[] lines = logContent.split(System.lineSeparator());
        String firstLine = lines[0];
        
        logger.fine("First line: " + firstLine);

        Matcher matcher = LOG_PARSE_PATTERN.matcher(firstLine);
        if (!matcher.matches()) {
            logger.fine("Log content does not match Spring Boot pattern");
            return null; // Not a valid Spring Boot log format
        }

        LogEntry entry = new LogEntry();
        logger.fine("Successfully matched Spring Boot log pattern");

        try {
            // Parse timestamp (group 1) and convert to OffsetDateTime
            String timestampStr = matcher.group(1);
            LocalDateTime localDateTime = LocalDateTime.parse(timestampStr, TIMESTAMP_FORMATTER);
            OffsetDateTime offsetDateTime = localDateTime.atOffset(ZoneOffset.UTC);
            entry.setTimestamp(offsetDateTime);
            logger.fine("Parsed timestamp: " + timestampStr + " -> " + offsetDateTime);
        } catch (DateTimeParseException e) {
            logger.log(Level.WARNING, "Failed to parse timestamp: " + matcher.group(1), e);
            // If timestamp parsing fails, use current time
            entry.setTimestamp(OffsetDateTime.now());
            logger.fine("Using current time as fallback timestamp");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unexpected error parsing timestamp", e);
            entry.setTimestamp(OffsetDateTime.now());
        }

        // Parse thread name (group 2)
        String thread = matcher.group(2);
        logger.fine("Parsed thread: " + thread);

        // Parse log level (group 3)
        String level = matcher.group(3);
        entry.setLevel(level);
        logger.fine("Parsed level: " + level);

        // Parse logger name (group 4)
        String loggerName = matcher.group(4);
        entry.addAttribute("logger", loggerName);
        entry.addAttribute("thread", thread);
        logger.fine("Parsed logger: " + loggerName);

        // For multi-line logs, use the entire content as the message
        if (lines.length > 1) {
            // Multi-line log: use full content as message
            entry.setMessage(logContent.trim());
            entry.addAttribute("multiline", true);
            logger.fine("Multi-line log detected with " + lines.length + " lines, using full content as message");
        } else {
            // Single-line log: extract just the message part (group 5)
            String message = matcher.group(5);
            entry.setMessage(message);
            entry.addAttribute("multiline", false);
            logger.fine("Single-line log, extracted message: " + message);
        }

        logger.info("Successfully parsed Spring Boot log entry with level: " + level);
        return entry;
    }

    @Override
    public boolean canParse(String logContent) {
        if (logContent == null || logContent.trim().isEmpty()) {
            logger.fine("Cannot parse null or empty content");
            return false;
        }

        logger.fine("Checking if content can be parsed as Spring Boot log format");

        // Test against the first line only for initial detection
        String firstLine = logContent.split(System.lineSeparator())[0];
        boolean canParse = LOG_PARSE_PATTERN.matcher(firstLine).matches();
        
        if (canParse) {
            logger.fine("Content matches Spring Boot log pattern");
        } else {
            logger.fine("Content does not match Spring Boot log pattern. First line: " + firstLine);
        }
        
        return canParse;
    }

    @Override
    public String getParserName() {
        return "Spring Boot Log Parser";
    }

    @Override
    public DateTimeFormatter getDateTimeFormatter() {
        logger.fine("Providing DateTimeFormatter for pattern: yyyy-MM-dd HH:mm:ss");
        return TIMESTAMP_FORMATTER;
    }

    @Override
    public String getStartPattern() {
        String pattern = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} \\[";
        logger.fine("Providing start pattern for multi-line detection: " + pattern);
        return pattern;
    }

    @Override
    public boolean isMultiLine() {
        logger.fine("Parser supports multi-line log entries");
        return true;
    }
}
