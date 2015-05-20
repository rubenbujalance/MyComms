package com.vodafone.mycomms.contacts.connection;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.framework.library.model.ConnectionResponse;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import model.Contact;
import model.FavouriteContact;
import model.RecentContact;

/**
 * Created by AMG on 18/05/2015.
 */
public class ContactController extends BaseController {

    private ContactConnection contactConnection;
    private IContactsConnectionCallback contactsConnectionCallback;
    private Realm mRealm;
    private Context mContext;
    private RealmContactTransactions realmContactTransactions;

    public ContactController(Activity activity, Realm realm) {
        super(activity);
        this.mRealm = realm;
        this.mContext = activity;
        realmContactTransactions = new RealmContactTransactions(realm);
    }

    public ContactController(Fragment fragment, Realm realm) {
        super(fragment);
        this.mRealm = realm;
        this.mContext = fragment.getActivity();
        realmContactTransactions = new RealmContactTransactions(realm);
    }

    public void getContactList(String accessToken){
        Log.i(Constants.TAG, "ContactController.startLogin: accessToken " + accessToken);
        if(contactConnection != null){
            contactConnection.cancel();
        }
        contactConnection = new ContactConnection(getContext(), this);

        contactConnection.request();
    }

    @Override
    public void onConnectionComplete(ConnectionResponse response){
        super.onConnectionComplete(response);
        Log.i(Constants.TAG, "ContactController.onConnectionComplete: ");
        String result = response.getData().toString();
        Log.i(Constants.TAG, "ContactController.onConnectionComplete: " + result);
        JSONObject jsonResponse;

        try {
            jsonResponse = new JSONObject(result);
            Log.i(Constants.TAG, "ContactController.onConnectionComplete: jsonResponse: " + jsonResponse.toString());
            ArrayList<Contact> realmContactList = new ArrayList<>();
            realmContactList = insertContactListInRealm(jsonResponse);
            if(this.getConnectionCallback() != null && this.getConnectionCallback() instanceof IContactsConnectionCallback){
                ((IContactsConnectionCallback)this.getConnectionCallback()).onContactsResponse(realmContactList);
            }
        } catch (Exception e){
            Log.e(Constants.TAG, "ContactController.onConnectionComplete: " + e.toString());
        }
    }

