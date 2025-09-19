# Log Processing Framework

The **Log Processing Framework** is a modular and extensensible Java-based framework for processing, filtering, aggregating, and reporting log data. It supports various log formats and provides tools for analyzing logs, generating reports, and visualizing results.

---

## Features

- **Log Parsing**: Supports multiple log formats (e.g., JSON, Apache Access Logs, Spring Boot Logs).
- **Filtering**: Apply filters to process only relevant log entries (e.g., regex-based filtering, time range filtering).
- **Aggregation**: Aggregate log data (e.g., error rates, log level counts, top endpoints).
- **Reporting**: Generate reports in various formats (e.g., Console, CSV, JSON).
- **Interactive Mode**: Use the `InteractiveLogProcessingManager` to interactively process logs.
- **Extensibility**: Easily add custom parsers, filters, aggregators, and reporters.

---

## Project Structure

```
src/
├── main/
│   ├── java/com/logframework/
│   │   ├── ConfigLoader.java                # Loads configuration files
│   │   ├── LogProcessingManager.java        # Core framework for processing logs
│   │   ├── InteractiveLogProcessingManager.java # Interactive demo for processing logs
│   │   ├── aggregator/                      # Aggregators for log data
│   │   ├── filter/                          # Filters for log entries
│   │   ├── parser/                          # Parsers for different log formats
│   │   ├── reporter/                        # Reporters for exporting results
│   ├── resources/
│   │   ├── log-processor.config             # Configuration file for the framework
│   │   ├── logs/                            # Sample log files
├── test/
│   ├── java/com/logframework/               # Unit and integration tests
│   ├── resources/logs/                      # Test log files
```

---

## Getting Started

### Prerequisites

- **Java 11+**
- **Maven** (for building and running the project)

---

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/your-repo/log-processing-framework.git
   cd log-processing-framework
   ```

2. Build the project:

   ```bash
   mvn clean install
   ```

3. Run the tests:
   ```bash
   mvn test
   ```

---

## Usage

### **LogProcessingManager**


## Interactive Log Processing

The `InteractiveLogProcessingManager` class provides an interactive way to process logs. It allows users to:

- Select log files to process.
- Choose filters, aggregators, and reporters dynamically.
- View results in real-time.


## How to Execute "Log Processor"

Once the uber-jar is created using the Maven Shade Plugin, you can run the `InteractiveLogProcessingManager` directly using the `java -jar` command.

1. Build the uber-jar:

   ```bash
   mvn clean package
   ```

2. Run the uber-jar:

   ```bash
   java -jar target/log-processing-framework-1.0-SNAPSHOT-shaded.jar
   ```

3. Follow the prompts in the interactive manager.

4. Follow the prompts to:

    - Select log files from directory.
    - Choose filters (e.g., regex-based filtering).
    - Choose aggregators (e.g., log level counts, error rates).
    - Choose reporters (e.g., console, CSV, JSON).

5. View the results in the console or in the specified output directory in json/csv.

**Example Interaction:**

```
Welcome to the Interactive Log Processing Manager!

📂 STEP 1: Select Log Files

Available log files:
  1. access.log
  2. json-log.json
  3. multiline.log

Select log file(s) (comma-separated numbers): 1,2

🔍 STEP 2: Select Filters (Optional)

🧩 Available filters:
  1. RegexFilter
  2. TimeRangeFilter

Select filter(s) (comma-separated numbers): 1,2

🔧 Configuring parameters for: RegexFilter
Enter value for field (The field to apply the regex on (e.g., message, level, or attribute name).): message
Enter value for regex (The regex pattern to match.): ERROR|WARN
✅ Added RegexFilter

🔧 Configuring parameters for: TimeRangeFilter
Enter value for startTime (The start time for filtering log entries (e.g., 2025-01-01 10:30:00).): 2025-01-01 10:00:00
Enter value for endTime (The end time for filtering log entries (e.g., 2025-01-01 12:30:00).): 2025-01-01 12:00:00
✅ Added TimeRangeFilter

📊 STEP 3: Select Aggregators (Optional)

🔢 Available aggregators:
  1. LogLevelCountAggregator
  2. ErrorRateOverTimeAggregator
  3. TopEndpointsAggregator

Select aggregator(s) (comma-separated numbers): 1,3

📤 STEP 4: Select Reporters

📂 Available reporters:
  1. ConsoleReporter
  2. CSVReporter
  3. JSONReporter

Select reporter(s) (comma-separated numbers): 1,2

Processing selected log files with the chosen filters, aggregators, and reporters...

Results:
- LogLevelCountAggregator: {ERROR=10, WARN=5, INFO=20}
- TopEndpointsAggregator: [/api/v1/resource1, /api/v1/resource2]
```

---

## Key Components

### 1. **LogProcessingManager**

- The main class for orchestrating the log processing pipeline.
- Allows registration of parsers, filters, aggregators, and reporters.
- Processes log files and generates reports.

### 2. **InteractiveLogProcessingManager**

- Provides an interactive interface for processing logs.
- Useful for testing and exploring the framework's capabilities.

### 3. **Parsers**

- **Purpose**: Parse log files into structured `LogEntry` objects.
- **Available Parsers**:
  - `JsonLogParser`: Parses JSON logs.
  - `ApacheAccessLogParser`: Parses Apache access logs.
  - `SpringBootLogParser`: Parses Spring Boot logs.

### 4. **Filters**

- **Purpose**: Filter log entries based on specific criteria.
- **Available Filters**:
  - `RegexFilter`: Filters logs based on regex patterns.
  - `TimeRangeFilter`: Filters logs within a specific time range.

### 5. **Aggregators**

- **Purpose**: Aggregate log data for analysis.
- **Available Aggregators**:
  - `LogLevelCountAggregator`: Counts log entries by log level.
  - `ErrorRateOverTimeAggregator`: Calculates error rates over time.
  - `TopEndpointsAggregator`: Finds the most accessed endpoints.

### 6. **Reporters**

- **Purpose**: Export aggregated results.
- **Available Reporters**:
  - `ConsoleReporter`: Prints results to the console.
  - `CSVReporter`: Exports results to CSV files.
  - `JSONReporter`: Exports results to JSON files.

---

