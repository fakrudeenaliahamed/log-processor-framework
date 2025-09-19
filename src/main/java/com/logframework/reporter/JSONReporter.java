package com.logframework.reporter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.logframework.dto.ResultDTO;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class JSONReporter implements LogReporter {

    private String outputDirectory = "reports"; // Default directory
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void report(ResultDTO data) {
        if (data == null) {
            System.out.println("No data to report.");
            return;
        }

        try {
            String filename = generateFilename(data.getTitle());
            writeJSON(data, filename);
            System.out.println("📄 JSON report generated: " + filename);
        } catch (IOException e) {
            System.err.println("❌ Error generating JSON report: " + e.getMessage());
        }
    }

    @Override
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    private void writeJSON(ResultDTO data, String filename) throws IOException {
        // Create output directory if it doesn't exist
        java.nio.file.Path outputPath = java.nio.file.Paths.get(outputDirectory);
        if (!java.nio.file.Files.exists(outputPath)) {
            java.nio.file.Files.createDirectories(outputPath);
        }

        // Write JSON file
        try (FileWriter writer = new FileWriter(outputDirectory + "/" + filename)) {
            String json = convertToJSONString(data);
            writer.write(json);
        }
    }

    private String convertToJSONString(ResultDTO data) throws IOException {
        // Create root JSON object
        ObjectNode root = objectMapper.createObjectNode();

        // Add metadata
        root.put("title", data.getTitle());
        root.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        root.put("recordCount", data.getData() != null ? data.getData().size() : 0);

        // Add headers
        ArrayNode headersArray = objectMapper.createArrayNode();
        if (data.getHeaders() != null) {
            for (String header : data.getHeaders()) {
                headersArray.add(header);
            }
        }
        root.set("headers", headersArray);

        // Add data
        ArrayNode dataArray = convertDataToJSONArray(data.getData(), data.getHeaders());
        root.set("data", dataArray);

        // Convert to pretty-printed JSON string
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
    }

    private ArrayNode convertDataToJSONArray(List<List<String>> data, List<String> headers) {
        ArrayNode dataArray = objectMapper.createArrayNode();

        if (data == null || data.isEmpty()) {
            return dataArray;
        }

        for (List<String> row : data) {
            ObjectNode rowObject = objectMapper.createObjectNode();

            // Convert row to object using headers as keys
            for (int i = 0; i < row.size() && i < headers.size(); i++) {
                rowObject.put(headers.get(i), row.get(i));
            }

            dataArray.add(rowObject);
        }

        return dataArray;
    }

    private String generateFilename(String title) {
        // Clean title for filename
        String cleanTitle = title.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        return cleanTitle + ".json";
    }

    // Getters and setters
    public String getOutputDirectory() {
        return outputDirectory;
    }
}
