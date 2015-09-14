package com.vodafone.mycomms.util;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.vodafone.mycomms.settings.PreferencesFragment;

public class CustomPreferencesFragmentActivity extends AppCompatActivity
        implements PreferencesFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent in = this.getIntent();
        int index = in.getIntExtra("index", 2);
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        PreferencesFragment fragment = PreferencesFragment.newInstance(index, null);

        fragmentTransaction.add(fragment, String.valueOf(index));
        fragmentTransaction.commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
