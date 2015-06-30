package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.util.Log;

import com.framework.library.connection.HttpConnection;
import com.framework.library.model.ConnectionResponse;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.RefreshFavouritesEvent;
import com.vodafone.mycomms.events.SetContactListAdapterEvent;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;

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

    public FavouriteController(Context context, Realm realm, String profileId) {
        super(context);
        this.mRealm = realm;
        this.mContext = context;
        this.mProfileId = profileId;
        realmContactTransactions = new RealmContactTransactions(realm, mProfileId);
    }

    public void getFavouritesList(String api){
        Log.i(Constants.TAG, "FavouriteController.getFavouritesList: ");
        if(mFavouriteConnection != null){
            mFavouriteConnection.cancel();
        }
        apiCall = api;
        method = HttpConnection.GET;
        mFavouriteConnection = new FavouriteConnection(getContext(), this, apiCall,method );
        mFavouriteConnection.request();
    }

    public void manageFavourite(String contactId){
        Log.i(Constants.TAG, "FavouriteController.manageFavourite: ");
        JSONObject json = null;
        HashMap body = new HashMap<>();
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
    }

    public boolean contactIsFavourite(String contactId)
    {
        return realmContactTransactions.favouriteContactIsInRealm(contactId);
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
                    ContactController contactController = new ContactController(mContext, mRealm, mProfileId);
                    contactController.insertFavouriteContactInRealm(jsonResponse);
                } else {
                    realmContactTransactions.deleteAllFavouriteContacts();
                }
                BusProvider.getInstance().post(new SetContactListAdapterEvent());
            } catch (Exception e) {
                Log.e(Constants.TAG, "ContactController.onConnectionComplete: favourites", e);
            }
        }
    }
}
