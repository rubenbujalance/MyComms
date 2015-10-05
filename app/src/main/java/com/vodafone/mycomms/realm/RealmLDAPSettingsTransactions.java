package com.vodafone.mycomms.realm;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.util.Constants;

import io.realm.Realm;
import io.realm.RealmQuery;
import model.GlobalContactsSettings;

public class RealmLDAPSettingsTransactions {

    public static GlobalContactsSettings getSettings(String profileId, Realm realm)
    {

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            RealmQuery<GlobalContactsSettings> query = mRealm.where(GlobalContactsSettings.class);
            query.equalTo(Constants.LDAP_SETTINGS_FIELD_PROFILE_ID, profileId);
            GlobalContactsSettings settings = query.findFirst();
            return settings;
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmLDAPSettingsTransactions.getSettings: ", e);
            Crashlytics.logException(e);
            return null;
        }
        finally
        {
            if(null == realm && null != mRealm)
                mRealm.close();
        }
    }

    public static boolean haveSettings(String profileId, Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            GlobalContactsSettings settings = mRealm.where(GlobalContactsSettings.class)
                    .equalTo(Constants.LDAP_SETTINGS_FIELD_PROFILE_ID, profileId).findFirst();

            return settings != null && settings.getToken() != null && settings.getToken().length() > 0;
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmLDAPSettingsTransactions.haveSettings: ",e);
            Crashlytics.logException(e);
        }
        finally
        {
            if(null == realm && null != mRealm)
                mRealm.close();
        }

        return false;
    }

    public static GlobalContactsSettings createOrUpdateData (String profileId, String user,
                                                             String password, String token,
                                                             String tokenType, String url,
                                                             Realm realm)
    {
        Log.i(Constants.TAG, "RealmLDAPSettingsTransactions.createOrUpdateData: " +
                "Creating/Updating LDAP data for user "+user);

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            mRealm.beginTransaction();
//            GlobalContactsSettings settings = getSettings(profileId, realm);
//            if(settings==null) {
            GlobalContactsSettings settings = new GlobalContactsSettings(profileId, user, password,
                        token, tokenType, url);
                mRealm.copyToRealmOrUpdate(settings);
//            }
//            else {
//                settings.setUser(user);
//                settings.setPassword(password);
//                settings.setToken(token);
//                settings.setTokenType(tokenType);
//                settings.setUrl(url);
//            }
            mRealm.commitTransaction();

            return settings;
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmLDAPSettingsTransactions.createOrUpdateData: ",e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
            return null;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public static void deleteTokenData (String profileId, Realm realm)
    {
        Log.i(Constants.TAG, "RealmLDAPSettingsTransactions.deleteTokenData: "+profileId);

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            mRealm.beginTransaction();
            GlobalContactsSettings settings = getSettings(profileId, realm);

            settings.setToken(null);
            settings.setTokenType(null);
            settings.setUrl(null);

            mRealm.commitTransaction();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmLDAPSettingsTransactions.deleteTokenData: ",e);
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
