package com.vodafone.mycomms.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.Response;
import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.events.ApplicationAndProfileInitialized;
import com.vodafone.mycomms.events.ApplicationAndProfileReadError;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.OKHttpErrorReceivedEvent;
import com.vodafone.mycomms.login.LoginSignupActivity;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.OKHttpWrapper;
import com.vodafone.mycomms.util.SystemUiHider;
import com.vodafone.mycomms.util.UserSecurity;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import io.fabric.sdk.android.Fabric;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
@SuppressWarnings("ResourceType")
public class SplashScreenActivity extends Activity {

    ProgressDialog mProgress;
    Context mContext;
    private boolean isForeground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.splash_screen);
        mContext = this;

        //Register Otto Bus
        BusProvider.getInstance().register(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        //Check if it has been called from the email link
        Uri uriData = getIntent().getData();

        if(uriData != null &&
                uriData.getHost() != null &&
                uriData.getHost().compareTo("user") == 0)
        {
            String refreshToken = uriData.getLastPathSegment();
            UserSecurity.setTokens(null, refreshToken, 0, this);
            renewToken();
        }
        else {
            //Normal behaviour
            if (!APIWrapper.isConnected(this)) {
                //No connection, cannot check version nor profile
                if (UserSecurity.isUserLogged(this)) {
                    if (!UserSecurity.hasExpired(this)) {
                        if(((MycommsApp)getApplication()).isProfileAvailable()) {
                            goToApp(true);
                        }
                    }
                }

                //No Internet connection, and user not logged in or accessToken expired
                Toast.makeText(this,
                        getString(R.string.no_internet_connection_log_in_needed),
                        Toast.LENGTH_LONG).show();
                goToLogin();

            } else {
//                new CheckVersionApi().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            checkVersion();
        }
    }

    private void checkVersion() {
        OKHttpWrapper.get(Constants.API_VERSION, SplashScreenActivity.this,
                new OKHttpWrapper.HttpCallback() {
                    @Override
                    public void onFailure(Response response, IOException e) {
                        if (response == null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SplashScreenActivity.this,
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
                                        public void run() {callBackVersionCheck(data);}
                                    });
                                }
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {callBackVersionCheck(null);}
                                });
                            }
                        } catch (Exception ex) {
                            Log.e(Constants.TAG, "CheckVersionApi.onPostExecute: ", ex);
                            Crashlytics.logException(ex);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SplashScreenActivity.this,
                                            getString(R.string.error_reading_data_from_server),
                                            Toast.LENGTH_LONG).show();
                                    callBackVersionCheck(null);
                                }
                            });
                        }
                    }

                    @Override
                    public void onSuccess(Response response) {
                        callBackVersionCheck(null);
                    }
                });
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

        if(((MycommsApp)getApplication()).isProfileAvailable()) {
            goToApp(false);
        }
        else {
            Toast.makeText(this,
                    getString(R.string.no_internet_connection_log_in_needed),
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

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.new_version_available));
                builder.setMessage(getString(R.string.must_update_to_last_application_version));
                builder.setCancelable(false);

                builder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Launch download and install
                        if(isDownloadManagerAvailable())
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

                if (UserSecurity.isUserLogged(this)) {
                    if (UserSecurity.hasExpired(this)) {
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
            Toast.makeText(this, getString(R.string.error_reading_data_from_server),
                    Toast.LENGTH_LONG).show();
            finish();
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
//        params.put("accessToken", UserSecurity.getAccessToken(this));
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

                UserSecurity.setTokens(accessToken, null, expiresIn, this);

                //Load profile and go to app
                ((MycommsApp)getApplication()).getProfileIdAndAccessToken();
                if(((MycommsApp)getApplication()).isProfileAvailable())
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
            Toast.makeText(this, getString(R.string.error_reading_data_from_server),
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private boolean isDownloadManagerAvailable() {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");

            List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent,
                                                                PackageManager.MATCH_DEFAULT_ONLY);

            return list.size() > 0;

        } catch (Exception e) {
            Log.e(Constants.TAG, "SplashScreenActivity.isDownloadManagerAvailable: ",e);
            Crashlytics.logException(e);
            return false;
        }
    }

    private void downloadNewVersion(String url)
    {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription(getString(R.string.downloading_new_version));
        request.setTitle(getString(R.string.app_name) + " " + getString(R.string.update2));

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "mycomms.apk");

        //Get download service and enqueue file
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);

        //Show an alert to indicate the file download
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    private class ProfileAPI extends AsyncTask<HashMap<String,Object>, Void, HashMap<String,Object>> {
        @Override
        protected HashMap<String,Object> doInBackground(HashMap<String,Object>[] params) {
            return APIWrapper.httpPostAPI("/auth/renew", params[0], params[1], SplashScreenActivity.this);
        }
        @Override
        protected void onPostExecute(HashMap<String,Object> result) {
            callBackRenewToken(result);
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
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onOKHttpErrorReceived(OKHttpErrorReceivedEvent event) {
        Log.i(Constants.TAG, "SplashScreenActivity.onOKHttpErrorReceived: ");
        String errorMessage = event.getErrorMessage();
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }
}
