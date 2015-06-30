package com.vodafone.mycomms.login;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

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

public class OAuthActivity extends Activity {

    WebView wvOAuth;
    String oauthPrefix;

    private boolean isForeground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth_web);

        wvOAuth = (WebView)findViewById(R.id.wvOAuth);

        //Register Otto Bus
        BusProvider.getInstance().register(this);

        //Read OAuth prefix
        oauthPrefix = getIntent().getStringExtra("oauth");

        //Load web view
        wvOAuth.getSettings().setJavaScriptEnabled(true);
        wvOAuth.getSettings().setSupportMultipleWindows(true);
        wvOAuth.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //If it is callback url, call api to get token
                if (Uri.parse(url).getPath().compareTo("/auth/"+ oauthPrefix + "/callback") == 0) {
                    callOAuthCallback(Uri.parse(url).getPath()+"?"+Uri.parse(url).getQuery());
                    return true;
                }

                // Otherwise, continue...
                return false;
            }

        });

        //Launch OAuth corresponding URL
        wvOAuth.loadUrl("https://" + EndpointWrapper.getBaseURL() + "/auth/" + oauthPrefix);
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
            }
            else if (status.compareTo("203") == 0) {
                //We have got user information, continue SignUp process
                //Save user data and go to signup
                JSONObject jsonResponse = (JSONObject)result.get("json");
                UserProfile.setMail(jsonResponse.getString("email"));
                UserProfile.setFirstName(jsonResponse.getString("name"));
                UserProfile.setLastName(jsonResponse.getString("lastname"));
                UserProfile.setOfficeLocation(jsonResponse.getString("officeLocation"));
                UserProfile.setCountryISO(jsonResponse.getString("country"));
                UserProfile.setPhone(jsonResponse.getString("phone"));
                UserProfile.setAvatar(jsonResponse.getString("avatar"));
                UserProfile.setOauth(jsonResponse.getString(oauthPrefix));
                UserProfile.setOauthPrefix(oauthPrefix);
                UserProfile.setPosition(jsonResponse.getString("position"));

                Intent in = new Intent(OAuthActivity.this, SignupMailActivity.class);
                startActivity(in);
                finish();
            }
            else
            {
                //Come back to loginActivity
                finish();
            }
        } catch(Exception ex) {
            Log.e(Constants.TAG, "OAuthActivity.callbackOAuthCallback: \n" + ex.toString());
            finish();
        }
    }

    //Called when user profile has been loaded
    @Subscribe
    public void onApplicationAndProfileInitializedEvent(ApplicationAndProfileInitialized event){
        if(!isForeground) return;
        goToApp();
    }

    //Called when user profile has failed
    @Subscribe
    public void onApplicationAndProfileReadErrorEvent(ApplicationAndProfileReadError event){
        if(!isForeground) return;

        if(((MycommsApp)getApplication()).isProfileAvailable()) {
            goToApp();
        }
        else {
            Toast.makeText(this,
                    getString(R.string.no_internet_connection_log_in_needed),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void goToApp()
    {
        //Go to app
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
}
