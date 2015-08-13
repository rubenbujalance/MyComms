package com.vodafone.mycomms.contacts.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import model.Contact;

public class ContactSearchListViewArrayAdapter extends ContactListViewArrayAdapter {

    public ContactSearchListViewArrayAdapter(Context context, List<Contact> items) {
        super(context, items);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }


}