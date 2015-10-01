package com.vodafone.mycomms.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.main.MainActivity;
import com.vodafone.mycomms.main.SplashScreenActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by str_oan on 24/07/2015.
 */
public final class NotificationMessages extends MainActivity
{

    public static NotificationManager mNotificationManager = null;
    public static PendingIntent resultPendingIntent;
    public static final int INBOX_LENGTH = 5;
    public static HashMap<String,ArrayList<String>> inboxMessages = new HashMap<>();
    public static HashMap<String,Integer> notificationIds = new HashMap<>();

    private static NotificationCompat.Builder createNotificationMessagesInstance(Context context, Bundle data)
    {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notif_white)
                        .setLargeIcon(BitmapFactory.decodeResource(
                                context.getResources(), R.mipmap.ic_launcher))
                        .setContentTitle(context.getString(R.string.app_name));

        Intent resultIntent = new Intent(context, SplashScreenActivity.class);
        resultIntent.putExtra(Constants.NOTIFICATION_EXTRA_KEY, data);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(SplashScreenActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        return mBuilder;
    }

    public static void sendMessage(Bundle data, Context context)
    {
        if(null == mNotificationManager)
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //Avoid duplicated notifications
        if(notificationIds.containsKey(data.toString())) return;

        String message = data.getString("message");
        NotificationCompat.Builder builder = createNotificationMessagesInstance(context, data);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentText(message);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setAutoCancel(true);
        Notification notification = builder.build();

        int id = (int)System.currentTimeMillis();
        notificationIds.put(data.toString(), id);
        mNotificationManager.notify((int)System.currentTimeMillis(), notification);
    }

    public static void resetInboxMessages(Context context) {
        if(mNotificationManager != null)
            mNotificationManager.cancelAll();

        inboxMessages = new HashMap<>();
        notificationIds = new HashMap<>();
    }
}
