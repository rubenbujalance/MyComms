package com.vodafone.mycomms.contacts.detail;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.squareup.okhttp.Response;
import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chatgroup.GroupChatActivity;
import com.vodafone.mycomms.contacts.connection.ContactsController;
import com.vodafone.mycomms.contacts.connection.FavouriteController;
import com.vodafone.mycomms.contacts.connection.RecentContactController;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.main.SplashScreenActivity;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmProfileTransactions;
import com.vodafone.mycomms.util.AvatarSFController;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.OKHttpWrapper;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.util.UncaughtExceptionHandlerController;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import io.realm.Realm;
import model.Contact;
import model.UserProfile;

public class ContactDetailMainActivity extends ToolbarActivity{
    private Contact contact;
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
    private TextView tvOfficeLocation;
    private ImageView ivAvatar;
    private int imageStarOn;
    private int imageStarOff;
    private TextView textAvatar;

    //Buttons
    private ImageView btnChat;
    private ImageView btnEmail;
    private ImageView btnPhone;
    private ImageView btnCalendar;
    private ImageView btFavourite;

    private FavouriteController favouriteController;

    private LinearLayout lay_no_connection;
    private String SF_URL;



    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler
                (
                        new UncaughtExceptionHandlerController(this, SplashScreenActivity.class)
                );

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_contact_detail);
        this.realm = Realm.getDefaultInstance();

        this.SF_URL = null;

        lay_no_connection = (LinearLayout) findViewById(R.id.no_connection_layout);
        if(Utils.isConnected(ContactDetailMainActivity.this))
            lay_no_connection.setVisibility(View.GONE);
        else
            lay_no_connection.setVisibility(View.VISIBLE);

        BusProvider.getInstance().register(this);

        mProfileId = Utils.getProfileId(this);

        mRecentContactController = new RecentContactController(this, mProfileId);
        new ContactsController(mProfileId, ContactDetailMainActivity.this);

        Intent intent = getIntent();
        if(null != intent && intent.hasExtra(Constants.CONTACT_CONTACT_ID))
            contactId = intent.getExtras().getString(Constants.CONTACT_CONTACT_ID);

        if (null != contactId && contactId.equals(mProfileId)) {
            UserProfile profile = getProfile(contactId);
            contact = ContactsController.mapProfileToContact(profile);
        }else
            contact = getContact(contactId);

        //Views
        ivIconStatus = (ImageView)findViewById(R.id.ivStatus);
        tvLocalTime = (TextView)findViewById(R.id.contact_local_time);
        tvCountry = (TextView)findViewById(R.id.contact_country);
        tvLastSeen = (TextView)findViewById(R.id.contact_last_seen);
        tvContactName = (TextView) findViewById(R.id.contact_contact_name);
        tvCompany = (TextView) findViewById(R.id.contact_company);
        tvPosition = (TextView) findViewById(R.id.contact_position);
        tvOfficeLocation = (TextView)findViewById(R.id.contact_office_location);
        ivAvatar = (ImageView) findViewById(R.id.avatar);
        imageStarOn = R.mipmap.icon_favorite_colour;
        imageStarOff = R.mipmap.icon_favorite_grey;
        textAvatar = (TextView)findViewById(R.id.avatarText);

        //Buttons
        btnChat = (ImageView)findViewById(R.id.btn_prof_chat);
        btnEmail = (ImageView)findViewById(R.id.btn_prof_email);
        btnPhone = (ImageView)findViewById(R.id.btn_prof_phone);
        btnCalendar = (ImageView)findViewById(R.id.btn_prof_calendar);
        btFavourite = (ImageView)findViewById(R.id.btFavourite);

        LinearLayout detailsContainer = (LinearLayout) findViewById(R.id.details_container);

        detailsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent in = new Intent(ContactDetailMainActivity.this, ContactDetailsPlusActivity.class);
