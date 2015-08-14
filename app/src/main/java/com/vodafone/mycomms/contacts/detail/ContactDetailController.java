package com.vodafone.mycomms.contacts.detail;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.framework.library.model.ConnectionResponse;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.contacts.connection.ContactDetailConnection;
import com.vodafone.mycomms.contacts.connection.ContactsController;
import com.vodafone.mycomms.contacts.connection.IContactDetailConnectionCallback;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;

import org.json.JSONObject;

import model.Contact;

/**
 * Created by STR_VIG on 21/05/2015.
 */
public class ContactDetailController extends BaseController {

    private ContactDetailConnection contactDetailConnection;
    private IContactDetailConnectionCallback contactDetailConnectionCallback;
    private Context mContext;
    private RealmContactTransactions realmContactTransactions;
    private String profileId;

    public ContactDetailController(Activity activity, String profileId) {
        super(activity);
        this.mContext = activity;
        this.profileId = profileId;
        realmContactTransactions = new RealmContactTransactions(profileId);
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
            contact = ContactsController.mapContact(jsonResponse, profileId);

            if(this.getConnectionCallback() != null && this.getConnectionCallback() instanceof IContactDetailConnectionCallback){
                ((IContactDetailConnectionCallback)this.getConnectionCallback()).onContactDetailReceived(contact);
            }
            realmContactTransactions.updateContact(contact, null);

        } catch (Exception e){
            Log.e(Constants.TAG, "ContactsController.onConnectionComplete: " + e.toString());
        }

    }

}
