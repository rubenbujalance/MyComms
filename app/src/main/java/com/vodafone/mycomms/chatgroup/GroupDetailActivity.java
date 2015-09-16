package com.vodafone.mycomms.chatgroup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chatgroup.view.GroupDetailRecyclerItemClickListener;
import com.vodafone.mycomms.chatgroup.view.GroupMembersViewAdapter;
import com.vodafone.mycomms.contacts.detail.ContactDetailMainActivity;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.main.SplashScreenActivity;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.util.UncaughtExceptionHandlerController;
import com.vodafone.mycomms.util.Utils;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import io.realm.Realm;
import model.Contact;
import model.GroupChat;

public class GroupDetailActivity extends ToolbarActivity implements Serializable
{
    private String LOG_TAG = GroupDetailActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private GroupMembersViewAdapter mGroupMembersViewAdapter;

    private model.UserProfile _profile;
    private String _profile_id;

    private RealmChatTransactions chatTransactions;
    private RealmContactTransactions contactTransactions;

    private ArrayList<String> contactIds;
    private ArrayList<String> groupChatOwnerIds;
    private ArrayList<Contact> contactList;
    private RealmGroupChatTransactions mGroupChatTransactions;

    private GroupChat _groupChat;

    private ImageView top_left_avatar, top_right_avatar, bottom_left_avatar, bottom_right_avatar
            ,bottom_right_chat_availability, bottom_left_chat_availability, top_right_chat_availability, top_left_chat_availability;
    private TextView top_left_avatar_text, top_right_avatar_text, bottom_left_avatar_text, bottom_right_avatar_text
            ,group_names, group_n_components;
    private LinearLayout lay_right_top_avatar_to_hide, lay_bottom_to_hide, lay_top_left_avatar;
    private LinearLayout lay_no_connection;
    private LinearLayout lay_add_contact, lay_phone;
    private ImageView img_sun;

    private Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
        activateToolbar();

        //Register Otto bus to listen to events
        BusProvider.getInstance().register(this);

        Thread.setDefaultUncaughtExceptionHandler
                (
                        new UncaughtExceptionHandlerController(this, SplashScreenActivity.class)
                );

        this.mRealm = Realm.getDefaultInstance();

        lay_no_connection = (LinearLayout) findViewById(R.id.no_connection_layout);
        if(Utils.isConnected(GroupDetailActivity.this))
            lay_no_connection.setVisibility(View.GONE);
        else
            lay_no_connection.setVisibility(View.VISIBLE);

        SharedPreferences sp = getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

        _profile_id = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");

        contactTransactions = new RealmContactTransactions(_profile_id);
        chatTransactions = new RealmChatTransactions(this);
        mGroupChatTransactions = new RealmGroupChatTransactions(this, _profile_id);
        _profile = RealmContactTransactions.getUserProfile(this.mRealm, _profile_id);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(false);
        mRecyclerView.setLayoutManager(layoutManager);

        //Load chat from db
        loadExtras();
        loadContactsFromIds();
        loadTheRestOfTheComponents();

