package com.vodafone.mycomms.contacts.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.contacts.connection.ContactController;
import com.vodafone.mycomms.contacts.connection.IContactsConnectionCallback;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.RefreshContactListEvent;
import com.vodafone.mycomms.events.RefreshRecentsContactListEvent;
import com.vodafone.mycomms.events.SetContactListAdapterEvent;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UserSecurity;
import com.vodafone.mycomms.view.tab.SlidingTabLayout;

import java.util.ArrayList;

import io.realm.Realm;
import model.Contact;

public class ContactListPagerFragment extends Fragment implements ContactListFragment.OnFragmentInteractionListener ,IContactsConnectionCallback {
    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;
    private boolean mBusRegistered = false;
    private ContactController mContactController;
    private Realm realm;
    private String accessToken;
    private String apiCall;
    private ContactListFragment contactListFragment;
    private ContactListFragment contactRecentListFragment;
    private ContactListFragment contactFavouritesListFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_contact_list_tabs_fragment, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accessToken = UserSecurity.getAccessToken(getActivity());
        realm = Realm.getInstance(getActivity());
        mContactController = new ContactController(this,realm);
        apiCall = Constants.CONTACT_API_GET_CONTACTS;
        mContactController.getContactList(accessToken, apiCall);
        mContactController.setConnectionCallback(this);
        BusProvider.getInstance().register(this);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
        BusProvider.getInstance().unregister(this);
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
            if (!mBusRegistered){
                mBusRegistered = true;
            }
            if(position == 0)
                contactFavouritesListFragment = fragment;
            else if (position == 1)
                contactRecentListFragment = fragment;
            else if (position == 2){
                contactListFragment = fragment;
            }
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

    @Override
    public void onConnectionNotAvailable() {
        Log.d(Constants.TAG, "ContactListPagerFragment.onConnectionNotAvailable: ");
    }

    @Override
    public void onContactsResponse(ArrayList<Contact> responseContactList) {
        Log.i(Constants.TAG, "onContactsResponse: " + apiCall);

        if(apiCall.equals(Constants.CONTACT_API_GET_FAVOURITES)) {
            //BusProvider.getInstance().post(new RefreshContactListEvent());
            if (contactListFragment!=null)
                contactListFragment.setListAdapterTabs();
            if (contactRecentListFragment!=null)
                contactRecentListFragment.setListAdapterTabs();
            if (contactFavouritesListFragment!=null)
                contactFavouritesListFragment.setListAdapterTabs();

            apiCall = Constants.CONTACT_API_GET_RECENTS;
            mContactController.getRecentList(accessToken, apiCall);

        }else if(apiCall.equals(Constants.CONTACT_API_GET_CONTACTS)){
            //apiCall = Constants.CONTACT_API_GET_FAVOURITES;
            //mContactController.getFavouritesList(accessToken, apiCall);

            if (contactListFragment!=null)
                contactListFragment.setListAdapterTabs();
            if (contactRecentListFragment!=null)
                contactRecentListFragment.setListAdapterTabs();
            if (contactFavouritesListFragment!=null)
                contactFavouritesListFragment.setListAdapterTabs();

            apiCall = Constants.CONTACT_API_GET_RECENTS;
            mContactController.getRecentList(accessToken, apiCall);
        }else if (apiCall.equals(Constants.CONTACT_API_GET_RECENTS)){
            if (contactListFragment!=null)
                contactListFragment.setListAdapterTabs();
            if (contactRecentListFragment!=null)
                contactRecentListFragment.setListAdapterTabs();
            if (contactFavouritesListFragment!=null)
                contactFavouritesListFragment.setListAdapterTabs();

        }
    }

    @Subscribe
    public void refreshContactListEvent(RefreshContactListEvent event) {
        Log.i(Constants.TAG, "RefreshContactListEvent: refreshContactListEvent");
        mContactController.getContactList(accessToken, apiCall);
        mContactController.setConnectionCallback(this);
    }

    @Subscribe
    public void setListAdapterEvent(SetContactListAdapterEvent event){
        Log.i(Constants.TAG, "ContactListPagerFragment.setListAdapterEvent: ");
        if (contactListFragment!=null)
            contactListFragment.setListAdapterTabs();
        if (contactRecentListFragment!=null)
            contactRecentListFragment.setListAdapterTabs();
        if (contactFavouritesListFragment!=null)
            contactFavouritesListFragment.setListAdapterTabs();
    }

    @Subscribe
    public void refreshRecentsContactListEvent(RefreshRecentsContactListEvent event) {
        Log.i(Constants.TAG, "RefreshContactListEvent: refreshRecentsContactListEvent");
        apiCall = Constants.CONTACT_API_GET_RECENTS;
        //mContactController.getRecentList(accessToken, apiCall);
        //mContactController.setConnectionCallback(this);
    }

}
