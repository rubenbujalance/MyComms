package com.vodafone.mycomms.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.framework.library.exception.ConnectionException;
import com.framework.library.model.ConnectionResponse;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.login.LoginSignupActivity;
import com.vodafone.mycomms.realm.RealmProfileTransactions;
import com.vodafone.mycomms.settings.connection.IProfileConnectionCallback;
import com.vodafone.mycomms.settings.connection.PasswordConnection;
import com.vodafone.mycomms.settings.connection.ProfileConnection;
import com.vodafone.mycomms.settings.connection.UpdateProfileConnection;
import com.vodafone.mycomms.settings.connection.UpdateSettingsConnection;
import com.vodafone.mycomms.settings.connection.UpdateTimeZoneConnection;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.OKHttpWrapper;
import com.vodafone.mycomms.util.UserSecurity;
import com.vodafone.mycomms.util.Utils;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import io.realm.Realm;
import model.UserProfile;

public class ProfileController extends BaseController {

    private RealmProfileTransactions mRealmProfileTransactions;
    private UserProfile userProfile;
    private String profileId;
    private Context mContext;

    public ProfileController(Context context) {
        super(context);
        this.mContext = context;
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        profileId = sharedPreferences.getString(Constants.PROFILE_ID_SHARED_PREF, null);

        mRealmProfileTransactions = new RealmProfileTransactions();
    }

    /**
     * Get Profile, uses DB and Network also. (First loads from DB by a callback then starts network connection.
     */
    public void getProfile(Realm realm){
        Log.i(Constants.TAG, "ProfileController.getProfile: ");

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

        if (sharedPreferences != null) {
            profileId = sharedPreferences.getString(Constants.PROFILE_ID_SHARED_PREF, null);
        }

        if (profileId != null && profileId.length() > 0) {
            UserProfile userProfileFromDB = null;
            if (mRealmProfileTransactions != null) {
                userProfileFromDB = mRealmProfileTransactions.getUserProfile(profileId, realm);
            }

            if (this.getConnectionCallback() != null && userProfileFromDB != null) {
                ((IProfileConnectionCallback) this.getConnectionCallback())
                        .onProfileReceived(userProfileFromDB);
            }
        }

//        new GetProfileAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
//                (String) Constants.CONTACT_API_GET_PROFILE);

        OKHttpWrapper.get(Constants.CONTACT_API_GET_PROFILE, getContext(), new OKHttpWrapper.HttpCallback() {
            @Override
            public void onFailure(Response response, IOException e) {
                getProfileCallback(null);
            }

            @Override
            public void onSuccess(Response response) {
                String json;

                try {
                    if (Integer.toString(response.code()).startsWith("2")) {
                        json = response.body().string();
                        getProfileCallback(json);
                    } else {
                        getProfileCallback(null);
                    }
                } catch (Exception e) {
                    Log.e(Constants.TAG, "ProfileController.onSuccess: ", e);
                }
            }
        });
    }

    public boolean isUserProfileChanged(String firstName, String lastName, String company, String
            position, String officeLocation)
    {
        if(null != this.userProfile)
        {
            if(firstName.equals(userProfile.getFirstName())
                    && lastName.equals(userProfile.getLastName())
                    && company.equals(userProfile.getCompany())
                    && position.equals(userProfile.getPosition())
                    && officeLocation.equals(userProfile.getOfficeLocation()))
            {
                return false;
            }
            return true;
        }
        else
        {
            return false;
        }

    }

    public void updateUserProfileInDB(String firstName, String lastName, String company, String
            position, String officeLocation){
        Log.d(Constants.TAG, "ProfileController.updateUserProfileInDB: ");
        if(userProfile != null){
            userProfile.setFirstName(firstName);
            userProfile.setLastName(lastName);
            userProfile.setCompany(company);
            userProfile.setPosition(position);
            userProfile.setOfficeLocation(officeLocation);
            mRealmProfileTransactions.insertUserProfile(userProfile, null);
        }
    }

    public void updateUserAvatarInDB(String avatarNewURL)
    {
        Log.d(Constants.TAG, "ProfileController.updateUserAvatarInDB: ");
        if(userProfile != null)
        {
            userProfile.setAvatar(avatarNewURL);
            mRealmProfileTransactions.insertUserProfile(userProfile, null);
        }
    }

