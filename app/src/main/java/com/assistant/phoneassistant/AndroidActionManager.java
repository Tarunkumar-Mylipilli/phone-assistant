package com.assistant.phoneassistant;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Toast;

import java.util.Calendar;

/**
 * AndroidActionManager: Centralized Native Android System Controller.
 * Handles Alarms, Timers, App Launching, Calendar Events, Notes, and System Settings.
 */
public class AndroidActionManager {

    private Context context;

    public AndroidActionManager(Context context) {
        this.context = context;
    }

    /**
     * Set a real native clock alarm via AlarmClock.ACTION_SET_ALARM.
     */
    public String setNativeAlarm(int hour, int minutes, String message, boolean skipUi) {
        try {
            Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
            intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
            intent.putExtra(AlarmClock.EXTRA_MINUTES, minutes);
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, message != null ? message : "Evolve AI Alarm");
            intent.putExtra(AlarmClock.EXTRA_SKIP_UI, skipUi);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                return "Successfully scheduled native Clock Alarm for " + String.format("%02d:%02d", hour, minutes) + " (" + message + ").";
            } else {
                return "Unable to schedule alarm: No default Clock application found on device.";
            }
        } catch (Exception e) {
            return "Alarm error: " + e.getMessage();
        }
    }

    /**
     * Set a real native countdown timer via AlarmClock.ACTION_SET_TIMER.
     */
    public String setNativeTimer(int seconds, String message, boolean skipUi) {
        try {
            Intent intent = new Intent(AlarmClock.ACTION_SET_TIMER);
            intent.putExtra(AlarmClock.EXTRA_LENGTH, seconds);
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, message != null ? message : "Evolve AI Timer");
            intent.putExtra(AlarmClock.EXTRA_SKIP_UI, skipUi);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                int mins = seconds / 60;
                return "Successfully scheduled native Clock Timer for " + mins + " minute(s).";
            } else {
                return "Unable to schedule timer: No default Timer/Clock app detected.";
            }
        } catch (Exception e) {
            return "Timer error: " + e.getMessage();
        }
    }

    /**
     * Launch any installed Android application by name (e.g., YouTube, WhatsApp, Maps, Chrome, Camera).
     */
    public String launchAppByName(String appName) {
        try {
            PackageManager pm = context.getPackageManager();
            String query = appName.toLowerCase().trim();
            String targetPkg = null;

            if (query.contains("youtube")) targetPkg = "com.google.android.youtube";
            else if (query.contains("whatsapp")) targetPkg = "com.whatsapp";
            else if (query.contains("maps") || query.contains("navigation")) targetPkg = "com.google.android.apps.maps";
            else if (query.contains("chrome") || query.contains("browser")) targetPkg = "com.android.chrome";
            else if (query.contains("camera")) {
                Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                camIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (camIntent.resolveActivity(pm) != null) {
                    context.startActivity(camIntent);
                    return "Opened Camera.";
                }
            } else if (query.contains("spotify")) targetPkg = "com.spotify.music";
            else if (query.contains("settings")) {
                Intent sIntent = new Intent(Settings.ACTION_SETTINGS);
                sIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(sIntent);
                return "Opened Android System Settings.";
            }

            if (targetPkg != null) {
                Intent launchIntent = pm.getLaunchIntentForPackage(targetPkg);
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(launchIntent);
                    return "Launched " + appName + " successfully.";
                }
            }

            return "App '" + appName + "' not found directly. Please ensure it is installed on your Realme GT 2.";
        } catch (Exception e) {
            return "Failed to open app: " + e.getMessage();
        }
    }

    /**
     * Create real native Calendar event via CalendarContract.
     */
    public String createCalendarEvent(String title, String description, int startHour, int startMinute, int durationMinutes) {
        try {
            Calendar beginTime = Calendar.getInstance();
            beginTime.set(Calendar.HOUR_OF_DAY, startHour);
            beginTime.set(Calendar.MINUTE, startMinute);

            Calendar endTime = (Calendar) beginTime.clone();
            endTime.add(Calendar.MINUTE, durationMinutes);

            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                    .putExtra(CalendarContract.Events.TITLE, title)
                    .putExtra(CalendarContract.Events.DESCRIPTION, description)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                return "Created native Calendar Event: '" + title + "' at " + String.format("%02d:%02d", startHour, startMinute) + ".";
            } else {
                return "No Calendar application found to record the event.";
            }
        } catch (Exception e) {
            return "Calendar error: " + e.getMessage();
        }
    }

    /**
     * Start navigation via Google Maps intent.
     */
    public String startNavigation(String destination) {
        try {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(destination));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(mapIntent);
                return "Starting navigation to " + destination + " on Google Maps.";
            } else {
                Intent webMap = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + Uri.encode(destination)));
                webMap.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(webMap);
                return "Opening maps navigation for " + destination + ".";
            }
        } catch (Exception e) {
            return "Navigation error: " + e.getMessage();
        }
    }
}
