package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.util.Log;

import com.framework.library.connection.ConnectionInterfaces;
import com.framework.library.connection.HttpConnection;
import com.vodafone.mycomms.connection.BaseConnection;
import com.vodafone.mycomms.util.Constants;

public class FavouriteConnection extends BaseConnection {
    //private static final int method = HttpConnection.POST;

    public FavouriteConnection(Context context, ConnectionInterfaces.ConnectionListener listener, String apiCall, int method){
        super(apiCall, context, listener, method, false);
        Log.i(Constants.TAG, "FavouriteConnection.ContactConnection: " + apiCall);
    }
}