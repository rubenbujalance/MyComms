package com.vodafone.mycomms.contacts.view;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.contacts.connection.ContactsController;
import com.vodafone.mycomms.contacts.connection.IContactsConnectionCallback;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.RecentContactsReceivedEvent;
import com.vodafone.mycomms.events.RefreshFavouritesEvent;
import com.vodafone.mycomms.events.SetContactListAdapterEvent;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.view.tab.SlidingTabLayout;

import java.util.ArrayList;

import model.Contact;

public class ContactListPagerFragment extends Fragment implements ContactListFragment.OnFragmentInteractionListener ,IContactsConnectionCallback {
    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;
    private boolean mBusRegistered = false;
    private ContactsController mContactsController;
    private String apiCall;
    private ContactListFragment contactListFragment;
    private ContactListFragment contactRecentListFragment;
    private ContactListFragment contactFavouritesListFragment;
    private String mProfileId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_contact_list_tabs_fragment, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = getActivity().getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        mProfileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");
        mContactsController = new ContactsController(getActivity(), mProfileId);
        apiCall = Constants.CONTACT_API_GET_CONTACTS;
        //mContactsController.getContactList(apiCall);
        //mContactsController.setConnectionCallback(this);
        BusProvider.getInstance().register(this);


        //((ContactListMainActivity)getActivity()).activateContactListToolbar();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new ContactListPagerAdapter(getFragmentManager()));
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
        //This sets default tab
        if (savedInstanceState == null) {
            Bundle extras = getActivity().getIntent().getExtras();
            if(extras == null) {
                mViewPager.setCurrentItem(Constants.CONTACTS_ALL);
            } else {
                mViewPager.setCurrentItem(Constants.CONTACTS_FAVOURITE);
            }
        } else {
            mViewPager.setCurrentItem(Constants.CONTACTS_ALL);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
            if (((MycommsApp)getActivity().getApplication()).comesFromToolbar){
                mViewPager.setCurrentItem(Constants.CONTACTS_ALL);
            } else{
                mViewPager.setCurrentItem(Constants.CONTACTS_FAVOURITE);
                ((MycommsApp)getActivity().getApplication()).comesFromToolbar = true;
            }
    }

    @Override
    public void onFragmentInteraction(String id) {
        Log.d(Constants.TAG, "ContactListPagerFragment.onFragmentInteraction: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContactsController.closeRealm();

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
    public void onContactsResponse(ArrayList<Contact> responseContactList, boolean morePages, int offsetPaging) {
        Log.i(Constants.TAG, "onContactsResponse: " + apiCall + "&o=" + offsetPaging);

        if(apiCall.equals(Constants.CONTACT_API_GET_FAVOURITES)) {
            setListsAdapter();

        }else if(apiCall.equals(Constants.CONTACT_API_GET_CONTACTS)){
            if (contactListFragment!=null)
                contactListFragment.setListAdapterTabs();
            if (contactRecentListFragment!=null && !morePages)
                contactRecentListFragment.setListAdapterTabs();
            if (contactFavouritesListFragment!=null && !morePages)
                contactFavouritesListFragment.setListAdapterTabs();

            if (morePages){
                apiCall = Constants.CONTACT_API_GET_CONTACTS;
                mContactsController.getContactList(apiCall + "&o=" + offsetPaging);
            } else {
                apiCall = Constants.CONTACT_API_GET_RECENTS;
                mContactsController.getRecentList(apiCall);
            }
        }else if (apiCall.equals(Constants.CONTACT_API_GET_RECENTS)){

            setListsAdapter();
            apiCall = Constants.CONTACT_API_GET_FAVOURITES;
            mContactsController.getFavouritesList(apiCall);
        }
    }

    @Override
    public void onRecentContactsResponse() {
        Log.i(Constants.TAG, "onRecentContactsResponse: " + apiCall);
        setListsAdapter();
        apiCall = Constants.CONTACT_API_GET_FAVOURITES;
        mContactsController.getFavouritesList(apiCall);
    }

    private void setListsAdapter()
    {
        if (contactListFragment!=null)
        {
            contactListFragment.getSearchBarController().hideSearchBarContent();
            contactListFragment.getSearchBarController().hideKeyboard();
            contactListFragment.setListAdapterTabs();
        }

        if (contactRecentListFragment!=null)
            contactRecentListFragment.setListAdapterTabs();
        if (contactFavouritesListFragment!=null)
            contactFavouritesListFragment.setListAdapterTabs();
    }

    @Subscribe
    public void onRecentContactsReceived(RecentContactsReceivedEvent event){
        Log.i(Constants.TAG, "ContactListPagerFragment.onRecentContactsReceived: ");
        setListsAdapter();
    }

    @Subscribe
    public void setListAdapterEvent(SetContactListAdapterEvent event){
        Log.i(Constants.TAG, "ContactListPagerFragment.setListAdapterEvent: ");
        setListsAdapter();
    }

    @Subscribe
    public void refreshFavouritesEvent(RefreshFavouritesEvent event){
        Log.i(Constants.TAG, "ContactListPagerFragment.refreshFavouritesEvent: ");
        apiCall = Constants.CONTACT_API_GET_FAVOURITES;
        mContactsController.getFavouritesList(apiCall);
        setListsAdapter();
    }
}
