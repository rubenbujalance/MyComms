package com.vodafone.mycomms.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.framework.library.exception.ConnectionException;
import com.framework.library.model.ConnectionResponse;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.settings.connection.IProfileConnectionCallback;
import com.vodafone.mycomms.settings.connection.PasswordConnection;
import com.vodafone.mycomms.settings.connection.ProfileConnection;
import com.vodafone.mycomms.settings.connection.UpdateProfileConnection;
import com.vodafone.mycomms.settings.connection.UpdateSettingsConnection;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UserSecurity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.realm.Realm;
import model.UserProfile;

/**
 * Created by str_vig on 26/05/2015.
 */
public class ProfileController extends BaseController {

    private RealmContactTransactions realmContactTransactions;
    private ProfileConnection profileConnection;
    private Realm realm;

    public ProfileController(Fragment fragment) {
        super(fragment);
        realm = Realm.getInstance(getActivity());
        realmContactTransactions = new RealmContactTransactions(realm);
    }

    public ProfileController(Activity activity) {
        super(activity);
        realm = Realm.getInstance(getActivity());
        realmContactTransactions = new RealmContactTransactions(realm);
    }

    public void getProfile(){
        Log.d(Constants.TAG, "ProfileController.getProfile: ");

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        String profileId = null;

        if(sharedPreferences != null) {
            profileId = sharedPreferences.getString(Constants.PROFILE_ID_SHARED_PREF, null);
        }

        if(profileId != null && profileId.length() > 0) {
            Log.d(Constants.TAG, "ProfileController.getProfile: retrieving profile with profileID:" + profileId);
            UserProfile userProfileFromDB = null;
            if(realmContactTransactions != null) {
                userProfileFromDB = realmContactTransactions.getUserProfile(profileId);
            }else{
                Log.e(Constants.TAG, "ProfileController.getProfile: realmContactTransactions is null");
            }

            if (this.getConnectionCallback() != null && userProfileFromDB != null) {
                Log.d(Constants.TAG, "ProfileController.getProfile: profile received from DB: " + printUserProfile(userProfileFromDB));
                ((IProfileConnectionCallback) this.getConnectionCallback()).onProfileReceived(userProfileFromDB);
            }
        }

        if(profileConnection != null){
            profileConnection.cancel();
        }
        profileConnection = new ProfileConnection(getContext(), this);
        profileConnection.request();
    }

