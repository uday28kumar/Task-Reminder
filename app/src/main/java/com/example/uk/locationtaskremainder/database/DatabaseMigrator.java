package com.example.uk.locationtaskremainder.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.HashMap;

public class DatabaseMigrator {

    private static final String TAG = DatabaseMigrator.class.getSimpleName();

    // Location Table columns.
    private static final int COL_LOC_ID = 0;
    private static final int COL_PLACE_NAME = 1;
    private static final int COL_LAT = 2;
    private static final int COL_LON = 3;
    private static final int COL_COUNT = 4;
    private static final int COL_HIDDEN = 5;

    // Task table columns.
    private static final int COL_TASK_ID = 0;
    private static final int COL_TASK_NAME = 1;
    private static final int COL_LOCATION_NAME = 2;
    private static final int COL_ALARM = 3;
    private static final int COL_TASK_COLOR = 4;
    private static final int COL_MIN_DISTANCE = 5;
    private static final int COL_DONE = 6;
    private static final int COL_REMIND_DIS = 7;
    private static final int COL_SNOOZE = 8;

    public static void migrateFromSqlToRoom(SupportSQLiteDatabase db) {

        HashMap<String, Long> locationMap = migrateLocationTable(db);
        migrateTaskTable(db, locationMap);
    }

    public static HashMap<String, Long> migrateLocationTable(SupportSQLiteDatabase db) {
        HashMap<String, Long> locationMap = new HashMap<String, Long>();

        // Create a table with name 'locations'.
        String createNewLocationsTable = "CREATE TABLE IF NOT EXISTS `locations` (`id` " +
                "INTEGER PRIMARY" + " KEY AUTOINCREMENT NOT NULL, `place_name` TEXT, `latitude` " +
                "REAL NOT NULL, " + "`longitude` REAL NOT NULL, `use_count` INTEGER NOT NULL, " +
                "`is_hidden` INTEGER" + " NOT" + " NULL, `date_added` TEXT)";
        db.execSQL(createNewLocationsTable);

        // Transfer all the data from old table to new table.
        Cursor cursor = db.query("SELECT * FROM location");

        while (cursor != null && cursor.moveToNext()) {

            String locationName = cursor.getString(COL_PLACE_NAME);
            double locationLatitude = cursor.getDouble(COL_LAT);
            double locationLongitude = cursor.getDouble(COL_LON);
            int isHidden = cursor.getInt(COL_HIDDEN);
            int useCount = cursor.getInt(COL_COUNT);
            String dateAdded = (new LocalDate()).toString();

            // insert it in the new table.
            ContentValues values = new ContentValues();
            values.put("place_name", locationName);
            values.put("latitude", locationLatitude);
            values.put("longitude", locationLongitude);
            values.put("use_count", useCount);
            values.put("is_hidden", isHidden);
            values.put("date_added", dateAdded);

            // Entry in hashmap.
            long newId = db.insert("locations", SQLiteDatabase.CONFLICT_REPLACE, values);
            locationMap.put(locationName, newId);
        }

        // Close cursor
        cursor.close();

        return locationMap;
    }

    public static void migrateTaskTable(SupportSQLiteDatabase db, HashMap<String, Long>
            locationMap) {

        // Rename old tasks table.
        db.execSQL("ALTER TABLE tasks RENAME TO old_tasks");

        // Create table tasks.
        String createNewTasksTable = "CREATE TABLE IF NOT EXISTS `tasks` (`id` INTEGER " +
                "PRIMARY KEY" + " AUTOINCREMENT NOT NULL, `task_name` TEXT, `location_id` INTEGER" +
                " NOT NULL, " + "`image_uri` TEXT, `is_done` INTEGER NOT NULL, `is_alarm_set` " +
                "INTEGER NOT " + "NULL, " + "`reminder_range` INTEGER NOT NULL, `note` TEXT, " +
                "`start_time` TEXT, " + "`end_time` " + "TEXT, `start_date` TEXT, `end_date` " +
                "TEXT, `next_start_date` TEXT, " + "`repeat_type` " + "INTEGER NOT NULL, " +
                "`movement_type` INTEGER NOT NULL, `activity_type` INTEGER " + "NOT " + "NULL, " +
                "`last_distance` REAL NOT NULL, `last_triggered` TEXT, `snoozed_at` " + "INTEGER," +
                " `date_added` TEXT, FOREIGN KEY(`location_id`) REFERENCES " + "`locations`" + "" +
                "(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )";
        db.execSQL(createNewTasksTable);

        // Copy data from old table to new table.
        Cursor cursor = db.query("SELECT * FROM old_tasks");
        while (cursor != null && cursor.moveToNext()) {
            String taskName = cursor.getString(COL_TASK_NAME);
            String locName = cursor.getString(COL_LOCATION_NAME);
            String alarm = cursor.getString(COL_ALARM);
            int minDistance = cursor.getInt(COL_MIN_DISTANCE);
            String doneStatus = cursor.getString(COL_DONE);
            int remindDistance = cursor.getInt(COL_REMIND_DIS);
            String snoozeTime = cursor.getString(COL_SNOOZE);

            ContentValues values = new ContentValues();
            values.put("task_name", taskName);
            values.put("location_id", locationMap.get(locName));
            int isDone = doneStatus.equals("true") ? 1 : 0;
            values.put("is_done", isDone);
            int isAlarmSet = alarm.equals("true") ? 1 : 0;
            values.put("is_alarm_set", isAlarmSet);
            values.put("reminder_range", remindDistance);
            values.put("start_time", new LocalTime(0, 0).toString());
            values.put("end_time", new LocalTime(23, 59).toString());
            values.put("start_date", new LocalDate().toString());
            values.put("next_start_date", new LocalDate().toString());
            values.put("repeat_type", DbConstants.NO_REPEAT);
            values.put("movement_type", DbConstants.BOTH_ENTER_EXIT);
            values.put("activity_type", DbConstants.ACTIVITY_ANYTHING);
            values.put("last_distance", minDistance);
            values.put("snoozed_at", Long.valueOf(snoozeTime));
            values.put("date_added", new LocalDate().toString());

            db.insert("tasks", SQLiteDatabase.CONFLICT_REPLACE, values);
        }

        // Close the cursor.
        cursor.close();
    }
}
