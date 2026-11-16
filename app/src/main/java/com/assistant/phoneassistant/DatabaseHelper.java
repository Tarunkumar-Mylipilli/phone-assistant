package com.assistant.phoneassistant;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "evolve_ai.db";
    private static final int DATABASE_VERSION = 1;

    // Tasks Table
    public static final String TABLE_TASKS = "tasks";
    public static final String COLUMN_TASK_ID = "id";
    public static final String COLUMN_TASK_TITLE = "title";
    public static final String COLUMN_TASK_CATEGORY = "category"; // Daily, Career, Fitness, Learning
    public static final String COLUMN_TASK_COMPLETED = "completed";

    // Memory Table
    public static final String TABLE_MEMORY = "memory";
    public static final String COLUMN_MEMORY_ID = "id";
    public static final String COLUMN_MEMORY_KEY = "memory_key";
    public static final String COLUMN_MEMORY_VALUE = "memory_val";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_TASKS + " (" +
                COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TASK_TITLE + " TEXT, " +
                COLUMN_TASK_CATEGORY + " TEXT, " +
                COLUMN_TASK_COMPLETED + " INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE " + TABLE_MEMORY + " (" +
                COLUMN_MEMORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_MEMORY_KEY + " TEXT, " +
                COLUMN_MEMORY_VALUE + " TEXT)");

        // Initial default tasks & memories
        db.execSQL("INSERT INTO " + TABLE_TASKS + " (title, category, completed) VALUES " +
                "('Morning Review & Routine Optimization', 'Daily', 0)," +
                "('Career Growth: Skill Practice & Code Session', 'Career', 0)," +
                "('Fitness: 30-min Daily Workout & Water Intake', 'Fitness', 0)," +
                "('Evening Reflection & Goal Alignment', 'Daily', 0)");

        db.execSQL("INSERT INTO " + TABLE_MEMORY + " (memory_key, memory_val) VALUES " +
                "('Core Purpose', 'Become the best version of myself in career, health, and mindset.')," +
                "('Preferred Focus', 'Software Development, AI automation, and daily fitness.')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMORY);
        onCreate(db);
    }

    // Task Operations
    public void addTask(String title, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TASK_TITLE, title);
        cv.put(COLUMN_TASK_CATEGORY, category);
        cv.put(COLUMN_TASK_COMPLETED, 0);
        db.insert(TABLE_TASKS, null, cv);
    }

    public List<String> getAllTasks() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_TASK_TITLE + ", " + COLUMN_TASK_CATEGORY + ", " + COLUMN_TASK_COMPLETED + " FROM " + TABLE_TASKS, null);
        if (cursor.moveToFirst()) {
            do {
                String title = cursor.getString(0);
                String cat = cursor.getString(1);
                int comp = cursor.getInt(2);
                list.add((comp == 1 ? "✓ " : "○ ") + "[" + cat + "] " + title);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void clearAllTasks() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, null, null);
    }

    // Memory Operations
    public void addMemory(String key, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_MEMORY_KEY, key);
        cv.put(COLUMN_MEMORY_VALUE, value);
        db.insert(TABLE_MEMORY, null, cv);
    }

    public List<String> getAllMemories() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_MEMORY_KEY + ", " + COLUMN_MEMORY_VALUE + " FROM " + TABLE_MEMORY, null);
        if (cursor.moveToFirst()) {
            do {
                list.add("• " + cursor.getString(0) + ": " + cursor.getString(1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void clearAllMemories() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEMORY, null, null);
    }
}
