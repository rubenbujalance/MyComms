//package com.vodafone.mycomms.settings;
//
//import android.app.Activity;
//import android.support.v4.app.Fragment;
//import android.util.Log;
//
//import com.framework.library.connection.HttpConnection;
//import com.framework.library.exception.ConnectionException;
//import com.framework.library.model.ConnectionResponse;
//import com.vodafone.mycomms.connection.BaseController;
//import com.vodafone.mycomms.settings.connection.SessionConnection;
//import com.vodafone.mycomms.util.Constants;
//
//import org.json.JSONObject;
//
//import java.util.HashMap;
//
//public class SessionController extends BaseController {
//
//    private SessionConnection sessionConnection;
//
//    public SessionController(Fragment fragment) {
//        super(fragment);
//    }
//
//    public SessionController(Activity activity) {
//        super(activity);
//    }
//
//    public void setDeviceId(String deviceId) {
//        Log.i(Constants.TAG, "SessionController.setDeviceId: deviceId " + deviceId);
//        HashMap<String, String> body = new HashMap<String, String>();
//        body.put(Constants.PROFILE_DEVICE_ID,deviceId );
//        JSONObject json = new JSONObject(body);
//        sessionConnection = new SessionConnection(getContext(),this, HttpConnection.POST);
//        sessionConnection.setPayLoad(json.toString());
//        sessionConnection.request();
//    }
//
//    @Override
//    public void onConnectionComplete(ConnectionResponse response){
//        super.onConnectionComplete(response);
//        Log.d(Constants.TAG, "SessionController.onConnectionComplete: " + response.getUrl());
//
//        if(response.getUrl() != null) {
//            String result = response.getData().toString();
//            Log.d(Constants.TAG, "SessionController.onConnectionComplete: result " + result);
//        }
//    }
//
//    @Override
//    public void onConnectionError(ConnectionException ex){
//        super.onConnectionError(ex);
//        Log.e(Constants.TAG, "SessionController.onConnectionError: " + ex.getUrl() + "," + ex.getContent());
//    }
//}
