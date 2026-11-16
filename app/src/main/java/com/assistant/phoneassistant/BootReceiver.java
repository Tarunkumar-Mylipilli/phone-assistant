package com.assistant.phoneassistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Re-schedule automatic worker on phone reboot
            PeriodicWorkRequest assistantWork = new PeriodicWorkRequest.Builder(
                    DailyAssistantWorker.class,
                    6, TimeUnit.HOURS
            ).build();

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "PhoneAutoAssistant",
                    ExistingPeriodicWorkPolicy.KEEP,
                    assistantWork
            );
        }
    }
}
