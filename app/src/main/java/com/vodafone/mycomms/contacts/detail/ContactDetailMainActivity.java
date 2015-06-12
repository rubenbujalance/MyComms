package com.vodafone.mycomms.contacts.detail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chat.ChatMainActivity;
import com.vodafone.mycomms.contacts.connection.FavouriteController;
import com.vodafone.mycomms.contacts.connection.IContactDetailConnectionCallback;
import com.vodafone.mycomms.contacts.connection.RecentContactController;
import com.vodafone.mycomms.custom.CircleImageView;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import io.realm.Realm;
import model.Contact;

public class ContactDetailMainActivity extends ToolbarActivity implements IContactDetailConnectionCallback {
    private Realm realm;
    private Contact contact;
    private ContactDetailController controller;
    private String contactId;
    private String action;
    private String mProfileId;
    private RecentContactController mRecentContactController;

    //Views
    private ImageView ivIconStatus;
    private TextView tvLocalTime;
    private TextView tvCountry;
    private TextView tvLastSeen;
    private TextView tvContactName;
    private TextView tvCompany;
    private TextView tvPosition;
    private TextView tvPhoneNumber;
    private TextView tvEmail;
    private TextView tvOfficeLocation;
    private CircleImageView ivAvatar;
    private int imageStarOn;
    private int imageStarOff;
    private TextView textAvatar;

