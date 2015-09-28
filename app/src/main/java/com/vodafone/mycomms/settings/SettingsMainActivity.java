package com.vodafone.mycomms.settings;

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
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.main.SplashScreenActivity;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.util.UncaughtExceptionHandlerController;
import com.vodafone.mycomms.util.Utils;

public class SettingsMainActivity extends ToolbarActivity implements ProfileFragment.OnFragmentInteractionListener, PreferencesFragment.OnFragmentInteractionListener, AccountsFragment.OnFragmentInteractionListener{

    public static final int VACATION_TIME_SETTER_ID = 1 ;
    public static final String VACATION_TIME_END_VALUE = "vacationTimeEndValue";
    private LinearLayout lay_no_connection;
    private LinearLayout lay_exit_preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler
                (
                        new UncaughtExceptionHandlerController(this, SplashScreenActivity.class)
                );
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.layout_profile_activity);

        lay_no_connection = (LinearLayout) findViewById(R.id.no_connection_layout);
        lay_exit_preferences = (LinearLayout) findViewById(R.id.lay_exit_preferences);
        lay_exit_preferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if(Utils.isConnected(SettingsMainActivity.this))
            lay_no_connection.setVisibility(View.GONE);
        else
            lay_no_connection.setVisibility(View.VISIBLE);

        BusProvider.getInstance().register(this);
        activateToolbar();
        setToolbarTitle(getResources().getString(R.string.Settings));

        if (savedInstanceState == null) {
            FragmentTransaction transaction;
            transaction = getSupportFragmentManager().beginTransaction();
            ProfileListPagerFragment fragment = new ProfileListPagerFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }
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

    @Override
    public void onStart()
    {
        super.onStart();
        MycommsApp.activityStarted();
    }

    @Override
    public void onStop()
    {
        MycommsApp.activityStopped();
        super.onStop();
    }
}
