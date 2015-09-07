package com.vodafone.mycomms.chatlist.view;

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
import com.vodafone.mycomms.events.ChatsReceivedEvent;
import com.vodafone.mycomms.events.MessageStatusChanged;
import com.vodafone.mycomms.main.SplashScreenActivity;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.NotificationMessages;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.util.UncaughtExceptionHandlerController;
import com.vodafone.mycomms.util.Utils;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

/**
 * Created by str_vig on 21/04/2015.
 */
public class ChatListActivity extends ToolbarActivity{

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private LinearLayout lay_no_connection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(Constants.TAG, "ChatMainActivity.onCreate: ");
        super.onCreate(savedInstanceState);

        BusProvider.getInstance().register(this);

        Thread.setDefaultUncaughtExceptionHandler
                (
                        new UncaughtExceptionHandlerController(this, SplashScreenActivity.class)
                );

        setContentView(R.layout.layout_main_activity);
        lay_no_connection = (LinearLayout) findViewById(R.id.no_connection_layout);
        lay_no_connection = (LinearLayout) findViewById(R.id.no_connection_layout);
        if(Utils.isConnected(ChatListActivity.this))
            lay_no_connection.setVisibility(View.GONE);
        else
            lay_no_connection.setVisibility(View.VISIBLE);

        enableToolbarIsClicked(false);
        activateChatListToolbar(ChatListActivity.this);
        activateFooter();

        setFooterListeners(this);
        setChatListListeners(this);
        activateFooterSelected(Constants.TOOLBAR_RECENTS);

        if (savedInstanceState == null) {
            FragmentTransaction transaction;
            transaction = getSupportFragmentManager().beginTransaction();
            ChatListFragment fragment = new ChatListFragment();
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
    protected void onResume() {
        super.onResume();

        //Reset notifications
        NotificationMessages.resetInboxMessages(ChatListActivity.this);

        setForegroundActivity(2);
        overridePendingTransition(0,0);
        //Update Pending Messages on Toolbar
        //It is done every time a message is received
        checkUnreadChatMessages();
        XMPPTransactions.checkAndReconnectXMPP(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onEventChatsReceived(ChatsReceivedEvent event){
        checkUnreadChatMessages();
    }

    @Subscribe
    public void onEventMessageStatusChanged(MessageStatusChanged event){
        checkUnreadChatMessages();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Subscribe
    public void onConnectivityChanged(ConnectivityChanged event)
    {

        Log.e(Constants.TAG, "DashBoardActivity.onConnectivityChanged: "
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
