package com.vodafone.mycomms.contacts.connection;

import com.vodafone.mycomms.connection.IConnectionCallback;

import java.util.ArrayList;

import model.Contact;

/**
 * Created by str_vig on 14/05/2015.
 */
public interface IContactsRefreshConnectionCallback extends IConnectionCallback {
    void onContactsRefreshResponse(ArrayList<Contact> contactList, boolean morePages, int offsetPaging);
    void onFavouritesRefreshResponse();
    void onRecentsRefreshResponse();
}