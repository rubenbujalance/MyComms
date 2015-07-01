package com.vodafone.mycomms.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.vodafone.mycomms.ContactListMainActivity;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chat.ChatMainActivity;
import com.vodafone.mycomms.chatlist.view.ChatListHolder;
import com.vodafone.mycomms.contacts.connection.RecentContactController;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ChatsReceivedEvent;
import com.vodafone.mycomms.events.DashboardCreatedEvent;
import com.vodafone.mycomms.events.NewsReceivedEvent;
import com.vodafone.mycomms.events.RecentAvatarsReceivedEvent;
import com.vodafone.mycomms.events.RecentContactsReceivedEvent;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmNewsTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import io.realm.Realm;
import model.News;
import model.RecentContact;

public class DashBoardActivity extends ToolbarActivity{
    private LinearLayout noConnectionLayout;
    private Realm _realm;
    private Realm mRealm;
    private RealmChatTransactions _chatTx;
    private ArrayList<News> newsArrayList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(Constants.TAG, "DashBoardActivity.onCreate: ");

        BusProvider.getInstance().register(this);

        enableToolbarIsClicked(false);
        setContentView(R.layout.layout_dashboard);

        initALL();
        //BusProvider.getInstance().post(new InitNews());
        //BusProvider.getInstance().post(new InitProfileAndContacts());

        mRealm = Realm.getInstance(getBaseContext());
        loadRecents();
        loadNews();

