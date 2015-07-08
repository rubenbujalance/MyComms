package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.vodafone.mycomms.search.SearchController;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.InternalContactSearch;

import java.util.ArrayList;

import io.realm.Realm;
import model.Contact;

public class DownloadLocalContacts extends AsyncTask<Void, Void, Void>{
    private InternalContactSearch internalContactSearch;
    private SearchController mSearchController;
    private Context mContext;
    private String mProfileId;
    private Realm mRealm;
    ArrayList<Contact> contactArrayList;

    public DownloadLocalContacts(Context context, String profileId, Realm realm){
        this.mContext = context;
        this.mProfileId = profileId;
        mRealm = Realm.getInstance(context);
        internalContactSearch = new InternalContactSearch(mContext, mProfileId);
        mSearchController = new SearchController(context, mRealm, profileId);
    }

    @Override
    protected Void doInBackground(Void... params) {
    Log.i(Constants.TAG, "DownloadLocalContacts.doInBackground: ");
        contactArrayList = getAllLocalContacts();
        Log.i(Constants.TAG, "DownloadLocalContacts.doInBackground: Size: " + contactArrayList.size());
        for (Contact contact : contactArrayList){
            Log.i(Constants.TAG, "DownloadLocalContacts.doInBackground: Local Contact " + contact.getFirstName() + " " + contact.getLastName());
            Log.i(Constants.TAG, "DownloadLocalContacts.doInBackground: Local Contact PHOTO " + contact.getAvatar());
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mSearchController.storeContactsIntoRealm(contactArrayList);
        mRealm.close();
    }

    public ArrayList<Contact> getAllLocalContacts() {
        Log.i(Constants.TAG, "DownloadLocalContacts.getAllLocalContacts: ");
        return internalContactSearch.getAllLocalContact();
    }
}