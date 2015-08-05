package com.vodafone.mycomms.search;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.framework.library.exception.ConnectionException;
import com.framework.library.model.ConnectionResponse;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.connection.IConnectionCallback;
import com.vodafone.mycomms.contacts.connection.ISearchConnectionCallback;
import com.vodafone.mycomms.realm.RealmAvatarTransactions;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.InternalContactSearch;
import com.vodafone.mycomms.util.Utils;

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
    private Context mContext;
    private RealmContactTransactions realmContactTransactions;
    private RealmAvatarTransactions realmAvatarTransactions;
    private InternalContactSearch internalContactSearch;
    private String apiCall;
    private int search = Constants.CONTACTS_ALL;
    private int offsetPaging = 0;
    private String mProfileId;
    private Realm realm;

    public SearchController(Context context, String profileId, Realm realm) {
        super(context);
        mContext = context;
        mProfileId = profileId;
        this.realm = realm;
        realmContactTransactions = new RealmContactTransactions(profileId);
        realmAvatarTransactions = new RealmAvatarTransactions();
        internalContactSearch = new InternalContactSearch(mContext, profileId);
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
                    offsetPaging = offsetPaging + 25;
                    search = Constants.CONTACTS_ALL;
                }

                realmContactList = insertContactListInRealm(jsonResponse);

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

    public ArrayList<Contact> insertContactListInRealm(JSONObject jsonObject)
    {
        ArrayList<Contact> realmContactList = new ArrayList<>();
        Realm realm = Realm.getDefaultInstance();
        try {
            Log.i(Constants.TAG, "ContactsController.insertContactListInRealm: ");
            JSONArray jsonArray = jsonObject.getJSONArray(Constants.CONTACT_DATA);
            Contact contact;

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                contact = mapContact(jsonObject, mProfileId, realm);

                realmContactList.add(contact);
            }
            realmContactTransactions.insertContactList(realmContactList, realm);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "SearchController.insertContactListInRealm: " + e.toString());
            realmContactList = null;
        }
        finally {
            realm.close();
        }
        return realmContactList;
    }

    private void updateContactAvatar (Contact contact, boolean doRefreshAdapter)
    {
        Realm realm = Realm.getDefaultInstance();
        try {
            if (contact.getAvatar()==null || contact.getAvatar().length()==0)
                return;

            ContactAvatar avatar = realmAvatarTransactions.getContactAvatarByContactId(contact
                    .getContactId(), realm);
            if (avatar == null || avatar.getUrl().compareTo(contact.getAvatar()) != 0) {
                String filename = "avatar_" + contact.getContactId() + ".jpg";

                new DownloadAvatars().execute(contact.getAvatar(), filename);

                if (avatar == null)
                {
                    avatar = new ContactAvatar(contact.getContactId(), contact.getAvatar(), filename);
                }
                else
                {
                    realmAvatarTransactions.updateAvatarUrlByContactId(
                            contact.getContactId(), contact.getAvatar(), realm);
                }

                realmAvatarTransactions.insertAvatar(avatar, realm);
            }
        } catch (Exception ex) {
            Log.e(Constants.TAG, "ContactsController.updateContactAvatar: ", ex);
        }finally {
            realm.close();
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
            ucon.setReadTimeout(Constants.HTTP_READ_AVATAR_TIMEOUT);
            ucon.setConnectTimeout(10000);

            InputStream is = ucon.getInputStream();
            BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);

            File file = new File(mContext.getFilesDir() + dir, avatarFileName);

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

    public Contact mapContact(JSONObject jsonObject, String profileId, Realm realm){
        Contact contact = new Contact();
        try {
            contact.setProfileId(profileId);
            if (!jsonObject.isNull(Constants.CONTACT_ID)){
                contact.setContactId(jsonObject.getString(Constants.CONTACT_ID));
                contact.setId(profileId + "_" + jsonObject.getString(Constants.CONTACT_ID));
                Log.w(Constants.TAG,"SearchController -> mapContact() has value: "+profileId);
            }
            if (!jsonObject.isNull(Constants.CONTACT_PLATFORM))
                contact.setPlatform(jsonObject.getString(Constants.CONTACT_PLATFORM));
            if (!jsonObject.isNull(Constants.CONTACT_FNAME))
                contact.setFirstName(jsonObject.getString(Constants.CONTACT_FNAME));
            if (!jsonObject.isNull(Constants.CONTACT_LNAME))
                contact.setLastName(jsonObject.getString(Constants.CONTACT_LNAME));
            if (!jsonObject.isNull(Constants.CONTACT_AVATAR))
                contact.setAvatar(jsonObject.getString(Constants.CONTACT_AVATAR));
            if (!jsonObject.isNull(Constants.CONTACT_POSITION))
                contact.setPosition(jsonObject.getString(Constants.CONTACT_POSITION));
            if (!jsonObject.isNull(Constants.CONTACT_COMPANY))
                contact.setCompany(jsonObject.getString(Constants.CONTACT_COMPANY));
            if (!jsonObject.isNull(Constants.CONTACT_TIMEZONE))
                contact.setTimezone(jsonObject.getString(Constants.CONTACT_TIMEZONE));
            if (!jsonObject.isNull(Constants.CONTACT_LASTSEEN))
                contact.setLastSeen(jsonObject.getLong(Constants.CONTACT_LASTSEEN));
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
            contact.setSearchHelper
                    ((
                                    Utils.normalizeStringNFD(contact.getFirstName()) + " " +
                                            Utils.normalizeStringNFD(contact.getLastName()) + " " +
                                            Utils.normalizeStringNFD(contact.getCompany()) + " " +
                                            Utils.normalizeStringNFD(contact.getEmails())).trim()
                    );

            contact.setSortHelper
                    ((
                                    Utils.normalizeStringNFD(contact.getFirstName()) + " " +
                                            Utils.normalizeStringNFD(contact.getLastName()) + " " +
                                            Utils.normalizeStringNFD(contact.getCompany())).trim()
                    );


            if(null != jsonObject.getString(Constants.CONTACT_ID)
                    && null != realmContactTransactions.getContactById(jsonObject.getString
                    (Constants.CONTACT_ID), realm)
                    && null != jsonObject.getString(Constants.CONTACT_PLATFORM)
                    && Constants.PLATFORM_SALES_FORCE.equals(jsonObject.getString(Constants
                    .CONTACT_PLATFORM)))
            {
                String SF_URL = realmContactTransactions.getContactById(jsonObject.getString
                        (Constants.CONTACT_ID), realm)
                        .getStringField1();
                if(null != SF_URL)
                    contact.setStringField1(SF_URL);
            }

        }catch (JSONException e){
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactDBController.mapContact: " + e.toString());
        }
        return contact;
    }

    public ArrayList<Contact> getLocalContactsByKeyWord(String keyWord) {
        Log.d(Constants.TAG, "SearchController.getLocalContactsByKeyWord: ");
        return internalContactSearch.getLocalContactsByKeyWord(keyWord);
    }

    public ArrayList<Contact> getContactsByKeyWord(String keyWord) {
        Log.d(Constants.TAG, "SearchController.getContactsByKeyWord: ");
        return realmContactTransactions.getContactsByKeyWord(keyWord, realm);
    }

    public ArrayList<Contact> getContactsByKeyWordWithoutLocalsAndSalesForce(String keyWord) {
        Log.d(Constants.TAG, "SearchController.getContactsByKeyWord: ");
        return realmContactTransactions.getContactsByKeyWordWithoutLocalsAndSalesForce(keyWord, realm);
    }

    public void storeContactsIntoRealm(ArrayList<Contact> contacts)
    {
        Log.d(Constants.TAG, "SearchController.storeContactsIntoRealm: ");
        realmContactTransactions.insertContactList(contacts, null);
    }

    public InternalContactSearch getInternalContactSearch()
    {
        return this.internalContactSearch;
    }

}
