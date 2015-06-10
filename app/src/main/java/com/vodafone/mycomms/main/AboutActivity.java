package com.vodafone.mycomms.main;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

public class AboutActivity  extends ToolbarActivity {
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private LinearLayout noConnectionLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(Constants.TAG, "AboutActivity.onCreate: ");
        super.onCreate(savedInstanceState);
        BusProvider.getInstance().register(this);
        setContentView(R.layout.layout_about);
        noConnectionLayout = (LinearLayout) findViewById(R.id.no_connection_layout);

        ImageView ivBtBack = (ImageView)findViewById(R.id.ivBtBack);
        ivBtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);

        // Disconnect from the XMPP server
        XMPPTransactions.disconnectMsgServerSession();
    }
}
