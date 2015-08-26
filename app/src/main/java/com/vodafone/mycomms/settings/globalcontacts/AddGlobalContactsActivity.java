package com.vodafone.mycomms.settings.globalcontacts;

import android.app.ProgressDialog;
import android.content.Context;
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

import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.contacts.connection.RecentContactController;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.GlobalContactsAddedEvent;
import com.vodafone.mycomms.main.MainActivity;
import com.vodafone.mycomms.main.SplashScreenActivity;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UncaughtExceptionHandlerController;

import model.GlobalContactsSettings;

public class AddGlobalContactsActivity extends MainActivity {

    //Attributes
    private LinearLayout layoutErrorBar;
    private TextView tvError;
    private EditText etEmail;
    private EditText etPassword;
    private Button btAddAccount;
    private SharedPreferences sp;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler
                (
                        new UncaughtExceptionHandlerController(this, SplashScreenActivity.class)
                );
        setContentView(R.layout.activity_add_global_contacts);

        BusProvider.getInstance().register(this);
        sp = this.getSharedPreferences(Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

        layoutErrorBar = (LinearLayout)findViewById(R.id.layoutErrorBar);
        etEmail = (EditText)findViewById(R.id.etEmail);
        etPassword = (EditText)findViewById(R.id.etPassword);
        tvError = (TextView)findViewById(R.id.tvError);
        btAddAccount = (Button)findViewById(R.id.btAddAccount);

        setCredentialsTextColor(false);

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
            public void onClick(View v)
            {
                setCredentialsTextColor(false);
                //Hide error bar if shown
                if (layoutErrorBar.getVisibility() == View.VISIBLE)
                    layoutErrorBar.setVisibility(View.GONE);

                //Start the process
                if (checkData()) {
                    pd = new ProgressDialog(AddGlobalContactsActivity.this);
                    pd.setCancelable(false);
                    pd.setTitle(getResources().getString(R.string.progress_dialog_validating_credentials));
                    pd.show();
                    String user = etEmail.getText().toString();
                    String password = etPassword.getText().toString();
                    GlobalContactsController gcController = new GlobalContactsController();

                    gcController.callLDAPAuthProcess(
                            user, password, AddGlobalContactsActivity.this,
                            new GlobalContactsController.GlobalContactsCallback() {
                                @Override
                                public void onFailure(String error, int resCode)
                                {
                                    Log.e(Constants.TAG, "AddGlobalContactsActivity.onFailure: ERROR ->" + error + " with code -> " + resCode);
                                    if(pd.isShowing())
                                        pd.dismiss();
                                    showMessageBarOnUIThread(error, resCode);

                                }

                                @Override
                                public void onSuccess(GlobalContactsSettings settings)
                                {
                                    if(pd.isShowing())
                                        pd.dismiss();
                                    BusProvider.getInstance().post(new GlobalContactsAddedEvent());
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            sp.edit().putBoolean(Constants.IS_GLOBAL_CONTACTS_LOADING_ENABLED, true).apply();
                                            Toast.makeText(AddGlobalContactsActivity.this,
                                                    R.string.global_contacts_settings_added,
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    finish();
                                }
                            });

                } else {
                    setCredentialsTextColor(true);
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
                if (layoutErrorBar.getVisibility() == View.VISIBLE)
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
                event.getConnectivityStatus()!=ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET) {
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

    private void showMessageBarOnUIThread(final String message, final int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvError.setText(message);
                layoutErrorBar.setVisibility(View.VISIBLE);
                if(code == 400)
                    setCredentialsTextColor(true);
            }
        });
    }

    private void setCredentialsTextColor(boolean isWrongCredentials)
    {
        if(isWrongCredentials)
        {
            etEmail.setTextColor(getResources().getColor(R.color.red_action));
            etPassword.setTextColor(getResources().getColor(R.color.red_action));
        }
        else
        {
            etEmail.setTextColor(getResources().getColor(R.color.contact_soft_black));
            etPassword.setTextColor(getResources().getColor(R.color.contact_soft_black));
        }
    }
}