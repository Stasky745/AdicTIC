package com.example.adictic.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.adictic.Constants;
import com.example.adictic.MyNotificationManager;
import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.Funcions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//class extending FirebaseMessagingService
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    String TAG = "Firebase: ";
    TodoApi mTodoService;

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

        List<String> blockList = new ArrayList<>(map.keySet());
        TodoApp.setBlockedApps(blockList);
    }
    
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        mTodoService = ((TodoApp)getApplicationContext()).getAPI();

        Map<String,String> messageMap = remoteMessage.getData();

        String title = null;
        String body = null;

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
                if(messageMap.get("blockDevice").equals("1")){
                    DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    TodoApp.setBlockedDevice(true);
                    mDPM.lockNow();
                }
                else TodoApp.setBlockedDevice(false);
            }
            else if(messageMap.containsKey("freeUse")){
                if(messageMap.get("freeUse").equals("1")){
                    Funcions.startFreeUseLimitList(getApplicationContext());
                    TodoApp.setStartFreeUse(Calendar.getInstance().getTimeInMillis());

                    title = getString(R.string.free_use_activation);
                }
                else{
                    Funcions.updateLimitedAppsList();
                    TodoApp.setStartFreeUse(0);
                    title = getString(R.string.free_use_deactivation);
                }
            }
            else if(messageMap.containsKey("limitApp")){
                updateLimitAppsList(messageMap);

                /** FER CRIDA WORKMANAGER **/
                Funcions.runLimitAppsWorker(getApplicationContext(), 0);

                title = getString(R.string.update_blocked_apps);
            }
            else if(messageMap.containsKey("blockApp")){
                updateBlockedAppsList(messageMap);
                System.out.println("Blocked: "+TodoApp.getBlockedApps());

                title = getString(R.string.update_blocked_apps);
            }
            else if(messageMap.containsKey("liveApp")){
                String s = messageMap.get("liveApp");
                if(s.equals("-1")) TodoApp.setLiveApp(false);
                TodoApp.setLiveApp(Boolean.parseBoolean(messageMap.get("bool")));
                Log.d(TAG, "Token liveApp: "+s);
            }
            else if(messageMap.containsKey("getIcon")){
                messageMap.remove("getIcon");
                List<String> list = new ArrayList<>(messageMap.keySet());
                sendIcon(list);
            }
            else if(messageMap.containsKey("horaris")){
                Funcions.runLimitAppsWorker(getApplicationContext(),0);

                title = getString(R.string.horaris_notification);
            }

                    /** Accions del dispositiu pare **/
            else if(messageMap.containsKey("currentAppUpdate")){
                String aux = messageMap.get("currentAppUpdate");

                Intent intent = new Intent("liveApp");
                    intent.putExtra("appName",aux);
                    intent.putExtra("time",messageMap.get("Time"));
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                Log.d(TAG, "Current AppUpdate: "+aux+" |Time: "+messageMap.get("time"));
            }
            //MyNotificationManager.getInstance(this).displayNotification(title, body);

        }

        // Check if message contains a notification payload.
        if (title != null || body != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

            if(body == null) body = "";
            MyNotificationManager.getInstance(this).displayNotification(title, body);
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private void sendIcon(List<String> list){
        for(String s : list){
            try {
                PackageManager mPm = getApplicationContext().getPackageManager();
                Drawable icon = mPm.getApplicationIcon(s);

                Bitmap bitmap = ((BitmapDrawable)icon).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
                byte[] byteArray = stream.toByteArray();

                RequestBody requestFile =
                        RequestBody.create(
                                MediaType.parse("image/png"),
                                byteArray
                        );

                // MultipartBody.Part is used to send also the actual file name
                MultipartBody.Part body =
                        MultipartBody.Part.createFormData(s, s, requestFile);

                Call<String> call = mTodoService.postIcon(s,body);

                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) { }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) { }
                });
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
