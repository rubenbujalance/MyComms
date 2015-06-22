package com.vodafone.mycomms.main.connection;

import android.content.Context;
import android.util.Log;

import com.framework.library.connection.ConnectionInterfaces;
import com.framework.library.connection.HttpConnection;
import com.vodafone.mycomms.connection.BaseConnection;
import com.vodafone.mycomms.util.Constants;

public class NewsConnection extends BaseConnection {
    private static final int method = HttpConnection.GET;

    public NewsConnection(Context context, ConnectionInterfaces.ConnectionListener listener, String apiCall) {
        super(apiCall, context, listener, method, true);
        Log.i(Constants.TAG, "NewsConnection.NewsConnection " + apiCall);
    }
}
