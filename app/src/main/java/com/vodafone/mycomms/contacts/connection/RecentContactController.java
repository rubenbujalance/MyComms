package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.OKHttpWrapper;
import com.vodafone.mycomms.util.UserSecurity;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import model.Contact;

public class RecentContactController {

    private Context mContext;
    private String mProfileId;
    private ContactsController contactsController;
    private ContactSearchController contactSearchController;

    private String URL_SET_RECENT = "https://" + EndpointWrapper.getBaseURL() +
            Constants.CONTACT_API_POST_RECENTS;

    public RecentContactController(Context context, String profileId) {
        this.mContext = context;
        this.mProfileId = profileId;
        contactsController = new ContactsController(mProfileId, mContext);
        contactSearchController = new ContactSearchController(mContext,mProfileId);
    }

    public void getRecentList() {
        Log.i(Constants.TAG, "RecentContactController.getRecentList: ");
        try{
            OKHttpWrapper.get(Constants.CONTACT_API_GET_RECENTS, mContext, new OKHttpWrapper.HttpCallback() {
                @Override
                public void onFailure(Response response, IOException e) {
                    Log.i(Constants.TAG, "RecentContactController.onFailure:");
                }

                @Override
                public void onSuccess(Response response) {
                    try {
                        String json;
                        if (response.isSuccessful()) {
                            json = response.body().string();
                            if (json != null && json.trim().length() > 0) {
                                try {
                                    JSONObject jsonResponse = new JSONObject(json);
                                    contactSearchController.getContactById(jsonResponse);
                                    Log.i(Constants.TAG, "RecentContactController.onSuccess: ");

                                } catch (JSONException e) {
                                    Log.e(Constants.TAG, "RecentContactController.onConnectionComplete: ", e);
                                }
                            }
                        } else {
                            Log.e(Constants.TAG, "RecentContactController.isNOTSuccessful");
                        }
                    } catch (IOException e){
                        Log.e(Constants.TAG, "RecentContactController.onSuccess: ", e);
                    }
                }
            });
        } catch (Exception e){
            Log.e(Constants.TAG, "RecentContactController.getRecentList: ", e);
        }
    }

    public void insertRecent(String contactId, String action){
        insertRecentPOSTOKHttp(contactId, action);
    }

    public void insertRecentOKHttp(String groupChatId, String action)
    {
        Log.i(Constants.TAG, "RecentContactController.insertRecentOKHttp: ");
        new RecentContactsOKHTTPAsyncTask(groupChatId, action).execute();
    }

    public void insertPendingChatsRecent(HashMap<String, Long> recentChatsHashMap){
        insertPendingChatsRecentPOSTOKHttp(recentChatsHashMap);
    }

    public class RecentContactsOKHTTPAsyncTask extends AsyncTask<String, Void, String>
    {
        private String groupChatId;
        private String action;

        public RecentContactsOKHTTPAsyncTask(String groupChatId, String action)
        {
            this.groupChatId = groupChatId;
            this.action = action;
        }
        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                Log.i(Constants.TAG, "RecentContactController.insertRecent: ");
                Request request = createGroupChatRequestForCreation(this.groupChatId, this.action);
                return executeRequest(request);

            }
            catch (Exception e)
            {
                Log.e(Constants.TAG, "RecentContactsOKHTTPAsyncTask.doInBackground: ",e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response)
        {
            Contact contact = new Contact("");
            contact.setId(groupChatId);
            contact.setContactId(groupChatId);
            contact.setProfileId(mProfileId);
            JSONObject jsonObject = createJsonObject(this.groupChatId, this.action);
            contactsController.insertRecentGroupChatIntoRealm(contact, jsonObject);
        }
    }

    public void insertRecentPOSTOKHttp(String contactId, String action)
    {
        new RecentContactsPOSTAsyncTask(contactId, action).execute();
    }

    public void insertPendingChatsRecentPOSTOKHttp(HashMap<String, Long> recentChatsHashMap)
    {
        new RecentPendingChatsRecentPOSTAsyncTask(recentChatsHashMap).execute();
    }

    public class RecentContactsPOSTAsyncTask extends AsyncTask<String, Void, String>
    {
        private String action;
        private String contactId;

