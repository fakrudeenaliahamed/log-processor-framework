package com.logframework;

import com.logframework.aggregator.*;
import com.logframework.reporter.CSVReporter;
import com.logframework.reporter.ConsoleReporter;
import com.logframework.reporter.JSONReporter;


import java.util.Arrays;

public class LogFrameworkDemo {
    public static void main(String[] args) {
        LogProcessingManager framework = new LogProcessingManager();

//        // Add filters
//        framework.addFilter(new TimeRangeFilter(
//                OffsetDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC),
//                OffsetDateTime.of(2025, 12, 30, 13, 0, 0, 0, ZoneOffset.UTC)
//        ));

        //framework.addFilter(new RegexFilter("ERROR|WARN", "level"));
//        framework.addFilter(new RegexFilter("/api/orders", "message"));

        // Add aggregators
        framework.addAggregator(new LogLevelCountAggregator());
        framework.addAggregator(new TopEndpointsAggregator());
        framework.addAggregator(new ErrorRateOverTimeAggregator());

        // Process log files
        framework.processLogFiles(Arrays.asList(
                "/Users/fakrudeen/develop/hivo/log-processing-framework/src/main/resources/logs/apache_access.log"
        ));
        // Add reporters
        framework.addReporter(new ConsoleReporter());
        framework.addReporter(new JSONReporter());
        framework.addReporter(new CSVReporter());

        framework.generateReport();

    }
}
