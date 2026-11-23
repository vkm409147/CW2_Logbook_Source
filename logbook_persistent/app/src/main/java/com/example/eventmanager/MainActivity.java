package com.example.eventmanager;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private LinearLayout eventsContainer;
    private Button btnSave, btnCancelEdit;
    private int editingEventId = -1;

    private EditText edtName, edtLocation, edtDate, edtTime;
    private EditText edtOrganizer, edtEmail, edtPhone, edtDescription;
    private EditText edtSearchKeyword;
    private Spinner spEventStatus;
    private ArrayAdapter<String> statusAdapter;

    private static final Pattern DATE_PATTERN =
            Pattern.compile("^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/\\d{4}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{9,15}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);

        // Ánh xạ form tạo sự kiện
        edtName = findViewById(R.id.edtName);
        edtLocation = findViewById(R.id.edtLocation);
        edtDate = findViewById(R.id.edtDate);
        edtTime = findViewById(R.id.edtTime);
        edtOrganizer = findViewById(R.id.edtOrganizer);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtDescription = findViewById(R.id.edtDescription);
        spEventStatus = findViewById(R.id.spEventStatus);
        statusAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Upcoming", "Completed", "Cancelled"});
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEventStatus.setAdapter(statusAdapter);

        // Ánh xạ phần tìm kiếm
        edtSearchKeyword = findViewById(R.id.edtSearchKeyword);
        Button btnSearch = findViewById(R.id.btnSearch);
        Button btnResetSearch = findViewById(R.id.btnResetSearch);

        eventsContainer = findViewById(R.id.eventsContainer);

        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> validateAndConfirmEvent());
        btnCancelEdit = findViewById(R.id.btnCancelEdit);
        btnCancelEdit.setOnClickListener(v -> clearFields());

        // Sự kiện nút Tìm kiếm
        btnSearch.setOnClickListener(v -> {
            String keyword = edtSearchKeyword.getText().toString().trim();
            if (keyword.isEmpty()) {
                Toast.makeText(this, "Please enter a search keyword", Toast.LENGTH_SHORT).show();
                return;
            }
            performSearch(keyword);
        });

        // Sự kiện nút Reset tìm kiếm (hiển thị lại toàn bộ)
        btnResetSearch.setOnClickListener(v -> {
            edtSearchKeyword.setText("");
            displayEvents();
        });

        displayEvents();
    }

    private void validateAndConfirmEvent() {
        String name = edtName.getText().toString().trim();
        String location = edtLocation.getText().toString().trim();
        String date = edtDate.getText().toString().trim();
        String time = edtTime.getText().toString().trim();
        String organizer = edtOrganizer.getText().toString().trim();
        String status = spEventStatus.getSelectedItem().toString();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();

        if (name.isEmpty()) { edtName.setError("Required"); edtName.requestFocus(); return; }
        if (location.isEmpty()) { edtLocation.setError("Required"); edtLocation.requestFocus(); return; }
        if (date.isEmpty()) { edtDate.setError("Required"); edtDate.requestFocus(); return; }
        if (time.isEmpty()) { edtTime.setError("Required"); edtTime.requestFocus(); return; }
        if (organizer.isEmpty()) { edtOrganizer.setError("Required"); edtOrganizer.requestFocus(); return; }

        if (!isValidDate(date)) {
            edtDate.setError("Invalid format. Use DD/MM/YYYY");
            edtDate.requestFocus();
            return;
        }

        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            edtEmail.setError("Invalid email format");
            edtEmail.requestFocus();
            return;
        }

        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            edtPhone.setError("Invalid phone format");
            edtPhone.requestFocus();
            return;
        }

        String summary = "Name: " + name + "\n" +
                "Location: " + location + "\n" +
                "Date: " + date + "\n" +
                "Time: " + time + "\n" +
                "Organizer: " + organizer + "\n" +
                "Status: " + status + "\n" +
                "Description: " + optionalValue(description) + "\n" +
                "Contact email: " + optionalValue(email) + "\n" +
                "Contact phone: " + optionalValue(phone);

        new AlertDialog.Builder(this)
                .setTitle("Review Event Details")
                .setMessage("Please review information before saving:\n\n" + summary)
                .setNegativeButton("Back to Edit", (dialog, which) -> dialog.dismiss())
                .setPositiveButton(editingEventId == -1 ? "Confirm & Save" : "Confirm Update",
                        (dialog, which) -> saveEventToDB(
                                name, location, date, time, organizer, status,
                                description, email, phone))
                .show();
    }

    private boolean isValidDate(String value) {
        if (!DATE_PATTERN.matcher(value).matches()) {
            return false;
        }
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT);
        format.setLenient(false);
        ParsePosition position = new ParsePosition(0);
        return format.parse(value, position) != null && position.getIndex() == value.length();
    }

    private String optionalValue(String value) {
        return value.isEmpty() ? "Not provided" : value;
    }

    private void saveEventToDB(String name, String location, String date, String time,
                               String organizer, String status, String description, String email, String phone) {
        boolean saved = editingEventId == -1
                ? databaseHelper.addEvent(name, location, date, time, organizer, status,
                        description, email, phone)
                : databaseHelper.updateEvent(editingEventId, name, location, date, time,
                        organizer, status, description, email, phone);

        if (saved) {
            Toast.makeText(this, editingEventId == -1
                    ? "Event saved successfully" : "Event updated successfully", Toast.LENGTH_SHORT).show();
            clearFields();
            displayEvents();
        } else {
            Toast.makeText(this, "Save failed. Check for an event with the same name and date.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void displayEvents() {
        eventsContainer.removeAllViews();

        try (Cursor cursor = databaseHelper.getAllEvents()) {
            if (cursor == null || cursor.getCount() == 0) {
                TextView empty = new TextView(this);
                empty.setText("No events saved yet.");
                empty.setTextSize(16);
                empty.setPadding(0, 16, 0, 16);
                eventsContainer.addView(empty);
                return;
            }

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("event_date"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("event_time"));
                String organizer = cursor.getString(cursor.getColumnIndexOrThrow("organizer"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("event_status"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("contact_email"));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow("contact_phone"));

                addEventRow(id, name, location, date, time, organizer, status,
                        description, email, phone);
            }
        }
    }

    // Hàm thực hiện tìm kiếm sự kiện theo từ khóa
    private void performSearch(String keyword) {
        eventsContainer.removeAllViews();

        try (Cursor cursor = databaseHelper.searchEvents(keyword)) {
            if (cursor == null || cursor.getCount() == 0) {
                TextView empty = new TextView(this);
                empty.setText("No matching events found.");
                empty.setTextSize(16);
                empty.setPadding(0, 16, 0, 16);
                eventsContainer.addView(empty);
                return;
            }

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("event_date"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("event_time"));
                String organizer = cursor.getString(cursor.getColumnIndexOrThrow("organizer"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("event_status"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("contact_email"));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow("contact_phone"));

                addEventRow(id, name, location, date, time, organizer, status,
                        description, email, phone);
            }
        }
    }

    private void addEventRow(int id, String name, String location, String date, String time,
                             String organizer, String status, String description, String email, String phone) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(16, 12, 16, 12);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 8, 0, 8);
        card.setLayoutParams(cardParams);
        card.setBackgroundColor(0xFFE8EEF8);

        int sessionCount = databaseHelper.getSessionCountForEvent(id);

        TextView details = new TextView(this);
        details.setText(
                name + "\n" +
                        "Location: " + location + "\n" +
                        "Date: " + date + " | Time: " + time + "\n" +
                        "Organizer: " + organizer + "\n" +
                        "Status: " + status + "\n" +
                        "Total Sessions: " + sessionCount
        );
        details.setTextSize(16);
        details.setTextColor(0xFF172B4D);
        card.addView(details);

        Button btnManageSessions = new Button(this);
        btnManageSessions.setText("Manage Sessions");
        btnManageSessions.setOnClickListener(v -> showSessionsDialog(id, name));
        card.addView(btnManageSessions);

        Button btnDetails = new Button(this);
        btnDetails.setText("View Full Details");
        btnDetails.setOnClickListener(v -> showEventDetails(
                name, location, date, time, organizer, status, description, email, phone));
        card.addView(btnDetails);

        Button btnEdit = new Button(this);
        btnEdit.setText("Edit Event");
        btnEdit.setOnClickListener(v -> loadEventForEditing(id));
        card.addView(btnEdit);

        Button btnDelete = new Button(this);
        btnDelete.setText("Delete Event");
        btnDelete.setOnClickListener(v -> confirmDelete(id, name));

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        buttonParams.gravity = Gravity.END;
        btnDelete.setLayoutParams(buttonParams);

        card.addView(btnDelete);
        eventsContainer.addView(card);
    }

    private void showEventDetails(String name, String location, String date, String time,
                                 String organizer, String status, String description, String email, String phone) {
        String details = "Name: " + name + "\n" +
                "Location: " + location + "\n" +
                "Date: " + date + "\n" +
                "Time: " + time + "\n" +
                "Organizer: " + organizer + "\n" +
                "Status: " + status + "\n" +
                "Description: " + optionalValue(description == null ? "" : description) + "\n" +
                "Contact email: " + optionalValue(email == null ? "" : email) + "\n" +
                "Contact phone: " + optionalValue(phone == null ? "" : phone);
        new AlertDialog.Builder(this)
                .setTitle("Event Details")
                .setMessage(details)
                .setPositiveButton("Close", null)
                .show();
    }

    private void loadEventForEditing(int eventId) {
        try (Cursor cursor = databaseHelper.getEvent(eventId)) {
            if (cursor == null || !cursor.moveToFirst()) {
                Toast.makeText(this, "Event no longer exists.", Toast.LENGTH_SHORT).show();
                displayEvents();
                return;
            }
            editingEventId = eventId;
            edtName.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            edtLocation.setText(cursor.getString(cursor.getColumnIndexOrThrow("location")));
            edtDate.setText(cursor.getString(cursor.getColumnIndexOrThrow("event_date")));
            edtTime.setText(cursor.getString(cursor.getColumnIndexOrThrow("event_time")));
            edtOrganizer.setText(cursor.getString(cursor.getColumnIndexOrThrow("organizer")));
            String status = cursor.getString(cursor.getColumnIndexOrThrow("event_status"));
            int statusPosition = statusAdapter.getPosition(status);
            spEventStatus.setSelection(Math.max(statusPosition, 0));
            edtDescription.setText(cursor.getString(cursor.getColumnIndexOrThrow("description")));
            edtEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow("contact_email")));
            edtPhone.setText(cursor.getString(cursor.getColumnIndexOrThrow("contact_phone")));
            btnSave.setText("Update Event");
            btnCancelEdit.setVisibility(View.VISIBLE);
            edtName.requestFocus();
        }
    }

    private void showSessionsDialog(int eventId, String eventName) {
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        TextView title = new TextView(this);
        title.setText("Sessions for: " + eventName);
        title.setTextSize(18);
        title.setPadding(0, 0, 0, 10);
        layout.addView(title);

        final EditText edtSessionSearch = new EditText(this);
        edtSessionSearch.setHint("Search session name or description");
        layout.addView(edtSessionSearch);

        LinearLayout searchButtons = new LinearLayout(this);
        searchButtons.setOrientation(LinearLayout.HORIZONTAL);
        Button btnSearchSessions = new Button(this);
        btnSearchSessions.setText("Search");
        Button btnResetSessions = new Button(this);
        btnResetSessions.setText("Reset");
        searchButtons.addView(btnSearchSessions);
        searchButtons.addView(btnResetSessions);
        layout.addView(searchButtons);

        LinearLayout sessionListLayout = new LinearLayout(this);
        sessionListLayout.setOrientation(LinearLayout.VERTICAL);

        final Runnable[] reloadSessions = new Runnable[1];
        reloadSessions[0] = () -> {
            sessionListLayout.removeAllViews();
            String keyword = edtSessionSearch.getText().toString().trim();
            try (Cursor cursor = keyword.isEmpty()
                    ? databaseHelper.getSessionsForEvent(eventId)
                    : databaseHelper.searchSessions(eventId, keyword)) {
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        int sessionId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                        String sName = cursor.getString(cursor.getColumnIndexOrThrow("session_name"));
                        String sTime = cursor.getString(cursor.getColumnIndexOrThrow("session_time"));
                        String sType = cursor.getString(cursor.getColumnIndexOrThrow("session_type"));
                        String speaker = cursor.getString(cursor.getColumnIndexOrThrow("speaker"));
                        String room = cursor.getString(cursor.getColumnIndexOrThrow("room"));
                        String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));

                        LinearLayout sessionRow = new LinearLayout(this);
                        sessionRow.setOrientation(LinearLayout.VERTICAL);
                        sessionRow.setPadding(0, 8, 0, 8);

                        TextView sessionDetails = new TextView(this);
                        sessionDetails.setText(
                                sName + " (" + sType + ") at " + sTime + "\n" +
                                "Speaker: " + optionalValue(speaker == null ? "" : speaker) + "\n" +
                                "Room: " + optionalValue(room == null ? "" : room) + "\n" +
                                "Description: " + optionalValue(description == null ? "" : description));
                        sessionRow.addView(sessionDetails);

                        LinearLayout actions = new LinearLayout(this);
                        actions.setOrientation(LinearLayout.HORIZONTAL);
                        Button btnEditSession = new Button(this);
                        btnEditSession.setText("Edit");
                        btnEditSession.setOnClickListener(v -> showSessionDialog(
                                eventId, sessionId, sName, sTime, speaker, room, sType, description,
                                reloadSessions[0]));
                        Button btnDeleteSession = new Button(this);
                        btnDeleteSession.setText("Delete");
                        btnDeleteSession.setOnClickListener(v -> new AlertDialog.Builder(this)
                                .setTitle("Delete Session")
                                .setMessage("Delete \"" + sName + "\"?")
                                .setNegativeButton("Cancel", null)
                                .setPositiveButton("Delete", (dialog, which) -> {
                                    if (databaseHelper.deleteSession(sessionId)) {
                                        Toast.makeText(this, "Session deleted", Toast.LENGTH_SHORT).show();
                                        reloadSessions[0].run();
                                    } else {
                                        Toast.makeText(this, "Could not delete session", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .show());
                        actions.addView(btnEditSession);
                        actions.addView(btnDeleteSession);
                        sessionRow.addView(actions);
                        sessionListLayout.addView(sessionRow);
                    }
                } else {
                    TextView noS = new TextView(this);
                    noS.setText(keyword.isEmpty() ? "No sessions added yet." : "No matching sessions found.");
                    sessionListLayout.addView(noS);
                }
            }
        };
        reloadSessions[0].run();
        layout.addView(sessionListLayout);

        btnSearchSessions.setOnClickListener(v -> reloadSessions[0].run());
        btnResetSessions.setOnClickListener(v -> {
            edtSessionSearch.setText("");
            reloadSessions[0].run();
        });

        Button btnAddSession = new Button(this);
        btnAddSession.setText("+ Add New Session");
        btnAddSession.setOnClickListener(v -> showSessionDialog(
                eventId, -1, "", "", "", "", "Workshop", "", reloadSessions[0]));
        layout.addView(btnAddSession);

        new AlertDialog.Builder(this)
                .setView(layout)
                .setPositiveButton("Close", (dialog, which) -> {
                    dialog.dismiss();
                    displayEvents();
                })
                .show();
    }

    private void showSessionDialog(int eventId, int sessionId, String sessionName, String sessionTime,
                                   String speaker, String room, String sessionType, String description,
                                   Runnable onSessionChanged) {
        final LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(20, 20, 20, 20);

        final EditText edtSName = new EditText(this);
        edtSName.setHint("Session name *");
        edtSName.setText(sessionName == null ? "" : sessionName);
        form.addView(edtSName);

        final EditText edtSTime = new EditText(this);
        edtSTime.setHint("Time *");
        edtSTime.setText(sessionTime == null ? "" : sessionTime);
        form.addView(edtSTime);

        final EditText edtSSpeaker = new EditText(this);
        edtSSpeaker.setHint("Speaker (optional)");
        edtSSpeaker.setText(speaker == null ? "" : speaker);
        form.addView(edtSSpeaker);

        final EditText edtSRoom = new EditText(this);
        edtSRoom.setHint("Room (optional)");
        edtSRoom.setText(room == null ? "" : room);
        form.addView(edtSRoom);

        final Spinner spSessionType = new Spinner(this);
        String[] sessionTypes = {
                "Workshop", "Presentation", "Panel Discussion",
                "Networking", "Keynote", "Others"
        };
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, sessionTypes);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSessionType.setAdapter(typeAdapter);
        int selectedType = typeAdapter.getPosition(sessionType);
        spSessionType.setSelection(selectedType < 0 ? sessionTypes.length - 1 : selectedType);
        form.addView(spSessionType);

        final EditText edtSDescription = new EditText(this);
        edtSDescription.setHint("Description (optional)");
        edtSDescription.setMinLines(2);
        edtSDescription.setGravity(Gravity.TOP);
        edtSDescription.setText(description == null ? "" : description);
        form.addView(edtSDescription);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(sessionId == -1 ? "Add Session" : "Edit Session")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton(sessionId == -1 ? "Add" : "Save", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String sName = edtSName.getText().toString().trim();
                    String sTime = edtSTime.getText().toString().trim();

                    if (sName.isEmpty()) {
                        edtSName.setError("Please enter a session name");
                        edtSName.requestFocus();
                        return;
                    }
                    if (sTime.isEmpty()) {
                        edtSTime.setError("Please enter a session time");
                        edtSTime.requestFocus();
                        return;
                    }

                    String sType = spSessionType.getSelectedItem().toString();
                    boolean saved = sessionId == -1
                            ? databaseHelper.addSession(eventId, sName, sTime,
                                    edtSSpeaker.getText().toString().trim(),
                                    edtSRoom.getText().toString().trim(), sType,
                                    edtSDescription.getText().toString().trim())
                            : databaseHelper.updateSession(sessionId, eventId, sName, sTime,
                                    edtSSpeaker.getText().toString().trim(),
                                    edtSRoom.getText().toString().trim(), sType,
                                    edtSDescription.getText().toString().trim());
                    if (saved) {
                        Toast.makeText(this, sessionId == -1
                                ? "Session added" : "Session updated", Toast.LENGTH_SHORT).show();
                        onSessionChanged.run();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Could not save session", Toast.LENGTH_SHORT).show();
                    }
                }));
        dialog.show();
    }

    private void confirmDelete(int eventId, String eventName) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete \"" + eventName + "\"? (All related sessions will also be deleted)")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean deleted = databaseHelper.deleteEvent(eventId);
                    if (deleted) {
                        Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
                        displayEvents();
                    } else {
                        Toast.makeText(this, "Could not delete event", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void clearFields() {
        editingEventId = -1;
        edtName.setText("");
        edtLocation.setText("");
        edtDate.setText("");
        edtTime.setText("");
        edtOrganizer.setText("");
        spEventStatus.setSelection(0);
        edtEmail.setText("");
        edtPhone.setText("");
        edtDescription.setText("");
        btnSave.setText("Save Event");
        btnCancelEdit.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }
}