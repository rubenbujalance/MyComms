package com.vodafone.mycomms.chatgroup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chat.ChatRecyclerViewAdapter;
import com.vodafone.mycomms.contacts.connection.IRecentContactConnectionCallback;
import com.vodafone.mycomms.contacts.connection.RecentContactController;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import io.realm.Realm;
import model.ChatMessage;
import model.Contact;
import model.GroupChat;

/**
 * Created by str_oan on 29/06/2015.
 */
public class GroupChatActivity extends ToolbarActivity implements
        IRecentContactConnectionCallback, Serializable
{

    private String LOG_TAG = GroupChatActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private ChatRecyclerViewAdapter mChatRecyclerViewAdapter;
    private EditText etChatTextBox;
    private TextView tvSendChat;
    private ImageView sendFileImage;
    private ImageView imgModifyGroupChat;

    private ArrayList<ChatMessage> _chatList = new ArrayList<>();
    private model.UserProfile _profile;
    private String _profile_id;

    private Realm mRealm;
    private RealmChatTransactions chatTransactions;
    private RealmContactTransactions contactTransactions;
    private RecentContactController mRecentContactController;

    private ArrayList<String> contactIds;
    private ArrayList<Contact> contactList;
    private String composedContactId = null;
    private RealmGroupChatTransactions mGroupChatTransactions;
    private GroupChat groupChat;
    private SharedPreferences sp;
    private String previousActivity;
    private boolean isGroupChatMode;

    private ImageView top_left_avatar, top_right_avatar, bottom_left_avatar, bottom_right_avatar;
    private TextView top_left_avatar_text, top_right_avatar_text, bottom_left_avatar_text, bottom_right_avatar_text;
    private LinearLayout lay_right_top_avatar_to_hide, lay_bottom_to_hide, lay_top_left_avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_main);
        activateToolbar();
