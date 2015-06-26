package com.vodafone.mycomms;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.contacts.connection.DownloadContactsAsyncTask;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.InitNews;
import com.vodafone.mycomms.events.InitProfileAndContacts;
import com.vodafone.mycomms.events.SetContactListAdapterEvent;
import com.vodafone.mycomms.main.connection.DownloadNewsAsyncTask;
import com.vodafone.mycomms.main.connection.NewsController;
import com.vodafone.mycomms.settings.ProfileController;
import com.vodafone.mycomms.settings.connection.FilePushToServerController;
import com.vodafone.mycomms.settings.connection.IProfileConnectionCallback;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UserSecurity;
import com.vodafone.mycomms.util.Utils;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

import java.io.File;
import java.util.HashMap;
import java.util.TimeZone;

import io.realm.Realm;
import model.UserProfile;

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
    private FilePushToServerController filePushToServerController;
    private SharedPreferences sp;
    private String profile_id;

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
        sp = getSharedPreferences(
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
    public void onProfileReceived(UserProfile userProfile) {
        Log.i(Constants.TAG, "MycommsApp.onProfileReceived: ");
        String timeZone = TimeZone.getDefault().getID();
        String test = userProfile.getTimezone();
        if (timeZone.equals(userProfile.getTimezone())) {
            profileController.setUserProfile(userProfile.getId(),
                    userProfile.getFirstName() + " " + userProfile.getLastName(),
                    userProfile.getPlatforms(),
                    userProfile.getTimezone());
        } else{
            profileController.setUserProfile(userProfile.getId(),
                    userProfile.getFirstName() + " " + userProfile.getLastName(),
                    userProfile.getPlatforms(),
                    timeZone);

            //profileController.updateUserProfileInDB(firstName, lastName, company, position, officeLocation);
            UserProfile newProfile = new UserProfile();
            newProfile = userProfile;
            Realm realm = Realm.getInstance(this);
            realm.beginTransaction();
            newProfile.setTimezone(timeZone);
            realm.copyToRealmOrUpdate(newProfile);
            realm.commitTransaction();
            //newProfile.setTimezone(timeZone);
            realm.close();
            HashMap<String, String> body = new HashMap<String, String>();
            if(userProfile.getTimezone() != null && !userProfile.getTimezone().equals("")) body.put("timeZone",userProfile.getTimezone());

            boolean isValid  = Utils.validateStringHashMap(body);
            if(!isValid) {
                profileController.showToast("Info not valid");
            } else {
                profileController.updateTimeZone(body);
            }
        }
        new DownloadContactsAsyncTask().execute(mContext);

        XMPPTransactions.initializeMsgServerSession(mContext, false);

        if(sp.getBoolean(Constants.FIRST_TIME_AVATAR_DELIVERY,false))
        {
            this.profile_id = userProfile.getId();
            new sendAvatar().execute();
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(Constants.FIRST_TIME_AVATAR_DELIVERY, false) ;
            editor.apply();
        }
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
    public void initProfileAndContacts(InitProfileAndContacts event)
    {
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

    public class sendAvatar extends AsyncTask<Void, Void, String>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try
            {
                String profId = "new_profile";
                File file =  new File(mContext.getFilesDir(), Constants.CONTACT_AVATAR_DIR +
                        "avatar_"+profId+".jpg");
                FilePushToServerController filePushToServerController =
                        new FilePushToServerController(mContext);

                filePushToServerController.sendImageRequest
                        (
                                Constants.CONTACT_API_POST_AVATAR,
                                Constants.MULTIPART_AVATAR,
                                file,
                                Constants.MEDIA_TYPE_JPG
                        );

                String response = filePushToServerController.executeRequest();
                file.renameTo(new File(mContext.getFilesDir(),Constants.CONTACT_AVATAR_DIR +
                        "avatar_"+profile_id+".jpg"));
                return response;
            }
            catch (Exception e)
            {
                Log.e(Constants.TAG, "FilePushToServerController.sendFile -> doInBackground: ERROR " + e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            Log.d(Constants.TAG, "FilePushToServerController.sendFile: Response content: " + result);
        }
    }

    @Subscribe
    public void contactsLoadedEvent(SetContactListAdapterEvent event){
        XMPPTransactions.initializeMsgServerSession(getApplicationContext(), true);
    }
}
