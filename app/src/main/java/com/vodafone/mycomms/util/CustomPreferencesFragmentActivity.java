package com.vodafone.mycomms.util;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.settings.AccountsFragment;
import com.vodafone.mycomms.settings.PreferencesFragment;
import com.vodafone.mycomms.settings.ProfileFragment;

public class CustomPreferencesFragmentActivity extends ToolbarActivity
        implements PreferencesFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_profile_activity);
        Intent in = this.getIntent();
        int fragmentId = in.getIntExtra("index", 2);
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (fragmentId)
        {
            case 0:
                PreferencesFragment preferencesFragment = PreferencesFragment.newInstance(0, null);
                fragmentTransaction.add(preferencesFragment, String.valueOf(0));
                fragmentTransaction.commit();
                break;
            case 1:
                ProfileFragment profileFragment = ProfileFragment.newInstance(1, null);
                fragmentTransaction.add(profileFragment, String.valueOf(1));
                fragmentTransaction.commit();
                break;
            case 2:
                AccountsFragment accountsFragment = AccountsFragment.newInstance(2, null);
                fragmentTransaction.add(accountsFragment, String.valueOf(2));
                fragmentTransaction.commit();
                break;
            default:
                AccountsFragment deafultAccountsFragment = AccountsFragment.newInstance(2, null);
                fragmentTransaction.add(deafultAccountsFragment, String.valueOf(2));
                fragmentTransaction.commit();
                break;
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
