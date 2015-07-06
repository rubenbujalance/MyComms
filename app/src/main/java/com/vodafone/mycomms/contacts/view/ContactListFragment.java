package com.vodafone.mycomms.contacts.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chat.ChatMainActivity;
import com.vodafone.mycomms.contacts.connection.ContactController;
import com.vodafone.mycomms.contacts.connection.ContactListController;
import com.vodafone.mycomms.contacts.connection.IContactsRefreshConnectionCallback;
import com.vodafone.mycomms.contacts.connection.ISearchConnectionCallback;
import com.vodafone.mycomms.contacts.connection.RecentContactController;
import com.vodafone.mycomms.contacts.detail.ContactDetailMainActivity;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ReloadAdapterEvent;
import com.vodafone.mycomms.events.SetContactListAdapterEvent;
import com.vodafone.mycomms.search.SearchBarController;
import com.vodafone.mycomms.search.SearchController;
import com.vodafone.mycomms.settings.SettingsMainActivity;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import io.realm.Realm;
import model.Contact;
import model.FavouriteContact;
import model.RecentContact;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class ContactListFragment extends ListFragment implements ISearchConnectionCallback, IContactsRefreshConnectionCallback {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Realm realm;
    private ContactController mContactController;
    private SearchController mSearchController;
    private SearchBarController mSearchBarController;
    private ArrayList<Contact> contactList;
    private ArrayList<FavouriteContact> favouriteContactList;
    private ArrayList<RecentContact> recentContactList;
    protected Handler handler = new Handler();

    private ContactListViewArrayAdapter adapter;
    private ListView listView;
    private Parcelable state;
    private TextView emptyText;
    private String apiCall;

    private String profileId;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private int mIndex;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private SharedPreferences sp;

    // TODO: Rename and change types of parameters
    public static ContactListFragment newInstance(int index, String param2) {
        ContactListFragment fragment = new ContactListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, index);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_fragment_pager_contact_list, container, false);
        listView = (ListView) v.findViewById(android.R.id.list);
        emptyText = (TextView) v.findViewById(android.R.id.empty);

        loadSearchBarEventsAndControllers(v);

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.contacts_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });

        if(isProgressDialogNeeded())showProgressDialog();

        return v;
    }

    private void loadSearchBarEventsAndControllers(View v)
    {
        mSearchBarController = new SearchBarController
                (
                        getActivity()
                        , mContactController
                        , contactList
                        , mSearchController
                        , adapter
                        , mIndex
                        , listView
                        , false
                );

        mSearchBarController.initiateComponentsForSearchView(v);
        mSearchBarController.setSearchBarEvents();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ContactListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        BusProvider.getInstance().register(this);

        if (getArguments() != null) {
            mIndex = getArguments().getInt(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        sp = getActivity().getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

        if(sp==null){
            Log.e(Constants.TAG, "contactListFragment.onCreate: error loading Shared Preferences");
            profileId = "";
        }else{
            profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");
        }
        Log.i(Constants.TAG, "ContactListFragment.onCreate: profileId " + profileId);
        realm = Realm.getInstance(getActivity());
        mContactController = new ContactController(getActivity(),realm, profileId);
        mSearchController = new SearchController(getActivity(), realm, profileId);

        setListAdapterTabs();
    }

    private void refreshContent(){
        ContactListController contactListController = new ContactListController(getActivity(),realm,profileId);
        if (mIndex==Constants.CONTACTS_ALL) {
            contactListController.getContactList(Constants.CONTACT_API_GET_CONTACTS);
            contactListController.setConnectionCallback(this);
        } else if (mIndex==Constants.CONTACTS_FAVOURITE) {
            contactListController.getContactList(Constants.CONTACT_API_GET_FAVOURITES);
            contactListController.setConnectionCallback(this);
        } else if (mIndex==Constants.CONTACTS_RECENT) {
            contactListController.getContactList(Constants.CONTACT_API_GET_RECENTS);
            contactListController.setConnectionCallback(this);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.i(Constants.TAG, "ContactListFragment.onListItemClick: Listclicking");
        if (mListener != null) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            //mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).getId());
            Intent in;

            in = new Intent(getActivity(), ContactDetailMainActivity.class);

            if(mIndex == Constants.CONTACTS_FAVOURITE)
            {
                in.putExtra(Constants.CONTACT_IS_FAVORITE, true);
            }
            else
            {
                in.putExtra(Constants.CONTACT_IS_FAVORITE, false);
            }

            if(mIndex == Constants.CONTACTS_ALL) {
                if (contactList.get(position).getContactId()!=null && contactList.get(position).getContactId().equals(profileId))
                    in = new Intent(getActivity(), SettingsMainActivity.class);
                in.putExtra(Constants.CONTACT_CONTACT_ID,contactList.get(position).getContactId() );
                startActivity(in);
            }
            else if (mIndex == Constants.CONTACTS_RECENT)
            {
                try {
                    String action = recentContactList.get(position).getAction();
                    if (action.compareTo(Constants.CONTACTS_ACTION_CALL) == 0) {
                        String strPhones = recentContactList.get(position).getPhones();
                        if (strPhones != null)
                        {
                            String phone = strPhones;
                            if(!recentContactList.get(position).getPlatform().equals(Constants
                                    .PLATFORM_LOCAL))
                            {
                                JSONArray jPhones = new JSONArray(strPhones);
                                phone = (String)((JSONObject) jPhones.get(0)).get(Constants.CONTACT_PHONE);
                            }

                            Utils.launchCall(phone, getActivity());
                        }
                    }
                    else if (action.compareTo(Constants.CONTACTS_ACTION_SMS) == 0)
                    {
                        /*String strPhones = recentContactList.get(position).getPhones();
                        if (strPhones != null) {
                            JSONArray jPhones = new JSONArray(strPhones);
                            String phone = (String)((JSONObject) jPhones.get(0)).get(Constants.CONTACT_PHONES);
                            Utils.launchSms(phone, getActivity());
                        }*/

                        // This is LOCAL contact, then in this case the action will be Send SMS
                        // message
                        if(null != recentContactList.get(position).getPlatform() && recentContactList.get
                            (position).getPlatform().equals(Constants.PLATFORM_LOCAL))
                        {
                            String phone = recentContactList.get(position).getPhones();
                            if(null != phone)
                            {
                                Utils.launchSms(phone, getActivity());
                            }
                        }
                        else
                        {
                            in = new Intent(getActivity(), ChatMainActivity.class);
                            in.putExtra(Constants.CHAT_FIELD_CONTACT_ID, recentContactList.get(position).getContactId());
                            in.putExtra(Constants.CHAT_PREVIOUS_VIEW, Constants.CHAT_VIEW_CONTACT_LIST);
                            startActivity(in);
                        }

                    }
                    else if (action.compareTo(Constants.CONTACTS_ACTION_EMAIL) == 0) {
                        String strEmails = recentContactList.get(position).getEmails();
                        if (strEmails != null)
                        {
                            String email = strEmails;
                            if(!recentContactList.get(position).getPlatform().equals(Constants
                                    .PLATFORM_LOCAL))
                            {
                                JSONArray jPhones = new JSONArray(strEmails);
                                email = (String)((JSONObject) jPhones.get(0)).get(Constants.CONTACT_EMAIL);
                            }

                            Utils.launchEmail(email, getActivity());
                        }
                    }
                    //ADD RECENT
                    RecentContactController recentController = new RecentContactController(this,realm,profileId);
                    recentController.insertRecent(recentContactList.get(position).getContactId(), action);
                    setListAdapterTabs();
                } catch (Exception ex) {
                    Log.e(Constants.TAG, "ContactListFragment.onListItemClick: ", ex);
                }
            } else if (mIndex == Constants.CONTACTS_FAVOURITE) { {
                if (favouriteContactList.get(position).getContactId()!=null && favouriteContactList.get(position).getContactId().equals(profileId))
                    in = new Intent(getActivity(), SettingsMainActivity.class);
                in.putExtra(Constants.CONTACT_CONTACT_ID,favouriteContactList.get(position).getContactId() );
                startActivity(in);
            }}

        }
    }

    @Override
    public void onSearchContactsResponse(ArrayList<Contact> contactList, boolean morePages, int
            offsetPaging) {
        Log.i(Constants.TAG, "onSearchContactsResponse: " + apiCall);

        if (morePages)
        {
            mSearchController.getContactList(apiCall + "&o=" + offsetPaging);
        }
    }

    @Override
    public void onConnectionNotAvailable()
    {
        Log.d(Constants.TAG, "ContactListFragment.onConnectionNotAvailable: ");
    }

    @Override
    public void onContactsRefreshResponse(ArrayList<Contact> contactList, boolean morePages, int offsetPaging) {

        if (morePages){
            Log.i(Constants.TAG, "ContactListFragment.onContactsRefreshResponse: ");
            apiCall = Constants.CONTACT_API_GET_CONTACTS;
            ContactListController contactListController = new ContactListController(getActivity(),realm, profileId);
            contactListController.getContactList(apiCall + "&o=" + offsetPaging);
            contactListController.setConnectionCallback(this);
        } else {
            Log.i(Constants.TAG, "ContactListFragment.onContactsRefreshResponse: FINISH");
            mSwipeRefreshLayout.setRefreshing(false);
            BusProvider.getInstance().post(new SetContactListAdapterEvent());
        }
    }

    @Override
    public void onFavouritesRefreshResponse() {
        Log.i(Constants.TAG, "ContactListFragment.onFavouritesRefreshResponse: ");
        mSwipeRefreshLayout.setRefreshing(false);
        BusProvider.getInstance().post(new SetContactListAdapterEvent());
    }

    @Override
    public void onRecentsRefreshResponse() {
        Log.i(Constants.TAG, "ContactListFragment.onRecentsRefreshResponse: ");
        mSwipeRefreshLayout.setRefreshing(false);
        BusProvider.getInstance().post(new SetContactListAdapterEvent());
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
        BusProvider.getInstance().unregister(this);
    }

    public void setListAdapterTabs()
    {
        Log.i(Constants.TAG, "ContactListFragment.setListAdapterTabs: index " + mIndex);
        if(mIndex == Constants.CONTACTS_FAVOURITE) {
            favouriteContactList = mContactController.getAllFavouriteContacts();
            if (favouriteContactList!=null) {
                setListAdapter(new ContactFavouriteListViewArrayAdapter(getActivity().getApplicationContext(),
                        favouriteContactList));
            }
        }else if(mIndex == Constants.CONTACTS_RECENT){
            if (emptyText!=null)
                emptyText.setText("");
            recentContactList = mContactController.getAllRecentContacts();
            if (recentContactList!=null) {
                RecentListViewArrayAdapter recentAdapter = new RecentListViewArrayAdapter(getActivity().getApplicationContext(), recentContactList);
                if (listView != null) {
                    state = listView.onSaveInstanceState();
                    setListAdapter(recentAdapter);
                    listView.onRestoreInstanceState(state);
                }
            }
        }else if(mIndex == Constants.CONTACTS_ALL){
            if (emptyText!=null)
                emptyText.setText("");
            contactList = loadAllContactsFromDB();
            if(null != contactList)
            {
                reloadAdapter();
            }
        }
    }

    /**
     * Reloads list adapter when data changes
     * @author ---
     */
    private void reloadAdapter()
    {
        adapter = new ContactListViewArrayAdapter(getActivity().getApplicationContext(), contactList);
        if (contactList!=null) {
            if (listView!=null)
                state = listView.onSaveInstanceState();
            if (adapter != null)
            {
                setListAdapter(adapter);
                if (state!=null)
                    listView.onRestoreInstanceState(state);
            }
            else
            {
                adapter = new ContactListViewArrayAdapter(getActivity().getApplicationContext(), contactList);
                setListAdapter(adapter);
                if (state!= null)
                    listView.onRestoreInstanceState(state);
            }
        }
    }


    /**
     * Gets all contacts from Realm DB by given key word
     * @author str_oan
     * @param keyWord (String) -> key word for make search
     * @return (ArrayList<Contact>) -> list of contacts from Realm DB if any, otherwise return
     * empty list
     */
    private ArrayList<Contact> loadAllContactsFromDB(String keyWord)
    {
        ArrayList<Contact> contactArrayList;
        if(null == keyWord)
        {
            contactArrayList = mContactController.getAllContacts();
        }
        else
        {
            contactArrayList = mSearchController.getContactsByKeyWord(keyWord);
        }

        return contactArrayList;
    }

    private ArrayList<Contact> loadAllContactsFromDB()
    {
        return loadAllContactsFromDB(null);
    }


    @Subscribe
    public void setListAdapterEvent(SetContactListAdapterEvent event){
        Log.i(Constants.TAG, "ContactListPagerFragment.setListAdapterEvent: ");
        if(!isProgressDialogNeeded())hideProgressDialog();
    }

    @Subscribe
    public void reloadAdapterEvent(ReloadAdapterEvent event){
        Log.i(Constants.TAG, "ContactListPagerFragment.reloadAdapterEvent: ");
        this.contactList = mSearchBarController.getContactList();
        reloadAdapter();
    }


    private void showProgressDialog()
    {
        mSwipeRefreshLayout.setProgressViewOffset(false, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
        mSwipeRefreshLayout.setRefreshing(true);
    }

    private void hideProgressDialog()
    {
        if(mSwipeRefreshLayout.isRefreshing())mSwipeRefreshLayout.setRefreshing(false);
    }

    private boolean isProgressDialogNeeded()
    {
        if(mIndex == Constants.CONTACTS_ALL && contactList.size() <= 0)
            return true;
        else if(mIndex == Constants.CONTACTS_RECENT && recentContactList.size() <= 0)
            return true;
        else if(mIndex == Constants.CONTACTS_FAVOURITE && favouriteContactList.size() <= 0)
            return true;
        else
            return false;
    }

    public SearchBarController getSearchBarController()
    {
        return this.mSearchBarController;
    }
}
