package com.vodafone.mycomms.login;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;

import java.util.HashMap;

public class MailSentActivity extends ActionBarActivity {

    TextView mTVHavingTrouble;
    TextView mWeSent;
    Button mResendEmail;
    String pin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_sent);

        mTVHavingTrouble = (TextView)findViewById(R.id.tvHavingTrouble);
        mResendEmail = (Button)findViewById(R.id.btResendEmail);
        mWeSent = (TextView)findViewById(R.id.tvWeSent);

        HashMap<String, Object> userProfile =
                ((MycommsApp)getApplication()).getUserProfile().getHashMap();
        mWeSent.setText(getString(R.string.we_sent_an_email_to)+"\n"+userProfile.get("email"));

        pin = (String)this.getIntent().getExtras().get("pin");

        mResendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callPhoneCheck(pin);
            }
        });

        //Force hide keyboard
        InputMethodManager mgr = ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE));
        mgr.hideSoftInputFromWindow(mWeSent.getWindowToken(), 0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mail_sent, menu);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent in = new Intent(MailSentActivity.this, LoginSignupActivity.class);
        in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(in);
    }

    public void callPhoneCheck(String pin)
    {
        HashMap<String, Object> header = new HashMap();
        header.put("x-otp-pin", pin);

        HashMap<String, Object> body =
                ((MycommsApp)getApplication()).getUserProfile().getHashMap();

        new CheckPhoneApi().execute(body,header);
    }

    private class CheckPhoneApi extends AsyncTask<HashMap<String,Object>, Void, HashMap<String,Object>> {
        @Override
        protected HashMap<String,Object> doInBackground(HashMap<String,Object>[] params) {
            return APIWrapper.httpPostAPI("/api/profile", params[0], params[1], MailSentActivity.this);
        }
    }
}