//                    in.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    String contactDetail[] = loadContactExtra();

                    in.putExtra(Constants.CONTACT_DETAIL_INFO, contactDetail);
                    startActivity(in);
                } catch (Exception e) {
                    Log.e(Constants.TAG, "ContactDetailMainActivity.onClick: ", e);
                }
            }
        });

        btnPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String strPhones = contact.getPhones();

                    if (strPhones != null)
                    {
                        String phone = strPhones;
                        if(!contact.getPlatform().equals(Constants.PLATFORM_LOCAL))
                        {
                            JSONArray jPhones = new JSONArray(strPhones);
                            phone = (String)((JSONObject)jPhones.get(0)).get(Constants
                                    .CONTACT_PHONE);
                        }

                        Utils.launchCall(phone, ContactDetailMainActivity.this);
                        action = Constants.CONTACTS_ACTION_CALL;
                        mRecentContactController.insertRecent(contactId, action);
                    }
                } catch (Exception ex) {
                    Log.e(Constants.TAG, "ContactDetailMainActivity.onClick: ", ex);
                }
            }
        });

        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String strEmails = contact.getEmails();

                    if (strEmails != null)
                    {
                        String email = strEmails;
                        if(!contact.getPlatform().equals(Constants.PLATFORM_LOCAL))
                        {
                            JSONArray jPhones = new JSONArray(strEmails);
                            email = (String) ((JSONObject) jPhones.get(0)).get(Constants.CONTACT_EMAIL);
                        }

                        Utils.launchEmail(email, ContactDetailMainActivity.this);

                        action = Constants.CONTACTS_ACTION_EMAIL;
                        mRecentContactController.insertRecent(contactId, action);
                    }
                } catch (Exception ex) {
                    Log.e(Constants.TAG, "ContactDetailMainActivity.onClick: ", ex);
                }
            }
        });

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String strPhones = contact.getPhones();

                    if (strPhones != null)
                    {

                        if(contact.getPlatform().equals(Constants.PLATFORM_LOCAL)
                                || contact.getPlatform().equals(Constants.PLATFORM_GLOBAL_CONTACTS))
                        {
                            String sms = getPhoneFromJSONForLunchSMS(strPhones);
                            Utils.launchSms(sms, ContactDetailMainActivity.this);
                        }
                        else
                        {
                            Intent in = new Intent(ContactDetailMainActivity.this, GroupChatActivity.class);
                            in.putExtra(Constants.CHAT_FIELD_CONTACT_ID, contactId);
                            in.putExtra(Constants.CHAT_PREVIOUS_VIEW, Constants.CHAT_VIEW_CONTACT_DETAIL);
                            startActivity(in);
                        }

                        action = Constants.CONTACTS_ACTION_SMS;
                        mRecentContactController.insertRecent(contactId, action);
                    }
                } catch (Exception ex) {
                    Log.e(Constants.TAG, "ContactDetailMainActivity.onClick: ", ex);
                }
            }
        });

        btnCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               try
               {
                   Utils.launchCalendar(contact.getFirstName(), ContactDetailMainActivity.this);
               }
               catch (Exception e)
               {
                   Log.e(Constants.TAG, "ContactDetailMainActivity.btnCalendaronClick: ",e);
               }
            }
        });

        //Initialization of favourite icon
        favouriteController = new FavouriteController(ContactDetailMainActivity.this, mProfileId);

        if (favouriteController.contactIsFavourite(contactId)) {
            Drawable imageStar = getResources().getDrawable(imageStarOn);
            btFavourite.setImageDrawable(imageStar);
            btFavourite.setTag("icon_favorite_colour");
        } else {
            Drawable imageStar = getResources().getDrawable(imageStarOff);
            btFavourite.setImageDrawable(imageStar);
            btFavourite.setTag("icon_favorite_grey");
        }

        btFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btFavourite.getTag().equals("icon_favorite_colour")) {
                    Drawable imageStar = getResources().getDrawable(imageStarOff);
                    btFavourite.setImageDrawable(imageStar);
                    btFavourite.setTag("icon_favorite_grey");
                } else if (btFavourite.getTag().equals("icon_favorite_grey")) {
                    Drawable imageStar = getResources().getDrawable(imageStarOn);
                    btFavourite.setImageDrawable(imageStar);
                    btFavourite.setTag("icon_favorite_colour");
                }

                favouriteController.manageFavourite(contactId);
            }
        });

        ImageView ivBtBack = (ImageView)findViewById(R.id.ivBtBack);
        ivBtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        LinearLayout backArea = (LinearLayout)findViewById(R.id.back_area);
        backArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loadContactDetail();
        setButtonsVisibility();
    }

    private String getPhoneFromJSONForLunchSMS(String strPhones)
    {
        try
        {
            JSONObject jsonObject;
            JSONArray jsonArray;
            if(strPhones.startsWith("[") && strPhones.endsWith("]"))
            {
                jsonArray = new JSONArray(strPhones);
                jsonObject = jsonArray.getJSONObject(0);
                return  (String)jsonObject.get(Constants.CONTACT_PHONE);
            }
            else
            {
                jsonObject = new JSONObject(strPhones);
                return  (String)jsonObject.get(Constants.CONTACT_PHONE);
            }
        }

        catch (Exception e)
        {
            Log.e(Constants.TAG, "getPhoneFromJSONForLunchSMS ",e);
            return null;
        }
    }

    private String[] loadContactExtra() {

        String contactDetail[] = new String[10];

        contactDetail[0] = contact.getFirstName();
        contactDetail[1] = contact.getLastName();

        contactDetail[2] = contact.getPhones();
        contactDetail[3] = contact.getEmails();

        contactDetail[4] = contact.getOfficeLocation();
        contactDetail[5] = contact.getPosition();
        contactDetail[6] = contact.getAvatar();
        contactDetail[7] = contact.getPlatform();
        contactDetail[8] = contact.getContactId();
        contactDetail[9] = contact.getStringField1(); //SFAvatar

        return contactDetail;
    }

    private void setButtonsVisibility()
    {
        if(null != contact.getPlatform() && contact.getPlatform().equals(Constants.PLATFORM_LOCAL) )
        {
            TableRow infoRow = (TableRow) findViewById(R.id.contact_info_row);
            infoRow.setVisibility(View.GONE);
        }

        if(null == contact.getPhones() || contact.getPhones().length() <= 0)
        {
            btnPhone.setImageDrawable(getResources().getDrawable(R.drawable.btn_prof_phone_off));
            btnChat.setImageDrawable(getResources().getDrawable(R.drawable.btn_prof_chat_off));
            btnPhone.setOnClickListener(null);
            btnChat.setOnClickListener(null);
        }
        else
        {
            btnPhone.setImageDrawable(getResources().getDrawable(R.drawable.btn_prof_phone));
            btnChat.setImageDrawable(getResources().getDrawable(R.drawable.btn_prof_chat));
        }

        if(null == contact.getEmails() || contact.getEmails().length() <= 0){
            btnEmail.setImageDrawable(getResources().getDrawable(R.drawable.btn_prof_email_off));
            btnEmail.setOnClickListener(null);
        }
        else
            btnEmail.setImageDrawable(getResources().getDrawable(R.drawable.btn_prof_email));

        if(null == contact.getOfficeLocation() || contact.getOfficeLocation().length() <= 0)
        {
            tvOfficeLocation.setVisibility(View.GONE);
        }
        else
            tvOfficeLocation.setVisibility(View.VISIBLE);

        if (contactId.equals(mProfileId)){
            btnChat.setImageDrawable(getResources().getDrawable(R.drawable.btn_prof_chat_off));
            btnChat.setOnClickListener(null);
        }
    }

    private void loadContactStatusInfo()
    {
        try {
            //Icon
            String icon = "";
            if(null != contact.getPresence() &&  contact.getPresence().length() > 0)
            {
                JSONObject jsonObject = new JSONObject(contact.getPresence());
                if(!jsonObject.isNull("icon"))
                {
                    icon = jsonObject.getString("icon");
                }
            }

            if(icon.compareTo("dnd")==0) ivIconStatus.setImageResource(R.mipmap.ico_notdisturb_white);
            else if(icon.compareTo("vacation")==0) ivIconStatus.setImageResource(R.mipmap.ico_vacation_white);
            else if(icon.compareTo("moon")==0) ivIconStatus.setImageResource(R.mipmap.ico_moon_white);
            else if(icon.compareTo("sun")==0) ivIconStatus.setImageResource(R.mipmap.ico_sun_white);
            else ivIconStatus.setVisibility(View.INVISIBLE);

        } catch (Exception ex) {
            Log.e(Constants.TAG, "ContactDetailMainActivity.loadContactStatusInfo: ", ex);
        }
        try {
            //Local time
            String presenceDetail = null;

            try {
                presenceDetail = new JSONObject(contact.getPresence()).getString("detail");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                assert null != presenceDetail;
                if (null != presenceDetail && presenceDetail.equals("#LOCAL_TIME#"))
                {
                    if(null != contact.getTimezone())
                    {
                        TimeZone tz = TimeZone.getTimeZone(contact.getTimezone());
                        Calendar c = Calendar.getInstance(tz);
                        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                        format.setTimeZone(c.getTimeZone());
                        Date parsed = format.parse(c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE));
                        String result = format.format(parsed);
                        tvLocalTime.setText(result);
                    }
                    else
                    {
                        tvLocalTime.setText(" ");
                    }
                }
                else {
                    tvLocalTime.setText(presenceDetail);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

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
                else if(minutes < 1) lastSeenStr = "seconds";

                if(null == lastSeenStr)
                {
                    tvLastSeen.setText("");
                }
                else
                {
                    tvLastSeen.setText(lastSeenStr + " ago");
                }
            }
        } catch (Exception ex) {
            Log.e(Constants.TAG, "ContactDetailMainActivity.loadContactStatusInfo: ", ex);
        }
    }

    private void loadContactAvatar()
    {
        if(null != contact.getPlatform() && Constants.PLATFORM_SALES_FORCE.equals(contact.getPlatform()))
        {
            AvatarSFController avatarSFController = new AvatarSFController
                    (
                            ContactDetailMainActivity.this, contact.getContactId(), mProfileId
                    );
            avatarSFController.getSFAvatar(contact.getAvatar());
        }

        String test1 = contact.getFirstName();
        String test2 = contact.getLastName();
        ImageView test3 = ivAvatar;
        TextView test4 = textAvatar;
        String test5 = contact.getAvatar();
        String test6 = SF_URL;

        Utils.loadContactAvatarDetail
                (
                        contact.getFirstName()
                        , contact.getLastName()
                        , this.ivAvatar
                        , this.textAvatar
                        , Utils.getAvatarURL
                                (
                                        contact.getPlatform()
                                        , SF_URL
                                        , contact.getAvatar()
                                )
                );
    }

    private void loadContactDetail()
    {
        setSF_URL();
        loadContactStatusInfo();
        loadContactInfo();
        loadContactAvatar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != realm)
            this.realm.close();
    }

    public void loadContactInfo()
    {
        if(null != contact.getFirstName())
            tvContactName.setText(contact.getFirstName());
        else
            tvContactName.setText("");
        if(null != contact.getLastName())
            tvContactName.setText(tvContactName.getText() + " " + contact.getLastName());

        if(null != contact.getCompany())
            tvCompany.setText(contact.getCompany());
        else
            tvCompany.setText("");

        if(null != contact.getPosition())
            tvPosition.setText(contact.getPosition());
        else
            tvPosition.setText("");

        if(null != contact.getOfficeLocation())
            tvOfficeLocation.setText(contact.getOfficeLocation());
        else
            tvOfficeLocation.setText("");
    }

    private Contact getContact(String contactId){
        List<Contact> contactList = RealmContactTransactions.getFilteredContacts(Constants
                .CONTACT_CONTACT_ID, contactId, realm);

        Contact contact = contactList.get(0);
        Log.d(Constants.TAG, "ContactDetailMainActivity.getContact: " + printContact(contact));

        if(null != contact.getPlatform() && !contact.getPlatform().equals("local"))
            getContactFromServerById(contactId);

        return contact;
    }

    private UserProfile getProfile(String profileId){
        RealmProfileTransactions realmProfileTransactions = new RealmProfileTransactions();

        return realmProfileTransactions.getUserProfile(profileId,null);
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

//    @Override
//    public void onContactDetailReceived(Contact contact) {
//        Log.d(Constants.TAG, "ContactDetailMainActivity.onContactDetailReceived: " + printContact(contact));
//        this.contact = contact;
//        loadContactDetail();
//        setButtonsVisibility();
//    }

//    @Override
//    public void onConnectionNotAvailable() {
//        Log.d(Constants.TAG, "ContactDetailMainActivity.onConnectionNotAvailable: ");
//    }

    @Subscribe
    public void onConnectivityChanged(ConnectivityChanged event)
    {
        Log.e(Constants.TAG, "ContactDetailMainActivity.onConnectivityChanged: "
                + event.getConnectivityStatus().toString());
        if(event.getConnectivityStatus()!= ConnectivityStatus.MOBILE_CONNECTED &&
                event.getConnectivityStatus()!=ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET)
            lay_no_connection.setVisibility(View.VISIBLE);
        else
            lay_no_connection.setVisibility(View.GONE);
    }

    private void setSF_URL()
    {
        if(null!= this.contact.getStringField1())
            this.SF_URL = this.contact.getStringField1();
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


    public void getContactFromServerById(String contactId)
    {
        Log.i(Constants.TAG, "ContactDetailMainActivity.getContactFromServerById: " + contactId);
        String URL = "/api/me/contact/?id="+contactId;
        try
        {
            OKHttpWrapper.get(URL, ContactDetailMainActivity.this, new OKHttpWrapper.HttpCallback()
            {
                @Override
                public void onFailure(Response response, IOException e) {
                    Log.i(Constants.TAG, "getContactFromServerById.onFailure:");
                }

                @Override
                public void onSuccess(Response response)
                {
                    try
                    {
                        String result = response.body().string();
                        Log.i(Constants.TAG, "ContactDetailMainActivity.onSuccess: Body content is ->" + result);
                        JSONObject jsonResponse;

                        try {

                            RealmContactTransactions realmContactTransactions = new RealmContactTransactions(mProfileId);
                            jsonResponse = new JSONObject(result);
                            String data = jsonResponse.getString(Constants.CONTACT_DATA);
                            jsonResponse = new JSONObject(data.substring(1, data.length()-1 )); //Removing squared bracelets.
                            contact = ContactsController.mapContact(jsonResponse, mProfileId);
                            realmContactTransactions.updateContact(contact, null);

                            ContactDetailMainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadContactDetail();
                                    setButtonsVisibility();
                                }
                            });
                        } catch (Exception e){
                            Log.e(Constants.TAG, "ContactsController.onConnectionComplete: " + e.toString());
                        }

                    } catch (Exception e) {
                        Log.e(Constants.TAG, "getContactFromServerById.onSuccess: ", e);
                    }
                }
            });
        } catch (Exception e){
            Log.e(Constants.TAG, "ContactDetailMainActivity.getContactFromServerById: ", e);
        }
    }
}