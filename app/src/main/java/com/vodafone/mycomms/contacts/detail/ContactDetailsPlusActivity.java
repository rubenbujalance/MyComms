package com.vodafone.mycomms.contacts.detail;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.custom.CircleImageView;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;

public class ContactDetailsPlusActivity extends ToolbarActivity {

    //Contact Detail
    private TextView tvContactName;
    private TextView tvPhoneNumber;
    private TextView tvEmail;
    private TextView tvOfficeLocation;
    private TextView tvPosition;
    private CircleImageView ivAvatar;

    //Buttons
    private ImageView btSms;
    private ImageView btEmail;
    private ImageView btChat;
    private ImageView btCall;
    private ImageView btEmailBar;
    private ImageView btChatBar;
    private ImageView btCallBar;

    private LinearLayout lay_no_connection;

    private String contactDetailInfo[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail_plus);

        BusProvider.getInstance().register(this);

        lay_no_connection = (LinearLayout) findViewById(R.id.no_connection_layout);
        if(APIWrapper.isConnected(ContactDetailsPlusActivity.this))
            lay_no_connection.setVisibility(View.GONE);
        else
            lay_no_connection.setVisibility(View.VISIBLE);

        initLayoutObjects();
        contactDetailInfo = getContactInformation();
        loadContactInfo();
    }

    private void initLayoutObjects() {
        tvContactName = (TextView) findViewById(R.id.contact_contact_name);
        tvPhoneNumber = (TextView) findViewById(R.id.contact_phone_number);
        tvEmail = (TextView) findViewById(R.id.contact_email);
        tvOfficeLocation = (TextView)findViewById(R.id.contact_office_location);
        ivAvatar = (CircleImageView)findViewById(R.id.avatar);
        tvPosition = (TextView) findViewById(R.id.contact_position);
//        textAvatar = (TextView)findViewById(R.id.avatarText); //TODO: Posar al layout
    }

    private String[] getContactInformation() {
        Intent intent = getIntent();
        return intent.getExtras().getStringArray(Constants.CONTACT_DETAIL_INFO);
    }

    private void loadContactInfo() {
        tvContactName.setText(contactDetailInfo[0]);
        tvPhoneNumber.setText(contactDetailInfo[2]);
        tvEmail.setText(contactDetailInfo[3]);
        tvOfficeLocation.setText(contactDetailInfo[4]);
        tvPosition.setText(contactDetailInfo[5]);
//        ivAvatar.setImageDrawable(contactDetailInfo[4]);
    }

    @Subscribe
    public void onConnectivityChanged(ConnectivityChanged event)
    {
        Log.e(Constants.TAG, "ContactDetailsPlusActivity.onConnectivityChanged: "
                + event.getConnectivityStatus().toString());
        if(event.getConnectivityStatus()!= ConnectivityStatus.MOBILE_CONNECTED &&
                event.getConnectivityStatus()!=ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET)
            lay_no_connection.setVisibility(View.VISIBLE);
        else
            lay_no_connection.setVisibility(View.GONE);
    }
}
