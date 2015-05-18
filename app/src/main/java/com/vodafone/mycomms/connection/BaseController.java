package com.vodafone.mycomms.connection;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.framework.library.connection.HttpConnection;
import com.framework.library.controller.Controller;
import com.framework.library.exception.ConnectionException;
import com.framework.library.model.ConnectionResponse;
import com.vodafone.mycomms.connection.token.AuthRenewConnection;
import com.vodafone.mycomms.util.Constants;

/**
 * Created by str_vig on 14/05/2015.
 */
public class BaseController extends Controller {
    public BaseController(Activity activity) {
        super(activity);
    }

    public BaseController(Fragment fragment) {
        super(fragment);
    }


    @Override
    public void onConnectionStart(ConnectionResponse response) {
        Log.d(Constants.TAG, "BaseController.onConnectionStart: ");
    }

    @Override
    public void onConnectionComplete(ConnectionResponse response) {
        Log.d(Constants.TAG, "BaseController.onConnectionComplete: ");
        BaseConnection.setNumOfFailedAuthRequest(0);
        Log.d(Constants.TAG, "BaseController.onConnectionComplete:" + response.getData());

    }

    @Override
    public void onConnectionError(ConnectionException e) {
        Log.w(Constants.TAG, "BaseController.onConnectionError: " + e.getUrl() +  ", " + e.getException() + ", " + e.getContent());
        if(e.getException() != null){
            Log.d(Constants.TAG, "BaseController.onConnectionError: " + e.getException().getMessage());
        }


        if(e.getContent() != null && e.getContent().contains(BaseConnection.VALUE_UNAUTHORIZED) && BaseConnection.getNumOfFailedAuthRequest() < BaseConnection.MAX_AUTH_RETRY){
            BaseConnection.setNumOfFailedAuthRequest(BaseConnection.getNumOfFailedAuthRequest() +1);

            AuthRenewConnection authRenewConnection = new AuthRenewConnection(getContext(), this);
            authRenewConnection.request();
        }
    }


}