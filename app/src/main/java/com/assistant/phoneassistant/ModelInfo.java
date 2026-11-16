package com.assistant.phoneassistant;

import android.content.Context;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ModelInfo represents an open-source quantized GGUF model tailored
 * specifically for Snapdragon 888 and 8GB Physical RAM.
 */
public class ModelInfo {
    private String id;
    private String name;
    private String parameterSize;
    private String downloadUrl;
    private long sizeBytes;
    private int recommendedRamMb;
    private String description;

    public ModelInfo(String id, String name, String parameterSize, String downloadUrl, long sizeBytes, int recommendedRamMb, String description) {
        this.id = id;
        this.name = name;
        this.parameterSize = parameterSize;
        this.downloadUrl = downloadUrl;
        this.sizeBytes = sizeBytes;
        this.recommendedRamMb = recommendedRamMb;
        this.description = description;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getParameterSize() { return parameterSize; }
    public String getDownloadUrl() { return downloadUrl; }
    public long getSizeBytes() { return sizeBytes; }
    public int getRecommendedRamMb() { return recommendedRamMb; }
    public String getDescription() { return description; }

    public boolean isDownloaded(Context context) {
        File dir = context.getExternalFilesDir("models");
        if (dir == null) return false;
        File file = new File(dir, id + ".gguf");
        return file.exists() && file.length() > 0;
    }
}
