package com.vodafone.mycomms.profile;

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
import com.vodafone.mycomms.view.tab.SlidingTabLayout;

public class ProfileListPagerFragment extends Fragment implements ProfileFragment.OnFragmentInteractionListener {

    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_profile_list_tabs_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new ProfileListPagerAdapter(getFragmentManager()));
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
        mViewPager.setCurrentItem(Constants.MY_PROFILE);
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
                case Constants.PREFERENCES:
                    Log.i(Constants.TAG, "ProfileListPagerAdapter.getItem: " + position);
                    ProfileFragment profileFragment2 = ProfileFragment.newInstance(position, "whatever");
                    fragment = profileFragment2;
                    break;
                case Constants.ACCOUNTS:
                    Log.i(Constants.TAG, "ProfileListPagerAdapter.getItem: " + position);
                    ProfileFragment profileFragment3 = ProfileFragment.newInstance(position, "whatever");
                    fragment = profileFragment3;
                    break;
                case Constants.MY_PROFILE:
                    Log.i(Constants.TAG, "ProfileListPagerAdapter.getItem: " + position);
                    ProfileFragment profileFragment = ProfileFragment.newInstance(position, "whatever");
                    fragment = profileFragment;
                    break;
                default:
                    break;
            }
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position == Constants.PREFERENCES)
                return getString(R.string.profile_list_tab_preferences);
            else if (position == Constants.ACCOUNTS)
                return getString(R.string.profile_list_tab_accounts);
            else if (position == Constants.MY_PROFILE){
                return getString(R.string.profile_list_tab_my_profile);
            }
            Log.i(Constants.TAG, "getPageTitle: Unknown page");
            return "whatever" ;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }
    }
}
