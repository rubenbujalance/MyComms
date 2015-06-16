package com.vodafone.mycomms.contacts.connection;

import com.vodafone.mycomms.connection.IConnectionCallback;

import java.util.ArrayList;

import model.Contact;

/**
 * Created by str_oan on 09/06/2015.
 */
public interface ISearchConnectionCallback extends IConnectionCallback
{
    void onSearchContactsResponse(ArrayList<Contact> contactList, boolean morePages, int
            offsetPaging);
}