//        setToolbarBackground(R.drawable.toolbar_header);

        //Register Otto bus to listen to events
        BusProvider.getInstance().register(this);

        sp = getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

        if(sp==null)
        {
            Log.e(Constants.TAG, "ChatMainActivity.onCreate: error loading Shared Preferences");
            finish();
        }
        mRealm = Realm.getInstance(this);

        _profile_id = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");

        contactTransactions = new RealmContactTransactions(mRealm,_profile_id);
        chatTransactions = new RealmChatTransactions(mRealm, this);


        mGroupChatTransactions = new RealmGroupChatTransactions
                (
                        mRealm
                        , this
                        , _profile_id
                );


        if(_profile_id == null)
        {
            Log.e(Constants.TAG, "ChatMainActivity.onCreate: profile_id not found in Shared Preferences");
            finish();
        }

        _profile = contactTransactions.getUserProfile(_profile_id);
        this.mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        this.mRecentContactController = new RecentContactController(this,mRealm,_profile_id);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);

        //Load chat from db
        loadExtras();
        loadContactsFromIds();
        loadTheRestOfTheComponents();

        String action = Constants.CONTACTS_ACTION_SMS;
        String id = groupChat.getId();
        mRecentContactController.insertRecentOKHttp(id, action);

    }

    public void setGroupChatAvatar()
    {
        if(this.isGroupChatMode)
        {
            if(null == this.groupChat.getAvatar() || this.groupChat.getAvatar().length() == 0)
            {
                ArrayList<ImageView> images = new ArrayList<>();
                images.add(top_left_avatar);
                images.add(bottom_left_avatar);
                images.add(bottom_right_avatar);

                ArrayList<TextView> texts = new ArrayList<>();
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
                for(Contact contact : contactList)
                {
                    if(i>3) break;

                    File avatarFile = new File(getFilesDir(), Constants.CONTACT_AVATAR_DIR +
                            "avatar_"+contact.getContactId()+".jpg");

                    if (contact.getAvatar()!=null &&
                            contact.getAvatar().length()>0 &&
                            contact.getAvatar().compareTo("")!=0 &&
                            avatarFile.exists())
                    {

                        Picasso.with(this)
                                .load(avatarFile)
                                .fit().centerCrop()
                                .into(images.get(i));

                    } else{
                        String initials = "";
                        if(null != contact.getFirstName() && contact.getFirstName().length() > 0)
                        {
                            initials = contact.getFirstName().substring(0,1);

                            if(null != contact.getLastName() && contact.getLastName().length() > 0)
                            {
                                initials = initials + contact.getLastName().substring(0,1);
                            }
                        }
                        images.get(i).setImageResource(R.color.grey_middle);
                        texts.get(i).setText(initials);
                    }
                    i++;
                }
            }
        }
        else
        {
            lay_right_top_avatar_to_hide.setVisibility(View.GONE);
            lay_bottom_to_hide.setVisibility(View.GONE);
            imgModifyGroupChat.setVisibility(View.GONE);
            lay_top_left_avatar.setLayoutParams
                (
                        new LinearLayout.LayoutParams
                                (
                                        LinearLayout.LayoutParams.MATCH_PARENT
                                        , LinearLayout.LayoutParams.MATCH_PARENT
                                )
                );

            Contact contact = contactList.get(0);
            File avatarFile = new File(getFilesDir(), Constants.CONTACT_AVATAR_DIR +
                    "avatar_"+contact.getContactId()+".jpg");

            if (contact.getAvatar()!=null &&
                    contact.getAvatar().length()>0 &&
                    contact.getAvatar().compareTo("")!=0 &&
                    avatarFile.exists())
            {

                Picasso.with(this)
                        .load(avatarFile)
                        .fit().centerCrop()
                        .into(top_left_avatar);
            }
            else
            {
                String initials = "";
                if(null != contact.getFirstName() && contact.getFirstName().length() > 0)
                {
                    initials = contact.getFirstName().substring(0,1);

                    if(null != contact.getLastName() && contact.getLastName().length() > 0)
                    {
                        initials = initials + contact.getLastName().substring(0,1);
                    }
                }
                top_left_avatar.setImageResource(R.color.grey_middle);
                top_left_avatar_text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                top_left_avatar_text.setText(initials);
            }
        }
    }

    private void startGroupChatListActivity()
    {
        Intent in = new Intent(GroupChatActivity.this, GroupChatListActivity.class);
        in.putExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY, LOG_TAG);
        in.putExtra(Constants.GROUP_CHAT_ID, groupChat.getId());
        startActivity(in);

        this.finish();
    }

    private void loadExtras()
    {
        Intent in = getIntent();

        this.previousActivity = in.getStringExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY);
        this.isGroupChatMode = in.getBooleanExtra(Constants.IS_GROUP_CHAT, false);
        if(isGroupChatMode)
        {
            this.groupChat = mGroupChatTransactions.getGroupChatById(in.getStringExtra(Constants.GROUP_CHAT_ID));
            loadContactIds();
        }
        else
        {
            this.contactIds = new ArrayList<>();
            this.contactIds.add(in.getStringExtra(Constants.CHAT_FIELD_CONTACT_ID));
        }
    }

    private void loadContactIds()
    {
        String[] ids = groupChat.getMembers().split("@");
        contactIds = new ArrayList<>();
        Collections.addAll(contactIds, ids);
    }

    private void loadContactsFromIds()
    {
        contactList = new ArrayList<>();
        if(isGroupChatMode)
        {
            Contact contact = new Contact();
            contact.setAvatar(_profile.getAvatar());
            contact.setFirstName(_profile.getFirstName());
            contact.setLastName(_profile.getLastName());
            contact.setContactId(_profile.getId());
            contactList.add(contact);
            for(String id : contactIds)
            {
                if(!id.equals(_profile_id))
                {
                    contact = contactTransactions.getContactById(id);
                    contactList.add(contact);
                }
            }
        }
        else
        {
            Contact contact = contactTransactions.getContactById(contactIds.get(0));
            contactList.add(contact);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_chat_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRealm != null){
            mRealm.close();
        }
    }

    @Override
    public void onConnectionNotAvailable() {
        Log.e(Constants.TAG, "ChatMainActivity.onConnectionNotAvailable: ");

        tvSendChat.setEnabled(false);
        tvSendChat.setTextColor(Color.GRAY);
    }

    private void loadTheRestOfTheComponents()
    {
        imgModifyGroupChat = (ImageView) findViewById(R.id.img_modify_group_chat);

        if(isGroupChatMode)
        {
            if(groupChat.getCreatorId().equals(_profile_id))
                imgModifyGroupChat.setVisibility(View.VISIBLE);
            else
                imgModifyGroupChat.setVisibility(View.GONE);
        }

        etChatTextBox = (EditText) findViewById(R.id.chat_text_box);
        tvSendChat = (TextView) findViewById(R.id.chat_send);
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

        sendFileImage = (ImageView) findViewById(R.id.send_image);

        if(contactIds==null || contactIds.size()==0) finish(); //Prevent from errors


        //This prevents the view focusing on the edit text and opening the keyboard
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        ImageView backButton = (ImageView) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Set avatar

        setGroupChatAvatar();

        imgModifyGroupChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                startGroupChatListActivity();
            }
        });
    }
}