package com.assistant.phoneassistant;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

/**
 * ModelDownloadManager handles background downloading of open-source GGUF models
 * using Android's native system DownloadManager with zero paid dependencies.
 */
public class ModelDownloadManager {

    private Context context;

    public ModelDownloadManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public long startModelDownload(ModelInfo model) {
        try {
            File modelsDir = context.getExternalFilesDir("models");
            if (modelsDir != null && !modelsDir.exists()) {
                modelsDir.mkdirs();
            }

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(model.getDownloadUrl()));
            request.setTitle("Downloading AI Model: " + model.getName());
            request.setDescription("Evolve AI Local Brain (" + model.getParameterSize() + ")");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalFilesDir(context, "models", model.getId() + ".gguf");
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(false);

            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager != null) {
                return downloadManager.enqueue(request);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean isModelDownloaded(String modelId) {
        File dir = context.getExternalFilesDir("models");
        if (dir == null) return false;
        File file = new File(dir, modelId + ".gguf");
        return file.exists() && file.length() > 1000000L; // At least 1MB
    }

    public File getModelFile(String modelId) {
        File dir = context.getExternalFilesDir("models");
        if (dir == null) return null;
        File file = new File(dir, modelId + ".gguf");
        return file.exists() ? file : null;
    }
}
