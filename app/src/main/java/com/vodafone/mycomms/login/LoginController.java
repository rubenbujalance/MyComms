package com.vodafone.mycomms.login;

import android.app.Activity;
import android.util.Log;

import com.framework.library.controller.Controller;
import com.framework.library.model.ConnectionResponse;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.login.connection.ILoginConnectionCallback;
import com.vodafone.mycomms.login.connection.LoginConnection;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UserSecurity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by str_vig on 14/05/2015.
 */

public class LoginController extends BaseController {

    private LoginConnection loginConnection;
    private ILoginConnectionCallback loginConnectionCallback;

    public LoginController(Activity activity) {
        super(activity);
    }

    public void startLogin(String email, String password){
        Log.d(Constants.TAG, "LoginController.startLogin: ");
        HashMap body = new HashMap<>();
        body.put("username", email);
        body.put("password", password);

        //body.put("username", "ruben_bujalance@stratesys-ts.com");
        //body.put("password", "w9Va6Xa4J");
        startLogin(body);
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

}
