package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.util.Log;

import com.framework.library.connection.ConnectionInterfaces;
import com.framework.library.connection.HttpConnection;
import com.vodafone.mycomms.connection.BaseConnection;
import com.vodafone.mycomms.util.Constants;

public class ContactConnection extends BaseConnection {
    private static final String URL = "/api/me/contact?p=mc";
    private static final int method = HttpConnection.GET;

    public ContactConnection(Context context, ConnectionInterfaces.ConnectionListener listener, String apiCall){
        super(URL, context, listener, method, false);
        Log.i(Constants.TAG, "ContactConnection.ContactConnection");
    }
}