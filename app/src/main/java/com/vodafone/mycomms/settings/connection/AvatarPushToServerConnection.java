package com.vodafone.mycomms.settings.connection;

import android.content.Context;
import android.util.Log;

import com.framework.library.connection.ConnectionInterfaces;
import com.framework.library.connection.HttpConnection;
import com.vodafone.mycomms.connection.BaseConnection;
import com.vodafone.mycomms.util.Constants;

/**
 * Created by str_oan on 15/06/2015.
 */
public class AvatarPushToServerConnection  extends BaseConnection {
    private static final int method = HttpConnection.POST;

    public AvatarPushToServerConnection(Context context, ConnectionInterfaces.ConnectionListener listener, String apiCall){
        super(apiCall, context, listener, method, false);
        Log.i(Constants.TAG, "AvatarPushToServerConnection.AvatarPushToServerConnection: " + apiCall);
    }
}