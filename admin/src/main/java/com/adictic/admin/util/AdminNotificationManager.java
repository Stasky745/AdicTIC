package com.adictic.admin.util;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.adictic.common.util.Constants;
import com.adictic.admin.MainActivity;
import com.adictic.admin.R;
import com.adictic.common.util.MyNotificationManager;

import java.util.Random;

public class AdminNotificationManager extends MyNotificationManager {

    private static AdminNotificationManager mInstance;

    public AdminNotificationManager(Context context) {
        super(context);
    }

    public static synchronized AdminNotificationManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AdminNotificationManager(context);
        }
        return mInstance;
    }

    public void displayGeneralNotification(String title, String body, Class activityIntent, Channels channel) {

        Intent mainIntent;
        if(activityIntent == null) mainIntent = new Intent(mCtx, MainActivity.class);
        else mainIntent = new Intent(mCtx, activityIntent);

        displayGeneralNotification(title, body, mainIntent, channel);
    }

    public void displayGeneralNotification(String title, String body, Intent activityIntent, Channels channel) {

        Integer notifID = new Random().nextInt();
        if(activityIntent==null) activityIntent = new Intent(mCtx, MainActivity.class);
        activityIntent.putExtra("notification_id", notifID);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mCtx);
        stackBuilder.addNextIntent(activityIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder mBuilder = createNotificationBuilder(title, body, channel, pendingIntent, R.drawable.adictic_lletra);

        NotificationManager mNotifyMgr = createNotificationManager(channel);

        if (mNotifyMgr != null) {
            mNotifyMgr.notify(notifID, mBuilder.build());
        }
    }

    public void displayNotificationChat(String title, String body, Long userID, Long myId) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mCtx, Constants.CHANNEL_ID)
                        .setAutoCancel(true)
                        .setContentTitle(title)
                        .setContentText(body);


        /*
         *  Clicking on the notification will take us to this intent
         *  Right now we are using the MainActivity as this is the only activity we have in our application
         *  But for your project you can customize it as you want
         * */

        Intent intent = new Intent(mCtx, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mCtx);
        stackBuilder.addNextIntent(intent);

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
