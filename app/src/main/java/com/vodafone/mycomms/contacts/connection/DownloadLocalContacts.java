package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.search.SearchController;
import com.vodafone.mycomms.util.Constants;

import java.util.ArrayList;

import io.realm.Realm;
import model.Contact;

public class DownloadLocalContacts
{
    private Context mContext;
    private String mProfileId;
    private ArrayList<Contact> contactArrayList;
    private final int PROPORTION_AMOUNT = 100;

    public DownloadLocalContacts(Context context, String profileId){
        this.mContext = context;
        this.mProfileId = profileId;
    }

    /**
     * Download and store local contacts
     * @author str_oan
     *
     */
    public void downloadAndStore()
    {
        new LoadLocalContacts().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Loads local contacts into array list in doInBackground(). Once did, store these contacts
     * into realm in onPostExecute()
     * @author str_oan
     */
    private class LoadLocalContacts extends AsyncTask<Void, Void, String>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params)
        {
            Log.i(Constants.TAG, "DownloadLocalContacts.doInBackground: ");
            SearchController mSearchController = new SearchController(mContext, mProfileId, null);
            contactArrayList = mSearchController.getInternalContactSearch().getAllLocalContact();
            return Integer.toString(contactArrayList.size());
        }

        @Override
        protected void onPostExecute(String amount) {
            super.onPostExecute(amount);
            Log.i("LoadLocalContacts", "onPostExecute: amount of find contacts is " + amount);
            storeContacts();
        }
    }

    /**
     * Store contacts into Realm taking contacts by proportion amount. This will execute batches of
     * async tasks for not overload realm instance
     * @author str_oan
     */
    private void storeContacts()
    {

        ArrayList<Contact> contactProportion;
        int amountOfContacts = this.contactArrayList.size();
        int start = 0;
        while (amountOfContacts > 0)
        {
            if(amountOfContacts > PROPORTION_AMOUNT)
            {
                contactProportion = getContactsByProportion(start, start + PROPORTION_AMOUNT);
                start = start + PROPORTION_AMOUNT;
            }
            else
            {
                contactProportion = getContactsByProportion(start, start + amountOfContacts);
                start = start + PROPORTION_AMOUNT;
            }
            amountOfContacts = amountOfContacts - PROPORTION_AMOUNT;
            new StoreLocalContactsIntoRealm().execute(contactProportion);
        }
    }

    /**
     * Get list of contacts from local contacts list by batches taking start and end position as reference
     * @author str_oan
     * @param start (int) -> start position from list
     * @param end (int) -> end position from list
     * @return (ArrayList) -> list of contacts from given position
     */
    private ArrayList<Contact> getContactsByProportion(int start, int end)
    {
        ArrayList<Contact> contacts = new ArrayList<>();
        for(int i = start; i < end; i++)
        {
            Contact contact = contactArrayList.get(i);
            contacts.add(contact);
        }
        return contacts;
    }

    /**
     * Store local contacts into realm
     * @author str_oan
     */
    private class StoreLocalContactsIntoRealm extends AsyncTask<ArrayList<Contact>, Void, String>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @SafeVarargs
        @Override
        protected final String doInBackground(ArrayList<Contact>... contacts)
        {
            Realm realm = Realm.getDefaultInstance();
            try
            {
                RealmContactTransactions realmContactTransactions =
                        new RealmContactTransactions(mProfileId);
                realmContactTransactions.insertContactList(contacts[0], realm);
                showInsertedIds(contacts[0]);
                return Integer.toString(contacts[0].size());
            }
            catch (Exception e)
            {
                Log.e("DownloadLocalContacts", "doInBackground: ", e);
                Crashlytics.logException(e);
                return null;
            }
            finally
            {
                realm.close();
            }
        }

        @Override
        protected void onPostExecute(String amount) {
            super.onPostExecute(amount);

            Log.i("DownloadLocalContacts", "onPostExecute: amount inserted contacts is: " + amount);
        }
    }

    /**
     * Shows in log local contact ids
     * @author str_oan
     * @param contacts (ArrayList<Contact>) -> list of contacts
     */
    private void showInsertedIds(ArrayList<Contact> contacts)
    {
        for(Contact c : contacts)
        {
            Log.i("showInsertedIds", "Contact id is: " + c.getId());
        }
    }
}