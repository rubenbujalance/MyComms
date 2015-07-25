package com.vodafone.mycomms.realm;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.util.Constants;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import model.UserProfile;

public class RealmProfileTransactions {
    private Realm mRealm;

    public RealmProfileTransactions() {
        //mRealm = Realm.getDefaultInstance();
    }

    public void insertUserProfile(UserProfile userProfile){
        Log.d(Constants.TAG, "RealmProfileTransactions.insertUserProfile: " + userProfile);
        if(userProfile == null){
            Log.e(Constants.TAG, "RealmProfileTransactions.insertUserProfile: UserProfile is null!!!!");
            return;
        }

        try {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(userProfile);

            realm.commitTransaction();
            realm.close();
        } catch (IllegalArgumentException e){
            Log.e(Constants.TAG, "RealmProfileTransactions.insertContact: " + e);
            Crashlytics.logException(e);
        }
    }

    public void updateProfileTimezone (String timezone, String profileId){
        try {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            UserProfile userProfile = getUserProfile(profileId);
            userProfile.setTimezone(timezone);
            realm.commitTransaction();
            realm.close();
        } catch (IllegalArgumentException e){
            e.printStackTrace();
            Log.e(Constants.TAG, "RealmProfileTransactions.updateProfileTimezone: " + e);
            Crashlytics.logException(e);
        } catch (Exception e){
            Log.e(Constants.TAG, "RealmProfileTransactions.updateProfileTimezone: timezone or profile are null " , e);
            Crashlytics.logException(e);
        }
    }

    public void removeUserProfile (String profileId){
        try {
            Realm realm = Realm.getDefaultInstance();
            RealmQuery<UserProfile> query = realm.where(UserProfile.class);
            query.equalTo(Constants.PROFILE_ID, profileId);
            UserProfile userProfile = query.findFirst();
            realm.close();
            if (userProfile != null) {
                userProfile.removeFromRealm();
            }
        } catch (Exception e){
            Log.e(Constants.TAG, "RealmProfileTransactions.removeUserProfile: " , e);
            Crashlytics.logException(e);
        }
    }

    public UserProfile getUserProfile(String profileId){
        try {
            Realm realm = Realm.getDefaultInstance();
            RealmQuery<UserProfile> query = realm.where(UserProfile.class);
            query.equalTo(Constants.PROFILE_ID, profileId);
                 //.equalTo(Constants.CONTACT_PROFILE_ID, mProfileId);
            RealmResults<UserProfile> result1 = query.findAll();
            realm.close();
            if (result1 != null) {
                return result1.first();
            } else {
                return null;
            }
        }catch (Exception e ){
            Log.e(Constants.TAG, "RealmProfileTransactions.getUserProfile: " , e);
            Crashlytics.logException(e);
            return  null;
        }
    }

    public void closeRealm() {
//        if(mRealm!=null && !Utils.isMainThread()) mRealm.close();
    }

}

