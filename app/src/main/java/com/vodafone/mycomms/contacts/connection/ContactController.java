package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UserSecurity;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;

public class ContactController extends BaseController {
    private Context mContext;
    private Realm mRealm;
    private String apiCall;
    private String mProfileId;
    private int offsetPaging = 0;

    public ContactController(Context context, Realm realm, String profileId) {
        super(context);
        this.mRealm = realm;
        this.mContext = context;
        this.mProfileId = profileId;
    }

    public void getContactList(String api){
        Log.i(Constants.TAG, "ContactController.getContactList: " + api);
        apiCall = api;
        new ContactAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                (String)apiCall);
    }

    public void contactListCallback(String json) {
        Log.e(Constants.TAG, "ContactController.contactListCallback: ");

        try {
            if (json != null && json.trim().length() > 0) {
                try {
                    JSONObject jsonResponse = new JSONObject(json);

                    ContactsController contactsController = new ContactsController(mContext, mRealm, mProfileId);
                    contactsController.insertContactListInRealm(jsonResponse);

                    //TODO: Update Contacts on Fragment

                    JSONObject jsonPagination = jsonResponse.getJSONObject(Constants.CONTACT_PAGINATION);
                    if (jsonPagination.getBoolean(Constants.CONTACT_PAGINATION_MORE_PAGES)) {
                        int pageSize = jsonPagination.getInt(Constants.CONTACT_PAGINATION_PAGESIZE);
                        offsetPaging = offsetPaging + pageSize;
                        getContactList(apiCall + "&o=" + offsetPaging);
                    } else {
                        offsetPaging = 0;
                    }

                } catch (JSONException e) {
                    Log.e(Constants.TAG, "ContactController.contactListCallback: ", e);
                }
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "ContactController.contactListCallback2: ",e);
        }
    }

    public class ContactAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            Log.e(Constants.TAG, "ContactAsyncTask.doInBackground: START");

            Response response = null;
            String json = null;

            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://" + EndpointWrapper.getBaseURL() +
                                params[0])
                        .addHeader("x-mycomms-version", "android/0.1.129")
                        .addHeader("Content-Type", "application/json; charset=utf-8")
                        .addHeader("Authorization", "Bearer " +
                                UserSecurity.getAccessToken(mContext))
                        .build();

                response = client.newCall(request).execute();
                json = response.body().string();

            } catch (Exception e) {
                Log.e(Constants.TAG, "ContactAsyncTask.doInBackground: ",e);
            }

            Log.e(Constants.TAG, "ContactAsyncTask.doInBackground: END");

            return json;
        }

        @Override
        protected void onPostExecute(String json) {
            contactListCallback(json);
        }
    }
}
