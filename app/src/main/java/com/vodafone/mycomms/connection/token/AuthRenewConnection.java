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
import com.vodafone.mycomms.util.UserSecurity;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by str_vig on 15/05/2015.
 */
public class AuthRenewConnection  extends BaseConnection{
    public static String URL = "/auth/renew";

    public AuthRenewConnection(Context context, ConnectionInterfaces.ConnectionListener listener){
        super( URL, context , listener , HttpConnection.GET, false);
        Log.d(Constants.TAG, "AuthRenewConnection.AuthRenewConnection: ");


        HashMap params = new HashMap<>();
        params.put("refreshToken", UserSecurity.getRefreshToken(context));

        JSONObject json = null;
        if(params != null)
            json = new JSONObject(params);
        else
            json = new JSONObject();

        this.setPayLoad(json.toString());
    }


}
