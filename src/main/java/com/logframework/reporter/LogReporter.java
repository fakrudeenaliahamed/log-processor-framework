package com.logframework.reporter;

import com.logframework.dto.ResultDTO;

public interface LogReporter {
    void report(ResultDTO data);
    void setOutputDirectory(String outputDirectory);
}
