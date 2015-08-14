package com.vodafone.mycomms.util;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.vodafone.mycomms.contacts.view.ContactListFragment;

/**
 * Created by str_rbm on 13/08/2015.
 */
public class CustomFragmentActivity extends AppCompatActivity
        implements ContactListFragment.OnFragmentInteractionListener {

    @Override
    public void onFragmentInteraction(String id) {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent in = this.getIntent();
        int index = in.getIntExtra("index", 2);
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ContactListFragment fragment = ContactListFragment.newInstance(index, null);

        fragmentTransaction.add(fragment, String.valueOf(index));
        fragmentTransaction.commit();
    }
}
