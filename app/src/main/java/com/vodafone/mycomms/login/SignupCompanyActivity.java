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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.UserProfile;
import com.vodafone.mycomms.custom.AutoCompleteTVSelectOnly;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class SignupCompanyActivity extends Activity {

    AutoCompleteTVSelectOnly mCompany;
    EditText mPosition;
    ArrayList<HashMap<String,String>> companies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_company);

        mCompany = (AutoCompleteTVSelectOnly) findViewById(R.id.etSignupCompany);
        mPosition = (EditText)findViewById(R.id.etSignupPosition);

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

        //Force the focus of the first field and opens the keyboard
        InputMethodManager mgr = ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE));
        mgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        mCompany.requestFocus();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }

        this.companies = data;
    }

    private boolean checkData()
    {
        boolean ok = true;

        Drawable errorIcon = getResources().getDrawable(R.drawable.ic_error_tooltip);
        errorIcon.setBounds(new Rect(0, 0,
                (int)(errorIcon.getIntrinsicWidth()*0.5),
                (int)(errorIcon.getIntrinsicHeight()*0.5)));

        if(mCompany.getText().toString().trim().length() <= 0)
        {
            mCompany.setError(
                    getString(R.string.select_your_company_to_continue),
                    errorIcon);

            ok = false;
        }
        else if(mCompany.getPosition() < 0)
        {
            mCompany.setError(
                    getString(R.string.select_your_company_to_continue),
                    errorIcon);

            ok = false;
        }
//        else if(mPosition.getText() != null &&
//                mPosition.getText().toString().trim().length() <= 0)
//        {
//            mPosition.setError(
//                    getString(R.string.enter_your_position_to_continue),
//                    errorIcon);
//
//            ok = false;
//        }

        return ok;
    }

    private void saveData ()
    {
        UserProfile profile = ((MycommsApp)getApplication()).getUserProfile();
        profile.setCountryISO(mCompany.getText().toString());
    }
}
