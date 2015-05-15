package com.vodafone.mycomms.realm;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import io.realm.Realm;
import model.Contact;
import model.FavouriteContact;
import model.RecentContact;

/**
 * Created by AMG on 12/05/2015.
 */
public class MockClass {
    private Context mContext;
    private Realm mRealm;

    private ArrayList<Contact> contactList = new ArrayList<>();
    private ArrayList<FavouriteContact> favouriteContactList = new ArrayList<>();
    private ArrayList<RecentContact> recentContactList = new ArrayList<>();
    private Contact contact;
    private FavouriteContact favouriteContact;
    private RecentContact recentContact;

    public MockClass(Context context, Realm realm) {
        mContext = context;
        mRealm = realm;
        //Realm.deleteRealmFile(context);

        InputStream stream = null;
        stream = context.getResources().openRawResource(R.raw.test_contacts);
        RealmContactTransactions realmContactTransactions = new RealmContactTransactions(mRealm, context);
        //realmContactTransactions.insertContactListJSON(stream);


        JsonElement json = new JsonParser().parse(new InputStreamReader(stream));
        JSONObject jsonObject = null;
        JSONArray jsonArray = null;
        try {
            jsonObject = new JSONObject(json.toString());
            jsonArray = jsonObject.getJSONArray("data");
        } catch (JSONException e) {
            e.printStackTrace();
        }
/*        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();*/
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                //Contact contact = gson.fromJson(jsonArray.getJSONObject(i).toString(), Contact.class);

                jsonObject = jsonArray.getJSONObject(i);
                contact = new Contact();
                if (!jsonObject.isNull("id")) contact.setId(jsonObject.getString("id"));

                if (!jsonObject.isNull("platform")) contact.setPlatform(jsonObject.getString("platform"));
                if (!jsonObject.isNull("firstName")) contact.setFirstName(jsonObject.getString("firstName"));
                if (!jsonObject.isNull("lastName")) contact.setLastName(jsonObject.getString("lastName"));
                if (!jsonObject.isNull("avatar")) contact.setAvatar(jsonObject.getString("avatar"));
                if (!jsonObject.isNull("position")) contact.setPosition(jsonObject.getString("position"));
                if (!jsonObject.isNull("company")) contact.setCompany(jsonObject.getString("company"));
                if (!jsonObject.isNull("timezone")) contact.setTimezone(jsonObject.getString("timezone"));
                if (!jsonObject.isNull("lastSeen")) contact.setLastSeen(jsonObject.getInt("lastSeen"));
                if (!jsonObject.isNull("officeLocation")) contact.setOfficeLocation(jsonObject.getString("officeLocation"));

                if (!jsonObject.isNull("phones")) contact.setPhones(jsonObject.getJSONArray("phones").toString());
                if (!jsonObject.isNull("emails")) contact.setEmails(jsonObject.getJSONArray("emails").toString());

                contactList.add(contact);

                favouriteContact = new FavouriteContact();
                if (!jsonObject.isNull("id")) favouriteContact.setId(jsonObject.getString("id"));

                if (!jsonObject.isNull("platform")) favouriteContact.setPlatform(jsonObject.getString("platform"));
                if (!jsonObject.isNull("firstName")) favouriteContact.setFirstName(jsonObject.getString("firstName"));
                if (!jsonObject.isNull("lastName")) favouriteContact.setLastName(jsonObject.getString("lastName"));
                if (!jsonObject.isNull("avatar")) favouriteContact.setAvatar(jsonObject.getString("avatar"));
                if (!jsonObject.isNull("position")) favouriteContact.setPosition(jsonObject.getString("position"));
                if (!jsonObject.isNull("company")) favouriteContact.setCompany(jsonObject.getString("company"));
                if (!jsonObject.isNull("timezone")) favouriteContact.setTimezone(jsonObject.getString("timezone"));
                if (!jsonObject.isNull("lastSeen")) favouriteContact.setLastSeen(jsonObject.getInt("lastSeen"));
                if (!jsonObject.isNull("officeLocation")) favouriteContact.setOfficeLocation(jsonObject.getString("officeLocation"));

                if (!jsonObject.isNull("phones")) favouriteContact.setPhones(jsonObject.getJSONArray("phones").toString());
                if (!jsonObject.isNull("emails")) favouriteContact.setEmails(jsonObject.getJSONArray("emails").toString());

                favouriteContactList.add(favouriteContact);

                recentContact = new RecentContact();
                if (!jsonObject.isNull("id")) recentContact.setId(jsonObject.getString("id"));

                if (!jsonObject.isNull("platform")) recentContact.setPlatform(jsonObject.getString("platform"));
                if (!jsonObject.isNull("firstName")) recentContact.setFirstName(jsonObject.getString("firstName"));
                if (!jsonObject.isNull("lastName")) recentContact.setLastName(jsonObject.getString("lastName"));
                if (!jsonObject.isNull("avatar")) recentContact.setAvatar(jsonObject.getString("avatar"));
                if (!jsonObject.isNull("position")) recentContact.setPosition(jsonObject.getString("position"));
                if (!jsonObject.isNull("company")) recentContact.setCompany(jsonObject.getString("company"));
                if (!jsonObject.isNull("timezone")) recentContact.setTimezone(jsonObject.getString("timezone"));
                if (!jsonObject.isNull("lastSeen")) recentContact.setLastSeen(jsonObject.getInt("lastSeen"));
                if (!jsonObject.isNull("officeLocation")) recentContact.setOfficeLocation(jsonObject.getString("officeLocation"));

                if (!jsonObject.isNull("phones")) recentContact.setPhones(jsonObject.getJSONArray("phones").toString());
                if (!jsonObject.isNull("emails")) recentContact.setEmails(jsonObject.getJSONArray("emails").toString());

                recentContactList.add(recentContact);
            }
        } catch (JSONException e) {
            Log.e(Constants.TAG, "MockClass.MockClass: " + e.toString());
        }

        realmContactTransactions.insertContactList(contactList);
        realmContactTransactions.insertContact(contactList.get(1));
        realmContactTransactions.updateContact("id", "row_id1", contactList.get(2));
        realmContactTransactions.deleteContact("id", "row_id5");
        ArrayList<Contact> newContactList = realmContactTransactions.getAllContacts();
        for (int i=0;i<newContactList.size();i++){
            Log.i(Constants.TAG, "MockClass.newContactList:  " + newContactList.get(i).getId());
            Log.i(Constants.TAG, "MockClass.newContactList:  " + newContactList.get(i).getFirstName());
            Log.i(Constants.TAG, "MockClass.newContactList:  " + newContactList.get(i).getLastName());
            Log.i(Constants.TAG, "MockClass.newContactList:  " + newContactList.get(i).getCompany());
            Log.i(Constants.TAG, "MockClass.newContactList:  " + newContactList.get(i).getEmails());
            Log.i(Constants.TAG, "MockClass.newContactList:  " + newContactList.get(i).getPhones());
        }
        newContactList = realmContactTransactions.getFilteredContacts("timezone","America");
        for (int i=0;i<newContactList.size();i++){
            Log.i(Constants.TAG, "getFilteredContacts.newContactList:  " + newContactList.get(i).getId());
            Log.i(Constants.TAG, "getFilteredContacts.newContactList:  " + newContactList.get(i).getFirstName());
            Log.i(Constants.TAG, "getFilteredContacts.newContactList:  " + newContactList.get(i).getLastName());
            Log.i(Constants.TAG, "getFilteredContacts.newContactList:  " + newContactList.get(i).getTimezone());
        }

        realmContactTransactions.insertFavouriteContactList(favouriteContactList);
        realmContactTransactions.insertFavouriteContact(favouriteContactList.get(1));
        realmContactTransactions.updateFavouriteContact("id", "row_id1", favouriteContactList.get(3));
        realmContactTransactions.deleteFavouriteContact("id", "row_id5");
        ArrayList<FavouriteContact> newFavouriteContactList = realmContactTransactions.getAllFavouriteContacts();
        for (int i=0;i<newFavouriteContactList.size();i++){
            Log.i(Constants.TAG, "MockClass.newFavouriteContactList:  " + newFavouriteContactList.get(i).getId());
            Log.i(Constants.TAG, "MockClass.newFavouriteContactList:  " + newFavouriteContactList.get(i).getFirstName());
            Log.i(Constants.TAG, "MockClass.newFavouriteContactList:  " + newFavouriteContactList.get(i).getLastName());
            Log.i(Constants.TAG, "MockClass.newFavouriteContactList:  " + newFavouriteContactList.get(i).getCompany());
            Log.i(Constants.TAG, "MockClass.newFavouriteContactList:  " + newFavouriteContactList.get(i).getEmails());
            Log.i(Constants.TAG, "MockClass.newFavouriteContactList:  " + newFavouriteContactList.get(i).getPhones());
        }

        newFavouriteContactList = realmContactTransactions.getFilteredFavouriteContacts("timezone", "America");
        for (int i=0;i<newFavouriteContactList.size();i++){
            Log.i(Constants.TAG, "getFilteredFavouriteContacts.newFavouriteContactList:  " + newFavouriteContactList.get(i).getId());
            Log.i(Constants.TAG, "getFilteredFavouriteContacts.newFavouriteContactList:  " + newFavouriteContactList.get(i).getFirstName());
            Log.i(Constants.TAG, "getFilteredFavouriteContacts.newFavouriteContactList:  " + newFavouriteContactList.get(i).getLastName());
            Log.i(Constants.TAG, "getFilteredFavouriteContacts.newFavouriteContactList:  " + newFavouriteContactList.get(i).getTimezone());
        }

        realmContactTransactions.insertRecentContactList(recentContactList);
        realmContactTransactions.insertRecentContact(recentContactList.get(1));
        realmContactTransactions.updateRecentContact("id", "row_id1", recentContactList.get(4));
        realmContactTransactions.deleteRecentContact("id", "row_id5");
        ArrayList<RecentContact> newRecentContactList = realmContactTransactions.getAllRecentContacts();
        for (int i=0;i<newRecentContactList.size();i++){
            Log.i(Constants.TAG, "MockClass.newRecentContactList:  " + newRecentContactList.get(i).getId());
            Log.i(Constants.TAG, "MockClass.newRecentContactList:  " + newRecentContactList.get(i).getFirstName());
            Log.i(Constants.TAG, "MockClass.newRecentContactList:  " + newRecentContactList.get(i).getLastName());
            Log.i(Constants.TAG, "MockClass.newRecentContactList:  " + newRecentContactList.get(i).getCompany());
            Log.i(Constants.TAG, "MockClass.newRecentContactList:  " + newRecentContactList.get(i).getEmails());
            Log.i(Constants.TAG, "MockClass.newRecentContactList:  " + newRecentContactList.get(i).getPhones());
        }

        newRecentContactList = realmContactTransactions.getFilteredRecentContacts("timezone", "America");
        for (int i=0;i<newRecentContactList.size();i++){
            Log.i(Constants.TAG, "getFilteredRecentContacts.newRecentContactList:  " + newRecentContactList.get(i).getId());
            Log.i(Constants.TAG, "getFilteredRecentContacts.newRecentContactList:  " + newRecentContactList.get(i).getFirstName());
            Log.i(Constants.TAG, "getFilteredRecentContacts.newRecentContactList:  " + newRecentContactList.get(i).getLastName());
            Log.i(Constants.TAG, "getFilteredRecentContacts.newRecentContactList:  " + newRecentContactList.get(i).getTimezone());
        }

        realmContactTransactions.deleteAllContacts();
        newContactList = realmContactTransactions.getAllContacts();
        if (newContactList.size() == 0){
            Log.i(Constants.TAG, "newContactList.DELETED");
        }
        for (int i=0;i<newContactList.size();i++){
            Log.i(Constants.TAG, "MockClass.newContactList:  " + newContactList.get(i).getId());
            Log.i(Constants.TAG, "MockClass.newContactList:  " + newContactList.get(i).getFirstName());
            Log.i(Constants.TAG, "MockClass.newContactList:  " + newContactList.get(i).getLastName());
        }

        realmContactTransactions.deleteAllFavouriteContacts();
        newFavouriteContactList = realmContactTransactions.getAllFavouriteContacts();
        if (newFavouriteContactList.size() == 0){
            Log.i(Constants.TAG, "newFavouriteContactList.DELETED");
        }
        for (int i=0;i<newFavouriteContactList.size();i++){
            Log.i(Constants.TAG, "MockClass.newFavouriteContactList:  " + newFavouriteContactList.get(i).getId());
            Log.i(Constants.TAG, "MockClass.newFavouriteContactList:  " + newFavouriteContactList.get(i).getFirstName());
            Log.i(Constants.TAG, "MockClass.newFavouriteContactList:  " + newFavouriteContactList.get(i).getLastName());
        }

        realmContactTransactions.deleteAllRecentContacts();
        newRecentContactList = realmContactTransactions.getAllRecentContacts();
        if (newRecentContactList.size() == 0){
            Log.i(Constants.TAG, "newRecentContactList.DELETED");
        }
        for (int i=0;i<newRecentContactList.size();i++){
            Log.i(Constants.TAG, "MockClass.newRecentContactList:  " + newRecentContactList.get(i).getId());
            Log.i(Constants.TAG, "MockClass.newRecentContactList:  " + newRecentContactList.get(i).getFirstName());
            Log.i(Constants.TAG, "MockClass.newRecentContactList:  " + newRecentContactList.get(i).getLastName());
        }
    }
}
