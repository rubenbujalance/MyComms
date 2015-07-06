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
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chat.ChatMainActivity;
import com.vodafone.mycomms.connection.AsyncTaskQueue;
import com.vodafone.mycomms.connection.ConnectionsQueue;
import com.vodafone.mycomms.contacts.connection.RecentContactController;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ChatsReceivedEvent;
import com.vodafone.mycomms.events.DashboardCreatedEvent;
import com.vodafone.mycomms.events.NewsReceivedEvent;
import com.vodafone.mycomms.events.RecentContactsReceivedEvent;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmNewsTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.SaveAndShowImageAsyncTask;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
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
    private AsyncTaskQueue recentsTasksQueue = new AsyncTaskQueue();
    private boolean recentsLoading = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(Constants.TAG, "DashBoardActivity.onCreate: ");

        BusProvider.getInstance().register(this);

        enableToolbarIsClicked(false);
        setContentView(R.layout.layout_dashboard);

        initALL();

        mRealm = Realm.getInstance(getBaseContext());
        loadRecents();
        loadNews();

        BusProvider.getInstance().post(new DashboardCreatedEvent());
    }


    private void initALL(){
        int sdk = Build.VERSION.SDK_INT;
        if (sdk < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    // Set Time line
                    //                DateFormat tf = new SimpleDateFormat("HH:mm");
                    //                String time = tf.format(Calendar.getInstance().getTime());

                    //                TextView timeText = (TextView) findViewById(R.id.timeDashboard);
                    //                timeText.setText(time);

                    // Set Date line
                                    DateFormat df = new SimpleDateFormat("EEEE, d MMMM");
                                    String date = df.format(Calendar.getInstance().getTime());

                                    TextView dateText = (TextView) findViewById(R.id.dateDashboard);
                                    dateText.setText(date);
                }
            });
        }

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
                in.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(in);
//                finish();
            }
        });

        LinearLayout btFavourite = (LinearLayout) findViewById(R.id.LayoutFavourite);
        btFavourite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Start Favourites activity
                Intent in = new Intent(DashBoardActivity.this, ContactListMainActivity.class);
                in.putExtra(Constants.toolbar, false);
                in.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(in);
