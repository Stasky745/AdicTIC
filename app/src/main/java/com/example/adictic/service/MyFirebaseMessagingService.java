package com.example.adictic.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.example.adictic.Constants;
import com.example.adictic.MyNotificationManager;
import com.example.adictic.TodoApp;
import com.example.adictic.util.Funcions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Calendar;
import java.util.Map;

//class extending FirebaseMessagingService
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    String TAG = "Firebase: ";

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
    }

    public void updateLimitAppsList(Map<String,String> map){
        map.remove("limitApp");
        for(Map.Entry<String,String> entry : map.entrySet()){
            TodoApp.getLimitApps().put(entry.getKey(), Long.parseLong(entry.getValue()));
        }
    }

    private void updateBlockedAppsList(Map<String,String> map){
        map.remove("blockApp");
        for(Map.Entry<String,String> entry : map.entrySet()){
            TodoApp.getBlockedApps().add(entry.getKey());
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        Map<String,String> messageMap = remoteMessage.getData();

        // Check if message contains a data payload.
        if (messageMap.size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

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

                         /** Accions del dispositiu fill**/
            if(messageMap.containsKey("blockDevice")){
                if(messageMap.get("blockDevice") == "1"){
                    DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    TodoApp.setBlockedDevice(true);
                    mDPM.lockNow();
                }
                else TodoApp.setBlockedDevice(false);
            }
            else if(messageMap.containsKey("freeUse")){
                if(messageMap.get("freeUse") == "1"){
                    Funcions.startFreeUseLimitList(getApplicationContext());
                    TodoApp.setStartFreeUse(Calendar.getInstance().getTimeInMillis());
                }
                else{
                    TodoApp.setTutorToken(null);
                    Funcions.updateLimitedAppsList();
                    TodoApp.setStartFreeUse(0);
                }
            }
            else if(messageMap.containsKey("limitApp")){
                updateLimitAppsList(messageMap);

                /** FER CRIDA WORKMANAGER **/
                Funcions.runLimitAppsWorker(getApplicationContext(), 0);
            }
            else if(messageMap.containsKey("blockApp")){
                updateBlockedAppsList(messageMap);
            }
            else if(messageMap.containsKey("liveApp")){
                String s = messageMap.get("liveApp");
                if(s.equals("-1")) TodoApp.setTutorToken(null);
                else TodoApp.setTutorToken(s);
            }

                    /** Accions del dispositiu pare **/
            else if(messageMap.containsKey("currentAppUpdate")){
                String aux = messageMap.get("currentAppUpdate");

                TodoApp.setCurrentAppKid(aux);
                TodoApp.setTimeOpenedCurrentAppKid(messageMap.get("time"));

            }
            //MyNotificationManager.getInstance(this).displayNotification(title, body);

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            MyNotificationManager.getInstance(this).displayNotification(title, body);
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
}
