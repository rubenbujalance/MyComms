package com.vodafone.mycomms.settings;

import android.support.v4.app.Fragment;
import android.util.Log;

import com.framework.library.exception.ConnectionException;
import com.framework.library.model.ConnectionResponse;

import model.Contact;
import model.UserProfile;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.contacts.connection.ContactConnection;
import com.vodafone.mycomms.contacts.connection.ContactController;
import com.vodafone.mycomms.settings.connection.IProfileConnectionCallback;
import com.vodafone.mycomms.settings.connection.ProfileConnection;
import com.vodafone.mycomms.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by str_vig on 26/05/2015.
 */
public class ProfileController extends BaseController {

    private ProfileConnection profileConnection;

    public ProfileController(Fragment fragment) {
        super(fragment);
    }

    public void getProfile(){
        Log.d(Constants.TAG, "ProfileController.getProfile: ");
        if(profileConnection != null){
            profileConnection.cancel();
        }

        profileConnection = new ProfileConnection(getContext(), this);
        profileConnection.request();
    }

    @Override
    public void onConnectionComplete(ConnectionResponse response){
        super.onConnectionComplete(response);
        Log.d(Constants.TAG, "ProfileController.onConnectionComplete: ");

        String result = response.getData().toString();



        UserProfile userProfile = null;
        try {

            JSONObject jsonResponse = new JSONObject(result);
//            String data = jsonResponse.getString(Constants.CONTACT_DATA);
//            jsonResponse = new JSONObject(data.substring(1, data.length()-1 )); //Removing squared bracelets.
//

            userProfile =  mapUserProfile(jsonResponse);
            Log.d(Constants.TAG, "ProfileController.onConnectionComplete: UserProfile parsed:" + userProfile.getFirstName() + "," + userProfile.getLastName());
        } catch (Exception e){
            Log.e(Constants.TAG, "ProfileController.onConnectionComplete: Exception while parsing userProfile" , e);
        }

        if(this.getConnectionCallback() != null && this.getConnectionCallback() instanceof IProfileConnectionCallback && response.getUrl() !=null  && response.getUrl().contains(ProfileConnection.URL)){
            ((IProfileConnectionCallback)this.getConnectionCallback()).onProfileReceived(userProfile);
        }
    }



    @Override
    public void onConnectionError(ConnectionException ex){
        super.onConnectionError(ex);
        Log.w(Constants.TAG, "ProfileController.onConnectionError: ");
        if(this.getConnectionCallback() != null && this.getConnectionCallback() instanceof IProfileConnectionCallback && ex.getUrl() !=null  && ex.getUrl().contains(ProfileConnection.URL)){
            ((IProfileConnectionCallback)this.getConnectionCallback()).onProfileConnectionError();
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


}