    public void updateUserProfileSettingsInDB(boolean privateLocation, boolean privateTimezone,
                                              String holidayEndDate,
                                              boolean doNotDisturb) {
        Log.d(Constants.TAG, "ProfileController.updateUserProfileSettingsInDB: privateLocation= " + privateLocation + ", privateTimezone= " + privateTimezone + ", holidayEndDate=" + holidayEndDate + ", doNotDisturb=" + doNotDisturb);
        if(userProfile != null){
            HashMap body = new HashMap<>();

            if(privateTimezone) body.put("privateTimeZone",privateTimezone );
            if(holidayEndDate!=null) {
                HashMap holiday = new HashMap();
                if(holidayEndDate.length()>0) {
                    holiday.put(Constants.PROFILE_HOLIDAY_END_DATE, holidayEndDate);
                }
                body.put(Constants.PROFILE_HOLIDAY, holiday);
            }
            if(doNotDisturb) body.put("doNotDisturb", doNotDisturb );
            if(privateLocation) body.put("privateLocation",privateLocation);


            JSONObject json = new JSONObject(body);
            Log.d(Constants.TAG, "ProfileController.updateUserProfileSettingsInDB: " + json.toString());
            userProfile.setSettings(json.toString());
            mRealmProfileTransactions.insertUserProfile(userProfile, null);
        }
    }

