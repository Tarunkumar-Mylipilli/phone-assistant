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

    // Navigation sections
    private LinearLayout sectionHome;
    private LinearLayout sectionAssistant;
    private LinearLayout sectionPlanner;
    private LinearLayout sectionMemory;
    private BottomNavigationView bottomNavigationView;

    // Home views
    private TextView tvMemoryInfo;
    private TextView tvBatteryInfo;
    private TextView tvDailyBrief;
    private Button btnCleanRam;
    private Button btnScreenTimeSettings;

    // AI Assistant views
    private TextView tvAiResponse;
    private EditText etUserQuery;
    private Button btnSendQuery;
    private Button chipBriefing;
    private Button chipCareer;

    // My Day Planner views
    private TextView tvPlannerList;
    private Button btnAddNewTask;

    // AI Memory views
    private TextView tvMemoryList;
    private Button btnClearMemory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupNavigation();
        updateDeviceInfo();
        refreshPlannerAndMemory();
        scheduleBackgroundRoutine();
    }

    private void initViews() {
        sectionHome = findViewById(R.id.sectionHome);
        sectionAssistant = findViewById(R.id.sectionAssistant);
        sectionPlanner = findViewById(R.id.sectionPlanner);
        sectionMemory = findViewById(R.id.sectionMemory);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Home
        tvMemoryInfo = findViewById(R.id.tvMemoryInfo);
        tvBatteryInfo = findViewById(R.id.tvBatteryInfo);
        tvDailyBrief = findViewById(R.id.tvDailyBrief);
        btnCleanRam = findViewById(R.id.btnCleanRam);
        btnScreenTimeSettings = findViewById(R.id.btnScreenTimeSettings);

        btnCleanRam.setOnClickListener(v -> cleanBackgroundApps());
        btnScreenTimeSettings.setOnClickListener(v -> openScreenTimeSettings());

        // Assistant
        tvAiResponse = findViewById(R.id.tvAiResponse);
        etUserQuery = findViewById(R.id.etUserQuery);
        btnSendQuery = findViewById(R.id.btnSendQuery);
        chipBriefing = findViewById(R.id.chipBriefing);
        chipCareer = findViewById(R.id.chipCareer);

        btnSendQuery.setOnClickListener(v -> {
            String query = etUserQuery.getText().toString().trim();
            if (!query.isEmpty()) {
                String reply = AIAssistantEngine.processQuery(query, dbHelper);
                tvAiResponse.setText(reply);
                etUserQuery.setText("");
                refreshPlannerAndMemory();
            }
        });

        chipBriefing.setOnClickListener(v -> {
            String reply = AIAssistantEngine.processQuery("daily briefing", dbHelper);
            tvAiResponse.setText(reply);
        });

        chipCareer.setOnClickListener(v -> {
            String reply = AIAssistantEngine.processQuery("career plan", dbHelper);
            tvAiResponse.setText(reply);
        });

        // Planner
        tvPlannerList = findViewById(R.id.tvPlannerList);
        btnAddNewTask = findViewById(R.id.btnAddNewTask);
        btnAddNewTask.setOnClickListener(v -> {
            dbHelper.addTask("New Personal Growth Milestone", "Daily");
            refreshPlannerAndMemory();
            Toast.makeText(this, "Milestone added to your plan!", Toast.LENGTH_SHORT).show();
        });

        // Memory
        tvMemoryList = findViewById(R.id.tvMemoryList);
        btnClearMemory = findViewById(R.id.btnClearMemory);
        btnClearMemory.setOnClickListener(v -> {
            dbHelper.clearAllMemories();
            refreshPlannerAndMemory();
            Toast.makeText(this, "Persistent AI Memory cleared.", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            sectionHome.setVisibility(View.GONE);
            sectionAssistant.setVisibility(View.GONE);
            sectionPlanner.setVisibility(View.GONE);
            sectionMemory.setVisibility(View.GONE);

            if (id == R.id.nav_home) {
                sectionHome.setVisibility(View.VISIBLE);
                updateDeviceInfo();
                return true;
            } else if (id == R.id.nav_assistant) {
                sectionAssistant.setVisibility(View.VISIBLE);
                return true;
            } else if (id == R.id.nav_planner) {
                sectionPlanner.setVisibility(View.VISIBLE);
                refreshPlannerAndMemory();
                return true;
            } else if (id == R.id.nav_memory) {
                sectionMemory.setVisibility(View.VISIBLE);
                refreshPlannerAndMemory();
                return true;
            }
            return false;
        });
    }

    private void refreshPlannerAndMemory() {
        // Refresh Tasks
        List<String> tasks = dbHelper.getAllTasks();
        StringBuilder taskSb = new StringBuilder();
        for (String t : tasks) {
            taskSb.append(t).append("\n\n");
        }
        tvPlannerList.setText(taskSb.length() > 0 ? taskSb.toString() : "No tasks scheduled. Add your first goal!");

        // Refresh Memory
        List<String> memories = dbHelper.getAllMemories();
        StringBuilder memSb = new StringBuilder();
        for (String m : memories) {
            memSb.append(m).append("\n\n");
        }
        tvMemoryList.setText(memSb.length() > 0 ? memSb.toString() : "Memory store is empty. Teach Evolve AI using 'remember <note>'");

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
}
