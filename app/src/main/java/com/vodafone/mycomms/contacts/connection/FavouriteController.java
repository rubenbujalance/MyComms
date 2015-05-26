package com.vodafone.mycomms.contacts.connection;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.framework.library.connection.HttpConnection;
import com.framework.library.model.ConnectionResponse;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UserSecurity;

import org.json.JSONObject;

import java.util.HashMap;

import io.realm.Realm;

public class FavouriteController  extends BaseController {
    private Context mContext;
    private Realm mRealm;
    private RealmContactTransactions realmContactTransactions;
    private FavouriteConnection mFavouriteConnection;
    private String apiCall;

    public FavouriteController(Activity activity, Realm realm) {
        super(activity);
        this.mRealm = realm;
        this.mContext = activity;
        realmContactTransactions = new RealmContactTransactions(realm);
    }

    public FavouriteController(Fragment fragment, Realm realm) {
        super(fragment);
        this.mRealm = realm;
        this.mContext = fragment.getActivity();
        realmContactTransactions = new RealmContactTransactions(realm);
    }

    public void manageFavourite(String contactId){
        Log.i(Constants.TAG, "FavouriteController.addFavourite: ");
        int method;
        JSONObject json = null;
        HashMap body = new HashMap<>();
        if(mFavouriteConnection != null){
            mFavouriteConnection.cancel();
        }
        if (realmContactTransactions.favouriteContactIsInRealm(contactId)){
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
    }

    @Override
    public void onConnectionComplete(ConnectionResponse response){
        Log.i(Constants.TAG, "FavouriteController.onConnectionComplete: ");
        super.onConnectionComplete(response);
        String result = response.getData().toString();

    }

    private class DeleteFavouriteRecord extends AsyncTask<HashMap<String,Object>, Void, HashMap<String,Object>> {
        @Override
        protected HashMap<String,Object> doInBackground(HashMap<String,Object>[] params) {
            APIWrapper apiWrapper = new APIWrapper();
            return apiWrapper.httpDeleteAPI(Constants.CONTACT_API_DEL_FAVOURITE, params[0], params[1], mContext);
        }
        @Override
        protected void onPostExecute(HashMap<String,Object> result) {
            callBackPassCheck(result);
        }
    }

    private void callBackPassCheck(HashMap<String, Object> result) {
        String status = (String)result.get("status");

        try {
            if (status.compareTo("204") != 0) {
                //onLoginError();
                Log.e(Constants.TAG, "FavouriteController.callBackPassCheck: Status204");
            } else if (status.compareTo("500") != 0) {
                //onLoginError();
                Log.e(Constants.TAG, "FavouriteController.callBackPassCheck: Status500");
            } else {
                Log.i(Constants.TAG, "FavouriteController.callBackPassCheck: All Good");
            }
        } catch(Exception ex) {
            Log.e(Constants.TAG, "FavouriteController.callBackPassCheck:" , ex);
        }
    }
}
