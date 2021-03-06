package com.vodafone.mycomms.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.view.tab.SlidingTabLayoutPreferences;

public class ProfileListPagerFragment extends Fragment implements ProfileFragment.OnFragmentInteractionListener {

    private SlidingTabLayoutPreferences mSlidingTabLayout;
    private ViewPager mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_profile_list_tabs_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new ProfileListPagerAdapter(getFragmentManager()));
        mSlidingTabLayout = (SlidingTabLayoutPreferences) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
        //This sets default tab
        mViewPager.setCurrentItem(Constants.MY_SETTINGS);
    }

    @Override
    public void onFragmentInteraction(String id) {
        Log.i(Constants.TAG, "ProfileListPagerFragment.onFragmentInteraction: ");
    }

    class ProfileListPagerAdapter extends FragmentPagerAdapter {
        public ProfileListPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position){
                case Constants.MY_SETTINGS:
                    Log.i(Constants.TAG, "ProfileListPagerAdapter.getItem: " + position);
                    PreferencesFragment preferencesFragment = PreferencesFragment.newInstance(position, "whatever");
                    fragment = preferencesFragment;

                    break;
                case Constants.MY_PROFILE:
                    Log.i(Constants.TAG, "ProfileListPagerAdapter.getItem: " + position);
                    ProfileFragment profileFragment = ProfileFragment.newInstance(position, "whatever");
                    fragment = profileFragment;

                    break;

                case Constants.MY_ACCOUNTS:
                    Log.i(Constants.TAG, "ProfileListPagerAdapter.getItem: " + position);
                    AccountsFragment accountsFragment = AccountsFragment.newInstance(position, "whatever");
                    fragment = accountsFragment;

                    break;
                default:
                    break;
            }
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position == Constants.MY_SETTINGS)
                return getString(R.string.profile_list_tab_preferences);
            else if (position == Constants.MY_PROFILE){
                return getString(R.string.profile_list_tab_my_profile);
            }
            else if (position == Constants.MY_ACCOUNTS){
                return getString(R.string.profile_list_tab_accounts);
            }
            Log.i(Constants.TAG, "getPageTitle: Unknown page");
            return "whatever" ;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }
    }
}
