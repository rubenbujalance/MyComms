package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.util.Log;

import com.framework.library.connection.ConnectionInterfaces;
import com.vodafone.mycomms.connection.BaseConnection;
import com.vodafone.mycomms.util.Constants;

/**
 * Created by str_vig on 21/05/2015.
 */
public class RecentContactConnection extends BaseConnection{

    public RecentContactConnection(Context context, ConnectionInterfaces.ConnectionListener listener, int method, String URL){
        super(URL, context, listener, method, false);
        Log.i(Constants.TAG, "RecentContactConnection.RecentContactConnection: ");
    }
}
