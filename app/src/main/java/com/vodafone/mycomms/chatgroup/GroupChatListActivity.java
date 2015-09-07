package com.vodafone.mycomms.chatgroup;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
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

public class GroupChatListActivity extends ToolbarActivity {

    private LinearLayout lay_no_connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main_activity);

        BusProvider.getInstance().register(this);

        Thread.setDefaultUncaughtExceptionHandler
                (
                        new UncaughtExceptionHandlerController(this, SplashScreenActivity.class)
                );

        lay_no_connection = (LinearLayout) findViewById(R.id.no_connection_layout);
        lay_no_connection = (LinearLayout) findViewById(R.id.no_connection_layout);
        if(Utils.isConnected(GroupChatListActivity.this))
            lay_no_connection.setVisibility(View.GONE);
        else
            lay_no_connection.setVisibility(View.VISIBLE);

        activateGroupChatListToolbar();
        hideFooter();

        setGroupChatListListeners(this);

        if (savedInstanceState == null) {
            FragmentTransaction transaction;
            transaction = getSupportFragmentManager().beginTransaction();
            GroupChatListFragment fragment = new GroupChatListFragment();
            transaction.replace(R.id.sample_content_fragment,fragment);
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
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onConnectivityChanged(ConnectivityChanged event)
    {

        Log.i(Constants.TAG, "DashBoardActivity.onConnectivityChanged: "
                + event.getConnectivityStatus().toString());

        if(event.getConnectivityStatus()!= ConnectivityStatus.MOBILE_CONNECTED &&
                event.getConnectivityStatus()!=ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET)
            lay_no_connection.setVisibility(View.VISIBLE);
        else
            lay_no_connection.setVisibility(View.GONE);
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
