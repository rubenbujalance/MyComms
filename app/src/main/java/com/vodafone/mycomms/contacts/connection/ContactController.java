package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.util.Log;

import com.squareup.okhttp.Response;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ContactListReceivedEvent;
import com.vodafone.mycomms.events.SetContactListAdapterEvent;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.OKHttpWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ContactController {
    private Context mContext;
    private int offsetPaging = 0;
    private String mProfileId;
    private ContactsController contactsController;

    public ContactController(Context context, String profileId) {
        this.mContext = context;
        this.mProfileId = profileId;
        contactsController = new ContactsController(mContext, profileId);
    }

    public void getContactList(String api){
        Log.i(Constants.TAG, "ContactController.getContactList: " + api);
//        new ContactAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
//                (String)apiCall);
        try{
            OKHttpWrapper.get(api, mContext, new OKHttpWrapper.HttpCallback() {
                @Override
                public void onFailure(Response response, IOException e) {
                    Log.i(Constants.TAG, "ContactController.onFailure:");
                }

                @Override
                public void onSuccess(Response response) {
                    try {
                        String json;
                        if (response.isSuccessful()) {
                            json = response.body().string();
                            if (json != null && json.trim().length() > 0) {
                                JSONObject jsonResponse = new JSONObject(json);

                                ContactsController contactsController = new ContactsController(mContext, mProfileId);
                                contactsController.insertContactListInRealm(jsonResponse);
                                contactsController.closeRealm();
                                //Update Contact List View on every pagination
                                BusProvider.getInstance().post(new SetContactListAdapterEvent());

                                JSONObject jsonPagination = jsonResponse.getJSONObject(Constants.CONTACT_PAGINATION);
                                if (jsonPagination.getBoolean(Constants.CONTACT_PAGINATION_MORE_PAGES)) {
                                    int pageSize = jsonPagination.getInt(Constants.CONTACT_PAGINATION_PAGESIZE);
                                    offsetPaging = offsetPaging + pageSize;
                                    getContactList(Constants.CONTACT_API_GET_CONTACTS + "&o=" + offsetPaging);
                                } else {
                                    offsetPaging = 0;
                                    //Bus Event Post when contacts have been received
                                    BusProvider.getInstance().post(new ContactListReceivedEvent());
                                }
                            }
                        } else {
                            Log.e(Constants.TAG, "ContactController.isNOTSuccessful");
                        }
                    } catch (IOException e) {
                        Log.e(Constants.TAG, "ContactController.onSuccess: ", e);
                    } catch (JSONException e) {
                        Log.e(Constants.TAG, "ContactController.onSuccess: ", e);
                    }
                }
            });
        } catch (Exception e){
            Log.e(Constants.TAG, "ContactController.getContactList: ", e);
        }
    }

//    public void contactListCallback(String json) {
//        Log.i(Constants.TAG, "ContactController.contactListCallback: ");
//
//        try {
//            if (json != null && json.trim().length() > 0) {
//                try {
//                    JSONObject jsonResponse = new JSONObject(json);
//
//                    contactsController.insertContactListInRealm(jsonResponse);
//
//                    //Update Contact List View on every pagination
//                    BusProvider.getInstance().post(new SetContactListAdapterEvent());
//
//                    JSONObject jsonPagination = jsonResponse.getJSONObject(Constants.CONTACT_PAGINATION);
//                    if (jsonPagination.getBoolean(Constants.CONTACT_PAGINATION_MORE_PAGES)) {
//                        int pageSize = jsonPagination.getInt(Constants.CONTACT_PAGINATION_PAGESIZE);
//                        offsetPaging = offsetPaging + pageSize;
//                        getContactList(Constants.CONTACT_API_GET_CONTACTS + "&o=" + offsetPaging);
//                    } else {
//                        offsetPaging = 0;
//                        //Bus Event Post when contacts have been received
//                        BusProvider.getInstance().post(new ContactListReceivedEvent());
//                    }
//
//                } catch (JSONException e) {
//                    Log.e(Constants.TAG, "ContactController.contactListCallback: ", e);
//                }
//            }
//        } catch (Exception e) {
//            Log.e(Constants.TAG, "ContactController.contactListCallback: ",e);
//        }
//    }

//    public class ContactAsyncTask extends AsyncTask<String, Void, String> {
//        @Override
//        protected String doInBackground(String... params) {
//            Response response = null;
//            String json = null;
//
//            try {
//                OkHttpClient client = new OkHttpClient();
//                Request request = new Request.Builder()
//                        .url("https://" + EndpointWrapper.getBaseURL() +
//                                params[0])
//                        .addHeader(Constants.API_HTTP_HEADER_VERSION,
//                                Utils.getHttpHeaderVersion(mContext))
//                        .addHeader(Constants.API_HTTP_HEADER_CONTENTTYPE,
//                                Utils.getHttpHeaderContentType())
//                        .addHeader(Constants.API_HTTP_HEADER_AUTHORIZATION,
//                                Utils.getHttpHeaderAuth(mContext))
//                        .build();
//
//                response = client.newCall(request).execute();
//                json = response.body().string();
//
//            } catch (Exception e) {
//                Log.e(Constants.TAG, "ContactAsyncTask.doInBackground: ",e);
//            }
//
//            return json;
//        }
//
//        @Override
//        protected void onPostExecute(String json) {
//            contactListCallback(json);
//        }
//    }

    public void closeRealm()
    {
        contactsController.closeRealm();
    }
}
