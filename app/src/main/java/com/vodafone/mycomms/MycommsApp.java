package com.vodafone.mycomms;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.NetworkEvents;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.contacts.connection.ContactsController;
import com.vodafone.mycomms.contacts.connection.FavouriteController;
import com.vodafone.mycomms.contacts.connection.RecentContactController;
import com.vodafone.mycomms.events.AllPendingMessagesReceivedEvent;
import com.vodafone.mycomms.events.ApplicationAndProfileInitialized;
import com.vodafone.mycomms.events.ApplicationAndProfileReadError;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ChatsReceivedEvent;
import com.vodafone.mycomms.events.ContactListReceivedEvent;
import com.vodafone.mycomms.events.DashboardCreatedEvent;
import com.vodafone.mycomms.events.NewsImagesReceivedEvent;
import com.vodafone.mycomms.events.NewsReceivedEvent;
import com.vodafone.mycomms.main.connection.INewsConnectionCallback;
import com.vodafone.mycomms.main.connection.NewsController;
import com.vodafone.mycomms.realm.RealmDBMigration;
import com.vodafone.mycomms.realm.RealmProfileTransactions;
import com.vodafone.mycomms.settings.ProfileController;
import com.vodafone.mycomms.settings.connection.FilePushToServerController;
import com.vodafone.mycomms.settings.connection.IProfileConnectionCallback;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UserSecurity;
import com.vodafone.mycomms.util.Utils;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import model.ChatMessage;
import model.News;
import model.UserProfile;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by str_rbm on 02/04/2015.
 *
 * Main application singleton class
 * It handles global data and backend services
 */

public class MycommsApp extends Application implements IProfileConnectionCallback, INewsConnectionCallback {

    private ProfileController profileController;
    private Context mContext;
    private static SharedPreferences sp;
    public static boolean appIsInitialized = false;
    private RecentContactController recentContactController;
    private NewsController mNewsController;
    private String profile_id;
    public static int contactViewOrigin = Constants.CONTACTS_ALL;
    public static Picasso picasso;
    private HashMap<String, Long> recentChatsHashMap = new HashMap<>();

    //Network listener
    private NetworkEvents networkEvents;
    private Realm realm;

    //Idle listeners
    private int lastInteractionTime;
    private Boolean isScreenOff = false;

    //Activity counter
    public static int stateCounter;
    private boolean countdownOn = false;
    public static boolean disconnectedProcess = false;
    private long startTime = 0;
    private long currentTime = 0;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(Constants.TAG, "MycommsApp.onCreate: ");

        //Initialize Crashlytics
        Fabric.with(getApplicationContext(), new Crashlytics());

        //Realm config

        //TODO WARNING - Change version when Realm DB schema changes
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(getApplicationContext())
                .name("mycomms.realm")
//                .encryptionKey()
                .schemaVersion(1)
                .migration(new RealmDBMigration())
                .build();

        Realm.setDefaultConfiguration(realmConfig);

        //Shared Preferences
        sp = getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        //Store if local contacts loading is needed
        if(!sp.contains(Constants.IS_LOCAL_CONTACTS_LOADING_ENABLED))
            sp.edit().putBoolean(Constants.IS_LOCAL_CONTACTS_LOADING_ENABLED, false).apply();
        //Store if global contacts loading is needed
        if(!sp.contains(Constants.IS_GLOBAL_CONTACTS_LOADING_ENABLED))
            sp.edit().putBoolean(Constants.IS_GLOBAL_CONTACTS_LOADING_ENABLED, false).apply();
        //Profile id
        if (sp.contains(Constants.PROFILE_ID_SHARED_PREF))
            profile_id = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);

        //Picasso configuration
        Downloader downloader = new OkHttpDownloader(getApplicationContext(), Long.MAX_VALUE);
        Picasso.Builder builder = new Picasso.Builder(getApplicationContext());
        builder.downloader(downloader);

        picasso = builder.build();

