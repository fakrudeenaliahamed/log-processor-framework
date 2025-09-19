package com.logframework.model;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.HashMap;

public class LogEntry {
    private OffsetDateTime timestamp;
    private String level;
    private String message;
    private String source;
    private Map<String, Object> attributes;

    public LogEntry() {
        this.attributes = new HashMap<>();
    }

    public LogEntry(OffsetDateTime timestamp, String level, String message, String source) {
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
        this.source = source;
        this.attributes = new HashMap<>();
    }

    // Getters and setters
    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }

    public void addAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s", timestamp, level, message);
    }
}
