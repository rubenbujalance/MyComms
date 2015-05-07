package com.vodafone.mycomms.chat;

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

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;

import java.util.ArrayList;

public class ChatMainActivity extends ToolbarActivity {

    private String LOG_TAG = ChatMainActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private ChatRecyclerViewAdapter mChatRecyclerViewAdapter;
    private EditText chatTextBox;
    private String chatText = "";
    private ArrayList<ChatListItem> chatList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //This prevents the view focusing on the edit text and opening the keyboard
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.activity_chat_main);
        activateToolbar();
        setToolbarBackground(R.drawable.toolbar_header);
        setChatListeners(this);

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

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mChatRecyclerViewAdapter = new ChatRecyclerViewAdapter(ChatMainActivity.this, null);
        mRecyclerView.setAdapter(mChatRecyclerViewAdapter);

        chatTextBox = (EditText) findViewById(R.id.chat_text_box);
        chatTextBox.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                try {
                    chatText = cs.toString();
                    Log.i(LOG_TAG,"Text Changed is: " + chatText);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "onTextChanged error: " + e.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

        TextView sendChat = (TextView) findViewById(R.id.chat_send);
        sendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG,"Sending text " + chatText);
                sendText();
            }
        });
    }

    private void sendText() {
        ChatListItem chatListItem = new ChatListItem(chatText, Constants.RIGHT_CHAT);
        chatList.add(chatListItem);
        mChatRecyclerViewAdapter = new ChatRecyclerViewAdapter(ChatMainActivity.this, chatList);
        mRecyclerView.setAdapter(mChatRecyclerViewAdapter);
        chatTextBox.setText("");
        Log.i(LOG_TAG, "Adapter Set");

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
}
