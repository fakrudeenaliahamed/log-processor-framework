package com.logframework.filter;

import com.logframework.model.LogEntry;
import java.util.regex.Pattern;
import java.util.logging.Logger;


@Description("Filters log entries by applying a regex to a specified field.")
public class RegexFilter implements LogFilter {
    private static final Logger logger = Logger.getLogger(RegexFilter.class.getName());

    @Parameter(
        "The field to apply the regex on (e.g., message, level, or attribute name). " +
        "Examples: 'message', 'level', 'user_id'."
    )
    private String field;

    @Parameter(
        "The regex pattern to match. " +
        "Examples: '.*error.*' (matches any text containing 'error'), '^INFO' (lines starting with 'INFO'), 'Exception|Error' (matches 'Exception' or 'Error')."
    )
    private String regex;

    private Pattern pattern;

    public void setField(String field) {
        logger.info("Setting field to: " + field);
        this.field = field;
    }

    public void setRegex(String regex) {
        logger.info("Setting regex pattern to: " + regex);
        this.regex = regex;
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public boolean matches(LogEntry entry) {
        if (field == null || regex == null) {
            logger.warning("Field or regex not set. Skipping filter.");
            return true; // If not configured, do not filter out anything
        }

        Object value = null;
        switch (field) {
            case "level":
                value = entry.getLevel();
                break;
            case "message":
                value = entry.getMessage();
                break;
            case "source":
                value = entry.getSource();
                break;
            default:
                value = entry.getAttribute(field);
        }

        if (value == null) {
            logger.fine("LogEntry missing field '" + field + "', skipping.");
            return false;
        }
        boolean result = pattern.matcher(value.toString()).find();
        logger.finer("Filtering entry: field='" + field + "', value='" + value + "', matches=" + result);
        return result;
    }
}
