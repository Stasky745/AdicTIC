package com.example.adictic_admin.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.adictic.common.util.Constants;
import com.example.adictic_admin.ui.Xats.XatActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Objects;

//class extending FirebaseMessagingService
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final String TAG = "Firebase: ";

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());
        assert sharedPreferences != null;
        long idUser = -1;

        Funcions.runUpdateTokenWorker(getApplicationContext(), idUser, token, 0);

    }
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        Map<String, String> messageMap = remoteMessage.getData();

        // Check if message contains a data payload.
        if (messageMap.size() > 0) {
            // body , title , userID , childID , myID
            if (messageMap.containsKey("chat")) {
                long userID = Long.parseLong(Objects.requireNonNull(remoteMessage.getData().get("userID")));
                long childID = Long.parseLong(Objects.requireNonNull(remoteMessage.getData().get("childID")));
                Intent intent;

                switch (Objects.requireNonNull(remoteMessage.getData().get("chat"))) {  //Message with Chat
                    case "0": // Access
                        if (XatActivity.userProfile.userId == userID && XatActivity.userProfile.childId == childID) {
                            intent = new Intent("chatAccess");
                            intent.putExtra("idUser", userID);
                            intent.putExtra("idChild", childID);
                            intent.putExtra("hasAccess", Boolean.parseBoolean(messageMap.get("hasAccess")));

                            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                        }

                        break;
                    case "1":  //Message with Chat
                        String body = remoteMessage.getData().get("body");
                        if (XatActivity.userProfile.userId == userID && XatActivity.userProfile.childId == childID) {
                            intent = new Intent("NewMessage");
                            intent.putExtra("message", body);
                            intent.putExtra("senderId", userID);
                            intent.putExtra("childId", childID);
                            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                        } else {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                NotificationManager mNotificationManager =
                                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                int importance = NotificationManager.IMPORTANCE_HIGH;

                                NotificationChannel mChannel = new NotificationChannel(Constants.CHANNEL_ID, Constants.CHANNEL_NAME, importance);
                                mChannel.setDescription(Constants.CHANNEL_DESCRIPTION);
                                mChannel.enableLights(true);
                                mChannel.setLightColor(Color.RED);
                                mChannel.enableVibration(true);
                                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                                mNotificationManager.createNotificationChannel(mChannel);
                            }

                            String title = "Tens un nou missatge!";
                            Long myId = Long.parseLong(Objects.requireNonNull(remoteMessage.getData().get("myID")));

                            MyNotificationManager.getInstance(this).displayNotificationChat(title, body, userID, myId);
                        }
                        break;
                    case "2":
                        if (XatActivity.userProfile.userId == userID && XatActivity.userProfile.childId == childID) {
                            intent = new Intent("CloseChat");
                            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                        }
                        break;
                }
            }
        }
    }
}
