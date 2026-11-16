package com.assistant.phoneassistant;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private TextView tvMemoryInfo;
    private TextView tvBatteryInfo;
    private TextView tvStatus;
    private Button btnCleanRam;
    private Button btnScheduleSync;
    private Button btnScreenTimeSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvMemoryInfo = findViewById(R.id.tvMemoryInfo);
        tvBatteryInfo = findViewById(R.id.tvBatteryInfo);
        tvStatus = findViewById(R.id.tvStatus);
        btnCleanRam = findViewById(R.id.btnCleanRam);
        btnScheduleSync = findViewById(R.id.btnScheduleSync);
        btnScreenTimeSettings = findViewById(R.id.btnScreenTimeSettings);

        updateDeviceInfo();
        scheduleBackgroundRoutine();

        btnCleanRam.setOnClickListener(v -> cleanBackgroundApps());

        btnScheduleSync.setOnClickListener(v -> {
            scheduleBackgroundRoutine();
            Toast.makeText(this, "Everyday Auto-Assistant Activated!", Toast.LENGTH_SHORT).show();
        });

        btnScreenTimeSettings.setOnClickListener(v -> openScreenTimeSettings());
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
            // Free background processes for installed user packages
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
        tvStatus.setText("Status: Background Routine Active (Checks every 6 hrs)");
    }

    private void openScreenTimeSettings() {
        try {
            // Open Android's Digital Wellbeing & Parental Controls directly
            Intent intent = new Intent();
            intent.setComponent(new android.content.ComponentName(
                    "com.google.android.apps.wellbeing",
                    "com.google.android.apps.wellbeing.home.TopLevelSettingsActivity"
            ));
            startActivity(intent);
        } catch (Exception e) {
            // Fallback to normal settings if Wellbeing is not standalone
            Intent fallback = new Intent(Settings.ACTION_SETTINGS);
            startActivity(fallback);
        }
    }
}
