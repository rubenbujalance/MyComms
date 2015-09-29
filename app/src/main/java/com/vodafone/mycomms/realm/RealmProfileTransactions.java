package com.vodafone.mycomms.realm;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.util.Constants;

import java.util.MissingResourceException;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import model.UserProfile;

public class RealmProfileTransactions
{
    public RealmProfileTransactions()
    {
    }

    public void insertUserProfile(UserProfile userProfile, Realm realm)
    {
        Log.d(Constants.TAG, "RealmProfileTransactions.insertUserProfile: " + userProfile);
        if(userProfile == null){
            Log.e(Constants.TAG, "RealmProfileTransactions.insertUserProfile: UserProfile is null!!!!");
            return;
        }

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            if(null != mRealm)
            {
                mRealm.beginTransaction();
                mRealm.copyToRealmOrUpdate(userProfile);
                mRealm.commitTransaction();
            }
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmProfileTransactions.insertUserProfile: ",e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        }
        finally
        {
            if(null == realm && null != mRealm)
                mRealm.close();
        }
    }

    public void updateProfileTimezone (String timezone, String profileId, Realm realm)
    {

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            if(null != mRealm)
            {
                mRealm.beginTransaction();
                UserProfile userProfile = getUserProfile(profileId, mRealm);
                userProfile.setTimezone(timezone);
                mRealm.commitTransaction();
            }

        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmProfileTransactions.updateProfileTimezone: ", e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public void removeUserProfile (String profileId, Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            if(null != mRealm)
            {
                mRealm.beginTransaction();
                RealmQuery<UserProfile> query = mRealm.where(UserProfile.class);
                query.equalTo(Constants.PROFILE_ID, profileId);
                UserProfile userProfile = query.findFirst();
                if (userProfile != null)
                {
                    userProfile.removeFromRealm();
                }
                mRealm.commitTransaction();
            }
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmProfileTransactions.removeUserProfile: ",e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public UserProfile getUserProfile(String profileId, Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            RealmQuery<UserProfile> query = mRealm.where(UserProfile.class);
            query.equalTo(Constants.PROFILE_ID, profileId);
            RealmResults<UserProfile> result1 = query.findAll();
            if (result1 != null && result1.size() > 0) {
                return result1.first();
            } else {
                return null;
            }
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmProfileTransactions.getUserProfile: ",e);
            Crashlytics.logException(e);
            return null;
        }
        finally
        {
            if(null == realm && null != mRealm)
                mRealm.close();
        }
    }
}

