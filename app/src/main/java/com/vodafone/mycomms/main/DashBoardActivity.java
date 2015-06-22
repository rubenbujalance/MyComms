package com.vodafone.mycomms.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.vodafone.mycomms.ContactListMainActivity;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chatlist.view.ChatListHolder;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.InitNews;
import com.vodafone.mycomms.events.InitProfileAndContacts;
import com.vodafone.mycomms.events.RefreshNewsEvent;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmNewsTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.util.Utils;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import io.realm.Realm;
import model.News;
import model.RecentContact;

public class DashBoardActivity extends ToolbarActivity{
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private LinearLayout noConnectionLayout;
    private Context mContext;
    private Realm _realm;
    private Realm mRealm;
    private RealmChatTransactions _chatTx;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Constants.TAG, "DashBoardActivity.onCreate: ");

        BusProvider.getInstance().register(this);

        setContentView(R.layout.layout_dashboard);
        initALL();
        BusProvider.getInstance().post(new InitNews());
        BusProvider.getInstance().post(new InitProfileAndContacts());

        mRealm = Realm.getInstance(getBaseContext());
        loadRecents();
        loadNews();
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
        Log.i(Constants.TAG, "DashBoardActivity.loadRecents: ");
        try {
            SharedPreferences sp = getSharedPreferences(
                    Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
            String profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");

            ArrayList<RecentContact> recentList = new ArrayList<>();

            RealmContactTransactions realmContactTransactions = new RealmContactTransactions(mRealm, profileId);
            recentList = realmContactTransactions.getAllRecentContacts();

            LinearLayout recentsContainer = (LinearLayout) findViewById(R.id.list_recents);
            LayoutInflater inflater = LayoutInflater.from(this);

            for (int i = 0; i < recentList.size(); i++) {
                View childrecents = inflater.inflate(R.layout.layout_recents_dashboard, recentsContainer, false);

                recentsContainer.addView(childrecents);
                childrecents.setPadding(10,20,10,20);

                ImageView recentAvatar = (ImageView) childrecents.findViewById(R.id.recent_avatar);
                File avatarFile = new File(getFilesDir(), Constants.CONTACT_AVATAR_DIR +
                        "avatar_"+recentList.get(i).getContactId()+".jpg");

                if (avatarFile.exists()) {
                    Picasso.with(this)
                            .load(avatarFile)
                            .fit().centerCrop()
                            .into(recentAvatar);
                } else {
                    String initials = "";
                    if(null != recentList.get(i).getFirstName() && recentList.get(i).getFirstName().length() > 0)
                    {
                        initials = recentList.get(i).getFirstName().substring(0,1);

                        if(null != recentList.get(i).getLastName() && recentList.get(i).getLastName().length() > 0)
                        {
                            initials = initials + recentList.get(i).getLastName().substring(0,1);
                        }

                    }
                    TextView avatarText = (TextView) childrecents.findViewById(R.id.avatarText);
                    recentAvatar.setImageResource(R.color.grey_middle);
                    avatarText.setText(initials);
                }

                TextView firstName = (TextView) childrecents.findViewById(R.id.recent_firstname);
                firstName.setText(recentList.get(i).getFirstName());

                TextView lastName = (TextView) childrecents.findViewById(R.id.recent_lastname);
                lastName.setText(recentList.get(i).getLastName());

                // Badges
                _realm = Realm.getInstance(this);
                _chatTx = new RealmChatTransactions(_realm, this);

                ChatListHolder chatHolder = new ChatListHolder(childrecents);

                long count =_chatTx.getChatPendingMessagesCount(recentList.get(i).getContactId());

                if(count > 0) {
                    TextView unread_messages = (TextView) childrecents.findViewById(R.id.unreaded_messages);
                    unread_messages.setVisibility(View.VISIBLE);
                    unread_messages.setText(String.valueOf(count));
                } else {
                    ImageView typeRecent = (ImageView) childrecents.findViewById(R.id.type_recent);
                    typeRecent.setVisibility(View.VISIBLE);

                    String action = recentList.get(i).getAction();
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
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "Load recents error: " + e);
        }
    }


    private void loadNews() {
        ArrayList<News> newsArrayList = new ArrayList<>();

        RealmNewsTransactions realmNewsTransactions = new RealmNewsTransactions(mRealm);
        newsArrayList = realmNewsTransactions.getAllNews();
        //TODO: REFORMAR UN COP FUNCIONI

        if(newsArrayList != null){
            try {
                LinearLayout container = (LinearLayout) findViewById(R.id.list_news);
                LayoutInflater inflater = LayoutInflater.from(this);

                for (int i = 0; i < newsArrayList.size(); i++) {
                    View child = inflater.inflate(R.layout.layout_news_dashboard, container, false);

                    container.addView(child);
                    child.setPadding(10, 20, 10, 20);

                    ImageView newsImage = (ImageView) child.findViewById(R.id.notice_image);
                    Picasso.with(this)
                            .load("https://" + EndpointWrapper.getBaseNewsURL() + newsArrayList.get(i).getImage())
                                    //.resize(300,300)
                                    //.centerInside()
                            .fit().centerInside()
                            .into(newsImage);

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
                Log.e(Constants.TAG, "Load news error: " + e);
            }
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
        BusProvider.getInstance().unregister(this);

        // Disconnect from the XMPP server
        XMPPTransactions.disconnectMsgServerSession();
        mRealm.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        XMPPTransactions.initializeMsgServerSession(getApplicationContext());
    }

    @Subscribe
    public void onEventNewsReceived(RefreshNewsEvent event){
        final ArrayList<News> news = event.getNews();
        if(news != null)
        {
            try {

                LinearLayout container = (LinearLayout) findViewById(R.id.list_news);
                LayoutInflater inflater = LayoutInflater.from(this);

                for (int i = 0; i < news.size(); i++) {
                    View child = inflater.inflate(R.layout.layout_news_dashboard, container, false);

                    container.addView(child);
                    child.setPadding(10, 20, 10, 20);

                    ImageView newsImage = (ImageView) child.findViewById(R.id.notice_image);
                    Picasso.with(this)
                            .load("https://" + EndpointWrapper.getBaseNewsURL() + news.get(i).getImage())
                            //.resize(300,300)
                            //.centerInside()
                            .fit().centerInside()
                            .into(newsImage);

                    final TextView title = (TextView) child.findViewById(R.id.notice_title);
                    title.setText(news.get(i).getTitle());

                    TextView date = (TextView) child.findViewById(R.id.notice_date);
                    Long current = Calendar.getInstance().getTimeInMillis();
                    date.setText(Utils.getShortStringTimeDifference(current - news.get(i).getPublished_at()));

                    final String detailImage = news.get(i).getImage();
                    final String detailTitle = news.get(i).getTitle();
                    final String detailAvatar = news.get(i).getAuthor_avatar();
                    final String detailAuthor = news.get(i).getAuthor_name();
                    final String detailPublished = Utils.getShortStringTimeDifference(current - news.get(i).getPublished_at());
                    final String detailHtml = news.get(i).getHtml();

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
                initALL();
            } catch (Exception e) {
                Log.e(Constants.TAG, "Load news error: " + e);
            }
        }
    }
}
