package com.vodafone.mycomms.connection.token;

import android.content.Context;
import android.util.Log;

import com.framework.library.connection.ConnectionInterfaces;
import com.framework.library.connection.HttpConnection;
import com.framework.library.model.IModel;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.connection.BaseConnection;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.Constants;

/**
 * Created by str_vig on 15/05/2015.
 */
public class AuthRenewConnection  extends BaseConnection{
    private static String URL = "/auth/renew";

    public AuthRenewConnection(Context context, ConnectionInterfaces.ConnectionListener listener){
        super( URL, context , listener , HttpConnection.GET);
        Log.d(Constants.TAG, "AuthRenewConnection.AuthRenewConnection: ");

    }
}