    public void setProfileId(String profileId){
        Log.i(Constants.TAG, "ProfileController.setProfileId: ");
        SharedPreferences sp = getContext().getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Constants.PROFILE_ID_SHARED_PREF, profileId);
        editor.putString(Constants.ACCESS_TOKEN_SHARED_PREF, UserSecurity.getAccessToken(getContext()));
        editor.apply();
    }

    @Override
    public void onConnectionComplete(ConnectionResponse response){
        super.onConnectionComplete(response);

        Log.d(Constants.TAG, "ProfileController.onConnectionComplete: " + response.getUrl() + response.getUrl());


        UserProfile userProfile = null;
        if(response.getUrl() != null && !response.getUrl().contains(UpdateSettingsConnection.URL)) {
            String result = response.getData().toString();

            try {

                if (result != null && result.length() >= 0) {
                    JSONObject jsonResponse = new JSONObject(result);

                    userProfile = mapUserProfile(jsonResponse);
                    realmContactTransactions.insertUserProfile(userProfile);
                    Log.d(Constants.TAG, "ProfileController.onConnectionComplete: UserProfile parsed:" + printUserProfile(userProfile));
                }

            } catch (Exception e) {
                Log.w(Constants.TAG, "ProfileController.onConnectionComplete: Exception while parsing userProfile", e);
            }
        }

        if(this.getConnectionCallback() != null && this.getConnectionCallback() instanceof IProfileConnectionCallback) {
            if (response.getUrl() != null && response.getUrl().contains(ProfileConnection.URL)) {
                if (userProfile != null) {
                    ((IProfileConnectionCallback) this.getConnectionCallback()).onProfileReceived(userProfile);
                } else {
                    ((IProfileConnectionCallback) this.getConnectionCallback()).onUpdateProfileConnectionCompleted();
                }
            }else if (response.getUrl() != null && response.getUrl().contains(PasswordConnection.URL)){

            }
        }



    }

    @Override
    public void onConnectionError(ConnectionException ex){
        super.onConnectionError(ex);
        Log.w(Constants.TAG, "ProfileController.onConnectionError: " + ex.getUrl() + "," + ex.getContent());
        if(this.getConnectionCallback() != null && this.getConnectionCallback() instanceof IProfileConnectionCallback ) {
            if (ex.getUrl() != null && ex.getUrl().contains(ProfileConnection.URL)) {
                if (ex.getContent() != null && ex.getContent().contains("\"err\":\"incorrectData\"")) {
                    //TODO It does not seems correct that the MyComms  Public API has two calls to "/api/me"  URLs and the only difference is that one is PUT and the pther is GET
                    //TODO Currently the Connectivity API is not prepared to retrieve the Method type (GET, PUT, DELETE) of the connection from the error, so no clean way of doing this.
                    ((IProfileConnectionCallback) this.getConnectionCallback()).onUpdateProfileConnectionError();
                } else {
                    ((IProfileConnectionCallback) this.getConnectionCallback()).onProfileConnectionError();
                }
            }else if (ex.getUrl() != null && ex.getUrl().contains(PasswordConnection.URL)){
                JSONObject jsonResponse = null;
                String error = "Unknown error while changing password";

                try {
                    jsonResponse = new JSONObject(ex.getContent());
                    error = jsonResponse.getString("des");
                } catch (JSONException e) {
                    Log.e(Constants.TAG, "SettingsController.onConnectionError: ", e);
                }

//                ((IProfileConnectionCallback) this.getConnectionCallback()).onPasswordChangeError(error);

            }
        }


    }

    public static UserProfile mapUserProfile(JSONObject jsonObject){
        UserProfile userProfile = new UserProfile();

        try {
            if (!jsonObject.isNull(Constants.CONTACT_ID)) userProfile.setId(jsonObject.getString(Constants.CONTACT_ID));
            //if (!jsonObject.isNull(Constants.CONTACT_DATA)) contact.setId(jsonObject.getString(Constants.CONTACT_DATA));
            if (!jsonObject.isNull(Constants.CONTACT_PLATFORM))
                userProfile.setPlatform(jsonObject.getString(Constants.CONTACT_PLATFORM));
            if (!jsonObject.isNull(Constants.CONTACT_FNAME))
                userProfile.setFirstName(jsonObject.getString(Constants.CONTACT_FNAME));
            if (!jsonObject.isNull(Constants.CONTACT_LNAME))
                userProfile.setLastName(jsonObject.getString(Constants.CONTACT_LNAME));
            if (!jsonObject.isNull(Constants.CONTACT_AVATAR)) userProfile.setAvatar(jsonObject.getString(Constants.CONTACT_AVATAR));
            if (!jsonObject.isNull(Constants.CONTACT_POSITION))
                userProfile.setPosition(jsonObject.getString(Constants.CONTACT_POSITION));
            if (!jsonObject.isNull(Constants.CONTACT_COMPANY)) userProfile.setCompany(jsonObject.getString(Constants.CONTACT_COMPANY));
            if (!jsonObject.isNull(Constants.CONTACT_TIMEZONE))
                userProfile.setTimezone(jsonObject.getString(Constants.CONTACT_TIMEZONE));
            if (!jsonObject.isNull(Constants.CONTACT_LASTSEEN)) userProfile.setLastSeen(jsonObject.getInt(Constants.CONTACT_LASTSEEN));
            if (!jsonObject.isNull(Constants.CONTACT_OFFICE_LOC))
                userProfile.setOfficeLocation(jsonObject.getString(Constants.CONTACT_OFFICE_LOC));
            if (!jsonObject.isNull(Constants.CONTACT_PHONES))
                userProfile.setPhones(jsonObject.getJSONArray(Constants.CONTACT_PHONES).toString());
            if (!jsonObject.isNull(Constants.CONTACT_EMAILS))
                userProfile.setEmails(jsonObject.getJSONArray(Constants.CONTACT_EMAILS).toString());
            if (!jsonObject.isNull(Constants.CONTACT_AVAILABILITY))
                userProfile.setAvailability(jsonObject.getString(Constants.CONTACT_AVAILABILITY));
            if (!jsonObject.isNull(Constants.CONTACT_PRESENCE))
                userProfile.setPresence(jsonObject.getString(Constants.CONTACT_PRESENCE));
            if (!jsonObject.isNull(Constants.CONTACT_COUNTRY))
                userProfile.setCountry(jsonObject.getString(Constants.CONTACT_COUNTRY));
            if (!jsonObject.isNull(Constants.PROFILE_SETTINGS)) userProfile.setSettings(jsonObject.getString(Constants.PROFILE_SETTINGS));
            if (!jsonObject.isNull(Constants.PROFILE_PLATFORMS)) userProfile.setPlatforms(jsonObject.getString(Constants.PROFILE_PLATFORMS));
        }catch (JSONException e){
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactDBController.mapContact: " + e.toString());
        }

        return  userProfile;
    }

    public static String printUserProfile(UserProfile userProfile){
        StringBuffer buf = new StringBuffer();
        buf.append("Userprofile[");
        buf.append(userProfile.getFirstName());
        buf.append(",");
        buf.append(userProfile.getLastName());
        buf.append("]");
        buf.append("company:");
        buf.append(userProfile.getCompany());
        buf.append(", position:");
        buf.append(userProfile.getPosition());
        buf.append(", officeLocation:");
        buf.append(userProfile.getOfficeLocation());
        buf.append(", settings:");
        buf.append(userProfile.getSettings());



        return buf.toString();
    }


    public void updateContactData(HashMap profileHashMap) {
        JSONObject json = new JSONObject(profileHashMap);
        Log.d(Constants.TAG, "ProfileController.updateContactData: " + json.toString());
        UpdateProfileConnection updateProfileConnection = new UpdateProfileConnection(getContext(),this);
        updateProfileConnection.setPayLoad(json.toString());
        updateProfileConnection.request();
    }

    public void updateSettingsData(HashMap settingsHashMap) {
        JSONObject json = new JSONObject(settingsHashMap);
        Log.d(Constants.TAG, "ProfileController.updateSettingsData: " + json.toString());
        UpdateSettingsConnection updateSettingsConnection = new UpdateSettingsConnection(getContext(),this);
        updateSettingsConnection.setPayLoad(json.toString());
        updateSettingsConnection.request();
    }

    public void updatePassword(HashMap passwordHashMap){
            JSONObject json = new JSONObject(passwordHashMap);
            Log.d(Constants.TAG, "ProfileController.updateContactData: " + json.toString());
            PasswordConnection passwordConnection = new PasswordConnection(getContext(),this);
            passwordConnection.setPayLoad(json.toString());
            passwordConnection.request();

    }


    public HashMap getProfileHashMap(UserProfile userProfile)
    {
        HashMap<String, String> body = new HashMap<String, String>();
        if(userProfile.getFirstName() != null) body.put("firstName",userProfile.getFirstName() );
        if(userProfile.getLastName()  != null) body.put("lastName",userProfile.getLastName() );
        if(userProfile.getCompany()  != null) body.put("company",userProfile.getCompany() );
        if(userProfile.getPosition() != null) body.put("position",userProfile.getPosition());
        if(userProfile.getOfficeLocation() != null) body.put("officeLocation",userProfile.getOfficeLocation());
        return body;
    }

}
