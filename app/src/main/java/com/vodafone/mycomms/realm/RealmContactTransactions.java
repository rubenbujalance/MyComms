package com.vodafone.mycomms.realm;

import android.util.Log;

import com.vodafone.mycomms.util.Constants;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import model.Contact;
import model.FavouriteContact;
import model.RecentContact;
import model.UserProfile;

public class RealmContactTransactions {
    private Realm mRealm;
    private String mProfileId;

    public RealmContactTransactions(Realm realm, String profileId) {
        mRealm = realm;
        mProfileId = profileId;
    }

    public void insertUserProfile(UserProfile userProfile){
        Log.d(Constants.TAG, "RealmContactTransactions.insertUserProfile: ");
        try {
            mRealm.beginTransaction();
            mRealm.copyToRealmOrUpdate(userProfile);

            mRealm.commitTransaction();
        } catch (IllegalArgumentException e){
            Log.e(Constants.TAG, "RealmContactTransactions.insertContact: " + e.toString());
        }
    }

    public void insertContactList (ArrayList<Contact> contactArrayList){
        int size = contactArrayList.size();
        try {
            mRealm.beginTransaction();
            for (int i = 0; i < size; i++) {
                mRealm.copyToRealmOrUpdate(contactArrayList.get(i));
            }
            mRealm.commitTransaction();
        } catch (IllegalArgumentException e){
            e.printStackTrace();
            Log.e(Constants.TAG, "RealmContactTransactions.insertContactList: " + e.toString());
        }
    }

    public void insertFavouriteContactList (ArrayList<FavouriteContact> contactArrayList){
        int size = contactArrayList.size();
        try {
            mRealm.beginTransaction();
            for (int i = 0; i < size; i++) {
                mRealm.copyToRealmOrUpdate(contactArrayList.get(i));
            }
            mRealm.commitTransaction();
        } catch (IllegalArgumentException e){
            Log.e(Constants.TAG, "RealmContactTransactions.insertFavouriteContactList: " + e.toString());
        }
    }

    public void insertRecentContactList (ArrayList<RecentContact> contactArrayList){
        int size = contactArrayList.size();
        try {
            mRealm.beginTransaction();
            for (int i = 0; i < size; i++) {
                mRealm.copyToRealmOrUpdate(contactArrayList.get(i));
            }
            mRealm.commitTransaction();
        } catch (IllegalArgumentException e){
            Log.e(Constants.TAG, "RealmContactTransactions.insertRecentContactList: " + e.toString());
        }
    }

    public ArrayList<Contact> getAllContacts(){
        ArrayList<Contact> contactArrayList = new ArrayList<>();
        RealmQuery<Contact> query = mRealm.where(Contact.class);
        query.equalTo(Constants.CONTACT_PROFILE_ID, mProfileId);
        RealmResults<Contact> result1 = query.findAll();

        if (result1!=null){
            result1.sort(Constants.CONTACT_FNAME, RealmResults.SORT_ORDER_ASCENDING);
            for (Contact contactListItem : result1) {
                contactArrayList.add(contactListItem);
            }
        }
        return contactArrayList;
    }

    public ArrayList<Contact> getFilteredContacts(String field, String filter){
        ArrayList<Contact> contactArrayList = new ArrayList<>();
        RealmQuery<Contact> query = mRealm.where(Contact.class);
        query.equalTo(field, filter)
             .equalTo(Constants.CONTACT_PROFILE_ID, mProfileId);
        RealmResults<Contact> result1 = query.findAll();

        if (result1!=null){
            for (Contact contactListItem : result1) {
                contactArrayList.add(contactListItem);
            }
        }
        return contactArrayList;
    }

    public Contact getContactById(String contactId){
        RealmQuery<Contact> query = mRealm.where(Contact.class);
        query.equalTo(Constants.CONTACT_CONTACT_ID, contactId)
             .equalTo(Constants.CONTACT_PROFILE_ID, mProfileId);
        RealmResults<Contact> result1 = query.findAll();

        if (result1!=null && result1.size()>0){
            return result1.first();
        }else {
            return null;
        }
    }

