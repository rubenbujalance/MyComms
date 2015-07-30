package com.vodafone.mycomms.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;

public class SettingsMainActivity extends ToolbarActivity implements ProfileFragment.OnFragmentInteractionListener, PreferencesFragment.OnFragmentInteractionListener, AccountsFragment.OnFragmentInteractionListener{

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    public static final int VACATION_TIME_SETTER_ID = 1 ;
    public static final String VACATION_TIME_END_VALUE = "vacationTimeEndValue";
    private LinearLayout lay_no_connection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //This prevents the view focusing on the edit text and opening the keyboard
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.layout_profile_activity);

        lay_no_connection = (LinearLayout) findViewById(R.id.no_connection_layout);
        if(APIWrapper.isConnected(SettingsMainActivity.this))
            lay_no_connection.setVisibility(View.GONE);
        else
            lay_no_connection.setVisibility(View.VISIBLE);

        BusProvider.getInstance().register(this);

        SharedPreferences sp = getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        String profileFullName = sp.getString(Constants.PROFILE_FULLNAME_SHARED_PREF, "");

        activateToolbar();

        //This sets the user name on the Settings Toolbar. Right now, it's title is only "Settings"
//        setToolbarTitle(profileFullName);
        setToolbarTitle(getResources().getString(R.string.Settings));

        if (savedInstanceState == null) {
            FragmentTransaction transaction;
            transaction = getSupportFragmentManager().beginTransaction();
            ProfileListPagerFragment fragment = new ProfileListPagerFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }

        /*ImageView ivBtBack = (ImageView)findViewById(R.id.ivBtBack);
        ivBtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/

    }


    public void setMainActivityTitle(String title){
        this.setToolbarTitle(title);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    @Override
    public void onFragmentInteraction(String id) {
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        switch(requestCode) {
//            case (VACATION_TIME_SETTER_ID) : {
//                if (resultCode == Activity.RESULT_OK) {
//                    String newText = data.getStringExtra(VACATION_TIME_END_VALUE);
//
//                }
//                break;
//            }
//        }
    }

    @Subscribe
    public void onConnectivityChanged(ConnectivityChanged event)
    {

        lay_no_connection = (LinearLayout) findViewById(R.id.no_connection_layout);
        Log.e(Constants.TAG, "DashBoardActivity.onConnectivityChanged: "
                + event.getConnectivityStatus().toString());
        if(event.getConnectivityStatus()!= ConnectivityStatus.MOBILE_CONNECTED &&
                event.getConnectivityStatus()!=ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET)
        {
            lay_no_connection.setVisibility(View.VISIBLE);
            Constants.isConnectionAvailable = false;
        }

        else
        {
            lay_no_connection.setVisibility(View.GONE);
            Constants.isConnectionAvailable = true;
        }

    }
}
