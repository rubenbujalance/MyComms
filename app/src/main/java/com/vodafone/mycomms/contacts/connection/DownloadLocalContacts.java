package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.vodafone.mycomms.search.SearchController;
import com.vodafone.mycomms.util.Constants;

import java.util.ArrayList;

import io.realm.Realm;
import model.Contact;

public class DownloadLocalContacts extends AsyncTask<Void, Void, Void>{
    private Context mContext;
    private String mProfileId;
    ArrayList<Contact> contactArrayList;

    public DownloadLocalContacts(Context context, String profileId){
        this.mContext = context;
        this.mProfileId = profileId;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected Void doInBackground(Void... params)
    {
        Realm realm = Realm.getDefaultInstance();
        Log.i(Constants.TAG, "DownloadLocalContacts.doInBackground: ");
        SearchController mSearchController = new SearchController(mContext, mProfileId, realm);
        contactArrayList = mSearchController.getInternalContactSearch().getAllLocalContact();
        mSearchController.storeContactsIntoRealm(contactArrayList);
        realm.close();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

    }
}