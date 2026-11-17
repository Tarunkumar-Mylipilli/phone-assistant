package com.assistant.phoneassistant;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper: Persistent SQLite storage for Evolve AI.
 * Stores tasks, habits, learning roadmaps, prepared lesson notes, and persistent AI memory.
 * Zero hardcoded/fabricated sample records — completely user-owned data.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "evolve_ai.db";
    private static final int DATABASE_VERSION = 3;

    // 1. Tasks & Planner Table
    public static final String TABLE_TASKS = "tasks";
    public static final String COLUMN_TASK_ID = "id";
    public static final String COLUMN_TASK_TITLE = "title";
    public static final String COLUMN_TASK_CATEGORY = "category";
    public static final String COLUMN_TASK_COMPLETED = "completed";

    // 2. Persistent Memory Table
    public static final String TABLE_MEMORY = "memory";
    public static final String COLUMN_MEMORY_ID = "id";
    public static final String COLUMN_MEMORY_KEY = "memory_key";
    public static final String COLUMN_MEMORY_VALUE = "memory_val";

    // 3. Habits Table
    public static final String TABLE_HABITS = "habits";
    public static final String COLUMN_HABIT_ID = "id";
    public static final String COLUMN_HABIT_NAME = "name";
    public static final String COLUMN_HABIT_STREAK = "streak";
    public static final String COLUMN_HABIT_DONE_TODAY = "done_today";

    // 4. Learning Schedules Table
    public static final String TABLE_LEARNING_SCHEDULE = "learning_schedule";
    public static final String COLUMN_SCHED_ID = "id";
    public static final String COLUMN_SCHED_SUBJECT = "subject";
    public static final String COLUMN_SCHED_HOUR = "hour";
    public static final String COLUMN_SCHED_MINUTE = "minute";
    public static final String COLUMN_SCHED_DURATION = "duration_mins";
    public static final String COLUMN_SCHED_ACTIVE = "active";

    // 5. Lessons Table
    public static final String TABLE_LESSONS = "lessons";
    public static final String COLUMN_LESSON_ID = "id";
    public static final String COLUMN_LESSON_SUBJECT = "subject";
    public static final String COLUMN_LESSON_MODULE = "module_name";
    public static final String COLUMN_LESSON_TITLE = "title";
    public static final String COLUMN_LESSON_ORDER = "order_index";
    public static final String COLUMN_LESSON_DURATION = "duration";
    public static final String COLUMN_LESSON_STATUS = "status"; // NOT_STARTED, PREPARING, READY, IN_PROGRESS, COMPLETED, NEEDS_REVISION
    public static final String COLUMN_LESSON_NOTES = "notes_content";
    public static final String COLUMN_LESSON_QUIZ = "quiz_json";

    // 6. Finance Table
    public static final String TABLE_FINANCE = "finance";
    public static final String COLUMN_FINANCE_ID = "id";
    public static final String COLUMN_FINANCE_TITLE = "title";
    public static final String COLUMN_FINANCE_AMOUNT = "amount";
    public static final String COLUMN_FINANCE_TYPE = "type";

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

        // Learning Schedule
        db.execSQL("CREATE TABLE " + TABLE_LEARNING_SCHEDULE + " (" +
                COLUMN_SCHED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SCHED_SUBJECT + " TEXT, " +
                COLUMN_SCHED_HOUR + " INTEGER, " +
                COLUMN_SCHED_MINUTE + " INTEGER, " +
                COLUMN_SCHED_DURATION + " INTEGER, " +
                COLUMN_SCHED_ACTIVE + " INTEGER DEFAULT 1)");

        // Lessons & Curriculum Notes
        db.execSQL("CREATE TABLE " + TABLE_LESSONS + " (" +
                COLUMN_LESSON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_LESSON_SUBJECT + " TEXT, " +
                COLUMN_LESSON_MODULE + " TEXT, " +
                COLUMN_LESSON_TITLE + " TEXT, " +
                COLUMN_LESSON_ORDER + " INTEGER, " +
                COLUMN_LESSON_DURATION + " INTEGER, " +
                COLUMN_LESSON_STATUS + " TEXT, " +
                COLUMN_LESSON_NOTES + " TEXT, " +
                COLUMN_LESSON_QUIZ + " TEXT)");

        // Finance
        db.execSQL("CREATE TABLE " + TABLE_FINANCE + " (" +
                COLUMN_FINANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FINANCE_TITLE + " TEXT, " +
                COLUMN_FINANCE_AMOUNT + " REAL, " +
                COLUMN_FINANCE_TYPE + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HABITS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LEARNING_SCHEDULE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LESSONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FINANCE);
        onCreate(db);
    }

    // ----------------- LEARNING SCHEDULE & CURRICULUM -----------------

    public void saveLearningSchedule(String subject, int hour, int minute, int durationMins) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_SCHED_SUBJECT, subject);
        cv.put(COLUMN_SCHED_HOUR, hour);
        cv.put(COLUMN_SCHED_MINUTE, minute);
        cv.put(COLUMN_SCHED_DURATION, durationMins);
        cv.put(COLUMN_SCHED_ACTIVE, 1);
        db.insert(TABLE_LEARNING_SCHEDULE, null, cv);

        // Check if curriculum exists for this subject, if not generate it
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_LESSONS + " WHERE " + COLUMN_LESSON_SUBJECT + " LIKE ?", new String[]{"%" + subject + "%"});
        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();

        if (!exists) {
            List<LearningCurriculumEngine.Lesson> roadmap = LearningCurriculumEngine.generateCurriculum(subject, "Beginner to Advanced");
            for (LearningCurriculumEngine.Lesson l : roadmap) {
                ContentValues lcv = new ContentValues();
                lcv.put(COLUMN_LESSON_SUBJECT, l.subject);
                lcv.put(COLUMN_LESSON_MODULE, l.moduleName);
                lcv.put(COLUMN_LESSON_TITLE, l.title);
                lcv.put(COLUMN_LESSON_ORDER, l.orderIndex);
                lcv.put(COLUMN_LESSON_DURATION, l.durationMinutes);
                lcv.put(COLUMN_LESSON_STATUS, l.status);
                lcv.put(COLUMN_LESSON_NOTES, l.notesContent);
                lcv.put(COLUMN_LESSON_QUIZ, l.quizJson);
                db.insert(TABLE_LESSONS, null, lcv);
            }
        }
    }

    public List<LearningCurriculumEngine.Lesson> getLessonsForSubject(String subject) {
        List<LearningCurriculumEngine.Lesson> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, subject, module_name, title, order_index, duration, status, notes_content, quiz_json FROM " + TABLE_LESSONS +
                " WHERE subject LIKE ? ORDER BY order_index ASC", new String[]{"%" + subject + "%"});
        if (cursor.moveToFirst()) {
            do {
                LearningCurriculumEngine.Lesson l = new LearningCurriculumEngine.Lesson(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getInt(4),
                        cursor.getInt(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8)
                );
                l.id = cursor.getLong(0);
                list.add(l);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public LearningCurriculumEngine.Lesson getNextReadyLesson() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, subject, module_name, title, order_index, duration, status, notes_content, quiz_json FROM " + TABLE_LESSONS +
                " WHERE status = 'READY' ORDER BY id ASC LIMIT 1", null);
        if (cursor.moveToFirst()) {
            LearningCurriculumEngine.Lesson l = new LearningCurriculumEngine.Lesson(
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(4),
                    cursor.getInt(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8)
            );
            l.id = cursor.getLong(0);
            cursor.close();
            return l;
        }
        cursor.close();
        return null;
    }

    public void markLessonCompleted(long lessonId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_LESSON_STATUS, "COMPLETED");
        db.update(TABLE_LESSONS, cv, "id = ?", new String[]{String.valueOf(lessonId)});
    }

    // ----------------- MEMORY OPERATIONS -----------------

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

    // ----------------- TASKS & ROUTINES -----------------

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

    // ----------------- HABITS & STREAKS -----------------

    public void addHabit(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_HABIT_NAME, name);
        cv.put(COLUMN_HABIT_STREAK, 0);
        cv.put(COLUMN_HABIT_DONE_TODAY, 0);
        db.insert(TABLE_HABITS, null, cv);
    }

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

    // ----------------- FINANCE -----------------

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
        return "💰 Total Tracked: ₹" + (int) savings + " | Spend: ₹" + (int) expenses;
    }
}
