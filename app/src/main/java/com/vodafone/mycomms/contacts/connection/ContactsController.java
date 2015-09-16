package com.vodafone.mycomms.contacts.connection;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ContactListReceivedEvent;
import com.vodafone.mycomms.events.SetContactListAdapterEvent;
import com.vodafone.mycomms.realm.RealmAvatarTransactions;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmProfileTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.OKHttpWrapper;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import io.realm.Realm;
import model.Contact;
import model.FavouriteContact;
import model.RecentContact;
import model.UserProfile;

public class ContactsController{

    private static RealmContactTransactions realmContactTransactions;
    private String mProfileId;
    private Context mContext;
    private int offsetPaging = 0;



    private RealmProfileTransactions mRealmProfileTransactions;


    public ContactsController(String profileId, Context mContext) {
        this.mProfileId = profileId;
        this.mContext = mContext;
        realmContactTransactions = new RealmContactTransactions(mProfileId);
        mRealmProfileTransactions = new RealmProfileTransactions();
    }

    public ArrayList<Contact> insertContactListInRealm(JSONObject jsonObject)
    {
        ArrayList<Contact> realmContactList = new ArrayList<>();
        Realm realm = Realm.getDefaultInstance();
        try {
            Log.i(Constants.TAG, "ContactsController.insertContactListInRealm: ");
            JSONArray jsonArray = jsonObject.getJSONArray(Constants.CONTACT_DATA);
            Contact contact;

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                contact = mapContact(jsonObject, mProfileId);
                realmContactList.add(contact);
            }
            RealmContactTransactions.insertContactList(realmContactList, null, mProfileId);

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);

                if(null != jsonObject.getString(Constants.CONTACT_ID)){
                    contact = RealmContactTransactions.getContactById(jsonObject.getString
                            (Constants.CONTACT_ID), realm);
                    if (null != contact){
                        String SF_URL = RealmContactTransactions.getContactById(jsonObject
                                .getString(Constants.CONTACT_ID), realm)
                                .getStringField1();
                        if(null != SF_URL)
                            realmContactTransactions.updateSFAvatar(contact, SF_URL, null);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "ContactsController.insertContactListInRealm: " + e.toString());
            realmContactList = null;
        }
        finally {
            if(null != realm)
                realm.close();
        }

