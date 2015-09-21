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
    private String apiCall;
    private String mProfileId;
    private HashMap<String, String> body;
    private ContactsController contactsController;

    public FavouriteController(Context context, String profileId) {
        super(context);
        this.mContext = context;
        this.mProfileId = profileId;
        realmContactTransactions = new RealmContactTransactions(mProfileId);
        contactsController = new ContactsController(mProfileId, mContext);
    }

    public void getFavouritesList(String api){
        Log.i(Constants.TAG, "FavouriteController.getFavouritesList: ");
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
                    } catch (Exception e) {
                        Log.e(Constants.TAG, "FavouriteController.onSuccess: ", e);
                    }
                }
            });
        } catch (Exception e){
            Log.e(Constants.TAG, "FavouriteController.getFavouritesList: ", e);
        }
    }

    private void addFavorite(final String api, final String contactId, final boolean isRetryNeeded)
    {
        Log.i(FavouriteController.class.getSimpleName(), "addFavorite -> " + api);

        Realm realm = Realm.getDefaultInstance();
        try
        {
            Contact contact = RealmContactTransactions.getContactById(contactId, realm);
            FavouriteContact favouriteContact = contactsController.mapContactToFavourite(contact);
            ArrayList<FavouriteContact> favoriteList = new ArrayList<>();
            favoriteList.add(favouriteContact);
            realmContactTransactions.insertFavouriteContactList(favoriteList, realm);

            apiCall = api;
            body.put("id", contactId);
            final JSONObject json = new JSONObject(body);

            try
            {
                OKHttpWrapper.post(apiCall, mContext, new OKHttpWrapper.HttpCallback() {
                    @Override
                    public void onFailure(Response response, IOException e)
                    {
                        Log.e(Constants.TAG, "addFavorite.onFailure: ", e);
                        if(isRetryNeeded)
                        {
                            Log.e(Constants.TAG, "addFavorite.onFailure: trying to perform the second add intent...");
                            addFavorite(api, contactId, false);
                        }
                        else
                            Log.e(Constants.TAG, "addFavorite.onFailure: NO MORE intents, favorite has not been added...");
                    }

                    @Override
                    public void onSuccess(Response response)
                    {
                        Log.i(Constants.TAG, "addFavorite.onSuccess -> Favorite has been added, id: "+contactId);
                    }
                }, json);
            }
            catch (Exception e)
            {
                Log.e(Constants.TAG, "addFavorite: ", e);
            }

        } catch (Exception e) {
            Log.e(Constants.TAG, "addFavorite.onSuccess ", e);
        } finally {
            if(null != realm)
                realm.close();
        }
    }

    private void deleteFavorite(final String api, final String contactId, final boolean isRetryNeeded)
    {
        Log.i(FavouriteController.class.getSimpleName(), "addFavorite -> "+ api);
        apiCall = api;

        try
        {
            realmContactTransactions.deleteFavouriteContact(contactId, null);
            try
            {
                OKHttpWrapper.delete(apiCall, mContext, new OKHttpWrapper.HttpCallback() {
                    @Override
                    public void onFailure(Response response, IOException e)
                    {
                        Log.e(Constants.TAG, "deleteFavorite.onFailure: ", e);
                        if(isRetryNeeded)
                        {
                            Log.e(Constants.TAG, "deleteFavorite.onFailure: trying to perform the second delete intent...");
                            deleteFavorite(api, contactId, false);
                        }
                        else
                            Log.e(Constants.TAG, "deleteFavorite.onFailure: NO MORE intents, favorite has not been deleted...");
                    }

                    @Override
                    public void onSuccess(Response response)
                    {
                        Log.i(Constants.TAG, "deleteFavorite.onSuccess -> Favorite has been deleted, id: " + contactId);
                    }
                });
            }
            catch (Exception e)
            {
                Log.e(Constants.TAG, "deleteFavorite: ", e);
            }
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "deleteFavorite ",e);
        }
    }

    public void manageFavourite(String contactId){
        Log.i(Constants.TAG, "FavouriteController.manageFavourite: ");
        body = new HashMap<>();

        if (RealmContactTransactions.favouriteContactIsInRealm(contactId, null))
            deleteFavorite((Constants.CONTACT_API_DEL_FAVOURITE + contactId), contactId, true);
        else
            addFavorite(Constants.CONTACT_API_POST_FAVOURITE, contactId, true);

        BusProvider.getInstance().post(new SetContactListAdapterEvent());
    }

    public static boolean contactIsFavourite(String contactId)
    {
        return RealmContactTransactions.favouriteContactIsInRealm(contactId, null);
    }
}
