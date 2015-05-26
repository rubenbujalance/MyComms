package com.vodafone.mycomms.contacts.connection;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.framework.library.exception.ConnectionException;
import com.framework.library.model.ConnectionResponse;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.connection.IConnectionCallback;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.SetConnectionLayoutVisibility;
import com.vodafone.mycomms.events.SetContactListAdapterEvent;
import com.vodafone.mycomms.events.SetNoConnectionLayoutVisibility;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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
    private String apiCall;
    private int search = Constants.CONTACTS_ALL;
    private int offsetPaging = 0;

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

    public void getContactList(String api){
        Log.i(Constants.TAG, "ContactController.getContactList: " + api);
        if(contactConnection != null){
            contactConnection.cancel();
        }
        apiCall = api;
        contactConnection = new ContactConnection(getContext(), this, apiCall);
        contactConnection.request();
    }

    public void getFavouritesList(String api){
        Log.i(Constants.TAG, "ContactController.getFavouritesList: ");
        if(contactConnection != null){
            contactConnection.cancel();
        }
        apiCall = api;
        contactConnection = new ContactConnection(getContext(), this, apiCall);
        contactConnection.request();
    }

    public void getRecentList(String api){
        Log.i(Constants.TAG, "ContactController.getRecentList: ");
        if(contactConnection != null){
            contactConnection.cancel();
        }
        apiCall = api;
        contactConnection = new ContactConnection(getContext(), this, apiCall);
        contactConnection.request();
    }

    @Override
    public void onConnectionComplete(ConnectionResponse response){
        super.onConnectionComplete(response);
        boolean morePages = false;
        Log.i(Constants.TAG, "ContactController.onConnectionComplete: init: " + apiCall + ", url=" + response.getUrl());
        String result = response.getData().toString();
        Log.i(Constants.TAG, "ContactController.onConnectionComplete: " + result);
        JSONObject jsonResponse;

        switch (search) {
            case Constants.CONTACTS_ALL:
                try {
                    jsonResponse = new JSONObject(result);
                    JSONObject jsonPagination = jsonResponse.getJSONObject(Constants.CONTACT_PAGINATION);
                    if (jsonPagination.getBoolean(Constants.CONTACT_PAGINATION_MORE_PAGES)) {
                        morePages = true;
                        offsetPaging = offsetPaging + 1;
                        search = Constants.CONTACTS_ALL;
                    } else{
                        search = Constants.CONTACTS_RECENT;
                        offsetPaging = 0;
                    }
                    Log.i(Constants.TAG, "ContactController.onConnectionComplete: jsonResponse: " + jsonResponse.toString());
                    ArrayList<Contact> realmContactList = new ArrayList<>();
                    realmContactList = insertContactListInRealm(jsonResponse);
                    if (this.getConnectionCallback() != null && this.getConnectionCallback() instanceof IContactsConnectionCallback) {
                        ((IContactsConnectionCallback) this.getConnectionCallback()).onContactsResponse(realmContactList, morePages, offsetPaging);
                    }

                    // realmContactTransactions.getContactById("mc_55361a9cc729d4430b9722f3");

                } catch (Exception e) {
                    Log.e(Constants.TAG, "ContactController.onConnectionComplete: contacts ", e);
                }
                break;
            case Constants.CONTACTS_RECENT:
                try {
                    search = Constants.CONTACTS_FAVOURITE;
                    jsonResponse = new JSONObject(result);

                    insertRecentContactInRealm(jsonResponse);

                    if (this.getConnectionCallback() != null && this.getConnectionCallback() instanceof IContactsConnectionCallback) {
                        ((IContactsConnectionCallback) this.getConnectionCallback()).onRecentContactsResponse();
                    }
                } catch (Exception e) {
                    Log.e(Constants.TAG, "ContactController.onConnectionComplete: recents ", e);
                }
                break;
            case Constants.CONTACTS_FAVOURITE:
                try {
                    search = Constants.CONTACTS_ALL;
                    if (result != null && !result.equals("")) {
                        jsonResponse = new JSONObject(result);
                        insertFavouriteContactInRealm(jsonResponse);
                    } else {
                        realmContactTransactions.deleteAllFavouriteContacts();
                    }
                    BusProvider.getInstance().post(new SetContactListAdapterEvent());
                } catch (Exception e) {
                    Log.e(Constants.TAG, "ContactController.onConnectionComplete: favourites", e);
                }
                break;
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

    public ArrayList<Contact> getAllContacts() {
        Log.d(Constants.TAG, "ContactController.getAllContacts: ");
        return realmContactTransactions.getAllContacts();
    }

    public ArrayList<FavouriteContact> getAllFavouriteContacts(){
        Log.i(Constants.TAG, "ContactController.getAllFavouriteContacts: ");
        ArrayList<FavouriteContact> favList = new ArrayList<>();
        favList = realmContactTransactions.getAllFavouriteContacts();
        return favList;
    }

    public ArrayList<RecentContact> getAllRecentContacts(){
        Log.i(Constants.TAG, "ContactController.getAllRecentContacts: ");
        ArrayList<RecentContact> recentList = new ArrayList<>();
        recentList = realmContactTransactions.getAllRecentContacts();
        return recentList;
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

    public void insertFavouriteContactInRealm(JSONObject json){
        JSONArray jsonArray = null;
        Contact contact;
        ArrayList<FavouriteContact> contactList = new ArrayList<>();
        try {
            Log.i(Constants.TAG, "ContactController.insertFavouriteContactInRealm: jsonResponse: " + json.toString());
            jsonArray = json.getJSONArray(Constants.CONTACT_FAVOURITES);
            for (int i = 0; i < jsonArray.length(); i++) {
                contact = realmContactTransactions.getContactById(jsonArray.getString(i));
                if (contact != null) {
                    contactList.add(mapContactToFavourite(contact));
                }
            }
            if (contactList.size()!=0) {
                realmContactTransactions.deleteAllFavouriteContacts();
                realmContactTransactions.insertFavouriteContactList(contactList);
                Log.i(Constants.TAG, "ContactController.insertFavouriteContactInRealm: inserted contactList ");
                //BusProvider.getInstance().post(new SetContactListAdapterEvent());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactController.insertFavouriteContactInRealm : " + e.toString());
        }
    }

    public void insertRecentContactInRealm(JSONObject json){
        JSONArray jsonArray = null;
        Contact contact;
        ArrayList<RecentContact> contactList = new ArrayList<>();
        try {
            Log.i(Constants.TAG, "ContactController.insertRecentContactInRealm: jsonResponse: " + json.toString());
            jsonArray = json.getJSONArray(Constants.CONTACT_RECENTS);
            for (int i = 0; i < jsonArray.length(); i++) {
                contact = realmContactTransactions.getContactById(jsonArray.getJSONObject(i).getString("id"));
                if (contact != null) {
                    contactList.add(mapContactToRecent(contact, jsonArray.getJSONObject(i)));
                }
            }
            if (contactList.size()!=0) {
                realmContactTransactions.deleteAllRecentContacts();
                realmContactTransactions.insertRecentContactList(contactList);
                Log.i(Constants.TAG, "ContactController.insertRecentContactInRealm: inserted contactList ");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactController.insertRecentContactInRealm : " + e.toString());
        }
    }

    public static Contact mapContact(JSONObject jsonObject){
        Contact contact = new Contact();
        try {
            if (!jsonObject.isNull(Constants.CONTACT_ID)) contact.setId(jsonObject.getString(Constants.CONTACT_ID));
            //if (!jsonObject.isNull(Constants.CONTACT_DATA)) contact.setId(jsonObject.getString(Constants.CONTACT_DATA));
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
        favoriteContact.setPosition(contact.getPosition());
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

    public RecentContact mapContactToRecent(Contact contact, JSONObject jsonObject){
        RecentContact recentContact = new RecentContact();
        recentContact.setId(contact.getId());
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
            recentContact.setAction(jsonObject.getString(Constants.CONTACT_RECENTS_ACTION));
            recentContact.setActionTimeStamp(jsonObject.getInt(Constants.CONTACT_RECENTS_ACTION_TIME));
        } catch (JSONException e){
            Log.e(Constants.TAG, "ContactController.mapContactToRecent: " + e);
        }
        return recentContact;
    }

    @Override
    public void onConnectionError(ConnectionException e) {
        super.onConnectionError(e);
        BusProvider.getInstance().post(new SetNoConnectionLayoutVisibility());
    }

    @Override
    public IConnectionCallback getConnectionCallback() {
        BusProvider.getInstance().post(new SetConnectionLayoutVisibility());
        return super.getConnectionCallback();
    }
}
