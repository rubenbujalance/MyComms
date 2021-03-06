package com.vodafone.mycomms.settings.connection;

import android.content.Context;
import android.util.Log;

import com.framework.library.connection.ConnectionInterfaces;
import com.framework.library.connection.HttpConnection;
import com.vodafone.mycomms.connection.BaseConnection;
import com.vodafone.mycomms.util.Constants;

/**
 * Created by str_vig on 26/05/2015.
 */
public class PasswordConnection extends BaseConnection {

    public static final String URL = "/user/me/password";
    private static final int method = HttpConnection.PUT;

    public PasswordConnection(Context context, ConnectionInterfaces.ConnectionListener listener) {
        super(URL, context, listener, method, false);
        Log.d(Constants.TAG, "PasswordConnection.PasswordConnection: ");

    }
}
