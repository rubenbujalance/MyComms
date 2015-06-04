package com.vodafone.mycomms;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.contacts.view.ContactListFragment;
import com.vodafone.mycomms.contacts.view.ContactListPagerFragment;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.SetConnectionLayoutVisibility;
import com.vodafone.mycomms.events.SetNoConnectionLayoutVisibility;
import com.vodafone.mycomms.settings.ProfileController;
import com.vodafone.mycomms.settings.connection.IProfileConnectionCallback;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.util.UserSecurity;
import com.vodafone.mycomms.xmpp.XMPPTransactions;
import com.vodafone.mycomms.util.UserSecurity;

public class ContactListMainActivity extends ToolbarActivity implements IProfileConnectionCallback, ContactListFragment.OnFragmentInteractionListener {

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

        //Save profile_id if accessToken has changed
        String profile_id = validateAccessToken();

        //Initialize messaging server session (needs the profile_id saved)
        if(profile_id != null) //If null, do initialization in callback method
            XMPPTransactions.initializeMsgServerSession(this);

        validateAccessToken();

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

    private String validateAccessToken(){
        Log.i(Constants.TAG, "ContactListMainActivity.validateAccessToken: ");
        String accessToken = UserSecurity.getAccessToken(this);
        SharedPreferences sp = getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

        String prefAccessToken = sp.getString(Constants.ACCESS_TOKEN_SHARED_PREF, "");
        if (prefAccessToken==null || prefAccessToken.equals("") || !prefAccessToken.equals(accessToken)){
            profileController = new ProfileController(this);
            profileController.setConnectionCallback(this);
            profileController.getProfile();
            
            return null;
        }
        else {
            return sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");
        }
    }

    @Subscribe
    public void setNoConnectionLayoutVisibility(SetNoConnectionLayoutVisibility event){
        setConnectionLayoutVisibility(false);
    }

    @Subscribe
    public void setConnectionLayoutVisibility(SetConnectionLayoutVisibility event){
        setConnectionLayoutVisibility(true);
    }

    @Override
    public void onProfileReceived(model.UserProfile userProfile) {
        Log.i(Constants.TAG, "ContactListMainActivity.onProfileReceived: ");
        profileController.setProfileId(userProfile.getId());
        XMPPTransactions.initializeMsgServerSession(this);
    }

    @Override
    public void onProfileConnectionError() {
        Log.e(Constants.TAG, "ContactListMainActivity.onProfileConnectionError: Error reading profile from api, finishing");
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
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);

        // Disconnect from the XMPP server
        XMPPTransactions.getXmppConnection().disconnect();
    }

    @Override
    public void onConnectionNotAvailable() {
        Log.w(Constants.TAG, "ContactListMainActivity.onConnectionNotAvailable: ");
    }
}
