package com.vodafone.mycomms.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.APIWrapper;

public class SignupTypeChooseActivity extends ActionBarActivity {

    Button mSignupEmail;
    Button mSignupSalesforce;
    ImageView mBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_type_choose);

        mSignupEmail = (Button)findViewById(R.id.btSignupMail);
        mSignupSalesforce = (Button)findViewById(R.id.btSignupSalesforce);
        mBack = (ImageView)findViewById(R.id.btBack);

        mSignupEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(APIWrapper.checkConnectionAndAlert(SignupTypeChooseActivity.this)) {
                    Intent in = new Intent(SignupTypeChooseActivity.this, SignupMailActivity.class);
                    startActivity(in);
                }
            }
        });

        mSignupSalesforce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(APIWrapper.checkConnectionAndAlert(SignupTypeChooseActivity.this)) {
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
