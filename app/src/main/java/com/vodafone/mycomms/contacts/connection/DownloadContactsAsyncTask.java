package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.SetContactListAdapterEvent;
import com.vodafone.mycomms.util.Constants;

import java.util.ArrayList;

import io.realm.Realm;
import model.Contact;

public class DownloadContactsAsyncTask extends AsyncTask<Context, Void, Void> implements IContactsConnectionCallback {
    private String mProfileId;
    private Realm realm;
    private ContactsController mContactsController;
    private String apiCall;
    private  Context mContext;

    @Override
    protected Void doInBackground(Context... params) {
        mContext = params[0];
        return null;
    }

    @Override
    protected void onPostExecute(Void _void) {
        super.onPostExecute(_void);
        Log.i(Constants.TAG, "DownloadContactsAsyncTask.doInBackground: ");
        SharedPreferences sp = mContext.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        mProfileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");
        realm = Realm.getInstance(mContext);
        mContactsController = new ContactsController(mContext,realm, mProfileId);
        apiCall = Constants.CONTACT_API_GET_CONTACTS;
        mContactsController.getContactList(apiCall);
        mContactsController.setConnectionCallback(this);
    }

    @Override
    public void onContactsResponse(ArrayList<Contact> contactList, boolean morePages, int offsetPaging) {
        Log.i(Constants.TAG, "DownloadContactsAsyncTask.onContactsResponse: " + apiCall);

        if(apiCall.equals(Constants.CONTACT_API_GET_FAVOURITES)) {
            //setListsAdapter();
            BusProvider.getInstance().post(new SetContactListAdapterEvent());

        }else if(apiCall.equals(Constants.CONTACT_API_GET_CONTACTS)){
            if (morePages){
                apiCall = Constants.CONTACT_API_GET_CONTACTS;
                mContactsController.getContactList(apiCall + "&o=" + offsetPaging);
            } else {
                apiCall = Constants.CONTACT_API_GET_RECENTS;
                mContactsController.getRecentList(apiCall);
            }
        }else if (apiCall.equals(Constants.CONTACT_API_GET_RECENTS)){
            apiCall = Constants.CONTACT_API_GET_FAVOURITES;
            mContactsController.getFavouritesList(apiCall);
        }
    }

    @Override
    public void onRecentContactsResponse() {
        Log.i(Constants.TAG, "DownloadContactsAsyncTask.onRecentContactsResponse: " + apiCall);
        apiCall = Constants.CONTACT_API_GET_FAVOURITES;
        mContactsController.getFavouritesList(apiCall);
    }

    @Override
    public void onConnectionNotAvailable() {
        Log.e(Constants.TAG, "DownloadContactsAsyncTask.onConnectionNotAvailable: ");
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.e(Constants.TAG, "DownloadContactsAsyncTask.onCancelled: ");
    }
}
