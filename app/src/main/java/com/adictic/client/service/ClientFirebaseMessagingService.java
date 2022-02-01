package com.adictic.client.service;

import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.WorkManager;

import com.adictic.client.rest.AdicticApi;
import com.adictic.client.ui.chat.ChatFragment;
import com.adictic.client.util.AdicticApp;
import com.adictic.client.util.Funcions;
import com.adictic.client.util.hilt.AdicticRepository;
import com.adictic.common.R;
import com.adictic.common.entity.BlockedApp;
import com.adictic.common.entity.NotificationInformation;
import com.adictic.common.entity.TimeBlock;
import com.adictic.common.entity.TimeFreeUse;
import com.adictic.common.ui.BlockAppsActivity;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.common.util.MyNotificationManager;
import com.adictic.jitsi.activities.IncomingInvitationActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

@AndroidEntryPoint
public class ClientFirebaseMessagingService extends FirebaseMessagingService {

    @Inject
    AdicticRepository repository;

    private final String TAG = "Firebase: ";
    private AdicticApi mTodoService;

    private long updateGeoloc = -1;

    private int retryCount = 0;
    private static final int TOTAL_RETRIES = 10;

    private CompositeDisposable disposable;

    @Override
    public void onCreate() {
        super.onCreate();
        disposable = new CompositeDisposable();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.dispose();
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        repository.runUpdateTokenWorker();
    }

    public void updateBlockedAppsList(Map<String, String> map) {
        if(!repository.accessibilityServiceOn())
            return;

        List<BlockedApp> blockedApps = new ArrayList<>();
        for(Map.Entry<String, String> entry : map.entrySet()) {
            try {
                String pkgName = entry.getKey();
                int totalTime = Integer.parseInt(entry.getValue());

                BlockedApp blockedApp = new BlockedApp();
                blockedApp.pkgName = pkgName;
                blockedApp.timeLimit = totalTime;
                blockedApps.add(blockedApp);
            }
            catch (NumberFormatException e){
                e.printStackTrace();
            }
        }

        Disposable dispUpdateApps = repository.updateApps(blockedApps)
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableCompletableObserver() {
            @Override
            public void onComplete() {
                AccessibilityScreenService.instance.isCurrentAppBlocked();
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                e.printStackTrace();
                AccessibilityScreenService.instance.isCurrentAppBlocked();
            }
        });

        disposable.add(dispUpdateApps);

        // Actualitzem la bdd de l'ús del dia actual a room i aprofitem per enviar al servidor si fa molt que no es fa
        repository.sendAppUsage();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        ClientNotificationManager clientNotificationManager = repository.getNotificationManager();
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        mTodoService = repository.getApi();
        SharedPreferences sharedPreferences = repository.getEncryptedSharedPreferences();
        assert sharedPreferences != null;

        Map<String, String> messageMap = remoteMessage.getData();

        String title = "";
        String body = "";
        Class<?> activitatClass = null;
        Intent activitatIntent = null;
        MyNotificationManager.Channels channel = MyNotificationManager.Channels.GENERAL;
        int notifID = -1;
        boolean showNotif = true;

