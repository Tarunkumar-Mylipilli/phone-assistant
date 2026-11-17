package com.assistant.phoneassistant;

import android.content.Intent;
import android.speech.RecognitionService;

public class EvolveRecognitionService extends RecognitionService {

    @Override
    protected void onStartListening(Intent recognizerIntent, Callback listener) {
    }

    @Override
    protected void onStopListening(Callback listener) {
    }

    @Override
    protected void onCancel(Callback listener) {
    }
}
