package com.vodafone.mycomms.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.realm.RealmContactTransactions;

import java.io.IOException;

public class AvatarSFController {
    private Context mContext;
    final String contactId;
    private String profileId;


    public AvatarSFController(Context context, String contactId, String profileId)
    {
        this.mContext = context;
        this.contactId = contactId;
        this.profileId = profileId;
    }

    public void getSFAvatar(String imageURL){
        Log.i(Constants.TAG, "AvatarSFController.getSFAvatar: " + imageURL);
        new AvatarSFAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                (String) imageURL);
    }

    public class AvatarSFAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            Response response;
            String responseUrl = null;
            if(null != params[0])
            {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(params[0])
                            .addHeader(Constants.API_HTTP_HEADER_VERSION,
                                    Utils.getHttpHeaderVersion(mContext))
                            .addHeader(Constants.API_HTTP_HEADER_CONTENTTYPE,
                                    Utils.getHttpHeaderContentType())
                            .addHeader(Constants.API_HTTP_HEADER_AUTHORIZATION,
                                    Utils.getHttpHeaderAuth(mContext))
                            .build();

                    response = client.newCall(request).execute();
                    if(Integer.toString(response.code()).startsWith("2"))
                        responseUrl = updateEachKindOfContactWithSFAvatarURL(response);

                } catch (IOException e) {
                    Log.e(Constants.TAG, "AvatarSFController.doInBackground: ",e);
                }
            }

            return responseUrl;
        }

        @Override
        protected void onPostExecute(String responseUrl) {
            if (responseUrl!=null){
                Log.i(Constants.TAG, "AvatarSFAsyncTask.onPostExecute: ResponseURL -> " +
                        ""+responseUrl);
            }
            else
                Log.e(Constants.TAG, "AvatarSFAsyncTask.onPostExecute: ERROR on download SF avatar");
        }
    }

    private String updateEachKindOfContactWithSFAvatarURL(Response response)
    {
        RealmContactTransactions realmContactTransactions = new RealmContactTransactions(profileId);
        String responseUrl = response.request().httpUrl().toString();
        realmContactTransactions.setContactSFAvatarURL(contactId, responseUrl);
        realmContactTransactions.closeRealm();
        return responseUrl;
    }


}
