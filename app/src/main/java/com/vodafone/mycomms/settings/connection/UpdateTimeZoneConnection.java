package com.vodafone.mycomms.settings.connection;

import android.content.Context;
import android.util.Log;

import com.framework.library.connection.ConnectionInterfaces;
import com.framework.library.connection.HttpConnection;
import com.vodafone.mycomms.connection.BaseConnection;
import com.vodafone.mycomms.util.Constants;

public class UpdateTimeZoneConnection extends BaseConnection {

    public static final String URL = "/api/me/info";
    private static final int method = HttpConnection.POST;

    public UpdateTimeZoneConnection(Context context, ConnectionInterfaces.ConnectionListener listener) {
        super(URL, context, listener, method, false);
        Log.d(Constants.TAG, "UpdateTimeZoneConnection.UpdateTimeZoneConnection: ");
        // getConnection().setTree(new UserProfile());
    }
}
