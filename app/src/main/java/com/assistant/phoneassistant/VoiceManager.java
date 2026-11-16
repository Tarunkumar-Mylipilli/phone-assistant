package com.assistant.phoneassistant;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

public class VoiceManager implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private boolean isReady = false;

    public VoiceManager(Context context) {
        tts = new TextToSpeech(context.getApplicationContext(), this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.setPitch(1.0f);
                tts.setSpeechRate(1.05f); // Crisp Jarvis-style pacing
                isReady = true;
            }
        }
    }

    public void speak(String text) {
        if (isReady && tts != null && text != null) {
            // Strip markdown asterisks and bullets for clean speech
            String cleanText = text.replaceAll("[*•#_`]", "").trim();
            tts.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "evolve_voice_utterance");
        }
    }

    public void stop() {
        if (tts != null) {
            tts.stop();
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