//                finish();
            }
        });
    }

    private void loadRecents(){
        Log.e(Constants.TAG, "DashBoardActivity.loadRecents: ");
        if(recentsLoading) return;

        recentsLoading = true;

        try {
            SharedPreferences sp = getSharedPreferences(
                    Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
            String profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");

            ArrayList<RecentContact> recentList = new ArrayList<>();

            RealmContactTransactions realmContactTransactions = new RealmContactTransactions(mRealm, profileId);

            LinearLayout recentsContainer = (LinearLayout) findViewById(R.id.list_recents);
            recentsContainer.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(this);
            RecentContact recentContact;

            recentList = realmContactTransactions.getAllRecentContacts();

            for (int i = 0; i < recentList.size(); i++) {
                recentContact = recentList.get(i);
                DrawSingleRecentAsyncTask task = new DrawSingleRecentAsyncTask(recentContact.getContactId(),
                        recentContact.getFirstName(),recentContact.getLastName(),
                        recentContact.getAvatar(),recentContact.getAction(),
                        recentContact.getPhones(),recentContact.getEmails(),
                        recentContact.getPlatform(),recentContact.getUniqueId(),profileId,
                        recentsContainer,inflater);

                recentsTasksQueue.putConnection(recentContact.getUniqueId(),task);
                task.execute();
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "Load recents error: ",e);
        }

        recentsLoading = false;
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
            News news;

            for (int i = 0; i < newsArrayList.size(); i++) {
                news = newsArrayList.get(i);

                new DrawSingleNewsAsyncTask(inflater,container,
                        news.getUuid(),news.getImage(),news.getTitle(),news.getAuthor_avatar(),
                        news.getAuthor_name(),news.getHtml(),news.getPublished_at())
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
//        if (sp.getBoolean(Constants.IS_TOOLBAR_CLICKED, true)){
//            enableToolbarIsClicked(false);
//            finish();
//            overridePendingTransition(0, 0);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setForegroundActivity(1);
        overridePendingTransition(0,0);
        //Update Pending Messages on Toolbar
        //RBM - It is done every time a message is received
        checkUnreadChatMessages();
        loadRecents();
    }

    @Subscribe
    public void onEventNewsReceived(NewsReceivedEvent event) {
        Log.e(Constants.TAG, "DashBoardActivity.onEventNewsReceived: ");
        final ArrayList<News> news = event.getNews();
        if(news != null) {
            if (newsArrayList==null || newsArrayList.size()==0) {
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
    public void onRecentContactsReceived(RecentContactsReceivedEvent event) {
        Log.e(Constants.TAG, "DashBoardActivity.onRecentContactsReceived: ");
        loadRecents();
    }

    public class DrawSingleNewsAsyncTask extends AsyncTask<Void,Void,Void>
    {
        LayoutInflater inflater;
        LinearLayout container;
        View child;
        Target target;
        String imageUrl;

        String titleStr;
        String dateStr;

        String uuid,image,title,author_avatar,author_name,html;
        long published_at;

        boolean loadFromDisk;
        File newsFile;
        ImageView newsImage;

        public DrawSingleNewsAsyncTask(LayoutInflater inflater, LinearLayout container,
                                       String uuid,String image,String title,String author_avatar,
                                       String author_name,String html,long published_at) {
            this.inflater = inflater;
            this.container = container;
            this.uuid = uuid;
            this.image = image;
            this.title = title;
            this.author_avatar = author_avatar;
            this.author_name = author_name;
            this.published_at = published_at;
            this.html = html;
            loadFromDisk = false;

        }

        @Override
        protected void onPreExecute() {
            child = inflater.inflate(R.layout.layout_news_dashboard, container, false);
            container.addView(child);
            child.setPadding(10, 20, 10, 20);
        }

        @Override
        protected Void doInBackground(Void... params) {

            newsImage = (ImageView) child.findViewById(R.id.notice_image);
            newsFile = new File(getFilesDir(), Constants.CONTACT_NEWS_DIR +
                    "news_"+uuid+".jpg");

            if (newsFile.exists()) {
                loadFromDisk = true;
            } else{
                //Download image
                if (image != null &&
                        image.length() > 0) {
                    imageUrl = "https://" + EndpointWrapper.getBaseNewsURL() + image;
                    File imagesDir = new File(getFilesDir() + Constants.CONTACT_NEWS_DIR);
                    if(!imagesDir.exists()) imagesDir.mkdirs();

                    target = new Target() {
                        @Override
                        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                            newsImage.setImageBitmap(bitmap);

                            SaveAndShowImageAsyncTask task =
                                    new SaveAndShowImageAsyncTask(
                                            newsImage, newsFile, bitmap);

                            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            if(newsFile.exists()) newsFile.delete();
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    };

                    newsImage.setTag(target);
                }
            }

            Long current = Calendar.getInstance().getTimeInMillis();
            final String detailImage = image;
            final String detailTitle = title;
            final String detailAvatar = author_avatar;
            final String detailAuthor = author_name;
            final String detailPublished = Utils.getShortStringTimeDifference(current - published_at);
            final String detailHtml = html;

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
                }
            });

            titleStr = title;
            dateStr = detailPublished;

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                final TextView title = (TextView) child.findViewById(R.id.notice_title);
                title.setText(titleStr);
                TextView date = (TextView) child.findViewById(R.id.notice_date);
                date.setText(dateStr);

                if (loadFromDisk)
                    Picasso.with(DashBoardActivity.this)
                            .load(newsFile)
                            .fit().centerInside()
                            .into(newsImage);
                else
                    Picasso.with(DashBoardActivity.this)
                            .load(imageUrl)
                            .into(target);
            } catch (Exception e) {
                Log.e(Constants.TAG, "DrawSingleNewsAsyncTask.onPostExecute: ",e);
            }
        }
    }

    public class DrawSingleRecentAsyncTask extends AsyncTask<Void,Void,Void>
    {

        String contactId,firstName,lastName,avatar,action,phones,emails,platform,recentId;
        LinearLayout recentsContainer;
        LayoutInflater inflater;
        ImageView recentAvatar;
        String profileId;
        View childRecents;

        //Avatar
        boolean loadAvatarFromDisk = false;
        File avatarFile = null;
        String nameInitials = null;
        TextView avatarText = null;
        Target avatarTarget;

        //Name
        String firstNameStr,lastNameStr;
        TextView firstNameView,lastNameView;

        // Action icon and badges
        TextView unread_messages;
        long pendingMsgsCount;
        ImageView typeRecent;

        public DrawSingleRecentAsyncTask(String contactId, String firstName, String lastName,
                                         String avatar, String action, String phones,
                                         String emails, String platform, String recentId,
                                         String profileId, LinearLayout recentsContainer,
                                         LayoutInflater inflater)
        {

            this.recentsContainer = recentsContainer;
            this.inflater = inflater;

            this.contactId = contactId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.avatar = avatar;
            this.action = action;
            this.phones = phones;
            this.emails = emails;
            this.platform = platform;
            this.profileId = profileId;
            this.recentId = recentId;
        }

        @Override
        protected void onPreExecute() {
            childRecents = inflater.inflate(R.layout.layout_recents_dashboard, recentsContainer, false);

            recentsContainer.addView(childRecents);
            childRecents.setPadding(10, 20, 10, 20);
            recentAvatar = (ImageView) childRecents.findViewById(R.id.recent_avatar);

            //Avatar
            avatarText = (TextView) childRecents.findViewById(R.id.avatarText);

            // Names
            firstNameView = (TextView) childRecents.findViewById(R.id.recent_firstname);
            lastNameView = (TextView) childRecents.findViewById(R.id.recent_lastname);

            //Action icon and badges
            unread_messages = (TextView) childRecents.findViewById(R.id.unread_messages);
            typeRecent = (ImageView) childRecents.findViewById(R.id.type_recent);
        }

        @Override
        protected Void doInBackground(Void... params) {
            avatarFile = new File(getFilesDir() + Constants.CONTACT_AVATAR_DIR,
                    "avatar_"+contactId+".jpg");

            //Avatar image
            if (avatarFile.exists()) { //From file if already exists
                loadAvatarFromDisk = true;
            } else {
                //Set name initials image during the download
                if (null != firstName && firstName.length() > 0) {
                    nameInitials = firstName.substring(0, 1);

                    if (null != lastName && lastName.length() > 0) {
                        nameInitials = nameInitials + lastName.substring(0, 1);
                    }

                }

                //Download avatar
                if (avatar != null &&
                        avatar.length() > 0 &&
                        !ConnectionsQueue.isConnectionAlive(avatarFile.toString())) {
                    File avatarsDir = new File(getFilesDir() + Constants.CONTACT_AVATAR_DIR);

                    if(!avatarsDir.exists()) avatarsDir.mkdirs();

                    avatarTarget = new Target() {
                        @Override
                        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                            recentAvatar.setImageBitmap(bitmap);
                            avatarText.setVisibility(View.INVISIBLE);

                            SaveAndShowImageAsyncTask task =
                                    new SaveAndShowImageAsyncTask(
                                            recentAvatar, avatarFile, bitmap, avatarText);

                            task.execute();
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            if(avatarFile.exists()) avatarFile.delete();
                            ConnectionsQueue.removeConnection(avatarFile.toString());
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    };
                    recentAvatar.setTag(avatarTarget);
                }
            }

            // Badges
            _realm = Realm.getInstance(DashBoardActivity.this);
            _chatTx = new RealmChatTransactions(_realm, DashBoardActivity.this);
            pendingMsgsCount = _chatTx.getChatPendingMessagesCount(contactId);

            LinearLayout btRecents = (LinearLayout) childRecents.findViewById(R.id.recent_content);

            btRecents.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        if (action.compareTo(Constants.CONTACTS_ACTION_CALL) == 0) {
                            String strPhones = phones;
                            if (strPhones != null)
                            {
                                String phone = strPhones;
                                if(!platform.equals(Constants
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
                            if(null != platform && platform.compareTo(Constants.PLATFORM_LOCAL)==0)
                            {
                                String phone = phones;
                                if(null != phone)
                                {
                                    Utils.launchSms(phone, DashBoardActivity.this);
                                }
                            }
                            else
                            {
                                Intent in = new Intent(DashBoardActivity.this, ChatMainActivity.class);
                                in.putExtra(Constants.CHAT_FIELD_CONTACT_ID, contactId);
                                in.putExtra(Constants.CHAT_PREVIOUS_VIEW, Constants.CHAT_VIEW_CONTACT_LIST);
                                startActivity(in);
                            }

                        }
                        else if (action.compareTo(Constants.CONTACTS_ACTION_EMAIL) == 0) {
                            String strEmails = emails;
                            if (strEmails != null)
                            {
                                String email = strEmails;
                                if(platform.compareTo(Constants.PLATFORM_LOCAL)!=0)
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
                        recentController.insertRecent(contactId, action);
                        //setListAdapterTabs();
                    } catch (Exception ex) {
                        Log.e(Constants.TAG, "DrawSingleRecentAsyncTask.onRecntItemClick: ",ex);
                    }
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                //Avatar
                if (loadAvatarFromDisk) {
                    Picasso.with(DashBoardActivity.this)
                            .load(avatarFile)
                            .fit().centerCrop()
                            .into(recentAvatar);
                } else if(avatarTarget!=null) {
                    recentAvatar.setImageResource(R.color.grey_middle);
                    avatarText.setText(nameInitials);

                    //Add this download to queue, to avoid duplicated downloads
                    ConnectionsQueue.putConnection(avatarFile.toString(), avatarTarget);
                    Picasso.with(DashBoardActivity.this)
                            .load(avatar)
                            .into(avatarTarget);
                }

                // Recent action icon and bagdes
                if (pendingMsgsCount > 0 && action.compareTo(Constants.CONTACTS_ACTION_SMS)==0) {
                    unread_messages.setVisibility(View.VISIBLE);
                    unread_messages.setText(String.valueOf(pendingMsgsCount));
                } else {
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

                // Names
                firstNameView.setText(firstName);
                lastNameView.setText(lastName);

                //Since it's finished, remove this task from queue
                recentsTasksQueue.removeConnection(recentId);

            }  catch (Exception e) {
                Log.e(Constants.TAG, "DrawSingleRecentAsyncTask.onPostExecute: ",e);
            }
        }
    }

}
