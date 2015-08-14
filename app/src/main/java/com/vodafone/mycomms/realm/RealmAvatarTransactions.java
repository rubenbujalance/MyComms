package com.vodafone.mycomms.realm;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.util.Constants;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import model.ContactAvatar;

/**
 * Created by AMG on 12/05/2015.
 */
public final class RealmAvatarTransactions
{

    public RealmAvatarTransactions()
    {
    }

    public void insertAvatar (ContactAvatar newAvatar, Realm realm)
    {

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            mRealm.beginTransaction();
            mRealm.copyToRealmOrUpdate(newAvatar);
            mRealm.commitTransaction();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmAvatarTransactions.insertAvatar: ",e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public void insertAvatarList (ArrayList<ContactAvatar> contactAvatarArrayList, Realm realm)
    {
        int size = contactAvatarArrayList.size();
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            mRealm.beginTransaction();
            for (int i = 0; i < size; i++) {
                mRealm.copyToRealmOrUpdate(contactAvatarArrayList.get(i));
            }
            mRealm.commitTransaction();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmAvatarTransactions.insertAvatarList: ",e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public ArrayList<ContactAvatar> getAllContactsAvatar(Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            ArrayList<ContactAvatar> contactAvatarArrayList = new ArrayList<>();
            RealmQuery<ContactAvatar> query = mRealm.where(ContactAvatar.class);

            RealmResults<ContactAvatar> result1 = query.findAll();
            if (result1!=null){
                for (ContactAvatar contactAvatarListItem : result1) {
                    contactAvatarArrayList.add(contactAvatarListItem);
                }
            }
            return contactAvatarArrayList;
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmAvatarTransactions.getAllContactsAvatar: ", e);
            Crashlytics.logException(e);
            return null;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }

    }

    public ArrayList<ContactAvatar> getFilteredContactsAvatar(String field, String filter, Realm realm)
    {

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            ArrayList<ContactAvatar> contactAvatarArrayList = new ArrayList<>();
            RealmQuery<ContactAvatar> query = mRealm.where(ContactAvatar.class);
            query.equalTo(field, filter);

            RealmResults<ContactAvatar> result1 = query.findAll();
            if (result1!=null){
                for (ContactAvatar contactAvatarListItem : result1) {
                    contactAvatarArrayList.add(contactAvatarListItem);
                }
            }
            return contactAvatarArrayList;
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmAvatarTransactions.getFilteredContactsAvatar: ", e);
            Crashlytics.logException(e);
            return null;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }

    }

    public ContactAvatar getContactAvatarByContactId(String contactId, Realm realm)
    {
        ContactAvatar avatar = null;
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            RealmQuery<ContactAvatar> query = mRealm.where(ContactAvatar.class);
            query.equalTo(Constants.AVATAR_CONTACT_ID, contactId);

            RealmResults<ContactAvatar> result = query.findAll();

            if (result != null && !result.isEmpty())
                avatar = result.first();

            return avatar;
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmAvatarTransactions.getContactAvatarByContactId: ", e);
            Crashlytics.logException(e);
            return null;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public void updateAvatarUrlByContactId(String contactId, String url, Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            mRealm.beginTransaction();
            ContactAvatar avatar = getContactAvatarByContactId(contactId, mRealm);
            avatar.setUrl(url);
            mRealm.commitTransaction();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmAvatarTransactions.updateAvatarUrlByContactId: ",e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public void deleteContactAvatar(String field, String filter, Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            mRealm.beginTransaction();
            RealmQuery<ContactAvatar> query = mRealm.where(ContactAvatar.class);
            query.equalTo(field, filter);
            RealmResults<ContactAvatar> result1 = query.findAll();
            if (result1 != null) {
                result1.clear();
            }
            mRealm.commitTransaction();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmAvatarTransactions.deleteContactAvatar: ",e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }

    }

    public void deleteAllContactsAvatar(Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            mRealm.beginTransaction();
            RealmQuery<ContactAvatar> query = mRealm.where(ContactAvatar.class);
            RealmResults<ContactAvatar> result1 = query.findAll();
            if (result1 != null) {
                result1.clear();
            }
            mRealm.commitTransaction();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmAvatarTransactions.deleteAllContactsAvatar: ",e);
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

