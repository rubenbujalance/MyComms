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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.vodafone.mycomms.ContactListMainActivity;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chatgroup.GroupChatActivity;
import com.vodafone.mycomms.connection.AsyncTaskQueue;
import com.vodafone.mycomms.connection.ConnectionsQueue;
import com.vodafone.mycomms.contacts.connection.RecentContactController;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ChatsReceivedEvent;
import com.vodafone.mycomms.events.DashboardCreatedEvent;
import com.vodafone.mycomms.events.NewsReceivedEvent;
import com.vodafone.mycomms.events.RecentContactsReceivedEvent;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.realm.RealmNewsTransactions;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.AvatarSFController;
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
import java.util.Collections;
import java.util.HashMap;

import model.Contact;
import model.News;
import model.RecentContact;
import model.UserProfile;

public class DashBoardActivity extends ToolbarActivity
{
    private ArrayList<News> newsArrayList;
    private AsyncTaskQueue recentsTasksQueue = new AsyncTaskQueue();
    private boolean recentsLoading = false;
    private RealmContactTransactions realmContactTransactions;
    private String _profileId;
    private RealmNewsTransactions realmNewsTransactions;
    private RecentContactController recentContactController;
    private LinearLayout lay_no_connection;


    private LinearLayout recentsContainer, recentsContainer2;
    private boolean isCurrentRecentContainerFirst = true;
    private int numberOfRecents = 0;

    private HashMap<String, View> hashMapRecentIdView = new HashMap<>();



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Constants.TAG, "DashBoardActivity.onCreate: ");

        SharedPreferences sp = getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

        if(sp==null) {
            Log.e(Constants.TAG, "DashBoardActivity.onCreate: error loading Shared Preferences");
            finish();
        }
        
        _profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");
        realmContactTransactions = new RealmContactTransactions(_profileId);
        realmNewsTransactions = new RealmNewsTransactions();
        recentContactController = new RecentContactController(this, _profileId);



        BusProvider.getInstance().register(this);

        enableToolbarIsClicked(false);
        setContentView(R.layout.layout_dashboard);

        initALL();

        BusProvider.getInstance().post(new DashboardCreatedEvent());

        recentsContainer = (LinearLayout) findViewById(R.id.list_recents);
        recentsContainer2 = (LinearLayout) findViewById(R.id.list_recents_2);


        lay_no_connection = (LinearLayout) findViewById(R.id.no_connection_layout);
        if(APIWrapper.isConnected(DashBoardActivity.this))
            lay_no_connection.setVisibility(View.GONE);
        else
            lay_no_connection.setVisibility(View.VISIBLE);

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

        activateFooter();

        setFooterListeners(this);

        activateFooterSelected(Constants.TOOLBAR_DASHBOARD);

        // Event Click listeners
        ImageView btMagnifier = (ImageView) findViewById(R.id.magnifier);
        btMagnifier.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Start Contacts activity
                Constants.isSearchBarFocusRequested = true;
                Constants.isDashboardOrigin = true;
                Intent in = new Intent(DashBoardActivity.this, ContactListMainActivity.class);
                //in.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(in);
//                finish();
            }
        });

        LinearLayout btFavourite = (LinearLayout) findViewById(R.id.LayoutFavourite);
        btFavourite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Start Favourites activity
                Intent in = new Intent(DashBoardActivity.this, ContactListMainActivity.class);
                ((MycommsApp)getApplication()).comesFromToolbar = false;
                in.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(in);
