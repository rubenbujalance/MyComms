package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.framework.library.connection.HttpConnection;
import com.framework.library.model.ConnectionResponse;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.RecentContactsReceivedEvent;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;

import io.realm.Realm;

public class RecentContactController extends BaseController {

    private final Realm mRealm;
    private Context mContext;private String mProfileId;
    private RecentContactConnection mRecentContactConnection;
    int method;

    public RecentContactController(Context appContext, Realm realm, String profileId) {
        super(appContext);
        this.mRealm = realm;
        this.mContext = appContext;
        this.mProfileId = profileId;
    }

    public void getRecentList() {
        Log.e(Constants.TAG, "RecentContactController.getRecentList: ");
//        if(mRecentContactConnection != null){
//            mRecentContactConnection.cancel();
//        }
        method = HttpConnection.GET;
//        String apiCall = Constants.CONTACT_API_GET_RECENTS;
//        mRecentContactConnection = new RecentContactConnection(mContext, this, method, apiCall);
//        mRecentContactConnection.request();

        new RecentContactsAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                (String)Constants.CONTACT_API_GET_RECENTS);
    }

    public void insertRecent(String contactId, String action){
        Log.i(Constants.TAG, "RecentContactController.insertRecent: ");
        if(mRecentContactConnection != null){
            mRecentContactConnection.cancel();
        }
        JSONObject json;
        HashMap body = new HashMap<>();
        method = HttpConnection.POST;
        String apiCall = Constants.CONTACT_API_POST_RECENTS;
        long timestamp = Calendar.getInstance().getTimeInMillis();
        body.put(Constants.CONTACT_ID, contactId);
        body.put(Constants.CONTACT_RECENTS_ACTION, action);
        body.put(Constants.CONTACT_RECENTS_ACTION_TIME, timestamp);
        json = new JSONObject(body);
        mRecentContactConnection = new RecentContactConnection(mContext, this, method, apiCall);
        mRecentContactConnection.setPayLoad(json.toString());
        mRecentContactConnection.request();
    }

    @Override
    public void onConnectionComplete(ConnectionResponse response) {
        Log.e(Constants.TAG, "RecentContactController.onConnectionComplete: ");

        super.onConnectionComplete(response);
        if (method==HttpConnection.POST) {
            Log.i(Constants.TAG, "RecentContactController.onConnectionComplete: POST");
            String apiCall = Constants.CONTACT_API_GET_RECENTS;
            ContactsController contactsController = new ContactsController(getActivity(), mRealm, mProfileId);
            contactsController.getRecentList(apiCall);
            BusProvider.getInstance().post(new RecentContactsReceivedEvent());
        } else{
            Log.i(Constants.TAG, "RecentContactController.onConnectionComplete: GET");
            String result = response.getData().toString();
            if (result != null && result.trim().length()>0) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    ContactSearchController contactSearchController = new ContactSearchController(mContext,mRealm, mProfileId);
                    contactSearchController.getContactById(jsonResponse);
                } catch (JSONException e){
                    Log.e(Constants.TAG, "RecentContactController.onConnectionComplete: ",e);
                }
            }
        }
    }

    public void recentsListCallback(String json) {
        Log.e(Constants.TAG, "RecentContactController.recentsListCallback: ");

        try {
            if (method == HttpConnection.POST) {
                Log.i(Constants.TAG, "RecentContactController.onConnectionComplete: POST");
                String apiCall = Constants.CONTACT_API_GET_RECENTS;
                ContactsController contactsController = new ContactsController(getActivity(), mRealm, mProfileId);
                contactsController.getRecentList(apiCall);
                BusProvider.getInstance().post(new RecentContactsReceivedEvent());
            } else {
                Log.i(Constants.TAG, "RecentContactController.onConnectionComplete: GET");
//                String result = response.body().string();
                if (json != null && json.trim().length() > 0) {
                    try {
                        JSONObject jsonResponse = new JSONObject(json);
                        ContactSearchController contactSearchController = new ContactSearchController(mContext, mRealm, mProfileId);
                        contactSearchController.getContactById(jsonResponse);
                    } catch (JSONException e) {
                        Log.e(Constants.TAG, "RecentContactController.onConnectionComplete: ", e);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "RecentContactController.onConnectionComplete: ",e);
        }
    }

    public class RecentContactsAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            Log.e(Constants.TAG, "RecentContactsAsyncTask.doInBackground: START");

            Response response = null;
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

                response = client.newCall(request).execute();
                json = response.body().string();

            } catch (Exception e) {
                Log.e(Constants.TAG, "RecentContactsAsyncTask.doInBackground: ",e);
            }

            Log.e(Constants.TAG, "RecentContactsAsyncTask.doInBackground: END");

            return json;
        }

        @Override
        protected void onPostExecute(String json) {
            recentsListCallback(json);
        }
    }
}
