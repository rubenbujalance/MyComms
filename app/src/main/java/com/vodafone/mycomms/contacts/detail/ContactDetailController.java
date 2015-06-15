package com.vodafone.mycomms.contacts.detail;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.framework.library.model.ConnectionResponse;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.contacts.connection.ContactController;
import com.vodafone.mycomms.contacts.connection.ContactDetailConnection;
import com.vodafone.mycomms.contacts.connection.IContactDetailConnectionCallback;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;

import org.json.JSONObject;

import io.realm.Realm;
import model.Contact;

public class ContactDetailController extends BaseController {

    private final Realm mRealm;
    private ContactDetailConnection contactDetailConnection;
    private IContactDetailConnectionCallback contactDetailConnectionCallback;
    private Context mContext;
    private RealmContactTransactions realmContactTransactions;

    public ContactDetailController(Activity activity, Realm realm, String mProfileId) {
        super(activity);
        this.mRealm = realm;
        this.mContext = activity;
        realmContactTransactions = new RealmContactTransactions(realm, mProfileId);

    }

    void getContactDetail(String id){
        ContactDetailConnection contactDetailConnection = new ContactDetailConnection(getContext(), this, id);
        contactDetailConnection.request();
    }

    @Override
    public void onConnectionComplete(ConnectionResponse response) {
        super.onConnectionComplete(response);
        String result = response.getData().toString();
        Log.i(Constants.TAG, "ContactDetailController.onConnectionComplete: " + result);
        JSONObject jsonResponse;

        try {
            Contact contact;
            jsonResponse = new JSONObject(result);
            String data = jsonResponse.getString(Constants.CONTACT_DATA);
            jsonResponse = new JSONObject(data.substring(1, data.length()-1 )); //Removing squared bracelets.
            contact = ContactController.mapContact(jsonResponse);

            if(this.getConnectionCallback() != null && this.getConnectionCallback() instanceof IContactDetailConnectionCallback){
                ((IContactDetailConnectionCallback)this.getConnectionCallback()).onContactDetailReceived(contact);
            }
            realmContactTransactions.updateContact(contact);

        } catch (Exception e){
            Log.e(Constants.TAG, "ContactController.onConnectionComplete: " + e.toString());
        }

    }
}