        mRecyclerView.addOnItemTouchListener(new GroupDetailRecyclerItemClickListener(GroupDetailActivity.this,
                mRecyclerView, new GroupDetailRecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent in = new Intent(GroupDetailActivity.this, ContactDetailMainActivity.class);
                ((MycommsApp) getApplication()).contactViewOrigin = Constants.CONTACTS_ALL;
                in.putExtra(Constants.CONTACT_CONTACT_ID, contactList.get(position).getContactId());
                startActivity(in);
            }
        }));

        refreshAdapter();
    }

    public void setHeaderAvatar()
    {
        if(null == _groupChat.getAvatar() || _groupChat.getAvatar().length() == 0)
        {
            ArrayList<ImageView> images = new ArrayList<>();
            images.add(top_left_avatar);
            images.add(bottom_left_avatar);
            images.add(bottom_right_avatar);

            final ArrayList<TextView> texts = new ArrayList<>();
            texts.add(top_left_avatar_text);
            texts.add(bottom_left_avatar_text);
            texts.add(bottom_right_avatar_text);

            if (null != contactIds && contactIds.size() > 3)
            {
                lay_right_top_avatar_to_hide.setVisibility(View.VISIBLE);
                images.add(top_right_avatar);
                texts.add(top_right_avatar_text);
            }

            int i = 0;
            boolean profileInside = false;
            String groupNames = "";
            String groupNComponents = contactList.size() + " people in group"; //TODO: Hardcode
            for(Contact contact : contactList)
            {
                if(i>3) break;

                final ImageView image = images.get(i);
                final TextView text = texts.get(i);
                text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

                //Image avatar
                if(null != contact.getFirstName() && contact.getFirstName().length() > 0) {
                    if (contact.getContactId().equals(_profile_id))
                        profileInside = true;
                    else
                        groupNames = contact.getFirstName() + ", " + groupNames;
                }
                Utils.loadContactAvatar
                        (
                                contact.getFirstName()
                                , contact.getLastName()
                                , image
                                , text
                                , contact.getAvatar()
                        );

                i++;
            }
            groupNames = groupNames.substring(0, groupNames.length()-2);
            if (profileInside)
                groupNames = groupNames + ", you...";
            group_names.setText(groupNames);
            group_n_components.setText(groupNComponents);
        }
        //Here we should load avatar provided by URL for whole group
        //by now we just the same as in previous If case
        else
        {
            ArrayList<ImageView> images = new ArrayList<>();
            images.add(top_left_avatar);
            images.add(bottom_left_avatar);
            images.add(bottom_right_avatar);

            final ArrayList<TextView> texts = new ArrayList<>();
            texts.add(top_left_avatar_text);
            texts.add(bottom_left_avatar_text);
            texts.add(bottom_right_avatar_text);

            if (null != contactIds && contactIds.size() > 3)
            {
                lay_right_top_avatar_to_hide.setVisibility(View.VISIBLE);
                images.add(top_right_avatar);
                texts.add(top_right_avatar_text);
            }

            int i = 0;
            boolean profileInside = false;
            String groupNames = "";
            String groupNComponents = contactList.size() + " people in group"; //TODO: Hardcode
            for(Contact contact : contactList)
            {
                if(i>3) break;

                final ImageView image = images.get(i);
                final TextView text = texts.get(i);
                text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

                //Image avatar
                if(null != contact.getFirstName() && contact.getFirstName().length() > 0) {
                    if (contact.getContactId().equals(_profile_id))
                        profileInside = true;
                    else
                        groupNames = contact.getFirstName() + ", " + groupNames;
                }
                Utils.loadContactAvatar
                        (
                                contact.getFirstName()
                                , contact.getLastName()
                                , image
                                , text
                                , contact.getAvatar()
                        );

                i++;
            }
            groupNames = groupNames.substring(0, groupNames.length()-2);
            if (profileInside)
                groupNames = groupNames + ", you...";
            group_names.setText(groupNames);
            group_n_components.setText(groupNComponents);
        }
    }

    private void startGroupChatListActivity()
    {
        Intent in = new Intent(GroupDetailActivity.this, GroupChatListActivity.class);
        in.putExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY, LOG_TAG);
        in.putExtra(Constants.GROUP_CHAT_ID, _groupChat.getId());
        startActivity(in);

        finish();
    }

    private void loadExtras()
    {
        Intent in = getIntent();

        _groupChat = mGroupChatTransactions.getGroupChatById(
                in.getStringExtra(Constants.GROUP_CHAT_ID), mRealm);
        loadContactIds();
        loadGroupChatOwnerIds();
    }

    private void loadContactIds()
    {
        String[] ids = _groupChat.getMembers().split("@");
        contactIds = new ArrayList<>();
        Collections.addAll(contactIds, ids);
    }

    private void loadGroupChatOwnerIds()
    {
        String ids[] = _groupChat.getOwners().split("@");
        groupChatOwnerIds = new ArrayList<>();
        Collections.addAll(groupChatOwnerIds, ids);
    }

    private void loadContactsFromIds()
    {
        contactList = new ArrayList<>();

        Contact userContact = new Contact();
        userContact.setAvatar(_profile.getAvatar());
        userContact.setFirstName(_profile.getFirstName());
        userContact.setLastName(_profile.getLastName());
        userContact.setContactId(_profile.getId());

        Contact contact;

        for(String id : contactIds)
        {
            if(id.compareTo(_profile_id)!=0)
                contact = contactTransactions.getContactById(id, mRealm);
            else contact = userContact;

            if(contact != null)
                contactList.add(contact);
        }
    }

    private void refreshAdapter()
    {
        mGroupMembersViewAdapter = new GroupMembersViewAdapter(GroupDetailActivity.this, contactList,
                _profile);
        mRecyclerView.setAdapter(mGroupMembersViewAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        XMPPTransactions.checkAndReconnectXMPP(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.mRealm.close();
    }

    private void loadTheRestOfTheComponents()
    {
        img_sun = (ImageView) findViewById(R.id.img_sun);
            img_sun.setVisibility(View.GONE);



        top_left_avatar = (ImageView) findViewById(R.id.top_left_avatar);
        top_right_avatar = (ImageView) findViewById(R.id.top_right_avatar);
        bottom_left_avatar = (ImageView) findViewById(R.id.bottom_left_avatar);
        bottom_right_avatar = (ImageView) findViewById(R.id.bottom_right_avatar);
        top_left_avatar_text = (TextView) findViewById(R.id.top_left_avatar_text);
        top_right_avatar_text = (TextView) findViewById(R.id.top_right_avatar_text);
        bottom_left_avatar_text = (TextView) findViewById(R.id.bottom_left_avatar_text);
        bottom_right_avatar_text = (TextView) findViewById(R.id.bottom_right_avatar_text);
        lay_right_top_avatar_to_hide = (LinearLayout) findViewById(R.id.lay_top_right_image_hide);
        lay_right_top_avatar_to_hide.setVisibility(View.GONE);
        lay_bottom_to_hide = (LinearLayout) findViewById(R.id.lay_bottom_both_image_hide);
        lay_bottom_to_hide.setVisibility(View.VISIBLE);
        lay_top_left_avatar = (LinearLayout) findViewById(R.id.lay_top_left_image);
        lay_phone = (LinearLayout) findViewById(R.id.lay_phone);
        this.lay_phone.setVisibility(View.GONE);

        bottom_right_chat_availability = (ImageView) findViewById(R.id.bottom_right_chat_availability);
        bottom_left_chat_availability = (ImageView) findViewById(R.id.bottom_left_chat_availability);
        top_right_chat_availability = (ImageView) findViewById(R.id.top_right_chat_availability);
        top_left_chat_availability = (ImageView) findViewById(R.id.top_left_chat_availability);

        bottom_right_chat_availability.setVisibility(View.VISIBLE);
        bottom_left_chat_availability.setVisibility(View.VISIBLE);
        top_right_chat_availability.setVisibility(View.VISIBLE);
        top_left_chat_availability.setVisibility(View.VISIBLE);

        group_names = (TextView) findViewById(R.id.group_names);
        group_n_components = (TextView) findViewById(R.id.group_n_components);

        lay_add_contact = (LinearLayout) findViewById(R.id.lay_add_member);

        if(groupChatOwnerIds.contains(_profile_id))
            lay_add_contact.setVisibility(View.VISIBLE);
        else
            lay_add_contact.setVisibility(View.INVISIBLE);

        lay_add_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGroupChatListActivity();
            }
        });

        if(contactIds==null || contactIds.size()==0) {
            Crashlytics.logException(new Exception("GroupDetailActivity.java > " +
                    "loadTheRestOfTheComponents: Error getting contact ids"));
            Toast.makeText(this,
                    getString(R.string.error_reading_data_from_server),Toast.LENGTH_LONG).show();
            finish(); //Prevent from errors
        }

        ImageView backButton = (ImageView) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        LinearLayout backArea = (LinearLayout) findViewById(R.id.back_area);
        backArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Set avatar
        setHeaderAvatar();
    }

    @Subscribe
    public void onConnectivityChanged(ConnectivityChanged event)
    {
        Log.i(Constants.TAG, "GroupDetailActivity.onConnectivityChanged: "
                + event.getConnectivityStatus().toString());

        if(event.getConnectivityStatus()!= ConnectivityStatus.MOBILE_CONNECTED &&
                event.getConnectivityStatus()!=ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET)
        {
            lay_no_connection.setVisibility(View.VISIBLE);
        }

        else
        {
            lay_no_connection.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        MycommsApp.activityStarted();
    }

    @Override
    public void onStop()
    {
        MycommsApp.activityStopped();
        super.onStop();
    }
}