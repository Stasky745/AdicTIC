package com.example.adictic.service;

import android.app.KeyguardManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.adictic.common.ui.BlockAppsActivity;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Crypt;
import com.developerspace.webrtcsample.RTCActivity;
import com.example.adictic.R;
import com.example.adictic.entity.BlockedApp;
import com.example.adictic.rest.AdicticApi;
import com.example.adictic.ui.chat.ChatFragment;
import com.example.adictic.util.AdicticApp;
import com.example.adictic.util.Funcions;
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

import static com.adictic.common.util.Constants.CHANNEL_ID;

//class extending FirebaseMessagingService
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final String TAG = "Firebase: ";
    private AdicticApi mTodoService;

    private long updateGeoloc = -1;

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());
        assert sharedPreferences != null;

        if (Objects.equals(Crypt.getAES(token), sharedPreferences.getString(Constants.SHARED_PREFS_TOKEN, "")))
            return;

        long idUser = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1);
        if(idUser!=-1) {
            if (sharedPreferences.getBoolean("isTutor", false))
                idUser = -1;
            else
                idUser = sharedPreferences.getLong("idUser", -1);

            Funcions.runUpdateTokenWorker(getApplicationContext(), idUser, token, 0);
        }
    }

    public void updateBlockedAppsList(Map<String, String> map) {
        List<BlockedApp> list = new ArrayList<>();
        map.remove("blockApp");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            BlockedApp blockedApp = new BlockedApp();
            blockedApp.pkgName = entry.getKey();
            blockedApp.timeLimit = Long.parseLong(entry.getValue());
            list.add(blockedApp);
        }

        Funcions.write2File(getApplicationContext(), Constants.FILE_BLOCKED_APPS,list);

        Funcions.startRestartBlockedAppsWorker24h(getApplicationContext());
        Funcions.runRestartBlockedAppsWorkerOnce(getApplicationContext(),0);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        mTodoService = ((AdicticApp) getApplicationContext()).getAPI();
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());
        assert sharedPreferences != null;

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

                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, Constants.CHANNEL_NAME, importance);
                mChannel.setDescription(Constants.CHANNEL_DESCRIPTION);
                mChannel.enableLights(true);
                mChannel.setLightColor(Color.RED);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                mNotificationManager.createNotificationChannel(mChannel);
            }

            if(!messageMap.containsKey("action")){
                Log.e(TAG,"La consulta de firebase no té la clau 'action'");
                return;
            }
            String action = messageMap.get("action");
            if(action==null){
                Log.e(TAG,"La clau 'action' de firebase és null");
                return;
            }
            switch(action){
                // ************* Accions del dispositiu fill *************
                case "blockDevice":
                    if (Objects.equals(messageMap.get("blockDevice"), "1")) {
                        DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                        sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_BLOCKEDDEVICE,true).apply();
                        mDPM.lockNow();
                    }
                    else sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_BLOCKEDDEVICE,false).apply();
                    break;
                case "freeUse":
                    if (Objects.equals(messageMap.get("freeUse"), "1")) {
                        sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_FREEUSE, true).apply();

                        title = getString(R.string.free_use_activation);
                    } else {
                        sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_FREEUSE, false).apply();

                        Funcions.endFreeUse(getApplicationContext());

                        title = getString(R.string.free_use_deactivation);
                    }
                    break;
                case "blockApp":
                    updateBlockedAppsList(messageMap);

                    title = getString(R.string.update_blocked_apps);
                    activitatIntent = BlockAppsActivity.class;
                    break;
                case "liveApp":
                    String s = messageMap.get("liveApp");
                    boolean active = Boolean.parseBoolean(messageMap.get("bool"));
                    sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_LIVEAPP,active).apply();

                    if(active && (!sharedPreferences.contains(Constants.SHARED_PREFS_APPUSAGEWORKERUPDATE) ||
                            Calendar.getInstance().getTimeInMillis() - sharedPreferences.getLong(Constants.SHARED_PREFS_LASTUPDATEAPPUSAGEWORKER,Constants.HOUR_IN_MILLIS+1) > Constants.HOUR_IN_MILLIS)) {

                        //Si el dispositiu no està bloquejat enviem el nou liveapp
                        KeyguardManager myKM = (KeyguardManager) getApplicationContext().getSystemService(KEYGUARD_SERVICE);
                        if(!myKM.isDeviceLocked())
                            WindowChangeDetectingService.instance.enviarLiveApp();

                        Funcions.runUniqueAppUsageWorker(getApplicationContext());

                        sharedPreferences.edit().putLong(Constants.SHARED_PREFS_LASTUPDATEAPPUSAGEWORKER, Calendar.getInstance().getTimeInMillis()).apply();
                    }

                    long now = Calendar.getInstance().getTimeInMillis();
                    long minute = 1000*60;
                    if(updateGeoloc == -1 || now - updateGeoloc > minute) {
                        Funcions.runGeoLocWorkerOnce(getApplicationContext());
                        updateGeoloc = now;
                    }

                    Log.d(TAG, "Token liveApp: " + s);
                    break;
                case "getIcon":
                    messageMap.remove("getIcon");
                    List<String> list = new ArrayList<>(messageMap.keySet());
                    sendIcon(list);
                    break;
                case "horaris":
                    Funcions.checkHoraris(getApplicationContext());
                    title = getString(R.string.horaris_notification);
                    break;
                case "events":
                    Funcions.checkEvents(getApplicationContext());
                    break;
                // ************* Accions del dispositiu tutor *************
                case "currentAppUpdate":
                    String aux = messageMap.get("currentAppUpdate");

                    Intent intentCurrApp = new Intent("liveApp");
                    intentCurrApp.putExtra("appName", messageMap.get("appName"));
                    intentCurrApp.putExtra("pkgName", aux);
                    intentCurrApp.putExtra("time", messageMap.get("time"));
                    intentCurrApp.putExtra("idChild", messageMap.get("idChild"));
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intentCurrApp);

                    Log.d(TAG, "Current AppUpdate: " + aux + " |Time: " + messageMap.get("time"));
                    break;
                case "installedApp":
                    String appNameInsApp = messageMap.get("installedApp");
                    String childNameInsApp = messageMap.get("childName");
                    title = getString(R.string.title_installed_app, childNameInsApp);
                    body = appNameInsApp;
                    activitatIntent = BlockAppsActivity.class;
                    break;
                case "uninstalledApp":
                    String appNameUninsApp = messageMap.get("uninstalledApp");
                    String childNameUninsApp = messageMap.get("childName");
                    title = getString(R.string.title_uninstalled_app, childNameUninsApp);
                    body = appNameUninsApp;
                    activitatIntent = BlockAppsActivity.class;
                    break;
                case "geolocFills":
                    Intent intentGeoFill = new Intent("actualitzarLoc");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intentGeoFill);

                    Log.d(TAG,"Actualitzar fills");
                    break;
                case "chat":
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

                                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, Constants.CHANNEL_NAME, importance);
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
                            body = remoteMessage.getData().get("body");
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

                                    NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, Constants.CHANNEL_NAME, importance);
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
                            Long userId = Long.parseLong(Objects.requireNonNull(remoteMessage.getData().get("userID")));
                            if (ChatFragment.adminUserId.equals(userId)) {
                                Intent intent = new Intent("CloseChat");
                                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                            }
                            break;
                    }
                    break;
                case "callVideochat":
                    String meetingId = messageMap.get("chatId");
                    if (meetingId==null || meetingId.trim().isEmpty())
                        Log.e(TAG,"Error en el meetingId");
                    else {
                        Intent callIntent = new Intent(this, RTCActivity.class);
                        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        callIntent.putExtra("meetingID",meetingId);
                        callIntent.putExtra("isJoin",true);
                        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, callIntent, 0);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                                .setSmallIcon(R.drawable.adictic_nolletra)
                                .setContentTitle("Trucant")
                                .setContentText("Test test")
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true);
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                        //notificationManager.cancelAll();
                        notificationManager.notify(251, builder.build());
                    }
                    break;
                default:
                    Log.e(TAG,"Clau 'action' no reconeguda: "+action);
                    break;
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS,100,stream);
                else{
                    bitmap.compress(Bitmap.CompressFormat.WEBP,100,stream);
                }

                byte[] byteArray = stream.toByteArray();

                RequestBody requestFile =
                        RequestBody.create(
                                byteArray,
                                MediaType.parse("image/webp")
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
