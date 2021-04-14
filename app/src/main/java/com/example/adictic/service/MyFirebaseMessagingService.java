package com.example.adictic.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.adictic.Constants;
import com.example.adictic.MyNotificationManager;
import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.entity.Horaris;
import com.example.adictic.activity.chat.ChatFragment;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.Funcions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    public void onNewToken(@NonNull String token) {
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
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        mTodoService = ((TodoApp)getApplicationContext()).getAPI();

        Map<String,String> messageMap = remoteMessage.getData();

        String title = "";
        String body = "";

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
                if(Objects.equals(messageMap.get("blockDevice"), "1")){
                    DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    TodoApp.setBlockedDevice(true);
                    mDPM.lockNow();
                }
                else TodoApp.setBlockedDevice(false);
            }
            else if(messageMap.containsKey("freeUse")){
                if(Objects.equals(messageMap.get("freeUse"), "1")){
                    Funcions.startFreeUseLimitList(getApplicationContext());

                    title = getString(R.string.free_use_activation);
                }
                else{
                    Funcions.updateLimitedAppsList();
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
                TodoApp.setLiveApp(Boolean.parseBoolean(messageMap.get("bool")));

                OneTimeWorkRequest myWork =
                        new OneTimeWorkRequest.Builder(AppUsageWorker.class).build();
                WorkManager.getInstance(this).enqueue(myWork);

                Log.d(TAG, "Token liveApp: "+s);
            }
            else if(messageMap.containsKey("getIcon")){
                messageMap.remove("getIcon");
                List<String> list = new ArrayList<>(messageMap.keySet());
                sendIcon(list);
            }
            else if(messageMap.containsKey("horaris")){
                Call<Horaris> call = mTodoService.getHoraris(TodoApp.getIDChild());
                call.enqueue(new Callback<Horaris>() {
                    @Override
                    public void onResponse(@NonNull Call<Horaris> call, @NonNull Response<Horaris> response) {
                        if(response.isSuccessful()){
                            assert response.body() != null;
                            Funcions.updateEventList(getApplicationContext(),response.body().events);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Horaris> call, @NonNull Throwable t) {

                    }
                });
                Funcions.runLimitAppsWorker(getApplicationContext(),0);
                title = getString(R.string.horaris_notification);
            }
            else if(messageMap.containsKey("geolocActive")){
                Funcions.runGeoLocWorker(getApplicationContext());
            }

            /**
             * Accions del dispositiu pare
             * **/
            else if(messageMap.containsKey("currentAppUpdate")){
                String aux = messageMap.get("currentAppUpdate");

                Intent intent = new Intent("liveApp");
                    intent.putExtra("appName",messageMap.get("appName"));
                    intent.putExtra("pkgName",aux);
                    intent.putExtra("time",messageMap.get("Time"));
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                Log.d(TAG, "Current AppUpdate: "+aux+" |Time: "+messageMap.get("time"));
            }
            //MyNotificationManager.getInstance(this).displayNotification(title, body);
            else if(messageMap.containsKey("chat")){
                switch (Objects.requireNonNull(remoteMessage.getData().get("chat"))) {
                    case "0":
                        //if the message contains data payload
                        //It is a map of custom keyvalues
                        //we can read it easily

                        //then here we can use the title and body to build a notification
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

                        MyNotificationManager.getInstance(this).displayNotification(title, body);
                        break;
                    case "1":  //Message with Chat
                        Long myId = Long.parseLong(Objects.requireNonNull(remoteMessage.getData().get("myID")));
                        Long userID = Long.parseLong(Objects.requireNonNull(remoteMessage.getData().get("userID")));
                        if (ChatFragment.adminUserId.equals(userID)) {
                            Intent intent = new Intent("NewMessage");
                            intent.putExtra("message", body);
                            intent.putExtra("senderId", userID);
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

                            MyNotificationManager.getInstance(this).displayNotificationChat(title, body, userID, myId);
                        }
                        break;
                    case "2":
                        Long userId = Long.parseLong(Objects.requireNonNull(remoteMessage.getData().get("userId")));
                        if (ChatFragment.adminUserId.equals(userId)) {
                            Intent intent = new Intent("CloseChat");
                            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                        }
                        break;
                }
            }

        }

        // Check if message contains a notification payload.
        if (!title.equals("")) {
            Log.d(TAG, "Message Notification Body: " + body);

            MyNotificationManager.getInstance(this).displayNotification(title, body);
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    @NonNull
    private Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }

    private void sendIcon(List<String> list){
        for(String s : list){
            try {
                PackageManager mPm = getApplicationContext().getPackageManager();
                Drawable icon = mPm.getApplicationIcon(s);

                Bitmap bitmap = getBitmapFromDrawable(icon);
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
                        MultipartBody.Part.createFormData("file", s, requestFile);

                Call<String> call = mTodoService.postIcon(s,body);

                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) { }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) { }
                });
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
