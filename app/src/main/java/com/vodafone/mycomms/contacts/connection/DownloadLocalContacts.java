package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.vodafone.mycomms.search.SearchController;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.InternalContactSearch;

import java.util.ArrayList;

import model.Contact;

public class DownloadLocalContacts extends AsyncTask<Void, Void, Void>{
    private InternalContactSearch internalContactSearch;
    private SearchController mSearchController;
    private Context mContext;
    private String mProfileId;
    ArrayList<Contact> contactArrayList;

    public DownloadLocalContacts(Context context, String profileId){
        this.mContext = context;
        this.mProfileId = profileId;
        internalContactSearch = new InternalContactSearch(mContext, mProfileId);

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mSearchController = new SearchController(mContext, mProfileId);
    }

    @Override
    protected Void doInBackground(Void... params) {
    Log.i(Constants.TAG, "DownloadLocalContacts.doInBackground: ");
        contactArrayList = getAllLocalContacts();
        Log.i(Constants.TAG, "DownloadLocalContacts.doInBackground: Size: " + contactArrayList.size());
        for (Contact contact : contactArrayList){
            Log.i(Constants.TAG, "DownloadLocalContacts.doInBackground: Contact Id " + contact.getId());
            Log.i(Constants.TAG, "DownloadLocalContacts.doInBackground: Local Contact " + contact.getFirstName() + " " + contact.getLastName());
            Log.i(Constants.TAG, "DownloadLocalContacts.doInBackground: Local Contact PHOTO " + contact.getAvatar());
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mSearchController.storeContactsIntoRealm(contactArrayList);
        mSearchController.closeRealm();
    }

    public ArrayList<Contact> getAllLocalContacts() {
        Log.i(Constants.TAG, "DownloadLocalContacts.getAllLocalContacts: ");
        return internalContactSearch.getAllLocalContact();
    }
}