package com.vodafone.mycomms.realm;

import android.content.Context;
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
    private Context mContext;

    public RealmContactTransactions(Realm realm, Context context) {
        mRealm = realm;
        mContext = context;
    }

    public void insertContact (Contact newContact){
        try {
            mRealm.beginTransaction();
            Contact contact = mRealm.createObject(Contact.class);
            contact.setId("XXX");
            contact.setFirstName(newContact.getFirstName());
            contact.setLastName(newContact.getLastName());
            contact.setPlatform(newContact.getPlatform());
            contact.setAvatar(newContact.getAvatar());
            contact.setPosition(newContact.getPosition());
            contact.setCompany(newContact.getCompany());
            contact.setTimezone(newContact.getTimezone());
            contact.setLastSeen(newContact.getLastSeen());
            contact.setOfficeLocation(newContact.getOfficeLocation());

            contact.setEmails(newContact.getEmails());
            contact.setPhones(newContact.getPhones());

            mRealm.copyToRealmOrUpdate(contact);

            mRealm.commitTransaction();
        } catch (IllegalArgumentException e){
            Log.e(Constants.TAG, "RealmContactTransactions.insertContact: " + e.toString());
        }
    }

    public void insertContactList (ArrayList<Contact> contactArrayList){
        int size = contactArrayList.size();
        try {
            mRealm.beginTransaction();
            Contact contact;
            for (int i = 0; i < size; i++) {
                contact = mRealm.createObject(Contact.class);
                contact.setId(contactArrayList.get(i).getId());
                contact.setFirstName(contactArrayList.get(i).getFirstName());
                contact.setLastName(contactArrayList.get(i).getLastName());
                contact.setPlatform(contactArrayList.get(i).getPlatform());
                contact.setAvatar(contactArrayList.get(i).getAvatar());
                contact.setPosition(contactArrayList.get(i).getPosition());
                contact.setCompany(contactArrayList.get(i).getCompany());
                contact.setTimezone(contactArrayList.get(i).getTimezone());
                contact.setLastSeen(contactArrayList.get(i).getLastSeen());
                contact.setOfficeLocation(contactArrayList.get(i).getOfficeLocation());

                contact.setEmails(contactArrayList.get(i).getEmails());
                contact.setPhones(contactArrayList.get(i).getPhones());

                mRealm.copyToRealmOrUpdate(contact);
            }
            mRealm.commitTransaction();
        } catch (IllegalArgumentException e){
            Log.e(Constants.TAG, "RealmContactTransactions.insertContactList: " + e.toString());
        }
    }

    public void insertFavouriteContact (FavouriteContact newContact){
        try {
            mRealm.beginTransaction();
            FavouriteContact contact = mRealm.createObject(FavouriteContact.class);
            contact.setId("XXX");
            contact.setFirstName(newContact.getFirstName());
            contact.setLastName(newContact.getLastName());
            contact.setPlatform(newContact.getPlatform());
            contact.setAvatar(newContact.getAvatar());
            contact.setPosition(newContact.getPosition());
            contact.setCompany(newContact.getCompany());
            contact.setTimezone(newContact.getTimezone());
            contact.setLastSeen(newContact.getLastSeen());
            contact.setOfficeLocation(newContact.getOfficeLocation());

            contact.setEmails(newContact.getEmails());
            contact.setPhones(newContact.getPhones());

            mRealm.copyToRealmOrUpdate(contact);

            mRealm.commitTransaction();
        } catch (IllegalArgumentException e){
            Log.e(Constants.TAG, "RealmContactTransactions.insertFavouriteContact: " + e.toString());
        }
    }

    public void insertFavouriteContactList (ArrayList<FavouriteContact> contactArrayList){
        int size = contactArrayList.size();
        try {
            mRealm.beginTransaction();
            FavouriteContact contact;
            for (int i = 0; i < size; i++) {
                contact = mRealm.createObject(FavouriteContact.class);
                contact.setId(contactArrayList.get(i).getId());
                contact.setFirstName(contactArrayList.get(i).getFirstName());
                contact.setLastName(contactArrayList.get(i).getLastName());
                contact.setPlatform(contactArrayList.get(i).getPlatform());
                contact.setAvatar(contactArrayList.get(i).getAvatar());
                contact.setPosition(contactArrayList.get(i).getPosition());
                contact.setCompany(contactArrayList.get(i).getCompany());
                contact.setTimezone(contactArrayList.get(i).getTimezone());
                contact.setLastSeen(contactArrayList.get(i).getLastSeen());
                contact.setOfficeLocation(contactArrayList.get(i).getOfficeLocation());

                contact.setEmails(contactArrayList.get(i).getEmails());
                contact.setPhones(contactArrayList.get(i).getPhones());

                mRealm.copyToRealmOrUpdate(contact);
            }
            mRealm.commitTransaction();
        } catch (IllegalArgumentException e){
            Log.e(Constants.TAG, "RealmContactTransactions.insertFavouriteContactList: " + e.toString());
        }
    }

    public void insertRecentContact (RecentContact newContact){
        try {
            mRealm.beginTransaction();
            RecentContact contact = mRealm.createObject(RecentContact.class);
            contact.setId("XXX");
            contact.setFirstName(newContact.getFirstName());
            contact.setLastName(newContact.getLastName());
            contact.setPlatform(newContact.getPlatform());
            contact.setAvatar(newContact.getAvatar());
            contact.setPosition(newContact.getPosition());
            contact.setCompany(newContact.getCompany());
            contact.setTimezone(newContact.getTimezone());
            contact.setLastSeen(newContact.getLastSeen());
            contact.setOfficeLocation(newContact.getOfficeLocation());

            contact.setEmails(newContact.getEmails());
            contact.setPhones(newContact.getPhones());

            mRealm.copyToRealmOrUpdate(contact);

            mRealm.commitTransaction();
        } catch (IllegalArgumentException e){
            Log.e(Constants.TAG, "RealmContactTransactions.insertRecentContact: " + e.toString());
        }
    }

    public void insertRecentContactList (ArrayList<RecentContact> contactArrayList){
        int size = contactArrayList.size();
        try {
            mRealm.beginTransaction();
            RecentContact contact;
            for (int i = 0; i < size; i++) {
                contact = mRealm.createObject(RecentContact.class);
                contact.setId(contactArrayList.get(i).getId());
                contact.setFirstName(contactArrayList.get(i).getFirstName());
                contact.setLastName(contactArrayList.get(i).getLastName());
                contact.setPlatform(contactArrayList.get(i).getPlatform());
                contact.setAvatar(contactArrayList.get(i).getAvatar());
                contact.setPosition(contactArrayList.get(i).getPosition());
                contact.setCompany(contactArrayList.get(i).getCompany());
                contact.setTimezone(contactArrayList.get(i).getTimezone());
                contact.setLastSeen(contactArrayList.get(i).getLastSeen());
                contact.setOfficeLocation(contactArrayList.get(i).getOfficeLocation());

                contact.setEmails(contactArrayList.get(i).getEmails());
                contact.setPhones(contactArrayList.get(i).getPhones());

                mRealm.copyToRealmOrUpdate(contact);
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

    public ArrayList<FavouriteContact> getAllFavouriteContacts(){
        ArrayList<FavouriteContact> contactArrayList = new ArrayList<>();
        RealmQuery<FavouriteContact> query = mRealm.where(FavouriteContact.class);

        RealmResults<FavouriteContact> result1 = query.findAll();
        if (result1!=null){
            for (FavouriteContact contactListItem : result1) {
                contactArrayList.add(contactListItem);
            }
        }
        return contactArrayList;
    }

    public ArrayList<FavouriteContact> getFilteredFavouriteContacts(String field, String filter){
        ArrayList<FavouriteContact> contactArrayList = new ArrayList<>();
        RealmQuery<FavouriteContact> query = mRealm.where(FavouriteContact.class);
        query.equalTo(field, filter);
        // Execute the query:
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
        // Execute the query:
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
        // Execute the query:
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
        ArrayList<Contact> contactArrayList = new ArrayList<>();
        RealmQuery<Contact> query = mRealm.where(Contact.class);
        query.equalTo(field, filter);
        // Execute the query:
        RealmResults<Contact> result1 = query.findAll();
        if (result1 != null) {
            result1.clear();
        }
        mRealm.commitTransaction();
    }

    public void deleteAllContacts() {
        mRealm.beginTransaction();
        ArrayList<Contact> contactArrayList = new ArrayList<>();
        RealmQuery<Contact> query = mRealm.where(Contact.class);
        // Execute the query:
        RealmResults<Contact> result1 = query.findAll();
        if (result1 != null) {
            result1.clear();
        }
        mRealm.commitTransaction();
    }

    public void deleteFavouriteContact(String field, String filter) {
        mRealm.beginTransaction();
        ArrayList<FavouriteContact> contactArrayList = new ArrayList<>();
        RealmQuery<FavouriteContact> query = mRealm.where(FavouriteContact.class);
        query.equalTo(field, filter);
        // Execute the query:
        RealmResults<FavouriteContact> result1 = query.findAll();
        if (result1 != null) {
            result1.clear();
        }
        mRealm.commitTransaction();
    }

    public void deleteAllFavouriteContacts() {
        mRealm.beginTransaction();
        ArrayList<FavouriteContact> contactArrayList = new ArrayList<>();
        RealmQuery<FavouriteContact> query = mRealm.where(FavouriteContact.class);
        // Execute the query:
        RealmResults<FavouriteContact> result1 = query.findAll();
        if (result1 != null) {
            result1.clear();
        }
        mRealm.commitTransaction();
    }

    public void deleteRecentContact(String field, String filter) {
        mRealm.beginTransaction();
        ArrayList<RecentContact> contactArrayList = new ArrayList<>();
        RealmQuery<RecentContact> query = mRealm.where(RecentContact.class);
        query.equalTo(field, filter);
        // Execute the query:
        RealmResults<RecentContact> result1 = query.findAll();
        if (result1 != null) {
            result1.clear();
        }
        mRealm.commitTransaction();
    }

    public void deleteAllRecentContacts() {
        mRealm.beginTransaction();
        ArrayList<RecentContact> contactArrayList = new ArrayList<>();
        RealmQuery<RecentContact> query = mRealm.where(RecentContact.class);
        // Execute the query:
        RealmResults<RecentContact> result1 = query.findAll();
        if (result1 != null) {
            result1.clear();
        }
        mRealm.commitTransaction();
    }

    public void updateContact(String field, String filter, Contact newContact) {
        mRealm.beginTransaction();
        RealmResults<Contact> realmContacts = mRealm.where(Contact.class).equalTo(field, filter).findAll();
        realmContacts.first().setFirstName(newContact.getFirstName());//TODO: This will need to use all Contact information
        mRealm.commitTransaction();
    }

    public void updateFavouriteContact(String field, String filter, FavouriteContact newContact) {
        mRealm.beginTransaction();
        RealmResults<FavouriteContact> realmContacts = mRealm.where(FavouriteContact.class).equalTo(field, filter).findAll();
        realmContacts.first().setFirstName(newContact.getFirstName());//TODO: This will need to use all FavouriteContact information
        mRealm.commitTransaction();
    }

    public void updateRecentContact(String field, String filter, RecentContact newContact) {
        mRealm.beginTransaction();
        RealmResults<RecentContact> realmContacts = mRealm.where(RecentContact.class).equalTo(field, filter).findAll();
        realmContacts.first().setFirstName(newContact.getFirstName());//TODO: This will need to use all RecentContact information
        mRealm.commitTransaction();
    }

}
