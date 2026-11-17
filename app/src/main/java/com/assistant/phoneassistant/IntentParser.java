package com.assistant.phoneassistant;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntentParser {

    public enum IntentType {
        SET_ALARM,
        SET_TIMER,
        LAUNCH_APP,
        CREATE_REMINDER,
        SAVE_MEMORY,
        RETRIEVE_MEMORY,
        SCHEDULE_LEARNING,
        CALENDAR_EVENT,
        NAVIGATION,
        LEARNING_ACTION,
        GENERAL_QUERY
    }

    public static class ParsedIntent {
        public IntentType type;
        public String rawQuery;
        public int hour = -1;
        public int minute = -1;
        public int durationSeconds = 0;
        public String label = "";
        public String targetApp = "";
        public String subject = "";
        public String memoryKey = "";
        public String memoryValue = "";
        public String destination = "";

        public ParsedIntent(IntentType type, String rawQuery) {
            this.type = type;
            this.rawQuery = rawQuery;
        }
    }

    public static ParsedIntent parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new ParsedIntent(IntentType.GENERAL_QUERY, "");
        }

        String raw = input.trim();
        String q = raw.toLowerCase(Locale.ROOT);

        // 1. Learning Scheduling (e.g. "Schedule SQL learning every day at 8 PM", "teach me React for 1 hour daily", "learn Python")
        if ((q.contains("schedule") || q.contains("learn") || q.contains("study") || q.startsWith("teach me")) &&
            (q.contains("sql") || q.contains("react") || q.contains("python") || q.contains("javascript") ||
             q.contains("node") || q.contains("excel") || q.contains("mern") || q.contains("dsa") || q.contains("java") ||
             q.contains("curriculum") || q.contains("lesson"))) {

            ParsedIntent pi = new ParsedIntent(IntentType.SCHEDULE_LEARNING, raw);
            pi.subject = extractSubject(q);
            pi.hour = extractHour(q, 20); // default 8 PM (20:00)
            pi.minute = extractMinute(q, 0);
            pi.durationSeconds = extractDurationMinutes(q, 60) * 60;
            return pi;
        }

        // 2. Alarm Intent
        if (q.contains("alarm")) {
            ParsedIntent pi = new ParsedIntent(IntentType.SET_ALARM, raw);
            extractAlarmTime(q, pi);
            pi.label = extractAlarmLabel(q);
            return pi;
        }

        // 3. Timer Intent
        if (q.contains("timer")) {
            ParsedIntent pi = new ParsedIntent(IntentType.SET_TIMER, raw);
            pi.durationSeconds = extractTimerDurationSeconds(q);
            pi.label = "Evolve AI Focus Timer";
            return pi;
        }

        // 4. Launch App
        if (q.startsWith("open ") || q.startsWith("launch ") || q.contains("open youtube") || q.contains("open whatsapp") || q.contains("open settings") || q.contains("open maps") || q.contains("open camera")) {
            ParsedIntent pi = new ParsedIntent(IntentType.LAUNCH_APP, raw);
            pi.targetApp = raw.replaceFirst("(?i)open |(?i)launch ", "").trim();
            return pi;
        }

        // 5. Memory Commands
        if (q.startsWith("remember ") || q.startsWith("save memory ")) {
            ParsedIntent pi = new ParsedIntent(IntentType.SAVE_MEMORY, raw);
            String content = raw.replaceFirst("(?i)remember |(?i)save memory ", "").trim();
            pi.memoryKey = "User Fact";
            pi.memoryValue = content;
            return pi;
        }

        if (q.contains("what do you remember") || q.contains("show memory") || q.contains("retrieve memory") || q.contains("my facts")) {
            return new ParsedIntent(IntentType.RETRIEVE_MEMORY, raw);
        }

        // 6. Navigation
        if (q.startsWith("navigate to ") || q.startsWith("directions to ")) {
            ParsedIntent pi = new ParsedIntent(IntentType.NAVIGATION, raw);
            pi.destination = raw.replaceFirst("(?i)navigate to |(?i)directions to ", "").trim();
            return pi;
        }

        // 7. Calendar Event
        if (q.contains("calendar") || (q.contains("schedule") && q.contains("meeting"))) {
            ParsedIntent pi = new ParsedIntent(IntentType.CALENDAR_EVENT, raw);
            pi.label = raw;
            return pi;
        }

        // Default: General Query (passed to Local Model or Knowledge System)
        return new ParsedIntent(IntentType.GENERAL_QUERY, raw);
    }

    private static String extractSubject(String q) {
        if (q.contains("sql")) return "SQL";
        if (q.contains("react")) return "React";
        if (q.contains("python")) return "Python";
        if (q.contains("node")) return "Node.js";
        if (q.contains("excel")) return "Excel";
        if (q.contains("mern")) return "MERN Stack";
        if (q.contains("dsa")) return "DSA";
        if (q.contains("java")) return "Java";
        return "Software Engineering";
    }

    private static int extractHour(String q, int defaultHour) {
        Pattern p = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?");
        Matcher m = p.matcher(q);
        if (m.find()) {
            int h = Integer.parseInt(m.group(1));
            String ampm = m.group(3);
            if ("pm".equalsIgnoreCase(ampm) && h < 12) h += 12;
            if ("am".equalsIgnoreCase(ampm) && h == 12) h = 0;
            return h;
        }
        return defaultHour;
    }

    private static int extractMinute(String q, int defaultMinute) {
        Pattern p = Pattern.compile("(\\d{1,2}):(\\d{2})");
        Matcher m = p.matcher(q);
        if (m.find()) {
            return Integer.parseInt(m.group(2));
        }
        return defaultMinute;
    }

    private static int extractDurationMinutes(String q, int defaultMins) {
        Pattern p = Pattern.compile("(\\d+)\\s*(hour|hr|minute|min)");
        Matcher m = p.matcher(q);
        if (m.find()) {
            int val = Integer.parseInt(m.group(1));
            String unit = m.group(2);
            if (unit.startsWith("hour") || unit.startsWith("hr")) return val * 60;
            return val;
        }
        return defaultMins;
    }

    private static void extractAlarmTime(String q, ParsedIntent pi) {
        Pattern p = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?");
        Matcher m = p.matcher(q);
        if (m.find()) {
            int h = Integer.parseInt(m.group(1));
            int min = m.group(2) != null ? Integer.parseInt(m.group(2)) : 0;
            String ampm = m.group(3);
            if ("pm".equalsIgnoreCase(ampm) && h < 12) h += 12;
            if ("am".equalsIgnoreCase(ampm) && h == 12) h = 0;
            pi.hour = h;
            pi.minute = min;
        } else {
            pi.hour = 7; // default 07:00
            pi.minute = 0;
        }
    }

    private static String extractAlarmLabel(String q) {
        if (q.contains("workout") || q.contains("gym")) return "Evolve Workout Alarm";
        if (q.contains("learning") || q.contains("study") || q.contains("sql")) return "Evolve Learning Session";
        return "Evolve AI Alarm";
    }

    private static int extractTimerDurationSeconds(String q) {
        Pattern p = Pattern.compile("(\\d+)\\s*(min|minute|sec|second|hr|hour)");
        Matcher m = p.matcher(q);
        if (m.find()) {
            int val = Integer.parseInt(m.group(1));
            String unit = m.group(2);
            if (unit.startsWith("sec")) return val;
            if (unit.startsWith("hr") || unit.startsWith("hour")) return val * 3600;
            return val * 60; // minutes
        }
        return 600; // default 10 mins
    }
}
