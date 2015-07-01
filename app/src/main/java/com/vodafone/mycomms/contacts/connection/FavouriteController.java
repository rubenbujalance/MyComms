package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.framework.library.connection.HttpConnection;
import com.framework.library.model.ConnectionResponse;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.RefreshFavouritesEvent;
import com.vodafone.mycomms.events.SetContactListAdapterEvent;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONObject;

import java.util.HashMap;

import io.realm.Realm;

public class FavouriteController  extends BaseController {
    private Context mContext;
    private Realm mRealm;
    private RealmContactTransactions realmContactTransactions;
    private FavouriteConnection mFavouriteConnection;
    private String apiCall;
    private String mProfileId;
    private int method;
    private HashMap body;

    public FavouriteController(Context context, Realm realm, String profileId) {
        super(context);
        this.mRealm = realm;
        this.mContext = context;
        this.mProfileId = profileId;
        realmContactTransactions = new RealmContactTransactions(realm, mProfileId);
    }

    public void getFavouritesList(String api){
        Log.i(Constants.TAG, "FavouriteController.getFavouritesList: ");
        apiCall = api;
        method = HttpConnection.GET;
        apiCall = api;
        new FavouritesAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                (String)apiCall);
    }

    public void manageFavourite(String contactId){
        Log.i(Constants.TAG, "FavouriteController.manageFavourite: ");
        JSONObject json = null;
        body = new HashMap<>();
        if(mFavouriteConnection != null){
            mFavouriteConnection.cancel();
        }
        if (realmContactTransactions.deleteFavouriteContact(contactId)){
            //Delete favourite
            method = HttpConnection.DELETE;
            apiCall = Constants.CONTACT_API_DEL_FAVOURITE;
            body.put("", "");
            json = new JSONObject();
            apiCall = apiCall + contactId;
            String test = "{}";
            mFavouriteConnection = new FavouriteConnection(getContext(), this, apiCall, method);
            mFavouriteConnection.setPayLoad(test);
            mFavouriteConnection.request();
            //HashMap<String,Object> params = new HashMap<>();
            //params.put("id", contactId);
            //new DeleteFavouriteRecord().execute(params, null);*/
        } else{
            //Add favourite
            method = HttpConnection.POST;
            apiCall = Constants.CONTACT_API_POST_FAVOURITE;
            body.put("id", contactId);
            json = new JSONObject(body);
            mFavouriteConnection = new FavouriteConnection(getContext(), this, apiCall, method);
            mFavouriteConnection.setPayLoad(json.toString());
            mFavouriteConnection.request();
        }
        //TODO: Pending Test
//        new FavouritesAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
//                (String)apiCall, json);
    }

    public boolean contactIsFavourite(String contactId)
    {
        return realmContactTransactions.favouriteContactIsInRealm(contactId);
    }

    public void favouriteListCallback(String json){
        Log.i(Constants.TAG, "FavouriteController.favouriteListCallback " + method);
        if (method == HttpConnection.POST || method == HttpConnection.DELETE) {
            BusProvider.getInstance().post(new RefreshFavouritesEvent());
        } else if (method == HttpConnection.GET){
            try {
                if (json != null && !json.equals("")) {
                    JSONObject jsonResponse = new JSONObject(json);
                    ContactsController contactsController = new ContactsController(mContext, mRealm, mProfileId);
                    contactsController.insertFavouriteContactInRealm(jsonResponse);
                } else {
                    realmContactTransactions.deleteAllFavouriteContacts();
                }
                BusProvider.getInstance().post(new SetContactListAdapterEvent());

                Log.i(Constants.TAG, "FavouriteController.favouriteListCallback: Calling ContactController");
                ContactController contactController = new ContactController(mContext,mRealm,mProfileId);
                contactController.getContactList(Constants.CONTACT_API_GET_CONTACTS);
            } catch (Exception e) {
                Log.e(Constants.TAG, "ContactsController.favouriteListCallback: favourites", e);
            }
        }
    }

    @Override
    public void onConnectionComplete(ConnectionResponse response){
        super.onConnectionComplete(response);
        Log.i(Constants.TAG, "FavouriteController.onConnectionComplete: method " + method);
        String result = response.getData().toString();
        if (method == HttpConnection.POST || method == HttpConnection.POST) {
            BusProvider.getInstance().post(new RefreshFavouritesEvent());
        } else if (method == HttpConnection.GET){
            try {
                if (result != null && !result.equals("")) {
                    JSONObject jsonResponse = new JSONObject(result);
                    ContactsController contactsController = new ContactsController(mContext, mRealm, mProfileId);
                    contactsController.insertFavouriteContactInRealm(jsonResponse);
                } else {
                    realmContactTransactions.deleteAllFavouriteContacts();
                }
                BusProvider.getInstance().post(new SetContactListAdapterEvent());

                Log.i(Constants.TAG, "FavouriteController.onConnectionComplete: Calling ContactController");
                ContactController contactController = new ContactController(mContext,mRealm,mProfileId);
                contactController.getContactList(Constants.CONTACT_API_GET_CONTACTS);
            } catch (Exception e) {
                Log.e(Constants.TAG, "ContactsController.onConnectionComplete: favourites", e);
            }
        }
    }

    public class FavouritesAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            Log.e(Constants.TAG, "FavouritesAsyncTask.doInBackground: START");
            //String jsonBody = params[1];
            String jsonBody;
            Response response = null;
            String json = null;

            try {

                OkHttpClient client = new OkHttpClient();
                Request request = null;
                if (method == HttpConnection.GET) {
                    request = new Request.Builder()
                            .url("https://" + EndpointWrapper.getBaseURL() +
                                    params[0])
                            .addHeader(Constants.API_HTTP_HEADER_VERSION,
                                    Utils.getHttpHeaderVersion(mContext))
                            .addHeader(Constants.API_HTTP_HEADER_CONTENTTYPE,
                                    Utils.getHttpHeaderContentType())
                            .addHeader(Constants.API_HTTP_HEADER_AUTHORIZATION,
                                    Utils.getHttpHeaderAuth(mContext))
                            .build();
                } if (method == HttpConnection.POST || method == HttpConnection.POST) {
                    jsonBody = params[1];
                    MediaType JSON
                            = MediaType.parse("application/json; charset=utf-8");
                    RequestBody body = RequestBody.create(JSON, jsonBody);
                    request = new Request.Builder()
                            .url("https://" + EndpointWrapper.getBaseURL() +
                                    params[0])
                            .addHeader(Constants.API_HTTP_HEADER_VERSION,
                                    Utils.getHttpHeaderVersion(mContext))
                            .addHeader(Constants.API_HTTP_HEADER_CONTENTTYPE,
                                    Utils.getHttpHeaderContentType())
                            .addHeader(Constants.API_HTTP_HEADER_AUTHORIZATION,
                                    Utils.getHttpHeaderAuth(mContext))
                            .post(body)
                            .build();
                }
                response = client.newCall(request).execute();
                json = response.body().string();


            } catch (Exception e) {
                Log.e(Constants.TAG, "FavouritesAsyncTask.doInBackground: ",e);
            }

            Log.e(Constants.TAG, "FavouritesAsyncTask.doInBackground: END");

            return json;
        }

        @Override
        protected void onPostExecute(String json) {
            favouriteListCallback(json);
        }
    }
}
