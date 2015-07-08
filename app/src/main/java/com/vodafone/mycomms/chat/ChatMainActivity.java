package com.vodafone.mycomms.chat;

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
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.contacts.connection.RecentContactController;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ChatsReceivedEvent;
import com.vodafone.mycomms.events.MessageSentEvent;
import com.vodafone.mycomms.events.MessageSentStatusChanged;
import com.vodafone.mycomms.events.XMPPConnectingEvent;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.settings.connection.FilePushToServerController;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import model.Chat;
import model.ChatMessage;
import model.Contact;

public class ChatMainActivity extends ToolbarActivity {

    private String LOG_TAG = ChatMainActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private ChatRecyclerViewAdapter mChatRecyclerViewAdapter;
    private EditText etChatTextBox;
    private TextView tvSendChat;
    private ImageView ivAvatarImage;
    private ImageView sendFileImage;
    private TextView tvAvatarText;

    private ArrayList<ChatMessage> _chatList = new ArrayList<>();
    private Chat _chat;
    private Contact _contact;
    private model.UserProfile _profile;
    private String _profile_id;

    private String photoPath = null;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private Bitmap photoBitmap = null;
    private File multiPartFile;
    private FilePushToServerController filePushToServerController;

    private String previousView;

    private RealmChatTransactions chatTransactions;
    private RealmContactTransactions contactTransactions;
    private RecentContactController mRecentContactController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_main);
        activateToolbar();
