package com.assistant.phoneassistant;

import android.content.Context;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.service.voice.VoiceInteractionSessionService;

public class EvolveVoiceInteractionSessionService extends VoiceInteractionSessionService {

    @Override
    public VoiceInteractionSession onNewSession(Bundle args) {
        return new EvolveVoiceSession(this);
    }

    public static class EvolveVoiceSession extends VoiceInteractionSession {
        public EvolveVoiceSession(Context context) {
            super(context);
        }

        @Override
        public void onHandleAssist(Bundle data, AssistStructure structure, AssistContent content) {
            super.onHandleAssist(data, structure, content);
            // Launch main voice assistant dialog when system assistant gesture triggered
            android.content.Intent intent = new android.content.Intent(getContext(), MainActivity.class);
            intent.putExtra("ACTION_VOICE_ASSIST", true);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
            hide();
        }
    }
}
