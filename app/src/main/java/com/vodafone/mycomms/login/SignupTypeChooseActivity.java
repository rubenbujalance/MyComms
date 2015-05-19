package com.vodafone.mycomms.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
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

    @Override
     public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_signup_type_choose, menu);
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
}
