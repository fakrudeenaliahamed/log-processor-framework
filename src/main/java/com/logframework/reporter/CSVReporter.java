package com.logframework.reporter;


import com.logframework.dto.ResultDTO;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class CSVReporter implements LogReporter {

    private String outputDirectory = "reports"; // Default directory

    @Override
    public void report(ResultDTO data) {

        if (data == null) {
            System.out.println("No data to report.");
            return;
        }
        try {
            String filename = generateFilename(data.getTitle());
            writeCSV(data, filename);
            System.out.println("📄 CSV report generated: " + filename);
        } catch (IOException e) {
            System.err.println("❌ Error generating CSV report: " + e.getMessage());
        }
    }

    @Override
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    private void writeCSV(ResultDTO data, String filename) throws IOException {
        // Create output directory if it doesn't exist
        java.nio.file.Path outputPath = java.nio.file.Paths.get(outputDirectory);
        if (!java.nio.file.Files.exists(outputPath)) {
            java.nio.file.Files.createDirectories(outputPath);
        }

        // Write CSV file
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputDirectory + "/" + filename))) {
            
            // Write headers
            List<String> headers = data.getHeaders();
            if (headers != null && !headers.isEmpty()) {
                writer.println(String.join(",", escapeCSVValues(headers)));
            }

            // Write data rows
            List<List<String>> rows = data.getData();
            if (rows != null && !rows.isEmpty()) {
                for (List<String> row : rows) {
                    writer.println(String.join(",", escapeCSVValues(row)));
                }
            }
        }
    }

    private List<String> escapeCSVValues(List<String> values) {
        return values.stream()
                .map(this::escapeCSVValue)
                .collect(java.util.stream.Collectors.toList());
    }

    private String escapeCSVValue(String value) {
        if (value == null) {
            return "";
        }
        
        // If value contains comma, newline, or quotes, wrap in quotes and escape internal quotes
        if (value.contains(",") || value.contains("\n") || value.contains("\r") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }

    String generateFilename(String title) {
        // Clean title for filename
        String cleanTitle = title.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        return cleanTitle + ".csv";
        
    }

    // Getters and setters
    public String getOutputDirectory() {
        return outputDirectory;
    }

}

