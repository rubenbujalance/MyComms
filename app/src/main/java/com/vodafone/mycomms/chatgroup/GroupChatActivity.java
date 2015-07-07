package com.vodafone.mycomms.chatgroup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chat.ChatRecyclerViewAdapter;
import com.vodafone.mycomms.contacts.connection.IRecentContactConnectionCallback;
import com.vodafone.mycomms.contacts.connection.RecentContactController;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ChatsReceivedEvent;
import com.vodafone.mycomms.events.XMPPConnectingEvent;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.settings.connection.FilePushToServerController;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import model.ChatMessage;
import model.Contact;
import model.GroupChat;

/**
 * Created by str_oan on 29/06/2015.
 */
public class GroupChatActivity extends ToolbarActivity implements
        IRecentContactConnectionCallback, Serializable {

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

    private String photoPath = null;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private Bitmap photoBitmap = null;
    private File multiPartFile;
    private FilePushToServerController filePushToServerController;

    private Realm mRealm;
    private RealmChatTransactions chatTransactions;
    private RealmContactTransactions contactTransactions;
    private RecentContactController mRecentContactController;

    private ArrayList<String> contactIds;
    private ArrayList<Contact> contactList;
    private String composedContactId = null;
    private GroupChatController mGroupChatController;
    private RealmGroupChatTransactions mGroupChatTransactions;
    private String groupChatAbout = null;
    private String groupChatAvatar = null;
    private String groupChatName = null;
    private GroupChat groupChat;
    private SharedPreferences sp;

    private ImageView top_left_avatar, top_right_avatar, bottom_left_avatar, bottom_right_avatar;
    private TextView top_left_avatar_text, top_right_avatar_text, bottom_left_avatar_text, bottom_right_avatar_text;
    private LinearLayout lay_to_hide;

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
        this.mGroupChatController = new GroupChatController(GroupChatActivity.this);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);

        //Load chat from db
        loadExtras();
        loadContactsFromIds();
        loadTheRestOfTheComponents();

    }

    public void setGroupChatAvatar()
    {
        if(null == this.groupChatAvatar)
        {
            ArrayList<ImageView> images = new ArrayList<>();
            images.add(top_left_avatar);
            images.add(bottom_left_avatar);
            images.add(bottom_right_avatar);

            ArrayList<TextView> texts = new ArrayList<>();
            texts.add(top_left_avatar_text);
            texts.add(bottom_left_avatar_text);
            texts.add(bottom_right_avatar_text);

            if(null != contactIds && contactIds.size() > 3)
            {
                lay_to_hide.setVisibility(View.VISIBLE);
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

    private void startGroupChatListActivity()
    {
        Intent in = new Intent(GroupChatActivity.this, GroupChatListActivity.class);
        in.putExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY, LOG_TAG);
        in.putExtra(Constants.GROUP_CHAT_ID, groupChat.getId());
        startActivity(in);
    }

    private void loadExtras()
    {
        Intent in = getIntent();
        this.groupChatName = in.getStringExtra(Constants.GROUP_CHAT_NAME);
        if(null != this.groupChatName && this.groupChatName.length() == 0)
            this.groupChatName = null;

        this.groupChatAbout = in.getStringExtra(Constants.GROUP_CHAT_ABOUT);
        if(null != this.groupChatAbout && this.groupChatAbout.length() == 0)
            this.groupChatAbout = null;

        this.groupChatAvatar = in.getStringExtra(Constants.GROUP_CHAT_AVATAR);
        if(null != this.groupChatAvatar && this.groupChatAvatar.length() == 0)
            this.groupChatAvatar = null;

        this.groupChat = mGroupChatTransactions.getGroupChatById(in.getStringExtra(Constants.GROUP_CHAT_ID));
        this.composedContactId = in.getStringExtra(Constants.GROUP_CHAT_MEMBERS);
        loadContactIds();

    }

    private void loadContactIds()
    {
        String[] ids = composedContactId.split("@");
        contactIds = new ArrayList<>();
        for(String id : ids)
        {
            contactIds.add(id);
        }
    }

    private void loadContactsFromIds()
    {
        contactList = new ArrayList<>();
        Contact contact = new Contact();
        contact.setAvatar(_profile.getAvatar());
        contact.setFirstName(_profile.getFirstName());
        contact.setLastName(_profile.getLastName());
        contact.setContactId(_profile.getId());
        contactList.add(contact);
        //Contact and profile
        for(String id : contactIds)
        {
            if(!id.equals(_profile_id))
            {
                contact = contactTransactions.getContactById(id);
                contactList.add(contact);
            }
        }
    }

    /*private void sendText()
    {
        String msg = etChatTextBox.getText().toString();
        ChatMessage chatMsg = chatTransactions.newChatMessageInstance
                (
                        composedContactId
                        , Constants.CHAT_MESSAGE_DIRECTION_SENT
                        , Constants.CHAT_MESSAGE_TYPE_TEXT
                        , msg
                        , ""
                        , composedContactId
                );

        groupChat = chatTransactions.updatedGroupChatInstance(chatMsg, composedContactId);

        chatTransactions.insertChat(groupChat);
        chatTransactions.insertGroupChatMessage(chatMsg, composedContactId);
        for(String id : contactIds)
        {
            //Send through XMPP
            if (!XMPPTransactions.sendText(id, Constants.XMPP_STANZA_TYPE_CHAT,
                    chatMsg.getId(), msg))
                return;

            //Insert in recents
            String action = Constants.CONTACTS_ACTION_SMS;
            mRecentContactController.insertRecent(id, action);
            mRecentContactController.setConnectionCallback(this);
        }

        _chatList.add(chatMsg);
        if(_chatList.size()>50) _chatList.remove(0);
        refreshAdapter();
        etChatTextBox.setText("");
    }*/

    /*private void imageSent(String imageUrl)
    {
        ChatMessage chatMsg = chatTransactions.newChatMessageInstance
                (
                        composedContactId
                        , Constants.CHAT_MESSAGE_DIRECTION_SENT
                        , Constants.CHAT_MESSAGE_TYPE_IMAGE
                        , ""
                        , imageUrl
                        , composedContactId
                );

            groupChat = chatTransactions.updatedGroupChatInstance(chatMsg, composedContactId);

            chatTransactions.insertChat(groupChat);
            chatTransactions.insertChatMessage(chatMsg);

        for(String id : contactIds)
        {
            //Send through XMPP
            if (!XMPPTransactions.sendImage(id, Constants.XMPP_STANZA_TYPE_CHAT,
                    chatMsg.getId(), imageUrl))
                return;

            //Download to file
            new DownloadFile().execute(imageUrl, chatMsg.getId());

            //Insert in recents
            String action = Constants.CONTACTS_ACTION_SMS;
            mRecentContactController.insertRecent(id, action);
            mRecentContactController.setConnectionCallback(this);
        }

        //Notify app to refresh any view if necessary
        if (previousView.equals(Constants.CHAT_VIEW_CHAT_LIST)) {
            BusProvider.getInstance().post(new MessageSentEvent());
        } else if (previousView.equals(Constants.CHAT_VIEW_CONTACT_LIST)) {
            //Recent List is refreshed onConnectionComplete
        }

        _chatList.add(chatMsg);
        if(_chatList.size()>50) _chatList.remove(0);
        refreshAdapter();
    }*/

    private void loadMessagesArray()
    {
        _chatList = chatTransactions.getAllChatMessages(composedContactId);
        refreshAdapter();
    }

    private void refreshAdapter()
    {
        mChatRecyclerViewAdapter = new ChatRecyclerViewAdapter(GroupChatActivity.this, _chatList,
                _profile, contactList.get(0));
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
            mRecentContactController.insertRecent(chatMsg.getContact_id(), Constants.CONTACTS_ACTION_SMS);
            mRecentContactController.setConnectionCallback(this);
            if(_chatList.size()>50) _chatList.remove(0);
            refreshAdapter();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK)
        {
            photoBitmap = decodeFile(photoPath);
            new sendFile().execute();
        }

        else if(requestCode == REQUEST_IMAGE_GALLERY && resultCode == Activity.RESULT_OK)
        {
            Uri selectedImage = data.getData();
            photoPath = getRealPathFromURI(selectedImage);
            photoBitmap = decodeFile(photoPath);

            new sendFile().execute();
        }
    }

    private void dispatchTakePictureIntent(String title, String subtitle)
    {

        //Build the alert dialog to let the user choose the origin of the picture

        AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
        builder.setTitle(title);

        if(subtitle != null) {
            LayoutInflater inflater = GroupChatActivity.this.getLayoutInflater();
            View view = inflater.inflate(R.layout.cv_title_subtitle, null);
            ((TextView) view.findViewById(R.id.tvTitle)).setText(title);
            ((TextView) view.findViewById(R.id.tvSubtitle)).setText(subtitle);
            builder.setCustomTitle(view);
        }
        else
        {
            builder.setTitle(title);
        }

        builder.setItems(R.array.add_photo_chooser, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                Intent in;

                if(which == 0)
                {
                    in = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    in.putExtra(MediaStore.EXTRA_OUTPUT, setImageUri());
                    startActivityForResult(in, REQUEST_IMAGE_CAPTURE);
                }
                else if(which == 1)
                {
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
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private String getRealPathFromURI(Uri contentURI)
    {
        String result;
        Cursor cursor = GroupChatActivity.this.getContentResolver().query(contentURI, null, null, null,
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

    private class sendFile extends AsyncTask<Void, Void, String> {
        private ProgressDialog pdia;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia = new ProgressDialog(GroupChatActivity.this);
            pdia.setMessage(GroupChatActivity.this.getString(R.string.progress_dialog_uploading_file));
            pdia.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try
            {
                filePushToServerController =  new FilePushToServerController(GroupChatActivity.this);
                multiPartFile = filePushToServerController.prepareFileToSend
                        (
                                photoBitmap,
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
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(pdia.isShowing()) pdia.dismiss();
            Log.d(Constants.TAG, "ChatMainActivity.sendFile: Response content: " +
                    filePushToServerController.getAvatarURL(result));

            if(result!=null && result.length()>0) {
                try {
                    JSONObject jsonImage = new JSONObject(result);
                    //imageSent(jsonImage.getString("file"));
                } catch (Exception e) {
                    Log.e(Constants.TAG, "sendFile.onPostExecute: ",e);
                }
            }
        }
    }

    @Subscribe
    public void onEventXMPPConnecting(XMPPConnectingEvent event){
        boolean isConnecting = event.isConnecting();

        if(!isConnecting)
            checkXMPPConnection();
        else setSendEnabled(false);
    }

    private class DownloadFile extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String[] params) {
            return XMPPTransactions.downloadToChatFile(params[0],params[1]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            refreshAdapter();
        }
    }

    private void loadTheRestOfTheComponents()
    {
        imgModifyGroupChat = (ImageView) findViewById(R.id.img_modify_group_chat);
        if(groupChat.getCreatorId().equals(_profile_id))
            imgModifyGroupChat.setVisibility(View.VISIBLE);
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
        lay_to_hide = (LinearLayout) findViewById(R.id.lay_top_right_image_hide);
        lay_to_hide.setVisibility(View.GONE);

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

        //Sent chat in grey by default
        setSendEnabled(false);

        etChatTextBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                if (cs != null && cs.length() > 0) checkXMPPConnection();
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
                //sendText();
            }
        });

        sendFileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                dispatchTakePictureIntent(getString(R.string.how_would_you_like_to_add_a_photo), null);
            }
        });

        imgModifyGroupChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                startGroupChatListActivity();
            }
        });
    }
}