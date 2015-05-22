package com.vodafone.mycomms.login;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.Constants;

import org.json.JSONObject;

import java.util.HashMap;

public class ForgotPassActivity extends ActionBarActivity {

    private static final int FORGOT_PASSWORD_ACTIVITY = 1;

    Button btSend;
    EditText etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);

        btSend = (Button) findViewById(R.id.btSend);
        etEmail = (EditText) findViewById(R.id.etEmail);

        etEmail.setHint(R.string.email);
        etEmail.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callSendNewPass();
            }
        });

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {setError(false, null);}
        });

        //Button back
        ImageView ivBack = (ImageView) findViewById(R.id.ivBack);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setError(boolean error, String errorString)
    {
        if(!error) {
            etEmail.setCompoundDrawables(null, null, null, null);
            btSend.setText(getString(R.string.send_new_password));

            int sdk = Build.VERSION.SDK_INT;
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                btSend.setBackgroundDrawable(getResources().getDrawable(R.drawable.bt_blue_style));
            } else {
                btSend.setBackground(getResources().getDrawable(R.drawable.bt_blue_style));
            }
        }
        else {
            Drawable errorIcon = getResources().getDrawable(R.drawable.ic_error_tooltip);
            errorIcon.setBounds(new Rect(0, 0,
                    (int) (errorIcon.getIntrinsicWidth() * 0.5),
                    (int) (errorIcon.getIntrinsicHeight() * 0.5)));

            etEmail.setCompoundDrawables(null, null, errorIcon, null);

            btSend.setText(errorString);

            int sdk = Build.VERSION.SDK_INT;
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                btSend.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.bt_red_style));
            } else {
                btSend.setBackground(this.getResources().getDrawable(R.drawable.bt_red_style));
            }
        }
    }

    @Override
     public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

    public void callSendNewPass()
    {
        Drawable errorIcon = null;

        if(etEmail.getText().toString().trim().length() <= 0)
        {
            setError(true, getString(R.string.oops_wrong_email));
            return;
        }
        else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(
                etEmail.getText().toString().trim()).matches())
        {
            setError(true, getString(R.string.oops_wrong_email));
            return;
        }

        String email = etEmail.getText().toString();

        HashMap hashEmail = new HashMap<>();
        hashEmail.put("email", etEmail.getText().toString());

        new NewPasswordApi().execute(null, null, hashEmail);
    }

    private void callBackSendNewPass(HashMap<String, Object> result)
    {
        JSONObject json = null;
        String text = null;
        String status = null;

        status = (String)result.get("status");

        try {
            if (status.compareTo("204") != 0) {
                if(result.containsKey("json")) json = (JSONObject)result.get("json");
                    setError(true, (String)json.get("des"));
            }
            else
            {
                //Come back to loginActivity
                finish();
            }
        } catch(Exception ex) {
            Log.e(Constants.TAG, "ForgotPassActivity.callBackSendNewPass: \n" + ex.toString());
            return;
        }
    }

    private class NewPasswordApi extends AsyncTask<HashMap<String,Object>, Void, HashMap<String,Object>> {
        @Override
        protected HashMap<String,Object> doInBackground(HashMap<String,Object>[] params) {
            return APIWrapper.httpGetAPI("/user/" + params[2].get("email") + "/password", params[1], ForgotPassActivity.this);
        }
        @Override
        protected void onPostExecute(HashMap<String,Object> result) {
            callBackSendNewPass(result);
        }
    }
}
