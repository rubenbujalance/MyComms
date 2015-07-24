package com.vodafone.mycomms.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.main.DashBoardActivity;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.NotificationMessages;

public class MyGcmListenerService extends GcmListenerService
{

    private final String GROUP_KEY_EMAILS  = "mycomms_group_key_notifier";

    public MyGcmListenerService()
    {
    }


    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Bundle test = data;
        Log.i(Constants.TAG, "MyGcmListenerService.onMessageReceived: From-" + from + "; Message-" + message);
        //sendNotification(message);
        NotificationMessages.sendMessage(message, this);
    }

    private void sendNotification(String message)
    {

        int number = 0;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("MyComms")
                        .setContentText(message)
                        .setGroup(GROUP_KEY_EMAILS)
                        .setGroupSummary(true)
                        .setNumber(++number);

        Intent resultIntent = new Intent(this, DashBoardActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(DashBoardActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_CANCEL_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService
                    (Context.NOTIFICATION_SERVICE);

        notificationManager.notify(1, mBuilder.build
                ());

    }



}
