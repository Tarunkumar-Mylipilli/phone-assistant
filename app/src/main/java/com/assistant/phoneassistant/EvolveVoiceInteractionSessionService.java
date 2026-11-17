package com.assistant.phoneassistant;

import android.content.Context;
import android.content.Intent;
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
        public void onHandleAssist(Bundle data, android.app.assist.AssistStructure structure, android.app.assist.AssistContent content) {
            super.onHandleAssist(data, structure, content);
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.putExtra("ACTION_VOICE_ASSIST", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
            hide();
        }
    }
}
