package com.vodafone.mycomms.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.UserProfile;
import com.vodafone.mycomms.main.SplashScreenActivity;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UserSecurity;

import org.json.JSONObject;

import java.util.HashMap;

public class SignupPincodeActivity extends Activity {

    TextView tvPin1;
    TextView tvPin2;
    TextView tvPin3;
    TextView tvPin4;

    View lnPin1;
    View lnPin2;
    View lnPin3;
    View lnPin4;

    EditText etPin;
    TextView tvPinPhoneNumber;

    Button btResendPin;

    int nextPinPos;
    String pin;
    ProgressDialog progress;
    boolean doingCall = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_pincode);

        tvPin1 = (TextView)findViewById(R.id.tvPin1);
        tvPin2 = (TextView)findViewById(R.id.tvPin2);
        tvPin3 = (TextView)findViewById(R.id.tvPin3);
        tvPin4 = (TextView)findViewById(R.id.tvPin4);

        lnPin1 = findViewById(R.id.lnPin1);
        lnPin2 = findViewById(R.id.lnPin2);
        lnPin3 = findViewById(R.id.lnPin3);
        lnPin4 = findViewById(R.id.lnPin4);

        etPin = (EditText)findViewById(R.id.etPin);
        tvPinPhoneNumber = (TextView)findViewById(R.id.tvPinPhoneNumber);
        btResendPin = (Button)findViewById(R.id.btResendPin);
        nextPinPos = 1;

        //Set the phone number text on the initial text
        tvPinPhoneNumber.setText(
                getString(R.string.your_sms_verification_code_has_been_sent_to) + " " +
                        getIntent().getStringExtra("phoneNumber"));

        //Every time a key is pressed, it has to be written to the correct field
        etPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s==null || s.length()==0) return;

                String text = s.toString();
                text = text.substring(text.length());

                if(!text.matches("\\d+")) return;

                if (nextPinPos == 1) {
                    //Start new pin
                    if (tvPin1.getText().toString().trim().length() > 0) {
                        setPinColor(Color.WHITE);
                        resetPin();
                    }
                    tvPin1.setText(text);
                    pin = text;
                } else if (nextPinPos == 2) {
                    tvPin2.setText(text);
                    pin += text;
                } else if (nextPinPos == 3) {
                    tvPin3.setText(text);
                    pin += text;
                } else if (nextPinPos == 4) {
                    tvPin4.setText(text);
                    pin += text;

                    if(APIWrapper.checkConnectionAndAlert(SignupPincodeActivity.this))
                        callPhoneCheck(pin);
                }

                if (nextPinPos < 4) nextPinPos++;
                else nextPinPos = 1;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

