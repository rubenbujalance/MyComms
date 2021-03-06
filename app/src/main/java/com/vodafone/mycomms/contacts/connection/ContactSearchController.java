package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.RecentContactsReceivedEvent;
import com.vodafone.mycomms.realm.RealmLDAPSettingsTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.OKHttpWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.realm.Realm;
import model.GlobalContactsSettings;

public class ContactSearchController {

    private Context mContext;
    private String mProfileId;
    private JSONObject mJSONRecents;
    private ContactsController contactsController;

    public ContactSearchController(Context appContext, String profileId) {
        this.mContext = appContext;
        this.mProfileId = profileId;
        contactsController = new ContactsController(mProfileId, mContext);
    }

    public void getContactById(final JSONObject jsonObject) {
        Log.i(Constants.TAG, "ContactSearchController.getContactById: ");
        mJSONRecents = jsonObject;
        String idList = getContactIdList();
        if (idList !=null && idList.length()>0) {
            String apiCall = buildAPICallRegardingContactsIdList(idList);
            //Get all Contacts related to Recents
            OKHttpWrapper.get(apiCall, mContext, new OKHttpWrapper.HttpCallback() {
                @Override
                public void onFailure(Response response, IOException e)
                {
                   Log.e(Constants.TAG, "ContactSearchController.onFailure:",e);
                }

                @Override
                public void onSuccess(Response response) {
                    try {
                        String json;
                        if (response.isSuccessful()) {
                            json = response.body().string();
                            contactsByIdsCallback(json);
                        } else {
                            Log.e(Constants.TAG, "ContactSearchController.isNOTSuccessful");
                        }
                    } catch (IOException e) {
                        Log.e(Constants.TAG, "ContactSearchController.onSuccess: ", e);
                    }
                }
            });
        }
    }

    private String buildAPICallRegardingContactsIdList(String idList)
    {
        String apiCall = Constants.CONTACT_API_GET_CONTACTS_IDS + idList;
        if(idList.contains(Constants.PLATFORM_GLOBAL_CONTACTS + "_"))
        {
            apiCall = getAPICallForGlobalContacts(idList);
        }
        return apiCall;
    }

    private String getAPICallForGlobalContacts(String idList)
    {
        Realm realm = Realm.getDefaultInstance();
        String apiCall = Constants.CONTACT_API_GET_CONTACTS_IDS + idList;
        try
        {
            GlobalContactsSettings settings = RealmLDAPSettingsTransactions.getSettings(mProfileId, realm);
            String token = settings.getToken();
            String tokenType = settings.getTokenType();
            String url = settings.getUrl();

            if(null != token && token.length() > 0 && null != tokenType && tokenType.length() > 0)
            {
                apiCall = apiCall
                        + "&" + Constants.LDAP_SETTINGS_FIELD_TOKEN_TYPE_HEADER + "=" + tokenType
                        + "&" + Constants.LDAP_SETTINGS_FIELD_TOKEN_HEADER + "=" + token
                        + "&" + Constants.LDAP_SETTINGS_FIELD_URL + "=" + url;
            }
            return apiCall;
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "ContactSearchController.getAPICallForGlobalContacts: ", e);
            Crashlytics.logException(e);
            return apiCall;
        }
        finally {
            realm.close();
        }
    }

    private String getContactIdList() {
        Log.i(Constants.TAG, "ContactSearchController.getContactIdList: ");
        String ids = "";
        Realm realm = Realm.getDefaultInstance();
        try {
            GlobalContactsSettings settings = RealmLDAPSettingsTransactions.getSettings(mProfileId, realm);
            if(mJSONRecents!=null && mJSONRecents.length()>0) {
                JSONArray jsonArray = mJSONRecents.getJSONArray(Constants.CONTACT_RECENTS);
                JSONObject jsonObject;
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString(Constants.CONTACT_ID) != null
                            && !jsonObject.getString(Constants.CONTACT_ID).equals("")) {
                        if(null == settings)
                        {
                            if (!jsonObject.getString(Constants.CONTACT_ID).startsWith(Constants.PLATFORM_GLOBAL_CONTACTS))
                            {
                                if (i > 0) ids += ",";
                                ids += jsonObject.getString(Constants.CONTACT_ID);
                            }
                        }
                        else
                        {
                            if (i > 0) ids += ",";
                            ids += jsonObject.getString(Constants.CONTACT_ID);
                        }
                    }
                }
            }
        } catch (JSONException e){
            Log.e(Constants.TAG, "ContactSearchController.getContactIdList: ", e);
            return null;
        }
        finally {
            if(null != realm)
                realm.close();
        }
        return ids;
    }

    public void contactsByIdsCallback(String json) {
        Log.i(Constants.TAG, "ContactSearchController.contactsByIdsCallback: ");
        if (json != null && json.trim().length()>0) {
            try {
                //Check pagination
                JSONObject jsonResponse = new JSONObject(json);

                contactsController.insertContactListInRealm(jsonResponse);
                contactsController.insertRecentContactInRealm(mJSONRecents);
                BusProvider.getInstance().post(new RecentContactsReceivedEvent());
                //Show Recent Contacts
            } catch (JSONException e) {
                Log.e(Constants.TAG, "ContactSearchController.onConnectionComplete: ", e);
            }
        }
    }
}