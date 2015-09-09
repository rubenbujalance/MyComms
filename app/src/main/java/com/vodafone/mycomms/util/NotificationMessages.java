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

        String message = data.getString("message");

        NotificationCompat.Builder builder = createNotificationMessagesInstance(context, data);
//        String from = data.getString(Constants.NOTIFICATION_BUNDLE_FROM_KEY);
//        if (from != null && from.contains("@"))
//            from = from.substring(0, from.indexOf("@"));
//        else from = null;

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
//        inboxStyle.setBigContentTitle(context.getString(R.string.new_unread_messages));
//        addMessageToInbox(from, message);
//        fillInbox(from, inboxStyle);
//        inboxStyle.setSummaryText(inboxMessages.get(from).size() + " new messages");
        builder.setStyle(inboxStyle);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentText(message);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setAutoCancel(true);
        Notification notification = builder.build();

//        if(from!=null) {
//            int id;
//            if(notificationIds.containsKey(from)) {id = notificationIds.get(from);}
//            else {
//                id = (int)System.currentTimeMillis();
//                notificationIds.put(from,id);
//            }
//
//            mNotificationManager.notify(id, notification);
//        }

        mNotificationManager.notify((int)System.currentTimeMillis(), notification);
    }

    private static void addMessageToInbox(String from, String message) {
        if(inboxMessages.containsKey(from))
        {inboxMessages.get(from).add(0,message);}
        else {
            ArrayList<String> messages = new ArrayList<>();
            messages.add(0,message);
            inboxMessages.put(from, messages);
        }
//        if(inboxMessages.size() > INBOX_LENGTH)
//            inboxMessages.remove(inboxMessages.size() - 1);
    }

    private static void fillInbox(String from, NotificationCompat.InboxStyle inboxStyle) {
        ArrayList<String> messages = inboxMessages.get(from);

        for(int i=0; i<INBOX_LENGTH; i++) {
            if(i >= messages.size()) break;
            inboxStyle.addLine(messages.get(i));
        }
    }

    public static void resetInboxMessages(Context context) {
        if(mNotificationManager != null)
            mNotificationManager.cancelAll();

        inboxMessages = new HashMap<>();
        notificationIds = new HashMap<>();
    }
}
