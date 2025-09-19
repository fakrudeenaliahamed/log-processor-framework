package com.logframework.reporter;

import com.logframework.dto.ResultDTO;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

public class ConsoleReporterTest {

    @Test
    public void testReportToConsole() {
        // Prepare test data
        List<String> headers = Arrays.asList("Column1", "Column2");
        List<List<String>> data = Arrays.asList(
                Arrays.asList("Value1", "Value2"),
                Arrays.asList("Value3", "Value4")
        );
        ResultDTO result = new ResultDTO("Test Report", headers, data);

        // Capture console output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Report to console
        ConsoleReporter reporter = new ConsoleReporter();
        reporter.report(result);

        // Restore original System.out
        System.setOut(System.out);

        // Verify console output
        String output = outContent.toString();
        assertTrue(output.contains("=== Test Report ==="));
        assertTrue(output.contains("Column1 | Column2"));
        assertTrue(output.contains("Value1 | Value2"));
        assertTrue(output.contains("Value3 | Value4"));
    }
}