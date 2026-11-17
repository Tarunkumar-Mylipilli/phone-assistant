package com.assistant.phoneassistant;

import java.util.ArrayList;
import java.util.List;

public class LearningCurriculumEngine {

    public static class Lesson {
        public long id;
        public String subject;
        public String moduleName;
        public String title;
        public int orderIndex;
        public int durationMinutes;
        public String status; // NOT_STARTED, PREPARING, READY, IN_PROGRESS, COMPLETED, NEEDS_REVISION
        public String notesContent;
        public String quizJson;

        public Lesson(String subject, String moduleName, String title, int orderIndex, int durationMinutes, String status, String notesContent, String quizJson) {
            this.subject = subject;
            this.moduleName = moduleName;
            this.title = title;
            this.orderIndex = orderIndex;
            this.durationMinutes = durationMinutes;
            this.status = status;
            this.notesContent = notesContent;
            this.quizJson = quizJson;
        }
    }

    public static List<Lesson> generateCurriculum(String subject, String level) {
        List<Lesson> lessons = new ArrayList<>();
        String sub = subject.toUpperCase();

        if (sub.contains("SQL")) {
            lessons.add(new Lesson("SQL", "Module 1: Relational Foundations", "1. Relational Database Concepts & Schema Architecture", 1, 45, "READY",
                    getSqlLesson1Notes(), getSqlLesson1Quiz()));
            lessons.add(new Lesson("SQL", "Module 1: Relational Foundations", "2. SELECT, WHERE, ORDER BY & Projection Filtering", 2, 45, "READY",
                    getSqlLesson2Notes(), getSqlLesson2Quiz()));
            lessons.add(new Lesson("SQL", "Module 2: Aggregations & Grouping", "3. GROUP BY, HAVING, and Aggregate Functions (COUNT, SUM, AVG)", 3, 60, "NOT_STARTED", "", ""));
            lessons.add(new Lesson("SQL", "Module 3: Multi-Table Relationships", "4. INNER JOIN, LEFT/RIGHT JOIN & Cross Product Execution", 4, 60, "NOT_STARTED", "", ""));
            lessons.add(new Lesson("SQL", "Module 4: Advanced Query Optimization", "5. Window Functions (ROW_NUMBER, RANK, DENSE_RANK) & Indexing", 5, 60, "NOT_STARTED", "", ""));
        } else if (sub.contains("REACT")) {
            lessons.add(new Lesson("React", "Module 1: Core Virtual DOM", "1. Component Lifecycle, JSX & Virtual DOM Reconciliation", 1, 45, "READY",
                    getReactLesson1Notes(), getReactLesson1Quiz()));
            lessons.add(new Lesson("React", "Module 2: State & Effects", "2. useState, useEffect & Custom Hook Architecture", 2, 45, "NOT_STARTED", "", ""));
            lessons.add(new Lesson("React", "Module 3: State Management", "3. Context API, Redux Toolkit & State Invariance", 3, 60, "NOT_STARTED", "", ""));
            lessons.add(new Lesson("React", "Module 4: Performance & SSR", "4. useMemo, useCallback, React 19 Server Components", 4, 60, "NOT_STARTED", "", ""));
        } else if (sub.contains("PYTHON")) {
            lessons.add(new Lesson("Python", "Module 1: Memory & Syntax", "1. Pythonic Memory Model, Variables & Data Structures", 1, 45, "READY",
                    getPythonLesson1Notes(), getPythonLesson1Quiz()));
            lessons.add(new Lesson("Python", "Module 2: Functional & OOP", "2. List Comprehensions, Generators & Dunder Methods", 2, 45, "NOT_STARTED", "", ""));
            lessons.add(new Lesson("Python", "Module 3: Concurrency", "3. Threading vs Multiprocessing vs Asyncio Coroutines", 3, 60, "NOT_STARTED", "", ""));
        } else {
            lessons.add(new Lesson(subject, "Module 1: Core Fundamentals", "1. Core Principles & Architecture of " + subject, 1, 45, "READY",
                    "## " + subject + " — Fundamental Theory Notes\n\n• Core architecture and foundational paradigms.\n• Key abstractions, design patterns, and industry best practices.\n• Real-world production deployment considerations.",
                    "[{\"q\":\"What is the primary objective of " + subject + "?\",\"a\":[\"System modularity and reliability\",\"Random execution\",\"None\"],\"correct\":0}]"));
            lessons.add(new Lesson(subject, "Module 2: Advanced Practice", "2. Deep Dive & Practical Production Implementation", 2, 60, "NOT_STARTED", "", ""));
        }

        return lessons;
    }

