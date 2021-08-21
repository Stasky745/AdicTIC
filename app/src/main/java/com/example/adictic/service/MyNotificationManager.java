package com.example.adictic.service;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import androidx.core.app.NotificationCompat;

import com.adictic.common.util.Constants;
import com.example.adictic.R;
import com.example.adictic.ui.chat.ChatActivity;
import com.example.adictic.ui.main.NavActivity;

public class MyNotificationManager {

    private static MyNotificationManager mInstance;
    private final Context mCtx;

    private MyNotificationManager(Context context) {
        mCtx = context;
    }

    public static synchronized MyNotificationManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MyNotificationManager(context);
        }
        return mInstance;
    }

    public void displayNotification(String title, String body, Class activityIntent) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mCtx, Constants.CHANNEL_ID)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.adictic_nolletra)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setLargeIcon(BitmapFactory.decodeResource(mCtx.getResources(), R.drawable.adictic_nolletra));


        /*
         *  Clicking on the notification will take us to this intent
         *  Right now we are using the MainActivity as this is the only activity we have in our application
         *  But for your project you can customize it as you want
         * */
        Intent mainIntent;
        if(activityIntent == null)
            mainIntent = new Intent(mCtx, NavActivity.class);
        else
            mainIntent = new Intent(mCtx, activityIntent);
//
//        Intent gameIntent = new Intent(mCtx, GameProfile.class);
//        gameIntent.putExtra("gameId", gameID);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mCtx);
//        stackBuilder.addNextIntentWithParentStack(gameIntent);
        stackBuilder.addNextIntent(mainIntent);


        /*
         *  Now we will create a pending intent
         *  The method getActivity is taking 4 parameters
         *  All paramters are describing themselves
         *  0 is the request code (the second parameter)
         *  We can detect this code in the activity that will open by this we can get
         *  Which notification opened the activity
         * */
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);


        /*
         *  Setting the pending intent to notification builder
         * */

        mBuilder.setContentIntent(pendingIntent);

        NotificationManager mNotifyMgr =
                (NotificationManager) mCtx.getSystemService(NOTIFICATION_SERVICE);

        /*
         * The first parameter is the notification id
         * better don't give a literal here (right now we are giving a int literal)
         * because using this id we can modify it later
         * */
        if (mNotifyMgr != null) {
            mNotifyMgr.notify(1, mBuilder.build());
        }
    }

    public void displayNotificationChat(String title, String body, Long userID, Long myId) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mCtx, Constants.CHANNEL_ID)
                        .setAutoCancel(true)
                        .setSmallIcon(R.mipmap.adictic_logo)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setLargeIcon(BitmapFactory.decodeResource(mCtx.getResources(), R.mipmap.adictic_logo));


        /*
         *  Clicking on the notification will take us to this intent
         *  Right now we are using the MainActivity as this is the only activity we have in our application
         *  But for your project you can customize it as you want
         * */

        Intent chatIntent = new Intent(mCtx, ChatActivity.class);
        chatIntent.putExtra("userId", userID);
        chatIntent.putExtra("myId", myId);

        Intent chatListIntent = new Intent(mCtx, NavActivity.class);
        chatListIntent.putExtra("GoToChats", true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mCtx);
        stackBuilder.addNextIntent(chatListIntent);
        stackBuilder.addNextIntent(chatIntent);

        /*
         *  Now we will create a pending intent
         *  The method getActivity is taking 4 parameters
         *  All paramters are describing themselves
         *  0 is the request code (the second parameter)
         *  We can detect this code in the activity that will open by this we can get
         *  Which notification opened the activity
         * */
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        /*
         *  Setting the pending intent to notification builder
         * */

        mBuilder.setContentIntent(pendingIntent);

        NotificationManager mNotifyMgr =
                (NotificationManager) mCtx.getSystemService(NOTIFICATION_SERVICE);

        /*
         * The first parameter is the notification id
         * better don't give a literal here (right now we are giving a int literal)
         * because using this id we can modify it later
         * */
        if (mNotifyMgr != null) {
            mNotifyMgr.notify(1, mBuilder.build());
        }

    }
}
