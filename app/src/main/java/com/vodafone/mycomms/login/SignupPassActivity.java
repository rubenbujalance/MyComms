package com.vodafone.mycomms.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.UserProfile;

import java.util.regex.Pattern;

public class SignupPassActivity extends Activity {

    EditText mPassword;
    EditText mConfirmPass;

    private static final String PASSWORD_PATTERN =
            "((?=.*\\d)(?=.*[A-Z]).{8,20})";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_pass);

        mPassword = (EditText)findViewById(R.id.etSignupPass);
        mConfirmPass = (EditText)findViewById(R.id.etSignupPassConf);

        ImageView ivBtFwd = (ImageView)findViewById(R.id.ivBtForward);
        ivBtFwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkData()) {
                    Intent in = new Intent(SignupPassActivity.this, SignupPhoneActivity.class);
                    startActivity(in);
                }
            }
        });

        ImageView ivBtBack = (ImageView)findViewById(R.id.ivBtBack);
        ivBtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Force show keyboard
        InputMethodManager mgr = ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
        mgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        mPassword.requestFocus();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_singup_pass, menu);
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

        Drawable errorIcon = getResources().getDrawable(R.drawable.ic_error_tooltip);
        errorIcon.setBounds(new Rect(0, 0,
                (int)(errorIcon.getIntrinsicWidth()*0.5),
                (int)(errorIcon.getIntrinsicHeight()*0.5)));

        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

        if(!pattern.matcher(mPassword.getText().toString().trim()).matches())
        {
            mPassword.setError(
                    getString(R.string.incorrect_format),
                    errorIcon);

            ok = false;
        }
        else if(mPassword.getText().toString().trim().compareTo(
                    mConfirmPass.getText().toString().trim()) != 0)
        {
            mConfirmPass.setError(
                    getString(R.string.passwords_do_not_match),
                    errorIcon);

            ok = false;
        }

        return ok;
    }

    private void saveData ()
    {
        UserProfile profile = ((MycommsApp)getApplication()).getUserProfile();
        profile.setPassword(mPassword.getText().toString());
    }
}
