package com.vodafone.mycomms.connection;

import android.content.Context;
import android.util.Log;

import com.framework.library.connection.ConnectionInterfaces;
import com.framework.library.connection.HttpConnection;
import com.vodafone.mycomms.util.Constants;

/**
 * Created by str_vig on 20/05/2015.
 */
public class TestConnection extends BaseConnection {
    private static final String URL = "/api/me";
    private static final int method = HttpConnection.GET;

    public TestConnection(Context context, ConnectionInterfaces.ConnectionListener listener){
        super(URL, context, listener, method, false);
        Log.d(Constants.TAG, "TestConnection.TestConnection: ");
    }

    public String toString (){
        return "TestConnection: " + URL;
    }

}
