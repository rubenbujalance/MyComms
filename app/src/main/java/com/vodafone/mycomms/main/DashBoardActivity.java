package com.vodafone.mycomms.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.os.Handler;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vodafone.mycomms.ContactListMainActivity;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.connection.BaseConnection;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.contacts.view.ContactListViewArrayAdapter;
import com.vodafone.mycomms.main.connection.INewsConnectionCallback;
import com.vodafone.mycomms.main.connection.NewsController;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.settings.ProfileController;
import com.vodafone.mycomms.settings.SessionController;
import com.vodafone.mycomms.settings.connection.IProfileConnectionCallback;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.util.UserSecurity;
import com.vodafone.mycomms.util.Utils;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;

public class DashBoardActivity extends ToolbarActivity implements INewsConnectionCallback, IProfileConnectionCallback {
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private LinearLayout noConnectionLayout;
    private ProfileController profileController;
    private NewsController mNewsController;
    private String apiCall;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fill News slider
        mNewsController = new NewsController(this);
        apiCall = Constants.NEWS_API_GET;
        mNewsController.getNewsList(apiCall);

        mNewsController.setConnectionCallback(this);

        // Footer layout
        BusProvider.getInstance().register(this);
        setContentView(R.layout.layout_dashboard);

        // Set Time line
        DateFormat tf = new SimpleDateFormat("HH:mm");
        String time = tf.format(Calendar.getInstance().getTime());

        TextView timeText = (TextView) findViewById(R.id.timeDashboard);
        timeText.setText(time);

        // Set Date line
        DateFormat df = new SimpleDateFormat("EEEE, d MMMM");
        String date = df.format(Calendar.getInstance().getTime());

        TextView dateText = (TextView) findViewById(R.id.dateDashboard);
        dateText.setText(date);

        noConnectionLayout = (LinearLayout) findViewById(R.id.no_connection_layout);
        activateFooter();

        setFooterListeners(this);

        activateFooterSelected(Constants.TOOLBAR_DASHBOARD);

        getProfileIdAndAccessToken();

        // Event Click listeners
        ImageView btMagnifier = (ImageView) findViewById(R.id.magnifier);
        btMagnifier.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Start Contacts activity
                Intent in = new Intent(DashBoardActivity.this, ContactListMainActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(in);
                finish();
            }
        });

        LinearLayout btFavourite = (LinearLayout) findViewById(R.id.LayoutFavourite);
        btFavourite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Start Favourites activity
                Intent in = new Intent(DashBoardActivity.this, ContactListMainActivity.class);
                in.putExtra(Constants.toolbar, false);
                in.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(in);
                finish();
            }
        });

    }

    private void getProfileIdAndAccessToken() {
        profileController = new ProfileController(this);

        //Save profile_id if accessToken has changed
        String profile_id = validateAccessToken();

        String deviceId = setDeviceId();

        //Initialize messaging server session (needs the profile_id saved)
        //if(profile_id != null) //If null, do initialization in callback method
        //    XMPPTransactions.initializeMsgServerSession(getApplicationContext());
    }

    private String validateAccessToken(){
        Log.i(Constants.TAG, "DashBoardActivity.validateAccessToken: ");
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
            return sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");
        }
    }

    private String setDeviceId(){
        Log.i(Constants.TAG, "DashBoardActivity.setDeviceId: ");
        TelephonyManager telephonyManager = (TelephonyManager
                )getSystemService( Context.TELEPHONY_SERVICE );
        String deviceId = Utils.getDeviceId(getContentResolver(), telephonyManager);
        SharedPreferences sp = getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Constants.DEVICE_ID_SHARED_PREF, deviceId);
        editor.apply();

        SessionController sessionController = new SessionController(this);
        sessionController.setDeviceId(deviceId);
        sessionController.setConnectionCallback(this);

        return deviceId;
    }

    //Prevent of going from main screen back to login
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);

        // Disconnect from the XMPP server
        XMPPTransactions.disconnectMsgServerSession();
    }

    @Override
    public void onProfileReceived(model.UserProfile userProfile) {
        Log.i(Constants.TAG, "DashBoardActivity.onProfileReceived: ");
        profileController.setProfileId(userProfile.getId());

        //XMPPTransactions.initializeMsgServerSession(getApplicationContext());
    }

    @Override
    public void onProfileConnectionError() {
        Log.e(Constants.TAG, "DashBoardActivity.onProfileConnectionError: Error reading profile from api, finishing");
        finish();
    }

    @Override
    public void onUpdateProfileConnectionError() {

    }

    @Override
    public void onUpdateProfileConnectionCompleted() {

    }

    @Override
    public void onPasswordChangeError(String error) {

    }

    @Override
    public void onPasswordChangeCompleted() {

    }

    @Override
    public void onNewsResponse(ArrayList newsList, boolean morePages, int offsetPaging) {
        Log.i(Constants.TAG, "onNewsResponse: " + apiCall);

        if (morePages){
            mNewsController.getNewsList(apiCall + "&o=" + offsetPaging);
        }
    }

    @Override
    public void onConnectionNotAvailable() {

    }

}
