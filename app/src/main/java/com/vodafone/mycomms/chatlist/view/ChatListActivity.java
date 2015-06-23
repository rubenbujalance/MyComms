package com.vodafone.mycomms.chatlist.view;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

/**
 * Created by str_vig on 21/04/2015.
 */
public class ChatListActivity extends ToolbarActivity{

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(Constants.TAG, "ChatMainActivity.onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main_activity);
        activateChatListToolbar();
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
        XMPPTransactions.initializeMsgServerSession(getApplicationContext(), false);
    }
}
