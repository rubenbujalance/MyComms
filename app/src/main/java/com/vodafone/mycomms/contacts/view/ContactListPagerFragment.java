package com.vodafone.mycomms.contacts.view;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.view.tab.SlidingTabLayout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ContactListPagerFragment extends Fragment implements ContactListFragment.OnFragmentInteractionListener {
    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_contact_list_tabs_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new ContactListPagerAdapter(getFragmentManager()));
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
    }

    @Override
    public void onFragmentInteraction(String id) {
        Log.d(Constants.TAG, "ContactListPagerFragment.onFragmentInteraction: ");
    }

    class ContactListPagerAdapter extends FragmentPagerAdapter {
        public ContactListPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }
        @Override
        public Fragment getItem(int position) {
            ContactListFragment fragment = ContactListFragment.newInstance(position, "whatever");
            return fragment;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            if(position == 0)
                return getString(R.string.contact_list_tab_favourites);
            else if (position == 1)
                return getString(R.string.contact_list_tab_recents);
            else if (position == 2){
                return getString(R.string.contact_list_tab_contacts);
            }
            Log.wtf(Constants.TAG, "ContactListPagerAdapter.getPageTitle: Unknown page");
            return "whatever" ;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Log.d(Constants.TAG, "ContactListPagerAdapter.destroyItem: ");
        }
    }
}
