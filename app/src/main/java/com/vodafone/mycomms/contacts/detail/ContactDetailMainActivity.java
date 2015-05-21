package com.vodafone.mycomms.contacts.detail;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chat.ChatMainActivity;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.realm.Realm;
import model.Contact;

public class ContactDetailMainActivity extends ToolbarActivity implements IContactDetailConnectionCallback {
    private Realm realm;
    private Contact contact;
    private ContactDetailController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.contact_detail);
        realm = Realm.getInstance(this);

        Intent intent = getIntent();
        String contactId = intent.getExtras().getString(Constants.CONTACT_ID);
        controller = new ContactDetailController(this, realm);
        controller.setConnectionCallback(this);
        contact = getContact(contactId);


        
        ImageView ivBtBack = (ImageView)findViewById(R.id.ivBtBack);
        ivBtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageView btnChat = (ImageView)findViewById(R.id.btchat);

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ContactDetailMainActivity.this, ChatMainActivity.class));
            }
        });
    }

    private String getElementFromJsonArrayString(String jsonArrayString, String key){
        Log.d(Constants.TAG, "ContactDetailMainActivity.getElementFromJsonArrayString: " + jsonArrayString + ", key=" + key);
        JSONObject jsonObject = null;
        String result = null;
        try {
            JSONArray jsonArray = new JSONArray(jsonArrayString);
            for(int i = 0; i < jsonArray.length() ; i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if (!jsonObject.isNull(key)) {

                   result = jsonObject.getString(key);
                }
            }

            Log.d(Constants.TAG, "ContactDetailMainActivity.getElementFromJsonArrayString: " + jsonObject != null ? jsonObject.toString() : "null" );
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactDetailMainActivity.getElementFromJsonArrayString: " ,e);
        }
        return result;
    }


    private String getElementFromJsonObjectString(String json, String key){
        Log.d(Constants.TAG, "ContactDetailMainActivity.getElementFromJsonObjectString: " + json + ", key=" + key);
        JSONObject jsonObject = null;
        String result = null;
        try {
            jsonObject = new JSONObject(json);
            result = jsonObject.getString(key);

            Log.d(Constants.TAG, "ContactDetailMainActivity.getElementFromJsonObjectString: " + result);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactDetailMainActivity.getElementFromJsonObjectString: " , e);
        }
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    public void loadContactDetails(Contact contactToShow){
        TextView contactName = (TextView) findViewById(R.id.contact_contact_name);
        contactName.setText(contactToShow.getFirstName() + " " + contactToShow.getLastName());

        TextView company = (TextView) findViewById(R.id.contact_company);
        company.setText(contactToShow.getCompany());

        TextView position = (TextView) findViewById(R.id.contact_position);
        position.setText(contactToShow.getPosition());

        TextView phoneNumber = (TextView) findViewById(R.id.contact_phone_number);
        phoneNumber.setText(getElementFromJsonArrayString(contactToShow.getPhones(), "phone"));

        TextView email = (TextView) findViewById(R.id.contact_email);
        email.setText(getElementFromJsonArrayString(contactToShow.getEmails(), "email"));

        EditText officeLocation = (EditText)findViewById(R.id.contact_office_location);
        officeLocation.setText(contactToShow.getOfficeLocation());

        String dayTimeString =  getElementFromJsonObjectString(contactToShow.getPresence(), "icon"); // "presence":{"icon":"sun","detail":"#LOCAL_TIME#"}
        String getCountryCode = contactToShow.getCountry();

        //EditText department = (EditText)findViewById(R.id.contact_department);

//        EditText additionalInfo = (EditText) findViewById(R.id.contact_additional_info);
//        LinearLayout additionalInfoBox = (LinearLayout) findViewById(R.id.contact_additional_info_box);
//        TextView additionalInfoLabel = (TextView) findViewById(R.id.contact_additional_info_label);
//
//        additionalInfoLabel.setVisibility(View.GONE);
//        additionalInfoBox.setVisibility(View.GONE);
//        additionalInfo.setVisibility(View.GONE);

    }

    private Contact getContact(String contactId){
        RealmContactTransactions realmContactTransactions = new RealmContactTransactions(realm);
        List<Contact> contactList = realmContactTransactions.getFilteredContacts(Constants.CONTACT_ID, contactId);

        Contact contact = contactList.get(0);
        Log.d(Constants.TAG, "ContactDetailMainActivity.getContact: " + printContact(contact));

        loadContactDetails(contact);

        controller.getContactDetail(contactId);
        return contact;
    }

    private String printContact(Contact contact){
            StringBuffer buf = new StringBuffer();
            buf.append("Contact[");
            buf.append(contact.getFirstName());
            buf.append(",");
            buf.append(contact.getLastName());
            buf.append("]");
            buf.append("company:");
            buf.append(contact.getCompany());
            return buf.toString();
    }

    @Override
    public void onContactDetailReceived(Contact contact) {
        Log.d(Constants.TAG, "ContactDetailMainActivity.onContactDetailReceived: " + printContact(contact));
        loadContactDetails(contact);
    }

    @Override
    public void onConnectionNotAvailable() {
        Log.d(Constants.TAG, "ContactDetailMainActivity.onConnectionNotAvailable: ");
    }
}
