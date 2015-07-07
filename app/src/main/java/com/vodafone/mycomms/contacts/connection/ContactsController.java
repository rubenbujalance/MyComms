package com.vodafone.mycomms.contacts.connection;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.framework.library.exception.ConnectionException;
import com.framework.library.model.ConnectionResponse;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.connection.IConnectionCallback;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.SetConnectionLayoutVisibility;
import com.vodafone.mycomms.events.SetContactListAdapterEvent;
import com.vodafone.mycomms.events.SetNoConnectionLayoutVisibility;
import com.vodafone.mycomms.realm.RealmAvatarTransactions;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.InternalContactSearch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import model.Contact;
import model.FavouriteContact;
import model.RecentContact;

/**
 * Created by AMG on 18/05/2015.
 */
public class ContactsController extends BaseController {

    private ContactConnection contactConnection;
    private IContactsConnectionCallback contactsConnectionCallback;
    private Context mContext;
    private RealmContactTransactions realmContactTransactions;
    private RealmAvatarTransactions realmAvatarTransactions;
    private InternalContactSearch internalContactSearch;
    private String apiCall;
    private String mProfileId;
    private int search = Constants.CONTACTS_ALL;
    private int offsetPaging = 0;

    public ContactsController(Activity activity, String profileId) {
        super(activity);
        this.mContext = activity;
        this.mProfileId = profileId;
        realmContactTransactions = new RealmContactTransactions(mProfileId);
        realmAvatarTransactions = new RealmAvatarTransactions();
        internalContactSearch = new InternalContactSearch(activity, profileId);
    }

    public ContactsController(Fragment fragment, String profileId) {
        super(fragment);
        this.mContext = fragment.getActivity();
        this.mProfileId = profileId;
        realmContactTransactions = new RealmContactTransactions(mProfileId);
        realmAvatarTransactions = new RealmAvatarTransactions();
    }

    public ContactsController(Context context, String profileId) {
        super(context);
        this.mContext = context;
        this.mProfileId = profileId;
        realmContactTransactions = new RealmContactTransactions(mProfileId);
        realmAvatarTransactions = new RealmAvatarTransactions();
    }

    public void getContactList(String api){
        Log.i(Constants.TAG, "ContactsController.getContactList: " + api);
        if(contactConnection != null){
            contactConnection.cancel();
        }
        apiCall = api;
        contactConnection = new ContactConnection(getContext(), this, apiCall);
        contactConnection.request();
    }

    public void getFavouritesList(String api){
        Log.i(Constants.TAG, "ContactsController.getFavouritesList: ");
        if(contactConnection != null){
            contactConnection.cancel();
        }
        apiCall = api;
        contactConnection = new ContactConnection(getContext(), this, apiCall);
        contactConnection.request();
    }

    public void getRecentList(String api) {
        Log.i(Constants.TAG, "ContactsController.getRecentList: ");
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
        Log.i(Constants.TAG, "ContactsController.onConnectionComplete: init: " + apiCall + ", url=" + response.getUrl());
        String result = response.getData().toString();
        JSONObject jsonResponse;
        if (apiCall.equals(Constants.CONTACT_API_GET_FAVOURITES)){
            search = Constants.CONTACTS_FAVOURITE;
        } else if (apiCall.equals(Constants.CONTACT_API_GET_RECENTS)){
            search = Constants.CONTACTS_RECENT;
        }

        if (result != null && result.trim().length()>0) {
            switch (search) {
                case Constants.CONTACTS_ALL:
                    try {
                        jsonResponse = new JSONObject(result);
                        JSONObject jsonPagination = jsonResponse.getJSONObject(Constants.CONTACT_PAGINATION);
                        if (jsonPagination.getBoolean(Constants.CONTACT_PAGINATION_MORE_PAGES)) {
                            int pageSize = jsonPagination.getInt(Constants.CONTACT_PAGINATION_PAGESIZE);
                            morePages = true;
                            offsetPaging = offsetPaging + pageSize;
                            search = Constants.CONTACTS_ALL;
                        } else {
                            search = Constants.CONTACTS_RECENT;
                            offsetPaging = 0;
                        }
                        ArrayList<Contact> realmContactList = new ArrayList<>();
                        realmContactList = insertContactListInRealm(jsonResponse);
                        BusProvider.getInstance().post(new SetContactListAdapterEvent());
                        if (this.getConnectionCallback() != null && this.getConnectionCallback() instanceof IContactsConnectionCallback) {
                            ((IContactsConnectionCallback) this.getConnectionCallback()).onContactsResponse(realmContactList, morePages, offsetPaging);
                        }
                        // realmContactTransactions.getContactById("mc_55361a9cc729d4430b9722f3");

                    } catch (Exception e) {
                        Log.e(Constants.TAG, "ContactsController.onConnectionComplete: contacts ", e);
                    }
                    break;
                case Constants.CONTACTS_RECENT:
                    try {
                        search = Constants.CONTACTS_FAVOURITE;
                        jsonResponse = new JSONObject(result);

                        insertRecentContactInRealm(jsonResponse);

                        if (this.getConnectionCallback() != null && this.getConnectionCallback() instanceof IContactsConnectionCallback) {
                            ((IContactsConnectionCallback) this.getConnectionCallback()).onRecentContactsResponse();
                        } else{
                            BusProvider.getInstance().post(new SetContactListAdapterEvent());
                        }
                    } catch (Exception e) {
                        Log.e(Constants.TAG, "ContactsController.onConnectionComplete: recents ", e);
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
                        Log.e(Constants.TAG, "ContactsController.onConnectionComplete: favourites", e);
                    }
                    break;
            }
        }
    }

