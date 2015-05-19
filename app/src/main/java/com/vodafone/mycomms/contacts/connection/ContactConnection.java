package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.util.Log;

import com.framework.library.connection.ConnectionInterfaces;
import com.framework.library.connection.HttpConnection;
import com.vodafone.mycomms.connection.BaseConnection;
import com.vodafone.mycomms.util.Constants;

/**
 * Created by AMG on 18/05/2015.
 */
public class ContactConnection extends BaseConnection {
    private static final String URL = "/api/me/contact?p=mc";
    private static final int method = HttpConnection.GET;

    public ContactConnection(Context context, ConnectionInterfaces.ConnectionListener listener){
        super(URL, context, listener, method);
        Log.i(Constants.TAG, "ContactConnection.ContactConnection");
    }
}