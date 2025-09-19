package com.logframework;

import com.logframework.model.LogEntry;
import com.logframework.parser.*;
import com.logframework.filter.LogFilter;
import com.logframework.aggregator.LogAggregator;
import com.logframework.reporter.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.*;
import java.util.regex.Pattern;

public class LogProcessingManager {
    private static final Logger logger = Logger.getLogger(LogProcessingManager.class.getName());

    private final List<LogParser> parsers;
    private final List<LogFilter> filters;
    private final List<LogAggregator> aggregators;
    private final List<LogReporter> reporters;

    private String outputDirectory = "reports"; // Default output directory

    public LogProcessingManager() {
        this(new ConfigLoader());
    }

    public LogProcessingManager(ConfigLoader configLoader) {
        this.parsers = new ArrayList<>();
        this.filters = new ArrayList<>();
        this.aggregators = new ArrayList<>();
        this.reporters = new ArrayList<>();

        outputDirectory = configLoader.getProperty("reporter.outputFolder", "reports");
        logger.info("Output directory set to: " + outputDirectory);

        String parserClasses = configLoader.getProperty("parsers", "");
        if (!parserClasses.isEmpty()) {
            for (String className : parserClasses.split(",")) {
                className = className.trim();
                if (!className.isEmpty()) {
                    try {
                        Class<?> clazz = Class.forName(className);
                        LogParser parser = (LogParser) clazz.getDeclaredConstructor().newInstance();
                        registerParser(parser);
                        logger.info("Loaded parser: " + className);
                    } catch (Exception e) {
                        logger.warning("Failed to load parser: " + className + " - " + e.getMessage());
                    }
                }
            }
        } else {
            registerParser(new JsonLogParser());
            registerParser(new ApacheAccessLogParser());
            logger.info("Loaded default parsers.");
        }
    }



    /**
     * Enhanced processLogFiles with multi-line support while maintaining streaming.
     * Main method for processing logs, filtering and aggregating data
     */
    public void processLogFiles(List<String> filePaths) {
        for (String filePath : filePaths) {
            LogParser selectedParser = null;
            Pattern startPattern = null;
            StringBuilder multiLineBuffer = new StringBuilder();

            try (var lines = Files.lines(Path.of(filePath))) {
                for (String line : (Iterable<String>) lines::iterator) {

                    // Select parser on first non-empty line
                    if (selectedParser == null) {
                        if (!line.trim().isEmpty()) {
                            selectedParser = selectParser(line);
                            if (selectedParser != null) {
                                System.out.printf("Processing %s with %s\n", filePath, selectedParser.getParserName());

                                // Initialize multi-line support if needed
                                if (selectedParser.isMultiLine()) {
                                    startPattern = Pattern.compile(selectedParser.getStartPattern());
                                }
                            } else {
                                System.err.println("No suitable parser found for: " + filePath);
                                break;
                            }
                        }
                        continue;
                    }

                    if (selectedParser != null) {
                        if (selectedParser.isMultiLine()) {
                            // MULTI-LINE PROCESSING
                            processMultiLineEntry(line, startPattern, multiLineBuffer, selectedParser, filePath);
                        } else {
                            // SINGLE-LINE PROCESSING
                            processSingleLineEntry(line, selectedParser, filePath);
                        }
                    }
                }

                // Handle the last multi-line entry after the stream ends
                if (selectedParser != null && selectedParser.isMultiLine() && multiLineBuffer.length() > 0) {
                    processCompleteEvent(multiLineBuffer.toString(), selectedParser, filePath);
                }

            } catch (IOException e) {
                logger.warning("Failed to read file: " + filePath);
            }
        }
        System.out.printf("\nAggregation complete for %d files\n", filePaths.size());
    }

    public void generateReport() {
        String runDirectory = createRunDirectory();

        for (LogAggregator aggregator : aggregators) {
            for (LogReporter reporter : reporters) {
                reporter.setOutputDirectory(runDirectory);
                reporter.report(aggregator.getResult());
            }
        }
    }


    public void registerParser(LogParser parser) {
        parsers.add(parser);
        logger.fine("Registered parser: " + parser.getClass().getName());
    }

    public void addFilter(LogFilter filter) {
        filters.add(filter);
        logger.fine("Added filter: " + filter.getClass().getName());
    }

    public void addAggregator(LogAggregator aggregator) {
        aggregators.add(aggregator);
        logger.fine("Added aggregator: " + aggregator.getClass().getName());
    }

