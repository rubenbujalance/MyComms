package com.vodafone.mycomms.contacts.connection;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.framework.library.exception.ConnectionException;
import com.framework.library.model.ConnectionResponse;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.connection.IConnectionCallback;
import com.vodafone.mycomms.realm.RealmAvatarTransactions;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.InternalContactSearch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import io.realm.Realm;
import model.Contact;
import model.ContactAvatar;

/**
 * Created by str_oan on 09/06/2015.
 */
public class SearchController extends BaseController
{
    private SearchConnection searchConnection;
    private Realm mRealm;
    private Context mContext;
    private RealmContactTransactions realmContactTransactions;
    private InternalContactSearch internalContactSearch;
    private String apiCall;
    private int search = Constants.CONTACTS_ALL;
    private int offsetPaging = 0;


    public SearchController(Activity activity, Realm realm, String profileId) {
        super(activity);
        this.mRealm = realm;
        this.mContext = activity;
        realmContactTransactions = new RealmContactTransactions(realm, profileId);
        internalContactSearch = new InternalContactSearch(activity);
    }

    public SearchController(Fragment fragment, Realm realm, String profileId) {
        super(fragment);
        this.mRealm = realm;
        this.mContext = fragment.getActivity();
        realmContactTransactions = new RealmContactTransactions(realm, profileId);
    }


    @Override
    public void onConnectionComplete(ConnectionResponse response)
    {
        super.onConnectionComplete(response);
        boolean morePages = false;
        Log.i(Constants.TAG, "SearchController.onConnectionComplete: init: " + apiCall + ", url=" +
                response.getUrl());
        String result = response.getData().toString();
        JSONObject jsonResponse;

        ArrayList<Contact> realmContactList = new ArrayList<>();

        if (result != null && result.trim().length()>0)
        {

            try
            {
                jsonResponse = new JSONObject(result);
                JSONObject jsonPagination = jsonResponse.getJSONObject(Constants.CONTACT_PAGINATION);
                if (jsonPagination.getBoolean(Constants.CONTACT_PAGINATION_MORE_PAGES))
                {
                    morePages = true;
                    offsetPaging = offsetPaging + 1;
                    search = Constants.CONTACTS_ALL;
                }

                realmContactList = getSearchedContacts(jsonResponse);



            } catch (Exception e) {
                Log.e(Constants.TAG, "SearchController.onConnectionComplete: contacts ", e);
            }

        }

        if (this.getConnectionCallback() != null && this.getConnectionCallback()
                instanceof ISearchConnectionCallback)
        {
            ((ISearchConnectionCallback) this.getConnectionCallback())
                    .onSearchContactsResponse(realmContactList, morePages,
                            offsetPaging);
        }
    }

    @Override
    public void onConnectionError(ConnectionException e) {
        Log.e(Constants.TAG, "SearchController -> onConnectionError() " + e.toString());
        super.onConnectionError(e);
    }

    @Override
    public IConnectionCallback getConnectionCallback() {
        return super.getConnectionCallback();
    }


    public void getContactList(String api){
        Log.i(Constants.TAG, "SearchController.getContactList: " + api);
        if(searchConnection != null){
            searchConnection.cancel();
        }
        apiCall = api;
        searchConnection = new SearchConnection(getContext(), this, apiCall);
        searchConnection.request();
    }

    private ArrayList<Contact> getSearchedContacts(JSONObject jsonObject)
    {
        ArrayList<Contact> realmContactList = new ArrayList<>();

        try
        {
            Log.i(Constants.TAG, "SearchController.getSearchedContacts: ");
            JSONArray jsonArray = jsonObject.getJSONArray(Constants.CONTACT_DATA);
            Contact contact;
            boolean doRefreshAdapter;

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                contact = mapContact(jsonObject);
                realmContactList.add(contact);
                doRefreshAdapter = (i==jsonArray.length()-1);
                updateContactAvatar(contact, doRefreshAdapter);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            Log.e(Constants.TAG, "SearchController.getSearchedContacts: " + e.toString());
            return null;
        }
        return realmContactList;
    }

    private void updateContactAvatar (Contact contact, boolean doRefreshAdapter)
    {
        try {
            if (contact.getAvatar()==null || contact.getAvatar().length()==0)
                return;

            RealmAvatarTransactions realmAvatarTransactions = new RealmAvatarTransactions(mRealm);
            ContactAvatar avatar = realmAvatarTransactions.getContactAvatarByContactId(contact.getId());
            if (avatar == null || avatar.getUrl().compareTo(contact.getAvatar()) != 0) {
                String filename = "avatar_" + contact.getId() + ".jpg";

                new DownloadAvatars().execute(contact.getAvatar(), filename);

                if (avatar == null)
                {
                    avatar = new ContactAvatar(contact.getId(), contact.getAvatar(), filename);
                }
                else
                {
                    mRealm.beginTransaction();
                    avatar.setUrl(contact.getAvatar());
                    mRealm.commitTransaction();
                }

                realmAvatarTransactions.insertAvatar(avatar);
            }
        } catch (Exception ex) {
            Log.e(Constants.TAG, "ContactController.updateContactAvatar: ", ex);
        }
    }

