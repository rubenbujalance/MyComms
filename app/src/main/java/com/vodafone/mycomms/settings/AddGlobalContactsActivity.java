package com.vodafone.mycomms.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;

public class AddGlobalContactsActivity extends ToolbarActivity {

    private LinearLayout layout_no_connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_global_contacts);

        layout_no_connection = (LinearLayout)findViewById(R.id.no_connection_bar_layout);

        ImageView ivBtBack = (ImageView)findViewById(R.id.ivBtBack);
        ivBtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Subscribe
     public void onConnectivityChanged(ConnectivityChanged event)
    {

        Log.e(Constants.TAG, "DashBoardActivity.onConnectivityChanged: "
                + event.getConnectivityStatus().toString());
        if(event.getConnectivityStatus()!= ConnectivityStatus.MOBILE_CONNECTED &&
                event.getConnectivityStatus()!=ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET)
            layout_no_connection.setVisibility(View.VISIBLE);
        else
            layout_no_connection.setVisibility(View.GONE);
    }
}