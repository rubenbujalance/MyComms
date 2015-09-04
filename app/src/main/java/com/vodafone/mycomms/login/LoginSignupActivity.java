package com.vodafone.mycomms.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.OKHttpErrorReceivedEvent;
import com.vodafone.mycomms.main.MainActivity;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UncaughtExceptionHandlerController;

public class LoginSignupActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler
                (
                        new UncaughtExceptionHandlerController(this, null)
                );
        setContentView(R.layout.login_signup_choose);

        //Register Otto Bus
        BusProvider.getInstance().register(this);

        Button btSignup = (Button)findViewById(R.id.btSignup);
            btSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(LoginSignupActivity.this, SignupTypeChooseActivity.class);
                startActivity(in);
            }
        });

        Button btLogin = (Button)findViewById(R.id.btLogin);
        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                APIWrapper.httpPostAPI("/api/profile", new JSONObject(), null);
                Intent in = new Intent(LoginSignupActivity.this, LoginActivity.class);
                startActivity(in);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Subscribe
    public void onOKHttpErrorReceived(OKHttpErrorReceivedEvent event) {
        Log.i(Constants.TAG, "LoginSignupActivity.onOKHttpErrorReceived: ");
        String errorMessage = event.getErrorMessage();
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        MycommsApp.activityStarted();
    }

    @Override
    public void onStop()
    {
        MycommsApp.activityStopped();
        super.onStop();
    }
}
