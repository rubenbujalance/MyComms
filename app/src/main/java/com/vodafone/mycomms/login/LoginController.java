package com.vodafone.mycomms.login;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.framework.library.exception.ConnectionException;
import com.framework.library.model.ConnectionResponse;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.login.connection.ILoginConnectionCallback;
import com.vodafone.mycomms.login.connection.LoginConnection;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UserSecurity;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by str_vig on 14/05/2015.
 */

public class LoginController extends BaseController {

    private LoginConnection loginConnection;
    public LoginController(Activity activity) {
        super(activity);
    }

    public void startLogin(String email, String password){
        Log.d(Constants.TAG, "LoginController.startLogin: ");

        HashMap<String, String> body = new HashMap<>();
        body.put("username", email);
        body.put("password", password);

        new GCMGetTokenAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, body);
    }

    private void startLogin(HashMap<String,Object> params){
        Log.d(Constants.TAG, "LoginController.startLogin: ");
        if(loginConnection != null){
            loginConnection.cancel();
        }

        loginConnection = new LoginConnection(getContext(), this);

        JSONObject json = null;
        if(params != null)
            json = new JSONObject(params);
        else
            json = new JSONObject();

        loginConnection.setPayLoad(json.toString());
        loginConnection.request();
    }

    @Override
    public void onConnectionComplete(ConnectionResponse response){
        super.onConnectionComplete(response);
        Log.d(Constants.TAG, "LoginController.onConnectionComplete: ");

        String result = response.getData().toString();

        JSONObject jsonResponse;
        try {
            jsonResponse = new JSONObject(result);
            String accessToken = jsonResponse.getString("accessToken");
            String refreshToken = jsonResponse.getString("refreshToken");
            long expiresIn = jsonResponse.getLong("expiresIn");

            UserSecurity.setTokens(accessToken, refreshToken, expiresIn, this.getContext());

        } catch (Exception e){
            Log.e(Constants.TAG, "LoginController.onConnectionComplete: Exception while processing json: " , e);
        }

        if(this.getConnectionCallback() != null && this.getConnectionCallback() instanceof ILoginConnectionCallback && response.getUrl() !=null  && response.getUrl().contains(LoginConnection.URL)){
            ((ILoginConnectionCallback)this.getConnectionCallback()).onLoginSuccess();
        }
    }

    @Override
    public void onConnectionError(ConnectionException ex){
        super.onConnectionError(ex);
        Log.w(Constants.TAG, "LoginController.onConnectionError: ");
        if(this.getConnectionCallback() != null && this.getConnectionCallback() instanceof ILoginConnectionCallback && ex.getUrl() !=null  && ex.getUrl().contains(LoginConnection.URL)){
            ((ILoginConnectionCallback)this.getConnectionCallback()).onLoginError(getContext().getString(R.string.oops_wrong_email));
        }
    }

    private class GCMGetTokenAsyncTask extends AsyncTask<Object, Void, Object> {
        @Override
        protected Object doInBackground(Object... params) {
            HashMap<String, String> body = (HashMap<String, String>) params[0];

            String token = Utils.getGCMToken(getContext());
            if(token!=null)
                body.put("deviceId", token);
            return body;
        }

        @Override
        protected void onPostExecute(Object obj) {
            HashMap body = (HashMap)obj;
            startLogin(body);
        }
    }

}
