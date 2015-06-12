package com.vodafone.mycomms;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.vodafone.mycomms.contacts.view.ContactListFragment;
import com.vodafone.mycomms.contacts.view.ContactListPagerFragment;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.settings.ProfileController;
import com.vodafone.mycomms.settings.connection.ISessionConnectionCallback;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

public class ContactListMainActivity extends ToolbarActivity implements ContactListFragment.OnFragmentInteractionListener, ISessionConnectionCallback {

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private LinearLayout noConnectionLayout;
    private ProfileController profileController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(Constants.TAG, "ContactListMainActivity.onCreate: ");
        super.onCreate(savedInstanceState);
        BusProvider.getInstance().register(this);
        setContentView(R.layout.layout_main_activity);
        noConnectionLayout = (LinearLayout) findViewById(R.id.no_connection_layout);
        activateContactListToolbar();
        setToolbarTitle("Contacts");
        activateFooter();

        setFooterListeners(this);
        setContactsListeners(this);

        //profileController = new ProfileController(this);

        //Save profile_id if accessToken has changed
        //String profile_id = validateAccessToken();

        //String deviceId = setDeviceId();

        //Initialize messaging server session (needs the profile_id saved)
        //if(profile_id != null) //If null, do initialization in callback method
            XMPPTransactions.initializeMsgServerSession(getApplicationContext());

        if (savedInstanceState == null) {
            FragmentTransaction transaction;
            transaction = getSupportFragmentManager().beginTransaction();
            ContactListPagerFragment fragment = new ContactListPagerFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }

        activateFooterSelected(Constants.TOOLBAR_CONTACTS);

        //Check if is first login and upload avatar
        checkAndUploadAvatar();
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
    public void onFragmentInteraction(String id) {

    }

    private void checkAndUploadAvatar()
    {
        //Check sharedPreferences
        // TODO RBM - Check sharedPreferences

        // TODO RBM - Upload avatar
    }

    public void setConnectionLayoutVisibility(boolean connection){
        if (connection){
            noConnectionLayout.setVisibility(View.GONE);
        } else{
            noConnectionLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onConnectionNotAvailable() {
        Log.e(Constants.TAG, "ContactListMainActivity.onProfileConnectionError: Error reading profile from api, finishing");
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);

        // Disconnect from the XMPP server
        XMPPTransactions.disconnectMsgServerSession();
    }
}
