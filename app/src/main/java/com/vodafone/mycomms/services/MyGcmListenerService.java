package com.vodafone.mycomms.services;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;
import com.vodafone.mycomms.util.NotificationMessages;

public class MyGcmListenerService extends GcmListenerService
{

    private final String GROUP_KEY_EMAILS  = "mycomms_group_key_notifier";

    public MyGcmListenerService()
    {
    }


    @Override
    public void onMessageReceived(String from, Bundle data) {
        Bundle test = data;
        NotificationMessages.sendMessage(data, this);
    }
}