        BusProvider.getInstance().post(new DashboardCreatedEvent());
    }

    private void initALL(){

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                // Set Time line
                DateFormat tf = new SimpleDateFormat("HH:mm");
                String time = tf.format(Calendar.getInstance().getTime());

                TextView timeText = (TextView) findViewById(R.id.timeDashboard);
                timeText.setText(time);

                // Set Date line
                DateFormat df = new SimpleDateFormat("EEEE, d MMMM");
                String date = df.format(Calendar.getInstance().getTime());

                TextView dateText = (TextView) findViewById(R.id.dateDashboard);
                dateText.setText(date);
            }
        });

        noConnectionLayout = (LinearLayout) findViewById(R.id.no_connection_layout);
        activateFooter();

        setFooterListeners(this);

        activateFooterSelected(Constants.TOOLBAR_DASHBOARD);

        // Event Click listeners
        ImageView btMagnifier = (ImageView) findViewById(R.id.magnifier);
        btMagnifier.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Start Contacts activity
                Constants.isSearchBarFocusRequested = true;
                Intent in = new Intent(DashBoardActivity.this, ContactListMainActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(in);
                finish();
            }
        });

        LinearLayout btFavourite = (LinearLayout) findViewById(R.id.LayoutFavourite);
        btFavourite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Start Favourites activity
                Intent in = new Intent(DashBoardActivity.this, ContactListMainActivity.class);
                in.putExtra(Constants.toolbar, false);
                in.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(in);
                finish();
            }
        });
    }

    private void loadRecents(){
        Log.e(Constants.TAG, "DashBoardActivity.loadRecents: ");

        try {
            SharedPreferences sp = getSharedPreferences(
                    Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
            final String profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");
            ArrayList<RecentContact> recentList = new ArrayList<>();

            RealmContactTransactions realmContactTransactions = new RealmContactTransactions(mRealm, profileId);
            recentList = realmContactTransactions.getAllRecentContacts();

            LinearLayout recentsContainer = (LinearLayout) findViewById(R.id.list_recents);
            recentsContainer.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(this);
            RecentContact recentContact;

            for (int i = 0; i < recentList.size(); i++) {
                recentContact = recentList.get(i);

                View childRecents = inflater.inflate(R.layout.layout_recents_dashboard, recentsContainer, false);

                recentsContainer.addView(childRecents);
                childRecents.setPadding(10, 20, 10, 20);

                final ImageView recentAvatar = (ImageView) childRecents.findViewById(R.id.recent_avatar);
                final File avatarFile = new File(getFilesDir() + Constants.CONTACT_AVATAR_DIR,
                        "avatar_"+recentContact.getContactId()+".jpg");

                //Avatar image
                if (avatarFile.exists()) { //From file if already exists
                    Picasso.with(this)
                            .load(avatarFile)
                            .fit().centerCrop()
                            .into(recentAvatar);
                } else {
                    //Set name initials image during the download
                    String initials = "";
                    if (null != recentContact.getFirstName() && recentContact.getFirstName().length() > 0) {
                        initials = recentContact.getFirstName().substring(0, 1);

                        if (null != recentContact.getLastName() && recentContact.getLastName().length() > 0) {
                            initials = initials + recentContact.getLastName().substring(0, 1);
                        }

                    }

                    final TextView avatarText = (TextView) childRecents.findViewById(R.id.avatarText);
                    recentAvatar.setImageResource(R.color.grey_middle);
                    avatarText.setText(initials);

                    //Download avatar
                    if (recentContact.getAvatar() != null &&
                            recentContact.getAvatar().length() > 0) {
                        File avatarsDir = new File(getFilesDir() + Constants.CONTACT_AVATAR_DIR);

                        if(!avatarsDir.exists()) avatarsDir.mkdirs();

                        final Target target = new Target() {
                            @Override
                            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                                OkHttpSaveBitmapToDiskAsyncTask task =
                                        new OkHttpSaveBitmapToDiskAsyncTask(
                                                recentAvatar, avatarText, avatarFile, bitmap);

                                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {
                                if(avatarFile.exists()) avatarFile.delete();
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                            }
                        };

                        recentAvatar.setTag(target);

                        Picasso.with(this)
                            .load(recentContact.getAvatar())
                            .into(target);
                    }
                }

                // Names
                TextView firstName = (TextView) childRecents.findViewById(R.id.recent_firstname);
                firstName.setText(recentContact.getFirstName());

                TextView lastName = (TextView) childRecents.findViewById(R.id.recent_lastname);
                lastName.setText(recentContact.getLastName());

                // Badges
                _realm = Realm.getInstance(this);
                _chatTx = new RealmChatTransactions(_realm, this);

                ChatListHolder chatHolder = new ChatListHolder(childRecents);

                long count =_chatTx.getChatPendingMessagesCount(recentContact.getContactId());
                String action = recentContact.getAction();
                if(count > 0 && action.equals(Constants.CONTACTS_ACTION_SMS)) {
                    TextView unread_messages = (TextView) childRecents.findViewById(R.id.unread_messages);
                    unread_messages.setVisibility(View.VISIBLE);
                    unread_messages.setText(String.valueOf(count));
                } else {
                    ImageView typeRecent = (ImageView) childRecents.findViewById(R.id.type_recent);
                    typeRecent.setVisibility(View.VISIBLE);

                    int sdk = Build.VERSION.SDK_INT;
                    if (action.equals(Constants.CONTACTS_ACTION_CALL)) {
                        if (sdk < Build.VERSION_CODES.JELLY_BEAN)
                            typeRecent.setBackgroundDrawable(getResources().getDrawable(R.mipmap.icon_notification_phone_grey));
                        else
                            typeRecent.setBackground(getResources().getDrawable(R.mipmap.icon_notification_phone_grey));
                    } else if (action.equals(Constants.CONTACTS_ACTION_EMAIL)) {
                        if (sdk < Build.VERSION_CODES.JELLY_BEAN)
                            typeRecent.setBackgroundDrawable(getResources().getDrawable(R.mipmap.icon_notification_mail_grey));
                        else
                            typeRecent.setBackground(getResources().getDrawable(R.mipmap.icon_notification_mail_grey));
                    } else {
                        if (sdk < Build.VERSION_CODES.JELLY_BEAN)
                            typeRecent.setBackgroundDrawable(getResources().getDrawable(R.mipmap.icon_notification_chat_grey));
                        else
                            typeRecent.setBackground(getResources().getDrawable(R.mipmap.icon_notification_chat_grey));
                    }
                }
                LinearLayout btRecents = (LinearLayout) childRecents.findViewById(R.id.recent_content);
                final ArrayList<RecentContact> finalRecentList = recentList;
                final int position = i;
                btRecents.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        try {
                            String action = finalRecentList.get(position).getAction();
                            if (action.compareTo(Constants.CONTACTS_ACTION_CALL) == 0) {
                                String strPhones = finalRecentList.get(position).getPhones();
                                if (strPhones != null)
                                {
                                    String phone = strPhones;
                                    if(!finalRecentList.get(position).getPlatform().equals(Constants
                                            .PLATFORM_LOCAL))
                                    {
                                        JSONArray jPhones = new JSONArray(strPhones);
                                        phone = (String)((JSONObject) jPhones.get(0)).get(Constants.CONTACT_PHONE);
                                    }

                                    Utils.launchCall(phone, DashBoardActivity.this);
                                }
                            }
                            else if (action.compareTo(Constants.CONTACTS_ACTION_SMS) == 0)
                            {
                                // This is LOCAL contact, then in this case the action will be Send SMS
                                // message
                                if(null != finalRecentList.get(position).getPlatform() && finalRecentList.get
                                        (position).getPlatform().equals(Constants.PLATFORM_LOCAL))
                                {
                                    String phone = finalRecentList.get(position).getPhones();
                                    if(null != phone)
                                    {
                                        Utils.launchSms(phone, DashBoardActivity.this);
                                    }
                                }
                                else
                                {
                                    Intent in = new Intent(DashBoardActivity.this, ChatMainActivity.class);
                                    in.putExtra(Constants.CHAT_FIELD_CONTACT_ID, finalRecentList.get(position).getContactId());
                                    in.putExtra(Constants.CHAT_PREVIOUS_VIEW, Constants.CHAT_VIEW_CONTACT_LIST);
                                    startActivity(in);
                                }

                            }
                            else if (action.compareTo(Constants.CONTACTS_ACTION_EMAIL) == 0) {
                                String strEmails = finalRecentList.get(position).getEmails();
                                if (strEmails != null)
                                {
                                    String email = strEmails;
                                    if(!finalRecentList.get(position).getPlatform().equals(Constants
                                            .PLATFORM_LOCAL))
                                    {
                                        JSONArray jPhones = new JSONArray(strEmails);
                                        email = (String)((JSONObject) jPhones.get(0)).get(Constants.CONTACT_EMAIL);
                                    }

                                    Utils.launchEmail(email, DashBoardActivity.this);
                                }
                            }
                            //ADD RECENT
                            Realm realm = Realm.getInstance(getBaseContext());
                            RecentContactController recentController = new RecentContactController(DashBoardActivity.this,realm,profileId);
                            recentController.insertRecent(finalRecentList.get(position).getContactId(), action);
                            //setListAdapterTabs();
                        } catch (Exception ex) {
                            Log.e(Constants.TAG, "DashBoardActivity.onItemClick: ", ex);
                        }
                    }
                });

            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "Load recents error: ",e);
        }
    }


    private void loadNews() {
        Log.e(Constants.TAG, "DashBoardActivity.loadNews: ");

        newsArrayList = new ArrayList<>();

        RealmNewsTransactions realmNewsTransactions = new RealmNewsTransactions(mRealm);
        newsArrayList = realmNewsTransactions.getAllNews();

        if(newsArrayList != null){
            drawNews(newsArrayList);
        }
    }

    private void drawNews(ArrayList<News> newsArrayList) {
        Log.e(Constants.TAG, "DashBoardActivity.drawNews: ");

        try{
            LinearLayout container = (LinearLayout) findViewById(R.id.list_news);
            container.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(this);

            for (int i = 0; i < newsArrayList.size(); i++) {
                View child = inflater.inflate(R.layout.layout_news_dashboard, container, false);

                container.addView(child);
                child.setPadding(10, 20, 10, 20);

                ImageView newsImage = (ImageView) child.findViewById(R.id.notice_image);
                File newsFile = new File(getFilesDir(), Constants.CONTACT_NEWS_DIR +
                        "news_"+newsArrayList.get(i).getUuid()+".jpg");

                if (newsFile.exists()) {
                    Picasso.with(this)
                            .load(newsFile)
                            .fit().centerInside()
                            .into(newsImage);
                } else{
                    Picasso.with(this)
                            .load("https://" + EndpointWrapper.getBaseNewsURL() + newsArrayList.get(i).getImage())
                            .fit().centerInside()
                            .into(newsImage);
                }

                final TextView title = (TextView) child.findViewById(R.id.notice_title);
                title.setText(newsArrayList.get(i).getTitle());

                TextView date = (TextView) child.findViewById(R.id.notice_date);
                Long current = Calendar.getInstance().getTimeInMillis();
                date.setText(Utils.getShortStringTimeDifference(current - newsArrayList.get(i).getPublished_at()));

                final String detailImage = newsArrayList.get(i).getImage();
                final String detailTitle = newsArrayList.get(i).getTitle();
                final String detailAvatar = newsArrayList.get(i).getAuthor_avatar();
                final String detailAuthor = newsArrayList.get(i).getAuthor_name();
                final String detailPublished = Utils.getShortStringTimeDifference(current - newsArrayList.get(i).getPublished_at());
                final String detailHtml = newsArrayList.get(i).getHtml();

                LinearLayout btnews = (LinearLayout) child.findViewById(R.id.notice_content);
                btnews.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent in = new Intent(DashBoardActivity.this, NewsDetailActivity.class);
                        in.putExtra(Constants.NEWS_IMAGE, detailImage);
                        in.putExtra(Constants.NEWS_TITLE, detailTitle);
                        in.putExtra(Constants.NEWS_AUTHOR_AVATAR, detailAvatar);
                        in.putExtra(Constants.NEWS_AUTHOR_NAME, detailAuthor);
                        in.putExtra(Constants.NEWS_PUBLISHED_AT, detailPublished);
                        in.putExtra(Constants.NEWS_HTML, detailHtml);
                        in.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(in);
                        finish();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "DashBoardActivity.drawNews: " + e);
        }
    }

    //Prevent of going from main screen back to login
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(Constants.TAG, "DashBoardActivity.onDestroy: ");

        // Disconnect from the XMPP server
        //XMPPTransactions.disconnectMsgServerSession();
        BusProvider.getInstance().unregister(this);
        mRealm.close();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(Constants.TAG, "DashBoardActivity.onPause: ");
        SharedPreferences sp = getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        if (sp.getBoolean(Constants.IS_TOOLBAR_CLICKED, true)){
            enableToolbarIsClicked(true);
            finish();
            overridePendingTransition(0, 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Update Pending Messages on Toolbar
//        loadRecents();
        checkUnreadChatMessages();
    }

    @Subscribe
    public void onEventNewsReceived(NewsReceivedEvent event) {
        Log.e(Constants.TAG, "DashBoardActivity.onEventNewsReceived: ");
        final ArrayList<News> news = event.getNews();
        if(news != null) {
            if (newsArrayList==null ||newsArrayList.size()==0) {
                Log.i(Constants.TAG, "DashBoardActivity.onEventNewsReceived: FIRST LOAD");
                drawNews(news);
            }
            initALL();
        }
    }

    @Subscribe
    public void onEventChatsReceived(ChatsReceivedEvent event) {
        checkUnreadChatMessages();
        loadRecents();
    }

    @Subscribe
    public void onRecentContactsReceived(RecentContactsReceivedEvent event){
        Log.e(Constants.TAG, "DashBoardActivity.onRecentContactsReceived: ");

        loadRecents();
        ((MycommsApp)getApplication()).getNews();
    }

    @Subscribe
    public void onRecentAvatarsReceived(RecentAvatarsReceivedEvent event){
        loadRecents();
    }

    public class OkHttpSaveBitmapToDiskAsyncTask extends AsyncTask<Void, Void, Void> {

        ImageView avatarImage;
        TextView avatarText;
        File avatarFile;
        Bitmap bitmap;

        public OkHttpSaveBitmapToDiskAsyncTask(ImageView avatarImage, TextView avatarText,
                                               File avatarFile, Bitmap bitmap) {
            this.avatarImage = avatarImage;
            this.avatarText = avatarText;
            this.avatarFile = avatarFile;
            this.bitmap = bitmap;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.e(Constants.TAG, "OkHttpSaveBitmapToDiskAsyncTask.doInBackground: START "+avatarFile);

            try {
                //Descarga del archivo local
                Log.e(Constants.TAG, "OkHttpSaveBitmapToDiskAsyncTask.doInBackground (INICIO DownloadAvatar by Picasso: "
                        + avatarFile.toString());

                avatarFile.createNewFile();
                FileOutputStream ostream = new FileOutputStream(avatarFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, ostream);
                ostream.close();

                Log.e(Constants.TAG, "OkHttpSaveBitmapToDiskAsyncTask.doInBackground (FIN DownloadAvatar by Picasso: "
                        + avatarFile.toString());

            } catch (Exception e) {
                Log.e(Constants.TAG, "DashBoardActivity.run(Picasso avatar Target): ", e);
                if(avatarFile.exists()) avatarFile.delete();
            }

            Log.e(Constants.TAG, "OkHttpSaveBitmapToDiskAsyncTask.doInBackground: END "+avatarFile);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.e(Constants.TAG, "OkHttpSaveBitmapToDiskAsyncTask.onPostExecute: "+avatarFile);

            //Carga del bitmap
            avatarText.setVisibility(View.INVISIBLE);

            Picasso.with(DashBoardActivity.this)
                    .load(avatarFile)
                    .fit().centerCrop()
                    .into(avatarImage);
        }
    }
}
