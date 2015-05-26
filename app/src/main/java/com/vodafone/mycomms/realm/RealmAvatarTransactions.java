package com.vodafone.mycomms.realm;

import android.util.Log;

import com.vodafone.mycomms.util.Constants;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import model.ContactAvatar;

/**
 * Created by AMG on 12/05/2015.
 */
public final class RealmAvatarTransactions {

    private static Realm mRealm;

    public RealmAvatarTransactions(Realm realm) {
        mRealm = realm;
    }

    public void insertAvatar (ContactAvatar newAvatar){
        try {
            mRealm.beginTransaction();
            mRealm.copyToRealmOrUpdate(newAvatar);

            mRealm.commitTransaction();
        } catch (IllegalArgumentException e){
            Log.e(Constants.TAG, "RealmAvatarTransactions.insertAvatar: ", e);
        }
    }

    public void insertAvatarList (ArrayList<ContactAvatar> contactAvatarArrayList){
        int size = contactAvatarArrayList.size();
        try {
            mRealm.beginTransaction();
            for (int i = 0; i < size; i++) {
                mRealm.copyToRealmOrUpdate(contactAvatarArrayList.get(i));
            }
            mRealm.commitTransaction();
        } catch (IllegalArgumentException e){
            e.printStackTrace();
            Log.e(Constants.TAG, "RealmAvatarTransactions.insertContactList: ",e);
        }
    }

    public ArrayList<ContactAvatar> getAllContactsAvatar(){
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

    public ArrayList<ContactAvatar> getFilteredContactsAvatar(String field, String filter){
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

    public ContactAvatar getContactAvatarByContactId(String contactId){
        ContactAvatar avatar = null;

        try {
            RealmQuery<ContactAvatar> query = mRealm.where(ContactAvatar.class);
            query.equalTo(Constants.AVATAR_CONTACT_ID, contactId);

            RealmResults<ContactAvatar> result = query.findAll();

            if (result != null && !result.isEmpty())
                avatar = result.first();

        } catch (Exception ex) {
            Log.e(Constants.TAG, "RealmAvatarTransactions.getContactAvatarByContactId: ", ex);
        }

        return avatar;
    }

    public void deleteContactAvatar(String field, String filter) {
        mRealm.beginTransaction();
        RealmQuery<ContactAvatar> query = mRealm.where(ContactAvatar.class);
        query.equalTo(field, filter);
        RealmResults<ContactAvatar> result1 = query.findAll();
        if (result1 != null) {
            result1.clear();
        }
        mRealm.commitTransaction();
    }

    public void deleteAllContactsAvatar() {
        mRealm.beginTransaction();
        RealmQuery<ContactAvatar> query = mRealm.where(ContactAvatar.class);
        RealmResults<ContactAvatar> result1 = query.findAll();
        if (result1 != null) {
            result1.clear();
        }
        mRealm.commitTransaction();
    }
}

