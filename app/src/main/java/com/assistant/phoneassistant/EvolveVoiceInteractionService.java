package com.assistant.phoneassistant;

import android.os.Bundle;
import android.service.voice.VoiceInteractionService;

/**
 * EvolveVoiceInteractionService: Registers Evolve AI as a native Android Voice Assistant.
 * Responds to system ASSIST triggers, long-press home button, and corner swipes.
 */
public class EvolveVoiceInteractionService extends VoiceInteractionService {

    @Override
    public void onReady() {
        super.onReady();
    }

    @Override
    public int onStartCommand(android.content.Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
