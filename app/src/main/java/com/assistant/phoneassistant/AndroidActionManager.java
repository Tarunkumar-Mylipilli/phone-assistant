package com.assistant.phoneassistant;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.provider.Settings;

import java.util.Calendar;

/**
 * AndroidActionManager: Centralized Native Android System Controller.
 * Safely executes native clock alarms, timers, notes, calendar events, app launches, and navigation.
 * All launches are wrapped in try-catch to prevent ActivityNotFoundException crashes.
 */
public class AndroidActionManager {

    private Context context;

    public AndroidActionManager(Context context) {
        this.context = context.getApplicationContext();
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

            context.startActivity(intent);
            return "Successfully scheduled native Clock Alarm for " + String.format("%02d:%02d", hour, minutes) + " (" + message + ").";
        } catch (ActivityNotFoundException e) {
            return "No Clock application found to handle the alarm request.";
        } catch (Exception e) {
            return "Failed to schedule alarm: " + e.getMessage();
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

            context.startActivity(intent);
            int mins = seconds / 60;
            return "Successfully scheduled native Clock Timer for " + mins + " minute(s).";
        } catch (ActivityNotFoundException e) {
            return "No Clock/Timer application found to handle countdown timer.";
        } catch (Exception e) {
            return "Failed to schedule timer: " + e.getMessage();
        }
    }

    /**
     * Launch any installed Android application by name with Android 11+ package visibility support.
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
            else if (query.contains("spotify")) targetPkg = "com.spotify.music";
            else if (query.contains("clock") || query.contains("alarm")) {
                Intent clockIntent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
                clockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    context.startActivity(clockIntent);
                    return "Opened Clock.";
                } catch (Exception ignored) {
                    targetPkg = "com.google.android.deskclock";
                }
            } else if (query.contains("camera")) {
                Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                camIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    context.startActivity(camIntent);
                    return "Opened Camera.";
                } catch (ActivityNotFoundException e) {
                    return "Camera application not available.";
                }
            } else if (query.contains("settings")) {
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

            // Fallback: search for intent by name substring across installed launchable apps
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            for (android.content.pm.ResolveInfo ri : pm.queryIntentActivities(mainIntent, 0)) {
                String label = ri.loadLabel(pm).toString().toLowerCase();
                if (label.contains(query) || ri.activityInfo.packageName.toLowerCase().contains(query)) {
                    Intent appIntent = pm.getLaunchIntentForPackage(ri.activityInfo.packageName);
                    if (appIntent != null) {
                        appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(appIntent);
                        return "Launched " + ri.loadLabel(pm) + " successfully.";
                    }
                }
            }

            return "App '" + appName + "' not directly found on this device.";
        } catch (ActivityNotFoundException e) {
            return "Application not installed or activity not found.";
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

            context.startActivity(intent);
            return "Created native Calendar Event: '" + title + "' at " + String.format("%02d:%02d", startHour, startMinute) + ".";
        } catch (ActivityNotFoundException e) {
            return "No Calendar application installed to handle event creation.";
        } catch (Exception e) {
            return "Calendar error: " + e.getMessage();
        }
    }

    /**
     * Start navigation via Google Maps.
     */
    public String startNavigation(String destination) {
        try {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(destination));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(mapIntent);
            return "Starting navigation to " + destination + " on Google Maps.";
        } catch (ActivityNotFoundException e) {
            try {
                Intent webMap = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + Uri.encode(destination)));
                webMap.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(webMap);
                return "Opening maps navigation for " + destination + ".";
            } catch (Exception ex) {
                return "Navigation error: Unable to open maps browser fallback.";
            }
        } catch (Exception e) {
            return "Navigation error: " + e.getMessage();
        }
    }
}
