package com.vodafone.mycomms.login;

import android.app.Activity;
import android.util.Log;

import com.framework.library.controller.Controller;
import com.framework.library.model.ConnectionResponse;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.login.connection.LoginConnection;
import com.vodafone.mycomms.util.Constants;

import org.json.JSONArray;
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
        HashMap body = new HashMap<>();
//        body.put("username", email);
//        body.put("password", password);

        body.put("username", "ruben_bujalance@stratesys-ts.com");
        body.put("password", "i9Vs1Qm8U");
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

//            UserSecurity.setTokens(accessToken, refreshToken, expiresIn, this.getContext());

        } catch (Exception e){
            Log.e(Constants.TAG, "LoginController.onConnectionComplete: " , e);
        }
    }
}