    private ArrayList<Contact> insertContactListInRealm(JSONObject jsonObject) {
        ArrayList<Contact> realmContactList = new ArrayList<>();
        try {
            Log.i(Constants.TAG, "ContactController.insertContactListInRealm: ");
            JSONArray jsonArray = jsonObject.getJSONArray(Constants.CONTACT_DATA);
            Contact contact;
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                contact = mapContact(jsonObject);
                realmContactList.add(contact);
            }
            RealmContactTransactions realmContactTransactions = new RealmContactTransactions(mRealm);
            realmContactTransactions.insertContactList(realmContactList);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactController.insertContactListInRealm: " + e.toString());
            return null;
        }
        return realmContactList;
    }

    public List<Contact> getAllContacts(){
        return realmContactTransactions.getAllContacts();
    }

    public ArrayList<FavouriteContact> getAllFavouriteContacts(){
        ArrayList<FavouriteContact> favList = new ArrayList<>();
        favList = realmContactTransactions.getAllFavouriteContacts();
        return favList;
    }

    public ArrayList<RecentContact> getAllRecentContacts(){
        return realmContactTransactions.getAllRecentContacts();
    }

    public void insertContactsInRealm(JSONObject json){
        JSONObject jsonObject = null;
        JSONArray jsonArray = null;
        try {
            jsonObject = new JSONObject(json.toString());
            jsonArray = jsonObject.getJSONArray(Constants.CONTACT_DATA);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactDBController.insertContactsInRealm: " + e.toString());
        }
        Contact contact;
        ArrayList<Contact> contactList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                contact = mapContact(jsonObject);
                if (contact != null) {
                    contactList.add(contact);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactDBController.insertContactsInRealm: " + e.toString());
        }
        realmContactTransactions.insertContactList(contactList);
    }

    public void insertFavouriteContactInRealm(JsonElement json){
        JSONObject jsonObject = null;
        JSONArray jsonArray = null;
        try {
            jsonObject = new JSONObject(json.toString());
            jsonArray = jsonObject.getJSONArray(Constants.CONTACT_DATA);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactDBController.insertFavouriteContactInRealm: " + e.toString());
        }
        Contact contact;
        ArrayList<Contact> contactList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                contact = mapContact(jsonObject);
                if (contact != null) {
                    contactList.add(contact);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactDBController.insertFavouriteContactInRealm: " + e.toString());
        }
        realmContactTransactions.insertContactList(contactList);
    }

    public static Contact mapContact(JSONObject jsonObject){
        Contact contact = new Contact();
        try {
            if (!jsonObject.isNull(Constants.CONTACT_ID)) contact.setId(jsonObject.getString(Constants.CONTACT_ID));
            if (!jsonObject.isNull(Constants.CONTACT_DATA)) contact.setId(jsonObject.getString(Constants.CONTACT_DATA));
            if (!jsonObject.isNull(Constants.CONTACT_PLATFORM))
                contact.setPlatform(jsonObject.getString(Constants.CONTACT_PLATFORM));
            if (!jsonObject.isNull(Constants.CONTACT_FNAME))
                contact.setFirstName(jsonObject.getString(Constants.CONTACT_FNAME));
            if (!jsonObject.isNull(Constants.CONTACT_LNAME))
                contact.setLastName(jsonObject.getString(Constants.CONTACT_LNAME));
            if (!jsonObject.isNull(Constants.CONTACT_AVATAR)) contact.setAvatar(jsonObject.getString(Constants.CONTACT_AVATAR));
            if (!jsonObject.isNull(Constants.CONTACT_POSITION))
                contact.setPosition(jsonObject.getString(Constants.CONTACT_POSITION));
            if (!jsonObject.isNull(Constants.CONTACT_COMPANY)) contact.setCompany(jsonObject.getString(Constants.CONTACT_COMPANY));
            if (!jsonObject.isNull(Constants.CONTACT_TIMEZONE))
                contact.setTimezone(jsonObject.getString(Constants.CONTACT_TIMEZONE));
            if (!jsonObject.isNull(Constants.CONTACT_LASTSEEN)) contact.setLastSeen(jsonObject.getInt(Constants.CONTACT_LASTSEEN));
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
        }catch (JSONException e){
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactDBController.mapContact: " + e.toString());
        }
        return contact;
    }

    public FavouriteContact mapFavouriteContact(JSONObject jsonObject){
        FavouriteContact contact = new FavouriteContact();
        try {
            if (!jsonObject.isNull(Constants.CONTACT_ID)) contact.setId(jsonObject.getString(Constants.CONTACT_ID));
            if (!jsonObject.isNull(Constants.CONTACT_PLATFORM))
                contact.setPlatform(jsonObject.getString(Constants.CONTACT_PLATFORM));
            if (!jsonObject.isNull(Constants.CONTACT_FNAME))
                contact.setFirstName(jsonObject.getString(Constants.CONTACT_FNAME));
            if (!jsonObject.isNull(Constants.CONTACT_LNAME))
                contact.setLastName(jsonObject.getString(Constants.CONTACT_LNAME));
            if (!jsonObject.isNull(Constants.CONTACT_AVATAR)) contact.setAvatar(jsonObject.getString(Constants.CONTACT_AVATAR));
            if (!jsonObject.isNull(Constants.CONTACT_POSITION))
                contact.setPosition(jsonObject.getString(Constants.CONTACT_POSITION));
            if (!jsonObject.isNull(Constants.CONTACT_COMPANY)) contact.setCompany(jsonObject.getString(Constants.CONTACT_COMPANY));
            if (!jsonObject.isNull(Constants.CONTACT_TIMEZONE))
                contact.setTimezone(jsonObject.getString(Constants.CONTACT_TIMEZONE));
            if (!jsonObject.isNull(Constants.CONTACT_LASTSEEN)) contact.setLastSeen(jsonObject.getInt(Constants.CONTACT_LASTSEEN));
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
        }catch (JSONException e){
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactDBController.mapFavouriteContact: " + e.toString());
        }
        return contact;
    }

    public FavouriteContact mapContactToFavourite(Contact contact){
        FavouriteContact favoriteContact = new FavouriteContact();
        favoriteContact.setId(contact.getId());
        favoriteContact.setPlatform(contact.getPlatform());
        favoriteContact.setFirstName(contact.getFirstName());
        favoriteContact.setLastName(contact.getLastName());
        favoriteContact.setAvatar(contact.getAvatar());
        favoriteContact.setCompany(contact.getCompany());
        favoriteContact.setLastSeen(contact.getLastSeen());
        favoriteContact.setOfficeLocation(contact.getOfficeLocation());
        favoriteContact.setPhones(contact.getPhones());
        favoriteContact.setOfficeLocation(contact.getOfficeLocation());
        favoriteContact.setEmails(contact.getEmails());
        favoriteContact.setAvailability(contact.getAvailability());
        favoriteContact.setPresence(contact.getPresence());
        favoriteContact.setCountry(contact.getCountry());
        return favoriteContact;
    }

    public RecentContact mapRecentContact(JSONObject jsonObject){
        RecentContact contact = new RecentContact();
        try {
            if (!jsonObject.isNull(Constants.CONTACT_ID)) contact.setId(jsonObject.getString(Constants.CONTACT_ID));
            if (!jsonObject.isNull(Constants.CONTACT_PLATFORM))
                contact.setPlatform(jsonObject.getString(Constants.CONTACT_PLATFORM));
            if (!jsonObject.isNull(Constants.CONTACT_FNAME))
                contact.setFirstName(jsonObject.getString(Constants.CONTACT_FNAME));
            if (!jsonObject.isNull(Constants.CONTACT_LNAME))
                contact.setLastName(jsonObject.getString(Constants.CONTACT_LNAME));
            if (!jsonObject.isNull(Constants.CONTACT_AVATAR)) contact.setAvatar(jsonObject.getString(Constants.CONTACT_AVATAR));
            if (!jsonObject.isNull(Constants.CONTACT_POSITION))
                contact.setPosition(jsonObject.getString(Constants.CONTACT_POSITION));
            if (!jsonObject.isNull(Constants.CONTACT_COMPANY)) contact.setCompany(jsonObject.getString(Constants.CONTACT_COMPANY));
            if (!jsonObject.isNull(Constants.CONTACT_TIMEZONE))
                contact.setTimezone(jsonObject.getString(Constants.CONTACT_TIMEZONE));
            if (!jsonObject.isNull(Constants.CONTACT_LASTSEEN)) contact.setLastSeen(jsonObject.getInt(Constants.CONTACT_LASTSEEN));
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

        }catch (JSONException e){
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactDBController.mapRecentContact: " + e.toString());
        }
        return contact;
    }

    public RecentContact mapContactToRecent(Contact contact){
        RecentContact recentContact = new RecentContact();
        recentContact.setId(contact.getId());
        recentContact.setPlatform(contact.getPlatform());
        recentContact.setFirstName(contact.getFirstName());
        recentContact.setLastName(contact.getLastName());
        recentContact.setAvatar(contact.getAvatar());
        recentContact.setCompany(contact.getCompany());
        recentContact.setLastSeen(contact.getLastSeen());
        recentContact.setOfficeLocation(contact.getOfficeLocation());
        recentContact.setPhones(contact.getPhones());
        recentContact.setOfficeLocation(contact.getOfficeLocation());
        recentContact.setEmails(contact.getEmails());
        recentContact.setAvailability(contact.getAvailability());
        recentContact.setPresence(contact.getPresence());
        recentContact.setCountry(contact.getCountry());
        return recentContact;
    }

    public void loadFakeContacts() {
        //if (realmContactTransactions.getAllContacts().size() == 0){
        InputStream stream = null;
        stream = mContext.getResources().openRawResource(R.raw.test_contacts);
        JsonElement json = new JsonParser().parse(new InputStreamReader(stream));
        JSONObject jsonObject = null;
        JSONArray jsonArray = null;
        try {
            jsonObject = new JSONObject(json.toString());
            jsonArray = jsonObject.getJSONArray(Constants.CONTACT_DATA);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            ArrayList<Contact> realmContactList = new ArrayList<>();
            Contact contact;
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                contact = mapContact(jsonObject);
                realmContactList.add(contact);
            }
            realmContactTransactions.insertContactList(realmContactList);
        } catch (JSONException e) {
            Log.e(Constants.TAG, "loadFakeContacts: " + e.toString());
        }
        //}
    }

    public void loadFakeFavouriteContacts() {
        //if (realmContactTransactions.getAllFavouriteContacts().size() == 0){
        InputStream stream = null;
        stream = mContext.getResources().openRawResource(R.raw.test_contacts);
        JsonElement json = new JsonParser().parse(new InputStreamReader(stream));
        JSONObject jsonObject = null;
        JSONArray jsonArray = null;
        try {
            jsonObject = new JSONObject(json.toString());
            jsonArray = jsonObject.getJSONArray(Constants.CONTACT_DATA);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "loadFakeFavouriteContact: " + e.toString());
        }

        try {
            ArrayList<FavouriteContact> realmContactList = new ArrayList<>();
            FavouriteContact contact;
            for (int i = 0; i < 2; i++) {
                jsonObject = jsonArray.getJSONObject(i);
                contact = mapFavouriteContact(jsonObject);
                realmContactList.add(contact);
            }
            realmContactTransactions.insertFavouriteContactList(realmContactList);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "loadFakeFavouriteContact: " + e.toString());
        }
        //}
    }
}