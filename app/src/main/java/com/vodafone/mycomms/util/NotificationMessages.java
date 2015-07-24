package com.vodafone.mycomms.util;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.main.DashBoardActivity;
import com.vodafone.mycomms.realm.RealmChatTransactions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by str_oan on 24/07/2015.
 */
public final class NotificationMessages extends Activity
{

    public static NotificationCompat.Builder mBuilder;
    public static NotificationManager mNotificationManager = null;
    public static PendingIntent resultPendingIntent;
    public static int notifyId = 1;
    public static final int INBOX_LENGTH = 5;
    public static List<String> inboxMessages;

    private static void createNotificationMessagesInstance(Context context)
    {

        mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R
                                .mipmap.ic_launcher))
                        .setContentTitle(context.getString(R.string.app_name));

        Intent resultIntent = new Intent(context, DashBoardActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(DashBoardActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        inboxMessages = new ArrayList<>();
    }


    public static void sendMessage(String message, Context context)
    {
        if(null == mNotificationManager)
            createNotificationMessagesInstance(context);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("New unread messages: ");
        inboxStyle.setSummaryText(getAllUnreadMessages(context) + " new messages");
        addMessageToInbox(message);
        fillInbox(inboxStyle);
        mBuilder.setStyle(inboxStyle);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setContentText(getAllUnreadMessages(context) + " new messages");
        mBuilder.setTicker("You have new message");
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        mNotificationManager.notify(notifyId, mBuilder.build());
    }

    private static void addMessageToInbox(String message)
    {
        inboxMessages.add(0, message);
        if(inboxMessages.size() > INBOX_LENGTH)
            inboxMessages.remove(INBOX_LENGTH - 1);
    }
    private static void fillInbox(NotificationCompat.InboxStyle inboxStyle)
    {
        for(String message : inboxMessages)
        {
            inboxStyle.addLine(message);
        }
    }

    private static long getAllUnreadMessages(Context context)
    {
        RealmChatTransactions realmChatTransactions = new RealmChatTransactions(context);
        long unreadMessages = realmChatTransactions.getAllChatPendingMessagesCount();
        realmChatTransactions.closeRealm();
        return unreadMessages;
    }

}
