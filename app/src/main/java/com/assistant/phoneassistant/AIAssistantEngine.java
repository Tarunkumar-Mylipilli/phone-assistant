package com.assistant.phoneassistant;

import java.util.List;
import java.util.Locale;

public class AIAssistantEngine {

    public static String processQuery(String input, DatabaseHelper db, LocalModelManager modelManager) {
        if (input == null || input.trim().isEmpty()) {
            return "I am Evolve AI, your personal life operating system. How can I assist you with your day, career, or fitness?";
        }

        String query = input.toLowerCase(Locale.ROOT).trim();

        // 1. Memory commands
        if (query.startsWith("remember ") || query.startsWith("save memory ")) {
            String memoryText = input.replaceFirst("(?i)remember |(?i)save memory ", "").trim();
            db.addMemory("User Fact", memoryText);
            return "Saved to persistent AI Memory: \"" + memoryText + "\"\n\nI will remember this context during future briefings and advice.";
        }

        // 2. Schedule & Planning commands
        if (query.startsWith("add task ") || query.startsWith("plan ") || query.startsWith("schedule ")) {
            String taskText = input.replaceFirst("(?i)add task |(?i)plan |(?i)schedule ", "").trim();
            db.addTask(taskText, "Daily");
            return "Scheduled in My Day: \"" + taskText + "\"\n\nPrioritized and integrated with today's milestones.";
        }

        // 3. Daily Operating Briefing
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

        // 4. Career Growth Coach
        if (query.contains("career") || query.contains("job") || query.contains("interview") || query.contains("skill") || query.contains("code") || query.contains("dsa")) {
            return "Evolve Career Coach Protocol:\n\n" +
                    "• 1. Data Structures & Algorithms: Complete 1 Medium problem daily (focus on Binary Search & Two Pointers).\n" +
                    "• 2. System Design: Review distributed caching & database indexing strategies.\n" +
                    "• 3. Portfolio Review: Ensure GitHub demonstrates production-grade tests & clean documentation.\n\n" +
                    "Would you like me to start a 5-question technical mock interview session?";
        }

        // 5. Fitness & Nutrition Coach
        if (query.contains("fitness") || query.contains("workout") || query.contains("meal") || query.contains("diet") || query.contains("exercise")) {
            return "Evolve Wellness & Fitness Protocol:\n\n" +
                    "• Conditioning: 30-min targeted hypertrophy / core strength session.\n" +
                    "• Nutrition Target: High-protein clean vegetarian options (paneer, tofu, lentils, Greek yogurt, sprouts).\n" +
                    "• Hydration: Aim for 3.0 Liters total (Tracked via daily routine).\n\n" +
                    "Shall I add this workout to your scheduled tasks?";
        }

        // 6. Memory retrieval
        if (query.contains("what do you remember") || query.contains("memory") || query.contains("who am i")) {
            List<String> memories = db.getAllMemories();
            StringBuilder sb = new StringBuilder("Evolve AI Memory Vault:\n\n");
            for (String m : memories) {
                sb.append(m).append("\n");
            }
            sb.append("\nYou can delete or add memories at any time under the AI Memory tab.");
            return sb.toString();
        }

        // 7. Teach Me Mode
        if (query.startsWith("teach me ") || query.contains("teach me")) {
            String topic = input.replaceFirst("(?i)teach me ", "").trim();
            return "Evolve 'TEACH ME' Mode Activated: [" + topic.toUpperCase() + "]\n\n" +
                    "1. Core Concept: " + topic + " solves foundational bottlenecks by isolating logic and establishing predictable invariants.\n" +
                    "2. Practical Example: Think of it like caching in system design—instead of recalculating, reuse validated outputs.\n\n" +
                    "Check question: Can you explain in one sentence why this is beneficial over brute force?";
        }

        // Context-aware fallback response
        return "Evolve AI Analysis: \"" + input + "\"\n\n" +
                "Engine: " + modelManager.getActiveModelId() + "\n" +
                "Guidance: Every action should connect directly to your personal career, discipline, or health targets. Would you like me to convert this thought into a daily milestone?";
    }
}
