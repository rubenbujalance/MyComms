package com.vodafone.mycomms.login.connection;

import android.content.Context;
import android.util.Log;

import com.framework.library.connection.ConnectionInterfaces.ConnectionListener;
import com.framework.library.connection.HttpConnection;
import com.vodafone.mycomms.connection.BaseConnection;
import com.vodafone.mycomms.util.Constants;

import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.util.Iterator;

/**
 * Created by str_vig on 14/05/2015.
 */
public class LoginConnection extends BaseConnection{
    private static final String URL = "/auth/login";

    public LoginConnection(Context context, ConnectionListener listener){
        super(URL, context, listener, HttpConnection.POST);
        Log.d(Constants.TAG, "LoginConnection.LoginConnection: ");
    }


    public void setPayLoad(String json){
        Log.d(Constants.TAG, "LoginConnection.setPayLoad: ");

        this.getConnection().setEncoding("UTF-8");
        this.getConnection().setData(json.toString());
    }



}