    //Buttons
    private ImageView btSms;
    private ImageView btEmail;
    private ImageView btChat;
    private ImageView btCall;
    private ImageView btEmailBar;
    private ImageView btChatBar;
    private ImageView btCallBar;
    private ImageView btFavourite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.contact_detail);

        SharedPreferences sp = getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        mProfileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");

        realm = Realm.getInstance(this);
        mRecentContactController = new RecentContactController(this,realm,mProfileId);

        Intent intent = getIntent();
        contactId = intent.getExtras().getString(Constants.CONTACT_CONTACT_ID);
        controller = new ContactDetailController(this, realm, mProfileId);
        controller.setConnectionCallback(this);
        contact = getContact(contactId);

        //Views
        ivIconStatus = (ImageView)findViewById(R.id.ivStatus);
        tvLocalTime = (TextView)findViewById(R.id.contact_local_time);
        tvCountry = (TextView)findViewById(R.id.contact_country);
        tvLastSeen = (TextView)findViewById(R.id.contact_last_seen);
        tvContactName = (TextView) findViewById(R.id.contact_contact_name);
        tvCompany = (TextView) findViewById(R.id.contact_company);
        tvPosition = (TextView) findViewById(R.id.contact_position);
        tvPhoneNumber = (TextView) findViewById(R.id.contact_phone_number);
        tvEmail = (TextView) findViewById(R.id.contact_email);
        tvOfficeLocation = (TextView)findViewById(R.id.contact_office_location);
        ivAvatar = (CircleImageView)findViewById(R.id.avatar);
        imageStarOn = R.drawable.abc_btn_rating_star_on_mtrl_alpha;
        imageStarOff = R.drawable.abc_btn_rating_star_off_mtrl_alpha;
        textAvatar = (TextView)findViewById(R.id.avatarText);

        //Buttons
        btSms = (ImageView)findViewById(R.id.bt_sms);
        btEmail = (ImageView)findViewById(R.id.bt_email);
        btChat = (ImageView)findViewById(R.id.bt_chat);
        btCall = (ImageView)findViewById(R.id.bt_call);
        btChatBar = (ImageView)findViewById(R.id.btchat);
        btEmailBar = (ImageView)findViewById(R.id.btemail);
        btCallBar = (ImageView)findViewById(R.id.btcall);
        btFavourite = (ImageView)findViewById(R.id.btFavourite);

        btCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String strPhones = contact.getPhones();

                    if (strPhones != null) {
                        JSONArray jPhones = new JSONArray(strPhones);
                        String phone = (String)((JSONObject)jPhones.get(0)).get(Constants.CONTACT_PHONE);

                        Utils.launchCall(phone, ContactDetailMainActivity.this);
                        action = Constants.CONTACTS_ACTION_CALL;
                        mRecentContactController.insertRecent(contactId, action);
                    }
                } catch (Exception ex) {
                    Log.e(Constants.TAG, "ContactDetailMainActivity.onClick: ", ex);
                    return;
                }
            }
        });

        btCallBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String strPhones = contact.getPhones();

                    if (strPhones != null) {
                        JSONArray jPhones = new JSONArray(strPhones);
                        String phone = (String)((JSONObject)jPhones.get(0)).get(Constants.CONTACT_PHONE);

                        Utils.launchCall(phone, ContactDetailMainActivity.this);

                        action = Constants.CONTACTS_ACTION_CALL;
                        mRecentContactController.insertRecent(contactId, action);
                    }
                } catch (Exception ex) {
                    Log.e(Constants.TAG, "ContactDetailMainActivity.onClick: ", ex);
                    return;
                }
            }
        });

        btEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String strEmails = contact.getEmails();

                    if (strEmails != null) {
                        JSONArray jPhones = new JSONArray(strEmails);
                        String email = (String) ((JSONObject) jPhones.get(0)).get(Constants.CONTACT_EMAIL);

                        Utils.launchEmail(email, ContactDetailMainActivity.this);

                        action = Constants.CONTACTS_ACTION_EMAIL;
                        mRecentContactController.insertRecent(contactId, action);
                    }
                } catch (Exception ex) {
                    Log.e(Constants.TAG, "ContactDetailMainActivity.onClick: ", ex);
                    return;
                }
            }
        });

        btEmailBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String strPhones = contact.getEmails();

                    if (strPhones != null) {
                        JSONArray jPhones = new JSONArray(strPhones);
                        String email = (String) ((JSONObject) jPhones.get(0)).get(Constants.CONTACT_EMAIL);

                        Utils.launchEmail(email, ContactDetailMainActivity.this);

                        action = Constants.CONTACTS_ACTION_EMAIL;
                        mRecentContactController.insertRecent(contactId, action);
                    }
                } catch (Exception ex) {
                    Log.e(Constants.TAG, "ContactDetailMainActivity.onClick: ", ex);
                    return;
                }
            }
        });

        btSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String strPhones = contact.getPhones();

                    if (strPhones != null) {
                        JSONArray jPhones = new JSONArray(strPhones);
                        String phone = (String)((JSONObject) jPhones.get(0)).get(Constants.CONTACT_PHONE);

                        Utils.launchSms(phone, ContactDetailMainActivity.this);

                        action = Constants.CONTACTS_ACTION_SMS;
                        mRecentContactController.insertRecent(contactId, action);
                    }
                } catch (Exception ex) {
                    Log.e(Constants.TAG, "ContactDetailMainActivity.onClick: ", ex);
                    return;
                }
            }
        });

        btChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(ContactDetailMainActivity.this, ChatMainActivity.class);
                in.putExtra(Constants.CHAT_FIELD_CONTACT_ID, contactId);
                in.putExtra(Constants.CHAT_PREVIOUS_VIEW, Constants.CHAT_VIEW_CONTACT_DETAIL);
                startActivity(in);
            }
        });

        btChatBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(ContactDetailMainActivity.this, ChatMainActivity.class);
                in.putExtra(Constants.CHAT_FIELD_CONTACT_ID, contactId);
                in.putExtra(Constants.CHAT_PREVIOUS_VIEW, Constants.CHAT_VIEW_CONTACT_DETAIL);
                startActivity(in);
            }
        });

        btFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FavouriteController favouriteController = new FavouriteController(ContactDetailMainActivity.this, realm, mProfileId);
                favouriteController.manageFavourite(contactId);

                if (btFavourite.getTag() == "abc_btn_rating_star_off_mtrl_alpha.png") {
                    Drawable imageStar = getResources().getDrawable(imageStarOn);
                    btFavourite.setImageDrawable(imageStar);
                    btFavourite.setTag("abc_btn_rating_star_on_mtrl_alpha.png");
                } else if (btFavourite.getTag() == "abc_btn_rating_star_on_mtrl_alpha.png") {
                    Drawable imageStar = getResources().getDrawable(imageStarOff);
                    btFavourite.setImageDrawable(imageStar);
                    btFavourite.setTag("abc_btn_rating_star_off_mtrl_alpha.png");
                }

                Log.e(Constants.TAG, "TAG: " + btFavourite.getTag());

            }
        });

        ImageView ivBtBack = (ImageView)findViewById(R.id.ivBtBack);
        ivBtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loadContactDetail();
    }

    private void loadContactStatusInfo()
    {
        Calendar currentCal = Calendar.getInstance();

        try {
            //Icon
            String icon = new JSONObject(contact.getPresence()).getString("icon");

            if(icon.compareTo("dnd")==0) ivIconStatus.setImageResource(R.mipmap.ico_notdisturb_white);
            else if(icon.compareTo("vacation")==0) ivIconStatus.setImageResource(R.mipmap.ico_vacation_white);
            else if(icon.compareTo("moon")==0) ivIconStatus.setImageResource(R.mipmap.ico_moon_white);
            else ivIconStatus.setImageResource(R.mipmap.ico_sun_white);
        } catch (Exception ex) {
            Log.e(Constants.TAG, "ContactDetailMainActivity.loadContactStatusInfo: ", ex);
        }
        try {
            //Local time
            TimeZone tz = TimeZone.getTimeZone(contact.getTimezone());

            SimpleDateFormat sourceFormat = new SimpleDateFormat("HH:mm");
            sourceFormat.setTimeZone(currentCal.getTimeZone());
            Date parsed = sourceFormat.parse(currentCal.get(Calendar.HOUR_OF_DAY)+":"+currentCal.get(Calendar.MINUTE));

            SimpleDateFormat destFormat = new SimpleDateFormat("HH:mm");
            destFormat.setTimeZone(tz);

            String result = destFormat.format(parsed);

            tvLocalTime.setText(result);

        } catch (Exception ex) {
            Log.e(Constants.TAG, "ContactDetailMainActivity.loadContactStatusInfo: ", ex);
        }
        try {
            //Country
            if(contact.getCountry() != null && contact.getCountry().length()>0)
                tvCountry.setText(Utils.getCountry(contact.getCountry(), this).get("name"));
        } catch (Exception ex) {
            Log.e(Constants.TAG, "ContactDetailMainActivity.loadContactStatusInfo: ", ex);
        }
        try {
            //Last seen
            if(contact.getLastSeen() != 0)
            {
                String lastSeenStr = null;
                long currentTime = System.currentTimeMillis();
                long lastSeen = contact.getLastSeen();

                long timeDifference = currentTime - lastSeen;
                long minutes = timeDifference / 60000;
                long hours = minutes / 60;
                long days = hours / 24;

                if(days >= 1) lastSeenStr = days + "d";
                else if(hours >=1) lastSeenStr = hours + "h";
                else if(minutes >=1) lastSeenStr = minutes + "m";
                else if(minutes < 1) lastSeenStr = "less then a minute";

                if(null == lastSeenStr)
                {
                    tvLastSeen.setText("");
                }
                else
                {
                    tvLastSeen.setText("Seen " + lastSeenStr + " ago");
                }


            }
        } catch (Exception ex) {
            Log.e(Constants.TAG, "ContactDetailMainActivity.loadContactStatusInfo: ", ex);
        }
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

    private void loadContactAvatar()
    {
        File avatarFile = new File(getFilesDir(), Constants.CONTACT_AVATAR_DIR + "avatar_"+contact.getContactId()+".jpg");

        if (contact.getAvatar()!=null &&
                contact.getAvatar().length()>0 &&
                contact.getAvatar().compareTo("")!=0 &&
                avatarFile.exists()) {

            textAvatar.setText(null);

            Picasso.with(this)
                    .load(avatarFile)
                    .into(ivAvatar);

        } else{
            String initials = contact.getFirstName().substring(0,1) +
                    contact.getLastName().substring(0,1);

            ivAvatar.setImageResource(R.color.grey_middle);
            textAvatar.setText(initials);
        }

    }

    private void loadContactDetail()
    {
        loadContactStatusInfo();
        loadContactInfo();
        loadContactAvatar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm!=null)
            realm.close();
    }

    public void loadContactInfo(){
        tvContactName.setText(contact.getFirstName() + " " + contact.getLastName());
        tvCompany.setText(contact.getCompany());
        tvPosition.setText(contact.getPosition());
        tvPhoneNumber.setText(getElementFromJsonArrayString(contact.getPhones(), Constants.CONTACT_PHONE));
        tvEmail.setText(getElementFromJsonArrayString(contact.getEmails(), Constants.CONTACT_EMAIL));
        tvOfficeLocation.setText(contact.getOfficeLocation());
    }

    private Contact getContact(String contactId){
        RealmContactTransactions realmContactTransactions = new RealmContactTransactions(realm, mProfileId);
        List<Contact> contactList = realmContactTransactions.getFilteredContacts(Constants.CONTACT_CONTACT_ID, contactId);

        Contact contact = contactList.get(0);
        Log.d(Constants.TAG, "ContactDetailMainActivity.getContact: " + printContact(contact));

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
        this.contact = contact;
        loadContactDetail();
    }

    @Override
    public void onConnectionNotAvailable() {
        Log.d(Constants.TAG, "ContactDetailMainActivity.onConnectionNotAvailable: ");
    }
}
