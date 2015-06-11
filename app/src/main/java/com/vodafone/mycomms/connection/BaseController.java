package com.vodafone.mycomms.connection;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.framework.library.controller.Controller;
import com.framework.library.exception.ConnectionException;
import com.framework.library.model.ConnectionResponse;
import com.vodafone.mycomms.connection.token.AuthRenewConnection;
import com.vodafone.mycomms.login.LoginActivity;
import com.vodafone.mycomms.main.SplashScreenActivity;
import com.vodafone.mycomms.util.Constants;

/**
 * Created by str_vig on 14/05/2015.
 */
public class BaseController extends Controller {

    IConnectionCallback connectionCallback;

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
        Log.w(Constants.TAG, "BaseController.onConnectionError: " + e.getUrl() + ", " + e.getException() + ", " + e.getContent());

        if(e.getException() != null){
            Log.d(Constants.TAG, "BaseController.onConnectionError: " + e.getException().getMessage());
            if(e.getException() instanceof ConnectionNotAvailableException){
                if(connectionCallback!= null) {
                    connectionCallback.onConnectionNotAvailable();
                }
            }
        }

        Log.e(Constants.TAG, "BaseController.onConnectionError: " + e.getContent());
        if(e.getContent() != null && e.getContent().contains(BaseConnection.VALUE_UNAUTHORIZED) /*&& BaseConnection.getNumOfFailedAuthRequest() < BaseConnection.MAX_AUTH_RETRY*/){
            Log.d(Constants.TAG, "BaseController.onConnectionError: unauthorized --> LoginActivity");
            //BaseConnection.setNumOfFailedAuthRequest(BaseConnection.getNumOfFailedAuthRequest() + 1);
            Intent in = new Intent(getContext(), LoginActivity.class);
            getContext().startActivity(in);
        }

        if(e.getContent() != null && e.getContent().contains(BaseConnection.VALUE_INVALID_ACCESS_TOKEN)){
            Log.d(Constants.TAG, "BaseController.onConnectionError: invalid_access_token --> AuthRenew");
            AuthRenewConnection authRenewConnection = new AuthRenewConnection(getContext(), this);
            authRenewConnection.request();
        }

        if(e.getUrl() != null && e.getUrl().contains(AuthRenewConnection.URL) && e.getContent().contains(BaseConnection.VALUE_INVALID_TOKEN) ){
            Log.d(Constants.TAG, "BaseController.onConnectionError: invalid_token after AuthRenew --> LoginActivity");
            Intent in = new Intent(getContext(), LoginActivity.class);
            getContext().startActivity(in);
        }

        if(e.getContent() != null && e.getContent().contains(BaseConnection.VALUE_INVALID_VERSION) ){
            Log.d(Constants.TAG, "BaseController.onConnectionError: invalid_version  --> SplashScreenActivity");
            Intent in = new Intent(getContext(), SplashScreenActivity.class);
            getContext().startActivity(in);
        }

    }

    public IConnectionCallback getConnectionCallback() {
        return connectionCallback;
    }

    public void setConnectionCallback(IConnectionCallback connectionCallback) {
        this.connectionCallback = connectionCallback;
    }

    public void showToast(String stringToShow) {
        Log.i(Constants.TAG, "BaseController.showToast: " + stringToShow);
        Toast toast = Toast.makeText(getContext(), stringToShow, Toast.LENGTH_LONG);
        toast.show();
    }
}