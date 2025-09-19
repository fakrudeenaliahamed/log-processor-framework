package com.logframework.aggregator;

import com.logframework.model.LogEntry;
import java.util.*;
import java.util.stream.Collectors;

import com.logframework.dto.ResultDTO;
import com.logframework.filter.Default;
import com.logframework.filter.Description;
import com.logframework.filter.Parameter;
import java.util.logging.Logger;

@Description(
    "Aggregates and returns the top N most frequently accessed endpoints (by 'path' attribute) in the logs. " +
    "Example output for topN=2: {\"/api/users\": 150, \"/api/orders\": 120}."
)
public class TopEndpointsAggregator implements LogAggregator {

    private static final Logger logger = Logger.getLogger(TopEndpointsAggregator.class.getName());

    @Parameter("Number of top endpoints to return.")
    @Default("10")
    private int topN;

    private Map<String, Long> endpointCounts = new HashMap<>();

    public void setTopN(int topN) {
        logger.info("Setting topN to: " + topN);
        this.topN = topN;
    }

    @Override
    public void process(LogEntry entry) {
        Object pathObj = entry.getAttribute("path");
        if (pathObj != null) {
            String path = pathObj.toString();
            endpointCounts.merge(path, 1L, Long::sum);
            logger.finest("Processed endpoint: " + path + " | Count: " + endpointCounts.get(path));
        } else {
            logger.fine("LogEntry missing 'path' attribute, skipping.");
        }
    }

    @Override
    public ResultDTO getResult() {
        logger.info("Generating result for TopEndpointsAggregator with topN = " + topN);
        Map<String, Long> topEndpoints = endpointCounts.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(topN)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        logger.fine("Top endpoints: " + topEndpoints);

        List<String> headers = Arrays.asList("Endpoint", "Count");
        List<List<String>> data = new ArrayList<>();
        for (Map.Entry<String, Long> entry : topEndpoints.entrySet()) {
            data.add(Arrays.asList(entry.getKey(), String.valueOf(entry.getValue())));
        }
        return new ResultDTO("Top " + topN + " Endpoints", headers, data);
    }

}
