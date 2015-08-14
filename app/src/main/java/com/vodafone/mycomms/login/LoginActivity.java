package com.vodafone.mycomms.login;

import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.connection.BaseConnection;
import com.vodafone.mycomms.events.ApplicationAndProfileInitialized;
import com.vodafone.mycomms.events.ApplicationAndProfileReadError;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.login.connection.ILoginConnectionCallback;
import com.vodafone.mycomms.main.DashBoardActivity;
import com.vodafone.mycomms.main.connection.MainAppCompatActivity;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

public class LoginActivity extends MainAppCompatActivity implements ILoginConnectionCallback {

    private static final int FORGOT_PASSWORD_ACTIVITY = 1;

    Button btLoginSalesforce;
    Button btLogin;
    TextView tvForgotPass;
    EditText etEmail;
    EditText etPassword;

    LoginController loginController;

    private boolean isForeground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Register Otto Bus
        BusProvider.getInstance().register(this);

        //Initializations
        btLogin = (Button) findViewById(R.id.btLogin);
        btLoginSalesforce = (Button) findViewById(R.id.btLoginSalesforce);
        tvForgotPass = (TextView) findViewById(R.id.tvForgotPass);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);

        etEmail.setHint(R.string.email);
        etPassword.setHint(R.string.password);
        etEmail.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        etPassword.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);

        String email = getIntent().getStringExtra("email");
        if(email != null)
            etEmail.setText(email);

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BaseConnection.isConnected(LoginActivity.this))
                    callPassCheck();
                else onConnectionError();
            }
        });

        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    callPassCheck();
                }
                return false;
            }
        });

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                resetError();
            }
        });

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                resetError();
            }
        });

        btLoginSalesforce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(APIWrapper.checkConnectionAndAlert(LoginActivity.this)) {
                    Intent in = new Intent(LoginActivity.this, OAuthActivity.class);
                    in.putExtra("oauth", "sf");
                    startActivity(in);
                }
            }
        });

        tvForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(LoginActivity.this, ForgotPassActivity.class);
                startActivityForResult(in, FORGOT_PASSWORD_ACTIVITY);
            }
        });

        //Button back
        ImageView ivBack = (ImageView) findViewById(R.id.ivBack);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loginController = new LoginController(this);
        loginController.setConnectionCallback(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FORGOT_PASSWORD_ACTIVITY && resultCode == RESULT_OK)
        {
            Utils.showAlert(this, getString(R.string.new_password_sent), getString(R.string.we_have_sent_you_an_email));
        }
    }

    private void resetError()
    {
        if (btLogin.getText().toString().compareTo(getString(R.string.login)) == 0)
            return;

//        etPassword.setError(null, null);
        etPassword.setCompoundDrawables(null, null, null, null);
        btLogin.setText(getString(R.string.login));

        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            btLogin.setBackgroundDrawable(getResources().getDrawable(R.drawable.bt_blue_style));
        } else {
            btLogin.setBackground(getResources().getDrawable(R.drawable.bt_blue_style));
        }
    }

    public void callPassCheck()
    {
        if(etEmail.getText().toString().trim().length() <= 0)
        {
            onLoginError(getString(R.string.oops_wrong_email));
        }
        else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(
                etEmail.getText().toString().trim()).matches())
        {
            onLoginError(getString(R.string.oops_wrong_email));
        }
        else {
            loginController.startLogin(etEmail.getText().toString(), etPassword.getText().toString());
        }
    }

    @Override
    public void onLoginSuccess() {
        Log.d(Constants.TAG, "LoginActivity.onLoginSuccess: ");

        //Load profile
        ((MycommsApp)getApplication()).getProfileIdAndAccessToken();

        if(((MycommsApp)getApplication()).isProfileAvailable())
            goToApp();
    }

    @Override
    public void onLoginError(CharSequence error) {
        Log.e(Constants.TAG, "LoginActivity.onLoginError: ");
        btLogin.setText(error);

        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            btLogin.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.bt_red_style));
        } else {
            btLogin.setBackground(this.getResources().getDrawable(R.drawable.bt_red_style));
        }

        Drawable errorIcon = getResources().getDrawable(R.drawable.ic_error_tooltip);
        errorIcon.setBounds(new Rect(0, 0,
                (int) (errorIcon.getIntrinsicWidth() * 0.5),
                (int) (errorIcon.getIntrinsicHeight() * 0.5)));

//                etPassword.setError(getString(R.string.oops_wrong_password), errorIcon);
        etPassword.setCompoundDrawables(null, null, errorIcon, null);
    }

    @Override
    public void onConnectionNotAvailable() {
        Log.w(Constants.TAG, "LoginActivity.onConnectionNotAvailable: ");
        onLoginError(getString(R.string.connection_error));
    }

    @Override
    public void onConnectionError() {
        onLoginError(getString(R.string.connection_error));
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
        Log.e(Constants.TAG, "LoginActivity.onApplicationAndProfileReadErrorEvent: ");
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
        Intent in = new Intent(LoginActivity.this, DashBoardActivity.class);
        startActivity(in);
        finish();
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
