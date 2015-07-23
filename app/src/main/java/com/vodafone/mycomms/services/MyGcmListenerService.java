package com.vodafone.mycomms.services;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.Constants;

import java.util.Calendar;

public class MyGcmListenerService extends GcmListenerService {
    public MyGcmListenerService() {
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.i(Constants.TAG, "MyGcmListenerService.onMessageReceived: From-"+from+"; Message-"+message);

        sendNotification(message);
    }

    private void sendNotification(String message) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(message);

//        PendingIntent resultPendingIntent = null;
//
//        if(Build.VERSION.SDK_INT >= 16) {
//            // The stack builder object will contain an artificial back stack for the
//            // started Activity.
//            // This ensures that navigating backward from the Activity leads out of
//            // your application to the Home screen.
//            Intent resultIntent = new Intent(this, DashBoardActivity.class);
//            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//            // Adds the back stack for the Intent (but not the Intent itself)
//            stackBuilder.addParentStack(DashBoardActivity.class);
//            // Adds the Intent that starts the Activity to the top of the stack
//            stackBuilder.addNextIntent(resultIntent);
//            resultPendingIntent =
//                    stackBuilder.getPendingIntent(
//                            0,
//                            PendingIntent.FLAG_UPDATE_CURRENT
//                    );
//        }
//        else {
//            Intent resultIntent = new Intent(this, DashBoardActivity.class);
//            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//            // Adds the back stack for the Intent (but not the Intent itself)
//            stackBuilder.addParentStack(DashBoardActivity.class);
//            // Adds the Intent that starts the Activity to the top of the stack
//            stackBuilder.addNextIntent(resultIntent);
//            resultPendingIntent =
//                    stackBuilder.getPendingIntent(
//                            0,
//                            PendingIntent.FLAG_UPDATE_CURRENT
//                    );
//        }
//
//        if(resultPendingIntent!=null)
//            mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);



        long notifId = Calendar.getInstance().getTimeInMillis();
        mNotificationManager.notify((int)notifId, mBuilder.build());
    }

}
