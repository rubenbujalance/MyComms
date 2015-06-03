package com.vodafone.mycomms.contacts.detail;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.framework.library.connection.HttpConnection;
import com.framework.library.model.ConnectionResponse;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.contacts.connection.ContactController;
import com.vodafone.mycomms.contacts.connection.RecentContactConnection;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.SetContactListAdapterEvent;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;

import org.json.JSONObject;

import java.util.HashMap;

import io.realm.Realm;

public class RecentContactController extends BaseController {

    private final Realm mRealm;
    private Context mContext;
    private RealmContactTransactions realmContactTransactions;

    public RecentContactController(Activity activity, Realm realm) {
        super(activity);
        this.mRealm = realm;
        this.mContext = activity;
        realmContactTransactions = new RealmContactTransactions(mRealm);
    }

    public void insertRecent(String contactId, String action){
        Log.i(Constants.TAG, "RecentContactController.insertRecent: ");
        JSONObject json = null;
        HashMap body = new HashMap<>();
        RecentContactController recentContactController;
        int method = HttpConnection.POST;
        String apiCall = Constants.CONTACT_API_POST_RECENTS;
        long timestamp = System.currentTimeMillis();
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
        Log.i(Constants.TAG, "RecentContactController.onConnectionComplete: ");
        super.onConnectionComplete(response);
        String result = response.getData().toString();
        Log.i(Constants.TAG, "RecentContactController.onConnectionComplete: " + result);
        String apiCall = Constants.CONTACT_API_GET_RECENTS;
        ContactController contactController = new ContactController(getActivity(), mRealm);
        contactController.getRecentList(apiCall);
        //Refresh Recent List
        BusProvider.getInstance().post(new SetContactListAdapterEvent());
    }
}
