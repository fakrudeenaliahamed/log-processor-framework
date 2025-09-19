package com.logframework;

import com.logframework.aggregator.LogAggregator;
import com.logframework.aggregator.LogLevelCountAggregator;
import com.logframework.filter.RegexFilter;
import com.logframework.parser.JsonLogParser;
import com.logframework.reporter.ConsoleReporter;
import com.logframework.reporter.LogReporter;
import com.logframework.reporter.CSVReporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.testng.Assert.assertTrue;

public class LogProcessingFrameworkEndToEndTest {

    private static final String TEST_OUTPUT_DIR = "reports";

    @BeforeMethod
    public void setUp() throws Exception {
        Files.createDirectories(Path.of(TEST_OUTPUT_DIR));
    }

    @AfterMethod
    public void tearDown() throws Exception {
        // Clean up test directory
        Files.walk(Path.of(TEST_OUTPUT_DIR))
                .map(Path::toFile)
                .forEach(File::delete);
        Files.deleteIfExists(Path.of(TEST_OUTPUT_DIR));
    }

    @Test
    public void testEndToEndProcessing() throws Exception {
        // Initialize the framework
        LogProcessingManager framework = new LogProcessingManager();

        // Register a parser
        framework.registerParser(new JsonLogParser());

        // Add a filter to only include logs with "error" in the message
        RegexFilter filter = new RegexFilter();
        filter.setField("message");
        filter.setRegex("error");
        framework.addFilter(filter);

        // Add an aggregator to count log levels
        LogLevelCountAggregator aggregator = new LogLevelCountAggregator();
        framework.addAggregator(aggregator);

        // Add a reporter to export results to a CSV file
        CSVReporter csvReporter = new CSVReporter();
        csvReporter.setOutputDirectory(TEST_OUTPUT_DIR);
        framework.addReporter(csvReporter);

        // Add a console reporter for verification
        ConsoleReporter consoleReporter = new ConsoleReporter();
        framework.addReporter(consoleReporter);

        // Process a sample log file
        String testLogFile = "src/test/resources/logs/json-log.json";
        framework.processLogFiles(List.of(testLogFile));
        
         for (LogAggregator agg : framework.getAggregators()) {
            for (LogReporter reporter : framework.getReporters()) {
                reporter.setOutputDirectory(TEST_OUTPUT_DIR);
                reporter.report(agg.getResult());
            }
        }

        // Verify the CSV file is created
        Path csvFilePath = Path.of(TEST_OUTPUT_DIR, "Log_Level_Counts.csv");
        assertTrue(Files.exists(csvFilePath), "CSV report file should exist.");

        // Verify the content of the CSV file (optional, for additional validation)
        List<String> lines = Files.readAllLines(csvFilePath);
        assertTrue(lines.size() > 1, "CSV file should contain data.");
        assertTrue(lines.get(0).contains("Level,Count"), "CSV file should have headers.");
    }
}