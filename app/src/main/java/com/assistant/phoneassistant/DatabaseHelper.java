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
    private static final int DATABASE_VERSION = 2;

    // 1. Tasks & Planner Table
    public static final String TABLE_TASKS = "tasks";
    public static final String COLUMN_TASK_ID = "id";
    public static final String COLUMN_TASK_TITLE = "title";
    public static final String COLUMN_TASK_CATEGORY = "category"; // Daily, Career, Fitness, Learning
    public static final String COLUMN_TASK_COMPLETED = "completed";

    // 2. Persistent Memory Table
    public static final String TABLE_MEMORY = "memory";
    public static final String COLUMN_MEMORY_ID = "id";
    public static final String COLUMN_MEMORY_KEY = "memory_key";
    public static final String COLUMN_MEMORY_VALUE = "memory_val";

    // 3. Habits & Streaks Table
    public static final String TABLE_HABITS = "habits";
    public static final String COLUMN_HABIT_ID = "id";
    public static final String COLUMN_HABIT_NAME = "name";
    public static final String COLUMN_HABIT_STREAK = "streak";
    public static final String COLUMN_HABIT_DONE_TODAY = "done_today";

    // 4. Career Skills & Roadmaps Table
    public static final String TABLE_SKILLS = "skills";
    public static final String COLUMN_SKILL_ID = "id";
    public static final String COLUMN_SKILL_NAME = "name";
    public static final String COLUMN_SKILL_LEVEL = "level"; // Beginner, Intermediate, Advanced
    public static final String COLUMN_SKILL_PROGRESS = "progress"; // percentage 0-100

    // 5. Finance & Expenses Table
    public static final String TABLE_FINANCE = "finance";
    public static final String COLUMN_FINANCE_ID = "id";
    public static final String COLUMN_FINANCE_TITLE = "title";
    public static final String COLUMN_FINANCE_AMOUNT = "amount";
    public static final String COLUMN_FINANCE_TYPE = "type"; // Income, Expense

    // 6. Private Journal Table
    public static final String TABLE_JOURNAL = "journal";
    public static final String COLUMN_JOURNAL_ID = "id";
    public static final String COLUMN_JOURNAL_DATE = "date";
    public static final String COLUMN_JOURNAL_ENTRY = "entry";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tasks
        db.execSQL("CREATE TABLE " + TABLE_TASKS + " (" +
                COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TASK_TITLE + " TEXT, " +
                COLUMN_TASK_CATEGORY + " TEXT, " +
                COLUMN_TASK_COMPLETED + " INTEGER DEFAULT 0)");

        // Memory
        db.execSQL("CREATE TABLE " + TABLE_MEMORY + " (" +
                COLUMN_MEMORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_MEMORY_KEY + " TEXT, " +
                COLUMN_MEMORY_VALUE + " TEXT)");

        // Habits
        db.execSQL("CREATE TABLE " + TABLE_HABITS + " (" +
                COLUMN_HABIT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_HABIT_NAME + " TEXT, " +
                COLUMN_HABIT_STREAK + " INTEGER DEFAULT 0, " +
                COLUMN_HABIT_DONE_TODAY + " INTEGER DEFAULT 0)");

        // Skills
        db.execSQL("CREATE TABLE " + TABLE_SKILLS + " (" +
                COLUMN_SKILL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SKILL_NAME + " TEXT, " +
                COLUMN_SKILL_LEVEL + " TEXT, " +
                COLUMN_SKILL_PROGRESS + " INTEGER DEFAULT 0)");

        // Finance
        db.execSQL("CREATE TABLE " + TABLE_FINANCE + " (" +
                COLUMN_FINANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FINANCE_TITLE + " TEXT, " +
                COLUMN_FINANCE_AMOUNT + " REAL, " +
                COLUMN_FINANCE_TYPE + " TEXT)");

        // Journal
        db.execSQL("CREATE TABLE " + TABLE_JOURNAL + " (" +
                COLUMN_JOURNAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_JOURNAL_DATE + " TEXT, " +
                COLUMN_JOURNAL_ENTRY + " TEXT)");

        // Seed initial data
        seedDefaults(db);
    }

    private void seedDefaults(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + TABLE_TASKS + " (title, category, completed) VALUES " +
                "('Morning Review & Routine Optimization', 'Daily', 0)," +
                "('Data Structures & Algorithms (1 Medium problem)', 'Career', 0)," +
                "('30-min Targeted Workout & Hydration Target', 'Fitness', 0)," +
                "('Evening Reflection & Goal Alignment', 'Daily', 0)");

        db.execSQL("INSERT INTO " + TABLE_MEMORY + " (memory_key, memory_val) VALUES " +
                "('Core Purpose', 'Become the best version of myself in career, health, and discipline.')," +
                "('Target Architecture', 'Full-stack AI developer with mastery over system design.')");

        db.execSQL("INSERT INTO " + TABLE_HABITS + " (name, streak, done_today) VALUES " +
                "('Early Wake-up Routine', 5, 1)," +
                "('Daily Coding & Problem Solving', 12, 1)," +
                "('3L Hydration Target', 8, 0)," +
                "('Evening Reflection', 4, 0)");

        db.execSQL("INSERT INTO " + TABLE_SKILLS + " (name, level, progress) VALUES " +
                "('Data Structures & Algorithms', 'Intermediate', 65)," +
                "('System Design & Distributed Systems', 'Intermediate', 55)," +
                "('SQL & Database Optimization', 'Advanced', 80)," +
                "('AI Model Quantization (GGUF)', 'Intermediate', 60)");

        db.execSQL("INSERT INTO " + TABLE_FINANCE + " (title, amount, type) VALUES " +
                "('Monthly Savings Target', 15000, 'Income')," +
                "('Cloud Infrastructure / Tools', 0, 'Expense')");

        db.execSQL("INSERT INTO " + TABLE_JOURNAL + " (date, entry) VALUES " +
                "('Today', 'Day started with clear objectives. Focus maintained on foundational system skills.')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HABITS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SKILLS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FINANCE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_JOURNAL);
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

    // Habit Operations
    public List<String> getAllHabits() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_HABIT_NAME + ", " + COLUMN_HABIT_STREAK + ", " + COLUMN_HABIT_DONE_TODAY + " FROM " + TABLE_HABITS, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0);
                int streak = cursor.getInt(1);
                int done = cursor.getInt(2);
                list.add((done == 1 ? "🔥 " : "⏳ ") + name + " (" + streak + " day streak)");
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void checkInHabit(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_HABITS + " SET " + COLUMN_HABIT_STREAK + " = " + COLUMN_HABIT_STREAK + " + 1, " + COLUMN_HABIT_DONE_TODAY + " = 1 WHERE " + COLUMN_HABIT_NAME + " LIKE ?", new String[]{"%" + name + "%"});
    }

    // Skill Operations
    public List<String> getAllSkills() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_SKILL_NAME + ", " + COLUMN_SKILL_LEVEL + ", " + COLUMN_SKILL_PROGRESS + " FROM " + TABLE_SKILLS, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0);
                String level = cursor.getString(1);
                int prog = cursor.getInt(2);
                list.add("⚡ " + name + " [" + level + "] • " + prog + "% Mastered");
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // Finance Operations
    public String getFinanceSummary() {
        SQLiteDatabase db = this.getReadableDatabase();
        double savings = 0;
        double expenses = 0;
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_FINANCE_AMOUNT + ", " + COLUMN_FINANCE_TYPE + " FROM " + TABLE_FINANCE, null);
        if (cursor.moveToFirst()) {
            do {
                double amt = cursor.getDouble(0);
                String type = cursor.getString(1);
                if ("Income".equalsIgnoreCase(type)) savings += amt;
                else expenses += amt;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return "💰 Savings Target: ₹" + (int) savings + " | Monthly Spend: ₹" + (int) expenses;
    }

    public void addExpense(String title, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_FINANCE_TITLE, title);
        cv.put(COLUMN_FINANCE_AMOUNT, amount);
        cv.put(COLUMN_FINANCE_TYPE, "Expense");
        db.insert(TABLE_FINANCE, null, cv);
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
