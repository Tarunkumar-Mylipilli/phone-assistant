package com.assistant.phoneassistant;

import android.content.Context;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * LocalModelManager handles discovery, verification, RAM budget constraints,
 * and lifecycle of quantized GGUF models on device.
 */
public class LocalModelManager {

    private Context context;
    private List<ModelInfo> availableModels;
    private String activeModelId = "rule_based";

    public LocalModelManager(Context context) {
        this.context = context;
        initCatalog();
    }

    private void initCatalog() {
        availableModels = new ArrayList<>();

        // 1. SmolLM2 1.7B Q4_K_M (Optimal for Snapdragon 888 & 8GB RAM)
        availableModels.add(new ModelInfo(
                "smollm2-1.7b-q4",
                "SmolLM2 Instruct (Recommended)",
                "1.7B Parameters (Q4_K_M)",
                "https://huggingface.co/HuggingFaceTB/SmolLM2-1.7B-Instruct-GGUF/resolve/main/smollm2-1.7b-instruct-q4_k_m.gguf",
                1050000000L, // ~1.05 GB
                1800,        // ~1.8 GB RAM footprint
                "Ultra-fast conversational reasoning tailored for on-device assistant tasks without heating."
        ));

        // 2. Qwen2.5 1.5B Instruct Q4_K_M (High Coding & Task Reasoning)
        availableModels.add(new ModelInfo(
                "qwen2.5-1.5b-q4",
                "Qwen 2.5 Technical Coach",
                "1.5B Parameters (Q4_K_M)",
                "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf",
                980000000L,  // ~980 MB
                1600,        // ~1.6 GB RAM footprint
                "Superior technical and career problem solving, math logic, and code explanations."
        ));

        // 3. Llama 3.2 1B Instruct Q4_K_M (Ultralight Background Agent)
        availableModels.add(new ModelInfo(
                "llama-3.2-1b-q4",
                "Llama 3.2 Ultralight",
                "1.2B Parameters (Q4_K_M)",
                "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q4_K_M.gguf",
                750000000L,  // ~750 MB
                1200,        // ~1.2 GB RAM footprint
                "Extremely lightweight model, uses minimal battery while running background evaluations."
        ));
    }

    public List<ModelInfo> getAvailableModels() {
        return availableModels;
    }

    public String getActiveModelId() {
        return activeModelId;
    }

    public void setActiveModelId(String id) {
        this.activeModelId = id;
    }

    public void removeModel(String id) {
        File dir = context.getExternalFilesDir("models");
        if (dir != null) {
            File f = new File(dir, id + ".gguf");
            if (f.exists()) {
                f.delete();
            }
        }
        if (activeModelId.equals(id)) {
            activeModelId = "rule_based";
        }
    }
}
