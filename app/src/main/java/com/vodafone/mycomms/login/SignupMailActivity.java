package com.vodafone.mycomms.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.UserProfile;
import com.vodafone.mycomms.custom.ClearableEditText;
import com.vodafone.mycomms.main.MainActivity;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UncaughtExceptionHandlerController;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONObject;

import java.util.HashMap;

public class SignupMailActivity extends MainActivity {

    ClearableEditText etEmail;
    Drawable errorIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler
                (
                        new UncaughtExceptionHandlerController(this, null)
                );
        setContentView(R.layout.sign_up_mail);

        etEmail = (ClearableEditText)findViewById(R.id.etSignupEmail);
        etEmail.setHint(R.string.email);
        etEmail.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        errorIcon = getResources().getDrawable(R.drawable.ic_error_tooltip);
        errorIcon.setBounds(new Rect(0, 0, errorIcon.getIntrinsicWidth(), errorIcon.getIntrinsicHeight()));

        //Button forward
        ImageView ivBtFwd = (ImageView)findViewById(R.id.ivBtForward);
        ivBtFwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkData()) {
                    if(Utils.checkConnectionAndAlert(SignupMailActivity.this))
                        callEmailCheck();
                }
            }
        });

        //Button back
        ImageView ivBtBack = (ImageView)findViewById(R.id.ivBtBack);
        ivBtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Force hide keyboard
                InputMethodManager mgr = ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
                mgr.hideSoftInputFromWindow(etEmail.getWindowToken(), 0);
                //Finish the activity
                finish();
            }
        });

        //Load data if comes from Salesforce signup
        if(UserProfile.getMail() != null) etEmail.setText(UserProfile.getMail());
    }

    private void callBackEmailCheck(HashMap<String, Object> result)
    {
        if(null != result)
        {
            Log.v(Constants.TAG, "SignupMailActivity.callBackEmailCheck: " + result.toString());

            JSONObject json = null;
            String text = null;
            String status = null;

            status = (String)result.get("status");

            if(result.containsKey("json")) json = (JSONObject)result.get("json");
            else if(result.containsKey("text")) text = (String)result.get("text");

            try {
                String title = null;
                String subtitle = null;

                if (status.compareTo("403") == 0 &&
                        json.get("err") != null &&
                        ((String)json.get("err")).compareTo("auth_proxy_user_error")==0) {
                    title = getString(R.string.user_already_exists);
                    subtitle = getString(R.string.the_entered_email_is_registered_2);
                }
                else if (status.compareTo("400") == 0 &&
                        json.get("err") != null &&
                        ((String)json.get("err")).compareTo("user_domain_not_allowed")==0) {
                    title = getString(R.string.uh_oh);
                    subtitle = "("+status+") "+json.get("des");
                }
                else if (status.compareTo("400") == 0 &&
                        json.get("err") != null &&
                        ((String)json.get("err")).compareTo("invalid_version")==0) {
                    title = getString(R.string.new_version_available);
                    subtitle = getString(R.string.must_update_to_last_application_version);
                }
                else
                {
                    saveData();
                    Intent in = new Intent(SignupMailActivity.this, SignupNameActivity.class);
                    startActivity(in);
                }

                if(title != null)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    LayoutInflater inflater = getLayoutInflater();
                    View view = inflater.inflate(R.layout.cv_title_subtitle, null);
                    ((TextView) view.findViewById(R.id.tvTitle)).setText(title);
                    ((TextView) view.findViewById(R.id.tvSubtitle)).setText(subtitle);
                    builder.setCustomTitle(view);

                    if (status.compareTo("403") == 0 &&
                            json.get("err") != null &&
                            ((String)json.get("err")).compareTo("auth_proxy_user_error")==0) {
                        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent in = new Intent(SignupMailActivity.this, LoginActivity.class);
                                in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                in.putExtra("email", etEmail.getText().toString());
                                startActivity(in);
                                finish();
                            }
                        });
                    }
                    else
                    {
                        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                    }

                    builder.create();
                    builder.show();
                }
            } catch(Exception ex) {
                Log.e(Constants.TAG, "SignupMailActivity.callBackEmailCheck: \n" + ex.toString());
            }
        }
    }

    private void callEmailCheck()
    {
        HashMap<String, Object> header = null;

        HashMap<String, Object> body = new HashMap<>();
        body.put("email", etEmail.getText().toString());
        body.put("password", "123456aA"); //Password dummy, solo para comprobar el mail en este punto

        new CheckEmailApi().execute(body,header);
    }

    private boolean checkData()
    {
        boolean ok = true;

        Drawable errorIcon = getResources().getDrawable(R.drawable.ic_error_tooltip);
        errorIcon.setBounds(new Rect(0, 0,
                (int)(errorIcon.getIntrinsicWidth()*0.5),
                (int)(errorIcon.getIntrinsicHeight()*0.5)));

        if(etEmail.getText().toString().trim().length() <= 0)
        {
            etEmail.setError(
                    getString(R.string.enter_your_email_to_continue),
                    errorIcon);

            ok = false;
        }
        else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(
                    etEmail.getText().toString().trim()).matches())
        {
            etEmail.setError(
                    getString(R.string.incorrect_format),
                    errorIcon);

            ok = false;
        }

        return ok;
    }

    private void saveData ()
    {
        UserProfile.setMail(etEmail.getText().toString());
    }

    private class CheckEmailApi extends AsyncTask<HashMap<String,Object>, Void, HashMap<String,Object>> {
        @Override
        protected HashMap<String,Object> doInBackground(HashMap<String,Object>[] params) {
            return APIWrapper.httpPostAPI("/api/profile", params[0], params[1], SignupMailActivity.this);
        }

        @Override
        protected void onPostExecute(HashMap<String,Object> result) {
            callBackEmailCheck(result);
        }
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
