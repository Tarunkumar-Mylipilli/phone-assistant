package com.assistant.phoneassistant;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private LocalModelManager modelManager;
    private VoiceManager voiceManager;

    // Navigation sections
    private LinearLayout sectionHome;
    private LinearLayout sectionAssistant;
    private LinearLayout sectionCareer;
    private LinearLayout sectionHabits;
    private LinearLayout sectionModels;
    private BottomNavigationView bottomNavigationView;

    // Home views
    private TextView tvMemoryInfo;
    private TextView tvBatteryInfo;
    private TextView tvDailyBrief;
    private TextView tvFinanceOverview;
    private Button btnCleanRam;
    private Button btnScreenTimeSettings;
    private Button btnVoiceSpeak;

    // AI Assistant views
    private TextView tvAiResponse;
    private EditText etUserQuery;
    private Button btnSendQuery;
    private Button chipBriefing;
    private Button chipCareer;

    // Career views
    private TextView tvSkillsList;
    private Button btnMockInterview;

    // Habits views
    private TextView tvHabitsList;
    private Button btnCheckInHabit;

    // AI Models views
    private TextView tvModelCatalog;
    private Button btnSwitchEngine;

    private int engineIndex = 0;
    private final String[] engines = {"SmolLM2-1.7B-Q4", "Qwen2.5-1.5B-Q4", "Llama-3.2-1B-Q4", "Local-Rule-Based"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        modelManager = new LocalModelManager(this);
        voiceManager = new VoiceManager(this);

        initViews();
        setupNavigation();
        updateDeviceInfo();
        refreshAllData();
        renderModelCatalog();
        scheduleBackgroundRoutine();
    }

    private void initViews() {
        sectionHome = findViewById(R.id.sectionHome);
        sectionAssistant = findViewById(R.id.sectionAssistant);
        sectionCareer = findViewById(R.id.sectionCareer);
        sectionHabits = findViewById(R.id.sectionHabits);
        sectionModels = findViewById(R.id.sectionModels);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Home
        tvMemoryInfo = findViewById(R.id.tvMemoryInfo);
        tvBatteryInfo = findViewById(R.id.tvBatteryInfo);
        tvDailyBrief = findViewById(R.id.tvDailyBrief);
        tvFinanceOverview = findViewById(R.id.tvFinanceOverview);
        btnCleanRam = findViewById(R.id.btnCleanRam);
        btnScreenTimeSettings = findViewById(R.id.btnScreenTimeSettings);
        btnVoiceSpeak = findViewById(R.id.btnVoiceSpeak);

        btnCleanRam.setOnClickListener(v -> cleanBackgroundApps());
        btnScreenTimeSettings.setOnClickListener(v -> openScreenTimeSettings());
        btnVoiceSpeak.setOnClickListener(v -> {
            String briefText = tvDailyBrief.getText().toString();
            voiceManager.speak("Welcome to Evolve A I. " + briefText);
            Toast.makeText(this, "Speaking daily briefing...", Toast.LENGTH_SHORT).show();
        });

        // Assistant
        tvAiResponse = findViewById(R.id.tvAiResponse);
        etUserQuery = findViewById(R.id.etUserQuery);
        btnSendQuery = findViewById(R.id.btnSendQuery);
        chipBriefing = findViewById(R.id.chipBriefing);
        chipCareer = findViewById(R.id.chipCareer);

        btnSendQuery.setOnClickListener(v -> {
            String query = etUserQuery.getText().toString().trim();
            if (!query.isEmpty()) {
                String reply = AIAssistantEngine.processQuery(query, dbHelper, modelManager);
                tvAiResponse.setText(reply);
                etUserQuery.setText("");
                refreshAllData();
                voiceManager.speak(reply);
            }
        });

        chipBriefing.setOnClickListener(v -> {
            String reply = AIAssistantEngine.processQuery("daily briefing", dbHelper, modelManager);
            tvAiResponse.setText(reply);
            voiceManager.speak(reply);
        });

        chipCareer.setOnClickListener(v -> {
            String reply = AIAssistantEngine.processQuery("career plan", dbHelper, modelManager);
            tvAiResponse.setText(reply);
            voiceManager.speak(reply);
        });

        // Career
        tvSkillsList = findViewById(R.id.tvSkillsList);
        btnMockInterview = findViewById(R.id.btnMockInterview);
        btnMockInterview.setOnClickListener(v -> {
            bottomNavigationView.setSelectedItemId(R.id.nav_assistant);
            String reply = "Evolve Mock Interviewer: Question 1:\n\n'In a distributed microservice system, how would you design an idempotent payment processing API to handle network retries without double-charging the customer?'\n\nType your answer below:";
            tvAiResponse.setText(reply);
            voiceManager.speak(reply);
        });

        // Habits
        tvHabitsList = findViewById(R.id.tvHabitsList);
        btnCheckInHabit = findViewById(R.id.btnCheckInHabit);
        btnCheckInHabit.setOnClickListener(v -> {
            dbHelper.checkInHabit("Coding");
            dbHelper.checkInHabit("Routine");
            refreshAllData();
            Toast.makeText(this, "Checked in! Streaks incremented.", Toast.LENGTH_SHORT).show();
        });

        // Models
        tvModelCatalog = findViewById(R.id.tvModelCatalog);
        btnSwitchEngine = findViewById(R.id.btnSwitchEngine);
        btnSwitchEngine.setOnClickListener(v -> {
            engineIndex = (engineIndex + 1) % engines.length;
            modelManager.setActiveModelId(engines[engineIndex]);
            renderModelCatalog();
            Toast.makeText(this, "Switched active AI Engine to: " + engines[engineIndex], Toast.LENGTH_SHORT).show();
        });
    }

    private void renderModelCatalog() {
        StringBuilder sb = new StringBuilder();
        sb.append("Active Engine: ").append(modelManager.getActiveModelId().toUpperCase()).append("\n");
        sb.append("Target Device: Realme GT 2 (Snapdragon 888, 8GB RAM)\n\n");

        for (ModelInfo m : modelManager.getAvailableModels()) {
            sb.append("• ").append(m.getName()).append("\n");
            sb.append("  Specs: ").append(m.getParameterSize()).append(" | RAM Req: ~").append(m.getRecommendedRamMb()).append(" MB\n");
            sb.append("  Note: ").append(m.getDescription()).append("\n\n");
        }
        sb.append("Operating Rule: Zero paid APIs. Models run 100% on-device or via fallback intelligence.");
        tvModelCatalog.setText(sb.toString());
    }

    private void setupNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            sectionHome.setVisibility(View.GONE);
            sectionAssistant.setVisibility(View.GONE);
            sectionCareer.setVisibility(View.GONE);
            sectionHabits.setVisibility(View.GONE);
            sectionModels.setVisibility(View.GONE);

            if (id == R.id.nav_home) {
                sectionHome.setVisibility(View.VISIBLE);
                updateDeviceInfo();
                return true;
            } else if (id == R.id.nav_assistant) {
                sectionAssistant.setVisibility(View.VISIBLE);
                return true;
            } else if (id == R.id.nav_career) {
                sectionCareer.setVisibility(View.VISIBLE);
                refreshAllData();
                return true;
            } else if (id == R.id.nav_habits) {
                sectionHabits.setVisibility(View.VISIBLE);
                refreshAllData();
                return true;
            } else if (id == R.id.nav_models) {
                sectionModels.setVisibility(View.VISIBLE);
                renderModelCatalog();
                return true;
            }
            return false;
        });
    }

    private void refreshAllData() {
        // Refresh Tasks
        List<String> tasks = dbHelper.getAllTasks();

        // Refresh Skills
        List<String> skills = dbHelper.getAllSkills();
        StringBuilder skillSb = new StringBuilder();
        for (String s : skills) {
            skillSb.append(s).append("\n\n");
        }
        tvSkillsList.setText(skillSb.length() > 0 ? skillSb.toString() : "No technical roadmaps loaded.");

        // Refresh Habits
        List<String> habits = dbHelper.getAllHabits();
        StringBuilder habitSb = new StringBuilder();
        for (String h : habits) {
            habitSb.append(h).append("\n\n");
        }
        tvHabitsList.setText(habitSb.length() > 0 ? habitSb.toString() : "No active streaks recorded.");

        // Finance Overview
        tvFinanceOverview.setText(dbHelper.getFinanceSummary());

        // Update Briefing
        tvDailyBrief.setText("Good day. " + tasks.size() + " active milestones scheduled across your career, health, and daily routines.");
    }

    private void updateDeviceInfo() {
        // RAM info
        ActivityManager actManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        if (actManager != null) {
            actManager.getMemoryInfo(memInfo);
            long availMb = memInfo.availMem / (1024 * 1024);
            long totalMb = memInfo.totalMem / (1024 * 1024);
            tvMemoryInfo.setText("Available RAM: " + availMb + " MB / " + totalMb + " MB");
        }

        // Battery info
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batteryPct = (int) ((level / (float) scale) * 100);
            tvBatteryInfo.setText("Battery Level: " + batteryPct + "%");
        }
    }

    private void cleanBackgroundApps() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            for (ActivityManager.RunningAppProcessInfo process : am.getRunningAppProcesses()) {
                if (!process.processName.equals(getPackageName())) {
                    am.killBackgroundProcesses(process.processName);
                }
            }
        }
        updateDeviceInfo();
        Toast.makeText(this, "Unnecessary background tasks cleared!", Toast.LENGTH_SHORT).show();
    }

    private void scheduleBackgroundRoutine() {
        PeriodicWorkRequest assistantWork = new PeriodicWorkRequest.Builder(
                DailyAssistantWorker.class,
                6, TimeUnit.HOURS
        ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "PhoneAutoAssistant",
                ExistingPeriodicWorkPolicy.KEEP,
                assistantWork
        );
    }

    private void openScreenTimeSettings() {
        try {
            Intent intent = new Intent();
            intent.setComponent(new android.content.ComponentName(
                    "com.google.android.apps.wellbeing",
                    "com.google.android.apps.wellbeing.home.TopLevelSettingsActivity"
            ));
            startActivity(intent);
        } catch (Exception e) {
            Intent fallback = new Intent(Settings.ACTION_SETTINGS);
            startActivity(fallback);
        }
    }

    @Override
    protected void onDestroy() {
        if (voiceManager != null) {
            voiceManager.shutdown();
        }
        super.onDestroy();
    }
}
