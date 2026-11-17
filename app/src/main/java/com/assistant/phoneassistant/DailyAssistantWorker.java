package com.assistant.phoneassistant;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;

public class DailyAssistantWorker extends Worker {

    public static final String CHANNEL_ID = "phone_assistant_channel";

    public DailyAssistantWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        DatabaseHelper db = new DatabaseHelper(context);

        // 1. Clear background cache processes safely
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            for (ActivityManager.RunningAppProcessInfo proc : am.getRunningAppProcesses()) {
                if (!proc.processName.equals(context.getPackageName())) {
                    am.killBackgroundProcesses(proc.processName);
                }
            }
        }

        // 2. Check scheduled learning sessions and pre-session preparation
        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);

        LearningCurriculumEngine.Lesson nextLesson = db.getNextReadyLesson();
        if (nextLesson != null && (currentHour >= 19 && currentHour <= 21)) {
            // Targeted evening learning session notification (8 PM window)
            showNotification(context, "Evolve AI • Learning Session Ready",
                    "Your " + nextLesson.subject + " lesson is ready: \"" + nextLesson.title + "\". Tap to begin.");
        } else {
            showNotification(context, "Evolve AI Routine", "Background system maintenance complete: Memory and routines aligned.");
        }

        return Result.success();
    }

    private void showNotification(Context context, String title, String message) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Assistant Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            nm.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        nm.notify(101, builder.build());
    }
}
