package com.adictic.jitsi.utilities;

import android.util.Log;

import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;

public class JitsiFuncions {

    private static final String TAG = "JitsiFuncions";

    public static JitsiMeetConferenceOptions.Builder createJitsiMeetConferenceOptionsBuilder(String meetingRoom) throws MalformedURLException {
        Log.d(TAG, "Creant sala de Jitsi amb id: "+meetingRoom);

        URL serverURL = new URL("https://meet.jit.si");

        JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
        builder.setServerURL(serverURL);
        builder.setWelcomePageEnabled(false);
        builder.setRoom(meetingRoom);
        builder.setFeatureFlag("chat.enabled", false);
        builder.setFeatureFlag("meeting-name.enabled", false);
        builder.setFeatureFlag("raise-hand.enabled", false);
        builder.setFeatureFlag("video-share.enabled", false);
        builder.setFeatureFlag("call-integration.enabled", false);
        builder.setFeatureFlag("calendar.enabled", false);
        builder.setFeatureFlag("live-streaming.enabled", false);
        builder.setFeatureFlag("tile-view.enabled", false);
        builder.setFeatureFlag("security-options.enabled", false);

        return builder;
    }


}
