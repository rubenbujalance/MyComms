package com.vodafone.mycomms.login;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.Constants;

import org.json.JSONObject;

import java.util.HashMap;

public class OAuthActivity extends Activity {

    WebView wvOAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth_web);

        wvOAuth = (WebView)findViewById(R.id.wvOAuth);

        wvOAuth.getSettings().setJavaScriptEnabled(true);
        wvOAuth.getSettings().setSupportMultipleWindows(true);
        wvOAuth.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //If it is callback url, call api to get token
                if (Uri.parse(url).getPath().compareTo("/auth/sf/callback") == 0) {
                    callOAuthCallback(Uri.parse(url).getPath()+"?"+Uri.parse(url).getQuery());
                    return true;
                }

                // Otherwise, continue...
                return false;
            }

        });

        wvOAuth.loadUrl("https://" + EndpointWrapper.getBaseURL() + "/auth/sf");
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
                //User exists. We can get accessToken
                json = (JSONObject)result.get("json");
//                json.get("accessToken")
//
//                json.
            }
            else if (status.compareTo("203") == 0) {
                //We have got user information, continue SignUp process

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

    private class CallOAuthCallback extends AsyncTask<HashMap<String,Object>, Void, HashMap<String,Object>> {
        @Override
        protected HashMap<String,Object> doInBackground(HashMap<String,Object>[] params) {
            return APIWrapper.httpGetAPI("/user/" + params[2].get("url") + "/password", params[1], OAuthActivity.this);
        }
        @Override
        protected void onPostExecute(HashMap<String,Object> result) {
            callbackOAuthCallback(result);
        }
    }
}
