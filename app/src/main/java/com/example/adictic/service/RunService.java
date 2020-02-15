package com.example.adictic.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.adictic.BlockActivity;
import com.example.adictic.MainActivity;
import com.example.adictic.R;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static android.content.ContentValues.TAG;

public class RunService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        final Handler mHandler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run () {
                mHandler.post(new Runnable() {
                    @Override
                    public void run () {
                        getForegroundTask();
                        mHandler.postDelayed(this,1000);
                    }
                });
            }
        }).start();

        startForeground();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        /*final Handler mHandler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run () {
                mHandler.post(new Runnable() {
                    @Override
                    public void run () {
                        getForegroundTask();
                        mHandler.postDelayed(this,1000);
                    }
                });
            }
        }).start();

        startForeground();*/
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private void getForegroundTask() {
        String currentApp = "NULL";
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (!mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }

        Log.e(TAG, "Current App in foreground is: " + currentApp);

        if(currentApp.equals("com.android.chrome")){
            Intent dialogIntent = new Intent(this, BlockActivity.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(dialogIntent);
        }
    }

    private void startForeground(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(this, 0, notificationIntent, 0);

            NotificationChannel nch = createNotificationChannel();
            Notification notification =
                    new Notification.Builder(this, nch.getId())
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText("Service is running background")
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentIntent(pendingIntent)
                            .setTicker("Ticker")
                            .build();

            startForeground(1, notification);
        }
    }

    private NotificationChannel createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Back";
            String description = "";
            int importance = NotificationManager.IMPORTANCE_LOW;
            channel = new NotificationChannel("AdicTIC", name, importance);
            channel.setSound(null, null);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        return channel;
    }
}
