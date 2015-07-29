package com.vodafone.mycomms.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
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
import com.squareup.okhttp.Response;
import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.realm.RealmLDAPSettingsTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.OKHttpWrapper;
import com.vodafone.mycomms.util.ToolbarActivity;

import org.json.JSONObject;

import java.io.IOException;

public class AddGlobalContactsActivity extends ToolbarActivity {

    private LinearLayout layoutErrorBar;
    private TextView tvError;
    private EditText etEmail;
    private EditText etPassword;
    private Button btAddAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_global_contacts);

        layoutErrorBar = (LinearLayout)findViewById(R.id.layoutErrorBar);
        etEmail = (EditText)findViewById(R.id.etEmail);
        etPassword = (EditText)findViewById(R.id.etPassword);
        tvError = (TextView)findViewById(R.id.tvError);
        btAddAccount = (Button)findViewById(R.id.btAddAccount);

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

            }
        });

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(layoutErrorBar.getVisibility()==View.VISIBLE &&
                        tvError.getText().toString()
                                .compareTo(getString(R.string.credentials_are_incorrect))==0)
                    layoutErrorBar.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
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
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        OKHttpWrapper.getLDAP("/discover", this, new OKHttpWrapper.HttpCallback() {
            @Override
            public void onFailure(Response response, IOException e) {
                try {
                    if (response == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvError.setText(R.string.connection_error);
                                layoutErrorBar.setVisibility(View.VISIBLE);
                            }
                        });

                        return;
                    }

                    int code = response.code();

                    if (code >= 400 && code < 500) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvError.setText(R.string.credentials_are_incorrect);
                                layoutErrorBar.setVisibility(View.VISIBLE);
                            }
                        });
                    } else if (code >= 500) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvError.setText(R.string.error_reading_data_from_server);
                                layoutErrorBar.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } catch (Exception ex) {
                    Log.e(Constants.TAG, "AddGlobalContactsActivity.onFailure: ", ex);
                }
            }

            @Override
            public void onSuccess(Response response) {

            }
        });
    }

    private void callLDAPAuth(String url) {
        final String user = etEmail.getText().toString();
        final String password = etPassword.getText().toString();

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("username", user);
            jsonObject.put("password", password);
        } catch (Exception e) {
            Log.e(Constants.TAG, "AddGlobalContactsActivity.callLDAPAuth: ",e);
            Toast.makeText(AddGlobalContactsActivity.this,
                    getString(R.string.credentials_are_incorrect),
                    Toast.LENGTH_LONG).show();
            return;
        }

        OKHttpWrapper.postLDAP(url, jsonObject, this, new OKHttpWrapper.HttpCallback() {
            @Override
            public void onFailure(Response response, IOException e) {
                try {
                    if (response == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvError.setText(R.string.connection_error);
                                layoutErrorBar.setVisibility(View.VISIBLE);
                            }
                        });

                        return;
                    }

                    int code = response.code();

                    if (code >= 400 && code < 500) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvError.setText(R.string.credentials_are_incorrect);
                                layoutErrorBar.setVisibility(View.VISIBLE);
                            }
                        });
                    } else if (code >= 500) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvError.setText(R.string.error_reading_data_from_server);
                                layoutErrorBar.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } catch (Exception ex) {
                    Log.e(Constants.TAG, "AddGlobalContactsActivity.callLDAPAuth: ", ex);
                }
            }

            @Override
            public void onSuccess(Response response) {
                SharedPreferences sp =
                        getSharedPreferences(Constants.MYCOMMS_SHARED_PREFS, MODE_PRIVATE);
                String profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);
                if(profileId==null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvError.setText(R.string.error_reading_data_from_server);
                            layoutErrorBar.setVisibility(View.VISIBLE);
                        }
                    });
                    Log.e(Constants.TAG, "AddGlobalContactsActivity.callLDAPAuth: " +
                            "Error getting profileId");
                    Crashlytics.logException(new Exception("Error getting profileId"));
                    return;
                }

                String token = null;

                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    token = jsonObject.getString("token");
                    if(token==null) throw new Exception("Error getting token from response");
                } catch (Exception ex) {
                    Log.e(Constants.TAG, "AddGlobalContactsActivity.callLDAPAuth: ", ex);
                    Crashlytics.logException(ex);
                    return;
                }

                RealmLDAPSettingsTransactions.createOrUpdateData(profileId, user, password,
                        token, null);
            }
        });
    }
}