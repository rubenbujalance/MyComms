package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.framework.library.connection.HttpConnection;
import com.framework.library.exception.ConnectionException;
import com.framework.library.model.ConnectionResponse;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.RecentContactsReceivedEvent;
import com.vodafone.mycomms.util.Constants;

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
        Log.e(Constants.TAG, "ContactSearchController.getRecentList: ");
//        if(mContactSearchConnection != null){
//            mContactSearchConnection.cancel();
//        }
        mJSONRecents = jsonObject;
        int method = HttpConnection.GET;
        idList = getContactIdList();
        if (idList!=null && idList.length()>0) {
            String apiCall = Constants.CONTACT_API_GET_CONTACTS_IDS + idList;
//            //Get all Contacts related to Recents
//            mContactSearchConnection = new ContactSearchConnection(mContext, this, method, apiCall);
//            mContactSearchConnection.request();
            new GetContactsByIdsAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    (String) apiCall);
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
        Log.e(Constants.TAG, "ContactSearchController.getContactIdList: ");
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
        Log.e(Constants.TAG, "ContactSearchController.onConnectionComplete: ");
        String result = response.getData().toString();
        if (result != null && result.trim().length()>0) {
            try {
                //Check pagination
                JSONObject jsonResponse = new JSONObject(result);
                ContactsController contactsController = new ContactsController(mContext, mRealm, mProfileId);
                ArrayList<Contact> contactArrayList = contactsController.insertContactListInRealm(jsonResponse);

                contactsController.insertRecentContactInRealm(mJSONRecents);
                //Show Recents on Dashboard
                BusProvider.getInstance().post(new RecentContactsReceivedEvent());
                //TODO RBM - For testing!! Restore before commit
//                new DownloadImagesAsyncTask(mContext, contactArrayList).execute();
            } catch (JSONException e) {
                Log.e(Constants.TAG, "ContactSearchController.onConnectionComplete: ", e);
            }
        }
    }

    public void contactsByIdsCallback(String json) {
        Log.e(Constants.TAG, "ContactSearchController.contactsByIdsCallback: ");
//        String result = response.getData().toString();
        if (json != null && json.trim().length()>0) {
            try {
                //Check pagination
                JSONObject jsonResponse = new JSONObject(json);
                ContactsController contactsController = new ContactsController(mContext, mRealm, mProfileId);
                ArrayList<Contact> contactArrayList = contactsController.insertContactListInRealm(jsonResponse);

                contactsController.insertRecentContactInRealm(mJSONRecents);
                //Show Recents on Dashboard
                BusProvider.getInstance().post(new RecentContactsReceivedEvent());
                //TODO RBM - For testing!! Restore before commit
//                new DownloadImagesAsyncTask(mContext, contactArrayList).execute();
            } catch (JSONException e) {
                Log.e(Constants.TAG, "ContactSearchController.onConnectionComplete: ", e);
            }
        }
    }

    @Override
    public void onConnectionError(ConnectionException e) {
        super.onConnectionError(e);
        Log.e(Constants.TAG, "ContactSearchController.onConnectionError: ");
    }

    public class GetContactsByIdsAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            Log.e(Constants.TAG, "GetContactsByIdsAsyncTask.doInBackground: START");

            String json = null;

            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://" + EndpointWrapper.getBaseURL() +
                                params[0])
                        .addHeader("x-mycomms-version", "android/0.1.129")
                        .addHeader("Content-Type", "application/json; charset=utf-8")
                        .addHeader("Authorization", "Bearer c7n_CO-qva-s9vgLfbljwQKLqf9hQVhMFNv" +
                                "WgP-ula0O-SG0DYdXPgI6zt1cgdZuANAgso_cjErpKdLY7YMaTisRp3RVdXsj" +
                                "M_L_YVEp1qUuzAoOSutLIfo5LN5ijbJdCPmhDZydQQxD0NHdHmilwUdikrVBR" +
                                "XAi2GTESdveOkI")
                        .build();

                Response response = client.newCall(request).execute();

                json = response.body().string();

            } catch (Exception e) {
                Log.e(Constants.TAG, "GetContactsByIdsAsyncTask.doInBackground: ",e);
            }

            Log.e(Constants.TAG, "GetContactsByIdsAsyncTask.doInBackground: END");

            return json;
        }

        @Override
        protected void onPostExecute(String json) {
            contactsByIdsCallback(json);
        }
    }
}