        // Check if message contains a data payload.
        if (messageMap.size() > 0) {
            Log.d(TAG, "Message data payload: " + messageMap);

            if(!messageMap.containsKey("action")){
                Log.e(TAG,"La consulta de firebase no té la clau 'action'");
                return;
            }
            String action = messageMap.get("action");
            if(action==null){
                Log.e(TAG,"La clau 'action' de firebase és null");
                return;
            }

            sendFirebaseLog(messageMap.get("logID"));

            NotificationInformation notificationInformation = new NotificationInformation();
            notificationInformation.dateMillis = System.currentTimeMillis();
            notificationInformation.read = false;
            notificationInformation.important = false;
            notificationInformation.notifCode = messageMap.get("notifCode");

            switch(action){
                // ************* Accions del dispositiu fill *************
                case "dailyLimit":
                    if(sharedPreferences.getBoolean(Constants.NOTIF_SETTINGS_DAILY_LIMIT, true)) {
                        notifID = MyNotificationManager.NOTIF_ID_DAILY_LIMIT;
                        int dailyLimit = Integer.parseInt(Objects.requireNonNull(messageMap.get("dailyLimit")));
                        sharedPreferences.edit().putInt(Constants.SHARED_PREFS_DAILY_USAGE_LIMIT, dailyLimit).apply();
                        if (repository.accessibilityServiceOn())
                            AccessibilityScreenService.instance.setLimitDevice(dailyLimit);

                        title = getString(R.string.new_daily_limit_device);
                    }
                    else
                        showNotif = false;
                    break;
                case "blockDevice":
                    if (Objects.equals(messageMap.get("blockDevice"), "1")) {
                        sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_BLOCKEDDEVICE,true).apply();
                        sharedPreferences.edit().putLong(Constants.SHARED_PREFS_BLOCKEDDEVICE_START, DateTime.now().getMillis()).apply();

                        if(repository.accessibilityServiceOn()) {
                            AccessibilityScreenService.instance.setBlockDevice(true);
                            AccessibilityScreenService.instance.updateDeviceBlock();

                            if(!(showNotif = showNotification(sharedPreferences,Constants.NOTIF_SETTINGS_BLOCK_DEVICE)))
                                break;

                            title = getString(R.string.phone_locked);
                            body = getString(R.string.notif_phone_locked);
                            channel = MyNotificationManager.Channels.BLOCK;
                            notifID = MyNotificationManager.NOTIF_ID_DEVICE_STATE;
                        }
                    }
                    else {
                        if(repository.accessibilityServiceOn()) {
                            AccessibilityScreenService.instance.setBlockDevice(false);
                            AccessibilityScreenService.instance.updateDeviceBlock();

                            if(!(showNotif = showNotification(sharedPreferences,Constants.NOTIF_SETTINGS_BLOCK_DEVICE)))
                                break;

                            title = getString(R.string.phone_unlocked);
                            body = getString(R.string.notif_phone_unlocked);
                            channel = MyNotificationManager.Channels.BLOCK;
                            notifID = MyNotificationManager.NOTIF_ID_DEVICE_STATE;
                        }

                        sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_BLOCKEDDEVICE,false).apply();
                        sendBlockDeviceTime(sharedPreferences);
                    }
                    break;
                case "freeUse":
                    if (Objects.equals(messageMap.get("freeUse"), "1")) {
                        sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_FREEUSE, true).apply();
                        sharedPreferences.edit().putLong(Constants.SHARED_PREFS_FREEUSE_START, System.currentTimeMillis()).apply();

                        if(repository.accessibilityServiceOn()) {
                            AccessibilityScreenService.instance.setFreeUse(true);
                            AccessibilityScreenService.instance.updateDeviceBlock();

                            if(!(showNotif = showNotification(sharedPreferences,Constants.NOTIF_SETTINGS_FREE_USE)))
                                break;
                        }

