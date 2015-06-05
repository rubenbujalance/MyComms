package com.vodafone.mycomms.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.mycomms.ContactListMainActivity;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.login.connection.ILoginConnectionCallback;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UserSecurity;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONObject;

import java.util.HashMap;

public class LoginActivity extends ActionBarActivity implements ILoginConnectionCallback {

    private static final int FORGOT_PASSWORD_ACTIVITY = 1;

    Button btLoginSalesforce;
    Button btLogin;
    TextView tvForgotPass;
    EditText etEmail;
    EditText etPassword;

    LoginController loginController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
                    callPassCheck();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

//    private void callBackPassCheck(HashMap<String, Object> result)
//    {
//        String status = (String)result.get("status");
//
//        try {
//            if (status.compareTo("200") != 0) {
//               onLoginError(getString(R.string.oops_wrong_password));
//            }
//            else
//            {
//                //Get tokens and expiration data from http response
//                JSONObject jsonResponse = (JSONObject)result.get("json");
//                String accessToken = jsonResponse.getString("accessToken");
//                String refreshToken = jsonResponse.getString("refreshToken");
//                long expiresIn = jsonResponse.getLong("expiresIn");
//
//                UserSecurity.setTokens(accessToken, refreshToken, expiresIn, this);
//
//                startMainActivity();
//            }
//        } catch(Exception ex) {
//            Log.e(Constants.TAG, "LoginActivity.callBackPassCheck:" , ex);
//            return;
//        }
//    }

    private void startMainActivity(){
        //Force hide keyboard
        InputMethodManager mgr = ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE));
        mgr.hideSoftInputFromWindow(etEmail.getWindowToken(), 0);


        //Start Main activity
        Intent in = new Intent(LoginActivity.this, ContactListMainActivity.class);

        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(in);
        finish();
    }
    @Override
    public void onLoginSuccess() {
        Log.d(Constants.TAG, "LoginActivity.onLoginSuccess: ");
        startMainActivity();
//        TestConnection testConnection = new TestConnection(this.getApplicationContext(), this.loginController);
//        testConnection.request();
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

//    private class CheckPasswordApi extends AsyncTask<HashMap<String,Object>, Void, HashMap<String,Object>> {
//        @Override
//        protected HashMap<String,Object> doInBackground(HashMap<String,Object>[] params) {
//            return APIWrapper.httpPostAPI("/auth/login", params[0], params[1], LoginActivity.this);
//        }
//        @Override
//        protected void onPostExecute(HashMap<String,Object> result) {
//            callBackPassCheck(result);
//        }
//    }
}
