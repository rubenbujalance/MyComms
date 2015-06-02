package com.vodafone.mycomms.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;

import java.util.ArrayList;

import io.realm.Realm;
import model.Chat;
import model.ChatMessage;
import model.Contact;

public class ChatMainActivity extends ToolbarActivity {

    private String LOG_TAG = ChatMainActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private ChatRecyclerViewAdapter mChatRecyclerViewAdapter;
    private EditText etChatTextBox;
//    private String _chatText = "";
    private ArrayList<ChatMessage> _chatList = new ArrayList<>();
    private Chat _chat;
    private Contact _contact;
    private Contact _profile;

    private Realm mRealm;
    private RealmChatTransactions chatTransactions;
    private RealmContactTransactions contactTransactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_main);
        activateToolbar();
        setToolbarBackground(R.drawable.toolbar_header);
        setChatListeners(this);

        mRealm = Realm.getInstance(this);
        chatTransactions = new RealmChatTransactions(mRealm, this);
        contactTransactions = new RealmContactTransactions(mRealm);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        refreshAdapter();

        etChatTextBox = (EditText) findViewById(R.id.chat_text_box);

        //Load chat from db
        Intent in = getIntent();
        String contact_id = in.getStringExtra(Constants.CHAT_FIELD_CONTACT_ID);

        if(contact_id==null || contact_id.length()==0) finish(); //Prevent from errors

        //Contact and profile
        _contact = contactTransactions.getContactById(contact_id);

        SharedPreferences sp = getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

        if(sp==null)
        {
            Log.e(Constants.TAG, "ChatMainActivity.onCreate: profile_id not found in Shared Preferences");
            finish();
        }

        String _profile_id = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);
        //TODO RBM - Remove after testing
        _profile_id = "mc_555a0792121ef1695cc7c1c3";
        _profile = contactTransactions.getContactById(_profile_id);

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
        /*Toolbar mToolbar = (Toolbar) findViewById(R.id.app_bar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/

//        etChatTextBox.addTextChangedListener(new TextWatcher() {
//
//            @Override
//            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
//                try {
//                    _chatText = cs.toString();
//                } catch (Exception e) {
//                    Log.e(LOG_TAG, "onTextChanged error: " + e.toString());
//                }
//            }
//            @Override
//            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {}
//            @Override
//            public void afterTextChanged(Editable arg0) {}
//        });

        TextView sendChat = (TextView) findViewById(R.id.chat_send);
        sendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "Sending text " + etChatTextBox.getText().toString());
                sendText();
            }
        });

        final Context mContext = this;
        ImageView clearText = (ImageView) findViewById(R.id.send_image);
        clearText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG,"sendText()");
                chatTransactions.deleteAllChatMessages(_chat.getContact_id());
                refreshAdapter();
            }
        });
    }

    private void sendText()
    {
        ChatMessage chatMsg = chatTransactions.newChatMessageInstance(
                _chat.getContact_id(), Constants.CHAT_MESSAGE_DIRECTION_SENT,
                Constants.CHAT_MESSAGE_TYPE_TEXT, etChatTextBox.getText().toString(), "");

        chatTransactions.insertChat(_chat);
        chatTransactions.insertChatMessage(chatMsg);

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
}
