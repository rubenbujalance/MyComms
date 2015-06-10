package com.vodafone.mycomms.contacts.connection;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.framework.library.connection.HttpConnection;
import com.framework.library.model.ConnectionResponse;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;

import io.realm.Realm;

public class RecentContactController extends BaseController {

    private final Realm mRealm;
    private Context mContext;
    private RealmContactTransactions realmContactTransactions;
    private String mProfileId;

    public RecentContactController(Activity activity, Realm realm, String profileId) {
        super(activity);
        this.mRealm = realm;
        this.mContext = activity;
        this.mProfileId = profileId;
        realmContactTransactions = new RealmContactTransactions(mRealm, mProfileId);
    }

    public RecentContactController(Fragment fragment, Realm realm, String profileId) {
        super(fragment);
        this.mRealm = realm;
        this.mContext = fragment.getActivity();
        this.mProfileId = profileId;
        realmContactTransactions = new RealmContactTransactions(mRealm, mProfileId);
    }

    public void insertRecent(String contactId, String action){
        Log.i(Constants.TAG, "RecentContactController.insertRecent: ");
        JSONObject json = null;
        HashMap body = new HashMap<>();
        int method = HttpConnection.POST;
        String apiCall = Constants.CONTACT_API_POST_RECENTS;
        long timestamp = Calendar.getInstance().getTimeInMillis();
        body.put(Constants.CONTACT_ID, contactId);
        body.put(Constants.CONTACT_RECENTS_ACTION, action);
        body.put(Constants.CONTACT_RECENTS_ACTION_TIME, timestamp);
        json = new JSONObject(body);
        RecentContactConnection recentContactConnection = new RecentContactConnection(mContext, this, method, apiCall);
        recentContactConnection.setPayLoad(json.toString());
        recentContactConnection.request();
    }

    @Override
    public void onConnectionComplete(ConnectionResponse response) {
        super.onConnectionComplete(response);
        Log.i(Constants.TAG, "RecentContactController.onConnectionComplete: ");
        String apiCall = Constants.CONTACT_API_GET_RECENTS;
        ContactController contactController = new ContactController(getActivity(), mRealm, mProfileId);
        contactController.getRecentList(apiCall);
    }
}
