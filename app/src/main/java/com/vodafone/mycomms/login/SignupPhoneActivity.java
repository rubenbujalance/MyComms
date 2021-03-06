package com.vodafone.mycomms.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.UserProfile;
import com.vodafone.mycomms.custom.AutoCompleteTVSelectOnly;
import com.vodafone.mycomms.custom.ClearableEditText;
import com.vodafone.mycomms.main.MainActivity;
import com.vodafone.mycomms.main.SplashScreenActivity;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UncaughtExceptionHandlerController;
import com.vodafone.mycomms.util.UserSecurity;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class SignupPhoneActivity extends MainActivity {

    AutoCompleteTVSelectOnly mCountry;
    ClearableEditText mPhone;
    ArrayList<HashMap<String,String>> countries;
    HashMap<String,HashMap<String,String>> hashCountries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler
                (
                        new UncaughtExceptionHandlerController(this, null)
                );
        setContentView(R.layout.sign_up_phone);

        mCountry = (AutoCompleteTVSelectOnly) findViewById(R.id.etSignupCountry);
        mPhone = (ClearableEditText)findViewById(R.id.etSignupPhone);

        mPhone.setHint(R.string.phone_number);
        mPhone.setInputType(
                InputType.TYPE_CLASS_PHONE);

        //Write SIM phone number as default
        String phoneNumber = "";

        try {
            TelephonyManager tMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            phoneNumber = tMgr.getLine1Number();
        } catch (Exception e) {
            Log.e(Constants.TAG, "SignupPhoneActivity.onCreate: " +
                    "Error obtaining SIM phone number",e);
        }

        mPhone.setText(phoneNumber);

        loadCountriesArray();

        /*
         * Autocomplete Adapter
         */

        // Create the adapter and set it to the AutoCompleteTextView
        SimpleAdapter adapter = new SimpleAdapter(
                this,
                countries,
                R.layout.cv_adapter_two_values,
                new String[] { "name","dial_code" },
                new int[] { R.id.tvMainValue, R.id.tvSecondValue });

        mCountry.setAdapter(adapter);
        mCountry.setViews(new int[] { R.id.tvMainValue, R.id.tvSecondValue });

        //Button forward
        ImageView ivBtFwd = (ImageView)findViewById(R.id.ivBtForward);
        ivBtFwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkData()) {
                    saveData();
                    if(Utils.checkConnectionAndAlert(SignupPhoneActivity.this))
                        callProfileCheck();
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

        //Load data if comes from OAuth signup
        if(UserProfile.getCountryISO() != null) {

            String country = hashCountries.get(UserProfile.getCountryISO()).get("name") + " " +
                                hashCountries.get(UserProfile.getCountryISO()).get("dial_code");
            String code = hashCountries.get(UserProfile.getCountryISO()).get("code");

            if(country != null)
            {
                mCountry.setCodeSelected(code);
                mCountry.setText(country);
            }
        }

        if(UserProfile.getPhone() != null) mPhone.setText(UserProfile.getPhone());
    }

    private void loadCountriesArray()
    {
        //Get Data From Text Resource File Contains Json Data.
        InputStream inputStream = getResources().openRawResource(R.raw.countries_new);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int ctr;
        try {
            ctr = inputStream.read();
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr);
                ctr = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            Log.e(Constants.TAG, "SignupPhoneActivity.loadCountriesArray: \n" + e.toString());
        }

        ArrayList<HashMap<String,String>> data = null;
        HashMap<String,HashMap<String,String>> hash = null;

        try {
            // Parse the data into jsonObject to get original data in form of json.
//            JSONObject jObject = new JSONObject(
//                    byteArrayOutputStream.toString());
            JSONArray jArray = new JSONArray(byteArrayOutputStream.toString());
//            JSONArray jArray = jObject.getJSONArray(null);
            String name;
            String dial_code;
            String isoCode;
            data = new ArrayList<>();
            hash = new HashMap<>();
            HashMap<String,String> map;

            for (int i = 0; i < jArray.length(); i++) {
                name = jArray.getJSONObject(i).getString("name");
                dial_code = "+" + jArray.getJSONObject(i).getString("Dial");
                isoCode = jArray.getJSONObject(i).getString("ISO3166-1-Alpha-2");
                map = new HashMap<>(3);
                map.put("name",name);
                map.put("dial_code",dial_code);
                map.put("code",isoCode);
                data.add(map);
                hash.put(isoCode,map);
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "SignupPhoneActivity.loadCountriesArray: \n" + e.toString());
        }

        this.countries = data;
        this.hashCountries = hash;
    }

    private boolean checkData()
    {
        boolean ok = true;

        Drawable errorIcon = getResources().getDrawable(R.drawable.ic_error_tooltip);
        errorIcon.setBounds(new Rect(0, 0,
                (int)(errorIcon.getIntrinsicWidth()*0.5),
                (int)(errorIcon.getIntrinsicHeight()*0.5)));

        if(mCountry.getText().toString().trim().length() <= 0)
        {
            mCountry.setError(
                    getString(R.string.select_your_phone_country_to_continue),
                    errorIcon);

            ok = false;
        }
        else if(mCountry.getCodeSelected() == null)
        {
            mCountry.setError(
                    getString(R.string.select_a_country_from_the_list),
                    errorIcon);

            ok = false;
        }
        else if(mPhone.getText().toString().trim().length() <= 0)
        {
            mPhone.setError(
                    getString(R.string.enter_your_phone_number_to_continue),
                    errorIcon);

            ok = false;
        }

        return ok;
    }

    private void saveData ()
    {
        UserProfile.setCountryISO(mCountry.getCodeSelected());
        UserProfile.setPhone(mPhone.getText().toString());
    }

    private void goToPincodeActivity() {
        Intent in = new Intent(SignupPhoneActivity.this, SignupPincodeActivity.class);
        String dialCode = mCountry.getText().toString().trim();
        dialCode = dialCode.substring(dialCode.lastIndexOf(" "));
        in.putExtra("phoneNumber", dialCode + " " + mPhone.getText().toString());
        startActivity(in);
    }

    public void callProfileCheck()
    {
        HashMap<String, Object> body =
                UserProfile.getHashMap();

        new CheckProfileApi().execute(body, new HashMap<String, Object>());
    }

    private void callBackPhoneCheck(HashMap<String, Object> result)
    {
        Log.i(Constants.TAG, "SignupPhoneActivity.callBackPhoneCheck: " + result.toString());

        String status = (String)result.get("status");

        try {
            if (status.compareTo("200") == 0) {
                //User have to check e-mail

                //Force hide keyboard
                InputMethodManager mgr = ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE));
                mgr.hideSoftInputFromWindow(mCountry.getWindowToken(), 0);
                mgr.hideSoftInputFromWindow(mPhone.getWindowToken(), 0);

                //Start "Email sent" activity
                Intent in = new Intent(getApplicationContext(), MailSentActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(in);
            }
            else if (status.compareTo("201") == 0) {
                //User created from OAuth, not necessary to check mail
                //We have accessToken to go to app

                //Delete the oauth from userProfile
                UserProfile.setOauth(null);
                UserProfile.setOauthPrefix(null);

                //Force hide keyboard
                InputMethodManager mgr = ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE));
                mgr.hideSoftInputFromWindow(mCountry.getWindowToken(), 0);
                mgr.hideSoftInputFromWindow(mPhone.getWindowToken(), 0);

                //Get tokens and expiration data from http response
                JSONObject jsonResponse = (JSONObject)result.get("json");
                String accessToken = jsonResponse.getString("accessToken");
                String refreshToken = jsonResponse.getString("refreshToken");
                long expiresIn = jsonResponse.getLong("expiresIn");

                UserSecurity.setTokens(accessToken, refreshToken, expiresIn, this);

                //Go to splashcreen which will check everything before entering app
                Intent in = new Intent(getApplicationContext(), SplashScreenActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(in);
                finish();
            }
            else if (status.compareTo("403") == 0)
            {
                goToPincodeActivity();
            }
            else
            {
                Toast.makeText(SignupPhoneActivity.this, getResources().getString(R.string.error_reading_data_from_server), Toast.LENGTH_LONG).show();
            }
        } catch(Exception ex) {
            Log.e(Constants.TAG, "SignupPhoneActivity.callBackPhoneCheck: ",ex);
            Crashlytics.logException(ex);
        }
    }

    private class CheckProfileApi extends AsyncTask<HashMap<String,Object>, Void, HashMap<String,Object>> {
        @Override
        protected HashMap<String,Object> doInBackground(HashMap<String,Object>[] params) {
            return APIWrapper.httpPostAPI("/api/profile",params[0],params[1],
                    SignupPhoneActivity.this);
        }
        @Override
        protected void onPostExecute(HashMap<String,Object> result)
        {
            if(null != result)
                callBackPhoneCheck(result);
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
