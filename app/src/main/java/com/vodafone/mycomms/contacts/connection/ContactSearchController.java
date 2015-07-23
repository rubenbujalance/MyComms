package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.framework.library.connection.HttpConnection;
import com.framework.library.exception.ConnectionException;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.RecentContactsReceivedEvent;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ContactSearchController extends BaseController {

    private Context mContext;
    private String mProfileId;
    private ContactSearchConnection mContactSearchConnection;
    private JSONObject mJSONRecents;
    private String idList = "";
    private int offsetPaging = 0;
    private ContactsController contactsController;

    public ContactSearchController(Context appContext, String profileId) {
        super(appContext);
        this.mContext = appContext;
        this.mProfileId = profileId;
        contactsController = new ContactsController(mContext, mProfileId);
    }

    public void getContactById(JSONObject jsonObject) {
        Log.i(Constants.TAG, "ContactSearchController.getContactById: ");
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

    private String getContactIdList() {
        Log.i(Constants.TAG, "ContactSearchController.getContactIdList: ");
        String ids = "";
        try {
            if(mJSONRecents!=null && mJSONRecents.length()>0) {
                JSONArray jsonArray = mJSONRecents.getJSONArray(Constants.CONTACT_RECENTS);
                JSONObject jsonObject;
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString(Constants.CONTACT_ID) != null
                            && !jsonObject.getString(Constants.CONTACT_ID).equals("")) {
                        if (i > 0) ids += ",";
                        ids += jsonObject.getString(Constants.CONTACT_ID);
                    }
                }
            }
        } catch (JSONException e){
            Log.e(Constants.TAG, "ContactSearchController.getContactIdList: ", e);
            return null;
        }
        return ids;
    }

//    @Override
//    public void onConnectionComplete(ConnectionResponse response) {
//        super.onConnectionComplete(response);
//        Log.i(Constants.TAG, "ContactSearchController.onConnectionComplete: ");
//        String result = response.getData().toString();
//        if (result != null && result.trim().length()>0) {
//            try {
//                //Check pagination
//                JSONObject jsonResponse = new JSONObject(result);
//                ArrayList<Contact> contactArrayList = contactsController.insertContactListInRealm(jsonResponse);
//
//                contactsController.insertRecentContactInRealm(mJSONRecents);
//                //Show Recents on Dashboard
//                BusProvider.getInstance().post(new RecentContactsReceivedEvent());
////                new DownloadImagesAsyncTask(mContext, contactArrayList).execute();
//            } catch (JSONException e) {
//                Log.e(Constants.TAG, "ContactSearchController.onConnectionComplete: ", e);
//            }
//        }
//    }

    public void contactsByIdsCallback(String json) {
        Log.i(Constants.TAG, "ContactSearchController.contactsByIdsCallback: ");
//        String result = response.getData().toString();
        if (json != null && json.trim().length()>0) {
            try {
                //Check pagination
                JSONObject jsonResponse = new JSONObject(json);

                ContactsController contactsController = new ContactsController(mContext, mProfileId);
                contactsController.insertContactListInRealm(jsonResponse);
                contactsController.closeRealm();
                contactsController = new ContactsController(mContext, mProfileId);
                contactsController.insertRecentContactInRealm(mJSONRecents);
                contactsController.closeRealm();
                BusProvider.getInstance().post(new RecentContactsReceivedEvent());
                //Show Recent Contacts
//                BusProvider.getInstance().post(new RecentContactsReceivedEvent());
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
            String json = null;

            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://" + EndpointWrapper.getBaseURL() +
                                params[0])
                        .addHeader(Constants.API_HTTP_HEADER_VERSION,
                                Utils.getHttpHeaderVersion(mContext))
                        .addHeader(Constants.API_HTTP_HEADER_CONTENTTYPE,
                                Utils.getHttpHeaderContentType())
                        .addHeader(Constants.API_HTTP_HEADER_AUTHORIZATION,
                                Utils.getHttpHeaderAuth(mContext))
                        .build();

                Response response = client.newCall(request).execute();

                json = response.body().string();

            } catch (Exception e) {
                Log.e(Constants.TAG, "GetContactsByIdsAsyncTask.doInBackground: ",e);
            }

            return json;
        }

        @Override
        protected void onPostExecute(String json) {
            contactsByIdsCallback(json);
        }
    }

    public void closeRealm()
    {
        contactsController.closeRealm();
    }
}