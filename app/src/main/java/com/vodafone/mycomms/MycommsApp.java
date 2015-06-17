package com.vodafone.mycomms;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.contacts.connection.DownloadContactsAsyncTask;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.InitNews;
import com.vodafone.mycomms.events.InitProfileAndContacts;
import com.vodafone.mycomms.main.connection.DownloadNewsAsyncTask;
import com.vodafone.mycomms.main.connection.NewsController;
import com.vodafone.mycomms.settings.ProfileController;
import com.vodafone.mycomms.settings.connection.IProfileConnectionCallback;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UserSecurity;
import com.vodafone.mycomms.util.Utils;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

import java.util.ArrayList;

import model.News;

/**
 * Created by str_rbm on 02/04/2015.
 *
 * Main application singleton class
 * It handles global data and backend services
 */

public class MycommsApp extends Application implements IProfileConnectionCallback {

    private ProfileController profileController;
    private NewsController newsController;
    private Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(Constants.TAG, "MycommsApp.onCreate: ");
        BusProvider.getInstance().register(this);
        mContext = getApplicationContext();
        //getProfileIdAndAccessToken();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        BusProvider.getInstance().unregister(this);
    }

    public void getProfileIdAndAccessToken() {
        Log.i(Constants.TAG, "MycommsApp.getProfileIdAndAccessToken: ");
        profileController = new ProfileController(mContext);

        //Save profile_id if accessToken has changed
        String profile_id = validateAccessToken();

        String deviceId = setDeviceId();
    }

    private String validateAccessToken(){
        Log.i(Constants.TAG, "MycommsApp.validateAccessToken: ");
        String accessToken = UserSecurity.getAccessToken(this);
        SharedPreferences sp = getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

        String prefAccessToken = sp.getString(Constants.ACCESS_TOKEN_SHARED_PREF, "");
        if (prefAccessToken==null || prefAccessToken.equals("") || !prefAccessToken.equals(accessToken)){
            profileController.setConnectionCallback(this);
            profileController.getProfile();

            return null;
        }
        else {
            String profile = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");
            new DownloadContactsAsyncTask().execute(this);
            return profile;
        }
    }

    private String setDeviceId(){
        Log.i(Constants.TAG, "MycommsApp.setDeviceId: ");
        TelephonyManager telephonyManager = (TelephonyManager
                )getSystemService( Context.TELEPHONY_SERVICE );
        String deviceId = Utils.getDeviceId(getContentResolver(), telephonyManager);
        SharedPreferences sp = getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Constants.DEVICE_ID_SHARED_PREF, deviceId);
        editor.apply();

        //SessionController sessionController = new SessionController(mContext);
        //sessionController.setDeviceId(deviceId);
        //sessionController.setConnectionCallback(this);

        return deviceId;
    }

    @Override
    public void onProfileReceived(model.UserProfile userProfile) {
        Log.i(Constants.TAG, "MycommsApp.onProfileReceived: ");
        profileController.setUserProfile(userProfile.getId(), userProfile.getFirstName() + " " + userProfile.getLastName());
        new DownloadContactsAsyncTask().execute(mContext);

        XMPPTransactions.initializeMsgServerSession(this);
    }

    @Override
    public void onProfileConnectionError() {
        Log.e(Constants.TAG, "MycommsApp.onProfileConnectionError: ");
    }

    @Override
    public void onUpdateProfileConnectionError() {

    }

    @Override
    public void onUpdateProfileConnectionCompleted() {

    }

    @Override
    public void onPasswordChangeError(String error) {
        Log.e(Constants.TAG, "MycommsApp.onPasswordChangeError: ");
    }

    @Override
    public void onPasswordChangeCompleted() {

    }

    @Override
    public void onConnectionNotAvailable() {
        Log.e(Constants.TAG, "MycommsApp.onConnectionNotAvailable: ");
    }

    @Subscribe
    public void initProfileAndContacts(InitProfileAndContacts event){
        Log.i(Constants.TAG, "MyCommsApp.InitProfileAndContacts: ");
        getProfileIdAndAccessToken();
    }

    @Subscribe
    public void initNews(InitNews event){
        Log.i(Constants.TAG, "MyCommsApp.InitNews: ");
        getNews();
    }

    public void getNews() {
        Log.i(Constants.TAG, "MycommsApp.getNews: ");
        new DownloadNewsAsyncTask().execute(this);
    }
}
