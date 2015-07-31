package com.vodafone.mycomms.chatgroup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Callback;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chat.ChatRecyclerViewAdapter;
import com.vodafone.mycomms.contacts.connection.RecentContactController;
import com.vodafone.mycomms.contacts.detail.ContactDetailMainActivity;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ChatsReceivedEvent;
import com.vodafone.mycomms.events.MessageSentEvent;
import com.vodafone.mycomms.events.MessageStatusChanged;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.settings.connection.FilePushToServerController;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.util.Utils;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import io.realm.Realm;
import model.Chat;
import model.ChatMessage;
import model.Contact;
import model.GroupChat;

/**
 * Created by str_oan on 29/06/2015.
 */
public class GroupChatActivity extends ToolbarActivity implements Serializable
{

    private String LOG_TAG = GroupChatActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private ChatRecyclerViewAdapter mChatRecyclerViewAdapter;
    private EditText etChatTextBox;
    private TextView tvSendChat;
    private ImageView imgModifyGroupChat;

    private ArrayList<ChatMessage> _chatList = new ArrayList<>();
    private model.UserProfile _profile;
    private String _profile_id;

    private RealmChatTransactions chatTransactions;
    private RealmContactTransactions contactTransactions;
    private RecentContactController mRecentContactController;

    private ArrayList<String> contactIds;
    private ArrayList<String> groupChatOwnerIds;
    private ArrayList<Contact> contactList;
    private RealmGroupChatTransactions mGroupChatTransactions;
    private SharedPreferences sp;
    private String previousActivity;

    private boolean isGroupChatMode;
    private Chat _chat;
    private GroupChat _groupChat;
    private String _contactId;
    private String _groupId;

    private String photoPath = null;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private File multiPartFile;
    private FilePushToServerController filePushToServerController;

    private ImageView sendFileImage;

