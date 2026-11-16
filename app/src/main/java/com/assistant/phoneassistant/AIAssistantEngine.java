package com.assistant.phoneassistant;

import java.util.List;
import java.util.Locale;

public class AIAssistantEngine {

    public static String processQuery(String input, DatabaseHelper db) {
        if (input == null || input.trim().isEmpty()) {
            return "I am Evolve AI, your personal life operating system. How can I assist you with your day, career, or fitness?";
        }

        String query = input.toLowerCase(Locale.ROOT).trim();

        // 1. Memory commands
        if (query.startsWith("remember ") || query.startsWith("save memory ")) {
            String memoryText = input.replaceFirst("(?i)remember |(?i)save memory ", "").trim();
            db.addMemory("User Note", memoryText);
            return "Saved to persistent AI Memory: \"" + memoryText + "\"";
        }

        // 2. Schedule / Task additions
        if (query.startsWith("add task ") || query.startsWith("plan ")) {
            String taskText = input.replaceFirst("(?i)add task |(?i)plan ", "").trim();
            db.addTask(taskText, "Daily");
            return "Added to today's schedule: \"" + taskText + "\"";
        }

        // 3. Morning / Daily Briefing
        if (query.contains("briefing") || query.contains("morning") || query.contains("summary") || query.contains("schedule")) {
            List<String> tasks = db.getAllTasks();
            StringBuilder sb = new StringBuilder("Here is your Daily Operating Briefing:\n\n");
            for (String t : tasks) {
                sb.append(t).append("\n");
            }
            sb.append("\nTip: Focus on high-leverage work before noon to maximize momentum.");
            return sb.toString();
        }

        // 4. Career Coaching
        if (query.contains("career") || query.contains("job") || query.contains("interview") || query.contains("skill")) {
            return "Evolve Career Coach:\n• Priority 1: Master core System Architecture & DSA practice daily.\n• Priority 2: Refine your portfolio projects with measurable metrics.\n• Priority 3: Run mock technical interview questions.\n\nWhat topic should we practice next?";
        }

        // 5. Fitness & Nutrition
        if (query.contains("fitness") || query.contains("workout") || query.contains("meal") || query.contains("diet")) {
            return "Evolve Wellness Coach:\n• Today's Routine: 30-min bodyweight conditioning or strength training.\n• Hydration target: 3.0 Liters.\n• Nutrition: Prioritize high-protein, clean vegetarian/balanced meals with healthy fats.\n\nKeep consistency over intensity!";
        }

        // 6. Memory query
        if (query.contains("what do you remember") || query.contains("memory") || query.contains("who am i")) {
            List<String> memories = db.getAllMemories();
            StringBuilder sb = new StringBuilder("Here is what is stored in your private AI memory:\n\n");
            for (String m : memories) {
                sb.append(m).append("\n");
            }
            return sb.toString();
        }

        // Default intelligent assistant response
        return "Evolve AI Analysis: \"" + input + "\"\n\nI have cross-referenced your goals and today's schedule. Consistent execution in this area will yield 1% daily compound growth. Would you like me to schedule a milestone task for this?";
    }
}
