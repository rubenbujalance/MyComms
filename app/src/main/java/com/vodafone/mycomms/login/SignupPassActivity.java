package com.vodafone.mycomms.login;

import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ImageView;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.UserProfile;
import com.vodafone.mycomms.custom.ClearableEditText;
import com.vodafone.mycomms.main.MainActivity;
import com.vodafone.mycomms.util.UncaughtExceptionHandlerController;

import java.util.regex.Pattern;

public class SignupPassActivity extends MainActivity {

    ClearableEditText mPassword;
    ClearableEditText mConfirmPass;

    private static final String PASSWORD_PATTERN =
            "((?=.*\\d)(?=.*[A-Z]).{8,20})";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler
                (
                        new UncaughtExceptionHandlerController(this, null)
                );
        setContentView(R.layout.sign_up_pass);

        mPassword = (ClearableEditText)findViewById(R.id.etSignupPass);
        mConfirmPass = (ClearableEditText)findViewById(R.id.etSignupPassConf);

        mPassword.setHint(R.string.password);
        mPassword.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);

        mConfirmPass.setHint(R.string.confirm_password);
        mConfirmPass.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);

        ImageView ivBtFwd = (ImageView)findViewById(R.id.ivBtForward);
        ivBtFwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkData()) {
                    saveData();
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
        UserProfile.setPassword(mPassword.getText().toString());
    }
}
