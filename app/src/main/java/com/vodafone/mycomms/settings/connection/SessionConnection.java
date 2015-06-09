package com.vodafone.mycomms.settings.connection;

import android.content.Context;
import android.util.Log;

import com.framework.library.connection.ConnectionInterfaces;
import com.vodafone.mycomms.connection.BaseConnection;
import com.vodafone.mycomms.util.Constants;

public class SessionConnection extends BaseConnection{

    public static final String URL = "/api/me/session";

    public SessionConnection(Context context, ConnectionInterfaces.ConnectionListener listener, int method){
        super(URL, context, listener, method);
        Log.i(Constants.TAG, "SessionConnection.SessionConnection: ");
    }
}
