package com.logframework;

import com.logframework.filter.*;
import com.logframework.aggregator.*;
import com.logframework.reporter.*;

import com.logframework.util.ConfigLoader;
import org.fusesource.jansi.Ansi.Color;
import org.fusesource.jansi.AnsiConsole;
import static org.fusesource.jansi.Ansi.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.*;
import java.util.stream.Collectors;

public class InteractiveLogProcessingManager {

    private Scanner scanner = new Scanner(System.in);
    private List<String> availableParsers = new ArrayList<>();
    private List<String> availableFilters = new ArrayList<>();
    private List<String> availableAggregators = new ArrayList<>();
    private List<String> availableReporters = new ArrayList<>();

    private ConfigLoader configLoader = new ConfigLoader();

    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        try {
            new InteractiveLogProcessingManager().run();
        } finally {
            AnsiConsole.systemUninstall();
        }
    }

    public void run(){
        loadConfiguration();

        configureLogging(configLoader.getProperty("reporter.outputFolder", "logs"));
        printWelcomeBanner();

        while (true) {
            LogProcessingManager framework = new LogProcessingManager(configLoader);

            // Step 1: Add log files
            List<String> logFiles = selectLogFiles();
            if (logFiles.isEmpty()) {
                printError("❌ No log files selected. Exiting...");
                break;
            }

            // Step 2: Select filters (optional)
            selectFilters(framework);

            // Step 3: Select aggregators
            selectAggregators(framework);

            // Step 4: Process logs
            processLogs(framework, logFiles);

            // Step 5: Select reporters and generate reports
            List<LogReporter> selectedReporters = selectReporters();

            generateReports(framework, selectedReporters);

            // Step 6: Ask to repeat or exit
            if (!askToRepeat()) {
                break;
            }
        }

        printMessage("👋 Goodbye! Thanks for using Log Processing Framework", Color.CYAN);
    }

    

    private void loadConfiguration() {
        if (configLoader.isLoaded()) {
            availableParsers = configLoader.getList("parsers");
            availableFilters = configLoader.getList("filters");
            availableAggregators = configLoader.getList("aggregator");
            availableReporters = configLoader.getList("reporter");

            printSuccess("✅ Configuration loaded successfully");
            printMessage("   Parsers: " + availableParsers.size() +
                    ", Filters: " + availableFilters.size() +
                    ", Aggregators: " + availableAggregators.size() +
                    ", Reporters: " + availableReporters.size(), Color.CYAN);
        } else {
            printError("⚠️ log-processor.config not found, using defaults");
        }
    }

    public static void configureLogging(String folder) {
        try {
            // Ensure the folder exists
            Path logDir = Path.of(folder);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }

            Logger rootLogger = Logger.getLogger("");
            // Remove default handlers
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }
            // Create a FileHandler that writes to the specified folder
            String logFile = logDir.resolve("log-processor.log").toString();
            FileHandler fileHandler = new FileHandler(logFile, true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);
            rootLogger.addHandler(fileHandler);

        } catch (IOException e) {
            Logger.getLogger(LogProcessingManager.class.getName())
                  .severe("Failed to configure file logging: " + e.getMessage());
        }
    }




    private List<String> parseConfigList(String configValue) {
        if (configValue == null || configValue.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(configValue.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private void printWelcomeBanner() {
        System.out.println(ansi().fg(Color.CYAN).a(
                "╔══════════════════════════════════════════════════════════════╗\n" +
                        "║                                                              ║\n" +
                        "║        🚀 LOG PROCESSING FRAMEWORK - STEP BY STEP MODE       ║\n" +
                        "║                                                              ║\n" +
                        "║      Fully dynamic - driven by parser.config                 ║\n" +
                        "║                                                              ║\n" +
                        "╚══════════════════════════════════════════════════════════════╝"
        ).reset());
    }

    private List<String> selectLogFiles() {
        printStep("📁 STEP 1: Select Log Files");
        List<String> selectedFiles = new ArrayList<>();

        while (true) {
            String pathInput = promptUser("Enter log file or directory path: ");
            if (pathInput.isEmpty()) {
                printError("❌ Path cannot be empty.");
                continue;
            }
            Path path = Paths.get(pathInput);
            if (Files.exists(path)) {
                if (Files.isRegularFile(path)) {
                    selectedFiles.add(path.toString());
                    printSuccess("✅ Added file: " + path);
                    break;
                } else if (Files.isDirectory(path)) {
                    try {
                        List<String> logFiles = Files.list(path)
                            .filter(p -> p.toString().endsWith(".log") || p.toString().endsWith(".txt"))
                            .map(Path::toString)
                            .sorted()
                            .collect(Collectors.toList());
                        if (logFiles.isEmpty()) {
                            printMessage("No .log or .txt files found in directory", Color.YELLOW);
                            continue;
                        }
                        selectedFiles.addAll(logFiles);
                        printSuccess("✅ Added " + logFiles.size() + " files from directory: " + path);
                        break;
                    } catch (IOException e) {
                        printError("Error reading directory: " + e.getMessage());
                    }
                } else {
                    printError("❌ Path is neither a file nor a directory.");
                }
            } else {
                printError("❌ Path does not exist: " + pathInput);
            }
        }
        return selectedFiles;
    }

    private void selectFilters(LogProcessingManager framework) {
        printStep("🔍 STEP 2: Select Filters (Optional)");

        printMessage("Filters help you narrow down which log entries to process.", Color.CYAN);
        String useFilters = promptUser("Do you want to add filters? (y/n): ");

        if (!"y".equalsIgnoreCase(useFilters) && !"yes".equalsIgnoreCase(useFilters)) {
            printMessage("⏭️ Skipping filters - will process all log entries", Color.YELLOW);
            return;
        }

        if (availableFilters.isEmpty()) {
            printError("❌ No filters available in configuration");
            return;
        }

        printMessage("\n🧩 Available filters:", Color.BLUE);
        for (int i = 0; i < availableFilters.size(); i++) {
            String className = availableFilters.get(i);
            String simpleName = getSimpleName(className);
            printMessage("  " + (i + 1) + ". " + simpleName, Color.BLUE);
        }

        String choice = promptUser("Select filter(s) (comma-separated numbers): ");

        try {
            // Parse comma-separated input
            String[] choices = choice.split(",");
            boolean addedAny = false;

            for (String c : choices) {
                int index = Integer.parseInt(c.trim()) - 1;

                if (index >= 0 && index < availableFilters.size()) {
                    String filterClassName = availableFilters.get(index);
                    printMessage("\n🔧 Configuring parameters for: " + getSimpleName(filterClassName), Color.CYAN);

                    if (addFilterDynamically(framework, filterClassName)) {
                        addedAny = true;
                    }
                } else {
                    printError("❌ Invalid selection: " + (index + 1));
                }
            }

            if (!addedAny) {
                printMessage("⚠️ No filters selected. All log entries will be processed.", Color.YELLOW);
            }
        } catch (NumberFormatException e) {
            printError("❌ Invalid input. Please enter comma-separated numbers.");
        }
    }

    private boolean addFilterDynamically(LogProcessingManager framework, String className) {
        try {
            Class<?> filterClass = Class.forName(className);
            String simpleName = getSimpleName(className);

            // Create an instance of the filter
            LogFilter filter = (LogFilter) filterClass.getDeclaredConstructor().newInstance();

            // Dynamically set values for fields annotated with @Parameter using setter methods
            for (var field : filterClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Parameter.class)) {
                    Parameter parameter = field.getAnnotation(Parameter.class);
                    String description = parameter.value();

                    // Prompt the user for the value
                    String input = promptUser("Enter value for " + field.getName() + " (" + description + "): ");

                    // Find and invoke the corresponding setter method
                    String setterName = "set" + capitalize(field.getName());
                    try {
                        var setterMethod = filterClass.getMethod(setterName, field.getType());
                        Object value = convertToFieldType(field.getType(), input);
                        setterMethod.invoke(filter, value);
                    } catch (NoSuchMethodException e) {
                        printError("❌ No setter method found for field: " + field.getName());
                    }
                }
            }

            // Add the filter to the framework
            framework.addFilter(filter);
            printSuccess("✅ Added " + simpleName);
            return true;

        } catch (Exception e) {
            printError("❌ Error creating filter: " + e.getMessage());
            return false;
        }
    }

    private void selectAggregators(LogProcessingManager framework) {
        printStep("📊 STEP 3: Select Aggregators");

        printMessage("Aggregators analyze your log data and generate insights.", Color.CYAN);

        if (availableAggregators.isEmpty()) {
            printError("❌ No aggregators available in configuration");
            return;
        }

        printMessage("\n📈 Available aggregators:", Color.BLUE);
        for (int i = 0; i < availableAggregators.size(); i++) {
            String className = availableAggregators.get(i);
            String simpleName = getSimpleName(className);
            printMessage("  " + (i + 1) + ". " + simpleName, Color.BLUE);
        }

        String choice = promptUser("Select aggregator(s) (comma-separated numbers): ");

        try {
            // Parse comma-separated input
            String[] choices = choice.split(",");
            boolean addedAny = false;

            for (String c : choices) {
                int index = Integer.parseInt(c.trim()) - 1;

                if (index >= 0 && index < availableAggregators.size()) {
                    String aggClassName = availableAggregators.get(index);
                    printMessage("\n🔧 Configuring parameters for: " + getSimpleName(aggClassName), Color.CYAN);

                    if (addAggregatorDynamically(framework, aggClassName)) {
                        addedAny = true;
                    }
                } else {
                    printError("❌ Invalid selection: " + (index + 1));
                }
            }

            if (!addedAny) {
                printMessage("⚠️ No aggregators selected. At least one aggregator is recommended.", Color.YELLOW);
            }
        } catch (NumberFormatException e) {
            printError("❌ Invalid input. Please enter comma-separated numbers.");
        }
    }

    private boolean addAggregatorDynamically(LogProcessingManager framework, String className) {
        try {
            Class<?> aggClass = Class.forName(className);
            String simpleName = getSimpleName(className);

            // Create an instance of the aggregator
            LogAggregator aggregator = (LogAggregator) aggClass.getDeclaredConstructor().newInstance();

            // Dynamically set values for fields annotated with @Parameter using setter methods
            for (var field : aggClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Parameter.class)) {
                    Parameter parameter = field.getAnnotation(Parameter.class);
                    String description = parameter.value();

                    // Prompt the user for the value
                    String input = promptUser("Enter value for " + field.getName() + " (" + description + "): ");

                    // Find and invoke the corresponding setter method
                    String setterName = "set" + capitalize(field.getName());
                    try {
                        var setterMethod = aggClass.getMethod(setterName, field.getType());
                        Object value = convertToFieldType(field.getType(), input);
                        setterMethod.invoke(aggregator, value);
                    } catch (NoSuchMethodException e) {
                        printError("❌ No setter method found for field: " + field.getName());
                    }
                }
            }

            // Add the aggregator to the framework
            framework.addAggregator(aggregator);
            printSuccess("✅ Added " + simpleName);
            return true;

        } catch (Exception e) {
            printError("❌ Error creating aggregator: " + e.getMessage());
            return false;
        }
    }

    private Object[] collectConstructorArguments(Constructor<?> constructor, String className) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();

        if (parameterTypes.length == 0) {
            return new Object[0]; // No arguments needed
        }

        System.out.println("📝 " + className + " Configuration:");
        
        // Special handling for known filter types
        if (className.equals("RegexFilter")) {
            return handleRegexFilterParameters();
        } else if (className.equals("TimeRangeFilter")) {
            return handleTimeRangeFilterParameters();
        }

        // Generic parameter collection for unknown types
        Object[] args = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> paramType = parameterTypes[i];
            String paramName = getParameterName(className, i, paramType);

            try {
                if (paramType == int.class || paramType == Integer.class) {
                    String input = promptUser(paramName + ": ");
                    if (input.isEmpty()) {
                        printError("❌ Parameter required");
                        return null;
                    }
                    args[i] = Integer.parseInt(input);
                } else if (paramType == String.class) {
                    String input = promptUser(paramName + ": ");
                    args[i] = input;
                } else if (paramType == boolean.class || paramType == Boolean.class) {
                    String input = promptUser(paramName + " (true/false): ");
                    args[i] = Boolean.parseBoolean(input);
                } else if (paramType == double.class || paramType == Double.class) {
                    String input = promptUser(paramName + ": ");
                    if (input.isEmpty()) {
                        printError("❌ Parameter required");
                        return null;
                    }
                    args[i] = Double.parseDouble(input);
                } else if (paramType == OffsetDateTime.class) {
                    String input = promptUser(paramName + " (YYYY-MM-DD HH:MM:SS): ");
                    if (input.isEmpty()) {
                        printError("❌ Parameter required");
                        return null;
                    }
                    args[i] = parseDateTime(input);
                } else {
                    printError("❌ Unsupported parameter type: " + paramType.getSimpleName());
                    return null;
                }
            } catch (Exception e) {
                printError("❌ Invalid input for " + paramName + ": " + e.getMessage());
                return null;
            }
        }

        return args;
    }

    private Object[] handleRegexFilterParameters() {
        System.out.println("🔎 RegexFilter Configuration:");
        System.out.println("This filter matches log entries using regular expressions.");
        System.out.println("Examples:");
        System.out.println("  Pattern: ERROR|WARN  Field: level    (matches ERROR or WARN log levels)");
        System.out.println("  Pattern: Exception   Field: message  (matches entries containing 'Exception')");
        System.out.println();
        
        try {
            String pattern = promptUser("Enter regex pattern: ");
            if (pattern.isEmpty()) {
                printError("❌ Pattern is required");
                return null;
            }
            
            String field = promptUser("Enter field to search (level/message): ");
            if (!field.equals("level") && !field.equals("message")) {
                printError("❌ Field must be 'level' or 'message'");
                return null;
            }
            
            return new Object[]{pattern, field};
        } catch (Exception e) {
            printError("❌ Error configuring RegexFilter: " + e.getMessage());
            return null;
        }
    }

    private Object[] handleTimeRangeFilterParameters() {
        System.out.println("📅 TimeRangeFilter Configuration:");
        System.out.println("This filter includes only log entries within a specific time range.");
        System.out.println("Format: YYYY-MM-DD HH:MM:SS (e.g., 2025-01-01 10:30:00)");
        System.out.println();
        
        try {
            String startStr = promptUser("Enter start time: ");
            if (startStr.isEmpty()) {
                printError("❌ Start time is required");
                return null;
            }
            
            String endStr = promptUser("Enter end time: ");
            if (endStr.isEmpty()) {
                printError("❌ End time is required");
                return null;
            }
            
            OffsetDateTime startTime = parseDateTime(startStr);
            OffsetDateTime endTime = parseDateTime(endStr);
            
            return new Object[]{startTime, endTime};
        } catch (DateTimeParseException e) {
            printError("❌ Invalid date format. Use: YYYY-MM-DD HH:MM:SS");
            return null;
        } catch (Exception e) {
            printError("❌ Error configuring TimeRangeFilter: " + e.getMessage());
            return null;
        }
    }

    private String getParameterName(String className, int paramIndex, Class<?> paramType) {
        // Smart parameter naming based on class and position
        if (className.contains("TopEndpointsAggregator")) {
            if (paramIndex == 0 && (paramType == int.class || paramType == Integer.class)) {
                return "Enter number of top endpoints to show (default 10)";
            }
        }
        
        // Generic fallback with better names
        if (paramType == String.class) {
            return "Enter text value for parameter " + (paramIndex + 1);
        } else if (paramType == int.class || paramType == Integer.class) {
            return "Enter integer value for parameter " + (paramIndex + 1);
        } else {
            return "Enter " + paramType.getSimpleName().toLowerCase() + " value for parameter " + (paramIndex + 1);
        }
    }

    private void processLogs(LogProcessingManager framework, List<String> logFiles) {
        printStep("⚡ STEP 4: Processing Logs");

        // Directly process the logs without asking for confirmation
        printMessage("🔄 Processing logs... Please wait...", Color.YELLOW);

        long startTime = System.currentTimeMillis();
        framework.processLogFiles(logFiles);
        long endTime = System.currentTimeMillis();

        printSuccess("✅ Processing completed successfully in " + (endTime - startTime) + "ms");
    }

    // NEW METHOD: Select multiple reporters
    private List<LogReporter> selectReporters() {
        printStep("📄 STEP 5: Select Report Types");

        if (availableReporters.isEmpty()) {
            printError("❌ No reporters available in configuration");
            return Arrays.asList(new ConsoleReporter()); // Fallback to console
        }

        List<LogReporter> selectedReporters = new ArrayList<>();

        printMessage("\n📊 Available report types:", Color.BLUE);
        for (int i = 0; i < availableReporters.size(); i++) {
            String className = availableReporters.get(i);
            String simpleName = getSimpleName(className);
            printMessage("  " + (i + 1) + ". " + simpleName, Color.BLUE);
        }

        String choice = promptUser("Select report type(s) (comma-separated numbers): ");

        try {
            // Parse comma-separated input
            String[] choices = choice.split(",");

            for (String c : choices) {
                int index = Integer.parseInt(c.trim()) - 1;

                if (index >= 0 && index < availableReporters.size()) {
                    String reporterClassName = availableReporters.get(index);
                    printMessage("\n🔧 Configuring parameters for: " + getSimpleName(reporterClassName), Color.CYAN);

                    LogReporter reporter = createReporter(reporterClassName);
                    if (reporter != null) {
                        selectedReporters.add(reporter);
                        printSuccess("✅ Added " + getSimpleName(reporterClassName));
                    }
                } else {
                    printError("❌ Invalid selection: " + (index + 1));
                }
            }
        } catch (NumberFormatException e) {
            printError("❌ Invalid input. Please enter comma-separated numbers.");
        }

        return selectedReporters;
    }

    private LogReporter createReporter(String className) {
        try {
            Class<?> reporterClass = Class.forName(className);
            
            // For file-based reporters, ask for output directory
            LogReporter reporter = (LogReporter) reporterClass.getDeclaredConstructor().newInstance();
            
            if (reporter instanceof CSVReporter || reporter instanceof JSONReporter) {
                String outputDir = promptUser("Enter output directory (default: reports): ");
                if (outputDir.isEmpty()) {
                    outputDir = "reports";
                }
                reporter.setOutputDirectory(outputDir);
            }
            
            return reporter;
        } catch (Exception e) {
            printError("❌ Error creating reporter: " + e.getMessage());
            return null;
        }
    }

    // NEW METHOD: Generate reports for all selected reporters
    private void generateReports(LogProcessingManager framework, List<LogReporter> reporters) {
        printStep("📊 Generating Reports");

        for (LogReporter reporter : reporters){
            framework.addReporter(reporter);
        }

        printMessage("🔄 Generating " + reporters.size() + " report type(s)...", Color.YELLOW);

            try {

                framework.generateReport();
            } catch (Exception e) {
                printError("❌ Error generating report: " + e.getMessage());
            }


        printSuccess("🎉 All reports generated successfully!");
    }

    private boolean askToRepeat() {
        printStep("🔄 STEP 6: What's Next?");

        System.out.println("Options:");
        System.out.println("1. Process another set of logs");
        System.out.println("2. Exit application");

        String choice = promptUser("Choose option (1-2): ");

        switch (choice) {
            case "1":
                printMessage("🔄 Starting new log processing session...\n", Color.CYAN);
                return true;
            case "2":
            default:
                return false;
        }
    }

    // Helper methods
    private String getSimpleName(String fullClassName) {
        return fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
    }

    private String promptUser(String prompt) {
        System.out.print(ansi().fg(Color.YELLOW).a(prompt).reset());
        return scanner.nextLine().trim();
    }

    private void printStep(String title) {
        System.out.println();
        System.out.println(ansi().fg(Color.MAGENTA).a("═".repeat(60)).reset());
        System.out.println(ansi().fg(Color.MAGENTA).bold().a(title).reset());
        System.out.println(ansi().fg(Color.MAGENTA).a("═".repeat(60)).reset());
    }

    private void printSuccess(String message) {
        System.out.println(ansi().fg(Color.GREEN).a(message).reset());
    }

    private void printError(String message) {
        System.out.println(ansi().fg(Color.RED).a(message).reset());
    }

    private void printMessage(String message, Color color) {
        System.out.println(ansi().fg(color).a(message).reset());
    }

    private OffsetDateTime parseDateTime(String dateTimeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return OffsetDateTime.of(
                java.time.LocalDateTime.parse(dateTimeStr, formatter),
                ZoneOffset.UTC
        );
    }

    private Object convertToFieldType(Class<?> fieldType, String input) {
        try {
            if (fieldType == int.class || fieldType == Integer.class) {
                return Integer.parseInt(input);
            } else if (fieldType == double.class || fieldType == Double.class) {
                return Double.parseDouble(input);
            } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                return Boolean.parseBoolean(input);
            } else if (fieldType == OffsetDateTime.class) {
                return parseDateTime(input);
            } else {
                return input; // Default to String
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid value for type " + fieldType.getSimpleName() + ": " + input);
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
