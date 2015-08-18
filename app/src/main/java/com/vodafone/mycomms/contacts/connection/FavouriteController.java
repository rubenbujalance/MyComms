package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.util.Log;

import com.framework.library.connection.HttpConnection;
import com.framework.library.model.ConnectionResponse;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.RefreshFavouritesEvent;
import com.vodafone.mycomms.events.SetContactListAdapterEvent;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.OKHttpWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import io.realm.Realm;
import model.Contact;
import model.FavouriteContact;

public class FavouriteController  extends BaseController {
    private Context mContext;
    private RealmContactTransactions realmContactTransactions;
    private FavouriteConnection mFavouriteConnection;
    private String apiCall;
    private String mProfileId;
    private int method;
    private HashMap body;
    private ContactsController contactsController;
    private ContactController contactController;

    public FavouriteController(Context context, String profileId) {
        super(context);
        this.mContext = context;
        this.mProfileId = profileId;
        realmContactTransactions = new RealmContactTransactions(mProfileId);
        contactsController = new ContactsController(mProfileId, mContext);
        contactController = new ContactController(mContext, mProfileId);
    }

    public void getFavouritesList(String api){
        Log.i(Constants.TAG, "FavouriteController.getFavouritesList: ");
        method = HttpConnection.GET;
        apiCall = api;

        try{
            OKHttpWrapper.get(apiCall, mContext, new OKHttpWrapper.HttpCallback() {
                @Override
                public void onFailure(Response response, IOException e) {
                    Log.i(Constants.TAG, "FavouriteController.onFailure:");
                }

                @Override
                public void onSuccess(Response response) {
                    try {
                        String json;
                        if (response.isSuccessful()) {
                            json = response.body().string();
                            if (json != null && json.trim().length() > 0) {
                                JSONObject jsonResponse = new JSONObject(json);
                                contactsController.insertFavouriteContactInRealm(jsonResponse);
                            } else {
                                realmContactTransactions.deleteAllFavouriteContacts(null);
                            }
                            BusProvider.getInstance().post(new SetContactListAdapterEvent());
                        } else {
                            Log.e(Constants.TAG, "FavouriteController.isNOTSuccessful");
                        }
                    } catch (IOException e) {
                        Log.e(Constants.TAG, "FavouriteController.onSuccess: ", e);
                    } catch (JSONException e) {
                        Log.e(Constants.TAG, "FavouriteController.onSuccess: ", e);
                    }
                }
            });
        } catch (Exception e){
            Log.e(Constants.TAG, "FavouriteController.getFavouritesList: ", e);
        }
    }

    private void addFavorite(String api, final String contactId)
    {
        Log.i(FavouriteController.class.getSimpleName(), "addFavorite -> "+ api);
        apiCall = api;
        body.put("id", contactId);
        final JSONObject json = new JSONObject(body);
        try{
            OKHttpWrapper.post(apiCall, mContext, new OKHttpWrapper.HttpCallback() {
                @Override
                public void onFailure(Response response, IOException e) {
                    Log.e(Constants.TAG, "addFavorite.onFailure: ", e);
                }

                @Override
                public void onSuccess(Response response) {
                    Realm realm = Realm.getDefaultInstance();
                    try {
                        Contact contact = realmContactTransactions.getContactById(contactId, realm);
                        FavouriteContact favouriteContact = contactsController.mapContactToFavourite(contact);
                        ArrayList<FavouriteContact> favoriteList = new ArrayList<>();
                        favoriteList.add(favouriteContact);
                        realmContactTransactions.insertFavouriteContactList(favoriteList, realm);
                    } catch (Exception e) {
                        Log.e(Constants.TAG, "addFavorite.onSuccess ", e);
                    } finally {
                        realm.close();
                    }
                }
            }, json);
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "addFavorite: ", e);
        }
    }

    private void deleteFavorite(String api, final String contactId)
    {
        Log.i(FavouriteController.class.getSimpleName(), "addFavorite -> "+ api);
        apiCall = api;
        try{
            OKHttpWrapper.delete(apiCall, mContext, new OKHttpWrapper.HttpCallback() {
                @Override
                public void onFailure(Response response, IOException e)
                {
                    Log.e(Constants.TAG, "deleteFavorite.onFailure: ", e);
                }

                @Override
                public void onSuccess(Response response)
                {
                    Realm realm = Realm.getDefaultInstance();
                    try
                    {
                        realmContactTransactions.deleteFavouriteContact(contactId, null);
                    }
                    catch (Exception e)
                    {
                        Log.e(Constants.TAG, "deleteFavorite.onSuccess ",e);
                    }
                    finally
                    {
                        realm.close();
                    }
                }
            });
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "deleteFavorite: ", e);
        }
    }

    public void manageFavourite(String contactId){
        Log.i(Constants.TAG, "FavouriteController.manageFavourite: ");
        JSONObject json;
        body = new HashMap<>();
        if(mFavouriteConnection != null){
            mFavouriteConnection.cancel();
        }
        if (realmContactTransactions.favouriteContactIsInRealm(contactId, null))
            deleteFavorite((Constants.CONTACT_API_DEL_FAVOURITE + contactId), contactId);
        else
            addFavorite(Constants.CONTACT_API_POST_FAVOURITE, contactId);

        BusProvider.getInstance().post(new SetContactListAdapterEvent());
    }

    public boolean contactIsFavourite(String contactId)
    {
        return realmContactTransactions.favouriteContactIsInRealm(contactId, null);
    }

    public void favouriteListCallback(String json){
        Log.i(Constants.TAG, "FavouriteController.favouriteListCallback " + method);
        if (method == HttpConnection.POST || method == HttpConnection.DELETE) {
            BusProvider.getInstance().post(new SetContactListAdapterEvent());
        } else if (method == HttpConnection.GET){
            try {
                if (json != null && !json.equals("")) {
                    JSONObject jsonResponse = new JSONObject(json);

                    contactsController.insertFavouriteContactInRealm(jsonResponse);
                } else {
                    realmContactTransactions.deleteAllFavouriteContacts(null);
                }
                BusProvider.getInstance().post(new SetContactListAdapterEvent());

                Log.i(Constants.TAG, "FavouriteController.favouriteListCallback: Calling ContactController");

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
        String conection = response.getConnection().toString();
        String URL = response.getUrl();
        if (method == HttpConnection.POST || method == HttpConnection.DELETE) {
            BusProvider.getInstance().post(new RefreshFavouritesEvent());
        } else if (method == HttpConnection.GET){
            try {
                if (result != null && !result.equals("")) {
                    JSONObject jsonResponse = new JSONObject(result);
                    contactsController.insertFavouriteContactInRealm(jsonResponse);
                } else {
                    realmContactTransactions.deleteAllFavouriteContacts(null);
                }
                BusProvider.getInstance().post(new SetContactListAdapterEvent());

                Log.i(Constants.TAG, "FavouriteController.onConnectionComplete: Calling ContactController");
            } catch (Exception e) {
                Log.e(Constants.TAG, "ContactsController.onConnectionComplete: favourites", e);
            }
        }
    }
}
