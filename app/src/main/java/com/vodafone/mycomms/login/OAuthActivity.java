package com.vodafone.mycomms.login;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UserSecurity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OAuthActivity extends Activity {

    WebView wvOAuth;
    String oauthPrefix;

    private boolean isForeground;

    private boolean pageStarted;
    private RelativeLayout relativeContainer;

    private Map<String,String> noCacheHeaders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(Constants.TAG, "OAuthActivity.onCreate: ");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth_web);

        wvOAuth = (WebView)findViewById(R.id.wvOAuth);
        relativeContainer = (RelativeLayout)findViewById(R.id.relative_container);
//        relativeContainer.setVisibility(View.INVISIBLE);

        //Register Otto Bus
        BusProvider.getInstance().register(this);

        //Read OAuth prefix
        oauthPrefix = getIntent().getStringExtra("oauth");

        //Load web view
        wvOAuth.getSettings().setJavaScriptEnabled(true);
        wvOAuth.getSettings().setSupportMultipleWindows(true);
        wvOAuth.getSettings().setAppCacheEnabled(false);
        setWebViewListener();

        //Launch OAuth corresponding URL
        wvOAuth.loadUrl("https://" + EndpointWrapper.getBaseURL() + "/auth/" + oauthPrefix,
                noCacheHeaders);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_oauth_web, menu);
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

    public void callOAuthCallback(String url)
    {
        HashMap hashUrl = null;

        try {
            hashUrl = new HashMap<>();
            hashUrl.put("url", url);
        } catch(Exception ex) {
            Log.e(Constants.TAG, "OAuthActivity.callOAuthToken: \n" + ex.toString());
            return;
        }

        new CallOAuthCallback().execute(null, null, hashUrl);
    }

    private void callbackOAuthCallback(HashMap<String, Object> result)
    {
        JSONObject json = null;
        String text = null;
        String status = null;

        status = (String)result.get("status");

        try {
            if (status.compareTo("200") == 0) {
                //User exists
                //Get tokens and expiration data from http response
                JSONObject jsonResponse = (JSONObject)result.get("json");
                String accessToken = jsonResponse.getString("accessToken");
                String refreshToken = jsonResponse.getString("refreshToken");
                long expiresIn = jsonResponse.getLong("expiresIn");

                UserSecurity.setTokens(accessToken, refreshToken, expiresIn, this);

                //Load profile
                ((MycommsApp)getApplication()).getProfileIdAndAccessToken();

                if(((MycommsApp)getApplication()).isProfileAvailable())
                    goToApp();
            }
            else if (status.compareTo("203") == 0) {
                //We have got user information, continue SignUp process
                //Save user data and go to signup
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
            else
            {
                //Come back to loginActivity
                Toast.makeText(this, getString(R.string.error_reading_data_from_server),
                        Toast.LENGTH_LONG).show();
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

    //Called when user profile has been loaded
    @Subscribe
    public void onApplicationAndProfileInitializedEvent(ApplicationAndProfileInitialized event){
        if(!isForeground) return;
        Log.e(Constants.TAG, "OAuthActivity.onApplicationAndProfileInitializedEvent: ");

        goToApp();
    }

    //Called when user profile has failed
    @Subscribe
    public void onApplicationAndProfileReadErrorEvent(ApplicationAndProfileReadError event){
        if(!isForeground) return;
        Log.e(Constants.TAG, "OAuthActivity.onApplicationAndProfileReadErrorEvent: ");

        if(((MycommsApp)getApplication()).isProfileAvailable()) {
            goToApp();
        } else {
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
        //Go to app
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

                    callOAuthCallback(Uri.parse(urlNewString).getPath() + "?" +
                            Uri.parse(urlNewString).getQuery());
//                    view.clearCache(true);
//                    Utils.clearCacheFolder(getApplicationContext().getCacheDir(), 1);
                    return true;
                }
                else {
                    view.loadUrl(urlNewString, noCacheHeaders);
                    return true;
                }
//                else {
//                    if (!loadingFinished) {
//                        redirect = true;
//                    }
//
//                    loadingFinished = false;
//
                // Otherwise, continue...
//                    return false;
//                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap facIcon)
            {
                super.onPageStarted(view,url,facIcon);
                relativeContainer.setVisibility(View.VISIBLE);
                wvOAuth.setVisibility(View.INVISIBLE);

//                loadingFinished = false;
//                if(relativeContainer.getVisibility() != View.VISIBLE)
//                {
//                    relativeContainer.setVisibility(View.VISIBLE);
//                    wvOAuth.setVisibility(View.INVISIBLE);
//                }
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                super.onPageFinished(view, url);
                relativeContainer.setVisibility(View.INVISIBLE);
                wvOAuth.setVisibility(View.VISIBLE);

//                if(!redirect){
//                    loadingFinished = true;
//                }
//
//                if(loadingFinished && !redirect){
//                    //HIDE LOADING IT HAS FINISHED
//                    if(relativeContainer.getVisibility() == View.VISIBLE)
//                    {
//                        relativeContainer.setVisibility(View.GONE);
//                        wvOAuth.setVisibility(View.VISIBLE);
//                    }
//                } else{
//                    redirect = false;
//                }
//                view.clearCache(true);
            }

        });
    }
}