    public void addReporter(LogReporter reporter) {
        reporters.add(reporter);
        logger.fine("Added reporter: " + reporter.getClass().getName());
    }

    public List<LogAggregator> getAggregators() {
        logger.fine("Retrieving aggregators list.");
        return Collections.unmodifiableList(aggregators);
    }

    public List<LogReporter> getReporters(){
        logger.fine("Retrieving reporter list.");
        return Collections.unmodifiableList(reporters);
    }

    private void processSingleLogFile(String filePath) {
        logger.info("Processing file: " + filePath);
        LogParser selectedParser = null;
        try (var lines = Files.lines(Path.of(filePath))) {
            for (String line : (Iterable<String>) lines::iterator) {
                if (selectedParser == null) {
                    selectedParser = trySelectParser(line, filePath);
                    if (selectedParser == null) {
                        logger.warning("No suitable parser found for: " + filePath);
                        break;
                    }
                }
                if (selectedParser != null) {
                    processLogLine(selectedParser, line, filePath);
                }
            }
            logger.fine("Finished processing file: " + filePath);
        } catch (IOException e) {
            logger.warning("Failed to read file: " + filePath + " - " + e.getMessage());
        }
    }

    private LogParser trySelectParser(String line, String filePath) {
        if (!line.trim().isEmpty()) {
            LogParser parser = selectParser(line);
            if (parser != null) {
                logger.info("Selected parser " + parser.getParserName() + " for file: " + filePath);
            } else {
                logger.warning("No suitable parser found for: " + filePath);
            }
            return parser;
        }
        logger.fine("Empty or whitespace line encountered while selecting parser for file: " + filePath);
        return null;
    }

    private LogParser selectParser(String line) {
        for (LogParser parser : parsers) {
            if (parser.canParse(line)) {
                logger.fine("Parser " + parser.getParserName() + " can parse the line.");
                return parser;
            }
        }
        logger.warning("No parser could parse the line: " + line);
        return null;
    }

    private void processLogLine(LogParser parser, String line, String filePath) {
        LogEntry entry = parser.parse(line);
        if (entry != null) {
            entry.setSource(filePath);
            boolean matches = filters.stream().allMatch(filter -> filter.matches(entry));
            if (matches) {
                for (LogAggregator aggregator : aggregators) {
                    aggregator.process(entry);
                }
                logger.finer("Log entry matched filters and was processed by aggregators.");
            } else {
                logger.finer("Log entry did not match filters and was skipped.");
            }
        } else {
            logger.finer("Parsed log entry is null for line: " + line);
        }
    }



    public String getOutputDirectory() {
        logger.fine("Retrieving output directory: " + outputDirectory);
        return outputDirectory;
    }


    private String createRunDirectory() {
        String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String report = outputDirectory + "/run-" + timestamp;
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(report));
        } catch (IOException e) {
            System.err.println("❌ Could not create run directory: " + outputDirectory);
        }
        return report;
    }

    /**
     * Handles multi-line log entry processing with streaming.
     */
    private void processMultiLineEntry(String line, Pattern startPattern, StringBuilder buffer, 
                                     LogParser parser, String filePath) {
        boolean isStartLine = startPattern.matcher(line).find();
        
        // If this is a start line and we have content in the buffer, process the previous event
        if (isStartLine && buffer.length() > 0) {
            processCompleteEvent(buffer.toString(), parser, filePath);
            buffer.setLength(0); // Clear buffer for new event
        }
        
        // Add current line to buffer
        buffer.append(line).append(System.lineSeparator());
    }

    /**
     * Handles single-line log entry processing (your existing logic).
     */
    private void processSingleLineEntry(String line, LogParser parser, String filePath) {
        LogEntry entry = parser.parse(line);
        if (entry != null) {
            entry.setSource(filePath);
            boolean matches = filters.stream().allMatch(filter -> filter.matches(entry));
            if (matches) {
                for (LogAggregator aggregator : aggregators) {
                    aggregator.process(entry); // Using your exact method signature
                }
            }
        }
    }

    /**
     * Processes a complete multi-line event.
     */
    private void processCompleteEvent(String eventContent, LogParser parser, String filePath) {
        LogEntry entry = parser.parse(eventContent.trim());
        if (entry != null) {
            entry.setSource(filePath);
            boolean matches = filters.stream().allMatch(filter -> filter.matches(entry));
            if (matches) {
                for (LogAggregator aggregator : aggregators) {
                    aggregator.process(entry); // Using your exact method signature
                }
            }
        }
    }
}