//        etPin.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                String text;
//
//                if (nextPinPos == 1) {
//                    //Start new pin
//                    if (tvPin1.getText().toString().trim().length() > 0) {
//                        setPinColor(Color.WHITE);
//                        resetPin();
//                    }
//                    text = String.valueOf((char) event.getUnicodeChar());
//                    tvPin1.setText(text);
//                    pin = text;
//                } else if (nextPinPos == 2) {
//                    text = String.valueOf((char) event.getUnicodeChar());
//                    tvPin2.setText(text);
//                    pin += text;
//                } else if (nextPinPos == 3) {
//                    text = String.valueOf((char) event.getUnicodeChar());
//                    tvPin3.setText(text);
//                    pin += text;
//                } else if (nextPinPos == 4) {
//                    text = String.valueOf((char) event.getUnicodeChar());
//                    tvPin4.setText(text);
//                    pin += text;
//
//                    if(APIWrapper.checkConnectionAndAlert(SignupPincodeActivity.this))
//                        callPhoneCheck(pin);
//                }
//
//                if (nextPinPos < 4) nextPinPos++;
//                else nextPinPos = 1;
//
//                return true;
//            }
//        });

        btResendPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callPhoneCheck(null);
            }
        });

        //Button back
        ImageView ivBtBack = (ImageView)findViewById(R.id.ivBtBack);
        ivBtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Force the focus of the first field and opens the keyboard
        InputMethodManager mgr = ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE));
        mgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        etPin.requestFocus();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(APIWrapper.checkConnectionAndAlert(SignupPincodeActivity.this))
            callPhoneCheck(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pincode, menu);
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

    public void callPhoneCheck(String pin)
    {
        doingCall = true;
        HashMap<String, Object> header = null;

        if(pin != null) {
            header = new HashMap();
            header.put("x-otp-pin", pin);
        }

        HashMap<String, Object> body =
                UserProfile.getHashMap();

        new CheckPhoneApi().execute(body,header);
    }

    public void setPinColor(int color)
    {
        tvPin1.setTextColor(color);
        tvPin2.setTextColor(color);
        tvPin3.setTextColor(color);
        tvPin4.setTextColor(color);

        lnPin1.setBackgroundColor(color);
        lnPin2.setBackgroundColor(color);
        lnPin3.setBackgroundColor(color);
        lnPin4.setBackgroundColor(color);
    }

    public void resetPin()
    {
        etPin.setText("");
        tvPin1.setText("");
        tvPin2.setText("");
        tvPin3.setText("");
        tvPin4.setText("");
    }

    private void callBackPhoneCheck(HashMap<String, Object> result)
    {
        Log.v(Constants.TAG, "SignupPincodeActivity.callBackPhoneCheck: " + result.toString());

        JSONObject json = null;
        String text = null;
        String status = null;

        status = (String)result.get("status");

        if(result.containsKey("json")) json = (JSONObject)result.get("json");
        else if(result.containsKey("text")) text = (String)result.get("text");

        try {
            if (status.compareTo("200") == 0) {
                setPinColor(Color.WHITE);

                //Save avatar in shared preferences
//                UserProfile.

                //Force hide keyboard
                InputMethodManager mgr = ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE));
                mgr.hideSoftInputFromWindow(etPin.getWindowToken(), 0);

                //Start "Email sent" activity
                Intent in = new Intent(getApplicationContext(), MailSentActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                in.putExtra("pin",pin); //We'll need the pin to resend email
                startActivity(in);
            }
            else if (status.compareTo("201") == 0) {
                //User created from OAuth, not necessary to check mail
                //We have accessToken to go to app

                //Delete the oauth from userProfile
                UserProfile.setOauth(null);
                UserProfile.setOauthPrefix(null);
                setPinColor(Color.WHITE);

                //Force hide keyboard
                InputMethodManager mgr = ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE));
                mgr.hideSoftInputFromWindow(etPin.getWindowToken(), 0);

                //Get tokens and expiration data from http response
                JSONObject jsonResponse = (JSONObject)result.get("json");
                String accessToken = jsonResponse.getString("accessToken");
                String refreshToken = jsonResponse.getString("refreshToken");
                long expiresIn = jsonResponse.getLong("expiresIn");

                UserSecurity.setTokens(accessToken, refreshToken, expiresIn, this);

                //Go to splashcreen which will check everything before entering app
                Intent in = new Intent(getApplicationContext(), SplashScreenActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(in);
                finish();
            }
            else
            {
                if(pin != null && pin.trim().length()>0)
                    setPinColor(Color.RED);
            }
        } catch(Exception ex) {
            Log.e(Constants.TAG, "SignupPincodeActivity.callBackPhoneCheck: \n" + ex.toString());
            doingCall = false;
            return;
        }

        doingCall = false;
    }

    private class CheckPhoneApi extends AsyncTask<HashMap<String,Object>, Void, HashMap<String,Object>> {
        @Override
        protected HashMap<String,Object> doInBackground(HashMap<String,Object>[] params) {
            return APIWrapper.httpPostAPI("/api/profile",params[0],params[1], SignupPincodeActivity.this);
        }
        @Override
        protected void onPostExecute(HashMap<String,Object> result) {
            callBackPhoneCheck(result);
        }
    }
}
