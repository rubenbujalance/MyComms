package com.vodafone.mycomms.main;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.Response;
import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chatgroup.GroupChatActivity;
import com.vodafone.mycomms.chatlist.view.ChatListActivity;
import com.vodafone.mycomms.events.ApplicationAndProfileInitialized;
import com.vodafone.mycomms.events.ApplicationAndProfileReadError;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.GroupChatCreatedEvent;
import com.vodafone.mycomms.events.OKHttpErrorReceivedEvent;
import com.vodafone.mycomms.login.LoginSignupActivity;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.NotificationMessages;
import com.vodafone.mycomms.util.OKHttpWrapper;
import com.vodafone.mycomms.util.SystemUiHider;
import com.vodafone.mycomms.util.UserSecurity;
import com.vodafone.mycomms.util.Utils;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import model.Chat;
import model.GroupChat;

import static android.widget.Toast.makeText;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
@SuppressWarnings("ResourceType")
public class SplashScreenActivity extends MainActivity {

    public Context mContext;
    public boolean isForeground;
    public boolean isAppCrashed;
    public String errorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_screen);
        mContext = SplashScreenActivity.this;

        //Profile Id
        SharedPreferences sp = getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

        //Register Otto Bus
        BusProvider.getInstance().register(SplashScreenActivity.this);

        getExtras();
        if(isAppCrashed)
            showAlertDialog();
        else
            new CheckIfGoToConversation().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void doOnPostCreateTasks()
    {
        //Check if it has been called from the email link
        Uri uriData = getIntent().getData();

        if(uriData != null &&
                uriData.getHost() != null &&
                uriData.getHost().compareTo("user") == 0)
        {
            String refreshToken = uriData.getLastPathSegment();
            UserSecurity.setTokens(null, refreshToken, 0, SplashScreenActivity.this);
            renewToken();
        }
        else {
            //Normal behaviour
            if (!Utils.isConnected(SplashScreenActivity.this)) {
                //No connection, cannot check version nor profile
                if (UserSecurity.isUserLogged(SplashScreenActivity.this)
                        && !UserSecurity.hasExpired(SplashScreenActivity.this)
                        && MycommsApp.isProfileAvailable())
                {
                    goToApp(true);
                }
                //No Internet connection, and user not logged in or accessToken expired
                makeText(SplashScreenActivity.this,
                        getString(R.string.no_internet_connection_log_in_needed),
                        Toast.LENGTH_LONG).show();
                goToLogin();

            }
            checkVersion();
        }
    }

    private void showAlertDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this);
        String title = SplashScreenActivity.this.getResources().getString(R.string.uncaught_exception_title);
        View view = Utils.getCustomAlertTitleView(SplashScreenActivity.this, R.layout.layout_uncaught_exception_alert);
        TextView textView = (TextView) view.findViewById(R.id.tv_uncaught_exception_alert_title);

        textView.setText(title);
        builder.setCustomTitle(view);

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                doOnPostCreateTasks();
            }
        });
        builder.setPositiveButton(R.string.uncaught_exception_contact_support, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                sendSupportEmailIfAppCrashed();
            }
        });

        builder.create();
        builder.show();
    }

    private void getExtras()
    {
        Intent intent = getIntent();
        isAppCrashed = intent.hasExtra(Constants.IS_APP_CRASHED_EXTRA);
        if(intent.hasExtra(Constants.APP_CRASH_MESSAGE))
            errorMessage = intent.getStringExtra(Constants.APP_CRASH_MESSAGE);
    }

    private void sendSupportEmailIfAppCrashed()
    {
        Log.i(Constants.TAG, "SplashScreenActivity.sendSupportEmailIfCrashed: Sending Email for crash with body -> " + errorMessage);
        Utils.launchSupportEmail
                (
                        SplashScreenActivity.this
                        , getApplicationContext().getResources().getString(R.string.support_subject_crash)
                        , getApplicationContext().getResources().getString(R.string.support_text_crash)
                                + "\n\n" + errorMessage
                        , getApplicationContext().getResources().getString(R.string.support_email)
                        , Constants.REQUEST_START_ACTIVITY_FOR_APP_CRASH
                );
    }

    public void checkVersion() {
        OKHttpWrapper.get(Constants.API_VERSION, SplashScreenActivity.this,
                new OKHttpWrapper.HttpCallback() {
                    @Override
                    public void onFailure(Response response, IOException e) {
                        doCheckVersionOnFailure(response, e);
                    }

                    @Override
                    public void onSuccess(Response response) {
                        callBackVersionCheck(null);
                    }
                });
    }

    public void doCheckVersionOnFailure(Response response, IOException e)
    {
        Log.e(Constants.TAG, "SplashScreenActivity.doCheckVersionOnFailure: ", e);
        if (response == null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    makeText(SplashScreenActivity.this,
                            getString(R.string.error_reading_data_from_server),
                            Toast.LENGTH_LONG).show();
                    callBackVersionCheck(null);
                }
            });

            return;
        }

        try {
            if (Integer.toString(response.code()).startsWith("4") &&
                    response.body() != null) {
                final JSONObject json = new JSONObject(response.body().string());

                if (json.get("err") != null &&
                        json.get("err").toString().
                                compareTo("invalid_version") == 0) {
                    final String data = json.get("data").toString();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callBackVersionCheck(data);
                        }
                    });
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callBackVersionCheck(null);
                    }
                });
            }
        } catch (Exception ex) {
            Log.e(Constants.TAG, "CheckVersionApi.onPostExecute: ", ex);
            Crashlytics.logException(ex);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    makeText(SplashScreenActivity.this,
                            getString(R.string.error_reading_data_from_server),
                            Toast.LENGTH_LONG).show();
                    callBackVersionCheck(null);
                }
            });
        }
    }

    //Called when user profile has been loaded
    @Subscribe
    public void onApplicationAndProfileInitializedEvent(ApplicationAndProfileInitialized event){
        if(!isForeground) return;
        goToApp(false);
    }

    //Called when user profile has failed
    @Subscribe
    public void onApplicationAndProfileReadErrorEvent(ApplicationAndProfileReadError event){
        Log.e(Constants.TAG, "SplashScreenActivity.onApplicationAndProfileReadErrorEvent: ");
        if(!isForeground) return;

        if(MycommsApp.isProfileAvailable()) {
            goToApp(false);
        }
        else {
            makeText(SplashScreenActivity.this,
                    getResources().getString(R.string.no_internet_connection_log_in_needed),
                    Toast.LENGTH_LONG).show();
            goToLogin();
        }
    }

    private void callBackVersionCheck(final String result)
    {
        try {
            if(result != null) {
                /*
                 * New version detected! Show an alert and start the update...
                 */
                Log.i(Constants.TAG, "SplashScreenActivity.callBackVersionCheck: " +
                        "New version detected");

                AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this);
                builder.setTitle(getString(R.string.new_version_available));
                builder.setMessage(getString(R.string.must_update_to_last_application_version));
                builder.setCancelable(false);

                builder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Launch download and install
                        if (isDownloadManagerAvailable(SplashScreenActivity.this))
                            downloadNewVersion(result);
                        else {
                            downloadNewVersionFromURI(result);
                        }

                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(R.string.support_button_text, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Launch download and install
                        Utils.launchSupportEmail
                                (
                                        SplashScreenActivity.this
                                        , getApplicationContext().getResources().getString(R.string.support_subject)
                                        , getApplicationContext().getResources().getString(R.string.support_text)
                                        , getApplicationContext().getResources().getString(R.string.support_email)
                                        , 0
                                );
                        if (isDownloadManagerAvailable(SplashScreenActivity.this))
                            downloadNewVersion(result);

                        dialog.dismiss();
                    }
                });

                builder.create();
                builder.show();

            } else {
                //Version is correct, check login
                Log.i(Constants.TAG, "SplashScreenActivity.callBackVersionCheck: " +
                        "Version OK");

                if (UserSecurity.isUserLogged(SplashScreenActivity.this)) {
                    if (UserSecurity.hasExpired(SplashScreenActivity.this)) {
                        renewToken();
                    } else {
                        ((MycommsApp)getApplication()).getProfileIdAndAccessToken();

                        if(((MycommsApp)getApplication()).isProfileAvailable())
                            goToApp(true);
                    }
                } else {
                    //User not logged in
                    goToLogin();
                }
            }
        } catch (Exception ex) {
            Log.e(Constants.TAG, "SplashScreenActivity.callBackVersionCheck: \n",ex);
            Crashlytics.logException(ex);
            makeText(SplashScreenActivity.this, getString(R.string
                            .error_reading_data_from_server),
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    //Alternative method created for users with problem with Downlad Manager
    private void downloadNewVersionFromURI(String uri) {
        try{
            Log.i(Constants.TAG, "SplashScreenActivity.downloadNewVersionFromURI");
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(uri));
            startActivity(i);

            //Show an alert to indicate the file download
            AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this);
            builder.setTitle(getString(R.string.update2));
            builder.setMessage(getString(R.string.please_check_the_download_status_in_the_notifications_bar));
            builder.setCancelable(false);

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //Launch download and install
                    finish();
                }
            });

            builder.create();
            builder.show();
        } catch (Exception e){
            Log.e(Constants.TAG, "SplashScreenActivity.downloadNewVersionFromURI: ", e);
        }
    }

    private void goToLogin()
    {
        Intent in = new Intent(SplashScreenActivity.this, LoginSignupActivity.class);
        startActivity(in);
        finish();
    }

    private void goToApp(boolean notify)
    {
        //Notify app that profile is available and we are entering
        if(notify) BusProvider.getInstance().post(new ApplicationAndProfileInitialized());

        //Go to app
        Intent in = new Intent(SplashScreenActivity.this, DashBoardActivity.class);
        startActivity(in);
        finish();
    }

    public void renewToken()
    {
        HashMap<String,Object> params = new HashMap<>();
        params.put("refreshToken", UserSecurity.getRefreshToken(this));

        new RenewTokenAPI().execute(params, null);
    }

    private void callBackRenewToken(HashMap<String, Object> result)
    {
        try {
            String status = (String)result.get("status");

            if (status.compareTo("200") == 0) {
                //Renew succeeded, save tokens and go to app

                //Get tokens and expiration data from http response
                //Renew doesn't return refresh token
                JSONObject jsonResponse = (JSONObject)result.get("json");
                String accessToken = jsonResponse.getString("accessToken");
                long expiresIn = jsonResponse.getLong("expiresIn");

                UserSecurity.setTokens(accessToken, null, expiresIn, SplashScreenActivity.this);

                //Load profile and go to app
                ((MycommsApp)getApplication()).getProfileIdAndAccessToken();
                if(MycommsApp.isProfileAvailable())
                    goToApp(true);
            }
            else
            {
                //Renew failed, go to login
                goToLogin();
            }
        } catch(Exception ex) {
            Log.e(Constants.TAG, "SplashScreenActivity.callBackRenewToken: \n",ex);
            Crashlytics.logException(ex);
            makeText(SplashScreenActivity.this, getString(R.string
                            .error_reading_data_from_server),
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void downloadNewVersion(String url)
    {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDescription(getString(R.string.downloading_new_version));
            request.setTitle(getString(R.string.app_name) + " " + getString(R.string.update2));

            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "mycomms.apk");

            //Get download service and enqueue file
            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            manager.enqueue(request);

            //Show an alert to indicate the file download
            AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this);
            builder.setTitle(getString(R.string.update2));
            builder.setMessage(getString(R.string.please_check_the_download_status_in_the_notifications_bar));
            builder.setCancelable(false);

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //Launch download and install
                    finish();
                }
            });

            builder.create();
            builder.show();
        } catch (Exception e){
            Log.e(Constants.TAG, "SplashScreenActivity.downloadNewVersion: ", e);
            Crashlytics.logException(e);
            downloadNewVersionFromURI(url);
        }
    }

    public static boolean isDownloadManagerAvailable(Context context)
    {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");

            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);

            return list.size() > 0;

        } catch (Exception e) {
            return false;
        }
    }

    //Async Tasks
    private class RenewTokenAPI extends AsyncTask<HashMap<String,Object>, Void, HashMap<String,Object>> {
        @Override
        protected HashMap<String,Object> doInBackground(HashMap<String,Object>[] params) {
            return APIWrapper.httpPostAPI("/auth/renew", params[0], params[1], SplashScreenActivity.this);
        }
        @Override
        protected void onPostExecute(HashMap<String,Object> result) {
            callBackRenewToken(result);
        }
    }

    private class CheckIfGoToConversation extends AsyncTask<Void, Void, Void> {
        boolean goToConversation;
        boolean waitToGroupChatDownload;
        Intent intentChat;
        Realm realm;

        @Override
        protected Void doInBackground(Void... params) {
            Log.i(Constants.TAG, "CheckIfGoToConversation.doInBackground: ");
            goToConversation = false;
            waitToGroupChatDownload = false;
            intentChat = null;
            realm = Realm.getDefaultInstance();

            //Reset notifications
            NotificationMessages.resetInboxMessages(SplashScreenActivity.this);

            //If we come from a notification, navigate if necessary
            try {
                //Check if it comes from a notification with a logged out user
                SharedPreferences sp = getSharedPreferences(
                        Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
                String token = sp.getString(Constants.ACCESS_TOKEN_SHARED_PREF, "");
                String profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");

                if(token!=null && token.length()>0 && profileId!=null && profileId.length()>0) {

                    Intent intent = getIntent();
                    Bundle notificationData = intent.getBundleExtra(Constants.NOTIFICATION_EXTRA_KEY);
                    intent.removeExtra(Constants.NOTIFICATION_EXTRA_KEY);
                    String type = null;

                    if (notificationData != null) {
                        type = notificationData.getString(Constants.NOTIFICATION_BUNDLE_TYPE_KEY);
                    }

                    //Check if it comes from a chat notification
                    if (type != null &&
                            (type.compareTo(Constants.NOTIFICATION_BUNDLE_CHAT_TYPE_VALUE) == 0 ||
                                    type.compareTo(Constants.NOTIFICATION_BUNDLE_GROUPCHAT_TYPE_VALUE) == 0)) {
                        String from = notificationData.getString(Constants.NOTIFICATION_BUNDLE_FROM_KEY);

                        if (from != null && from.contains("@")) {
                            from = from.substring(0, from.indexOf("@"));

                            Log.i(Constants.TAG, "CheckIfGoToConversation.doInBackground: Notification received " +
                                    "[Type-"+type+"; From-"+from);

                            boolean isGroupChat =
                                    (type.compareTo(Constants.NOTIFICATION_BUNDLE_GROUPCHAT_TYPE_VALUE) == 0);

                            if (!isGroupChat) {
                                //Check if chat exists and navigate
                                RealmChatTransactions realmChatTransactions =
                                        new RealmChatTransactions(SplashScreenActivity.this);
                                Chat chat = realmChatTransactions.getChatByContactId(from, realm);

                                if (chat != null) {
                                    intentChat = new Intent(SplashScreenActivity.this, GroupChatActivity.class);
                                    intentChat.putExtra(Constants.CHAT_FIELD_CONTACT_ID, from);
                                    intentChat.putExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY, Constants.DASHBOARD_ACTIVITY);
                                    intentChat.putExtra(Constants.IS_GROUP_CHAT, false);

                                    goToConversation = true;
                                } else {
                                    intentChat = new Intent(SplashScreenActivity.this, ChatListActivity.class);
                                    intentChat.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                                    goToConversation = true;
                                }
                            } else {
                                //Check if chat exists and navigate
                                RealmGroupChatTransactions realmGroupChatTransactions =
                                        new RealmGroupChatTransactions(
                                                SplashScreenActivity.this, profileId);
                                GroupChat groupChat =
                                        realmGroupChatTransactions.getGroupChatById(from, realm);

                                if (groupChat != null) {
                                    intentChat = new Intent(SplashScreenActivity.this, GroupChatActivity.class);
                                    intentChat.putExtra(Constants.GROUP_CHAT_ID, from);
                                    intentChat.putExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY, Constants.DASHBOARD_ACTIVITY);
                                    intentChat.putExtra(Constants.IS_GROUP_CHAT, true);

                                    goToConversation = true;
                                } else {
                                    waitToGroupChatDownload = true;
                                    XMPPTransactions.downloadAndSaveGroupChat(null, from, getApplicationContext());
//                                    intentChat = new Intent(SplashScreenActivity.this, ChatListActivity.class);
//                                    intentChat.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(Constants.TAG, "CheckIfGoToConversation.doInBackground: ",e);
                Crashlytics.logException(e);
                goToConversation = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(goToConversation) {
                Log.i(Constants.TAG, "CheckIfGoToConversation.onPostExecute: GoToConversation");
                Intent intent = new Intent(SplashScreenActivity.this, DashBoardActivity.class);
                intent.putExtra(Constants.GO_TO_CHAT_INTENT_KEY, intentChat);
                startActivity(intent);
                finish();
            }
            else if(!waitToGroupChatDownload){
                Log.i(Constants.TAG, "CheckIfGoToConversation.onPostExecute: doPostCreateTasks");
                doOnPostCreateTasks();
            }
            else {
                //In any other case, we will wait to group chat creation response
                Log.i(Constants.TAG, "CheckIfGoToConversation.onPostExecute: waitToGroupChatDownload");
            }
        }
    }

    @Override
    protected void onResume() {
        isForeground = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        isForeground = false;
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Constants.REQUEST_START_ACTIVITY_FOR_APP_CRASH)
            doOnPostCreateTasks();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(SplashScreenActivity.this);
    }

    @Subscribe
    public void onOKHttpErrorReceived(OKHttpErrorReceivedEvent event) {
        Log.i(Constants.TAG, "SplashScreenActivity.onOKHttpErrorReceived: ");
        String errorMessage = event.getErrorMessage();
        makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onGroupChatCreatedEvent(GroupChatCreatedEvent event) {
        Log.i(Constants.TAG, "SplashScreenActivity.onGroupChatCreatedEvent: " +
                "Id-"+event.getGroupId()+"; Success-"+event.isSuccess());

        final String groupId = event.getGroupId();
        final boolean success = event.isSuccess();

        if(!success) {
            //If failed, go to chat list
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intentChat = new Intent(SplashScreenActivity.this, ChatListActivity.class);
                    intentChat.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                    Intent intent = new Intent(SplashScreenActivity.this, DashBoardActivity.class);
                    intent.putExtra(Constants.GO_TO_CHAT_INTENT_KEY, intentChat);
                    startActivity(intent);
                    finish();
                }
            });
        }
        else {
            //If success, go to group chat
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intentChat = new Intent(SplashScreenActivity.this, GroupChatActivity.class);
                    intentChat.putExtra(Constants.GROUP_CHAT_ID, groupId);
                    intentChat.putExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY, Constants.DASHBOARD_ACTIVITY);
                    intentChat.putExtra(Constants.IS_GROUP_CHAT, true);

                    Intent intent = new Intent(SplashScreenActivity.this, DashBoardActivity.class);
                    intent.putExtra(Constants.GO_TO_CHAT_INTENT_KEY, intentChat);
                    startActivity(intent);
                    finish();
                }
            });
        }
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
