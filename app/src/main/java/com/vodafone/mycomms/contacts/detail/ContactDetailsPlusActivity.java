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
import com.vodafone.mycomms.chatgroup.GroupChatActivity;
import com.vodafone.mycomms.contacts.connection.RecentContactController;
import com.vodafone.mycomms.custom.CircleImageView;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class ContactDetailsPlusActivity extends ToolbarActivity {

    //Contact Detail
    private TextView tvContactName;
    private TextView tvPhoneNumber;
    private TextView tvEmail;
    private TextView tvOfficeLocation;
    private TextView tvPosition;
    private TextView txtAvatar;
    private CircleImageView ivAvatar;

    //Buttons
    private ImageView btnChat;
    private ImageView btnEmail;
    private ImageView btnPhone;

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
        initButtonListeners();
    }

    private void initLayoutObjects() {
        tvContactName = (TextView) findViewById(R.id.contact_contact_name);
        tvPhoneNumber = (TextView) findViewById(R.id.contact_phone_number);
        tvEmail = (TextView) findViewById(R.id.contact_email);
        tvOfficeLocation = (TextView)findViewById(R.id.contact_office_location);
        ivAvatar = (CircleImageView)findViewById(R.id.avatar);
        tvPosition = (TextView) findViewById(R.id.contact_position);
        txtAvatar = (TextView) findViewById(R.id.avatarText);

        btnChat = (ImageView)findViewById(R.id.btn_prof_chat);
        btnEmail = (ImageView)findViewById(R.id.btn_prof_email);
        btnPhone = (ImageView)findViewById(R.id.btn_prof_phone);
    }

    private String[] getContactInformation() {
        Intent intent = getIntent();
        return intent.getExtras().getStringArray(Constants.CONTACT_DETAIL_INFO);
    }

    private void loadContactInfo() {
        String fullName;
        if (null != contactDetailInfo[0]) //First Name
            fullName = contactDetailInfo[0];
        else
            fullName = "";
        if(null != contactDetailInfo[1]) //Last Name
            fullName = contactDetailInfo[0] + " " + contactDetailInfo[1];
        tvContactName.setText(fullName);

        if(null != contactDetailInfo[2] && contactDetailInfo[2].length() > 0) //Phones
        {
            if(null != Utils.getElementFromJsonArrayString(contactDetailInfo[2], Constants.CONTACT_PHONE))
                tvPhoneNumber.setText(Utils.getElementFromJsonArrayString(contactDetailInfo[2], Constants.CONTACT_PHONE));
            else
                tvPhoneNumber.setText("");
        }
        else
            tvPhoneNumber.setText("");

        if(null != contactDetailInfo[3] && contactDetailInfo[3].length() > 0) //Emails
        {
            if(null != Utils.getElementFromJsonArrayString(contactDetailInfo[3], Constants.CONTACT_EMAIL))
                tvEmail.setText(Utils.getElementFromJsonArrayString(contactDetailInfo[3], Constants.CONTACT_EMAIL));
            else
                tvEmail.setText("");
        }
        else
            tvEmail.setText("");

        tvOfficeLocation.setText(contactDetailInfo[4]);
        tvPosition.setText(contactDetailInfo[5]);

        loadContactAvatar();

    }

    private void loadContactAvatar() {
        //TODO: Check if checking of salesforce and local is necessary
        String test[] = contactDetailInfo;
        Utils.loadContactAvatar
        (
            contactDetailInfo[0] //FirstName
            , contactDetailInfo[1] //LastName
            , ivAvatar
            , txtAvatar
            , Utils.getAvatarURL
                    (
                            contactDetailInfo[7] //Platform
                            , null
                            , contactDetailInfo[6] //Avatar
                    )
        );
    }

    private void initButtonListeners() {
        String profileId = Utils.getProfileId(this);
        final String platform = contactDetailInfo[7];
        final String contactId = contactDetailInfo[8];
        final RecentContactController recentContactController = new RecentContactController(this, profileId);

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String strPhones = contactDetailInfo[2];

                    if (strPhones != null)
                    {

                        if(platform.equals(Constants.PLATFORM_LOCAL))
                        {
                            String sms;
                            //TODO: Implement multiple phone choice
//                            JSONArray jPhones = new JSONArray(strPhones);
                            JSONObject jPhones = new JSONObject(strPhones);
                            sms = (String)jPhones.get(Constants
                                    .CONTACT_PHONE);
                            Utils.launchSms(sms, ContactDetailsPlusActivity.this);
                        }
                        else
                        {
                            Intent in = new Intent(ContactDetailsPlusActivity.this, GroupChatActivity.class);
                            in.putExtra(Constants.CHAT_FIELD_CONTACT_ID, contactDetailInfo[8]);
                            in.putExtra(Constants.CHAT_PREVIOUS_VIEW, Constants.CHAT_VIEW_CONTACT_DETAIL);
                            startActivity(in);
                        }
                        recentContactController.insertRecent(contactId, Constants.CONTACTS_ACTION_SMS);
                    }
                } catch (Exception ex) {
                    Log.e(Constants.TAG, "ContactDetailsPlusActivity.onClick: ", ex);
                }
            }
        });

        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String strEmails = contactDetailInfo[3];

                    if (strEmails != null)
                    {
                        String email = strEmails;
                        if(!platform.equals(Constants.PLATFORM_LOCAL))
                        {
                            JSONArray jPhones = new JSONArray(strEmails);
                            email = (String) ((JSONObject) jPhones.get(0)).get(Constants.CONTACT_EMAIL);
                        }

                        Utils.launchEmail(email, ContactDetailsPlusActivity.this);
                        recentContactController.insertRecent(contactId, Constants.CONTACTS_ACTION_EMAIL);
                    }
                } catch (Exception ex) {
                    Log.e(Constants.TAG, "ContactDetailsPlusActivity.onClick: ", ex);
                }
            }
        });

        btnPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String strPhones = contactDetailInfo[2];

                    if (strPhones != null)
                    {
                        String phone = strPhones;
                        if(!platform.equals(Constants.PLATFORM_LOCAL))
                        {
                            JSONArray jPhones = new JSONArray(strPhones);
                            phone = (String)((JSONObject)jPhones.get(0)).get(Constants
                                    .CONTACT_PHONE);
                        }

                        Utils.launchCall(phone, ContactDetailsPlusActivity.this);
                        recentContactController.insertRecent(contactId, Constants.CONTACTS_ACTION_CALL);
                    }
                } catch (Exception ex) {
                    Log.e(Constants.TAG, "ContactDetailsPlusActivity.onClick: ", ex);
                }
            }
        });
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
