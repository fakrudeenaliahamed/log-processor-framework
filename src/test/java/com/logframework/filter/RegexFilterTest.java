package com.logframework.filter;

import com.logframework.model.LogEntry;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class RegexFilterTest {

    @Test
    public void testMatchesFieldWithRegex() {
        RegexFilter filter = new RegexFilter();
        filter.setField("message");
        filter.setRegex("error");

        LogEntry entry = new LogEntry();
        entry.setMessage("This is an error message");
        assertTrue(filter.matches(entry));
    }

    @Test
    public void testDoesNotMatchWhenRegexNotFound() {
        RegexFilter filter = new RegexFilter();
        filter.setField("message");
        filter.setRegex("error");

        LogEntry entry = new LogEntry();
        entry.addAttribute("message", "All good here");

        assertFalse(filter.matches(entry));
    }

    @Test
    public void testDoesNotMatchWhenFieldMissing() {
        RegexFilter filter = new RegexFilter();
        filter.setField("message");
        filter.setRegex("error");

        LogEntry entry = new LogEntry();
        entry.addAttribute("other", "error in other field");

        assertFalse(filter.matches(entry));
    }

//    @Test
//    public void testMatchesCaseInsensitive() {
//        RegexFilter filter = new RegexFilter();
//        filter.setField("message");
//        filter.setRegex("ERROR");
//
//        LogEntry entry = new LogEntry();
//        entry.addAttribute("message", "this is an Error message");
//
//        assertTrue(filter.matches(entry));
//    }

    @Test
    public void testNoFieldOrRegexConfigured() {
        RegexFilter filter = new RegexFilter();
        LogEntry entry = new LogEntry();
        entry.addAttribute("message", "error");

        assertTrue(filter.matches(entry)); // Should not filter out anything if not configured
    }
}