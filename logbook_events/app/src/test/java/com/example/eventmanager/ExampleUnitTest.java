package com.example.eventmanager;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void acceptsValidLeapDay() {
        assertTrue(EventEntry.isValidDate("29/02/2024"));
    }

    @Test
    public void rejectsImpossibleDatesAndWrongFormats() {
        assertFalse(EventEntry.isValidDate("29/02/2025"));
        assertFalse(EventEntry.isValidDate("2025-02-28"));
    }
}