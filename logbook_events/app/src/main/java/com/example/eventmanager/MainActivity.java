package com.example.eventmanager;

import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final List<EventEntry> events = new ArrayList<>();
    private EditText nameInput;
    private EditText dateInput;
    private EditText locationInput;
    private EditText organiserInput;
    private Spinner typeInput;
    private LinearLayout eventsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameInput = findViewById(R.id.eventName);
        dateInput = findViewById(R.id.eventDate);
        locationInput = findViewById(R.id.eventLocation);
        organiserInput = findViewById(R.id.eventOrganizer);
        typeInput = findViewById(R.id.eventType);
        eventsList = findViewById(R.id.eventsList);

        ArrayAdapter<String> types = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Conference", "Workshop", "Seminar", "Social", "Other"});
        types.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeInput.setAdapter(types);

        Button addEvent = findViewById(R.id.addEvent);
        addEvent.setOnClickListener(view -> addEvent());
        displayEvents();
    }

    private void addEvent() {
        String name = nameInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String organiser = organiserInput.getText().toString().trim();

        if (name.isEmpty()) {
            nameInput.setError("Please enter an event name.");
            nameInput.requestFocus();
            return;
        }
        if (date.isEmpty() || !EventEntry.isValidDate(date)) {
            dateInput.setError("Enter a real date in DD/MM/YYYY format.");
            dateInput.requestFocus();
            return;
        }
        if (location.isEmpty()) {
            locationInput.setError("Please enter a location.");
            locationInput.requestFocus();
            return;
        }
        if (organiser.isEmpty()) {
            organiserInput.setError("Please enter an organiser.");
            organiserInput.requestFocus();
            return;
        }

        events.add(new EventEntry(
                name, date, location, organiser, typeInput.getSelectedItem().toString()));
        clearForm();
        displayEvents();
    }

    private void clearForm() {
        nameInput.setText("");
        dateInput.setText("");
        locationInput.setText("");
        organiserInput.setText("");
        typeInput.setSelection(0);
    }

    private void displayEvents() {
        eventsList.removeAllViews();
        if (events.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No events have been added.");
            eventsList.addView(empty);
            return;
        }

        for (EventEntry event : events) {
            TextView row = new TextView(this);
            row.setText(
                    event.name + "\n" +
                    "Date: " + event.date + "\n" +
                    "Location: " + event.location + "\n" +
                    "Organiser: " + event.organiser + "\n" +
                    "Type: " + event.type);
            row.setTextColor(0xFF172B4D);
            row.setTextSize(16);
            row.setPadding(16, 16, 16, 16);
            row.setBackgroundColor(0xFFE8EEF8);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 12);
            row.setLayoutParams(params);
            row.setGravity(Gravity.START);
            eventsList.addView(row);
        }
    }
}