//        OkHttpClient picassoClient = new OkHttpClient();
//
//        picassoClient.interceptors().add(new Interceptor() {
//            @Override
//            public Response intercept(Chain chain) throws IOException {
//                Request newRequest = chain.request().newBuilder()
//                        .addHeader(Constants.API_HTTP_HEADER_VERSION,
//                                Utils.getHttpHeaderVersion(mContext))
//                        .addHeader(Constants.API_HTTP_HEADER_CONTENTTYPE,
//                                Utils.getHttpHeaderContentType())
//                        .addHeader(Constants.API_HTTP_HEADER_AUTHORIZATION,
//                                Utils.getHttpHeaderAuth(mContext))
//                        .build();
//                return chain.proceed(newRequest);
//            }
//        });
//
//        picasso = new Picasso.Builder(getApplicationContext()).downloader(new OkHttpDownloader(picassoClient)).build();

        //**********************

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath(getResources().getString(R.string.font_name_source_sans_pro))
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        mNewsController = new NewsController(getApplicationContext());

        //Initializations
        BusProvider.getInstance().register(this);
        mContext = getApplicationContext();

        //Network listener
        networkEvents = new NetworkEvents(this, BusProvider.getInstance());

        try {
            networkEvents.register();
        } catch (Exception ex) {
            Log.e(Constants.TAG, "MycommsApp.onCreate: ",ex);
            Crashlytics.logException(ex);
        }

        startUserInactivityDetectThread(); // start the thread to detect inactivity
        new ScreenReceiver();  // creating receive SCREEN_OFF and SCREEN_ON broadcast msgs from the device.

        //Variable that counts how many activities are open
        stateCounter = 0;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        BusProvider.getInstance().unregister(this);
        if(realm!=null) realm.close();
        //Network listener
        try {
            networkEvents.unregister();
        }
        catch (Exception e) {}

    }

    @Subscribe
    public void onConnectivityChanged(ConnectivityChanged event) {
        Log.i(Constants.TAG, "MycommsApp.onConnectivityChanged: "
                + event.getConnectivityStatus().toString());
        if(event.getConnectivityStatus()==ConnectivityStatus.MOBILE_CONNECTED ||
                event.getConnectivityStatus()==ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET)
        {
//            XMPPTransactions.initializeMsgServerSession(getApplicationContext());
        }
    }

    public void getProfileIdAndAccessToken() {
        Log.i(Constants.TAG, "MycommsApp.getProfileIdAndAccessToken: ");
        profileController = new ProfileController(mContext);

        //Save profile_id if accessToken has changed
        String profile_id = validateAccessToken();

        if(profile_id!=null)
            BusProvider.getInstance().post(new ApplicationAndProfileInitialized());
    }

    private String validateAccessToken(){
        Log.i(Constants.TAG, "MycommsApp.validateAccessToken: ");
        String accessToken = UserSecurity.getAccessToken(this);

        String prefAccessToken = sp.getString(Constants.ACCESS_TOKEN_SHARED_PREF, "");

        if (prefAccessToken==null || prefAccessToken.equals("") ||
                !prefAccessToken.equals(accessToken) ||
                sp.getString(Constants.PROFILE_ID_SHARED_PREF, "")==null ||
                sp.getString(Constants.PROFILE_ID_SHARED_PREF, "").length()==0){
            profileController.setConnectionCallback(this);
            if(realm == null) this.realm = Realm.getDefaultInstance();
            profileController.getProfile(this.realm);

            return null;
        }
        else {
            String profile = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");
            //new DownloadContactsAsyncTask().execute(this);
            return profile;
        }
    }

    public static boolean isProfileAvailable()
    {
        try {
            return sp.contains(Constants.PROFILE_ID_SHARED_PREF);

        } catch(Exception e) {
            Log.e(Constants.TAG, "SplashScreenActivity.isProfileAvailable: ",e);
            Crashlytics.logException(e);
            return false;
        }
    }

    private String setDeviceId(){
        Log.i(Constants.TAG, "MycommsApp.setDeviceId: ");
        TelephonyManager telephonyManager = (TelephonyManager
                )getSystemService( Context.TELEPHONY_SERVICE );
        String deviceId = Utils.getDeviceId(getContentResolver(), telephonyManager);
        SharedPreferences sp = getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Constants.DEVICE_ID_SHARED_PREF, deviceId);
        editor.apply();

        return deviceId;
    }

