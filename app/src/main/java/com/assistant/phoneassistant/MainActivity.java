package com.assistant.phoneassistant;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.BatteryManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_SPEECH_RECOGNITION = 201;
    private static final int REQUEST_CAMERA_PERMISSION = 202;

    private DatabaseHelper dbHelper;
    private LocalModelManager modelManager;
    private VoiceManager voiceManager;
    private AndroidActionManager actionManager;

    // Navigation sections
    private LinearLayout sectionHome;
    private LinearLayout sectionAssistant;
    private LinearLayout sectionLearning;
    private LinearLayout sectionVision;
    private LinearLayout sectionActions;
    private BottomNavigationView bottomNavigationView;

    // Home views
    private TextView tvMemoryInfo;
    private TextView tvBatteryInfo;
    private TextView tvDailyBrief;
    private Button btnCleanRam;
    private Button btnScreenTimeSettings;
    private Button btnVoiceSpeak;

    // AI Assistant views
    private TextView tvAiResponse;
    private EditText etUserQuery;
    private Button btnSendQuery;
    private Button chipAlarm;
    private Button chipTimer;
    private Button chipSqlLearn;

    // Learning views
    private TextView tvLearningHeader;
    private TextView tvLessonStatus;
    private TextView tvLessonContent;
    private Button btnCompleteLesson;
    private LearningCurriculumEngine.Lesson currentActiveLesson;

    // Vision Coach views
    private PreviewView previewView;
    private ImageView ivCameraFrame;
    private TextView tvLiveGuidance;
    private TextView tvPhotoScore;
    private TextView tvPhotoFeedback;
    private Button btnCapturePhoto;
    private Button btnCycleCoachMode;

    private ExecutorService cameraExecutor;
    private int coachModeIndex = 0;
    private final String[] coachModes = {"Portrait", "Food", "Landscape", "Document", "Outfit"};

    // Native Actions views
    private Button btnTriggerAlarm;
    private Button btnTriggerTimer;
    private Button btnLaunchYouTube;
    private Button btnLaunchMaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Use thread-safe singleton DatabaseHelper to eliminate lock crashes
        dbHelper = DatabaseHelper.getInstance(this);
        modelManager = new LocalModelManager(this);
        voiceManager = new VoiceManager(this);
        actionManager = new AndroidActionManager(this);

        cameraExecutor = Executors.newSingleThreadExecutor();

        initViews();
        setupNavigation();
        updateDeviceInfo();
        refreshAllData();
        scheduleBackgroundRoutine();
    }

    private void initViews() {
        sectionHome = findViewById(R.id.sectionHome);
        sectionAssistant = findViewById(R.id.sectionAssistant);
        sectionLearning = findViewById(R.id.sectionLearning);
        sectionVision = findViewById(R.id.sectionVision);
        sectionActions = findViewById(R.id.sectionActions);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Home
        tvMemoryInfo = findViewById(R.id.tvMemoryInfo);
        tvBatteryInfo = findViewById(R.id.tvBatteryInfo);
        tvDailyBrief = findViewById(R.id.tvDailyBrief);
        btnCleanRam = findViewById(R.id.btnCleanRam);
        btnScreenTimeSettings = findViewById(R.id.btnScreenTimeSettings);
        btnVoiceSpeak = findViewById(R.id.btnVoiceSpeak);

        btnCleanRam.setOnClickListener(v -> cleanBackgroundApps());
        btnScreenTimeSettings.setOnClickListener(v -> openScreenTimeSettings());

        // 1. FIXED SPEAK BUTTON: Launches Speech-To-Text intent to listen to user
        btnVoiceSpeak.setOnClickListener(v -> startVoiceRecognition());

        // Assistant
        tvAiResponse = findViewById(R.id.tvAiResponse);
        etUserQuery = findViewById(R.id.etUserQuery);
        btnSendQuery = findViewById(R.id.btnSendQuery);
        chipAlarm = findViewById(R.id.chipAlarm);
        chipTimer = findViewById(R.id.chipTimer);
        chipSqlLearn = findViewById(R.id.chipSqlLearn);

        btnSendQuery.setOnClickListener(v -> {
            String query = etUserQuery.getText().toString().trim();
            if (!query.isEmpty()) {
                String reply = AIAssistantEngine.processQuery(query, dbHelper, modelManager, actionManager);
                tvAiResponse.setText(reply);
                etUserQuery.setText("");
                refreshAllData();
                voiceManager.speak(reply);
            }
        });

        chipAlarm.setOnClickListener(v -> {
            String reply = actionManager.setNativeAlarm(4, 30, "Evolve Morning Protocol", true);
            tvAiResponse.setText(reply);
            voiceManager.speak(reply);
        });

        chipTimer.setOnClickListener(v -> {
            String reply = actionManager.setNativeTimer(600, "Evolve 10m Focus Session", true);
            tvAiResponse.setText(reply);
            voiceManager.speak(reply);
        });

        chipSqlLearn.setOnClickListener(v -> {
            String reply = AIAssistantEngine.processQuery("Schedule SQL learning every day at 8 PM", dbHelper, modelManager, actionManager);
            tvAiResponse.setText(reply);
            refreshAllData();
            voiceManager.speak(reply);
        });

        // Learning Dashboard
        tvLearningHeader = findViewById(R.id.tvLearningHeader);
        tvLessonStatus = findViewById(R.id.tvLessonStatus);
        tvLessonContent = findViewById(R.id.tvLessonContent);
        btnCompleteLesson = findViewById(R.id.btnCompleteLesson);

        btnCompleteLesson.setOnClickListener(v -> {
            if (currentActiveLesson != null) {
                dbHelper.markLessonCompleted(currentActiveLesson.id);
                Toast.makeText(this, "Lesson marked COMPLETED! Quiz recorded.", Toast.LENGTH_SHORT).show();
                refreshLearningView();
            } else {
                Toast.makeText(this, "No active lesson loaded.", Toast.LENGTH_SHORT).show();
            }
        });

        // Vision Coach
        previewView = findViewById(R.id.previewView);
        ivCameraFrame = findViewById(R.id.ivCameraFrame);
        tvLiveGuidance = findViewById(R.id.tvLiveGuidance);
        tvPhotoScore = findViewById(R.id.tvPhotoScore);
        tvPhotoFeedback = findViewById(R.id.tvPhotoFeedback);
        btnCapturePhoto = findViewById(R.id.btnCapturePhoto);
        btnCycleCoachMode = findViewById(R.id.btnCycleCoachMode);

        btnCycleCoachMode.setOnClickListener(v -> {
            coachModeIndex = (coachModeIndex + 1) % coachModes.length;
            btnCycleCoachMode.setText("Mode: " + coachModes[coachModeIndex]);
            Toast.makeText(this, "Camera Mode: " + coachModes[coachModeIndex], Toast.LENGTH_SHORT).show();
        });

        btnCapturePhoto.setOnClickListener(v -> {
            if (previewView.getBitmap() != null) {
                Bitmap bmp = previewView.getBitmap();
                VisionCoachEngine.AnalysisResult res = VisionCoachEngine.analyzeBitmap(bmp, coachModes[coachModeIndex]);
                tvLiveGuidance.setText(res.guidance);
                tvPhotoScore.setText("Score: " + res.overallScore + "/100 | Brightness: " + res.brightnessScore + " | Contrast: " + res.contrastScore + " | Sharpness: " + res.sharpnessScore);
                tvPhotoFeedback.setText("Positive: " + res.positiveFeedback + "\nImprovement: " + res.improvementFeedback);
                voiceManager.speak(res.guidance);
            } else {
                Toast.makeText(this, "Frame buffer aligning...", Toast.LENGTH_SHORT).show();
            }
        });

        // Native Actions Direct Triggers
        btnTriggerAlarm = findViewById(R.id.btnTriggerAlarm);
        btnTriggerTimer = findViewById(R.id.btnTriggerTimer);
        btnLaunchYouTube = findViewById(R.id.btnLaunchYouTube);
        btnLaunchMaps = findViewById(R.id.btnLaunchMaps);

        btnTriggerAlarm.setOnClickListener(v -> {
            String res = actionManager.setNativeAlarm(4, 30, "Evolve Morning Routine", true);
            Toast.makeText(this, res, Toast.LENGTH_LONG).show();
            voiceManager.speak(res);
        });

        btnTriggerTimer.setOnClickListener(v -> {
            String res = actionManager.setNativeTimer(600, "10-Minute Deep Work", true);
            Toast.makeText(this, res, Toast.LENGTH_LONG).show();
            voiceManager.speak(res);
        });

        btnLaunchYouTube.setOnClickListener(v -> {
            String res = actionManager.launchAppByName("YouTube");
            Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
        });

        btnLaunchMaps.setOnClickListener(v -> {
            String res = actionManager.startNavigation("Nearby Gym");
            Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Start on-device Speech-To-Text voice recognition.
     */
    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Listening for Evolve AI command...");
        try {
            startActivityForResult(intent, REQUEST_SPEECH_RECOGNITION);
        } catch (Exception e) {
            Toast.makeText(this, "Voice recognition not supported on device.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Start CameraX PreviewView stream.
     */
    private void startCameraX() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview);

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Failed to start camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraX();
            } else {
                Toast.makeText(this, "Camera permission required for Vision Coach.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SPEECH_RECOGNITION && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);
                etUserQuery.setText(spokenText);
                bottomNavigationView.setSelectedItemId(R.id.nav_assistant);
                String reply = AIAssistantEngine.processQuery(spokenText, dbHelper, modelManager, actionManager);
                tvAiResponse.setText(reply);
                refreshAllData();
                voiceManager.speak(reply);
            }
        }
    }

    private void setupNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            sectionHome.setVisibility(View.GONE);
            sectionAssistant.setVisibility(View.GONE);
            sectionLearning.setVisibility(View.GONE);
            sectionVision.setVisibility(View.GONE);
            sectionActions.setVisibility(View.GONE);

            if (id == R.id.nav_home) {
                sectionHome.setVisibility(View.VISIBLE);
                updateDeviceInfo();
                return true;
            } else if (id == R.id.nav_assistant) {
                sectionAssistant.setVisibility(View.VISIBLE);
                return true;
            } else if (id == R.id.nav_learning) {
                sectionLearning.setVisibility(View.VISIBLE);
                refreshLearningView();
                return true;
            } else if (id == R.id.nav_vision) {
                sectionVision.setVisibility(View.VISIBLE);
                startCameraX();
                return true;
            } else if (id == R.id.nav_actions) {
                sectionActions.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });
    }

    private void refreshLearningView() {
        currentActiveLesson = dbHelper.getNextReadyLesson();
        if (currentActiveLesson != null) {
            tvLearningHeader.setText("SUBJECT: " + currentActiveLesson.subject.toUpperCase() + " • " + currentActiveLesson.moduleName);
            tvLessonStatus.setText("Status: " + currentActiveLesson.status + " (Duration: " + currentActiveLesson.durationMinutes + " mins)");
            tvLessonContent.setText("### " + currentActiveLesson.title + "\n\n" + currentActiveLesson.notesContent);
        } else {
            tvLearningHeader.setText("SMART LEARNING PREPARATION");
            tvLessonStatus.setText("Status: No active curricula scheduled");
            tvLessonContent.setText("Schedule any subject from the Jarvis assistant tab (e.g. 'Schedule SQL learning every day at 8 PM').\n\nLessons, theory notes, and quizzes will be generated and cached locally in SQLite for offline access.");
        }
    }

    private void refreshAllData() {
        List<String> tasks = dbHelper.getAllTasks();
        List<String> memories = dbHelper.getAllMemories();

        StringBuilder sb = new StringBuilder();
        sb.append("Local Model: ").append(modelManager.getActiveModelId().toUpperCase()).append("\n");
        sb.append("Tasks: ").append(tasks.size()).append(" scheduled | Memories Stored: ").append(memories.size());

        tvDailyBrief.setText(sb.toString());
        refreshLearningView();
    }

    private void updateDeviceInfo() {
        ActivityManager actManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        if (actManager != null) {
            actManager.getMemoryInfo(memInfo);
            long availMb = memInfo.availMem / (1024 * 1024);
            long totalMb = memInfo.totalMem / (1024 * 1024);
            tvMemoryInfo.setText("Available RAM: " + availMb + " MB / " + totalMb + " MB");
        }

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
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        super.onDestroy();
    }
}
