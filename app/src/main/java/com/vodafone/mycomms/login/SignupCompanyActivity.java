package com.vodafone.mycomms.login;

import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.UserProfile;
import com.vodafone.mycomms.custom.AutoCompleteTVSelectOnly;
import com.vodafone.mycomms.custom.ClearableEditText;
import com.vodafone.mycomms.main.MainActivity;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UncaughtExceptionHandlerController;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class SignupCompanyActivity extends MainActivity {

    AutoCompleteTVSelectOnly mCompany;
    ClearableEditText mPosition;
    ClearableEditText mOfficeLoc;
    ArrayList<HashMap<String,String>> companies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler
                (
                        new UncaughtExceptionHandlerController(this, null)
                );
        setContentView(R.layout.sign_up_company);

        mCompany = (AutoCompleteTVSelectOnly) findViewById(R.id.etSignupCompany);
        mPosition = (ClearableEditText)findViewById(R.id.etSignupPosition);
        mOfficeLoc = (ClearableEditText)findViewById(R.id.etSignupOfficeLoc);

        mPosition.setHint(R.string.position);
        mPosition.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        mOfficeLoc.setHint(R.string.office_location);
        mOfficeLoc.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        /*
         * Autocomplete Adapter
         */

        // Get the string array

        loadCompaniesArray();
        String[] simpleCompanies = new String[companies.size()];

        for(int i=0; i<companies.size(); i++)
        {
//            simpleCompanies[i] = companies.get(i)[0];
        }

        // Create the adapter and set it to the AutoCompleteTextView
        SimpleAdapter adapter = new SimpleAdapter(
                this,
                companies,
                android.R.layout.simple_list_item_1,
                new String[] { "name" },
                new int[] { android.R.id.text1 });

        mCompany.setAdapter(adapter);
        mCompany.setViews(new int[] { android.R.id.text1 });

        ImageView ivBtFwd = (ImageView)findViewById(R.id.ivBtForward);
        ivBtFwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkData()) {
                    saveData();
                    Intent in = new Intent(SignupCompanyActivity.this, SignupPassActivity.class);
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

        //Load data if comes from Salesforce signup
        if(UserProfile.getCompanyName() != null) mCompany.setText(UserProfile.getCompanyName());
        if(UserProfile.getPosition() != null) mPosition.setText(UserProfile.getPosition());
        if(UserProfile.getOfficeLocation() != null) mOfficeLoc.setText(UserProfile.getOfficeLocation());
    }

    private void loadCompaniesArray()
    {
        /*
         * Delete this code when companies list comes from API
         */
        //Get Data From Text Resource File Contains Json Data.
        InputStream inputStream = getResources().openRawResource(R.raw.companies);
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
            Log.e(Constants.TAG, "SignupCompanyActivity.loadCompaniesArray: \n" + e.toString());
        }
        String json = byteArrayOutputStream.toString();

        /*
         * END Delete...
         */

        ArrayList<HashMap<String,String>> data = null;

        try {
            // Parse the data into jsonObject to get original data in form of json.
            JSONObject jObject = new JSONObject(json);
            JSONArray jArray = jObject.getJSONArray("companies");
            String name;
            String code;
            String isoCode;
            data = new ArrayList<>();
            HashMap hash;

            for (int i = 0; i < jArray.length(); i++) {
                name = jArray.getJSONObject(i).getString("name");
                code = jArray.getJSONObject(i).getString("code");
                hash = new HashMap();
                hash.put("name",name);
                hash.put("code",code);
                data.add(hash);
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "SignupCompanyActivity.loadCompaniesArray: \n" + e.toString());
        }

        this.companies = data;
    }

    private boolean checkData()
    {
        boolean ok = true;

        Drawable errorIcon = getResources().getDrawable(R.drawable.ic_error_tooltip);
        errorIcon.setBounds(new Rect(0, 0,
                (int) (errorIcon.getIntrinsicWidth() * 0.5),
                (int) (errorIcon.getIntrinsicHeight() * 0.5)));

        if(mCompany.getText().toString().trim().length() <= 0)
        {
            mCompany.setError(
                    getString(R.string.select_your_company_to_continue),
                    errorIcon);

            ok = false;
        }
        else if(mCompany.getCodeSelected() == null)
        {
            mCompany.setError(
                    getString(R.string.select_your_company_to_continue),
                    errorIcon);

            ok = false;
        }

        return ok;
    }

    private void saveData ()
    {
        UserProfile.setCompanyName(mCompany.getText().toString());
        UserProfile.setPosition(mPosition.getText().toString());
        UserProfile.setOfficeLocation(mOfficeLoc.getText().toString());
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
