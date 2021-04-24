package com.example.adictic.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.adictic.R;
import com.example.adictic.entity.Horaris;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.entity.BlockedApp;
import com.example.adictic.ui.BlockAppsActivity;
import com.example.adictic.ui.chat.ChatFragment;
import com.example.adictic.ui.inici.Login;
import com.example.adictic.util.Constants;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.TodoApp;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
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

    private final String TAG = "Firebase: ";
    private TodoApi mTodoService;
    private SharedPreferences sharedPreferences;

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
    }

    public void updateBlockedAppsList(Map<String, String> map) {
        List<BlockedApp> list = new ArrayList<>();
        map.remove("limitApp");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            BlockedApp blockedApp = new BlockedApp();
            blockedApp.pkgName = entry.getKey();
            blockedApp.timeLimit = Long.parseLong(entry.getValue());
            blockedApp.blockedNow = false;
            list.add(blockedApp);
        }

        Funcions.write2File(getApplicationContext(),list);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        mTodoService = ((TodoApp) getApplicationContext()).getAPI();
        sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());

        Map<String, String> messageMap = remoteMessage.getData();

        String title = "";
        String body = "";
        Class activitatIntent = null;

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
            if (messageMap.containsKey("blockDevice")) {
                if (Objects.equals(messageMap.get("blockDevice"), "1")) {
                    DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    sharedPreferences.edit().putBoolean("blockedDevice",true).apply();
                    mDPM.lockNow();
                } else sharedPreferences.edit().putBoolean("blockedDevice",false).apply();
            } else if (messageMap.containsKey("freeUse")) {
                if (Objects.equals(messageMap.get("freeUse"), "1")) {
                    Funcions.startFreeUseLimitList(getApplicationContext());

                    title = getString(R.string.free_use_activation);
                } else {
                    Funcions.updateLimitedAppsList(getApplicationContext());
                    title = getString(R.string.free_use_deactivation);
                }
            } else if (messageMap.containsKey("blockApp")) {
                updateBlockedAppsList(messageMap);

                title = getString(R.string.update_blocked_apps);
                activitatIntent = BlockAppsActivity.class;
            } else if (messageMap.containsKey("liveApp")) {
                String s = messageMap.get("liveApp");
                boolean active = Boolean.parseBoolean(messageMap.get("bool"));
                sharedPreferences.edit().putBoolean("liveApp",active).apply();

                if(active && (!sharedPreferences.contains("appUsageWorkerUpdate") ||
                        Calendar.getInstance().getTimeInMillis() - sharedPreferences.getLong("lastUpdateAppUsageWorker",-1) > Constants.HOUR_IN_MILLIS))
                {
                    OneTimeWorkRequest myWork =
                            new OneTimeWorkRequest.Builder(AppUsageWorker.class).build();
                    WorkManager.getInstance(this).enqueue(myWork);

                    sharedPreferences.edit().putLong("lastUpdateAppUsageWorker", Calendar.getInstance().getTimeInMillis()).apply();
                }

                Log.d(TAG, "Token liveApp: " + s);
            } else if (messageMap.containsKey("getIcon")) {
                messageMap.remove("getIcon");
                List<String> list = new ArrayList<>(messageMap.keySet());
                sendIcon(list);
            } else if (messageMap.containsKey("horaris")) {
                Call<Horaris> call = mTodoService.getHoraris(sharedPreferences.getLong("idUser",-1));
                call.enqueue(new Callback<Horaris>() {
                    @Override
                    public void onResponse(@NonNull Call<Horaris> call, @NonNull Response<Horaris> response) {
                        if (response.isSuccessful()) {
                            assert response.body() != null;
                            Funcions.updateEventList(getApplicationContext(), response.body().events);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Horaris> call, @NonNull Throwable t) {

                    }
                });
                Funcions.runLimitAppsWorker(getApplicationContext(), 0);
                title = getString(R.string.horaris_notification);
            } else if (messageMap.containsKey("geolocActive")) {
                Funcions.runGeoLocWorker(getApplicationContext());
            }

            /**
             * Accions del dispositiu pare
             * **/
            else if (messageMap.containsKey("currentAppUpdate")) {
                String aux = messageMap.get("currentAppUpdate");

                Intent intent = new Intent("liveApp");
                intent.putExtra("appName", messageMap.get("appName"));
                intent.putExtra("pkgName", aux);
                intent.putExtra("time", messageMap.get("Time"));
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                Log.d(TAG, "Current AppUpdate: " + aux + " |Time: " + messageMap.get("time"));
            }
            else if(messageMap.containsKey("installedApp")){
                String appName = messageMap.get("installedApp");
                String childName = messageMap.get("childName");
                title = getString(R.string.title_installed_app,childName);
                body = appName;
                activitatIntent = BlockAppsActivity.class;
            }
            else if(messageMap.containsKey("uninstalledApp")){
                String appName = messageMap.get("uninstalledApp");
                String childName = messageMap.get("childName");
                title = getString(R.string.title_uninstalled_app,childName);
                body = appName;
                activitatIntent = BlockAppsActivity.class;
            }

            //MyNotificationManager.getInstance(this).displayNotification(title, body);
            else if (messageMap.containsKey("chat")) {
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

                        MyNotificationManager.getInstance(this).displayNotification(title, body, null);
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

            MyNotificationManager.getInstance(this).displayNotification(title, body, activitatIntent);
        }
    }

    @NonNull
    private Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }

    private void sendIcon(List<String> list) {
        for (String s : list) {
            try {
                PackageManager mPm = getApplicationContext().getPackageManager();
                Drawable icon = mPm.getApplicationIcon(s);

                Bitmap bitmap = getBitmapFromDrawable(icon);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                RequestBody requestFile =
                        RequestBody.create(
                                MediaType.parse("image/png"),
                                byteArray
                        );

                // MultipartBody.Part is used to send also the actual file name
                MultipartBody.Part body =
                        MultipartBody.Part.createFormData("file", s, requestFile);

                Call<String> call = mTodoService.postIcon(s, body);

                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    }
                });
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
