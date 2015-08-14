package com.vodafone.mycomms.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.main.DashBoardActivity;
import com.vodafone.mycomms.main.MainActivity;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by str_oan on 24/07/2015.
 */
public final class NotificationMessages extends MainActivity
{

    public static NotificationCompat.Builder mBuilder;
    public static NotificationManager mNotificationManager = null;
    public static PendingIntent resultPendingIntent;
    public static int notificationId = 1;
    public static final int INBOX_LENGTH = 5;
    public static ArrayList<String> inboxMessages;

    private static void createNotificationMessagesInstance(Context context)
    {

        mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notif_white)
                        .setLargeIcon(BitmapFactory.decodeResource(
                                context.getResources(), R.mipmap.ic_launcher))
                        .setContentTitle(context.getString(R.string.app_name));

        Intent resultIntent = new Intent(context, DashBoardActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(DashBoardActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        inboxMessages = new ArrayList<>();
    }


    public static void sendMessage(String message, Context context)
    {
        if(null == mNotificationManager)
            createNotificationMessagesInstance(context);

        long timestamp = Calendar.getInstance().getTimeInMillis();

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(context.getString(R.string.new_unread_messages));
        //RBM: We can't know how many messages unread we have. They are not being saved in DB
        //when a notification arrives, until user opens the app
        addMessageToInbox(message);
        fillInbox(inboxStyle);
        inboxStyle.setSummaryText(inboxMessages.size() + " new messages");
        mBuilder.setStyle(inboxStyle);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setContentText(context.getString(R.string.new_unread_messages));
        mBuilder.setTicker(context.getString(R.string.new_unread_messages));
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        mBuilder.setAutoCancel(true);
        Notification notification = mBuilder.build();
        mNotificationManager.notify(notificationId, notification);
    }

    private static void addMessageToInbox(String message) {
        if(inboxMessages==null) inboxMessages = new ArrayList<>();
        inboxMessages.add(0, message);
//        if(inboxMessages.size() > INBOX_LENGTH)
//            inboxMessages.remove(inboxMessages.size() - 1);
    }

    private static void fillInbox(NotificationCompat.InboxStyle inboxStyle) {
        for(int i=0; i<INBOX_LENGTH; i++) {
            if(i >= inboxMessages.size()) break;
            inboxStyle.addLine(inboxMessages.get(i));
        }
    }

    public static void resetInboxMessages() {
        inboxMessages = new ArrayList<>();
    }

}
