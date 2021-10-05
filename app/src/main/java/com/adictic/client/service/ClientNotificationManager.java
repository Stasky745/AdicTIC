package com.adictic.client.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.adictic.client.R;
import com.adictic.client.ui.chat.ChatActivity;
import com.adictic.client.ui.main.NavActivity;
import com.adictic.common.util.MyNotificationManager;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class ClientNotificationManager extends MyNotificationManager {

    public ClientNotificationManager(Context context){
        super(context);
    }

    public void displayGeneralNotification(String title, String body, @NotNull Class<?> activityIntent, Channels channel, Integer id) {
        displayGeneralNotification(title, body, new Intent(mCtx, activityIntent), channel, id);
    }

    public void displayGeneralNotification(String title, String body, Intent activityIntent, Channels channel, Integer notifID) {

        if(notifID == -1)
            notifID = new Random().nextInt();

        if(activityIntent==null) activityIntent = new Intent(mCtx, NavActivity.class);
        activityIntent.putExtra("notification_id", notifID);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mCtx);
        stackBuilder.addNextIntent(activityIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder mBuilder = createNotificationBuilder(title, body, channel, pendingIntent, R.drawable.adictic_nolletra);

        NotificationManager mNotifyMgr = createNotificationManager(channel);

        if (mNotifyMgr != null) {
            mNotifyMgr.notify(notifID, mBuilder.build());
        }
    }

    public void displayNotificationChat(String title, String body, Long userID, Long myId) {

        Intent chatIntent = new Intent(mCtx, ChatActivity.class);
        chatIntent.putExtra("userId", userID);
        chatIntent.putExtra("myId", myId);

        Intent chatListIntent = new Intent(mCtx, NavActivity.class);
        chatListIntent.putExtra("GoToChats", true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mCtx);
        stackBuilder.addNextIntent(chatListIntent);
        stackBuilder.addNextIntent(chatIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = createNotificationBuilder(title, body, Channels.CHAT, pendingIntent, R.drawable.adictic_nolletra);

        NotificationManager mNotifyMgr = createNotificationManager(Channels.CHAT);

        if (mNotifyMgr != null) {
            mNotifyMgr.notify(new Random().nextInt(), mBuilder.build());
        }

    }
}
