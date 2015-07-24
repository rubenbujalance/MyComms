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

public class MyGcmListenerService extends GcmListenerService
{

    private final long NOTIFICATION_ID = 1;
    public MyGcmListenerService() {
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Bundle test = data;
        Log.i(Constants.TAG, "MyGcmListenerService.onMessageReceived: From-"+from+"; Message-"+message);

        sendNotification(message);
    }

    private void sendNotification(String message)
    {

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(message);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Messages: ");
        inboxStyle.addLine(message);
        mBuilder.setStyle(inboxStyle);

        Intent resultIntent = new Intent(this, DashBoardActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(DashBoardActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        if(resultPendingIntent!=null)
            mBuilder.setContentIntent(resultPendingIntent);

        mNotificationManager.notify
                (
                        (int)NOTIFICATION_ID
                        , mBuilder.build()
                );
    }

}
