package com.vodafone.mycomms;

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
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;

/**
 * Created by str_vig on 21/04/2015.
 */
public class ContactListMainActivity extends ToolbarActivity implements ContactListFragment.OnFragmentInteractionListener{

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private LinearLayout noConnectionLayout;

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
        // TODO - Check sharedPreferences

        // TODO - Upload avatar
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
    }

    public void setConnectionLayoutVisibility(boolean connection){
        if (connection){
            noConnectionLayout.setVisibility(View.GONE);
        } else{
            noConnectionLayout.setVisibility(View.VISIBLE);
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
}
