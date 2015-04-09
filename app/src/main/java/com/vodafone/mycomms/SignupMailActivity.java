package com.vodafone.mycomms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

public class SignupMailActivity extends Activity {

    EditText mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_mail);

        mEmail = (EditText)findViewById(R.id.etSignupEmail);

        //Button forward
        ImageView ivBtFwd = (ImageView)findViewById(R.id.ivBtForward);
        ivBtFwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkData()) {
                    saveData();
                    Intent in = new Intent(SignupMailActivity.this, SignupPassActivity.class);
                    startActivity(in);
                }
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
        InputMethodManager inputMethodManager = ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE));
        inputMethodManager.showSoftInput(findViewById(R.id.etSignupFirstN), InputMethodManager.SHOW_IMPLICIT);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_signup_mail, menu);
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

    private boolean checkData()
    {
        boolean ok = true;

        if(mEmail.getText().toString().trim().length() <= 0)
        {
            mEmail.setError(
                    getString(R.string.enter_your_email_to_continue));

            ok = false;
        }
        else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(
                    mEmail.getText().toString().trim()).matches())
        {
            mEmail.setError(
                    getString(R.string.incorrect_format));

            ok = false;
        }

        return ok;
    }

    private void saveData ()
    {
        UserProfile profile = ((MycommsApp)getApplication()).getUserProfile();
        profile.setMail(mEmail.getText().toString());
    }
}
