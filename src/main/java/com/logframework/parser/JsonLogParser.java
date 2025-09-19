package com.logframework.parser;

import com.logframework.model.LogEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class JsonLogParser implements LogParser {
    private static final Logger logger = Logger.getLogger(JsonLogParser.class.getName());
    private final ObjectMapper objectMapper;

    public JsonLogParser() {
        this.objectMapper = new ObjectMapper();
        logger.info("JsonLogParser initialized");
    }

    @Override
    public DateTimeFormatter getDateTimeFormatter() {
        return DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSZ", java.util.Locale.ENGLISH);
    }

    @Override
    public LogEntry parse(String logContent) {

        if (logContent == null || logContent.trim().isEmpty()) {
            logger.warning("Received null or empty log content");
            return null;
        }

        logger.fine("Attempting to parse JSON log content: " + logContent);

        try {
            JsonNode node = objectMapper.readTree(logContent);
            LogEntry entry = new LogEntry();

            // Parse timestamp
            if (node.has("timestamp")) {
                String timestampStr = node.get("timestamp").asText();
                entry.setTimestamp(OffsetDateTime.parse(timestampStr));
                logger.fine("Parsed timestamp: " + timestampStr);
            } else {
                logger.fine("No timestamp field found in JSON log entry");
            }

            // Parse level
            if (node.has("level")) {
                String level = node.get("level").asText();
                entry.setLevel(level);
                logger.fine("Parsed level: " + level);
            } else {
                logger.fine("No level field found in JSON log entry");
            }

            // Parse message
            if (node.has("message")) {
                String message = node.get("message").asText();
                entry.setMessage(message);
                logger.fine("Parsed message: " + message);
            } else {
                logger.fine("No message field found in JSON log entry");
            }

            // Parse additional attributes
            int attributeCount = 0;
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                if (!Arrays.asList("timestamp", "level", "message").contains(key)) {
                    entry.addAttribute(key, field.getValue().asText());
                    attributeCount++;
                }
            }
            
            logger.fine("Parsed " + attributeCount + " additional attributes");
            logger.fine("Successfully parsed JSON log entry");
            return entry;

        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to parse JSON log content: " + logContent, e);
            return null;
        }
    }

    @Override
    public boolean canParse(String logContent) {
        if (logContent == null || logContent.trim().isEmpty()) {
            logger.fine("Cannot parse null or empty content");
            return false;
        }

        logger.fine("Checking if content can be parsed as JSON: " + logContent);

        try {
            objectMapper.readTree(logContent);
            logger.fine("Content can be parsed as valid JSON");
            return true;
        } catch (Exception e) {
            logger.fine("Content cannot be parsed as JSON: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getParserName() {
        return "JSON Parser";
    }
}
