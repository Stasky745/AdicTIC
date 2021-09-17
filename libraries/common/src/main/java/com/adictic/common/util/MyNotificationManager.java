package com.adictic.common.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MyNotificationManager {

    protected static MyNotificationManager mInstance;
    protected final Context mCtx;

    public MyNotificationManager(Context context) {
        mCtx = context;
    }

    public enum Channels {
        GENERAL, CHAT, VIDEOCHAT
    }

    protected static class Channel {
        public String id;
        public String name;
        public String description;
        public Integer notif_importance;

        public Channel(String id, String name, String description, Integer notif_importance){
            this.id = id;
            this.name = name;
            this.description = description;
            this.notif_importance = notif_importance;
        }
    }

    protected static final Map<Channels, Channel> channel_info = new HashMap<Channels, Channel>() {{
        put(Channels.GENERAL, new Channel("General", "GENERAL notification", "This is the description", NotificationManager.IMPORTANCE_DEFAULT));
        put(Channels.CHAT, new Channel("CHAT", "Chat notification", "New message in the chat", NotificationManager.IMPORTANCE_HIGH));
        put(Channels.VIDEOCHAT, new Channel("VIDEOCHAT", "Videochat notification", "Calling to a videochat", NotificationManager.IMPORTANCE_MAX));
    }};

    public static synchronized MyNotificationManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MyNotificationManager(context);
        }
        return mInstance;
    }

    protected NotificationCompat.Builder createNotificationBuilder(String title, String body, Channels channel, PendingIntent pendingIntent, int icon){
        Channel channelInfo = Objects.requireNonNull(channel_info.get(channel));

        return new NotificationCompat.Builder(mCtx, Objects.requireNonNull(channelInfo.id))
                .setAutoCancel(true)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(channelInfo.notif_importance)
                .setLargeIcon(BitmapFactory.decodeResource(mCtx.getResources(), icon))
                .setContentIntent(pendingIntent);
    }

    protected NotificationManager createNotificationManager(Channels channel) {
        NotificationManager notificationManager = mCtx.getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Channel channelInfo = channel_info.get(channel);
            if(channelInfo!=null) {
                NotificationChannel notificationChannel = new NotificationChannel(channelInfo.id, channelInfo.name, channelInfo.notif_importance);
                notificationChannel.setDescription(channelInfo.description);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        return notificationManager;
    }
}
