package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.util.Log;

import com.framework.library.model.ConnectionResponse;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;

import org.json.JSONObject;

import java.util.ArrayList;

import io.realm.Realm;
import model.Contact;

public class ContactListController extends BaseController {

    private Realm mRealm;
    private Context mContext;
    private String mProfileId;
    private RealmContactTransactions realmContactTransactions;
    private ContactConnection contactConnection;
    private int offsetPaging = 0;
    private String api;

    public ContactListController(Context context, Realm realm, String profileId) {
        super(context);
        this.mRealm = realm;
        this.mContext = context;
        this.mProfileId = profileId;
        realmContactTransactions = new RealmContactTransactions(realm, mProfileId);
    }

    public void getContactList(String apiCall){
        api = apiCall;
        if(contactConnection != null){
            contactConnection.cancel();
        }
        contactConnection = new ContactConnection(getContext(), this, apiCall);
        contactConnection.request();
    }

    @Override
    public void onConnectionComplete(ConnectionResponse response){
        boolean morePages = false;
        JSONObject jsonResponse;
        String result = response.getData().toString();
        if (result != null && result.trim().length()>0) {
            try {
                jsonResponse = new JSONObject(result);
                ContactController contactController = new ContactController(mContext, mRealm, mProfileId);
                if (api.equals(Constants.CONTACT_API_GET_FAVOURITES)){
                    contactController.insertFavouriteContactInRealm(jsonResponse);

                    if (this.getConnectionCallback() != null && this.getConnectionCallback() instanceof IContactsRefreshConnectionCallback) {
                        ((IContactsRefreshConnectionCallback) this.getConnectionCallback()).onFavouritesRefreshResponse();
                    }
                } else if (api.equals(Constants.CONTACT_API_GET_RECENTS)){
                    contactController.insertFavouriteContactInRealm(jsonResponse);

                    if (this.getConnectionCallback() != null && this.getConnectionCallback() instanceof IContactsRefreshConnectionCallback) {
                        ((IContactsRefreshConnectionCallback) this.getConnectionCallback()).onRecentsRefreshResponse();
                    }
                } else { //API Get Contacts
                    JSONObject jsonPagination = jsonResponse.getJSONObject(Constants.CONTACT_PAGINATION);
                    if (jsonPagination.getBoolean(Constants.CONTACT_PAGINATION_MORE_PAGES)) {
                        int pageSize = jsonPagination.getInt(Constants.CONTACT_PAGINATION_PAGESIZE);
                        morePages = true;
                        offsetPaging = offsetPaging + pageSize;
                    } else {
                        offsetPaging = 0;
                    }
                    ArrayList<Contact> realmContactList = new ArrayList<>();
                    contactController = new ContactController(mContext, mRealm, mProfileId);
                    realmContactList = contactController.insertContactListInRealm(jsonResponse);

                    if (this.getConnectionCallback() != null && this.getConnectionCallback() instanceof IContactsRefreshConnectionCallback) {
                        ((IContactsRefreshConnectionCallback) this.getConnectionCallback()).onContactsRefreshResponse(realmContactList, morePages, offsetPaging);
                    }
                }

            } catch (Exception e) {
                Log.e(Constants.TAG, "ContactListController.onConnectionComplete: contacts ", e);
            }
        }
    }
}