//                finish();
            }
        });
    }

    private void loadRecents(LinearLayout currentRecentContainer){
        Log.i(Constants.TAG, "DashBoardActivity.loadRecents: ");
        if(recentsLoading) return;

        recentsLoading = true;

        try {
            ArrayList<RecentContact> recentList = new ArrayList<>();


            LayoutInflater inflater = LayoutInflater.from(this);
            currentRecentContainer.removeAllViews();
            recentList = realmContactTransactions.getAllRecentContacts();
            this.numberOfRecents = recentList.size();

            for (RecentContact contact: recentList)
            {

                if(contact.getId().startsWith("mg_"))
                {
                    DrawSingleGroupChatRecentAsyncTask task = new DrawSingleGroupChatRecentAsyncTask
                            (
                                    contact.getAction()
                                    , contact.getUniqueId()
                                    , currentRecentContainer
                                    , inflater
                                    , contact.getId()
                            );

                    recentsTasksQueue.putConnection(contact.getUniqueId(),task);
                    task.execute();
                }
                else
                {
                    DrawSingleRecentAsyncTask task = new DrawSingleRecentAsyncTask
                            (
                                    contact.getContactId()
                                    , contact.getFirstName()
                                    , contact.getLastName()
                                    , contact.getAvatar()
                                    , contact.getAction()
                                    , contact.getPhones()
                                    , contact.getEmails()
                                    , contact.getPlatform()
                                    , contact.getUniqueId()
                                    , currentRecentContainer
                                    , inflater
                            );

                    recentsTasksQueue.putConnection(contact.getUniqueId(),task);
                    task.execute();
                }
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "Load recents error: ",e);
        }

        recentsLoading = false;
    }

    private void loadNews() {
        Log.i(Constants.TAG, "DashBoardActivity.loadNews: ");

        newsArrayList = new ArrayList<>();
        newsArrayList = realmNewsTransactions.getAllNews();

        if(newsArrayList != null){
            drawNews(newsArrayList);
        }
    }

    private void loadLocalContacts(){
        Log.i(Constants.TAG, "DashBoardActivity.loadLocalContacts: ");
        ((MycommsApp)getApplication()).getLocalContacts();
    }

    private void drawNews(ArrayList<News> newsArrayList) {
        Log.i(Constants.TAG, "DashBoardActivity.drawNews: ");

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
        realmContactTransactions.closeRealm();
        realmNewsTransactions.closeRealm();
        recentContactController.closeRealm();
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

        if(isCurrentRecentContainerFirst)
            loadRecents(recentsContainer);
        else
            loadRecents(recentsContainer2);

        loadNews();
        loadLocalContacts();
    }

    @Subscribe
    public void onEventNewsReceived(NewsReceivedEvent event) {
        Log.i(Constants.TAG, "DashBoardActivity.onEventNewsReceived: ");
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
//        loadRecents();
    }

    @Subscribe
    public void onRecentContactsReceived(RecentContactsReceivedEvent event) {
        Log.i(Constants.TAG, "DashBoardActivity.onRecentContactsReceived: ");

        if(isCurrentRecentContainerFirst)
            loadRecents(recentsContainer);
        else
            loadRecents(recentsContainer2);
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
            imageUrl = "https://" + EndpointWrapper.getBaseNewsURL() + image;

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
                MycommsApp.picasso
                        .load(imageUrl)
                        .fit().centerCrop()
                        .into(newsImage, new Callback() {
                            @Override
                            public void onSuccess() {
                            }
                            @Override
                            public void onError() {
                                newsImage.setImageResource(R.color.grey_middle);
                            }
                        });
            } catch (Exception e) {
                Log.e(Constants.TAG, "DrawSingleNewsAsyncTask.onPostExecute: ",e);
            }
        }
    }

    public class DrawSingleGroupChatRecentAsyncTask extends AsyncTask<Void,Void,String>
    {

        String contactId,action,recentId;
        LinearLayout recentsContainer;
        LayoutInflater inflater;

        ImageView top_left_avatar, top_right_avatar, bottom_left_avatar, bottom_right_avatar;
        TextView top_left_avatar_text, top_right_avatar_text, bottom_left_avatar_text, bottom_right_avatar_text;
        LinearLayout lay_top_right_image, layout_bottom_both_images, lay_main_container;

        View childRecents;

        //Avatar
        File avatarFile = null;
        String nameInitials = null;
        Target avatarTarget;

        //Name
        TextView firstNameView;

        // Action icon and badges
        TextView unread_messages;
        long pendingMsgsCount;
        ImageView typeRecent;


        ArrayList<String> contactIds = new ArrayList<>();
        ArrayList<Contact> contacts = new ArrayList<>();
        String groupChatId;
        ArrayList<ImageView> images = new ArrayList<>();

        HashMap<ImageView,TextView> mapAvatarImageAndText = new HashMap<>();

        public DrawSingleGroupChatRecentAsyncTask
                (
                        String action
                        , String recentId
                        , LinearLayout recentsContainer
                        , LayoutInflater inflater
                        , String groupChatId
                )
        {

            this.recentsContainer = recentsContainer;
            this.inflater = inflater;
            this.action = action;
            this.recentId = recentId;
            this.groupChatId = groupChatId;
            RealmGroupChatTransactions realmGroupChatTransactions = new
                    RealmGroupChatTransactions(DashBoardActivity.this, _profileId);
            this.contactId = realmGroupChatTransactions.getGroupChatById(groupChatId).getMembers();

            String[] ids = contactId.split("@");
            Collections.addAll(contactIds, ids);
        }

        private void loadContactsFromIds
                (
                        ArrayList<String> ids
                )
        {
            RealmContactTransactions realmContactTransactions =
                    new RealmContactTransactions(_profileId);
            UserProfile userProfile = realmContactTransactions.getUserProfile();
            Contact contact = new Contact();
            contact.setAvatar(userProfile.getAvatar());
            contact.setFirstName(userProfile.getFirstName());
            contact.setLastName(userProfile.getLastName());
            contact.setContactId(userProfile.getId());
            contact.setPlatform(userProfile.getPlatform());
            contacts.add(contact);
            for(String id : ids)
            {
                if(!id.equals(userProfile.getId()))
                {
                    contact = realmContactTransactions.getContactById(id);
                    if(null != contact)
                        contacts.add(contact);
                }
            }
        }


        @Override
        protected void onPreExecute()
        {
            childRecents = inflater.inflate(R.layout.layout_group_chat_recents_dashboard, recentsContainer, false);

            recentsContainer.addView(childRecents);
            hashMapRecentIdView.put(this.groupChatId,childRecents);

            childRecents.setPadding(10, 20, 10, 20);

            top_left_avatar = (ImageView) childRecents.findViewById(R.id.top_left_avatar);
            top_right_avatar = (ImageView) childRecents.findViewById(R.id.top_right_avatar);
            bottom_left_avatar = (ImageView) childRecents.findViewById(R.id.bottom_left_avatar);
            bottom_right_avatar = (ImageView) childRecents.findViewById(R.id.bottom_right_avatar);

            top_left_avatar_text = (TextView) childRecents.findViewById(R.id.top_left_avatar_text);
            top_right_avatar_text = (TextView) childRecents.findViewById(R.id.top_right_avatar_text);
            bottom_left_avatar_text = (TextView) childRecents.findViewById(R.id.bottom_left_avatar_text);
            bottom_right_avatar_text = (TextView) childRecents.findViewById(R.id.bottom_right_avatar_text);

            firstNameView = (TextView) childRecents.findViewById(R.id.group_chat_recent_first_name);

            lay_top_right_image = (LinearLayout) childRecents.findViewById(R.id
                    .lay_top_right_image_hide);
            layout_bottom_both_images = (LinearLayout) childRecents.findViewById(R.id
                    .lay_bottom_both_image_hide);
            layout_bottom_both_images.setVisibility(View.VISIBLE);

            lay_main_container = (LinearLayout) childRecents.findViewById(R.id.recent_content);

            if(contactIds.size() == 3)
                lay_top_right_image.setVisibility(View.GONE);
            else
                lay_top_right_image.setVisibility(View.VISIBLE);

            //Action icon and badges
            unread_messages = (TextView) childRecents.findViewById(R.id.unread_messages);
            typeRecent = (ImageView) childRecents.findViewById(R.id.type_recent);

            images.add(top_left_avatar);
            images.add(bottom_right_avatar);
            images.add(bottom_left_avatar);
            if(contactIds.size() > 3)
                images.add(top_right_avatar);

            mapAvatarImageAndText.put(top_left_avatar, top_left_avatar_text);
            mapAvatarImageAndText.put(top_right_avatar, top_right_avatar_text);
            mapAvatarImageAndText.put(bottom_left_avatar, bottom_left_avatar_text);
            mapAvatarImageAndText.put(bottom_right_avatar, top_right_avatar_text);
        }

        @Override
        protected String doInBackground(Void... params) {
            if (null != contactIds && contactIds.size() >= 3)
            {
                try
                {
                    LinearLayout btRecents = (LinearLayout) childRecents.findViewById(R.id.recent_content);
                    btRecents.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            try {
                                if (action.compareTo(Constants.CONTACTS_ACTION_SMS) == 0) {
                                    Intent in = new Intent(DashBoardActivity.this, GroupChatActivity.class);
                                    in.putExtra(Constants.GROUP_CHAT_ID, groupChatId);
                                    in.putExtra(Constants.CHAT_PREVIOUS_VIEW, "DashBoardActivity");
                                    in.putExtra(Constants.IS_GROUP_CHAT, true);
                                    startActivity(in);
                                }

                                RecentContactController recentContactController = new
                                        RecentContactController(DashBoardActivity.this, _profileId);
                                recentContactController.insertRecentOKHttp(groupChatId, Constants.CONTACTS_ACTION_SMS);

                            } catch (Exception ex) {
                                Log.e(Constants.TAG, "DrawSingleRecentAsyncTask.onRecntItemClick: ", ex);
                            }
                        }
                    });

                    return Integer.toString(this.contactIds.size());
                } catch (Exception e) {
                    Log.e(Constants.TAG, "DrawSingleGroupChatRecentAsyncTask.mapAvatarToContactId: ", e);
                    Crashlytics.logException(e);
                    return null;
                }
            }
            return Integer.toString(this.contactIds.size());
        }

        @Override
        protected void onPostExecute(String result)
        {
            if(result==null) return;

            if(null != contactIds && contactIds.size() >= 3)
            {
                contacts = new ArrayList<>();
                loadContactsFromIds(contactIds);

                int i = 0;
                for(final ImageView image : images)
                {
                    try
                    {
                        Contact contact = this.contacts.get(i);
                        i++;
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
                        mapAvatarImageAndText.get(image).setVisibility(View.VISIBLE);
                        mapAvatarImageAndText.get(image).setText(finalInitials);
                        mapAvatarImageAndText.get(image).setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

                        if (contact.getAvatar()!=null &&
                                contact.getAvatar().length()>0)
                        {
                            if (contact.getContactId().equals(_profileId) || contact.getPlatform()
                                .equalsIgnoreCase
                                    (Constants
                                    .PLATFORM_MY_COMMS)
                                    || contact.getPlatform().equalsIgnoreCase(Constants.PLATFORM_LOCAL)) {
                                MycommsApp.picasso
                                        .load(contact.getAvatar())
                                        .placeholder(R.color.grey_middle)
                                        .noFade()
                                        .fit().centerCrop()
                                        .into(image, new Callback() {
                                            @Override
                                            public void onSuccess() {
                                                mapAvatarImageAndText.get(image).setVisibility(View.INVISIBLE);
                                            }

                                            @Override
                                            public void onError() {
                                                image.setImageResource(R.color.grey_middle);
                                                mapAvatarImageAndText.get(image).setVisibility(View.VISIBLE);
                                                mapAvatarImageAndText.get(image).setText(finalInitials);
                                            }
                                        });
                            }
                            else if (contact.getPlatform().equalsIgnoreCase(Constants.PLATFORM_SALES_FORCE))
                            {
                                AvatarSFController avatarSFController = new AvatarSFController
                                        (
                                                DashBoardActivity.this
                                                , image
                                                , mapAvatarImageAndText.get(image)
                                                , contact.getContactId()
                                        );
                                avatarSFController.getSFAvatar(contact.getAvatar());
                            }
                        }
                        else
                        {
                            image.setImageResource(R.color.grey_middle);
                            mapAvatarImageAndText.get(image).setText(initials);
                        }
                    }
                    catch (Exception e)
                    {
                        Log.e(Constants.TAG, "DrawSingleRecentAsyncTask.onPostExecute: ",e);
                    }
                }

                pendingMsgsCount = getRealmGroupChatTransactions().getGroupChatPendingMessagesCount(groupChatId);

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
                firstNameView.setText("Group(" + contacts.size() + ")");
                //Since it's finished, remove this task from queue
                recentsTasksQueue.removeConnection(recentId);
                lay_main_container.setVisibility(View.VISIBLE);
            }

            numberOfRecents --;
            if(numberOfRecents == 0)
                loadRecentLayout();
        }
    }


    public class DrawSingleRecentAsyncTask extends AsyncTask<Void,Void,Void>
    {

        String contactId,firstName,lastName,avatar,action,phones,emails,platform,recentId;
        LinearLayout recentsContainer;
        LayoutInflater inflater;
        ImageView recentAvatar;
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
                                         LinearLayout recentsContainer, LayoutInflater inflater)
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
            this.recentId = recentId;
        }

        @Override
        protected void onPreExecute() {
            childRecents = inflater.inflate(R.layout.layout_recents_dashboard, recentsContainer, false);

            recentsContainer.addView(childRecents);
            hashMapRecentIdView.put(this.contactId,childRecents);

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
        protected Void doInBackground(Void... params)
        {
            avatarFile = new File(getFilesDir() + Constants.CONTACT_AVATAR_DIR,
                    "avatar_"+contactId+".jpg");

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
                    platform.equalsIgnoreCase(Constants.PLATFORM_MY_COMMS))
            {
                File avatarsDir = new File(getFilesDir() + Constants.CONTACT_AVATAR_DIR);

                if (!avatarsDir.exists()) avatarsDir.mkdirs();

                avatarTarget = new Target() {
                    @Override
                    public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                        recentAvatar.setImageBitmap(bitmap);
                        avatarText.setVisibility(View.INVISIBLE);

                        SaveAndShowImageAsyncTask task =
                                new SaveAndShowImageAsyncTask(
                                        recentAvatar, avatarFile, bitmap, avatarText);

                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
            else if (avatar != null &&
                    avatar.length() > 0 &&
                    !ConnectionsQueue.isConnectionAlive(avatarFile.toString())
                    && platform.equalsIgnoreCase(Constants.PLATFORM_SALES_FORCE)) {
                AvatarSFController avatarSFController = new AvatarSFController(getBaseContext(), recentAvatar, avatarText, contactId);
                avatarSFController.getSFAvatar(avatar);
            }

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
                                Intent in = new Intent(DashBoardActivity.this, GroupChatActivity.class);
                                in.putExtra(Constants.CHAT_FIELD_CONTACT_ID, contactId);
                                in.putExtra(Constants.CHAT_PREVIOUS_VIEW, "DashBoardActivity");
                                in.putExtra(Constants.IS_GROUP_CHAT, false);
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
                        recentContactController.insertRecent(contactId, action);
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
            try
            {
                //RBM - NEW Avatar management ****************************
                recentAvatar.setImageResource(R.color.grey_middle);
                avatarText.setVisibility(View.VISIBLE);
                avatarText.setText(nameInitials);
                avatarText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                if (avatar != null &&
                    avatar.length() > 0)
                {
                    MycommsApp.picasso
                            .load(avatar)
                            .placeholder(R.color.grey_middle)
                            .noFade()
                            .fit().centerCrop()
                            .into(recentAvatar, new Callback() {
                                @Override
                                public void onSuccess() {
                                    avatarText.setVisibility(View.INVISIBLE);
                                }

                                @Override
                                public void onError() {
                                    recentAvatar.setImageResource(R.color.grey_middle);
                                    avatarText.setVisibility(View.VISIBLE);
                                    avatarText.setText(nameInitials);
                                }
                            });

                }

                //********************************************************
                //TODO: Check if this code is necessary
                //Local avatar
                if (avatar != null &&
                        avatar.length() > 0 &&
                        platform.equalsIgnoreCase(Constants.PLATFORM_LOCAL)) {
                    Picasso.with(DashBoardActivity.this)
                            .load(avatar)
                            .fit().centerCrop()
                            .into(recentAvatar);
                } else if  (platform.equalsIgnoreCase(Constants.PLATFORM_LOCAL) &&
                        avatar == null ||
                        avatar.length() < 0) {
                    recentAvatar.setImageResource(R.color.grey_middle);
                    avatarText.setText(nameInitials);
                }

                // Badges
                pendingMsgsCount = getRealmChatTransactions().getChatPendingMessagesCount(contactId);

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

            numberOfRecents --;
            if(numberOfRecents == 0)
                loadRecentLayout();
        }
    }

    @Subscribe
    public void onConnectivityChanged(ConnectivityChanged event)
    {

        Log.i(Constants.TAG, "DashBoardActivity.onConnectivityChanged: "
                + event.getConnectivityStatus().toString());
        if(event.getConnectivityStatus()!= ConnectivityStatus.MOBILE_CONNECTED &&
                event.getConnectivityStatus()!=ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET)
            lay_no_connection.setVisibility(View.VISIBLE);
        else
            lay_no_connection.setVisibility(View.GONE);
    }

    private void loadRecentLayout()
    {
        if(isCurrentRecentContainerFirst)
        {
            isCurrentRecentContainerFirst = false;
            recentsContainer2.setVisibility(View.GONE);
            recentsContainer.setVisibility(View.VISIBLE);
        }
        else
        {
            isCurrentRecentContainerFirst = true;
            recentsContainer.setVisibility(View.GONE);
            recentsContainer2.setVisibility(View.VISIBLE);
        }
    }

    private void loadUnreadMessages()
    {

    }





}
