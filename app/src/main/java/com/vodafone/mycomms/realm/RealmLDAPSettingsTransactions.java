package com.vodafone.mycomms.realm;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.util.Constants;

import io.realm.Realm;
import io.realm.RealmQuery;
import model.GlobalContactsSettings;

public class RealmLDAPSettingsTransactions {

    private static void insertOrUpdateSettings (GlobalContactsSettings settings, Realm realm){
        boolean isNewRealm = false;
        try {
            if(realm==null) {
                realm = Realm.getDefaultInstance();
                isNewRealm = true;
            }
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(settings);
            realm.commitTransaction();
        } catch (IllegalArgumentException e){
            Log.e(Constants.TAG, "RealmLDAPSettingsTransactions.insertUpdateSettings: ", e);
            Crashlytics.logException(e);
            realm.cancelTransaction();
        } finally {
            if(isNewRealm) realm.close();
        }
    }

    public static GlobalContactsSettings getSettings(String profileId, Realm realm){
        boolean isNewRealm = false;
        try {
            if (realm == null) {
                realm = Realm.getDefaultInstance();
                isNewRealm = true;
            }

            RealmQuery<GlobalContactsSettings> query = realm.where(GlobalContactsSettings.class);
            query.equalTo(Constants.LDAP_SETTINGS_FIELD_PROFILE_ID, profileId);
            GlobalContactsSettings settings = query.findFirst();

            if (settings != null)
                return settings;

        } catch (IllegalArgumentException e){
            Log.e(Constants.TAG, "RealmLDAPSettingsTransactions.getSettings: ",e);
            Crashlytics.logException(e);
            realm.cancelTransaction();
        } finally {
            if(isNewRealm) realm.close();
        }

        return null;
    }

    public static void createOrUpdateData (String profileId, String user, String password,
                                           String token, Realm realm){
        Log.i(Constants.TAG, "RealmLDAPSettingsTransactions.createOrUpdateData: " +
                "Creating/Updating LDAP data for user "+user);

        boolean isNewRealm = false;
        try {
            if(realm==null) {
                realm = Realm.getDefaultInstance();
                isNewRealm = true;
            }
            realm.beginTransaction();
            GlobalContactsSettings settings = getSettings(profileId, realm);
            if(settings==null) {
                settings = new GlobalContactsSettings(profileId, user, password, token);
                insertOrUpdateSettings(settings, realm);
            }
            else {
                settings.setUser(user);
                settings.setPassword(password);
                settings.setToken(token);
            }
            realm.commitTransaction();
        } catch (IllegalArgumentException e){
            Log.e(Constants.TAG, "RealmLDAPSettingsTransactions.setUserAndPassword: ",e);
            Crashlytics.logException(e);
            realm.cancelTransaction();
        } finally {
            if(isNewRealm) realm.close();
        }
    }
}