        return realmContactList;
    }

    public void insertFavouriteContactInRealm(JSONObject json){
        JSONArray jsonArray;
        Contact contact;
        ArrayList<FavouriteContact> contactList = new ArrayList<>();
        Realm realm = Realm.getDefaultInstance();
        try {
            Log.i(Constants.TAG, "ContactsController.insertFavouriteContactInRealm: jsonResponse: " + json.toString());
            jsonArray = json.getJSONArray(Constants.CONTACT_FAVOURITES);
            for (int i = 0; i < jsonArray.length(); i++) {
                contact = RealmContactTransactions.getContactById(jsonArray.getString(i), realm);
                if (contact != null) {
                    contactList.add(mapContactToFavourite(contact));
                }
            }
            if (contactList.size()!=0) {
                realmContactTransactions.deleteAllFavouriteContacts(null);
                realmContactTransactions.insertFavouriteContactList(contactList, null);
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "ContactsController.insertFavouriteContactInRealm : ",e);
        }
        finally
        {
            if(null != realm)
                realm.close();
        }
    }

    public void insertRecentContactInRealm(JSONObject json)
    {
        JSONArray jsonArray;
        Contact contact;
        ArrayList<RecentContact> contactList = new ArrayList<>();

        Realm realm = Realm.getDefaultInstance();

        try {
            jsonArray = json.getJSONArray(Constants.CONTACT_RECENTS);
            for (int i = 0; i < jsonArray.length(); i++) {
                contact = RealmContactTransactions.getContactById(
                        jsonArray.getJSONObject(i).getString(Constants.CONTACT_ID), realm);
                if (contact != null) {
                    contactList.add(mapContactToRecent(contact, jsonArray.getJSONObject(i)));
                }
                else{
                    if (jsonArray.getJSONObject(i).getString(Constants.CONTACT_ID).startsWith("mg_")) {
                        Contact groupContact = new Contact("");
                        String groupChatId = jsonArray.getJSONObject(i).getString(Constants.CONTACT_ID);
                        groupContact.setId(groupChatId);
                        groupContact.setContactId(groupChatId);
                        groupContact.setProfileId(mProfileId);
                        contactList.add(mapContactToRecent(groupContact, jsonArray.getJSONObject(i)));
                    }
                }
            }
            if (contactList.size()!=0) {
                realmContactTransactions.insertRecentContactList(contactList, null);
            }
        } catch (JSONException e) {
            Log.e(Constants.TAG, "ContactsController.insertRecentContactInRealm : ",e);
        }finally {
            realm.close();
        }
    }

    public void insertRecentGroupChatIntoRealm(Contact contact, JSONObject jsonObject)
    {
        try
        {
            Log.i(Constants.TAG, "ContactsController.insertRecentGroupChatIntoRealm: "
                    + contact.getContactId());
            ArrayList<RecentContact> contactList = new ArrayList<>();
            contactList.add(mapContactToRecent(contact, jsonObject));
            realmContactTransactions.insertRecentContactList(contactList, null);
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "ContactsController.insertRecentGroupChatIntoRealm : ",e);
        }
    }

    public static Contact mapContact(JSONObject jsonObject, String profileId){
        Contact contact = new Contact();
        try {
            contact.setProfileId(profileId);
            if (!jsonObject.isNull(Constants.CONTACT_ID)){
                contact.setContactId(jsonObject.getString(Constants.CONTACT_ID));
                contact.setId(profileId + "_" + jsonObject.getString(Constants.CONTACT_ID));
            }
            if (!jsonObject.isNull(Constants.CONTACT_PLATFORM)) {
                contact.setPlatform(jsonObject.getString(Constants.CONTACT_PLATFORM));
                contact.setLongField1(Utils.setPlatformOrder(jsonObject.getString(Constants.CONTACT_PLATFORM)));
            }
            if (!jsonObject.isNull(Constants.CONTACT_FNAME))
                contact.setFirstName(jsonObject.getString(Constants.CONTACT_FNAME));
            if (!jsonObject.isNull(Constants.CONTACT_LNAME))
                contact.setLastName(jsonObject.getString(Constants.CONTACT_LNAME));
            if (!jsonObject.isNull(Constants.CONTACT_AVATAR))
                contact.setAvatar(jsonObject.getString(Constants.CONTACT_AVATAR));
            if (!jsonObject.isNull(Constants.CONTACT_POSITION))
                contact.setPosition(jsonObject.getString(Constants.CONTACT_POSITION));
            if (!jsonObject.isNull(Constants.CONTACT_COMPANY))
                contact.setCompany(jsonObject.getString(Constants.CONTACT_COMPANY));
            if (!jsonObject.isNull(Constants.CONTACT_TIMEZONE))
                contact.setTimezone(jsonObject.getString(Constants.CONTACT_TIMEZONE));
            if (!jsonObject.isNull(Constants.CONTACT_LASTSEEN))
                contact.setLastSeen(jsonObject.getLong(Constants.CONTACT_LASTSEEN));
            if (!jsonObject.isNull(Constants.CONTACT_OFFICE_LOC))
                contact.setOfficeLocation(jsonObject.getString(Constants.CONTACT_OFFICE_LOC));
            if (!jsonObject.isNull(Constants.CONTACT_PHONES))
                contact.setPhones(jsonObject.getJSONArray(Constants.CONTACT_PHONES).toString());
            if (!jsonObject.isNull(Constants.CONTACT_EMAILS))
                contact.setEmails(jsonObject.getJSONArray(Constants.CONTACT_EMAILS).toString());
            if (!jsonObject.isNull(Constants.CONTACT_AVAILABILITY))
                contact.setAvailability(jsonObject.getString(Constants.CONTACT_AVAILABILITY));
            if (!jsonObject.isNull(Constants.CONTACT_PRESENCE))
                contact.setPresence(jsonObject.getString(Constants.CONTACT_PRESENCE));
            if (!jsonObject.isNull(Constants.CONTACT_COUNTRY))
                contact.setCountry(jsonObject.getString(Constants.CONTACT_COUNTRY));

            //Search Helper
            String searchHelper = "";
            if(contact.getFirstName()!=null && contact.getFirstName().length()>0)
                searchHelper  += Utils.normalizeStringNFD(contact.getFirstName()) + " ";
            if(contact.getLastName()!=null && contact.getLastName().length()>0)
                searchHelper  += Utils.normalizeStringNFD(contact.getLastName()) + " ";
            if(contact.getCompany()!=null && contact.getCompany().length()>0)
                searchHelper  += Utils.normalizeStringNFD(contact.getCompany()) + " ";
            if(contact.getEmails()!=null && contact.getEmails().length()>0)
                searchHelper  += Utils.normalizeStringNFD(contact.getEmails()) + " ";

            searchHelper = searchHelper.trim();
            contact.setSearchHelper(searchHelper);

            //Sort Helper
            String sortHelper = "";
            if(contact.getPlatform()!=null && contact.getPlatform().length()>0)
                sortHelper  += contact.getLongField1() + " ";
            if(contact.getFirstName()!=null && contact.getFirstName().length()>0)
                sortHelper  += Utils.normalizeStringNFD(contact.getFirstName()) + " ";
            if(contact.getLastName()!=null && contact.getLastName().length()>0)
                sortHelper  += Utils.normalizeStringNFD(contact.getLastName()) + " ";
            if(contact.getCompany()!=null && contact.getCompany().length()>0)
                sortHelper  += Utils.normalizeStringNFD(contact.getCompany()) + " ";

            sortHelper = sortHelper.trim();
            contact.setSortHelper(sortHelper);
        }
        catch (Exception e){
            Log.e(Constants.TAG, "ContactDBController.mapContact: ",e);
        }
        return contact;
    }

    public FavouriteContact mapContactToFavourite(Contact contact){
        FavouriteContact favouriteContact = new FavouriteContact();
        favouriteContact.setId(contact.getId());
        favouriteContact.setProfileId(contact.getProfileId());
        favouriteContact.setContactId(contact.getContactId());
        favouriteContact.setPlatform(contact.getPlatform());
        favouriteContact.setFirstName(contact.getFirstName());
        favouriteContact.setLastName(contact.getLastName());
        favouriteContact.setAvatar(contact.getAvatar());
        favouriteContact.setCompany(contact.getCompany());
        favouriteContact.setPosition(contact.getPosition());
        favouriteContact.setLastSeen(contact.getLastSeen());
        favouriteContact.setOfficeLocation(contact.getOfficeLocation());
        favouriteContact.setPhones(contact.getPhones());
        favouriteContact.setOfficeLocation(contact.getOfficeLocation());
        favouriteContact.setEmails(contact.getEmails());
        favouriteContact.setAvailability(contact.getAvailability());
        favouriteContact.setPresence(contact.getPresence());
        favouriteContact.setCountry(contact.getCountry());
        favouriteContact.setTimezone(contact.getTimezone());

        return favouriteContact;
    }

    public RecentContact mapContactToRecent(Contact contact, JSONObject jsonObject){
        RecentContact recentContact = new RecentContact();
        recentContact.setId(contact.getId());
        recentContact.setProfileId(contact.getProfileId());
        recentContact.setContactId(contact.getContactId());
        recentContact.setPlatform(contact.getPlatform());
        recentContact.setFirstName(contact.getFirstName());
        recentContact.setLastName(contact.getLastName());
        recentContact.setAvatar(contact.getAvatar());
        recentContact.setCompany(contact.getCompany());
        recentContact.setPosition(contact.getPosition());
        recentContact.setLastSeen(contact.getLastSeen());
        recentContact.setOfficeLocation(contact.getOfficeLocation());
        recentContact.setPhones(contact.getPhones());
        recentContact.setOfficeLocation(contact.getOfficeLocation());
        recentContact.setEmails(contact.getEmails());
        recentContact.setAvailability(contact.getAvailability());
        recentContact.setPresence(contact.getPresence());
        recentContact.setCountry(contact.getCountry());
        try {
            recentContact.setUniqueId(contact.getContactId() + "_" + mProfileId +  jsonObject.getString(Constants.CONTACT_RECENTS_ACTION));
            recentContact.setAction(jsonObject.getString(Constants.CONTACT_RECENTS_ACTION));
            recentContact.setTimestamp(jsonObject.getLong(Constants.CONTACT_RECENTS_ACTION_TIME));
        } catch (JSONException e){
            Log.e(Constants.TAG, "ContactsController.mapContactToRecent: " ,e);
        }
        return recentContact;
    }

    public static Contact mapProfileToContact(UserProfile profile){
        Contact contact = new Contact();
        contact.setId(profile.getId());
        contact.setProfileId(profile.getId());
        contact.setContactId(profile.getId());
        contact.setPlatform(profile.getPlatform());
        contact.setFirstName(profile.getFirstName());
        contact.setLastName(profile.getLastName());
        contact.setAvatar(profile.getAvatar());
        contact.setCompany(profile.getCompany());
        contact.setPosition(profile.getPosition());
        contact.setLastSeen(profile.getLastSeen());
        contact.setPhones(profile.getPhones());
        contact.setOfficeLocation(profile.getOfficeLocation());
        contact.setEmails(profile.getEmails());
        contact.setAvailability(profile.getAvailability());
        contact.setPresence(profile.getPresence());
        contact.setCountry(profile.getCountry());

        return contact;
    }

    /**
     * Try to send invitation to the contact for join MyComms App
     * @author str_oan
     * @param contact (Contact) -> contact which will be invited
     * @return (boolean) -> true if contact has successfully received the invitation, false otherwise
     */
    public boolean isContactCanBeInvited(Contact contact, String userProfileEmails)
    {
        String[] domains = new String[]
                {
                        "vodafone.com",
                        "vodafone.com.au",
                        "ono.es",
                        "quickcomm.com",
                        "bluefishplc.com",
                        "vodacom.co.za",
                        "jjuan.net",
                        "my-comms.com",
                        "igzinc.com",
                        "intelygenz.com",
                        "comediadesign.com"
                };

        String emails = contact.getEmails();
        String platform = contact.getPlatform();
        if(null != emails && emails.length() > 0 && null != platform && platform.length() > 0)
        {
            for (String str : domains)
            {
                if(platform.equals("mc"))
                    return false;
                if(null != userProfileEmails && emails.contains(userProfileEmails))
                    return false;
                if(emails.contains(str))
                    return true;
            }
        }
        return false;
    }

    public String getUserProfileEmails()
    {
        Realm realm = Realm.getDefaultInstance();
        try
        {
            UserProfile userProfile = mRealmProfileTransactions.getUserProfile(mProfileId, realm);
            String userProfileEmails = userProfile.getEmails();
            userProfileEmails = Utils.getElementFromJsonArrayString(userProfileEmails, Constants.CONTACT_EMAIL);
            return userProfileEmails;
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "ContactsController.getUserProfileEmails: ", e);
            return null;
        }
        finally {
            if (realm != null)
                realm.close();
        }
    }

    /**
     * Creates alert dialog where you should confirm if invitation shall be sent
     * @author str_oan
     * @param contact (Contact) -> contact which will be invited
     */
    public void createInviteAlertWithEvents(Contact contact)
    {
        try
        {
            String emails = contact.getEmails();
            String firstName = contact.getFirstName();
            View view = Utils.getCustomAlertTitleView(mContext, R.layout.layout_invite_contact);
            TextView textView = (TextView) view.findViewById(R.id.tv_invite_title);

            if(null != emails && emails.length() > 0 && null != firstName)
            {
                String email = Utils.getElementFromJsonArrayString(emails, "email");
                if(null == email||email.length()<=0)
                    email = Utils.getElementFromJsonObjectString(emails, "email");
                final String finalEmail = email;

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                String title;
                if(null!=finalEmail && finalEmail.length()>0)
                {
                    title =
                            mContext.getResources().getString(R.string.invite_contact_confirmation_1)
                                    + " " + firstName
                                    + " " + mContext.getResources().getString(R.string.invite_contact_confirmation_2)
                                    + " " + email + "?";
                    textView.setText(title);
                    builder.setCustomTitle(view);

                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    builder.setPositiveButton(R.string.invite, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            sendInvitation(finalEmail);
                            dialog.dismiss();
                        }
                    });

                    builder.create();
                    builder.show();
                }
                else
                {
                    title = mContext.getResources().getString(R.string.invite_to_mycomms_error_wrong_email);
                    textView.setText(title);
                    builder.setCustomTitle(view);

                    builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dialog.dismiss();
                        }
                    });

                    builder.create();
                    builder.show();
                }
            }
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "ContactsController.createInviteAlertWithEvents: ", e);
            Crashlytics.logException(e);
        }

    }

    /**
     * Sends invitation for join MyComms App via OKHTTP wrapper
     * @author str_oan
     * @param email (String) -> email address where invitation will be sent
     */
    public void sendInvitation(final String email)
    {
        HashMap<String, String> body = new HashMap<>();
        body.put("email", email);
        final JSONObject json = new JSONObject(body);
        final Handler mHandler = new Handler(Looper.getMainLooper());
        try
        {
            OKHttpWrapper.post(Constants.CONTACT_SEND_INVITATION, mContext, new OKHttpWrapper.HttpCallback()
            {
                @Override
                public void onFailure(Response response, IOException e)
                {
                    Log.e(Constants.TAG, "sendInvitation.onFailure: ", e);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showSuccessOrErrorResponse(false);
                        }
                    });
                }
                @Override
                public void onSuccess(Response response)
                {
                    Log.i(Constants.TAG, "sendInvitation.onSuccess: invitation sent to "+email);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showSuccessOrErrorResponse(true);
                        }
                    });
                }
            }, json);
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "sendInvitation: ", e);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    showSuccessOrErrorResponse(false);
                }
            });
        }
    }

    /**
     * Shows confirmation dialog which indicates if invitation has been correctly sent
     * @author str_oan
     * @param isCorrectlyDelivered (boolean) -> if is true then shows affirmative message, otherwise
     *                             will show error message
     */
    public void showSuccessOrErrorResponse(boolean isCorrectlyDelivered)
    {
        View view = Utils.getCustomAlertTitleView(mContext, R.layout.layout_invite_contact);
        TextView textView = (TextView) view.findViewById(R.id.tv_invite_title);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        if(isCorrectlyDelivered)
        {
            textView.setText(mContext.getResources().getString(R.string.invitation_correctly_sent));
            builder.setCustomTitle(view);
        }
        else
        {
            textView.setText(mContext.getResources().getString(R.string.invitation_badly_sent));
            builder.setCustomTitle(view);
            builder.setIcon(mContext.getResources().getDrawable(R.drawable.ic_no_results));
        }

        builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        builder.create();
        builder.show();
    }

    public void setRealmProfileTransactions(RealmProfileTransactions realmProfileTransactions) {
        mRealmProfileTransactions = realmProfileTransactions;
    }


    public void getContactList(String api){
        Log.i(Constants.TAG, "ContactsController.getContactList: " + api);

        try{
            OKHttpWrapper.get(api, mContext, new OKHttpWrapper.HttpCallback() {
                @Override
                public void onFailure(Response response, IOException e) {
                    Log.i(Constants.TAG, "ContactsController.onFailure:");
                }

                @Override
                public void onSuccess(Response response) {
                    try {
                        String json;
                        if (response.isSuccessful()) {
                            json = response.body().string();
                            if (json != null && json.trim().length() > 0) {
                                JSONObject jsonResponse = new JSONObject(json);

                                insertContactListInRealm(jsonResponse);
                                //Update Contact List View on every pagination
                                BusProvider.getInstance().post(new SetContactListAdapterEvent());

                                JSONObject jsonPagination = jsonResponse.getJSONObject(Constants.CONTACT_PAGINATION);
                                if (jsonPagination.getBoolean(Constants.CONTACT_PAGINATION_MORE_PAGES)) {
                                    int pageSize = jsonPagination.getInt(Constants.CONTACT_PAGINATION_PAGESIZE);
                                    offsetPaging = offsetPaging + pageSize;
                                    getContactList(Constants.CONTACT_API_GET_CONTACTS + "&o=" + offsetPaging);
                                } else {
                                    offsetPaging = 0;
                                    //Bus Event Post when contacts have been received
                                    BusProvider.getInstance().post(new ContactListReceivedEvent());
                                }
                            }
                        } else {
                            Log.e(Constants.TAG, "ContactsController.isNOTSuccessful");
                        }
                    } catch (Exception e) {
                        Log.e(Constants.TAG, "ContactsController.onSuccess: ", e);
                    }
                }
            });
        } catch (Exception e){
            Log.e(Constants.TAG, "ContactsController.getContactList: ", e);
        }
    }

}
