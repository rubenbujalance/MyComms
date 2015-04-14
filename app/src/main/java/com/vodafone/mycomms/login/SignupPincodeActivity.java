package com.vodafone.mycomms.login;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.mycomms.R;

public class SignupPincodeActivity extends Activity {

    TextView tvPin1;
    TextView tvPin2;
    TextView tvPin3;
    TextView tvPin4;
    EditText etPin;
    TextView tvPinPhoneNumber;
    int nextPinPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_pincode);

        tvPin1 = (TextView)findViewById(R.id.tvPin1);
        tvPin2 = (TextView)findViewById(R.id.tvPin2);
        tvPin3 = (TextView)findViewById(R.id.tvPin3);
        tvPin4 = (TextView)findViewById(R.id.tvPin4);
        etPin = (EditText)findViewById(R.id.etPin);
        tvPinPhoneNumber = (TextView)findViewById(R.id.tvPinPhoneNumber);
        nextPinPos = 1;

        //Request focus to the editText
        etPin.requestFocus();
        InputMethodManager mgr = ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE));
        mgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        //Set the phone number text on the initial text
        tvPinPhoneNumber.setText(
                getString(R.string.your_sms_verification_code_has_been_sent_to) + " " +
                        getIntent().getStringExtra("phoneNumber"));

        etPin.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction()!=KeyEvent.ACTION_UP)
                    return true;

                String text;

                if(nextPinPos == 1)
                {
                    text = String.valueOf((char)event.getUnicodeChar());
                    ((SignupPincodeActivity)v.getContext()).tvPin1.setText(text);
                }
                else if(nextPinPos == 2)
                {
                    text = String.valueOf((char)event.getUnicodeChar());
                    ((SignupPincodeActivity)v.getContext()).tvPin2.setText(text);
                }
                else if(nextPinPos == 3)
                {
                    text = String.valueOf((char)event.getUnicodeChar());
                    ((SignupPincodeActivity)v.getContext()).tvPin3.setText(text);
                }
                else if(nextPinPos == 4)
                {
                    text = String.valueOf((char)event.getUnicodeChar());
                    ((SignupPincodeActivity)v.getContext()).tvPin4.setText(text);
                    callPinCheck();
                }

                if(nextPinPos<4) nextPinPos++;
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

    public void callPinCheck()
    {
//        tvPin1.setTextColor("#ff0000");
    }
}
