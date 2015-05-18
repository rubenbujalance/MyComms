package com.vodafone.mycomms;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.vodafone.mycomms.contacts.view.ContactListFragment;
import com.vodafone.mycomms.contacts.view.ContactListPagerFragment;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;

import io.realm.Realm;

/**
 * Created by str_vig on 21/04/2015.
 */
public class ContactListMainActivity extends ToolbarActivity implements ContactListFragment.OnFragmentInteractionListener{

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private Realm realm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(Constants.TAG, "ContactListMainActivity.onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main_activity);
        activateToolbar();
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
        //Realm.deleteRealmFile(this);
        //realm = Realm.getInstance(this);
        //MockClass mockClass = new MockClass(this, realm);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
