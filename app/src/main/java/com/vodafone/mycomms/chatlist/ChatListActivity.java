package com.vodafone.mycomms.chatlist;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.contacts.view.ContactListFragment;
import com.vodafone.mycomms.contacts.view.ContactListPagerFragment;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;

/**
 * Created by str_vig on 21/04/2015.
 */
public class ChatListActivity extends ToolbarActivity implements ChatListFragment.OnFragmentInteractionListener{

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(Constants.TAG, "ChatMainActivity.onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main_activity);
        activateToolbar();
        setToolbarTitle("Messages");
        activateFooter();

        setFooterListeners(this);
        setContactsListeners(this);

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
    public void onFragmentInteraction(String id) {

    }
}
