package com.vodafone.mycomms.login;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.UserProfile;
import com.vodafone.mycomms.events.ApplicationAndProfileInitialized;
import com.vodafone.mycomms.events.ApplicationAndProfileReadError;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.main.DashBoardActivity;
import com.vodafone.mycomms.main.MainActivity;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UncaughtExceptionHandlerController;
import com.vodafone.mycomms.util.UserSecurity;

import org.json.JSONObject;

import java.util.HashMap;

public class OAuthActivity extends MainActivity {

    public WebView wvOAuth;
    String oauthPrefix;
    private boolean isForeground;
    private RelativeLayout relativeContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(Constants.TAG, "OAuthActivity.onCreate: ");
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandlerController(this, null));
        setContentView(R.layout.activity_oauth_web);
        wvOAuth = (WebView)findViewById(R.id.wvOAuth);
        relativeContainer = (RelativeLayout)findViewById(R.id.relative_container);
        BusProvider.getInstance().register(this);
        oauthPrefix = getIntent().getStringExtra("oauth");
        wvOAuth.getSettings().setJavaScriptEnabled(true);
        wvOAuth.getSettings().setSupportMultipleWindows(true);
        wvOAuth.getSettings().setAppCacheEnabled(false);
        setWebViewListener();
        wvOAuth.loadUrl("https://" + EndpointWrapper.getBaseURL() + "/auth/" + oauthPrefix,
                new HashMap<String, String>());
    }

    public void callOAuthCallback(String url)
    {
        HashMap<String,Object> hashUrl = null;

        try {
            hashUrl = new HashMap<>();
            hashUrl.put("url", url);
            new CallOAuthCallback().execute(null, null, hashUrl);
        } catch(Exception ex) {
            Log.e(Constants.TAG, "OAuthActivity.callOAuthToken: \n" + ex.toString());
            return;
        }
    }

    private void callbackOAuthCallback(HashMap<String, Object> result)
    {
        JSONObject json = null;
        String text = null;
        String status = null;

        status = (String)result.get("status");

        try
        {
            if (status.compareTo("200") == 0)
            {
                JSONObject jsonResponse = (JSONObject)result.get("json");
                String accessToken = jsonResponse.getString("accessToken");
                String refreshToken = jsonResponse.getString("refreshToken");
                long expiresIn = jsonResponse.getLong("expiresIn");

                UserSecurity.setTokens(accessToken, refreshToken, expiresIn, this);

                //Load profile
                try {
                    ((MycommsApp)getApplication()).getProfileIdAndAccessToken();
                }
                catch (Exception e)
                {
                    Log.e(Constants.TAG, "OAuthActivity.callbackOAuthCallback: ", e);
                }

                if(MycommsApp.isProfileAvailable())
                    goToApp();
            }
            else if (status.compareTo("203") == 0)
            {
                JSONObject jsonResponse = (JSONObject)result.get("json");
                if(!jsonResponse.isNull("email"))
                    UserProfile.setMail(jsonResponse.getString("email"));
                if(!jsonResponse.isNull("name"))
                    UserProfile.setFirstName(jsonResponse.getString("name"));
                if(!jsonResponse.isNull("lastname"))
                    UserProfile.setLastName(jsonResponse.getString("lastname"));
                if(!jsonResponse.isNull("officeLocation"))
                    UserProfile.setOfficeLocation(jsonResponse.getString("officeLocation"));
                if(!jsonResponse.isNull("country"))
                    UserProfile.setCountryISO(jsonResponse.getString("country"));
                if(!jsonResponse.isNull("phone"))
                    UserProfile.setPhone(jsonResponse.getString("phone"));
                if(!jsonResponse.isNull("avatar"))
                    UserProfile.setAvatar(jsonResponse.getString("avatar"));
                if(!jsonResponse.isNull(oauthPrefix))
                    UserProfile.setOauth(jsonResponse.getString(oauthPrefix));
                if(!jsonResponse.isNull(oauthPrefix))
                    UserProfile.setOauthPrefix(oauthPrefix);
                if(!jsonResponse.isNull("position"))
                    UserProfile.setPosition(jsonResponse.getString("position"));

                goToSignUp();
            }
            else {
                Toast.makeText(this, getString(R.string.error_reading_data_from_server), Toast.LENGTH_LONG).show();
                finish();
            }
        } catch(Exception ex) {
            Log.e(Constants.TAG, "OAuthActivity.callbackOAuthCallback: \n" + ex.toString());
            Crashlytics.logException(ex);
            Toast.makeText(this, getString(R.string.error_reading_data_from_server),
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Subscribe
    public void onApplicationAndProfileInitializedEvent(ApplicationAndProfileInitialized event){
        if(!isForeground) return;
        Log.e(Constants.TAG, "OAuthActivity.onApplicationAndProfileInitializedEvent: ");

        goToApp();
    }

    @Subscribe
    public void onApplicationAndProfileReadErrorEvent(ApplicationAndProfileReadError event){
        if(!isForeground) return;
        Log.e(Constants.TAG, "OAuthActivity.onApplicationAndProfileReadErrorEvent: ");

        if(MycommsApp.isProfileAvailable())
            goToApp();
        else {
            Toast.makeText(this,
                    getString(R.string.no_internet_connection_log_in_needed),
                    Toast.LENGTH_LONG).show();

            finish();
        }
    }

    private void goToSignUp()
    {
        //Go to app
        wvOAuth.clearCache(true);
        Intent in = new Intent(OAuthActivity.this, SignupMailActivity.class);
        startActivity(in);
        finish();
    }

    private void goToApp()
    {
        wvOAuth.clearCache(true);
        Intent in = new Intent(OAuthActivity.this, DashBoardActivity.class);
        startActivity(in);
        finish();
    }

    private class CallOAuthCallback extends AsyncTask<HashMap<String,Object>, Void, HashMap<String,Object>> {
        @Override
        protected HashMap<String,Object> doInBackground(HashMap<String,Object>[] params) {
            return APIWrapper.httpGetAPI((String)params[2].get("url"), params[1], OAuthActivity.this);
        }
        @Override
        protected void onPostExecute(HashMap<String,Object> result) {
            callbackOAuthCallback(result);
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


    private void setWebViewListener()
    {
        wvOAuth.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
                //If it is callback url, call api to get token
                if (Uri.parse(urlNewString).getPath().compareTo("/auth/" + oauthPrefix + "/callback") == 0) {
                    relativeContainer.setVisibility(View.VISIBLE);
                    wvOAuth.setVisibility(View.INVISIBLE);

                    callOAuthCallback(Uri.parse(urlNewString).getPath() + "?" + Uri.parse(urlNewString).getQuery());
                    return true;
                } else {
                    view.loadUrl(urlNewString, new HashMap<String, String>());
                    return true;
                }
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap facIcon) {
                super.onPageStarted(view, url, facIcon);
                relativeContainer.setVisibility(View.VISIBLE);
                wvOAuth.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                relativeContainer.setVisibility(View.INVISIBLE);
                wvOAuth.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        MycommsApp.activityStarted();
    }

    @Override
    public void onStop() {
        MycommsApp.activityStopped();
        super.onStop();
    }
}