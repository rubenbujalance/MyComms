package com.vodafone.mycomms.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.realm.RealmLDAPSettingsTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AddGlobalContactsActivity extends ToolbarActivity {

    /*
    PROCESS TO LOGIN IN LDAP
    1. Call "discover" endpoint to get the user URL (get a 200 code)
    2. Call "user" url (receiving a 401-Unauthorized) to get the final auth url as a header
    3. Call the final URL to authenticate user into LDAP (receiving 200)
        3a. If a 401-Unauthorized is received, credentials are incorrect
     */

    //Constants
    private final String LDAP_AUTH_URL_KEY = "MsRtcOAuth";
    private final String LDAP_HEADER_AUTH_KEY = "WWW-Authenticate";
    private final String LDAP_TOKEN_KEY = "access_token";
    private final String LDAP_TOKEN_TYPE_KEY = "token_type";;
    private final String LDAP_USERNAME_KEY = "username";;
    private final String LDAP_PASSWORD_KEY = "password";
    private final String LDAP_GRANT_TYPE_KEY = "grant_type";
    private final String LDAP_GRANT_TYPE_VALUE = "password";
    private final String LDAP_HEADER_CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded";

    //Attributes
    private LinearLayout layoutErrorBar;
    private TextView tvError;
    private EditText etEmail;
    private EditText etPassword;
    private Button btAddAccount;

    //OkHttp
    private OkHttpClient client;

    //LDAP Temp
    private String tempUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_global_contacts);

        client = new OkHttpClient();
        client.setConnectTimeout(10, TimeUnit.SECONDS);
        client.setReadTimeout(30, TimeUnit.SECONDS);
        client.setRetryOnConnectionFailure(false);

        layoutErrorBar = (LinearLayout)findViewById(R.id.layoutErrorBar);
        etEmail = (EditText)findViewById(R.id.etEmail);
        etPassword = (EditText)findViewById(R.id.etPassword);
        tvError = (TextView)findViewById(R.id.tvError);
        btAddAccount = (Button)findViewById(R.id.btAddAccount);

        TextView tvInitialText = (TextView)findViewById(R.id.tvInitialText);
        tvInitialText.setText(Html.fromHtml(getString(R.string.provide_your_corporate_credentials)));
        TextView tvExplanationText = (TextView)findViewById(R.id.tvExplanationText);
        tvExplanationText.setText(Html.fromHtml(getString(R.string.ldap_need_help_with_your_username)));

        etPassword.setTypeface(etEmail.getTypeface());
        layoutErrorBar.setVisibility(View.GONE);

        ImageView ivBtBack = (ImageView)findViewById(R.id.ivBtBack);
        ivBtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btAddAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Hide error bar if shown
                if (layoutErrorBar.getVisibility() == View.VISIBLE &&
                        tvError.getText().toString()
                                .compareTo(getString(R.string.credentials_are_incorrect)) == 0)
                    layoutErrorBar.setVisibility(View.GONE);

                //Start the process
                if (checkData()) {
                    callLDAPDiscover();
                } else {
                    tvError.setText(R.string.credentials_are_incorrect);
                    layoutErrorBar.setVisibility(View.VISIBLE);
                }
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (layoutErrorBar.getVisibility() == View.VISIBLE &&
                        tvError.getText().toString()
                                .compareTo(getString(R.string.credentials_are_incorrect)) == 0)
                    layoutErrorBar.setVisibility(View.GONE);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);
    }

    @Subscribe
     public void onConnectivityChanged(ConnectivityChanged event) {

        Log.e(Constants.TAG, "DashBoardActivity.onConnectivityChanged: "
                + event.getConnectivityStatus().toString());
        if(event.getConnectivityStatus()!= ConnectivityStatus.MOBILE_CONNECTED &&
                event.getConnectivityStatus()!=ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET)
            if(layoutErrorBar.getVisibility()!=View.VISIBLE) {
                tvError.setText(R.string.no_internet_connection_is_available);
                layoutErrorBar.setVisibility(View.VISIBLE);
            }
            else if (tvError.getText().toString()
                    .compareTo(getString(R.string.no_internet_connection_is_available))==0){
                layoutErrorBar.setVisibility(View.GONE);
            }
    }

    private void callLDAPDiscover() {
        Request.Builder builder = new Request.Builder();
        Request request = builder.url(EndpointWrapper.getLDAPDiscover()).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                showMessageBarOnUIThread(getString(R.string.connection_error));
            }

            @Override
            public void onResponse(Response response) {
                try {
                    int code = response.code();

                    if (code >= 400 && code < 500) {
                        showMessageBarOnUIThread(getString(R.string.connection_error));
                    } else if (code >= 500) {
                        showMessageBarOnUIThread(
                                getString(R.string.error_reading_data_from_server));
                    } else if (code == 200) {
                        JSONObject jsonObject = new JSONObject(
                                response.body().string()).getJSONObject("_links");
                        String href = ((JSONObject) jsonObject.get("user"))
                                .getString("href");

                        //If everythig is OK, save url and continue the process
                        tempUrl = href;
                        callLDAPUser(href);
                    } else {
                        throw new Exception(
                                getString(R.string.error_reading_data_from_server));
                    }
                } catch (Exception ex) {
                    Log.e(Constants.TAG,
                            "AddGlobalContactsActivity.callLDAPDiscover: ", ex);
                    Crashlytics.logException(ex);
                    showMessageBarOnUIThread(getString(R.string.credentials_are_incorrect));
                }
            }
        });
    }

    private void callLDAPUser(String url) {
        Request.Builder builder = new Request.Builder();
        Request request = builder.url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                showMessageBarOnUIThread(getString(R.string.connection_error));
            }

            @Override
            public void onResponse(Response response) {
                try {
                    int code = response.code();

                    if (code >= 400 && code < 500) {
                        Headers headers = response.headers();
                        if (headers == null || headers.size() == 0 ||
                                headers.get(LDAP_HEADER_AUTH_KEY) == null) {
                            throw new Exception(getString(R.string.error_reading_data_from_server));
                        } else {
                            String[] authHeader = headers.get(LDAP_HEADER_AUTH_KEY).split(",");
                            String hrefHeader = null;

                            for (int i = 0; i < authHeader.length; i++) {
                                if (authHeader[i].trim().startsWith(LDAP_AUTH_URL_KEY)) {
                                    hrefHeader = authHeader[0].trim();
                                }
                            }

                            if (hrefHeader == null) throw new Exception(
                                    getString(R.string.error_reading_data_from_server));

                            String href = hrefHeader.split("=")[1]
                                    .replaceAll("\"", "");

                            //If evething it's ok, continue the process
                            callLDAPAuth(href);
                        }
                    } else if (code >= 500) {
                        showMessageBarOnUIThread(
                                getString(R.string.error_reading_data_from_server));
                    }
                    else {
                        showMessageBarOnUIThread(
                                getString(R.string.error_reading_data_from_server));
                    }
                } catch (Exception ex) {
                    showMessageBarOnUIThread(getString(R.string.error_reading_data_from_server));
                }
            }
        });
    }

    private void callLDAPAuth(String url) {
        RequestBody body;
        final String user = etEmail.getText().toString();
        final String password = etPassword.getText().toString();

        try {
            String params = LDAP_GRANT_TYPE_KEY + "=" + LDAP_GRANT_TYPE_VALUE + "&" +
                    LDAP_USERNAME_KEY + "=" + user + "&" +
                    LDAP_PASSWORD_KEY + "=" + password;

            body = RequestBody.create(
                    MediaType.parse(LDAP_HEADER_CONTENT_TYPE_VALUE), params);
        } catch (Exception e) {
            Log.e(Constants.TAG, "AddGlobalContactsActivity.callLDAPAuth: ",e);
            showMessageBarOnUIThread(getString(R.string.error_reading_data_from_server));
            return;
        }

        Request.Builder builder = new Request.Builder();
        Request request = builder
                .url(url)
                .addHeader(Constants.API_HTTP_HEADER_CONTENTTYPE,
                        LDAP_HEADER_CONTENT_TYPE_VALUE)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                showMessageBarOnUIThread(getString(R.string.connection_error));
            }

            @Override
            public void onResponse(Response response) {
                try {
                    int code = response.code();

                    if (code >= 400 && code < 500) {
                        showMessageBarOnUIThread(getString(R.string.credentials_are_incorrect));
                    } else if (code >= 200 && code < 300) {
                        SharedPreferences sp =
                                getSharedPreferences(Constants.MYCOMMS_SHARED_PREFS, MODE_PRIVATE);
                        String profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);

                        if (profileId == null) {
                            throw new Exception("Error getting profileId");
                        }

                        String token, tokenType;

                        JSONObject jsonObject = new JSONObject(response.body().string());
                        token = jsonObject.getString(LDAP_TOKEN_KEY);
                        tokenType = jsonObject.getString(LDAP_TOKEN_TYPE_KEY);

                        if (token == null || tokenType == null || tempUrl==null)
                            throw new Exception("Error getting token from response");

                        RealmLDAPSettingsTransactions.createOrUpdateData(profileId, user, password,
                                token, tokenType, tempUrl, null);

                        //Close activity. Different transitions depending on origin activity
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddGlobalContactsActivity.this,
                                        R.string.global_contacts_settings_added, Toast.LENGTH_LONG);
                            }
                        });
                        finish();
                    } else {
                        showMessageBarOnUIThread(
                                getString(R.string.error_reading_data_from_server));
                    }
                } catch (Exception ex) {
                    Log.e(Constants.TAG, "AddGlobalContactsActivity.callLDAPAuth: ", ex);
                    showMessageBarOnUIThread(getString(R.string.error_reading_data_from_server));
                    Crashlytics.logException(ex);
                }
            }
        });
    }

    private boolean checkData() {
        if (etEmail.getText() != null && etEmail.getText().toString().length() > 0 &&
                etPassword.getText() != null && etPassword.getText().toString().length() > 0)
            return true;
        else return false;
    }

    private void showMessageBarOnUIThread(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvError.setText(message);
                layoutErrorBar.setVisibility(View.VISIBLE);
            }
        });
    }
}