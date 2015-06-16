package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.util.Log;

import com.framework.library.connection.ConnectionInterfaces;
import com.framework.library.connection.HttpConnection;
import com.vodafone.mycomms.connection.BaseConnection;
import com.vodafone.mycomms.util.Constants;

/**
 * Created by str_oan on 09/06/2015.
 */
public class SearchConnection extends BaseConnection {
    private static final int method = HttpConnection.GET;

     public SearchConnection(Context context, ConnectionInterfaces.ConnectionListener listener,
                             String apiCall)
    {
        super(apiCall, context, listener, method, false);
        Log.i(Constants.TAG, "SearchConnection.SearchConnection: " + apiCall);
    }
}