    public UserProfile getUserProfile(String contactId){
        try {
            RealmQuery<UserProfile> query = mRealm.where(UserProfile.class);
            query.equalTo(Constants.CONTACT_CONTACT_ID, contactId);
                 //.equalTo(Constants.CONTACT_PROFILE_ID, mProfileId);
            RealmResults<UserProfile> result1 = query.findAll();

            if (result1 != null) {
                return result1.first();
            } else {
                return null;
            }
        }catch (Exception e ){
            Log.e(Constants.TAG, "RealmContactTransactions.getUserProfile: " , e);
            return  null;
        }
    }

    public boolean favouriteContactIsInRealm(String contactId){
        //Checks if the Contact is favourite. If it is, it deletes it and returns false to send it to the API
        Log.i(Constants.TAG, "RealmContactTransactions.favouriteContactIsInRealm: ");
        mRealm.beginTransaction();
        RealmQuery<FavouriteContact> query = mRealm.where(FavouriteContact.class);
        query.equalTo(Constants.CONTACT_CONTACT_ID, contactId)
             .equalTo(Constants.CONTACT_PROFILE_ID, mProfileId);
        RealmResults<FavouriteContact> result1 = query.findAll();

        if (result1!=null && result1.size()!=0){
            result1.clear();
            mRealm.commitTransaction();
            return true;
        }else {
            mRealm.cancelTransaction();
            return false;
        }
    }

    public ArrayList<FavouriteContact> getAllFavouriteContacts(){
        Log.i(Constants.TAG, "RealmContactTransactions.getAllFavouriteContacts: ");
        ArrayList<FavouriteContact> contactArrayList = new ArrayList<>();
        RealmQuery<FavouriteContact> query = mRealm.where(FavouriteContact.class);
        query.equalTo(Constants.CONTACT_PROFILE_ID, mProfileId);
        RealmResults<FavouriteContact> result1 = query.findAll();

        if (result1 != null){
            result1.sort(Constants.CONTACT_FNAME, RealmResults.SORT_ORDER_ASCENDING);
            for (FavouriteContact contactListItem : result1) {
                contactArrayList.add(contactListItem);
            }
            return contactArrayList;
        }
        return null;
    }

    public ArrayList<RecentContact> getAllRecentContacts(){
        ArrayList<RecentContact> contactArrayList = new ArrayList<>();
        RealmQuery<RecentContact> query = mRealm.where(RecentContact.class);
        query.equalTo(Constants.CONTACT_PROFILE_ID, mProfileId);
        RealmResults<RecentContact> result1 = query.findAll();

        if (result1!=null){
            result1.sort(Constants.CONTACT_RECENTS_ACTION_TIME); // Sort ascending
            result1.sort(Constants.CONTACT_RECENTS_ACTION_TIME, RealmResults.SORT_ORDER_DESCENDING);
            for (RecentContact contactListItem : result1) {
                contactArrayList.add(contactListItem);
            }
        }
        return contactArrayList;
    }

    public void deleteAllFavouriteContacts() {
        Log.i(Constants.TAG, "RealmContactTransactions.deleteAllFavouriteContacts: ");
        mRealm.beginTransaction();
        RealmQuery<FavouriteContact> query = mRealm.where(FavouriteContact.class);
        RealmResults<FavouriteContact> result1 = query.findAll();
        query.equalTo(Constants.CONTACT_PROFILE_ID, mProfileId);

        if (result1 != null) {
            result1.clear();
        }
        mRealm.commitTransaction();
    }

    public void updateContact(Contact updatedContact) {
        mRealm.beginTransaction();
        mRealm.copyToRealmOrUpdate(updatedContact);
        mRealm.commitTransaction();
    }

}

