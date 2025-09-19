package com.logframework.aggregator;

import com.logframework.dto.ResultDTO;
import com.logframework.filter.Description;
import com.logframework.filter.Parameter;
import com.logframework.model.LogEntry;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

@Description(
    "Aggregates and calculates the error rate (percentage of ERROR log entries) over time buckets. " +
    "The bucket duration is controlled by the bucketFormatter pattern. " +
    "For example, with the default pattern 'uuuu-MM-dd''T''HH:mm', all log entries within the same minute " +
    "are grouped together. " +
    "Example output: {\"2025-09-18T16:15\": 25.0, \"2025-09-18T16:16\": 0.0} means 25% error rate at 16:15, 0% at 16:16."
)
public class ErrorRateOverTimeAggregator implements LogAggregator {
    private static final Logger logger = Logger.getLogger(ErrorRateOverTimeAggregator.class.getName());

    // Map of time bucket (e.g., "2025-09-18T16:15") to [totalCount, errorCount]
    private final Map<String, int[]> bucketCounts = new TreeMap<>();

    @Parameter(
        "DateTimeFormatter pattern for time buckets. Controls the bucket duration (e.g., per minute, hour, or day). " +
        "Examples: 'uuuu-MM-dd''T''HH:mm' (per minute), 'uuuu-MM-dd''T''HH' (per hour), 'uuuu-MM-dd' (per day)."
    )
    private DateTimeFormatter bucketDuration = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH");

    public void setBucketDuration(String bucketDuration) {
        logger.info("Setting bucketDuration pattern to: " + bucketDuration);
        this.bucketDuration = DateTimeFormatter.ofPattern(bucketDuration);
    }

    @Override
    public void process(LogEntry entry) {
        OffsetDateTime ts = entry.getTimestamp();
        if (ts == null) {
            logger.fine("LogEntry timestamp is null, skipping entry.");
            return;
        }
        String bucket = ts.format(bucketDuration);

        int[] counts = bucketCounts.computeIfAbsent(bucket, k -> new int[3]);
        counts[0]++; // total count
        if ("ERROR".equalsIgnoreCase(entry.getLevel())) {
            counts[1]++; // error count
            logger.finer("Incremented error count for bucket: " + bucket);
        }
        logger.finest("Processed entry for bucket: " + bucket + " | Total: " + counts[0] + ", Errors: " + counts[1]);
    }

    @Override
    public ResultDTO getResult() {
        logger.info("Generating ResultDTO for ErrorRateOverTimeAggregator.");
        List<String> headers = Arrays.asList("Time Bucket", "Total", "Errors", "Error Rate (%)");
        List<List<String>> data = new ArrayList<>();
        for (Map.Entry<String, int[]> e : bucketCounts.entrySet()) {
            int total = e.getValue()[0];
            int errors = e.getValue()[1];
            double rate = total > 0 ? (errors * 100.0 / total) : 0.0;
            data.add(Arrays.asList(
                e.getKey(),
                String.valueOf(total),
                String.valueOf(errors),
                String.format("%.2f", rate)
            ));
            logger.finer("Bucket: " + e.getKey() + " | Total: " + total + ", Errors: " + errors + ", Error Rate: " + rate);
        }
        return new ResultDTO("Error Rate Over Time", headers, data);
    }
}