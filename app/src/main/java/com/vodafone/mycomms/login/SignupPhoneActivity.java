package com.vodafone.mycomms.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.opengl.Visibility;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.UserProfile;
import com.vodafone.mycomms.custom.AutoCompleteTVSelectOnly;
import com.vodafone.mycomms.custom.ClearableEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class SignupPhoneActivity extends Activity {

    AutoCompleteTVSelectOnly mCountry;
    ClearableEditText mPhone;
    ArrayList<HashMap<String,String>> countries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_phone);

        mCountry = (AutoCompleteTVSelectOnly) findViewById(R.id.etSignupCountry);
        mPhone = (ClearableEditText)findViewById(R.id.etSignupPhone);

        mPhone.setHint(R.string.phone_number);
        mPhone.setInputType(
                InputType.TYPE_CLASS_PHONE);

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
                    Intent in = new Intent(SignupPhoneActivity.this, SignupPincodeActivity.class);
                    String dialCode = mCountry.getText().toString().trim();
                    dialCode = dialCode.substring(dialCode.lastIndexOf(" "));
                    in.putExtra("phoneNumber",dialCode + " " + mPhone.getText().toString());
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
        InputMethodManager mgr = ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE));
        mgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        mCountry.requestFocus();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_signup_phone, menu);
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
            e.printStackTrace();
        }

        ArrayList<HashMap<String,String>> data = null;

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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.countries = data;
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
        UserProfile profile = ((MycommsApp)getApplication()).getUserProfile();
        profile.setCountryISO(mCountry.getCodeSelected());
        profile.setPhone(mPhone.getText().toString());
    }
}