    public ArrayList<Contact> insertContactListInRealm(JSONObject jsonObject) {
        ArrayList<Contact> realmContactList = new ArrayList<>();

        try {
            Log.i(Constants.TAG, "ContactsController.insertContactListInRealm: ");
            JSONArray jsonArray = jsonObject.getJSONArray(Constants.CONTACT_DATA);
            Contact contact;

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                contact = mapContact(jsonObject, mProfileId);
                realmContactList.add(contact);
            }
            realmContactTransactions.insertContactList(realmContactList);

            //new DownloadAvatars().execute(realmContactList);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactsController.insertContactListInRealm: " + e.toString());
            return null;
        }
        return realmContactList;
    }

    public ArrayList<Contact> getAllContacts() {
        Log.d(Constants.TAG, "ContactsController.getAllContacts: ");
        return realmContactTransactions.getAllContacts();
    }

    public ArrayList<FavouriteContact> getAllFavouriteContacts(){
        Log.i(Constants.TAG, "ContactsController.getAllFavouriteContacts: ");
        ArrayList<FavouriteContact> favList = new ArrayList<>();
        favList = realmContactTransactions.getAllFavouriteContacts();
        return favList;
    }

    public ArrayList<RecentContact> getAllRecentContacts(){
        Log.i(Constants.TAG, "ContactsController.getAllRecentContacts: ");
        ArrayList<RecentContact> recentList = new ArrayList<>();
        recentList = realmContactTransactions.getAllRecentContacts();
        return recentList;
    }

    public void insertFavouriteContactInRealm(JSONObject json){
        JSONArray jsonArray = null;
        Contact contact;
        ArrayList<FavouriteContact> contactList = new ArrayList<>();
        try {
            Log.i(Constants.TAG, "ContactsController.insertFavouriteContactInRealm: jsonResponse: " + json.toString());
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
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactsController.insertFavouriteContactInRealm : " + e.toString());
        }
    }

    public void insertFavouriteContactInRealm(JSONObject json, boolean isGroupChatObject){
        JSONArray jsonArray = null;
        Contact contact;
        ArrayList<FavouriteContact> contactList = new ArrayList<>();
        try {
            Log.i(Constants.TAG, "ContactsController.insertFavouriteContactInRealm: jsonResponse: " + json.toString());
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
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactsController.insertFavouriteContactInRealm : " + e.toString());
        }
    }

    public void insertRecentContactInRealm(JSONObject json){
        JSONArray jsonArray = null;
        Contact contact;
        ArrayList<RecentContact> contactList = new ArrayList<>();
        try {
            Log.i(Constants.TAG, "ContactsController.insertRecentContactInRealm: jsonResponse: " + json.toString());
            jsonArray = json.getJSONArray(Constants.CONTACT_RECENTS);
            for (int i = 0; i < jsonArray.length(); i++) {
                contact = realmContactTransactions.getContactById(jsonArray.getJSONObject(i).getString(Constants.CONTACT_ID));
                if (contact != null) {
                    contactList.add(mapContactToRecent(contact, jsonArray.getJSONObject(i)));
                }
            }
            if (contactList.size()!=0) {
                //realmContactTransactions.deleteAllRecentContacts();
                realmContactTransactions.insertRecentContactList(contactList);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactsController.insertRecentContactInRealm : " + e.toString());
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
            if (!jsonObject.isNull(Constants.CONTACT_LASTSEEN)) contact.setLastSeen(jsonObject.getLong(Constants.CONTACT_LASTSEEN));
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

            contact.setSearchHelper((contact.getFirstName() + " " + contact.getLastName() + " "
                    + contact.getCompany() + " " + contact.getEmails()).trim());
            contact.setSortHelper((contact.getFirstName() + " " + contact.getLastName() + " "
                    + contact.getCompany()).trim());

        }catch (JSONException e){
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactDBController.mapContact: " + e.toString());
        }
        return contact;
    }

    public FavouriteContact mapFavouriteContact(JSONObject jsonObject){
        FavouriteContact contact = new FavouriteContact();
        try {
            contact.setContactId(mProfileId);
            if (!jsonObject.isNull(Constants.CONTACT_ID)){
                contact.setContactId(jsonObject.getString(Constants.CONTACT_ID));
                contact.setId(mProfileId + "_" + jsonObject.getString(Constants.CONTACT_ID));
            }
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

    public RecentContact mapRecentContact(JSONObject jsonObject){
        RecentContact contact = new RecentContact();
        try {
            contact.setProfileId(mProfileId);
            if (!jsonObject.isNull(Constants.CONTACT_ID)){
                contact.setContactId(jsonObject.getString(Constants.CONTACT_ID));
                contact.setId(mProfileId + "_" + jsonObject.getString(Constants.CONTACT_ID));
            }
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
            Log.e(Constants.TAG, "ContactsController.mapContactToRecent: " + e);
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

    public void closeRealm() {
        realmAvatarTransactions.closeRealm();
        realmContactTransactions.closeRealm();
    }
}
