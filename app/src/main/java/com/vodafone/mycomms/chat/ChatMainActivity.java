package com.vodafone.mycomms.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.contacts.connection.IRecentContactConnectionCallback;
import com.vodafone.mycomms.contacts.detail.RecentContactController;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ChatsReceivedEvent;
import com.vodafone.mycomms.events.RefreshChatListEvent;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

import java.io.File;
import java.util.ArrayList;

import io.realm.Realm;
import model.Chat;
import model.ChatMessage;
import model.Contact;

public class ChatMainActivity extends ToolbarActivity implements IRecentContactConnectionCallback {

    private String LOG_TAG = ChatMainActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private ChatRecyclerViewAdapter mChatRecyclerViewAdapter;
    private EditText etChatTextBox;
    private TextView tvSendChat;
    private ImageView ivAvatarImage;
    private TextView tvAvatarText;

    private ArrayList<ChatMessage> _chatList = new ArrayList<>();
    private Chat _chat;
    private Contact _contact;
    private Contact _profile;

    private String previousView;

    private Realm mRealm;
    private RealmChatTransactions chatTransactions;
    private RealmContactTransactions contactTransactions;
    private RecentContactController mRecentController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_main);
        activateToolbar();
        setToolbarBackground(R.drawable.toolbar_header);

        //Register Otto bus to listen to events
        BusProvider.getInstance().register(this);

        mRealm = Realm.getInstance(this);
        chatTransactions = new RealmChatTransactions(mRealm, this);
        contactTransactions = new RealmContactTransactions(mRealm);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        refreshAdapter();

        etChatTextBox = (EditText) findViewById(R.id.chat_text_box);
        tvSendChat = (TextView) findViewById(R.id.chat_send);
        ivAvatarImage = (ImageView) findViewById(R.id.companyLogo);
        tvAvatarText = (TextView) findViewById(R.id.avatarText);

        //Load chat from db
        Intent in = getIntent();
        String contact_id = in.getStringExtra(Constants.CHAT_FIELD_CONTACT_ID);
        previousView = in.getStringExtra(Constants.CHAT_PREVIOUS_VIEW);

        if(contact_id==null || contact_id.length()==0) finish(); //Prevent from errors

        //Contact and profile
        _contact = contactTransactions.getContactById(contact_id);

        SharedPreferences sp = getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

        if(sp==null)
        {
            Log.e(Constants.TAG, "ChatMainActivity.onCreate: error loading Shared Preferences");
            finish();
        }

        String _profile_id = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);
        _profile = contactTransactions.getContactById(_profile_id);

        if(_profile_id == null)
        {
            Log.e(Constants.TAG, "ChatMainActivity.onCreate: profile_id not found in Shared Preferences");
            finish();
        }

        //Chat listeners
        setChatListeners(this, _contact);

        //Load chat
        _chat = chatTransactions.getChatById(contact_id);

        //If there was no chat, create a new one, but not saved in db yet
        //If chat exists, load all messages
        if(_chat==null) _chat = chatTransactions.newChatInstance(contact_id);
        else loadMessagesArray();

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
        File avatarFile = new File(getFilesDir(), Constants.CONTACT_AVATAR_DIR + "avatar_"+_contact.getId()+".jpg");

        if (_contact.getAvatar()!=null &&
                _contact.getAvatar().length()>0 &&
                _contact.getAvatar().compareTo("")!=0 &&
                avatarFile.exists()) {

            tvAvatarText.setText(null);

            Picasso.with(this)
                    .load(avatarFile)
                    .into(ivAvatarImage);

        } else{
            String initials = _contact.getFirstName().substring(0,1) +
                    _contact.getLastName().substring(0,1);

            ivAvatarImage.setImageResource(R.color.grey_middle);
            tvAvatarText.setText(initials);
        }

        //Sent chat in grey by default
        tvSendChat.setTextColor(Color.GRAY);
        tvSendChat.setEnabled(false);

        etChatTextBox.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                if(cs!=null && cs.length()>0) checkXMPPConnection();
                else setSendEnabled(false);
            }
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {}
            @Override
            public void afterTextChanged(Editable arg0) {}
        });

        tvSendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "Sending text " + etChatTextBox.getText().toString());
                sendText();
            }
        });

//        ImageView clearText = (ImageView) findViewById(R.id.send_image);
//        clearText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i(LOG_TAG,"sendText()");
//                chatTransactions.deleteAllChatMessages(_chat.getContact_id());
//                refreshAdapter();
//            }
//        });
    }

    private void sendText()
    {
        String msg = etChatTextBox.getText().toString();
        if(!XMPPTransactions.sendText(_contact.getId(), msg))
            return;

        //Save to DB
        ChatMessage chatMsg = chatTransactions.newChatMessageInstance(
                _chat.getContact_id(), Constants.CHAT_MESSAGE_DIRECTION_SENT,
                Constants.CHAT_MESSAGE_TYPE_TEXT, msg, "");

        _chat = chatTransactions.updatedChatInstance(_chat, chatMsg);

        chatTransactions.insertChat(_chat);
        chatTransactions.insertChatMessage(chatMsg);

        RecentContactController mRecentContactController = new RecentContactController(this,mRealm);
        String action = Constants.CONTACTS_ACTION_SMS;
        mRecentContactController.insertRecent(_chat.getContact_id(), action);
        mRecentContactController.setConnectionCallback(this);

        //Refresh previous list view if necessary
        if (previousView.equals(Constants.CHAT_VIEW_CHAT_LIST)) {
            BusProvider.getInstance().post(new RefreshChatListEvent());
        } else if (previousView.equals(Constants.CHAT_VIEW_CONTACT_LIST)) {
            //Recent List is refreshed onConnectionComplete
        }

        _chatList.add(chatMsg);
        refreshAdapter();
        etChatTextBox.setText("");
    }

    private void loadMessagesArray()
    {
        _chatList = chatTransactions.getAllChatMessages(_chat.getContact_id());
        refreshAdapter();
    }

    private void refreshAdapter()
    {
        mChatRecyclerViewAdapter = new ChatRecyclerViewAdapter(ChatMainActivity.this, _chatList, _profile, _contact);
        mRecyclerView.setAdapter(mChatRecyclerViewAdapter);
    }

    private void checkXMPPConnection()
    {
        if(XMPPTransactions.getXmppConnection()!=null &&
                XMPPTransactions.getXmppConnection().isConnected())
            setSendEnabled(true);
        else
            setSendEnabled(false);
    }

    private void setSendEnabled(boolean enable)
    {
        if(!enable) {
            tvSendChat.setEnabled(false);
            tvSendChat.setTextColor(Color.GRAY);
        }
        else {
            tvSendChat.setEnabled(true);
            tvSendChat.setTextColor(Color.parseColor("#02B1FF"));
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
    protected void onResume() {
        super.onResume();

        checkXMPPConnection();
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

    @Subscribe
    public void onEventChatsReceived(ChatsReceivedEvent event){
        ChatMessage chatMsg = event.getMessage();
        if(chatMsg!=null)
        {
            _chatList.add(chatMsg);
            refreshAdapter();
        }
    }
}
