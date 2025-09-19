package com.logframework.parser;

import com.logframework.model.LogEntry;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;

public class ApacheAccessLogParser implements LogParser {

    private static final Logger logger = Logger.getLogger(ApacheAccessLogParser.class.getName());

    private static final String COMBINED_LOG_FORMAT_REGEX =
            "^(\\S+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"([A-Z]+) (.+?) (HTTP/\\d\\.\\d)\" (\\d{3}) (\\d+|-)? \"([^\"]*)\" \"([^\"]*)\"";

    private static final Pattern APACHE_PATTERN = Pattern.compile(COMBINED_LOG_FORMAT_REGEX);

    @Override
    public DateTimeFormatter getDateTimeFormatter() {
        return DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", java.util.Locale.ENGLISH);
    }

    @Override
    public String getStartPattern() {
        return null;
    }

    @Override
    public LogEntry parse(String logContent) {
        logger.finer("Attempting to parse log line: " + logContent);
        Matcher matcher = APACHE_PATTERN.matcher(logContent);
        if (matcher.matches()) {
            LogEntry entry = new LogEntry();

            // Group 1: IP Address
            entry.addAttribute("ip", matcher.group(1));

            // Group 4: Timestamp
            try {
                entry.setTimestamp(OffsetDateTime.parse(matcher.group(4), getDateTimeFormatter()));
            } catch (Exception e) {
                logger.warning("Failed to parse timestamp from: " + matcher.group(4) + " - " + e.getMessage());
                entry.setTimestamp(OffsetDateTime.now());
            }

            // Group 5, 6, 7: Method, Path, Protocol
            String method = matcher.group(5);
            String path = matcher.group(6);
            entry.addAttribute("method", method);
            entry.addAttribute("path", path);
            entry.addAttribute("protocol", matcher.group(7));

            // Group 8: Status Code
            int statusCode = Integer.parseInt(matcher.group(8));
            entry.addAttribute("status", statusCode);

            // Set a standardized log level based on the status code
            if (statusCode >= 500) {
                entry.setLevel("ERROR");
            } else if (statusCode >= 400) {
                entry.setLevel("WARN");
            } else {
                entry.setLevel("INFO");
            }

            // Group 9: Response Size
            String sizeStr = matcher.group(9);
            entry.addAttribute("size", "-".equals(sizeStr) ? 0 : Integer.parseInt(sizeStr));

            // Group 10: Referrer
            entry.addAttribute("referrer", matcher.group(10));

            // Group 11: User-Agent
            entry.addAttribute("userAgent", matcher.group(11));

            // Create a meaningful main message for the log entry
            entry.setMessage(String.format("%s %s - Status %d", method, path, statusCode));

            logger.fine("Parsed Apache access log entry: " + entry);
            return entry;
        } else {
            logger.finer("Log line did not match Apache access log pattern.");
        }

        return null;
    }

    @Override
    public boolean canParse(String logContent) {
        boolean result = logContent != null && !logContent.trim().isEmpty() && APACHE_PATTERN.matcher(logContent).matches();
        logger.finer("canParse called for line: " + logContent + " | result: " + result);
        return result;
    }

    @Override
    public String getParserName() {
        return "Apache Access Log Parser (Combined Format)";
    }
}