//    public void getLocalContacts(){
//        SharedPreferences sp = getSharedPreferences(
//                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
//        String profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");
//        DownloadLocalContacts downloadLocalContacts =
//                new DownloadLocalContacts(MycommsApp.this, profileId, false);
//        downloadLocalContacts.downloadAndStore();
//    }

    @Override
    public void onProfileReceived(UserProfile userProfile) {
        Log.i(Constants.TAG, "MycommsApp.onProfileReceived: ");

        String timeZone = TimeZone.getDefault().getID();

        if (timeZone.equals(userProfile.getTimezone())) {
            profileController.setUserProfile(userProfile.getId(),
                    userProfile.getFirstName() + " " + userProfile.getLastName(),
                    userProfile.getPlatforms(),
                    userProfile.getTimezone());
        } else{
            profileController.setUserProfile(userProfile.getId(),
                    userProfile.getFirstName() + " " + userProfile.getLastName(),
                    userProfile.getPlatforms(),
                    timeZone);

            profileController.updateProfileTimezone(timeZone);
        }

        //Always call timezone update, just to assure we send the deviceId
        HashMap<String, String> body = new HashMap<>();

        if(timeZone!=null && timeZone.length()>0)
            body.put("timeZone", timeZone);

        profileController.updateTimeZone(body);

        // Profile loaded
        // Notify application to initialize everything
        if(!appIsInitialized)
            BusProvider.getInstance().post(new ApplicationAndProfileInitialized());
    }

    @Override
    public void onProfileConnectionError() {
        Log.e(Constants.TAG, "MycommsApp.onProfileConnectionError: ");

        // Connection error
        // Notify application
        BusProvider.getInstance().post(new ApplicationAndProfileReadError());
    }

    @Override
    public void onUpdateProfileConnectionError() {

    }

    @Override
    public void onUpdateProfileConnectionCompleted() {

    }

    @Override
    public void onPasswordChangeError(String error) {
        Log.e(Constants.TAG, "MycommsApp.onPasswordChangeError: ");
    }

    @Override
    public void onPasswordChangeCompleted() {

    }

    @Override
    public void onConnectionNotAvailable() {
        Log.e(Constants.TAG, "MycommsApp.onConnectionNotAvailable: ");
    }

    @Override
    public void onNewsResponse(ArrayList<News> newsList) {
        Log.i(Constants.TAG, "MyCommsApp.onNewsResponse: ");
        NewsReceivedEvent event = new NewsReceivedEvent();
        event.setNews(newsList);
        BusProvider.getInstance().post(event);
    }

    @Subscribe
    public void onApplicationAndProfileInitialized(ApplicationAndProfileInitialized event)
    {
        Log.i(Constants.TAG, "MycommsApp.onApplicationAndProfileInitialized: ");

        appIsInitialized = true;

        //Check if sign up avatar is pending to upload
        profile_id = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);
        if(sp.getBoolean(Constants.FIRST_TIME_AVATAR_DELIVERY,false))
        {
            if(profile_id!=null)
                new sendAvatar().execute(profile_id);
        }

        if(profile_id!=null) {
            //Set crashlytics user info
            Crashlytics.setUserIdentifier(profile_id);

            try {
                if(realm == null) realm = Realm.getDefaultInstance();
                RealmProfileTransactions ptx = new RealmProfileTransactions();
                UserProfile userProfile = ptx.getUserProfile(profile_id, realm);
                Crashlytics.setUserName(userProfile.getFirstName()+" "+userProfile.getLastName());
                Crashlytics.setUserEmail(userProfile.getEmails().split(";")[0]);
            } catch (Exception e) {
                Log.e(Constants.TAG, "MycommsApp.onApplicationAndProfileInitialized: ");
            }
        }
    }

    @Subscribe
    public void onDashboardCreatedEvent(DashboardCreatedEvent event)
    {
        Log.i(Constants.TAG, "MycommsApp.onDashboardCreatedEvent: ");

        String profile_id = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);
        recentContactController = new RecentContactController(this, profile_id);
        recentContactController.getRecentListWithPreviousLDAPAuthentication();
        getNews();

        XMPPTransactions.checkAndReconnectXMPP(getApplicationContext());
    }

    @Subscribe
    public void onEventNewsReceived(NewsReceivedEvent event) {
        Log.i(Constants.TAG, "MyCommsApp.onEventNewsReceived: ");
        String profile_id = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);
        ContactsController contactsController = new ContactsController(profile_id, mContext);
        contactsController.getContactList(Constants.CONTACT_API_GET_CONTACTS);
        FavouriteController favouriteController = new FavouriteController(mContext, profile_id);
        favouriteController.getFavouritesList(Constants.CONTACT_API_GET_FAVOURITES);
    }

    @Subscribe
    public void onEventNewsImagesReceived(NewsImagesReceivedEvent event){
        Log.i(Constants.TAG, "MycommsApp.onEventNewsImagesReceived: ");
    }

    @Subscribe
    public void onContactListReceived(ContactListReceivedEvent event){
        Log.i(Constants.TAG, "MycommsApp.onContactListReceived: ");
        //new loadGroupChats().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Subscribe
    public void onEventChatsReceived(ChatsReceivedEvent event){
        Log.i(Constants.TAG, "MycommsApp.onEventChatsReceived: ");
        ChatMessage chatMsg = event.getMessage();
        int pendingMessages = event.getPendingMessages();

        if(chatMsg.getDirection()==Constants.CHAT_MESSAGE_DIRECTION_RECEIVED) {
            if(chatMsg.getGroup_id()!=null && chatMsg.getGroup_id().length()>0) {
                recentChatsHashMap.put(chatMsg.getGroup_id(), chatMsg.getTimestamp());
            }else {
                recentChatsHashMap.put(chatMsg.getContact_id(), chatMsg.getTimestamp());
            }
            if (pendingMessages == 0 && recentChatsHashMap!=null && recentChatsHashMap.size()>0){
                RecentContactController recentContactController =
                        new RecentContactController(this, profile_id);
                HashMap<String, Long> recentChatsHashMapClone = new HashMap<>();
                recentChatsHashMapClone.putAll(recentChatsHashMap);
                recentContactController.insertPendingChatsRecent(recentChatsHashMapClone);
                recentChatsHashMap.clear();
                recentContactController.getRecentList();
            }
        }
    }

    @Subscribe
    public void onAllPendingMessagesReceived(AllPendingMessagesReceivedEvent event){
        Log.i(Constants.TAG, "MycommsApp.onAllPendingMessagesReceived: ");
        if(recentChatsHashMap!=null && recentChatsHashMap.size()>0) {
            if(recentContactController==null)
                recentContactController = new RecentContactController(this, profile_id);

            HashMap<String, Long> recentChatsHashMapClone = new HashMap<>();
            recentContactController.insertPendingChatsRecent(recentChatsHashMap);
            recentChatsHashMap.clear();
        }
    }

//    public class loadGroupChats extends AsyncTask<String, Void, String>
//    {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//            try
//            {
//                GroupChatController groupChatController = new GroupChatController(mContext, profile_id);
//                ArrayList<GroupChat> chats = groupChatController.getAllGroupChatsFromAPI();
//                return groupChatController.insertGroupChatsIntoRealmIfNotExist(chats);
//            }
//            catch (Exception e)
//            {
//                Log.e(Constants.TAG, "MyCommsApp.loadGroupChats -> doInBackground: ERROR "
//                        + e.toString());
//                Crashlytics.logException(e);
//                return null;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(String result)
//        {
//            super.onPostExecute(result);
//            if(null == result) result = "0";
//            Log.d(Constants.TAG, "MyCommsApp.onPostExecute: Inserted group chat ids (if any): " +
//                    result);
//        }
//    }

    public void getNews() {
        Log.i(Constants.TAG, "MycommsApp.getNews: ");
        String apiCall = Constants.NEWS_API_GET;
        mNewsController.getNewsList(apiCall);
    }

    public class sendAvatar extends AsyncTask<String, Void, String>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try
            {
                String profile_id = params[0];
                File file =  new File(mContext.getFilesDir(), Constants.CONTACT_AVATAR_DIR +
                        "avatar_new_profile.jpg");
                String response = null;
                if(sp.getBoolean(Constants.FIRST_TIME_AVATAR_DELIVERY, false))
                {
                    if(null != file && file.exists() && file.length() != 0)
                    {
                        FilePushToServerController filePushToServerController =
                                new FilePushToServerController(mContext);

                        filePushToServerController.sendImageRequest
                                (
                                        Constants.CONTACT_API_POST_AVATAR,
                                        Constants.MULTIPART_AVATAR,
                                        file,
                                        Constants.MEDIA_TYPE_JPG
                                );

                        response = filePushToServerController.executeRequest();

                        file.renameTo(new File(mContext.getFilesDir(), Constants.CONTACT_AVATAR_DIR +
                                "avatar_" + profile_id + ".jpg"));
                    }
                    else
                        Log.e(Constants.TAG, "sendAvatar.doInBackground: avatar file is null, impossible to push first avatar");

                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean(Constants.FIRST_TIME_AVATAR_DELIVERY, false);
                    editor.commit();
                }
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
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            if(null != result)
                Log.d(Constants.TAG, "FilePushToServerController.sendFile: Response content: " + result);
            else
                Log.e(Constants.TAG, "sendAvatar.onPostExecute: ERROR -> response is null");
        }
    }

    public void startUserInactivityDetectThread() {
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(15000); // checks every 15sec for inactivity

                        if (countdownOn && !isScreenOff && !isApplicationOnBackground()) {
                            Log.i(Constants.TAG, "MycommsApp.startUserInactivityDetectThread: Interaction!!!");
                            countdownOn = false;
                            disconnectedProcess = false;
                            try {
                                networkEvents.register();
                            } catch (Exception e) {
                                Log.e(Constants.TAG, "MyCommsApp.startUserInactivityDetectThread: e ", e);
                            }
                        }
                        if (!countdownOn && (isScreenOff || isApplicationOnBackground())) {
                            Log.i(Constants.TAG, "MycommsApp.startUserInactivityDetectThread: Starting CountDown");
                            Log.i(Constants.TAG, "MycommsApp.startUserInactivityDetectThread: No Interaction!!!");
                            countdownOn = true;
                            startTime = Calendar.getInstance().getTimeInMillis();
                        }
                        if (countdownOn && !disconnectedProcess && (isScreenOff || isApplicationOnBackground())) {
                            currentTime = Calendar.getInstance().getTimeInMillis();
                            if ((currentTime - startTime)>Constants.IDLE_TIME){
                                //Time to shut down services
                                Log.i(Constants.TAG, "MycommsApp.startUserInactivityDetectThread: TIME OUT. Shut down services");
                                XMPPTransactions.disconnectMsgServerSession();
                                XMPPTransactions.disconnectPingThreadSession();
                                try {
                                    networkEvents.unregister();
                                } catch (Exception e) {
                                    Log.e(Constants.TAG, "MyCommsApp.startUserInactivityDetectThread: e ", e);
                                }
                                disconnectedProcess = true;
                            }
                        }

                    } catch (InterruptedException e) {
                        Log.e(Constants.TAG, "MyCommsApp.run: e ", e);
                    } catch (Exception e) {
                        Log.e(Constants.TAG, "MyCommsApp.run: e ", e);
                    }
                }
            }
        }).start();
    }

    private class ScreenReceiver extends BroadcastReceiver {

        protected ScreenReceiver() {
            // register receiver that handles screen on and screen off logic
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(this, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Log.e(Constants.TAG, "MyCommsApp ScreenReceiver.onReceive: SCREEN OFF");
                isScreenOff = true;
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                Log.e(Constants.TAG, "MyCommsApp ScreenReceiver.onReceive: SCREEN ON");
                isScreenOff = false;
            }
        }
    }

    /**
     * @return true if application is on background
     * */
    public static boolean isApplicationOnBackground() {
        return stateCounter == 0;
    }

    //to be called on each Activity onStart()
    public static void activityStarted() {
        stateCounter++;
    }

    //to be called on each Activity onStop()
    public static void activityStopped() {
        stateCounter--;
    }


}
