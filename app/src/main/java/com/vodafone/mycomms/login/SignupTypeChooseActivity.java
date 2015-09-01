package com.vodafone.mycomms.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.main.connection.MainAppCompatActivity;
import com.vodafone.mycomms.util.UncaughtExceptionHandlerController;
import com.vodafone.mycomms.util.Utils;

public class SignupTypeChooseActivity extends MainAppCompatActivity {

    Button mSignupEmail;
    Button mSignupSalesforce;
    ImageView mBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler
                (
                        new UncaughtExceptionHandlerController(this, null)
                );
        setContentView(R.layout.signup_type_choose);

        mSignupEmail = (Button)findViewById(R.id.btSignupMail);
        mSignupSalesforce = (Button)findViewById(R.id.btSignupSalesforce);
        mBack = (ImageView)findViewById(R.id.btBack);

        mSignupEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utils.checkConnectionAndAlert(SignupTypeChooseActivity.this)) {
                    Intent in = new Intent(SignupTypeChooseActivity.this, SignupMailActivity.class);
                    startActivity(in);
                }
            }
        });

        mSignupSalesforce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utils.checkConnectionAndAlert(SignupTypeChooseActivity.this)) {
                    Intent in = new Intent(SignupTypeChooseActivity.this, OAuthActivity.class);
                    in.putExtra("oauth", "sf");
                    startActivity(in);
                }
            }
        });

        //Button back
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
