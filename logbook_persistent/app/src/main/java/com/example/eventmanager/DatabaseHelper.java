package com.example.eventmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "event_manager.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_EVENTS = "events";
    public static final String TABLE_SESSIONS = "sessions";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createEventsTable =
                "CREATE TABLE " + TABLE_EVENTS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "location TEXT NOT NULL, " +
                        "event_date TEXT NOT NULL, " +
                        "event_time TEXT NOT NULL, " +
                        "organizer TEXT NOT NULL, " +
                        "event_status TEXT NOT NULL, " +
                        "description TEXT, " +
                        "contact_email TEXT, " +
                        "contact_phone TEXT, " +
                        "UNIQUE(name, event_date)" +
                        ")";
        db.execSQL(createEventsTable);

        String createSessionsTable =
                "CREATE TABLE " + TABLE_SESSIONS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "event_id INTEGER NOT NULL, " +
                        "session_name TEXT NOT NULL, " +
                        "session_time TEXT NOT NULL, " +
                        "speaker TEXT, " +
                        "room TEXT, " +
                        "session_type TEXT, " +
                        "description TEXT, " +
                        "FOREIGN KEY(event_id) REFERENCES " + TABLE_EVENTS + "(id) ON DELETE CASCADE" +
                        ")";
        db.execSQL(createSessionsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("PRAGMA foreign_keys=ON;");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    public boolean addEvent(String name, String location, String date, String time,
                            String organizer, String status, String description, String email, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.insert(TABLE_EVENTS, null, eventValues(
                name, location, date, time, organizer, status, description, email, phone)) != -1;
    }

    public boolean updateEvent(int eventId, String name, String location, String date, String time,
                               String organizer, String status, String description, String email, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            return db.update(TABLE_EVENTS, eventValues(
                    name, location, date, time, organizer, status, description, email, phone),
                    "id = ?", new String[]{String.valueOf(eventId)}) > 0;
        } catch (SQLiteConstraintException exception) {
            return false;
        }
    }

    private ContentValues eventValues(String name, String location, String date, String time,
                                      String organizer, String status, String description, String email, String phone) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("location", location);
        values.put("event_date", date);
        values.put("event_time", time);
        values.put("organizer", organizer);
        values.put("event_status", status);
        values.put("description", description);
        values.put("contact_email", email);
        values.put("contact_phone", phone);
        return values;
    }

    public Cursor getEvent(int eventId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_EVENTS, null, "id = ?",
                new String[]{String.valueOf(eventId)}, null, null, null);
    }

    public Cursor getAllEvents() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_EVENTS, null, null, null, null, null, "event_date ASC, event_time ASC");
    }

    public boolean deleteEvent(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_EVENTS, "id = ?", new String[]{String.valueOf(eventId)});
        return result > 0;
    }

    public boolean addSession(int eventId, String sessionName, String sessionTime,
                              String speaker, String room, String sessionType, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.insert(TABLE_SESSIONS, null, sessionValues(
                eventId, sessionName, sessionTime, speaker, room, sessionType, description)) != -1;
    }

    public boolean updateSession(int sessionId, int eventId, String sessionName, String sessionTime,
                                 String speaker, String room, String sessionType, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(TABLE_SESSIONS, sessionValues(
                eventId, sessionName, sessionTime, speaker, room, sessionType, description),
                "id = ?", new String[]{String.valueOf(sessionId)}) > 0;
    }

    private ContentValues sessionValues(int eventId, String sessionName, String sessionTime,
                                        String speaker, String room, String sessionType, String description) {
        ContentValues values = new ContentValues();
        values.put("event_id", eventId);
        values.put("session_name", sessionName);
        values.put("session_time", sessionTime);
        values.put("speaker", speaker);
        values.put("room", room);
        values.put("session_type", sessionType);
        values.put("description", description);
        return values;
    }

    public boolean deleteSession(int sessionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_SESSIONS, "id = ?",
                new String[]{String.valueOf(sessionId)}) > 0;
    }

    public Cursor getSessionsForEvent(int eventId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_SESSIONS, null, "event_id = ?", new String[]{String.valueOf(eventId)}, null, null, "session_time ASC");
    }

    public int getSessionCountForEvent(int eventId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SESSIONS + " WHERE event_id = ?", new String[]{String.valueOf(eventId)});
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    public Cursor searchEvents(String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_EVENTS +
                " WHERE name LIKE ? OR description LIKE ? OR event_date LIKE ? OR organizer LIKE ?" +
                " ORDER BY event_date ASC";
        String searchPattern = "%" + keyword + "%";
        return db.rawQuery(query, new String[]{
                searchPattern, searchPattern, searchPattern, searchPattern
        });
    }

    public Cursor searchSessions(int eventId, String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_SESSIONS +
                " WHERE event_id = ? AND (session_name LIKE ? OR description LIKE ?)" +
                " ORDER BY session_time ASC";
        String searchPattern = "%" + keyword + "%";
        return db.rawQuery(query, new String[]{
                String.valueOf(eventId), searchPattern, searchPattern
        });
    }
}