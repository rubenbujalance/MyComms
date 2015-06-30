package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.util.Log;

import com.framework.library.connection.HttpConnection;
import com.framework.library.model.ConnectionResponse;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.RecentContactsReceivedEvent;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.DownloadImagesAsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.realm.Realm;
import model.Contact;

public class ContactSearchController extends BaseController {

    private final Realm mRealm;
    private Context mContext;
    private String mProfileId;
    private ContactSearchConnection mContactSearchConnection;
    private JSONObject mJSONRecents;
    private String idList = "";
    private int offsetPaging = 0;

    public ContactSearchController(Context appContext, Realm realm, String profileId) {
        super(appContext);
        this.mRealm = realm;
        this.mContext = appContext;
        this.mProfileId = profileId;
    }

    public void getContactById(JSONObject jsonObject) {
        Log.i(Constants.TAG, "ContactSearchController.getRecentList: ");
        if(mContactSearchConnection != null){
            mContactSearchConnection.cancel();
        }
        mJSONRecents = jsonObject;
        int method = HttpConnection.GET;
        idList = getContactIdList();
        if (idList!=null && !idList.equals("")) {
            String apiCall = Constants.CONTACT_API_GET_CONTACTS_IDS + idList;
            //Get all Contacts related to Recents
            mContactSearchConnection = new ContactSearchConnection(mContext, this, method, apiCall);
            mContactSearchConnection.request();
        }
    }

    private void getContactByIdPagination(){
        int method = HttpConnection.GET;
        if (idList!=null && !idList.equals("")) {
            String apiCall = Constants.CONTACT_API_GET_CONTACTS_IDS + idList + "&o=" + offsetPaging;
            mContactSearchConnection = new ContactSearchConnection(mContext, this, method, apiCall);
            mContactSearchConnection.request();
        }
    }

    private String getContactIdList() {
        Log.i(Constants.TAG, "ContactSearchController.getContactIdList: ");
        String ids = "";
        try {
            JSONArray jsonArray = mJSONRecents.getJSONArray(Constants.CONTACT_RECENTS);
            JSONObject jsonObject;
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getString(Constants.CONTACT_ID)!=null
                        && !jsonObject.getString(Constants.CONTACT_ID).equals("")){
                    if(i>0) ids += ",";
                    ids += jsonObject.getString(Constants.CONTACT_ID);
                }
            }
        } catch (JSONException e){
            Log.e(Constants.TAG, "ContactSearchController.getContactIdList: ", e);
            return null;
        }
        return ids;
    }

    @Override
    public void onConnectionComplete(ConnectionResponse response) {
        super.onConnectionComplete(response);
        Log.i(Constants.TAG, "ContactSearchController.onConnectionComplete: ");
        String result = response.getData().toString();
        if (result != null && result.trim().length()>0) {
            try {
                //Check pagination
                JSONObject jsonResponse = new JSONObject(result);
                ContactController contactController = new ContactController(mContext, mRealm, mProfileId);
                ArrayList<Contact> contactArrayList = contactController.insertContactListInRealm(jsonResponse);

                JSONObject jsonPagination = jsonResponse.getJSONObject(Constants.CONTACT_PAGINATION);
                if (jsonPagination.getBoolean(Constants.CONTACT_PAGINATION_MORE_PAGES)) {
                    int pageSize = jsonPagination.getInt(Constants.CONTACT_PAGINATION_PAGESIZE);
                    offsetPaging = offsetPaging + pageSize;
                    getContactByIdPagination();
                } else {
                    offsetPaging = 0;
                    contactController.insertRecentContactInRealm(mJSONRecents);
                    //Show Recents on Dashboard
                    BusProvider.getInstance().post(new RecentContactsReceivedEvent());
                    new DownloadImagesAsyncTask(mContext, contactArrayList).execute();
                }
            } catch (JSONException e) {
                Log.e(Constants.TAG, "ContactSearchController.onConnectionComplete: ", e);
            }
        }
    }
}