    public void setUserProfile(String profileIdfromAPI, String profileFullName, String platforms, String timeZone){
        Log.i(Constants.TAG, "ProfileController.setProfileId: ");
        SharedPreferences sp = getContext().getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Constants.PROFILE_ID_SHARED_PREF, profileIdfromAPI);
        editor.putString(Constants.PROFILE_FULLNAME_SHARED_PREF, profileFullName);
        editor.putString(Constants.ACCESS_TOKEN_SHARED_PREF, UserSecurity.getAccessToken(getContext()));
        editor.putString(Constants.PLATFORMS_SHARED_PREF, platforms);
        editor.putString(Constants.TIMEZONE_SHARED_PREF, timeZone);
        editor.apply();
        profileId = profileIdfromAPI;
    }

    public void updateProfileTimezone(String timezone)
    {
        mRealmProfileTransactions.updateProfileTimezone(timezone, profileId, null);
    }

    @Override
    public void onConnectionComplete(ConnectionResponse response)
    {
        super.onConnectionComplete(response);
        Log.i(Constants.TAG, "ProfileController.onConnectionComplete: " + response.getUrl());

        boolean isUserProfileReceived = false;
        if(response.getUrl() != null && !response.getUrl().endsWith(UpdateSettingsConnection.URL))
        {
            try
            {
                String result = response.getData().toString();
                if (result != null && result.length() > 0) {
                    JSONObject jsonResponse = new JSONObject(result);
                    this.userProfile = mapUserProfile(jsonResponse);

                    mRealmProfileTransactions.insertUserProfile(userProfile, null);
                    Log.d(Constants.TAG, "ProfileController.onConnectionComplete: UserProfile parsed:" + printUserProfile(userProfile));
                    if(userProfile != null) {
                        isUserProfileReceived = true;
                    }
                }
            } catch (Exception e) {
                Log.e(Constants.TAG, "ProfileController.onConnectionComplete: Exception (handled correctly) while parsing userProfile",e);
            }
        }

        if(this.getConnectionCallback() != null && this.getConnectionCallback() instanceof IProfileConnectionCallback) {
            if (response.getUrl() != null && response.getUrl().endsWith(ProfileConnection.URL)) {
                if (isUserProfileReceived) {
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
        Log.e(Constants.TAG, "ProfileController.onConnectionError: " + ex.getUrl() + "," + ex.getContent());
        if(this.getConnectionCallback() != null && this.getConnectionCallback() instanceof IProfileConnectionCallback ) {
            if (ex.getUrl() != null && ex.getUrl().contains(ProfileConnection.URL)) {
                if (ex.getContent() != null && ex.getContent().contains("\"err\":\"incorrectData\"")) {
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
                    Crashlytics.logException(e);
                }

                //Commented due to "BaseController.onConnectionError: {"err":"auth_proxy_error","des":"invalid body request"}
                //((IProfileConnectionCallback) this.getConnectionCallback()).onPasswordChangeError(error);
            }
        }
    }

    public static UserProfile mapUserProfile(JSONObject jsonObject){
        UserProfile userProfile = new UserProfile();

        try {
            if (!jsonObject.isNull(Constants.CONTACT_ID))
                userProfile.setId(jsonObject.getString(Constants.CONTACT_ID));
            if (!jsonObject.isNull(Constants.CONTACT_PLATFORM))
                userProfile.setPlatform(jsonObject.getString(Constants.CONTACT_PLATFORM));
            if (!jsonObject.isNull(Constants.CONTACT_FNAME))
                userProfile.setFirstName(jsonObject.getString(Constants.CONTACT_FNAME));
            if (!jsonObject.isNull(Constants.CONTACT_LNAME))
                userProfile.setLastName(jsonObject.getString(Constants.CONTACT_LNAME));
            if (!jsonObject.isNull(Constants.CONTACT_AVATAR))
                userProfile.setAvatar(jsonObject.getString(Constants.CONTACT_AVATAR));
            if (!jsonObject.isNull(Constants.CONTACT_POSITION))
                userProfile.setPosition(jsonObject.getString(Constants.CONTACT_POSITION));
            if (!jsonObject.isNull(Constants.CONTACT_COMPANY))
                userProfile.setCompany(jsonObject.getString(Constants.CONTACT_COMPANY));
            if (!jsonObject.isNull(Constants.CONTACT_TIMEZONE))
                userProfile.setTimezone(jsonObject.getString(Constants.CONTACT_TIMEZONE));
            if (!jsonObject.isNull(Constants.CONTACT_LASTSEEN))
                userProfile.setLastSeen(jsonObject.getInt(Constants.CONTACT_LASTSEEN));
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
            if (!jsonObject.isNull(Constants.PROFILE_SETTINGS))
                userProfile.setSettings(jsonObject.getString(Constants.PROFILE_SETTINGS));
            if (!jsonObject.isNull(Constants.PROFILE_PLATFORMS))
                userProfile.setPlatforms(jsonObject.getString(Constants.PROFILE_PLATFORMS));
        }catch (JSONException e){
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactDBController.mapContact: " + e.toString());
            Crashlytics.logException(e);
        }
        return  userProfile;
    }

    public static String printUserProfile(UserProfile userProfile){
        if(userProfile == null){
            return "UserProfile is NULL";
        }
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
        Log.i(Constants.TAG, "ProfileController.updateContactData: " + json.toString());
        UpdateProfileConnection updateProfileConnection = new UpdateProfileConnection(getContext(),this);
        updateProfileConnection.setPayLoad(json.toString());
        updateProfileConnection.request();
    }

    public void updateSettingsData(HashMap settingsHashMap) {
        JSONObject json = new JSONObject(settingsHashMap);
        Log.i(Constants.TAG, "ProfileController.updateSettingsData: " + json.toString());
        UpdateSettingsConnection updateSettingsConnection =
                new UpdateSettingsConnection(getContext(),this);
        updateSettingsConnection.setPayLoad(json.toString());
        updateSettingsConnection.request();
    }

    public void updatePassword(HashMap passwordHashMap){
            JSONObject json = new JSONObject(passwordHashMap);
            Log.i(Constants.TAG, "ProfileController.updateContactData: " + json.toString());
            PasswordConnection passwordConnection = new PasswordConnection(getContext(),this);
            passwordConnection.setPayLoad(json.toString());
            passwordConnection.request();
    }

    public void updateTimeZone(HashMap timeZoneHashMap) {
        new GCMGetTokenAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, timeZoneHashMap);
    }

    private void updateTimeZoneWithDeviceId(HashMap body) {
        JSONObject json = new JSONObject(body);
        Log.i(Constants.TAG, "ProfileController.updateTimeZone: " + json.toString());

        UpdateTimeZoneConnection updateTimeZoneConnection = new UpdateTimeZoneConnection(getContext(),this);
        updateTimeZoneConnection.setPayLoad(json.toString());
        updateTimeZoneConnection.request();
    }

    public HashMap getProfileHashMap(UserProfile userProfile)
    {
        HashMap<String, String> body = new HashMap<String, String>();
        if(userProfile.getFirstName() != null) body.put("firstName",userProfile.getFirstName() );
        if(userProfile.getLastName()  != null) body.put("lastName",userProfile.getLastName() );
        if(userProfile.getCompany()  != null) body.put("company",userProfile.getCompany() );
        if(userProfile.getPosition() != null) body.put("position",userProfile.getPosition());
        if(userProfile.getOfficeLocation() != null) body.put("officeLocation",userProfile.getOfficeLocation());
        if(userProfile.getTimezone() != null && !userProfile.getTimezone().equals("")) body.put("timeZone",userProfile.getTimezone());
        return body;
    }

    public void logoutToAPI()
    {
        Realm realm = Realm.getDefaultInstance();
        try {
            UserProfile userProfile = mRealmProfileTransactions.getUserProfile(profileId, realm);
            String jsonEmails = userProfile.getEmails();
            if (jsonEmails == null || jsonEmails.length() == 0) return;

            JSONArray jsonArray = new JSONArray(jsonEmails);
            String email = ((JSONObject)jsonArray.get(0)).getString("email");

            new LogoutProfileAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    Constants.PROFILE_API_LOGOUT, email);

        } catch (Exception e) {
            Log.e(Constants.TAG, "ProfileController.logoutToAPI: ",e);
            Crashlytics.logException(e);
        }
        finally {
            if (realm != null)
               realm.close();
        }
    }

    public void getProfileCallback(String json) {
        Log.i(Constants.TAG, "ProfileController.getProfileCallback: ");
        boolean isUserProfileReceived = false;
        try {
            if (json != null && json.length() > 0) {
                JSONObject jsonResponse = new JSONObject(json);

                userProfile = mapUserProfile(jsonResponse);
                mRealmProfileTransactions.insertUserProfile(userProfile, null);
                if(userProfile != null) {
                    isUserProfileReceived = true;
                }
            }
            else {
                ((IProfileConnectionCallback) this.getConnectionCallback()).onProfileConnectionError();
            }
        } catch (Exception e) {
            Log.w(Constants.TAG, "ProfileController.onConnectionComplete: " +
                    "Exception while parsing profile received",e);
        }

        if (isUserProfileReceived) {
            ((IProfileConnectionCallback) this.getConnectionCallback()).onProfileReceived(userProfile);
        }
    }

    public class LogoutProfileAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            Log.i(Constants.TAG, "LogoutProfileAsyncTask.doInBackground: START");
            Response response;
            String jsonResp = null;

            try {
                String json = "{\"userId\":\""+params[1]+"\"," +
                        "\"deviceId\":\""+Utils.getGCMToken(getContext())+"\"}";
                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(
                        MediaType.parse(Utils.getHttpHeaderContentType()), json);
                Request request = new Request.Builder()
                        .url("https://" + EndpointWrapper.getBaseURL() +
                                params[0])
                        .addHeader(Constants.API_HTTP_HEADER_VERSION,
                                Utils.getHttpHeaderVersion(getContext()))
                        .addHeader(Constants.API_HTTP_HEADER_CONTENTTYPE,
                                Utils.getHttpHeaderContentType())
                        .addHeader(Constants.API_HTTP_HEADER_AUTHORIZATION,
                                Utils.getHttpHeaderAuth(getContext()))
                        .post(body)
                        .build();

                response = client.newCall(request).execute();
                jsonResp = response.body().string();

            } catch (Exception e) {
                Log.e(Constants.TAG, "LogoutProfileAsyncTask.doInBackground: ",e);
                Crashlytics.logException(e);
            }

            Log.i(Constants.TAG, "LogoutProfileAsyncTask.doInBackground: END");

            return jsonResp;
        }

        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);

            SharedPreferences sp = mContext.getSharedPreferences(
                    Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
            String profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);

            //Remove cookies if Sales Force login
            Utils.removeCookies();

            //Reset user security data
            UserSecurity.resetTokens(getActivity());

            //Reset profile data
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(Constants.ACCESS_TOKEN_SHARED_PREF);
            editor.remove(Constants.PROFILE_ID_SHARED_PREF);
            editor.apply();

            //Remove User from DB
            if(profileId!=null) {
                RealmProfileTransactions profileTx = new RealmProfileTransactions();
                profileTx.removeUserProfile(profileId, null);
            }

            XMPPTransactions.disconnectMsgServerSession();

            //Go to login page as a new task
            Intent in = new Intent(mContext, LoginSignupActivity.class);
            in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(in);
        }
    }

    private class GCMGetTokenAsyncTask extends AsyncTask<Object, Void, Object> {
        @Override
        protected Object doInBackground(Object... params) {
            HashMap body = (HashMap)params[0];

            String token = Utils.getGCMToken(getContext());
            if(token!=null)
                body.put("deviceId", token);

            return body;
        }

        @Override
        protected void onPostExecute(Object obj) {
            HashMap body = (HashMap)obj;
            updateTimeZoneWithDeviceId(body);
        }
    }

}
