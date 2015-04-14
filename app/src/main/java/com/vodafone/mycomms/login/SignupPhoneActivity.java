package com.vodafone.mycomms.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;

import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.UserProfile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class SignupPhoneActivity extends Activity {

    AutoCompleteTextView mCountry;
    EditText mPhone;
    ArrayList<String[]> countries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_phone);

        mCountry = (AutoCompleteTextView) findViewById(R.id.etSignupCountry);
        mPhone = (EditText)findViewById(R.id.etSignupPhone);

        /*
         * Autocomplete Adapter
         */

        // Get the string array

        loadCountriesArray();
        String[] simpleCountries = new String[countries.size()];

        for(int i=0; i<countries.size(); i++)
        {
            simpleCountries[i] = countries.get(i)[1] + " " + countries.get(i)[2];
        }

        // Create the adapter and set it to the AutoCompleteTextView
        ArrayAdapter adapter =
                new ArrayAdapter (this, android.R.layout.simple_list_item_1, simpleCountries);
        mCountry.setAdapter(adapter);

        /*
         */

        ImageView ivBtFwd = (ImageView)findViewById(R.id.ivBtForward);
        ivBtFwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkData()) {
                    Intent in = new Intent(SignupPhoneActivity.this, SignupPincodeActivity.class);
                    String dialCode = countries.get(mCountry.getListSelection())[2];
                    in.putExtra("phoneNumber",dialCode + " " + mPhone.getText().toString());
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

        (findViewById(R.id.etSignupCountry)).requestFocus();
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
        InputStream inputStream = getResources().openRawResource(R.raw.countries);
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

        ArrayList<String[]> data = null;

        try {
            // Parse the data into jsonObject to get original data in form of json.
            JSONObject jObject = new JSONObject(
                    byteArrayOutputStream.toString());
            JSONArray jArray = jObject.getJSONArray("countries");
            String name;
            String dial_code;
            String isoCode;
            data = new ArrayList<>();

            for (int i = 0; i < jArray.length(); i++) {
                name = jArray.getJSONObject(i).getString("name");
                dial_code = jArray.getJSONObject(i).getString("dial_code");
                isoCode = jArray.getJSONObject(i).getString("code");

                data.add(new String[]{isoCode,name,dial_code});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.countries = data;
    }

    private boolean checkData()
    {
        boolean ok = true;

        if(mCountry.getText().toString().trim().length() <= 0)
        {
            mCountry.setError(
                    getString(R.string.enter_your_phone_country_to_continue));

            ok = false;
        }
        else if(mPhone.getText().toString().trim().length() <= 0)
        {
            mPhone.setError(
                    getString(R.string.enter_your_phone_number_to_continue));

            ok = false;
        }

        return ok;
    }

    private void saveData ()
    {
        UserProfile profile = ((MycommsApp)getApplication()).getUserProfile();
        profile.setCountryISO(mCountry.getText().toString());
    }
}
