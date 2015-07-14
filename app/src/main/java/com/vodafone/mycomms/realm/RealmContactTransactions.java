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

    public RealmContactTransactions(String profileId) {
        mRealm = Realm.getDefaultInstance();
        mProfileId = profileId;
    }

    public void insertContactList (ArrayList<Contact> contactArrayList){
        int size = contactArrayList.size();
        try {
            mRealm.beginTransaction();
            for (int i = 0; i < size; i++)
            {
                if(!contactArrayList.get(i).getContactId().equals(mProfileId))
                {
                    mRealm.copyToRealmOrUpdate(contactArrayList.get(i));
                }
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
            for (int i = 0; i < size; i++)
            {
                if(!contactArrayList.get(i).getContactId().equals(mProfileId))
                {
                    mRealm.copyToRealmOrUpdate(contactArrayList.get(i));
                }

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
            for (int i = 0; i < size; i++)
            {
                if(!contactArrayList.get(i).getContactId().equals(mProfileId))
                {
                    mRealm.copyToRealmOrUpdate(contactArrayList.get(i));
                }
            }
            mRealm.commitTransaction();
        } catch (IllegalArgumentException e){
            Log.e(Constants.TAG, "RealmContactTransactions.insertRecentContactList: " + e.toString());
        }
    }

    public ArrayList<Contact> getAllContacts(){
        ArrayList<Contact> contactArrayList = new ArrayList<>();
        RealmQuery<Contact> query = mRealm.where(Contact.class).not().equalTo(Constants
                .CONTACT_PLATFORM,Constants.PLATFORM_LOCAL);
        query.equalTo(Constants.CONTACT_PROFILE_ID, mProfileId);
        RealmResults<Contact> result1 = query.findAll();

        if (result1!=null){
            result1.sort(Constants.CONTACT_FNAME, RealmResults.SORT_ORDER_ASCENDING);
            for (Contact contactListItem : result1) {
                contactArrayList.add(contactListItem);
            }
        }
        return sortContacts(contactArrayList);
    }

    public ArrayList<Contact> sortContacts(ArrayList<Contact> contactsArrayList){
        int size = contactsArrayList.size();
        int notLettersStart = 0;
        ArrayList<Contact> noLettersContactsArrayList = new ArrayList<>();
        ArrayList<Contact> lettersContactsArrayList = new ArrayList<>();
        for (int i=0; i < size; i++){
            Character.isLetter(contactsArrayList.get(i).getFirstName().charAt(0));
            if (Character.isLetter(contactsArrayList.get(i).getFirstName().charAt(0))){
                lettersContactsArrayList.add(contactsArrayList.get(i));
            }else{
                notLettersStart = notLettersStart + 1;
                noLettersContactsArrayList.add(contactsArrayList.get(i));
            }
        }
        int notLetters = 0;
        ArrayList<Contact> sortedContactsArrayList = new ArrayList<>();
        for (int i=0; i < size; i++){
            if (i<lettersContactsArrayList.size()){
                sortedContactsArrayList.add(lettersContactsArrayList.get(i));
            }else{
                sortedContactsArrayList.add(noLettersContactsArrayList.get(notLetters));
                notLetters = notLetters+1;
            }
        }
        return sortedContactsArrayList;

    }

    public ArrayList<Contact> getContactsByKeyWord(String keyWord)
    {
        if(keyWord==null || keyWord.length()<=0) return null;

        ArrayList<Contact> contactArrayList = new ArrayList<>();
        String[] keywordSplit = keyWord.split(" ");

        RealmQuery<Contact> query = mRealm.where(Contact.class)
                .equalTo(Constants.CONTACT_PROFILE_ID, mProfileId);

        for(int i=0; i<keywordSplit.length; i++)
        {
            query = query.contains(Constants.CONTACT_SEARCH_HELPER, keywordSplit[i], false);
        }

        RealmResults<Contact> result = query.findAll();

        if (result!=null){
            result.sort(Constants.CONTACT_SORT_HELPER, RealmResults.SORT_ORDER_ASCENDING);
            for (Contact contactListItem : result)
            {
                contactArrayList.add(contactListItem);
            }
        }
        return contactArrayList;
    }

    public ArrayList<Contact> getContactsByKeyWordWithoutLocalsAndSalesForce(String keyWord)
    {
        if(keyWord==null || keyWord.length()<=0) return null;

        ArrayList<Contact> contactArrayList = new ArrayList<>();
        String[] keywordSplit = keyWord.split(" ");

        RealmQuery<Contact> query = mRealm.where(Contact.class)
                .equalTo(Constants.CONTACT_PROFILE_ID, mProfileId)
                .not().equalTo(Constants.CONTACT_PLATFORM, Constants.PLATFORM_LOCAL)
                .not().equalTo(Constants.CONTACT_PLATFORM, Constants.PLATFORM_SALES_FORCE);

        for(String key : keywordSplit)
        {
            query = query.contains(Constants.CONTACT_SEARCH_HELPER, key, false);
        }

        RealmResults<Contact> result = query.findAll();

        if (result!=null){
            result.sort(Constants.CONTACT_SORT_HELPER, RealmResults.SORT_ORDER_ASCENDING);
            for (Contact contactListItem : result)
            {
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

    public boolean isAnyContactSaved()
    {
        RealmQuery<Contact> query = mRealm.where(Contact.class)
                .equalTo(Constants.CONTACT_PLATFORM,Constants.PLATFORM_MY_COMMS)
                .equalTo(Constants.CONTACT_PROFILE_ID, mProfileId);

        Contact result = query.findFirst();

        if (result==null) return false;
        else return true;
    }

    public UserProfile getUserProfile(){
        try {
            RealmQuery<UserProfile> query = mRealm.where(UserProfile.class);
            query.equalTo(Constants.CONTACT_ID, mProfileId);
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

    public boolean deleteFavouriteContact(String contactId){
        //Checks if the Contact is favourite. If it is, it deletes it and returns false to send it to the API
        Log.i(Constants.TAG, "RealmContactTransactions.deleteFavouriteContact: ");
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

    public boolean favouriteContactIsInRealm(String contactId){
        //Checks if the Contact is favourite. If it is, it deletes it and returns false to send it to the API
        Log.i(Constants.TAG, "RealmContactTransactions.favouriteContactIsInRealm: ");
        RealmQuery<FavouriteContact> query = mRealm.where(FavouriteContact.class);
        query.equalTo(Constants.CONTACT_CONTACT_ID, contactId)
             .equalTo(Constants.CONTACT_PROFILE_ID, mProfileId);
        RealmResults<FavouriteContact> result1 = query.findAll();

        if (result1!=null && result1.size()!=0){
            return true;
        }else {
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
            return sortFavouriteContacts(contactArrayList);
        }
        return null;
    }

    public ArrayList<FavouriteContact> sortFavouriteContacts(ArrayList<FavouriteContact> favouriteContactsArrayList){
        int size = favouriteContactsArrayList.size();
        int notLettersStart = 0;
        ArrayList<FavouriteContact> noLettersFavouriteContactsArrayList = new ArrayList<>();
        ArrayList<FavouriteContact> lettersFavouriteContactsArrayList = new ArrayList<>();
        for (int i=0; i < size; i++){
            Character.isLetter(favouriteContactsArrayList.get(i).getFirstName().charAt(0));
            if (Character.isLetter(favouriteContactsArrayList.get(i).getFirstName().charAt(0))){
                lettersFavouriteContactsArrayList.add(favouriteContactsArrayList.get(i));
            }else{
                notLettersStart = notLettersStart + 1;
                noLettersFavouriteContactsArrayList.add(favouriteContactsArrayList.get(i));
            }
        }
        int notLetters = 0;
        ArrayList<FavouriteContact> sortedFavouriteContactsArrayList = new ArrayList<>();
        for (int i=0; i < size; i++){
            if (i<lettersFavouriteContactsArrayList.size()){
                sortedFavouriteContactsArrayList.add(lettersFavouriteContactsArrayList.get(i));
            }else{
                sortedFavouriteContactsArrayList.add(noLettersFavouriteContactsArrayList.get(notLetters));
                notLetters = notLetters+1;
            }
        }
        return sortedFavouriteContactsArrayList;

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

    public void closeRealm() {if(mRealm!=null) mRealm.close();}

}

