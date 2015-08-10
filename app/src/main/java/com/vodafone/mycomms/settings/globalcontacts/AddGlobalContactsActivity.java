package com.vodafone.mycomms.settings.globalcontacts;

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

import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.main.MainActivity;
import com.vodafone.mycomms.util.Constants;

import io.realm.Realm;
import model.GlobalContactsSettings;

public class AddGlobalContactsActivity extends MainActivity {

    Realm realm;

    //Attributes
    private LinearLayout layoutErrorBar;
    private TextView tvError;
    private EditText etEmail;
    private EditText etPassword;
    private Button btAddAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_global_contacts);

//        realm = Realm.getDefaultInstance();

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
                    String user = etEmail.getText().toString();
                    String password = etPassword.getText().toString();
                    GlobalContactsController gcController = new GlobalContactsController();

                    gcController.callLDAPAuthProcess(
                            user, password, AddGlobalContactsActivity.this,
                            new GlobalContactsController.GlobalContactsCallback() {
                                @Override
                                public void onFailure(String error, int resCode) {
                                    showMessageBarOnUIThread(error);
                                }

                                @Override
                                public void onSuccess(GlobalContactsSettings settings) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(AddGlobalContactsActivity.this,
                                                    R.string.global_contacts_settings_added,
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    finish();
                                }
                            });

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