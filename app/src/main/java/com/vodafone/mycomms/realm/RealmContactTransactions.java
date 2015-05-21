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

/**
 * Created by AMG on 12/05/2015.
 */
public class RealmContactTransactions {
    private Realm mRealm;

    public RealmContactTransactions(Realm realm) {
        mRealm = realm;
    }

    public void insertContact (Contact newContact){
        try {
            mRealm.beginTransaction();
            mRealm.copyToRealmOrUpdate(newContact);

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

    public void insertFavouriteContact (FavouriteContact newContact){
        try {
            mRealm.beginTransaction();
            mRealm.copyToRealmOrUpdate(newContact);

            mRealm.commitTransaction();
        } catch (IllegalArgumentException e){
            Log.e(Constants.TAG, "RealmContactTransactions.insertFavouriteContact: " + e.toString());
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

    public void insertRecentContact (RecentContact newContact){
        try {
            mRealm.beginTransaction();
            mRealm.copyToRealm(newContact);

            mRealm.commitTransaction();
        } catch (IllegalArgumentException e){
            Log.e(Constants.TAG, "RealmContactTransactions.insertRecentContact: " + e.toString());
        }
    }

    public void insertRecentContactList (ArrayList<RecentContact> contactArrayList){
        int size = contactArrayList.size();
        try {
            mRealm.beginTransaction();
            for (int i = 0; i < size; i++) {
                mRealm.copyToRealm(contactArrayList.get(i));
            }
            mRealm.commitTransaction();
        } catch (IllegalArgumentException e){
            Log.e(Constants.TAG, "RealmContactTransactions.insertRecentContactList: " + e.toString());
        }
    }

    public ArrayList<Contact> getAllContacts(){
        ArrayList<Contact> contactArrayList = new ArrayList<>();
        RealmQuery<Contact> query = mRealm.where(Contact.class);

        RealmResults<Contact> result1 = query.findAll();
        if (result1!=null){
            for (Contact contactListItem : result1) {
                contactArrayList.add(contactListItem);
            }
        }
        return contactArrayList;
    }

    public ArrayList<Contact> getFilteredContacts(String field, String filter){
        ArrayList<Contact> contactArrayList = new ArrayList<>();
        RealmQuery<Contact> query = mRealm.where(Contact.class);
        query.equalTo(field, filter);

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
        query.equalTo(Constants.CONTACT_ID, contactId);

        RealmResults<Contact> result1 = query.findAll();

        if (result1!=null){
            return result1.first();
        }else {
            return null;
        }
    }

    public ArrayList<FavouriteContact> getAllFavouriteContacts(){
        ArrayList<FavouriteContact> contactArrayList = new ArrayList<>();
        RealmQuery<FavouriteContact> query = mRealm.where(FavouriteContact.class);
        RealmResults<FavouriteContact> result1 = query.findAll();
        if (result1 != null){
            //result1.sort(Constants.CONTACT_FNAME); // Sort ascending
            result1.sort(Constants.CONTACT_FNAME, RealmResults.SORT_ORDER_ASCENDING);
            for (FavouriteContact contactListItem : result1) {
                contactArrayList.add(contactListItem);
            }
            return contactArrayList;
        }
        return null;
    }

    public ArrayList<FavouriteContact> getFilteredFavouriteContacts(String field, String filter){
        ArrayList<FavouriteContact> contactArrayList = new ArrayList<>();
        RealmQuery<FavouriteContact> query = mRealm.where(FavouriteContact.class);
        query.equalTo(field, filter);
        RealmResults<FavouriteContact> result1 = query.findAll();
        if (result1!=null){
            for (FavouriteContact contactListItem : result1) {
                contactArrayList.add(contactListItem);
            }
        }
        return contactArrayList;
    }

    public ArrayList<RecentContact> getAllRecentContacts(){
        ArrayList<RecentContact> contactArrayList = new ArrayList<>();
        RealmQuery<RecentContact> query = mRealm.where(RecentContact.class);
        RealmResults<RecentContact> result1 = query.findAll();
        if (result1!=null){
            result1.sort("lastInteraction"); // Sort ascending
            result1.sort("lastInteraction", RealmResults.SORT_ORDER_ASCENDING);
            for (RecentContact contactListItem : result1) {
                contactArrayList.add(contactListItem);
            }
        }
        return contactArrayList;
    }

    public ArrayList<RecentContact> getFilteredRecentContacts(String field, String filter){
        ArrayList<RecentContact> contactArrayList = new ArrayList<>();
        RealmQuery<RecentContact> query = mRealm.where(RecentContact.class);
        query.equalTo(field, filter);
        RealmResults<RecentContact> result1 = query.findAll();
        if (result1!=null){
            result1.sort("lastInteraction"); // Sort ascending
            result1.sort("lastInteraction", RealmResults.SORT_ORDER_ASCENDING);
            for (RecentContact contactListItem : result1) {
                contactArrayList.add(contactListItem);
            }
        }
        return contactArrayList;
    }

    public void deleteContact(String field, String filter) {
        mRealm.beginTransaction();
        RealmQuery<Contact> query = mRealm.where(Contact.class);
        query.equalTo(field, filter);
        RealmResults<Contact> result1 = query.findAll();
        if (result1 != null) {
            result1.clear();
        }
        mRealm.commitTransaction();
    }

    public void deleteAllContacts() {
        mRealm.beginTransaction();
        RealmQuery<Contact> query = mRealm.where(Contact.class);
        RealmResults<Contact> result1 = query.findAll();
        if (result1 != null) {
            result1.clear();
        }
        mRealm.commitTransaction();
    }

    public void deleteFavouriteContact(String field, String filter) {
        mRealm.beginTransaction();
        RealmQuery<FavouriteContact> query = mRealm.where(FavouriteContact.class);
        query.equalTo(field, filter);
        RealmResults<FavouriteContact> result1 = query.findAll();
        if (result1 != null) {
            result1.clear();
        }
        mRealm.commitTransaction();
    }

    public void deleteAllFavouriteContacts() {
        Log.i(Constants.TAG, "RealmContactTransactions.deleteAllFavouriteContacts: ");
        mRealm.beginTransaction();
        RealmQuery<FavouriteContact> query = mRealm.where(FavouriteContact.class);
        RealmResults<FavouriteContact> result1 = query.findAll();
        if (result1 != null) {
            result1.clear();
        }
        mRealm.commitTransaction();
    }

    public void deleteRecentContact(String field, String filter) {
        mRealm.beginTransaction();
        RealmQuery<RecentContact> query = mRealm.where(RecentContact.class);
        query.equalTo(field, filter);
        RealmResults<RecentContact> result1 = query.findAll();
        if (result1 != null) {
            result1.clear();
        }
        mRealm.commitTransaction();
    }

    public void deleteAllRecentContacts() {
        mRealm.beginTransaction();
        RealmQuery<RecentContact> query = mRealm.where(RecentContact.class);
        RealmResults<RecentContact> result1 = query.findAll();
        if (result1 != null) {
            result1.clear();
        }
        mRealm.commitTransaction();
    }

    public void updateContact(String field, String filter, Contact newContact) {
        mRealm.beginTransaction();
        RealmResults<Contact> realmContacts = mRealm.where(Contact.class).equalTo(field, filter).findAll();
        realmContacts.first().setFirstName(newContact.getFirstName());
        mRealm.commitTransaction();
    }

    public void updateContact(Contact updatedContact) {
        mRealm.beginTransaction();
        mRealm.copyToRealmOrUpdate(updatedContact);
        mRealm.commitTransaction();
    }

    public void updateFavouriteContact(String field, String filter, FavouriteContact newContact) {
        mRealm.beginTransaction();
        RealmResults<FavouriteContact> realmContacts = mRealm.where(FavouriteContact.class).equalTo(field, filter).findAll();
        realmContacts.first().setFirstName(newContact.getFirstName());
        mRealm.commitTransaction();
    }

    public void updateFavouriteContact(FavouriteContact updatedContact) {
        mRealm.beginTransaction();
        mRealm.copyToRealmOrUpdate(updatedContact);
        mRealm.commitTransaction();
    }

    public void updateRecentContact(String field, String filter, RecentContact newContact) {
        mRealm.beginTransaction();
        RealmResults<RecentContact> realmContacts = mRealm.where(RecentContact.class).equalTo(field, filter).findAll();
        realmContacts.first().setFirstName(newContact.getFirstName());
        mRealm.commitTransaction();
    }

    public void updateRecentContact(RecentContact updatedContact) {
        mRealm.beginTransaction();
        mRealm.copyToRealmOrUpdate(updatedContact);
        mRealm.commitTransaction();
    }
}
