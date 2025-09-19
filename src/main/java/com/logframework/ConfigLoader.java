package com.logframework;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigLoader {
    private final Properties props = new Properties();
    private boolean loaded = false;

    public ConfigLoader() {
        load();
    }

    private void load() {
        // 1. Try environment variable
        String configPath = System.getenv("log-processor.config");
        if (configPath != null && !configPath.isBlank()) {
            try {
                props.load(Files.newBufferedReader(Path.of(configPath)));
                loaded = true;
            } catch (IOException e) {
                System.err.println("Failed to load log-processor.config from env path: " + configPath);
            }
        }

        // 2. Try classpath (inside JAR)
        if (!loaded) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("log-processor.config")) {
                if (in != null) {
                    props.load(in);
                    loaded = true;
                }
            } catch (IOException e) {
                System.err.println("Failed to load log-processor.config from classpath.");
            }
        }
    }

    public String getProperty(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public List<String> getList(String key) {
        String value = props.getProperty(key, "");
        if (value == null || value.trim().isEmpty()) return new ArrayList<>();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }


    public boolean isLoaded() {
        return loaded;
    }
}