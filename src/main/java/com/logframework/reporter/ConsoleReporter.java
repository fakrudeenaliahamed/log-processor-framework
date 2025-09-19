package com.logframework.reporter;

import com.logframework.dto.ResultDTO;
import java.util.List;

public class ConsoleReporter implements LogReporter {

    @Override
    public void report(ResultDTO data) {
        if (data == null) {
            System.out.println("No data to report.");
            return;
        }
        System.out.println("\n=== " + data.getTitle() + " ===");

        List<String> headers = data.getHeaders();
        List<List<String>> rows = data.getData();

        // Print headers
        if (headers != null && !headers.isEmpty()) {
            System.out.println(String.join(" | ", headers));
            System.out.println("-".repeat(String.join(" | ", headers).length()));
        }

        // Print data rows
        if (rows != null && !rows.isEmpty()) {
            for (List<String> row : rows) {
                System.out.println(String.join(" | ", row));
            }
        } else {
            System.out.println("(No data)");
        }
    }
    @Override
    public void setOutputDirectory(String outputDirectory) {
        // ConsoleReporter does not use output directory
    }
}