    class DownloadAvatars extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... aurl) {
            try {
                URL url = new URL(aurl[0]);
                String avatarFileName = aurl[1];
                String dir = Constants.CONTACT_AVATAR_DIR;

                File file = new File(mContext.getFilesDir() + dir);
                if (file.exists()) {
                    file.delete();
                }
                file.mkdirs();
                if (downloadFile(String.valueOf(url), dir, avatarFileName)) {
                    String avatarFile = mContext.getFilesDir() + dir + avatarFileName;
                    Log.i(Constants.TAG, "DownloadAvatars.doInBackground: avatarFile: " + avatarFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(Constants.TAG, "DownloadAvatars.doInBackground: " + e.toString());
            }
            return null;

        }
    }

        public boolean downloadFile(final String path, String dir, String avatarFileName) {
            try {
                URL url = new URL(path);

                URLConnection ucon = url.openConnection();
                ucon.setReadTimeout(5000);
                ucon.setConnectTimeout(10000);

                InputStream is = ucon.getInputStream();
                BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);

                File file = new File(mContext.getFilesDir() + dir + avatarFileName);

                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();

                FileOutputStream outStream = new FileOutputStream(file);
                byte[] buff = new byte[5 * 1024];

                int len;
                while ((len = inStream.read(buff)) != -1) {
                    outStream.write(buff, 0, len);
                }
                outStream.flush();
                outStream.close();
                inStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(Constants.TAG, "DownloadAvatars.downloadFile: " + e.toString());
                return false;
            }

            return true;
        }

    public static Contact mapContact(JSONObject jsonObject){
        Contact contact = new Contact();
        try {
            if (!jsonObject.isNull(Constants.CONTACT_ID)) contact.setId(jsonObject.getString(Constants.CONTACT_ID));
            if (!jsonObject.isNull(Constants.CONTACT_PLATFORM))
                contact.setPlatform(jsonObject.getString(Constants.CONTACT_PLATFORM));
            if (!jsonObject.isNull(Constants.CONTACT_FNAME))
                contact.setFirstName(jsonObject.getString(Constants.CONTACT_FNAME));
            if (!jsonObject.isNull(Constants.CONTACT_LNAME))
                contact.setLastName(jsonObject.getString(Constants.CONTACT_LNAME));
            if (!jsonObject.isNull(Constants.CONTACT_AVATAR)) contact.setAvatar(jsonObject.getString(Constants.CONTACT_AVATAR));
            if (!jsonObject.isNull(Constants.CONTACT_POSITION))
                contact.setPosition(jsonObject.getString(Constants.CONTACT_POSITION));
            if (!jsonObject.isNull(Constants.CONTACT_COMPANY)) contact.setCompany(jsonObject.getString(Constants.CONTACT_COMPANY));
            if (!jsonObject.isNull(Constants.CONTACT_TIMEZONE))
                contact.setTimezone(jsonObject.getString(Constants.CONTACT_TIMEZONE));
            if (!jsonObject.isNull(Constants.CONTACT_LASTSEEN)) contact.setLastSeen(jsonObject.getLong(Constants.CONTACT_LASTSEEN));
            if (!jsonObject.isNull(Constants.CONTACT_OFFICE_LOC))
                contact.setOfficeLocation(jsonObject.getString(Constants.CONTACT_OFFICE_LOC));
            if (!jsonObject.isNull(Constants.CONTACT_PHONES))
                contact.setPhones(jsonObject.getJSONArray(Constants.CONTACT_PHONES).toString());
            if (!jsonObject.isNull(Constants.CONTACT_EMAILS))
                contact.setEmails(jsonObject.getJSONArray(Constants.CONTACT_EMAILS).toString());
            if (!jsonObject.isNull(Constants.CONTACT_AVAILABILITY))
                contact.setAvailability(jsonObject.getString(Constants.CONTACT_AVAILABILITY));
            if (!jsonObject.isNull(Constants.CONTACT_PRESENCE))
                contact.setPresence(jsonObject.getString(Constants.CONTACT_PRESENCE));
            if (!jsonObject.isNull(Constants.CONTACT_COUNTRY))
                contact.setCountry(jsonObject.getString(Constants.CONTACT_COUNTRY));
        }catch (JSONException e){
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactDBController.mapContact: " + e.toString());
        }
        return contact;
    }



    /*
    private ArrayList<Contact> insertContactListInRealm(JSONObject jsonObject) {
        ArrayList<Contact> realmContactList = new ArrayList<>();

        try {
            Log.i(Constants.TAG, "ContactController.insertContactListInRealm: ");
            JSONArray jsonArray = jsonObject.getJSONArray(Constants.CONTACT_DATA);
            Contact contact;
            boolean doRefreshAdapter;

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                contact = mapContact(jsonObject);
                realmContactList.add(contact);
                doRefreshAdapter = (i==jsonArray.length()-1);
                updateContactAvatar(contact, doRefreshAdapter);
            }
            RealmContactTransactions realmContactTransactions = new RealmContactTransactions(mRealm);
            realmContactTransactions.insertContactList(realmContactList);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactController.insertContactListInRealm: " + e.toString());
            return null;
        }
        return realmContactList;
    }
    */

}
