package com.logframework.reporter;

import com.logframework.dto.ResultDTO;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;
import static org.mockito.Mockito.*;

public class CSVReporterTest {

    private static final String TEST_OUTPUT_DIR = "test-reports";
    private CSVReporter reporter;

    @BeforeMethod
    public void setUp() throws Exception {
        reporter = Mockito.spy(new CSVReporter()); // Spy the CSVReporter
        reporter.setOutputDirectory(TEST_OUTPUT_DIR);
        Files.createDirectories(Path.of(TEST_OUTPUT_DIR));
    }

    @AfterMethod
    public void tearDown() throws Exception {
        Files.walk(Path.of(TEST_OUTPUT_DIR))
                .map(Path::toFile)
                .forEach(File::delete);
        Files.deleteIfExists(Path.of(TEST_OUTPUT_DIR));
    }

    @Test
    public void testExportToFileWithStubbedFilename() throws Exception {
        // Stub the generateFilename method to return a fixed test file name
        doReturn("test_file.csv").when(reporter).generateFilename(anyString());

        // Prepare test data
        List<String> headers = Arrays.asList("Column1", "Column2");
        List<List<String>> data = Arrays.asList(
                Arrays.asList("Value1", "Value2"),
                Arrays.asList("Value3", "Value4")
        );
        ResultDTO result = new ResultDTO("Test Report", headers, data);

        // Export to CSV
        reporter.report(result);

        // Verify the file exists
        Path filePath = Path.of(TEST_OUTPUT_DIR, "test_file.csv");
        assertTrue(Files.exists(filePath), "CSV file should exist.");

        // Verify the file content
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            assertEquals(reader.readLine(), "Column1,Column2");
            assertEquals(reader.readLine(), "Value1,Value2");
            assertEquals(reader.readLine(), "Value3,Value4");
            assertNull(reader.readLine(), "File should only contain 3 lines.");
        }
    }


}