                        title = getString(R.string.free_use_activation);
                    } else {
                        sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_FREEUSE, false).apply();

                        if(repository.accessibilityServiceOn()) {
                            AccessibilityScreenService.instance.setFreeUse(false);
                            AccessibilityScreenService.instance.updateDeviceBlock();
                        }

                        sendFreeUseTime(sharedPreferences);

                        if(!(showNotif = showNotification(sharedPreferences,Constants.NOTIF_SETTINGS_FREE_USE)))
                            break;

                        title = getString(R.string.free_use_deactivation);
                    }
                    notifID = MyNotificationManager.NOTIF_ID_DEVICE_STATE;
                    channel = MyNotificationManager.Channels.BLOCK;
                    break;
                case "blockApp":
                    updateBlockedAppsList(messageMap);

                    if(!(showNotif = showNotification(sharedPreferences,Constants.NOTIF_SETTINGS_BLOCK_APP)))
                        break;

                    title = getString(R.string.update_blocked_apps);
                    activitatClass = BlockAppsActivity.class;
                    channel = MyNotificationManager.Channels.BLOCK;
                    notifID = MyNotificationManager.NOTIF_ID_BLOCK_APPS;
                    break;
                case "liveApp":
                    String s = messageMap.get("liveApp");
                    boolean active = Boolean.parseBoolean(messageMap.get("bool"));

                    if(repository.accessibilityServiceOn())
                        AccessibilityScreenService.instance.setLiveApp(active);

                    sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_LIVEAPP,active).apply();

                    if(active) {
                        //Si el dispositiu no està bloquejat enviem el nou liveapp
                        KeyguardManager myKM = (KeyguardManager) getApplicationContext().getSystemService(KEYGUARD_SERVICE);
                        if (!myKM.isDeviceLocked() && repository.accessibilityServiceOn())
                            AccessibilityScreenService.instance.enviarLiveApp();

                        // També actualitzem les dades d'ús al servidor
                        try {
                            if(WorkManager.getInstance(getApplicationContext()).getWorkInfosByTag(Constants.WORKER_TAG_APP_USAGE).get().isEmpty())
                                repository.startAppUsageWorker24h();
                            else
                                repository.sendAppUsage();

                        } catch (ExecutionException | InterruptedException e) {
                            repository.startAppUsageWorker24h();
                        }

                        long now = System.currentTimeMillis();
                        long timeout = 1000*60*3;
                        if(updateGeoloc == -1 || now - updateGeoloc > timeout) {
                            repository.runGeoLocWorkerOnce();
                            updateGeoloc = now;
                        }
                    }

                    Log.d(TAG, "Token liveApp: " + s);
                    break;
                case "getIcon":
                    messageMap.remove("getIcon");
                    List<String> list = new ArrayList<>(messageMap.keySet());
                    sendIcon(list);
                    break;
                case "horaris":
                    repository.checkHoraris();

                    if(!(showNotif = showNotification(sharedPreferences,Constants.NOTIF_SETTINGS_HORARIS)))
                        break;

                    title = getString(R.string.horaris_notification);
                    channel = MyNotificationManager.Channels.BLOCK;
                    notifID = MyNotificationManager.NOTIF_ID_HORARIS;
                    break;
                case "events":
                    repository.checkEvents();

                    if(!(showNotif = showNotification(sharedPreferences,Constants.NOTIF_SETTINGS_EVENTS)))
                        break;

                    title = getString(R.string.events_notification);
                    channel = MyNotificationManager.Channels.BLOCK;
                    notifID = MyNotificationManager.NOTIF_ID_EVENTS;
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
                    if(sharedPreferences.getBoolean(Constants.NOTIF_SETTINGS_INSTALL_APPS, true)) {
                        String appNameInsApp = messageMap.get("installedApp");
                        String childNameInsApp = messageMap.get("childName");
                        title = getString(R.string.title_installed_app, childNameInsApp);
                        channel = MyNotificationManager.Channels.INSTALL;
                        body = appNameInsApp;
                        activitatClass = BlockAppsActivity.class;

                        notificationInformation.title = title;
                        notificationInformation.message = body;
                        notificationInformation.childName = childNameInsApp;

                        repository.addNotificationToList(notificationInformation);
                    }
                    else
                        showNotif = false;

                    break;
                case "uninstalledApp":
                    if(sharedPreferences.getBoolean(Constants.NOTIF_SETTINGS_UNINSTALL_APPS, true)) {
                        String appNameUninsApp = messageMap.get("uninstalledApp");
                        String childNameUninsApp = messageMap.get("childName");
                        title = getString(R.string.title_uninstalled_app, childNameUninsApp);
                        channel = MyNotificationManager.Channels.INSTALL;
                        body = appNameUninsApp;
                        activitatClass = BlockAppsActivity.class;

                        notificationInformation.title = title;
                        notificationInformation.message = body;
                        notificationInformation.childName = childNameUninsApp;

                        repository.addNotificationToList(notificationInformation);
                    }
                    else
                        showNotif = false;

                    break;
                case "geolocFills":
                    Intent intentGeoFill = new Intent("actualitzarLoc");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intentGeoFill);

                    Log.d(TAG,"Actualitzar fills");
                    break;
                case "notification":
                    String notifCode = messageMap.get("notifCode");

                    if(notifCode == null)
                        break;

                    if(sharedPreferences.getBoolean(notifCode, true)) {
                        String notifChildName = messageMap.get("childName");
                        title = messageMap.get("title");
                        body = messageMap.get("message");

                        channel = MyNotificationManager.Channels.IMPORTANT;

                        notificationInformation.title = title;
                        notificationInformation.message = body;
                        notificationInformation.childName = notifChildName;
                        notificationInformation.important = Boolean.parseBoolean(messageMap.get("important"));
                        notificationInformation.dateMillis = messageMap.get("dateMillis") != null ? Long.parseLong(Objects.requireNonNull(messageMap.get("dateMillis"))) : notificationInformation.dateMillis;

                        repository.addNotificationToList(notificationInformation);
                    }
                    else
                        showNotif = false;

                    break;
                case "chat":
                    switch (Objects.requireNonNull(remoteMessage.getData().get("chat"))) {
                        case "0":
                            channel = MyNotificationManager.Channels.CHAT;
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

                                if(!(showNotif = showNotification(sharedPreferences,Constants.NOTIF_SETTINGS_CHAT)))
                                    break;

                                clientNotificationManager.displayNotificationChat(title, body, userID, myId);
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
                    String type = messageMap.get("type");
                    if (type == null)
                        Log.e(TAG, "Error en el missatge de firebase de callVideochat: No hi ha type");
                    else {
                        switch (type) {
                            case "invitation": {
                                String meetingId = messageMap.get("chatId");
                                Intent intent = new Intent(getApplicationContext(), IncomingInvitationActivity.class);
                                intent.putExtra("admin_name", messageMap.get("admin_name"));
                                intent.putExtra("admin_id", messageMap.get("admin_id"));
                                intent.putExtra(com.adictic.jitsi.utilities.Constants.REMOTE_MSG_MEETING_ROOM, meetingId);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                clientNotificationManager.displayGeneralNotification(getString(R.string.callNotifTitle), getString(R.string.callNotifDesc, messageMap.get("admin_name")), intent, MyNotificationManager.Channels.VIDEOCHAT, notifID);
                                startActivity(intent);
                                break;
                            }
                            case "invitationResponse": {
                                Intent intent = new Intent("invitationResponse");
                                intent.putExtra(
                                        "invitationResponse",
                                        messageMap.get("invitationResponse")
                                );
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                                break;
                            }
                            case "invitation_child": {
                                String meetingId = messageMap.get("chatId");
                                Intent intent = new Intent(getApplicationContext(), IncomingInvitationActivity.class);
                                intent.putExtra("child_name", messageMap.get("child_name"));
                                intent.putExtra("child_id", messageMap.get("child_id"));
                                intent.putExtra("parentCall", true);
                                intent.putExtra(com.adictic.jitsi.utilities.Constants.REMOTE_MSG_MEETING_ROOM, meetingId);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                clientNotificationManager.displayGeneralNotification(getString(R.string.callChildNotifTitle, messageMap.get("child_name")), getString(R.string.callChildNotifDesc), intent, MyNotificationManager.Channels.VIDEOCHAT, notifID);
                                startActivity(intent);
                                break;
                            }
                        }
                    }
                    break;
                case "newPassword":
                    String newPass = messageMap.get("newPass");
                    if(newPass!=null && !newPass.trim().isEmpty())
                        sharedPreferences.edit().putString(Constants.SHARED_PREFS_PASSWORD, newPass).apply();
                    break;
                default:
                    Log.e(TAG,"Clau 'action' no reconeguda: "+action);
                    break;
            }
        }

        // Check if message contains a notification payload.
        if (showNotif && title != null && !title.equals("")) {
            Log.d(TAG, "Message Notification Body: " + body);

            if(activitatClass == null)
                clientNotificationManager.displayGeneralNotification(title, body, activitatIntent, channel, notifID);
            else
                clientNotificationManager.displayGeneralNotification(title, body, activitatClass, channel, notifID);
        }
    }

    private void sendFirebaseLog(String logID) {
        try {
            mTodoService.postFirebaseLog(Long.parseLong(logID)).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    super.onResponse(call, response);
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    super.onFailure(call, t);
                }
            });
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private boolean showNotification(SharedPreferences sharedPreferences, String notifName){
        return sharedPreferences.getBoolean(notifName, true);
    }

    private void sendBlockDeviceTime(SharedPreferences sharedPreferences) {
        long idChild = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER, -1);

        TimeBlock timeBlock = new TimeBlock();
        timeBlock.start = sharedPreferences.getLong(Constants.SHARED_PREFS_BLOCKEDDEVICE_START, -1);
        if(timeBlock.start == -1 || idChild == -1)
            return;
        timeBlock.end = DateTime.now().getMillis();
        retryCount = 0;
        Call<String> call = mTodoService.postTempsBloqueig(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER, -1), timeBlock);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    super.onResponse(call, response);
                if(!response.isSuccessful() && retryCount++ < TOTAL_RETRIES)
                    call.clone().enqueue(this);
                else {
                    sharedPreferences.edit().putLong(Constants.SHARED_PREFS_BLOCKEDDEVICE_START, -1).apply();
                    retryCount = 0;
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    super.onFailure(call, t);
                if(retryCount++ < TOTAL_RETRIES)
                    call.clone().enqueue(this);
                else {
                    sharedPreferences.edit().putLong(Constants.SHARED_PREFS_BLOCKEDDEVICE_START, -1).apply();
                    retryCount = 0;
                }
            }
        });
    }

    private void sendFreeUseTime(SharedPreferences sharedPreferences) {
        long idChild = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER, -1);

        TimeFreeUse timeFreeUse = new TimeFreeUse();
        timeFreeUse.start = sharedPreferences.getLong(Constants.SHARED_PREFS_FREEUSE_START, -1);
        if(timeFreeUse.start == -1 || idChild == -1)
            return;
        timeFreeUse.end = System.currentTimeMillis();
        retryCount = 0;
        Call<String> call = mTodoService.postTempsFreeUse(idChild, timeFreeUse);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                super.onResponse(call, response);
                if(response.isSuccessful())
                    sharedPreferences.edit().putLong(Constants.SHARED_PREFS_FREEUSE_START, -1).apply();
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                super.onFailure(call, t);
            }
        });
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
            if(s.equals("action") || s.equals("logID"))
                continue;
            try {
                PackageManager mPm = getApplicationContext().getPackageManager();
                Drawable icon = mPm.getApplicationIcon(s);

                Bitmap bitmap = getBitmapFromDrawable(icon);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS,100,stream);
                else
                    bitmap.compress(Bitmap.CompressFormat.WEBP,100,stream);

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
                    super.onResponse(call, response);
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    super.onFailure(call, t);
                    }
                });
            } catch (PackageManager.NameNotFoundException e) {
//                e.printStackTrace();
            }
        }
    }
}