    private ImageView top_left_avatar, top_right_avatar, bottom_left_avatar, bottom_right_avatar;
    private TextView top_left_avatar_text, top_right_avatar_text, bottom_left_avatar_text, bottom_right_avatar_text;
    private LinearLayout lay_right_top_avatar_to_hide, lay_bottom_to_hide, lay_top_left_avatar;
    private LinearLayout lay_no_connection;

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_main);
        activateToolbar();

        //Register Otto bus to listen to events
        BusProvider.getInstance().register(this);

        this.realm = Realm.getDefaultInstance();
        this.realm.setAutoRefresh(true);

        lay_no_connection = (LinearLayout) findViewById(R.id.no_connection_layout);
        if(APIWrapper.isConnected(GroupChatActivity.this))
            lay_no_connection.setVisibility(View.GONE);
        else
            lay_no_connection.setVisibility(View.VISIBLE);

        sp = getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

        _profile_id = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");

        contactTransactions = new RealmContactTransactions(_profile_id, GroupChatActivity.this);
        chatTransactions = new RealmChatTransactions(this);
        mGroupChatTransactions = new RealmGroupChatTransactions(this, _profile_id);
        _profile = contactTransactions.getUserProfile(realm);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecentContactController = new RecentContactController(this, _profile_id);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);

        //Load chat from db
        loadExtras();
        loadContactsFromIds();
        loadTheRestOfTheComponents();

        //Load all messages
        loadMessagesArray();

        refreshAdapter();

        //Sent chat in grey by default
        setSendEnabled(false);

        etChatTextBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                if (cs != null && cs.length() > 0) setSendEnabled(true);
                else setSendEnabled(false);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

        tvSendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "Sending text " + etChatTextBox.getText().toString());
                sendText();
            }
        });

        sendFileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                dispatchTakePictureIntent(getString(R.string.how_would_you_like_to_add_a_photo), null);
            }
        });

    }

    public void setHeaderAvatar()
    {
        if(this.isGroupChatMode)
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
                for(Contact contact : contactList)
                {
                    if(i>3) break;

                    final ImageView image = images.get(i);
                    final TextView text = texts.get(i);
                    text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

                    //Image avatar
                    String initials = "";
                    if(null != contact.getFirstName() && contact.getFirstName().length() > 0)
                    {
                        initials = contact.getFirstName().substring(0,1);

                        if(null != contact.getLastName() && contact.getLastName().length() > 0)
                        {
                            initials = initials + contact.getLastName().substring(0,1);
                        }

                    }

                    final String finalInitials = initials;

                    image.setImageResource(R.color.grey_middle);
                    text.setVisibility(View.VISIBLE);
                    text.setText(finalInitials);

                    if (contact.getAvatar()!=null &&
                            contact.getAvatar().length()>0)
                    {
                        MycommsApp.picasso
                                .load(contact.getAvatar())
                                .placeholder(R.color.grey_middle)
                                .noFade()
                                .fit().centerCrop()
                                .into(image, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        text.setVisibility(View.INVISIBLE);
                                    }

                                    @Override
                                    public void onError() {
                                        image.setImageResource(R.color.grey_middle);
                                        text.setVisibility(View.VISIBLE);
                                        text.setText(finalInitials);
                                    }
                                });
                    }
                    else
                    {
                        image.setImageResource(R.color.grey_middle);
                        text.setText(initials);
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
            //Image avatar
            String initials = "";
            if(null != contact.getFirstName() && contact.getFirstName().length() > 0)
            {
                initials = contact.getFirstName().substring(0,1);

                if(null != contact.getLastName() && contact.getLastName().length() > 0)
                {
                    initials = initials + contact.getLastName().substring(0,1);
                }

            }

            final String finalInitials = initials;

            top_left_avatar.setImageResource(R.color.grey_middle);
            top_left_avatar_text.setVisibility(View.VISIBLE);
            top_left_avatar_text.setText(finalInitials);
            top_left_avatar_text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);

            if (contact.getAvatar()!=null &&
                    contact.getAvatar().length()>0)
            {
                MycommsApp.picasso
                        .load(contact.getAvatar())
                        .placeholder(R.color.grey_middle)
                        .noFade()
                        .fit().centerCrop()
                        .into(top_left_avatar, new Callback() {
                            @Override
                            public void onSuccess() {
                                top_left_avatar_text.setVisibility(View.INVISIBLE);
                            }

                            @Override
                            public void onError() {
                                top_left_avatar.setImageResource(R.color.grey_middle);
                                top_left_avatar_text.setVisibility(View.VISIBLE);
                                top_left_avatar_text.setText(finalInitials);
                            }
                        });
            }
            else
            {
                top_left_avatar.setImageResource(R.color.grey_middle);
                top_left_avatar_text.setText(initials);
            }

            top_left_avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in = new Intent(GroupChatActivity.this, ContactDetailMainActivity.class);
                    in.putExtra(Constants.CONTACT_CONTACT_ID, _contactId);
                    in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(in);
                }
            });
        }
    }

    private void startGroupChatListActivity()
    {
        Intent in = new Intent(GroupChatActivity.this, GroupChatListActivity.class);
        in.putExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY, LOG_TAG);
        in.putExtra(Constants.GROUP_CHAT_ID, _groupChat.getId());
        startActivity(in);

        finish();
    }

    private void loadExtras()
    {
        Intent in = getIntent();

        this.previousActivity = in.getStringExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY);
        this.isGroupChatMode = in.getBooleanExtra(Constants.IS_GROUP_CHAT, false);

        if(isGroupChatMode)
        {
            _groupChat = mGroupChatTransactions.getGroupChatById(
                    in.getStringExtra(Constants.GROUP_CHAT_ID),realm);
            _groupId = _groupChat.getId();
            loadContactIds();
            loadGroupChatOwnerIds();
        }
        else
        {
            String contactId = in.getStringExtra(Constants.CHAT_FIELD_CONTACT_ID);
            _chat = chatTransactions.getChatByContactId(contactId, realm);
            if(_chat==null) _chat = chatTransactions.newChatInstance(contactId);
            _contactId = _chat.getContact_id();
            this.contactIds = new ArrayList<>();
            this.contactIds.add(contactId);
        }
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
                    contact = contactTransactions.getContactById(id, realm);
                    if(contact != null)
                        contactList.add(contact);
                }
            }
        }
        else
        {
            Contact contact = contactTransactions.getContactById(contactIds.get(0), realm);
            if(contact != null)
                contactList.add(contact);
        }
    }

    private void sendText()
    {
        String msg = etChatTextBox.getText().toString();
        ChatMessage chatMsg;
        //Save to DB
        if(isGroupChatMode) {
            chatMsg = mGroupChatTransactions.newGroupChatMessageInstance(_groupChat.getId(), "",
                    Constants.CHAT_MESSAGE_DIRECTION_SENT, Constants.CHAT_MESSAGE_TYPE_TEXT,
                    msg, "", Constants.CHAT_MESSAGE_STATUS_NOT_SENT,
                    Constants.CHAT_MESSAGE_NOT_READ);

            _groupChat = mGroupChatTransactions.updatedGroupChatInstance(_groupChat, chatMsg);

            mGroupChatTransactions.insertOrUpdateGroupChat(_groupChat, realm);
            mGroupChatTransactions.insertGroupChatMessage(_groupId, chatMsg, realm);
        }
        else {
            chatMsg = chatTransactions.newChatMessageInstance(
                    _chat.getContact_id(), Constants.CHAT_MESSAGE_DIRECTION_SENT,
                    Constants.CHAT_MESSAGE_TYPE_TEXT, msg, "",
                    Constants.CHAT_MESSAGE_STATUS_NOT_SENT, Constants.CHAT_MESSAGE_NOT_READ);

            _chat = chatTransactions.updatedChatInstance(_chat, chatMsg);

            chatTransactions.insertChat(_chat, realm);
            chatTransactions.insertChatMessage(chatMsg, realm);
        }

        //Send through XMPP
        String groupContactId;
        if(isGroupChatMode) groupContactId = _groupId;
        else groupContactId = _contactId;

        if(!XMPPTransactions.sendText(isGroupChatMode, groupContactId, chatMsg.getId(), msg))
            return;

        //Insert in recents
        RecentContactController recentContactController = new
                RecentContactController(this,_profile_id);

        if(isGroupChatMode)
            recentContactController.insertRecentOKHttp(_groupId, Constants.CONTACTS_ACTION_SMS);
        else recentContactController.insertRecent(_contactId, Constants.CONTACTS_ACTION_SMS);

        BusProvider.getInstance().post(new MessageSentEvent());

        _chatList.add(chatMsg);
        if(_chatList.size()>50) _chatList.remove(0);

        refreshAdapter();
        etChatTextBox.setText("");
    }

    private void imageSent(String imageUrl, Bitmap bitmap)
    {
        Log.i(Constants.TAG, "GroupChatActivity.imageSent: ");
        //Save to DB
        ChatMessage chatMsg;

        if(isGroupChatMode) {
            chatMsg = mGroupChatTransactions.newGroupChatMessageInstance(
                    _groupChat.getId(), "", Constants.CHAT_MESSAGE_DIRECTION_SENT,
                    Constants.CHAT_MESSAGE_TYPE_IMAGE,"", imageUrl,
                    Constants.CHAT_MESSAGE_STATUS_NOT_SENT, Constants.CHAT_MESSAGE_NOT_READ);

            _groupChat = mGroupChatTransactions.updatedGroupChatInstance(_groupChat, chatMsg);

            mGroupChatTransactions.insertOrUpdateGroupChat(_groupChat, realm);
            mGroupChatTransactions.insertGroupChatMessage(_groupId, chatMsg, realm);
        }
        else {
            chatMsg = chatTransactions.newChatMessageInstance(
                    _chat.getContact_id(), Constants.CHAT_MESSAGE_DIRECTION_SENT,
                    Constants.CHAT_MESSAGE_TYPE_IMAGE, "", imageUrl,
                    Constants.CHAT_MESSAGE_STATUS_NOT_SENT, Constants.CHAT_MESSAGE_NOT_READ);

            _chat = chatTransactions.updatedChatInstance(_chat, chatMsg);

            chatTransactions.insertChat(_chat, realm);
            chatTransactions.insertChatMessage(chatMsg, realm);
        }

        //Send through XMPP
        String groupContactId;
        if(isGroupChatMode) groupContactId = _groupId;
        else groupContactId = _contactId;

        XMPPTransactions.sendImage(isGroupChatMode, groupContactId, chatMsg.getId(), imageUrl);

        //Save and show
        String dirStr = getFilesDir() + Constants.CONTACT_CHAT_FILES;
        String fileStr = "file_" + chatMsg.getId() + ".jpg";

        try {
            File dirFile = new File(dirStr);
            if (!dirFile.exists()) dirFile.mkdirs();

            File file = new File(dirStr, fileStr);
            file.createNewFile();
            FileOutputStream outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outStream);
            outStream.flush();
            outStream.close();

        } catch (Exception e) {
            Log.e(Constants.TAG, "GroupChatActivity.imageSent: ",e);
            Crashlytics.logException(e);
        }

        //Insert in recents
        String action = Constants.CONTACTS_ACTION_SMS;
        mRecentContactController.insertRecent(groupContactId, action);

        //Notify app to refresh any view if necessary
        if (previousActivity != null && !previousActivity.equals("") && previousActivity.equals(Constants.CHAT_VIEW_CHAT_LIST)) {
            BusProvider.getInstance().post(new MessageSentEvent());
        } else if (previousActivity != null && !previousActivity.equals("") && previousActivity.equals(Constants.CHAT_VIEW_CONTACT_LIST)) {
            //Recent List is refreshed onConnectionComplete
        }

        _chatList.add(chatMsg);
        if(_chatList.size()>50) _chatList.remove(0);

        refreshAdapter();
    }

    private void loadMessagesArray()
    {
        if(isGroupChatMode && _groupChat!=null)
            _chatList = mGroupChatTransactions.getAllGroupChatMessages(_groupChat.getId(), realm);
        else if(!isGroupChatMode && _chat!=null)
            _chatList = chatTransactions.getAllChatMessages(_chat.getContact_id(), realm);

        refreshAdapter();
    }

    private void refreshAdapter()
    {
        mChatRecyclerViewAdapter = new ChatRecyclerViewAdapter(GroupChatActivity.this, _chatList,
                _profile, isGroupChatMode, realm);
        mRecyclerView.setAdapter(mChatRecyclerViewAdapter);
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
        XMPPTransactions.checkAndReconnectXMPP(getApplicationContext());

        if(etChatTextBox.getText().toString()!=null &&
                etChatTextBox.getText().toString().length()>0) setSendEnabled(true);
        else setSendEnabled(false);

        ArrayList<ChatMessage> messages;

        if(isGroupChatMode)
            messages = mGroupChatTransactions.getNotReadReceivedGroupChatMessages(_groupId, realm);
        else
            messages = chatTransactions.getNotReadReceivedContactChatMessages(_contactId, realm);

        if (messages != null && messages.size() > 0) {
            XMPPTransactions.sendReadIQReceivedMessagesList(isGroupChatMode, messages);
            if(isGroupChatMode)
                mGroupChatTransactions.setGroupChatAllReceivedMessagesAsRead(_groupId, realm);
            else chatTransactions.setContactAllChatMessagesReceivedAsRead(_contactId, realm);
        }
    }

    @Subscribe
    public void onEventChatsReceived(ChatsReceivedEvent event){
        Log.i(Constants.TAG, "GroupChatActivity.onEventChatsReceived: ");
        ChatMessage chatMsg = event.getMessage();
        if((isGroupChatMode && chatMsg!=null && chatMsg.getGroup_id().compareTo(_groupId)==0)
                || (!isGroupChatMode && chatMsg!=null && chatMsg.getContact_id().compareTo(_contactId)==0))
        {
            if(_chatList.size()==0 ||
                    (_chatList.get(_chatList.size()-1).getId().compareTo(chatMsg.getId())!=0)) {
                _chatList.add(chatMsg);
                if (_chatList.size() > 50) _chatList.remove(0);
                refreshAdapter();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.realm.close();
    }

    private void loadTheRestOfTheComponents()
    {
        imgModifyGroupChat = (ImageView) findViewById(R.id.img_modify_group_chat);

        if(isGroupChatMode)
        {
            if(groupChatOwnerIds.contains(_profile_id))
                imgModifyGroupChat.setVisibility(View.VISIBLE);
            else
                imgModifyGroupChat.setVisibility(View.GONE);
        }
        else
            imgModifyGroupChat.setVisibility(View.GONE);

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

        if(contactIds==null || contactIds.size()==0) {
            Crashlytics.logException(new Exception("GroupChatActivity.java > " +
                    "loadTheRestOfTheComponents: Error getting contact ids"));
            Toast.makeText(this,
                    getString(R.string.error_reading_data_from_server),Toast.LENGTH_LONG).show();
            finish(); //Prevent from errors
        }

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
        setHeaderAvatar();

        imgModifyGroupChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                startGroupChatListActivity();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        try
        {
            Log.i(Constants.TAG, "GroupChatActivity.onActivityResult: Image selected");

            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK)
                new sendFile().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            else if(requestCode == REQUEST_IMAGE_GALLERY && resultCode == Activity.RESULT_OK)
            {
                Uri selectedImage = data.getData();
                photoPath = Utils.getRealPathFromUri(selectedImage, this);
                new sendFile().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

        }
        catch (Exception e) {
            Log.e(Constants.TAG, "GroupChatActivity.onActivityResult: ",e);
        }
    }

    private void dispatchTakePictureIntent(String title, String subtitle)
    {

        //Build the alert dialog to let the user choose the origin of the picture

        AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
        builder.setTitle(title);

        if(subtitle != null) {
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.cv_title_subtitle, null);
            ((TextView) view.findViewById(R.id.tvTitle)).setText(title);
            ((TextView) view.findViewById(R.id.tvSubtitle)).setText(subtitle);
            builder.setCustomTitle(view);
        }
        else {
            builder.setTitle(title);
        }

        builder.setItems(R.array.add_photo_chooser, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent in;
                if(which == 0) {
                    in = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    in.putExtra(MediaStore.EXTRA_OUTPUT, setImageUri());
                    startActivityForResult(in, REQUEST_IMAGE_CAPTURE);
                }
                else if(which == 1) {
                    in = new Intent();
                    in.setType("image/*");
                    in.setAction(Intent.ACTION_PICK);

                    startActivityForResult(in, REQUEST_IMAGE_GALLERY);
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        builder.create();
        builder.show();
    }

    public Uri setImageUri()
    {
        // Store image in dcim
        File file = new File(Environment.getExternalStorageDirectory() + "/DCIM/", "image" + new
                Date().getTime() + ".png");
        Uri imgUri = Uri.fromFile(file);
        photoPath = file.getAbsolutePath();
        return imgUri;
    }

    public Bitmap decodeFile(String path)
    {
        try
        {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, o);
            return BitmapFactory.decodeFile(path);
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "GroupChatActivity.decodeFile: ",e);
            Crashlytics.logException(e);
        }
        return null;
    }

    private String getRealPathFromURI(Uri contentURI)
    {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null,
                null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    @SuppressLint("NewApi")
    public Uri transformUriAPI22(Uri selectedImage) {
        if (selectedImage != null && selectedImage.toString().length() > 0) {
            try {
                final String extractUriFrom = selectedImage.toString();
                String firstExtraction = extractUriFrom.contains("com.google.android.apps.photos.contentprovider") ? extractUriFrom.split("/1/")[1] : extractUriFrom;
                firstExtraction = firstExtraction.contains("/ACTUAL") ? firstExtraction.replace("/ACTUAL", "").toString() : firstExtraction;

                String secondExtraction = URLDecoder.decode(firstExtraction, "UTF-8");
                selectedImage = Uri.parse(secondExtraction);
            } catch (Exception e) {
                Log.e(Constants.TAG, "GroupChatActivity.transformUriAPI22: ",e);
            }
        }

        return selectedImage;
    }

    @SuppressLint("NewApi")
    public String getRealPathFromURI_API11to18(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(
                this,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if(cursor != null){
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }
        return result;
    }

    private class sendFile extends AsyncTask<Void, Void, String> {
        private ProgressDialog pdia;
        private Bitmap bitmap;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia = new ProgressDialog(GroupChatActivity.this);
            pdia.setMessage(getString(R.string.progress_dialog_uploading_file));
            pdia.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try
            {
                bitmap = Utils.decodeFile(photoPath);
                filePushToServerController =  new FilePushToServerController(GroupChatActivity.this);
                multiPartFile = filePushToServerController.prepareFileToSend
                        (
                                bitmap,
                                Constants.MULTIPART_FILE,
                                _profile_id
                        );
                filePushToServerController.sendImageRequest
                        (
                                Constants.CONTACT_API_POST_FILE,
                                Constants.MULTIPART_FILE,
                                multiPartFile,
                                Constants.MEDIA_TYPE_JPG
                        );

                String response = filePushToServerController.executeRequest();

                return response;
            }
            catch (Exception e)
            {
                Log.e(Constants.TAG, "FilePushToServerController.sendFile -> doInBackground: ERROR " + e.toString());
                Crashlytics.logException(e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(pdia.isShowing()) pdia.dismiss();
            Log.d(Constants.TAG, "GroupChatActivity.sendFile: Response content: " +
                    filePushToServerController.getAvatarURL(result));

            if(result!=null && result.length()>0) {
                try {
                    JSONObject jsonImage = new JSONObject(result);
                    imageSent(jsonImage.getString("file"), bitmap);
                } catch (Exception e) {
                    Log.e(Constants.TAG, "sendFile.onPostExecute: ",e);
                    Crashlytics.logException(e);
                }
            }
        }
    }

    @Subscribe
    public void onEventMessageStatusChanged(MessageStatusChanged event){
        ChatMessage chatMsg = chatTransactions.getChatMessageById(event.getId(), realm);

        if(chatMsg.getDirection().compareTo(Constants.CHAT_MESSAGE_DIRECTION_RECEIVED)==0)
            return;

        if((isGroupChatMode && chatMsg!=null && chatMsg.getGroup_id().compareTo(_groupId)==0)
                || (!isGroupChatMode && chatMsg!=null && chatMsg.getContact_id().compareTo(_contactId)==0))
            loadMessagesArray();
    }

    private class DownloadFile extends AsyncTask<String, Void, Boolean> {
        private String param0;
        private String param1;

        @Override
        protected Boolean doInBackground(String[] params) {
            Log.i(Constants.TAG, "DownloadFile.doInBackground: ");
            param0 = params[0];
            param1 = params[1];
            XMPPTransactions.downloadToChatFile(param0,param1);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            Log.i(Constants.TAG, "DownloadFile.onPostExecute: ");
            refreshAdapter();
        }
    }

    @Subscribe
    public void onConnectivityChanged(ConnectivityChanged event)
    {

        Log.i(Constants.TAG, "GroupChatActivity.onConnectivityChanged: "
                + event.getConnectivityStatus().toString());

        if(event.getConnectivityStatus()!= ConnectivityStatus.MOBILE_CONNECTED &&
                event.getConnectivityStatus()!=ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET)
        {
            lay_no_connection.setVisibility(View.VISIBLE);
            setSendEnabled(false);
        }

        else
        {
            lay_no_connection.setVisibility(View.GONE);
            setSendEnabled(true);
        }
    }
}