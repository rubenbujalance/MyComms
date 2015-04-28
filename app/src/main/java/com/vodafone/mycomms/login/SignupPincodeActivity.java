package com.vodafone.mycomms.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.mycomms.APIWrapper;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

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
    ProgressDialog progress;

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
        etPin.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() != KeyEvent.ACTION_UP)
                    return true;

                String text;

                if (nextPinPos == 1) {
                    if (((SignupPincodeActivity) v.getContext()).tvPin1.getText().toString().trim().length() > 0) {
                        setPinColor(Color.WHITE);
                        resetPin();
                    }

                    text = String.valueOf((char) event.getUnicodeChar());
                    ((SignupPincodeActivity) v.getContext()).tvPin1.setText(text);
                } else if (nextPinPos == 2) {
                    text = String.valueOf((char) event.getUnicodeChar());
                    ((SignupPincodeActivity) v.getContext()).tvPin2.setText(text);
                } else if (nextPinPos == 3) {
                    text = String.valueOf((char) event.getUnicodeChar());
                    ((SignupPincodeActivity) v.getContext()).tvPin3.setText(text);
                } else if (nextPinPos == 4) {
                    text = String.valueOf((char) event.getUnicodeChar());
                    ((SignupPincodeActivity) v.getContext()).tvPin4.setText(text);
                    callPhoneCheck(etPin.getText().toString());
                }

                if (nextPinPos < 4) nextPinPos++;
                else nextPinPos = 1;

                return true;
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
        HashMap<String, Object> header = null;

        if(pin != null) {
            header = new HashMap();
            header.put("x-otp-pin", pin);
        }

        HashMap<String, Object> body =
                ((MycommsApp)getApplication()).getUserProfile().getHashMap();

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
        tvPin1.setText("");
        tvPin2.setText("");
        tvPin3.setText("");
        tvPin4.setText("");
    }

    private void callBackPinCheck(HashMap<String,Object> result)
    {
        System.out.print(result);

        JSONObject json = null;
        String text = null;
        String status = null;

        status = (String)result.get("status");

        if(result.containsKey("json")) json = (JSONObject)result.get("json");
        else if(result.containsKey("text")) text = (String)result.get("text");

        try {
            if (status.compareTo("400") == 0 &&
                    json.get("err") != null) {

            }
        } catch(Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    private class CheckPhoneApi extends AsyncTask<HashMap<String,Object>, Void, HashMap<String,Object>> {

        @Override
        protected HashMap<String,Object> doInBackground(HashMap<String,Object>[] params) {
            HashMap<String,Object> response = null;
            HashMap<String,Object> hashParams = params[0];
            HashMap<String,Object> hashHeaders = params[1];

            //Build the JSONObject params
//            Iterator<String> it = hashJsonParams.keySet().iterator();
//            String key,value = null;
//            JSONObject httpParams = new JSONObject();
//
//            try {
//                while (it.hasNext()) {
//                    key = it.next();
//                    value = hashJsonParams.get(key);
//                    httpParams.put(key, value);
//                }
//            } catch (Exception ex) { ex.printStackTrace(); }

            response = APIWrapper.httpPostAPI("/api/profile",hashParams,hashHeaders, SignupPincodeActivity.this);

            return response;
        }

        @Override
        protected void onPostExecute(HashMap<String,Object> result) {
            callBackPinCheck(result);
        }
    }
}
