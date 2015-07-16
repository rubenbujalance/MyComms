package com.vodafone.mycomms;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.contacts.view.ContactListFragment;
import com.vodafone.mycomms.contacts.view.ContactListPagerFragment;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ChatsReceivedEvent;
import com.vodafone.mycomms.settings.connection.ISessionConnectionCallback;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

public class ContactListMainActivity extends ToolbarActivity implements ContactListFragment.OnFragmentInteractionListener, ISessionConnectionCallback {

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private LinearLayout lay_no_connection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(Constants.TAG, "ContactListMainActivity.onCreate: ");
        super.onCreate(savedInstanceState);
        BusProvider.getInstance().register(this);
        setContentView(R.layout.layout_main_activity);
        lay_no_connection = (LinearLayout) findViewById(R.id.no_connection_layout);
        lay_no_connection = (LinearLayout) findViewById(R.id.no_connection_layout);
        if(APIWrapper.isConnected(ContactListMainActivity.this))
            lay_no_connection.setVisibility(View.GONE);
        else
            lay_no_connection.setVisibility(View.VISIBLE);

        enableToolbarIsClicked(false);
        activateContactListToolbar();
        setToolbarTitle(getResources().getString(R.string.toolbar_title_contacts));
        activateFooter();

        setFooterListeners(this);
        setContactsListeners(this);

        if (savedInstanceState == null) {
            FragmentTransaction transaction;
            transaction = getSupportFragmentManager().beginTransaction();
            ContactListPagerFragment fragment = new ContactListPagerFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }

        activateFooterSelected(Constants.TOOLBAR_CONTACTS);
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
    public void onFragmentInteraction(String id) {}

    @Override
    public void onConnectionNotAvailable() {
        Log.e(Constants.TAG, "ContactListMainActivity.onProfileConnectionError: Error reading profile from api, finishing");
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
        XMPPTransactions.closeRealm();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setForegroundActivity(0);
        overridePendingTransition(0,0);
        //Update Pending Messages on Toolbar
        //RBM - It is done every time a message is received
        checkUnreadChatMessages();
        XMPPTransactions.checkAndReconnectXMPP(getApplicationContext());
    }

    @Subscribe
    public void onEventChatsReceived(ChatsReceivedEvent event){
        checkUnreadChatMessages();
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
}
