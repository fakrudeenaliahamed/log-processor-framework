package com.logframework.dto;

import java.util.List;

public class ResultDTO {
    private String title;
    private List<String> headers;
    private List<List<String>> data;

    public ResultDTO(String title, List<String> headers, List<List<String>> data) {
        this.title = title;
        this.headers = headers;
        this.data = data;
    }

    public String getTitle() {
        return title;
    }
    public List<String> getHeaders() {
        return headers;
    }
    public List<List<String>> getData() {
        return data;
    }

    
}