//        setToolbarBackground(R.drawable.toolbar_header);

        //Register Otto bus to listen to events
        BusProvider.getInstance().register(this);

        SharedPreferences sp = getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

        if(sp==null)
        {
            Log.e(Constants.TAG, "ChatMainActivity.onCreate: error loading Shared Preferences");
            finish();
        }

        chatTransactions = new RealmChatTransactions(this);
        _profile_id = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");
        contactTransactions = new RealmContactTransactions(_profile_id);
        _profile = contactTransactions.getUserProfile();

        if(_profile_id == null)
        {
            Log.e(Constants.TAG, "ChatMainActivity.onCreate: profile_id not found in Shared Preferences");
            finish();
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecentContactController = new RecentContactController(this, _profile_id);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        refreshAdapter();

        etChatTextBox = (EditText) findViewById(R.id.chat_text_box);
        tvSendChat = (TextView) findViewById(R.id.chat_send);
        ivAvatarImage = (ImageView) findViewById(R.id.companyLogo);
        sendFileImage = (ImageView) findViewById(R.id.send_image);
        tvAvatarText = (TextView) findViewById(R.id.avatarText);

        //Load chat from db
        Intent in = getIntent();
        String contact_id = in.getStringExtra(Constants.CHAT_FIELD_CONTACT_ID);
        previousView = in.getStringExtra(Constants.CHAT_PREVIOUS_VIEW);

        if(contact_id==null || contact_id.length()==0) finish(); //Prevent from errors

        //Contact and profile
        _contact = contactTransactions.getContactById(contact_id);

        //Chat listeners
//        setChatHeaderListener(this, _contact);

        //Load chat
        _chat = chatTransactions.getChatByContactId(contact_id);

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
        File avatarFile = new File(getFilesDir(), Constants.CONTACT_AVATAR_DIR + "avatar_"+_contact.getContactId()+".jpg");

        if (_contact.getAvatar()!=null &&
                _contact.getAvatar().length()>0 &&
                _contact.getAvatar().compareTo("")!=0 &&
                avatarFile.exists()) {

            tvAvatarText.setText(null);

            Picasso.with(this)
                    .load(avatarFile)
                    .into(ivAvatarImage);

        } else{
            String initials = "";
            if(null != _contact.getFirstName() && _contact.getFirstName().length() > 0)
            {
                initials = _contact.getFirstName().substring(0,1);

                if(null != _contact.getLastName() && _contact.getLastName().length() > 0)
                {
                    initials = initials + _contact.getLastName().substring(0,1);
                }

            }

            ivAvatarImage.setImageResource(R.color.grey_middle);
            tvAvatarText.setText(initials);
        }

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

    private void sendText()
    {
        final String msg = etChatTextBox.getText().toString();

        new Thread(new Runnable() {
            @Override
            public void run() {
                //Save to DB
                final ChatMessage chatMsg = chatTransactions.newChatMessageInstance(
                        _chat.getContact_id(), Constants.CHAT_MESSAGE_DIRECTION_SENT,
                        Constants.CHAT_MESSAGE_TYPE_TEXT, msg, "");

                _chat = chatTransactions.updatedChatInstance(_chat, chatMsg);
                final String id = chatMsg.getId();

                chatTransactions.insertChat(_chat);
                chatTransactions.insertChatMessage(chatMsg);

                //Send through XMPP
                if(!XMPPTransactions.sendText(false, _contact.getContactId(),
                        chatMsg.getId(), msg))
                    return;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Insert in recents
                        String action = Constants.CONTACTS_ACTION_SMS;
                        mRecentContactController.insertRecent(_chat.getContact_id(), action);

                        //Notify app to refresh any view if necessary
                        if (previousView.equals(Constants.CHAT_VIEW_CHAT_LIST)) {
                            BusProvider.getInstance().post(new MessageSentEvent());
                        } else if (previousView.equals(Constants.CHAT_VIEW_CONTACT_LIST)) {
                            //Recent List is refreshed onConnectionComplete
                        }

                        _chatList.add(chatMsg);
                        if(_chatList.size()>50) _chatList.remove(0);

                        refreshAdapter();
                        etChatTextBox.setText("");
                    }
                });
            }
        }).start();

    }

    private void imageSent(String imageUrl)
    {
        //Save to DB
        ChatMessage chatMsg = chatTransactions.newChatMessageInstance(
                _chat.getContact_id(), Constants.CHAT_MESSAGE_DIRECTION_SENT,
                Constants.CHAT_MESSAGE_TYPE_IMAGE, "", imageUrl);

        _chat = chatTransactions.updatedChatInstance(_chat, chatMsg);

        chatTransactions.insertChat(_chat);
        chatTransactions.insertChatMessage(chatMsg);

        //Send through XMPP
        if (!XMPPTransactions.sendImage(_contact.getContactId(), Constants.XMPP_STANZA_TYPE_CHAT,
                chatMsg.getId(), imageUrl))
            return;

        //Download to file
        //TODO: Testing executeOnExecutor
        //new DownloadFile().execute(imageUrl, chatMsg.getId());
        DownloadFile downloadFile = new DownloadFile();
        downloadFile.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imageUrl, chatMsg.getId());
        //Insert in recents
        String action = Constants.CONTACTS_ACTION_SMS;
        mRecentContactController.insertRecent(_chat.getContact_id(), action);

        //Notify app to refresh any view if necessary
        if (previousView.equals(Constants.CHAT_VIEW_CHAT_LIST)) {
            BusProvider.getInstance().post(new MessageSentEvent());
        } else if (previousView.equals(Constants.CHAT_VIEW_CONTACT_LIST)) {
            //Recent List is refreshed onConnectionComplete
        }

        _chatList.add(chatMsg);
        if(_chatList.size()>50) _chatList.remove(0);

        refreshAdapter();
    }

    private void loadMessagesArray()
    {
        _chatList = chatTransactions.getAllChatMessages(_chat.getContact_id());
        refreshAdapter();
    }

    private void refreshAdapter()
    {
        mChatRecyclerViewAdapter = new ChatRecyclerViewAdapter(ChatMainActivity.this, _chatList, _profile);
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

    protected void onResume() {
        super.onResume();
        XMPPTransactions.initializeMsgServerSession(getApplicationContext());

        if(etChatTextBox.getText().toString()!=null &&
                etChatTextBox.getText().toString().length()>0) checkXMPPConnection();
        else setSendEnabled(false);

        final String contactId = _contact.getContactId();

        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<ChatMessage> messages =
                        chatTransactions.getNotReadReceivedContactChatMessages(contactId);

                if (messages != null && messages.size() > 0) {
                    XMPPTransactions.sendReadIQReceivedMessagesList(messages);
                    chatTransactions.setContactAllChatMessagesReceivedAsRead(contactId);
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecentContactController.closeRealm();
        chatTransactions.closeRealm();
        XMPPTransactions.closeRealm();
        contactTransactions.closeRealm();
    }

    @Subscribe
    public void onEventChatsReceived(ChatsReceivedEvent event){
        ChatMessage chatMsg = event.getMessage();
        if(chatMsg!=null)
        {
            _chatList.add(chatMsg);
            if(chatMsg.getDirection().equals(Constants.CHAT_MESSAGE_DIRECTION_RECEIVED)) {
                mRecentContactController.insertRecent(chatMsg.getContact_id(), Constants.CONTACTS_ACTION_SMS);
            }

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

            //TODO: Testing executeOnExecutor
//            new sendFile().execute();
            sendFile sendFile = new sendFile();
            sendFile.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void dispatchTakePictureIntent(String title, String subtitle)
    {

        //Build the alert dialog to let the user choose the origin of the picture

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatMainActivity.this);
        builder.setTitle(title);

        if(subtitle != null) {
            LayoutInflater inflater = ChatMainActivity.this.getLayoutInflater();
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
        Cursor cursor = ChatMainActivity.this.getContentResolver().query(contentURI, null, null, null,
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
            pdia = new ProgressDialog(ChatMainActivity.this);
            pdia.setMessage(ChatMainActivity.this.getString(R.string.progress_dialog_uploading_file));
            pdia.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try
            {
                filePushToServerController =  new FilePushToServerController(ChatMainActivity.this);
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

                return filePushToServerController.executeRequest();
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
                    imageSent(jsonImage.getString("file"));
                } catch (Exception e) {
                    Log.e(Constants.TAG, "sendFile.onPostExecute: ",e);
                }
            }
        }
    }

    @Subscribe
    public void onEventMessageSentStatusChanged(MessageSentStatusChanged event){
        ChatMessage chatMsg = chatTransactions.getChatMessageById(event.getId());

        if(chatMsg!=null && chatMsg.getContact_id().compareTo(_contact.getContactId())==0)
            loadMessagesArray();
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
}
