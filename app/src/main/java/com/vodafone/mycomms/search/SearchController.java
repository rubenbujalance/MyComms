package com.vodafone.mycomms.search;

import android.content.Context;
import android.util.Log;

import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.InternalContactSearch;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.realm.Realm;
import model.Contact;

/**
 * Created by str_oan on 09/06/2015.
 */
public class SearchController
{
    private RealmContactTransactions realmContactTransactions;
    private InternalContactSearch internalContactSearch;
    private String mProfileId;
    private Realm realm;

    public SearchController(Context context, String profileId, Realm realm) {
        mProfileId = profileId;
        this.realm = realm;
        realmContactTransactions = new RealmContactTransactions(profileId);
        internalContactSearch = new InternalContactSearch(context, profileId);
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
                contact = mapContact(jsonObject, mProfileId, realm);

                realmContactList.add(contact);
            }
            realmContactTransactions.insertContactList(realmContactList, realm);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "SearchController.insertContactListInRealm: " + e.toString());
            realmContactList = null;
        }
        finally {
            realm.close();
        }
        return realmContactList;
    }

    public Contact mapContact(JSONObject jsonObject, String profileId, Realm realm){
        Contact contact = new Contact();
        try {
            contact.setProfileId(profileId);
            if (!jsonObject.isNull(Constants.CONTACT_ID)){
                contact.setContactId(jsonObject.getString(Constants.CONTACT_ID));
                contact.setId(profileId + "_" + jsonObject.getString(Constants.CONTACT_ID));
                Log.w(Constants.TAG,"SearchController -> mapContact() has value: "+profileId);
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
            contact.setSearchHelper
                    ((
                                    Utils.normalizeStringNFD(contact.getFirstName()) + " " +
                                            Utils.normalizeStringNFD(contact.getLastName()) + " " +
                                            Utils.normalizeStringNFD(contact.getCompany()) + " " +
                                            Utils.normalizeStringNFD(contact.getEmails())).trim()
                    );

            contact.setSortHelper
                    ((
                                    contact.getLongField1() + " " +
                                            Utils.normalizeStringNFD(contact.getFirstName()) + " " +
                                            Utils.normalizeStringNFD(contact.getLastName()) + " " +
                                            Utils.normalizeStringNFD(contact.getCompany())).trim()
                    );


            if(null != jsonObject.getString(Constants.CONTACT_ID)
                    && null != realmContactTransactions.getContactById(jsonObject.getString
                    (Constants.CONTACT_ID), realm)
                    && null != jsonObject.getString(Constants.CONTACT_PLATFORM)
                    && Constants.PLATFORM_SALES_FORCE.equals(jsonObject.getString(Constants
                    .CONTACT_PLATFORM)))
            {
                String SF_URL = realmContactTransactions.getContactById(jsonObject.getString
                        (Constants.CONTACT_ID), realm)
                        .getStringField1();
                if(null != SF_URL)
                    contact.setStringField1(SF_URL);
            }

        }catch (JSONException e){
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactDBController.mapContact: " + e.toString());
        }
        return contact;
    }

    public ArrayList<Contact> getLocalContactsByKeyWord(String keyWord) {
        Log.d(Constants.TAG, "SearchController.getLocalContactsByKeyWord: ");
        return internalContactSearch.getLocalContactsByKeyWord(keyWord);
    }

    public ArrayList<Contact> getContactsByKeyWord(String keyWord) {
        Log.d(Constants.TAG, "SearchController.getContactsByKeyWord: ");
        return realmContactTransactions.getContactsByKeyWord(keyWord, realm);
    }

    public ArrayList<Contact> getContactsByKeyWordWithoutLocalsAndSalesForce(
            String keyWord) {
        Log.d(Constants.TAG, "SearchController.getContactsByKeyWord: ");
        return realmContactTransactions
                .getContactsByKeyWordWithoutLocalsAndSalesForce(keyWord, realm);
    }

    public void storeContactsIntoRealm(ArrayList<Contact> contacts)
    {
        Log.d(Constants.TAG, "SearchController.storeContactsIntoRealm: ");
        realmContactTransactions.insertContactList(contacts, null);
    }

    public InternalContactSearch getInternalContactSearch()
    {
        return this.internalContactSearch;
    }

}
