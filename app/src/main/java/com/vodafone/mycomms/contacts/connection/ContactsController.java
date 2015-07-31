package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.util.Log;

import com.vodafone.mycomms.realm.RealmAvatarTransactions;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.realm.Realm;
import model.Contact;
import model.FavouriteContact;
import model.RecentContact;

public class ContactsController{

    private static RealmContactTransactions realmContactTransactions;
    private RealmAvatarTransactions realmAvatarTransactions;
    private String mProfileId;
    private Context mContext;

    public ContactsController(String profileId, Context mContext) {
        this.mProfileId = profileId;
        this.mContext = mContext;
        realmContactTransactions = new RealmContactTransactions(mProfileId, mContext);
        realmAvatarTransactions = new RealmAvatarTransactions(mContext);
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
            realmContactTransactions.insertContactList(realmContactList, null);

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);

                if(null != jsonObject.getString(Constants.CONTACT_ID)){
                    contact = realmContactTransactions.getContactById(jsonObject.getString
                            (Constants.CONTACT_ID), realm);
                    if (null != contact){
                        String SF_URL = realmContactTransactions.getContactById(jsonObject
                                .getString(Constants.CONTACT_ID), realm)
                                .getStringField1();
                        if(null != SF_URL)
                            realmContactTransactions.updateSFAvatar(contact, SF_URL, null);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactsController.insertContactListInRealm: " + e.toString());
            return null;
        }
        finally {
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
                contact = realmContactTransactions.getContactById(jsonArray.getString(i), realm);
                if (contact != null) {
                    contactList.add(mapContactToFavourite(contact));
                }
            }
            if (contactList.size()!=0) {
                realmContactTransactions.deleteAllFavouriteContacts(null);
                realmContactTransactions.insertFavouriteContactList(contactList, null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactsController.insertFavouriteContactInRealm : ",e);
        }
        finally {
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
                contact = realmContactTransactions.getContactById(
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            if(contact.getFirstName()!=null && contact.getFirstName().length()>0)
                sortHelper  += Utils.normalizeStringNFD(contact.getFirstName()) + " ";
            if(contact.getLastName()!=null && contact.getLastName().length()>0)
                sortHelper  += Utils.normalizeStringNFD(contact.getLastName()) + " ";
            if(contact.getCompany()!=null && contact.getCompany().length()>0)
                sortHelper  += Utils.normalizeStringNFD(contact.getCompany()) + " ";

            sortHelper = sortHelper.trim();
            contact.setSortHelper(sortHelper);

        }catch (JSONException e){
            e.printStackTrace();
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
}
