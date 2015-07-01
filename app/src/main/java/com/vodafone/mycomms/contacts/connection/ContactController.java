package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.util.Log;

import com.framework.library.model.ConnectionResponse;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;

import org.json.JSONObject;

import io.realm.Realm;

public class ContactController extends BaseController {
    private Context mContext;
    private Realm mRealm;
    private RealmContactTransactions realmContactTransactions;
    private ContactConnection mContactConnection;
    private String apiCall;
    private String mProfileId;
    private int offsetPaging = 0;

    public ContactController(Context context, Realm realm, String profileId) {
        super(context);
        this.mRealm = realm;
        this.mContext = context;
        this.mProfileId = profileId;
        realmContactTransactions = new RealmContactTransactions(realm, mProfileId);
    }

    public void getContactList(String api){
        Log.i(Constants.TAG, "ContactController.getContactList: " + api);
        if(mContactConnection != null){
            mContactConnection.cancel();
        }
        apiCall = api;
        mContactConnection = new ContactConnection(getContext(), this, apiCall);
        mContactConnection.request();
    }

    @Override
    public void onConnectionComplete(ConnectionResponse response) {
        super.onConnectionComplete(response);

        String result = response.getData().toString();

        Log.i(Constants.TAG, "ContactController.onConnectionComplete: " + result);

        JSONObject jsonResponse;

        if (result != null && result.trim().length()>0) {
            try {
                jsonResponse = new JSONObject(result);

                ContactsController contactsController = new ContactsController(mContext, mRealm, mProfileId);
                contactsController.insertContactListInRealm(jsonResponse);

                //TODO: Update Contacts on Fragment

                JSONObject jsonPagination = jsonResponse.getJSONObject(Constants.CONTACT_PAGINATION);
                if (jsonPagination.getBoolean(Constants.CONTACT_PAGINATION_MORE_PAGES)) {
                    int pageSize = jsonPagination.getInt(Constants.CONTACT_PAGINATION_PAGESIZE);
                    offsetPaging = offsetPaging + pageSize;
                    getContactList(apiCall + "&o=" + offsetPaging);
                } else {
                    offsetPaging = 0;
                }
            } catch (Exception e) {
                Log.e(Constants.TAG, "ContactController.onConnectionComplete: contacts ", e);
            }
        }
    }
}
