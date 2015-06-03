package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.util.Log;

import com.framework.library.connection.ConnectionInterfaces;
import com.framework.library.connection.HttpConnection;
import com.vodafone.mycomms.connection.BaseConnection;
import com.vodafone.mycomms.util.Constants;

/**
 * Created by str_vig on 21/05/2015.
 */
public class ContactDetailConnection extends BaseConnection{
    private static final String URL = "/api/me/contact/?id=";
    private static final int method = HttpConnection.GET;

    public ContactDetailConnection(Context context, ConnectionInterfaces.ConnectionListener listener, String contactId){
        super(URL+contactId, context, listener, method);
        Log.i(Constants.TAG, "ContactDetailConnection.ContactDetailConnection");
    }
}
