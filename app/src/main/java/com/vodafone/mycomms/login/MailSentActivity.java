package com.vodafone.mycomms.login;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.UserProfile;
import com.vodafone.mycomms.main.SplashScreenActivity;
import com.vodafone.mycomms.main.connection.MainAppCompatActivity;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.UncaughtExceptionHandlerController;

import java.util.HashMap;

public class MailSentActivity extends MainAppCompatActivity {

    TextView mTVHavingTrouble;
    TextView mWeSent;
    Button mResendEmail;
    String pin = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler
                (
                        new UncaughtExceptionHandlerController(this, null)
                );
        setContentView(R.layout.activity_mail_sent);

        mTVHavingTrouble = (TextView)findViewById(R.id.tvHavingTrouble);
        mResendEmail = (Button)findViewById(R.id.btResendEmail);
        mWeSent = (TextView)findViewById(R.id.tvWeSent);

        HashMap<String, Object> userProfile =
                UserProfile.getHashMap();
        mWeSent.setText(getString(R.string.we_sent_an_email_to) + "\n" + userProfile.get("email"));

        if(this.getIntent().getExtras()!=null &&
                this.getIntent().getExtras().get("pin")!=null)
            pin = (String)this.getIntent().getExtras().get("pin");

        mResendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callPhoneCheck(pin);
            }
        });
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
        if(pin!=null)
            header.put("x-otp-pin", pin);

        HashMap<String, Object> body =
                UserProfile.getHashMap();

        new CheckPhoneApi().execute(body, header);
    }

    private class CheckPhoneApi extends AsyncTask<HashMap<String,Object>, Void, HashMap<String,Object>> {
        @Override
        protected HashMap<String,Object> doInBackground(HashMap<String,Object>[] params) {
            return APIWrapper.httpPostAPI("/api/profile", params[0], params[1], MailSentActivity.this);
        }
    }
}
