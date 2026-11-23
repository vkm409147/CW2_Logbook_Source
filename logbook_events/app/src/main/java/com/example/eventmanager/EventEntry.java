package com.example.eventmanager;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Pattern;

public final class EventEntry {

    private static final Pattern DATE_PATTERN =
            Pattern.compile("^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/\\d{4}$");

    public final String name;
    public final String date;
    public final String location;
    public final String organiser;
    public final String type;

    public EventEntry(String name, String date, String location, String organiser, String type) {
        this.name = name;
        this.date = date;
        this.location = location;
        this.organiser = organiser;
        this.type = type;
    }

    public static boolean isValidDate(String value) {
        if (value == null || !DATE_PATTERN.matcher(value).matches()) {
            return false;
        }
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT);
        format.setLenient(false);
        ParsePosition position = new ParsePosition(0);
        return format.parse(value, position) != null && position.getIndex() == value.length();
    }
}
