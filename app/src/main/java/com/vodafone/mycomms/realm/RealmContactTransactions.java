package com.vodafone.mycomms.realm;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
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
    private String mProfileId;

    public RealmContactTransactions(String profileId)
    {
        mProfileId = profileId;
    }

    public void insertContactList (ArrayList<Contact> contactArrayList, Realm realm){
        int size = contactArrayList.size();

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            mRealm.beginTransaction();
            for (int i = 0; i < size; i++)
            {
                if(!contactArrayList.get(i).getContactId().equals(mProfileId))
                {
                    mRealm.copyToRealmOrUpdate(contactArrayList.get(i));
                }
            }
            mRealm.commitTransaction();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmContactTransactions.insertContactList: ",e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }

    }

    public void insertFavouriteContactList (ArrayList<FavouriteContact> contactArrayList, Realm
            realm){
        int size = contactArrayList.size();
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            mRealm.beginTransaction();
            for (int i = 0; i < size; i++)
            {
                if(!contactArrayList.get(i).getContactId().equals(mProfileId))
                {
                    mRealm.copyToRealmOrUpdate(contactArrayList.get(i));
                }

            }
            mRealm.commitTransaction();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmContactTransactions.insertFavouriteContactList: ",e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public void insertRecentContactList (ArrayList<RecentContact> contactArrayList, Realm realm){
        int size = contactArrayList.size();
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            mRealm.beginTransaction();
            for (int i = 0; i < size; i++)
            {
                if(!contactArrayList.get(i).getContactId().equals(mProfileId))
                {
                    mRealm.copyToRealmOrUpdate(contactArrayList.get(i));
                }
            }
            mRealm.commitTransaction();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmContactTransactions.insertRecentContactList: ",e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public ArrayList<Contact> getAllContacts(Realm realm)
    {
        ArrayList<Contact> contactArrayList = new ArrayList<>();

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            RealmQuery<Contact> query = mRealm.where(Contact.class)
                    .equalTo(Constants.CONTACT_PLATFORM,Constants.PLATFORM_MY_COMMS);
            query.equalTo(Constants.CONTACT_PROFILE_ID, mProfileId);
            RealmResults<Contact> result1 = query.findAll();

            if (result1!=null){
                result1.sort(Constants.CONTACT_SORT_HELPER, RealmResults.SORT_ORDER_ASCENDING);
                for (Contact contactListItem : result1) {
                    contactArrayList.add(contactListItem);
                }
            }

            return sortContacts(contactArrayList);
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmContactTransactions.getAllContacts: ", e);
            Crashlytics.logException(e);
            return null;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
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

    public ArrayList<Contact> getContactsByKeyWord(String keyWord, Realm realm)
    {
        if(keyWord==null || keyWord.length()<=0) return null;

        ArrayList<Contact> contactArrayList = new ArrayList<>();

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
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
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmContactTransactions.getContactsByKeyWord: ", e);
            Crashlytics.logException(e);
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
        return contactArrayList;
    }

    public ArrayList<Contact> getContactsByKeyWordWithoutLocalsAndSalesForce(String keyWord,
                                                                             Realm realm)
    {
        if(keyWord==null || keyWord.length()<=0) return null;

        ArrayList<Contact> contactArrayList = new ArrayList<>();

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
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
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmContactTransactions.getContactsByKeyWordWithoutLocalsAndSalesForce: ", e);
            Crashlytics.logException(e);
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
        return contactArrayList;
    }

    public ArrayList<Contact> getFilteredContacts(String field, String filter, Realm realm)
    {
        ArrayList<Contact> contactArrayList = new ArrayList<>();

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            RealmQuery<Contact> query = mRealm.where(Contact.class);
            query.equalTo(field, filter)
                    .equalTo(Constants.CONTACT_PROFILE_ID, mProfileId);
            RealmResults<Contact> result1 = query.findAll();

            if (result1!=null){
                for (Contact contactListItem : result1) {
                    contactArrayList.add(contactListItem);
                }
            }
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmContactTransactions.getFilteredContacts: ",e);
            Crashlytics.logException(e);
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }

        return contactArrayList;
    }

    public Contact getContactById(String contactId, Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
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
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmContactTransactions.getContactById: ", e);
            Crashlytics.logException(e);
            return null;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public boolean isAnyContactSaved(Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            RealmQuery<Contact> query = mRealm.where(Contact.class)
                    .equalTo(Constants.CONTACT_PLATFORM,Constants.PLATFORM_MY_COMMS)
                    .equalTo(Constants.CONTACT_PROFILE_ID, mProfileId);

            Contact result = query.findFirst();

            if (result==null) return false;
            else return true;
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmContactTransactions.isAnyContactSaved: ", e);
            Crashlytics.logException(e);
            return false;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public UserProfile getUserProfile(Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            RealmQuery<UserProfile> query = mRealm.where(UserProfile.class);
            query.equalTo(Constants.CONTACT_ID, mProfileId);
            //.equalTo(Constants.CONTACT_PROFILE_ID, mProfileId);
            RealmResults<UserProfile> result1 = query.findAll();

            if (result1 != null) {
                return result1.first();
            } else {
                return null;
            }
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmContactTransactions.getUserProfile: ", e);
            Crashlytics.logException(e);
            return null;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public boolean deleteFavouriteContact(String contactId, Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
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
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmContactTransactions.deleteFavouriteContact: ", e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
            return false;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public boolean favouriteContactIsInRealm(String contactId, Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
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
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmContactTransactions.favouriteContactIsInRealm: ", e);
            Crashlytics.logException(e);
            return false;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public ArrayList<FavouriteContact> getAllFavouriteContacts(Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
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
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmContactTransactions.getAllFavouriteContacts: ", e);
            Crashlytics.logException(e);
            return null;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public FavouriteContact getFavouriteContactByContactId(String contactId, Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            Log.i(Constants.TAG, "RealmContactTransactions.getFavouriteContactByContactId: ");
            RealmQuery<FavouriteContact> query = mRealm.where(FavouriteContact.class);
            query.equalTo(Constants.CONTACT_PROFILE_ID, mProfileId);
            query.equalTo(Constants.CONTACT_CONTACT_ID, contactId);
            FavouriteContact favouriteContact = query.findFirst();

            return favouriteContact;
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmContactTransactions.getFavouriteContactByContactId: ", e);
            Crashlytics.logException(e);
            return null;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
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

    public ArrayList<RecentContact> getAllRecentContacts(Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
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
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmContactTransactions.getAllRecentContacts: ", e);
            Crashlytics.logException(e);
            return null;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }

    }

    public RecentContact getRecentContactByContactId(String contactId, Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            RealmQuery<RecentContact> query = mRealm.where(RecentContact.class);
            query.equalTo(Constants.CONTACT_CONTACT_ID, contactId);
            query.equalTo(Constants.CONTACT_PROFILE_ID, mProfileId);
            RecentContact recentContact = query.findFirst();
            return recentContact;
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmContactTransactions.getRecentContactByContactId: ",e);
            Crashlytics.logException(e);
            return null;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public void deleteAllFavouriteContacts(Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
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
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmContactTransactions.deleteAllFavouriteContacts: ",e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public void updateContact(Contact updatedContact, Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            mRealm.beginTransaction();
            mRealm.copyToRealmOrUpdate(updatedContact);
            mRealm.commitTransaction();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmContactTransactions.updateContact: ",e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public void updateSFAvatar(Contact contact, String SF_URL, Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            mRealm.beginTransaction();
            contact.setStringField1(SF_URL);
            mRealm.commitTransaction();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmContactTransactions.updateSFAvatar: ",e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public void setContactSFAvatarURL(String contactId, String URL, Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            mRealm.beginTransaction();
            Contact contact = getContactById(contactId, mRealm);
            contact.setStringField1(URL);
            contact = mRealm.copyToRealmOrUpdate(contact);
            String updatedURL = contact.getStringField1();
            Log.i(Constants.TAG, "RealmContactTransactions.setContactSFAvatarURL: Object Updated with" +
                    " URL" + "->" + updatedURL);
            mRealm.commitTransaction();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmContactTransactions.setContactSFAvatarURL: ",e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }

    }

}