        public RecentContactsPOSTAsyncTask(String contactId, String action)
        {
            this.action = action;
            this.contactId = contactId;
        }
        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                String jsonRequest = createdStringBodyForSetRecent(this.contactId,this.action, null);
                Request request = createPOSTRequestForCreation(jsonRequest);
                return executeRequest(request);

            }
            catch (Exception e)
            {
                Log.e(Constants.TAG, "RecentContactsPOSTAsyncTask.doInBackground: ",e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response)
        {
            getRecentList();
        }
    }

    public class RecentPendingChatsRecentPOSTAsyncTask extends AsyncTask<String, Void, String>
    {
        private HashMap<String, Long> recentChatsHashMap;

        public RecentPendingChatsRecentPOSTAsyncTask(HashMap<String, Long> recentChatsHashMap)
        {
            this.recentChatsHashMap = recentChatsHashMap;
        }

        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                String chatId;
                Long timeStamp;
                Request request;
                for (HashMap.Entry<String, Long> recentChat : this.recentChatsHashMap.entrySet())
                {
                    chatId = recentChat.getKey();
                    timeStamp = recentChat.getValue();
                    String jsonRequest = createdStringBodyForSetRecent(chatId, Constants.CONTACTS_ACTION_SMS, timeStamp);
                    request = createPOSTRequestForCreation(jsonRequest);
                    executeRequest(request);
                }
                return null;

            }
            catch (Exception e)
            {
                Log.e(Constants.TAG, "RecentPendingChatsRecentPOSTAsyncTask.doInBackground: ",e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
//            getRecentList();
//            BusProvider.getInstance().post(new RecentContactsReceivedEvent());
            this.recentChatsHashMap.clear();
        }
    }

    public static JSONObject createJsonObject(String groupChatId, String action)
    {
        try
        {
            return new JSONObject(createdStringBodyForSetRecent(groupChatId, action, null));
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "RecentContactsPOSTAsyncTask.createJsonObject: ERROR ", e);
            return null;
        }

    }

    public static String createdStringBodyForSetRecent(String groupChatId, String action, Long timestamp)
    {
        if (timestamp == null)
            timestamp = Calendar.getInstance().getTimeInMillis();

        return "{\"id\":\""
                    + groupChatId + "\","
                    + "\"timestamp\": "+timestamp+","
                    + "\"action\":\""+action+"\"}";
    }


    public Request createGroupChatRequestForCreation(String groupChatId, String action)
    {
        try
        {
            final String authorization = "Authorization";
            final String version_token = "x-mycomms-version";
            final String ACCESS_TOKEN = "Bearer ";

            String jsonRequest = createdStringBodyForSetRecent(groupChatId, action, null);
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            RequestBody requestBody = RequestBody.create(JSON, jsonRequest);
            return new Request.Builder()
                    .addHeader(authorization, ACCESS_TOKEN + UserSecurity.getAccessToken(mContext))
                    .addHeader(version_token, getVersionName())
                    .url(URL_SET_RECENT)
                    .post(requestBody)
                    .build();
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "RecentContactController.createGroupChatRequest: ERROR ", e);
            return null;
        }
    }

    public Request createPOSTRequestForCreation(String jsonRequest)
    {
        try
        {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON, jsonRequest);
            return new Request.Builder()
                    .addHeader(Constants.API_HTTP_HEADER_VERSION,
                            Utils.getHttpHeaderVersion(mContext))
                    .addHeader(Constants.API_HTTP_HEADER_CONTENTTYPE,
                            Utils.getHttpHeaderContentType())
                    .addHeader(Constants.API_HTTP_HEADER_AUTHORIZATION,
                            Utils.getHttpHeaderAuth(mContext))
                    .url(URL_SET_RECENT)
                    .post(requestBody)
                    .build();
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "RecentContactController.createGroupChatRequest: ERROR ", e);
            return null;
        }
    }

    public String executeRequest(Request request)
    {
        try
        {
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            return responseToString(response);
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "RecentContactController.executeRequest: ERROR ", e);
            return null;
        }

    }

    public String responseToString(Response response)
    {
        try
        {
            return response.body().string();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RecentContactController.convertResponseToString: " + "ERROR ",e);
            return null;
        }
    }

    private String getVersionName()
    {
        try
        {
            PackageInfo pinfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            int versionCode = pinfo.versionCode;
            String versionName = pinfo.versionName;

            return "android/"+versionName+"."+versionCode;
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "RecentContactController.getVersionName: ERROR ",e);
            return "";
        }
    }
}
