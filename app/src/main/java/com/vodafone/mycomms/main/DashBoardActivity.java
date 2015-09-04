package com.vodafone.mycomms.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.ContactListMainActivity;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.contacts.connection.RecentContactController;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ChatsReceivedEvent;
import com.vodafone.mycomms.events.DashboardCreatedEvent;
import com.vodafone.mycomms.events.GlobalContactsAddedEvent;
import com.vodafone.mycomms.events.GroupChatCreatedEvent;
import com.vodafone.mycomms.events.MessageStatusChanged;
import com.vodafone.mycomms.events.NewsReceivedEvent;
import com.vodafone.mycomms.events.RecentContactsReceivedEvent;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.NotificationMessages;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.util.UncaughtExceptionHandlerController;
import com.vodafone.mycomms.util.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import io.realm.Realm;
import model.News;

public class DashBoardActivity extends ToolbarActivity
{
    public RecentContactController recentContactController;
    public LinearLayout lay_no_connection;
    public DashBoardActivityController mDashBoardActivityController;
    public Realm realm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Constants.TAG, "DashBoardActivity.onCreate: ");

        //Exception handler
        Thread.setDefaultUncaughtExceptionHandler
                (
                        new UncaughtExceptionHandlerController()
                );

        SharedPreferences sp = getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        boolean isLocalContactsLoadingNeeded = sp.getBoolean(Constants.IS_LOCAL_CONTACTS_LOADING_ENABLED, false);
        this.realm = Realm.getDefaultInstance();

        String _profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");
        RealmContactTransactions realmContactTransactions = new RealmContactTransactions(_profileId);
        recentContactController = new RecentContactController(this, _profileId);

        BusProvider.getInstance().register(this);

        enableToolbarIsClicked(false);
        setContentView(R.layout.layout_dashboard);

        initALL();

        mDashBoardActivityController = new DashBoardActivityController
                (
                        DashBoardActivity.this
                        , this.realm
                        , realmContactTransactions
                        , _profileId
                        , this.recentContactController
                );

        BusProvider.getInstance().post(new DashboardCreatedEvent());

        lay_no_connection = (LinearLayout) findViewById(R.id.no_connection_layout);
        if(Utils.isConnected(DashBoardActivity.this))
            lay_no_connection.setVisibility(View.GONE);
        else
            lay_no_connection.setVisibility(View.VISIBLE);

        if(isLocalContactsLoadingNeeded)
            mDashBoardActivityController.loadLocalContacts();
    }

    private void initALL(){
        int sdk = Build.VERSION.SDK_INT;
        if (sdk < Build.VERSION_CODES.JELLY_BEAN_MR1) {

        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                // Set Time line
                //                DateFormat tf = new SimpleDateFormat("HH:mm");
                //                String time = tf.format(Calendar.getInstance().getTime());

                //                TextView timeText = (TextView) findViewById(R.id.timeDashboard);
                //                timeText.setText(time);

                // Set Date line
                DateFormat df = new SimpleDateFormat("EEEE, d MMMM", Locale.US);
                String date = df.format(Calendar.getInstance().getTime());

                TextView dateText = (TextView) findViewById(R.id.dateDashboard);
                dateText.setText(date);
            }
        });

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
                MycommsApp.contactViewOrigin = Constants.CONTACTS_ALL;
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
                MycommsApp.contactViewOrigin = Constants.CONTACTS_FAVOURITE;
                in.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(in);
//                finish();
            }
        });
    }



    //Prevent of going from main screen back to login
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(Constants.TAG, "DashBoardActivity.onDestroy: ");
        BusProvider.getInstance().unregister(this);
        if(null != this.realm)
            this.realm.close();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(Constants.TAG, "DashBoardActivity.onPause: ");
        SharedPreferences sp = getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setForegroundActivity(1);
        overridePendingTransition(0,0);
        checkUnreadChatMessages();
        mDashBoardActivityController.loadRecentContactsAndUnreadMessages();
        mDashBoardActivityController.loadNews();
        //Reset notifications inbox
        NotificationMessages.resetInboxMessages();
    }

    @Subscribe
    public void onEventNewsReceived(NewsReceivedEvent event) {
        Log.i(Constants.TAG, "DashBoardActivity.onEventNewsReceived: ");
        final ArrayList<News> news = event.getNews();
        if(news != null) {
            if (mDashBoardActivityController.newsArrayList==null || mDashBoardActivityController.newsArrayList.size()==0) {
                Log.i(Constants.TAG, "DashBoardActivity.onEventNewsReceived: FIRST LOAD");
                mDashBoardActivityController.drawNews(news);
            }
            initALL();
        }
    }

    @Subscribe
    public void onEventChatsReceived(ChatsReceivedEvent event) {
        Log.i(Constants.TAG, "DashBoardActivity.onEventChatsReceived: ");
        checkUnreadChatMessages();
        int pendingMessages = event.getPendingMessages();
        if (pendingMessages == 0)
        {
            mDashBoardActivityController.loadRecentContactsAndUnreadMessages();
        }
    }

    @Subscribe
     public void onEventMessageStatusChanged(MessageStatusChanged event){
        Log.i(Constants.TAG, "DashBoardActivity.onEventChatsReceived: ");
        checkUnreadChatMessages();
        mDashBoardActivityController.loadRecentContactsAndUnreadMessages();
    }

    @Subscribe
    public void onRecentContactsReceived(RecentContactsReceivedEvent event) {
        Log.i(Constants.TAG, "DashBoardActivity.onRecentContactsReceived: ");

        mDashBoardActivityController.loadRecentContactsAndUnreadMessages();
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

    @Subscribe
    public void onEventGroupChatCreated(GroupChatCreatedEvent event){
        Log.i(Constants.TAG, "DashBoardActivity.onEventGroupChatCreated: ");
        mDashBoardActivityController.loadRecentContactsAndUnreadMessages();
    }

    @Subscribe
    public void onGlobalContactsAddedEvent(GlobalContactsAddedEvent event)
    {
        Log.i(Constants.TAG, "DashBoardActivity.onGlobalContactsAddedEvent: ");
        recentContactController.getRecentList();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        MycommsApp.activityStarted();
    }

    @Override
    public void onStop()
    {
        MycommsApp.activityStopped();
        super.onStop();
    }
}
