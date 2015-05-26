package com.vodafone.mycomms.contacts.connection;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.framework.library.model.ConnectionResponse;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.SetContactListAdapterEvent;
import com.vodafone.mycomms.realm.RealmAvatarTransactions;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import io.realm.Realm;
import model.Contact;
import model.ContactAvatar;
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

    public void getContactList(String accessToken, String api){
        Log.i(Constants.TAG, "ContactController.getContactList: " + api);
        if(contactConnection != null){
            contactConnection.cancel();
        }
        apiCall = api;
        contactConnection = new ContactConnection(getContext(), this, apiCall);
        contactConnection.request();
    }

    public void getFavouritesList(String accessToken, String api){
        Log.i(Constants.TAG, "ContactController.getFavouritesList: accessToken " + accessToken);
        if(contactConnection != null){
            contactConnection.cancel();
        }
        apiCall = api;
        contactConnection = new ContactConnection(getContext(), this, apiCall);
        contactConnection.request();
    }

    public void getRecentList(String accessToken, String api){
        Log.i(Constants.TAG, "ContactController.getRecentList: accessToken " + accessToken);
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
        Log.i(Constants.TAG, "ContactController.onConnectionComplete: init: " + apiCall + ", url=" + response.getUrl());
        String result = response.getData().toString();
        Log.i(Constants.TAG, "ContactController.onConnectionComplete: " + result);
        JSONObject jsonResponse;

        if (apiCall.equals(Constants.CONTACT_API_GET_CONTACTS) &&
                result != null && result.trim().length()>0) {
            try {
                jsonResponse = new JSONObject(result);
                Log.i(Constants.TAG, "ContactController.onConnectionComplete: jsonResponse: " + jsonResponse.toString());
                ArrayList<Contact> realmContactList = insertContactListInRealm(jsonResponse);

                if (this.getConnectionCallback() != null && this.getConnectionCallback() instanceof IContactsConnectionCallback) {
                    ((IContactsConnectionCallback) this.getConnectionCallback()).onContactsResponse(realmContactList);
                }

            } catch (Exception e) {
                Log.e(Constants.TAG, "ContactController.onConnectionComplete: contacts " , e);
            }
        } else if (apiCall.equals(Constants.CONTACT_API_GET_FAVOURITES) &&
                    result != null && result.trim().length()>0){
            try {
                jsonResponse = new JSONObject(result);

                insertFavouriteContactInRealm(jsonResponse);
                //Log.i(Constants.TAG, "ContactController.onConnectionComplete: BusProvider RefreshContactListEvent");
                //BusProvider.getInstance().post(new RefreshContactListEvent());
            } catch (Exception e) {
                Log.e(Constants.TAG, "ContactController.onConnectionComplete: favourites", e);
            }
        } else if (apiCall.equals(Constants.CONTACT_API_GET_RECENTS) &&
                result != null && result.trim().length()>0){
            try {
                jsonResponse = new JSONObject(result);

                insertRecentContactInRealm(jsonResponse);

                if (this.getConnectionCallback() != null && this.getConnectionCallback() instanceof IContactsConnectionCallback) {
                    ((IContactsConnectionCallback) this.getConnectionCallback()).onRecentsContactsResponse();
                }
            } catch (Exception e) {
                Log.e(Constants.TAG, "ContactController.onConnectionComplete: recents " , e);
            }
        }
    }

    private ArrayList<Contact> insertContactListInRealm(JSONObject jsonObject) {
        ArrayList<Contact> realmContactList = new ArrayList<>();

        try {
            Log.i(Constants.TAG, "ContactController.insertContactListInRealm: ");
            JSONArray jsonArray = jsonObject.getJSONArray(Constants.CONTACT_DATA);
            Contact contact;
            boolean doRefreshAdapter;

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                contact = mapContact(jsonObject);
                realmContactList.add(contact);
                doRefreshAdapter = (i==jsonArray.length()-1);
                updateContactAvatar(contact, doRefreshAdapter);
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

    private void updateContactAvatar (Contact contact, boolean doRefreshAdapter)
    {
        try {
            RealmAvatarTransactions realmAvatarTransactions = new RealmAvatarTransactions(mRealm);
            ContactAvatar avatar = realmAvatarTransactions.getContactAvatarByContactId(contact.getId());
            if (avatar == null || avatar.getUrl().compareTo(contact.getAvatar()) != 0) {
                String filename = "avatar_" + contact.getId() + ".jpg";
                Picasso.with(mContext)
                        .load(contact.getAvatar())
                        .into(getImageTarget(filename, doRefreshAdapter));

                if (avatar == null)
                    avatar = new ContactAvatar(contact.getId(), contact.getAvatar(), filename);
                else avatar.setUrl(contact.getAvatar());

                realmAvatarTransactions.insertAvatar(avatar);
            }
        } catch (Exception ex) {
            Log.e(Constants.TAG, "ContactController.updateContactAvatar: ", ex);
        }
    }

    private Target getImageTarget(final String fileName, final boolean doRefreshAdapter)
    {
        return new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        File file = new File(mContext.get, fileName);
                        try {
//                            file.createNewFile();
                            FileOutputStream ostream = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, ostream);
                            ostream.close();

                            if(doRefreshAdapter)
                                BusProvider.getInstance().post(new SetContactListAdapterEvent());
                        } catch (Exception e) {
                            Log.e(Constants.TAG, "ContactController.getImageTarget.run: ", e);
                        }
                    }
                }).start();
            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {}
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}
        };
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
                BusProvider.getInstance().post(new SetContactListAdapterEvent());
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

    public void loadFakeRecentContacts() {
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
            Log.e(Constants.TAG, "loadFakeRecentContact: " + e.toString());
        }

        try {
            ArrayList<RecentContact> realmContactList = new ArrayList<>();
            RecentContact contact;
            for (int i = 0; i < 2; i++) {
                jsonObject = jsonArray.getJSONObject(i);
                contact = mapRecentContact(jsonObject);
                realmContactList.add(contact);
            }
            realmContactTransactions.insertRecentContactList(realmContactList);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "loadFakeRecentContact: " + e.toString());
        }
    }
}
