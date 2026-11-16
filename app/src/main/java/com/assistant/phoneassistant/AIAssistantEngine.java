package com.assistant.phoneassistant;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIAssistantEngine {

    public static String processQuery(String input, DatabaseHelper db, LocalModelManager modelManager, AndroidActionManager actionManager) {
        if (input == null || input.trim().isEmpty()) {
            return "Jarvis online. Awaiting system command or personal optimization request.";
        }

        String query = input.toLowerCase(Locale.ROOT).trim();

        // 1. ACTION: Set Native Alarm (e.g., "set alarm at 4:30 am", "set alarm tomorrow at 4:30 am", "alarm 05:00")
        if (query.contains("alarm")) {
            // Pattern to extract time like 4:30 am, 04:30, 5 am, 6:00 pm
            Pattern timePattern = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?");
            Matcher matcher = timePattern.matcher(query);
            if (matcher.find()) {
                int hour = Integer.parseInt(matcher.group(1));
                int minutes = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
                String ampm = matcher.group(3);

                if ("pm".equalsIgnoreCase(ampm) && hour < 12) hour += 12;
                if ("am".equalsIgnoreCase(ampm) && hour == 12) hour = 0;

                String label = "Evolve Morning Protocol";
                if (query.contains("workout")) label = "Evolve Workout";
                else if (query.contains("study") || query.contains("dsa")) label = "Evolve Learning Session";

                // Invoke Native Android Clock via AndroidActionManager
                return actionManager.setNativeAlarm(hour, minutes, label, true);
            }
            return "Please specify the alarm time (e.g., 'Jarvis, set an alarm tomorrow at 4:30 AM').";
        }

        // 2. ACTION: Set Native Timer (e.g., "set a timer for 10 minutes", "timer 5 mins")
        if (query.contains("timer")) {
            Pattern timerPattern = Pattern.compile("(\\d+)\\s*(min|minute|sec|second)");
            Matcher matcher = timerPattern.matcher(query);
            if (matcher.find()) {
                int amount = Integer.parseInt(matcher.group(1));
                String unit = matcher.group(2);
                int totalSeconds = unit.startsWith("sec") ? amount : amount * 60;

                // Invoke Native Android Timer via AndroidActionManager
                return actionManager.setNativeTimer(totalSeconds, "Evolve Focus Session", true);
            }
            return "Please specify the duration (e.g., 'Set a timer for 10 minutes').";
        }

        // 3. ACTION: Launch Apps (e.g., "open youtube", "launch whatsapp", "open settings", "open maps")
        if (query.startsWith("open ") || query.startsWith("launch ")) {
            String appName = input.replaceFirst("(?i)open |(?i)launch ", "").trim();
            return actionManager.launchAppByName(appName);
        }

        // 4. ACTION: Calendar Events (e.g., "schedule meeting at 14:00", "calendar event dsa practice at 10:00")
        if (query.contains("calendar") || (query.contains("schedule") && query.contains("event"))) {
            return actionManager.createCalendarEvent("Evolve Deep Work Session", "Scheduled via Evolve AI Jarvis", 10, 0, 60);
        }

        // 5. ACTION: Navigation (e.g., "navigate to...", "directions to...")
        if (query.startsWith("navigate to ") || query.startsWith("directions to ")) {
            String destination = input.replaceFirst("(?i)navigate to |(?i)directions to ", "").trim();
            return actionManager.startNavigation(destination);
        }

        // 6. Memory commands
        if (query.startsWith("remember ") || query.startsWith("save memory ")) {
            String memoryText = input.replaceFirst("(?i)remember |(?i)save memory ", "").trim();
            db.addMemory("User Fact", memoryText);
            return "Saved to persistent AI Memory: \"" + memoryText + "\"\n\nI will remember this context during future briefings and advice.";
        }

        // 7. Schedule & Planning commands
        if (query.startsWith("add task ") || query.startsWith("plan ") || query.startsWith("schedule ")) {
            String taskText = input.replaceFirst("(?i)add task |(?i)plan |(?i)schedule ", "").trim();
            db.addTask(taskText, "Daily");
            return "Scheduled in My Day: \"" + taskText + "\"\n\nPrioritized and integrated with today's milestones.";
        }

        // 8. Daily Operating Briefing
        if (query.contains("briefing") || query.contains("morning") || query.contains("today") || query.contains("summary")) {
            List<String> tasks = db.getAllTasks();
            StringBuilder sb = new StringBuilder("Evolve AI — Daily Operating Briefing:\n\n");
            sb.append("Current AI Engine: ").append(modelManager.getActiveModelId().toUpperCase()).append("\n\n");
            sb.append("Active Milestones Today:\n");
            for (String t : tasks) {
                sb.append("  ").append(t).append("\n");
            }
            sb.append("\nRecommended Priority: Tackle technical or demanding goals before 12:00 PM.");
            return sb.toString();
        }

        // 9. Career Growth Coach
        if (query.contains("career") || query.contains("job") || query.contains("interview") || query.contains("skill") || query.contains("code") || query.contains("dsa")) {
            return "Evolve Career Coach Protocol:\n\n" +
                    "• 1. Data Structures & Algorithms: Complete 1 Medium problem daily (focus on Binary Search & Two Pointers).\n" +
                    "• 2. System Design: Review distributed caching & database indexing strategies.\n" +
                    "• 3. Portfolio Review: Ensure GitHub demonstrates production-grade tests & clean documentation.\n\n" +
                    "Would you like me to start a 5-question technical mock interview session?";
        }

        // 10. Fitness & Nutrition Coach
        if (query.contains("fitness") || query.contains("workout") || query.contains("meal") || query.contains("diet") || query.contains("exercise")) {
            return "Evolve Wellness & Fitness Protocol:\n\n" +
                    "• Conditioning: 30-min targeted hypertrophy / core strength session.\n" +
                    "• Nutrition Target: High-protein clean vegetarian options (paneer, tofu, lentils, Greek yogurt, sprouts).\n" +
                    "• Hydration: Aim for 3.0 Liters total (Tracked via daily routine).\n\n" +
                    "Shall I add this workout to your scheduled tasks?";
        }

        // 11. Teach Me Mode
        if (query.startsWith("teach me ") || query.contains("teach me")) {
            String topic = input.replaceFirst("(?i)teach me ", "").trim();
            return "Evolve 'TEACH ME' Mode Activated: [" + topic.toUpperCase() + "]\n\n" +
                    "1. Core Concept: " + topic + " solves foundational bottlenecks by isolating logic and establishing predictable invariants.\n" +
                    "2. Practical Example: Think of it like caching in system design—instead of recalculating, reuse validated outputs.\n\n" +
                    "Check question: Can you explain in one sentence why this is beneficial over brute force?";
        }

        // Context-aware fallback response
        return "Evolve AI Action System: \"" + input + "\"\n\n" +
                "Engine: " + modelManager.getActiveModelId() + "\n" +
                "I can execute native phone actions: 'Set alarm at 4:30 AM', 'Set timer for 10 minutes', 'Open YouTube', or 'Schedule calendar event'.";
    }
}