    private static String getSqlLesson1Notes() {
        return "# SQL Master Class: Relational Database Concepts & Schema Architecture\n\n" +
                "## 1. Learning Objectives\n" +
                "• Understand Codd's Relational Model and tabular data structures.\n" +
                "• Master Primary Key (PK), Foreign Key (FK), and Referential Integrity.\n" +
                "• Analyze ACID Properties (Atomicity, Consistency, Isolation, Durability).\n\n" +
                "## 2. Core Architectural Theory\n" +
                "Relational Databases organize data into mathematical relations (tables) consisting of tuples (rows) and attributes (columns). Unlike unstructured document stores, RDBMS enforces strong consistency through mathematical constraints.\n\n" +
                "```sql\n" +
                "CREATE TABLE Users (\n" +
                "    user_id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    email VARCHAR(255) UNIQUE NOT NULL,\n" +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n" +
                ");\n" +
                "```\n\n" +
                "## 3. Real-World Applications & Mistakes\n" +
                "• Common Pitfall: Omitting foreign key constraints, resulting in orphaned records.\n" +
                "• Best Practice: Explicitly define `NOT NULL` and indexed primary keys for deterministic query planner optimization.";
    }

    private static String getSqlLesson1Quiz() {
        return "[{\"q\":\"What does the ACID property 'Atomicity' guarantee?\",\"a\":[\"Transactions either fully succeed or completely rollback without partial state\",\"Data is written across multiple disk drives\",\"Transactions run simultaneously without locks\"],\"correct\":0}," +
                "{\"q\":\"What constraint prevents orphaned child records in a relational database?\",\"a\":[\"Foreign Key with Referential Integrity\",\"Unique Index on Child table\",\"Composite Primary Key\"],\"correct\":0}]";
    }

    private static String getSqlLesson2Notes() {
        return "# SQL Master Class: SELECT, WHERE, ORDER BY & Projection Filtering\n\n" +
                "## 1. Learning Objectives\n" +
                "• Master SQL Query Execution Order (FROM -> WHERE -> GROUP BY -> HAVING -> SELECT -> ORDER BY -> LIMIT).\n" +
                "• Prevent full table scans using SARGable WHERE clause predicates.\n" +
                "• Implement pagination using deterministic `ORDER BY` with `LIMIT` & `OFFSET`.\n\n" +
                "```sql\n" +
                "SELECT user_id, email\n" +
                "FROM Users\n" +
                "WHERE created_at >= '2026-01-01'\n" +
                "ORDER BY created_at DESC\n" +
                "LIMIT 20 OFFSET 0;\n" +
                "```";
    }

    private static String getSqlLesson2Quiz() {
        return "[{\"q\":\"What is the true internal SQL evaluation order of clauses?\",\"a\":[\"FROM -> WHERE -> GROUP BY -> HAVING -> SELECT -> ORDER BY\",\"SELECT -> FROM -> WHERE -> ORDER BY\",\"WHERE -> SELECT -> FROM -> HAVING\"],\"correct\":0}]";
    }

    private static String getReactLesson1Notes() {
        return "# React Architecture: Virtual DOM, Reconciliation & JSX\n\n" +
                "## 1. Theory & Fiber Reconciliation\n" +
                "React maintains an in-memory representation of UI known as the Virtual DOM. When state changes occur, React Fiber compares the old and new tree via the Diffing Algorithm in O(n) heuristic time.\n\n" +
                "```jsx\n" +
                "function Counter() {\n" +
                "  const [count, setCount] = useState(0);\n" +
                "  return <button onClick={() => setCount(c => c + 1)}>Count: {count}</button>;\n" +
                "}\n" +
                "```";
    }

    private static String getReactLesson1Quiz() {
        return "[{\"q\":\"Why does React use keys when rendering dynamic lists?\",\"a\":[\"To identify which items changed, were added, or removed during reconciliation\",\"To apply unique CSS styles\",\"To sort array elements alphabetically\"],\"correct\":0}]";
    }

    private static String getPythonLesson1Notes() {
        return "# Python Internals: Memory Management, PyObject & Execution Engine\n\n" +
                "## 1. Everything is an Object\n" +
                "In CPython, every entity is wrapped in a `PyObject` structure containing an atomic reference counter `ob_refcnt` and a type pointer `ob_type`. Garbage collection uses cyclic generational GC to clean up reference cycles.\n\n" +
                "```python\n" +
                "# Invariant memory inspection\n" +
                "import sys\n" +
                "x = [1, 2, 3]\n" +
                "print(sys.getrefcount(x))  # Reference count\n" +
                "```";
    }

    private static String getPythonLesson1Quiz() {
        return "[{\"q\":\"How does CPython reclaim memory for cyclical references?\",\"a\":[\"Generational Cyclic Garbage Collector\",\"Immediate OS deallocation\",\"Manual free() calls\"],\"correct\":0}]";
    }
}
