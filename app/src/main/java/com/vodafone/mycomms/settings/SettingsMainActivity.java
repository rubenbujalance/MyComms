package com.vodafone.mycomms.settings;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.ToolbarActivity;

/**
 * Created by amg on 05/05/2015.
 */
public class SettingsMainActivity extends ToolbarActivity implements ProfileFragment.OnFragmentInteractionListener, PreferencesFragment.OnFragmentInteractionListener{

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //This prevents the view focusing on the edit text and opening the keyboard
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.layout_profile_activity);
        activateToolbar();

        if (savedInstanceState == null) {
            FragmentTransaction transaction;
            transaction = getSupportFragmentManager().beginTransaction();
            ProfileListPagerFragment fragment = new ProfileListPagerFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }

        ImageView ivBtBack = (ImageView)findViewById(R.id.ivBtBack);
        ivBtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    @Override
    public void onFragmentInteraction(String id) {
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }
}
