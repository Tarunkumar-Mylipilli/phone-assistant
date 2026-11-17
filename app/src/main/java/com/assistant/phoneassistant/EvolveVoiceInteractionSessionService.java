package com.assistant.phoneassistant;

import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.service.voice.VoiceInteractionSessionService;

public class EvolveVoiceInteractionSessionService extends VoiceInteractionSessionService {

    @Override
    public VoiceInteractionSession onNewSession(Bundle args) {
        return new EvolveVoiceSession(this);
    }

    public static class EvolveVoiceSession extends VoiceInteractionSession {
        public EvolveVoiceSession(android.content.Context context) {
            super(context);
        }

        @Override
        public void onShow(Bundle args, int showFlags) {
            super.onShow(args, showFlags);
            android.content.Intent intent = new android.content.Intent(getContext(), MainActivity.class);
            intent.putExtra("ACTION_VOICE_ASSIST", true);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
            hide();
        }
    }
}
