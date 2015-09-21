package com.vodafone.mycomms.contacts.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chatgroup.GroupChatActivity;
import com.vodafone.mycomms.contacts.connection.RecentContactController;
import com.vodafone.mycomms.contacts.detail.ContactDetailMainActivity;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ReloadAdapterEvent;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.search.SearchBarController;
import com.vodafone.mycomms.search.SearchController;
import com.vodafone.mycomms.settings.SettingsMainActivity;
import com.vodafone.mycomms.settings.globalcontacts.AddGlobalContactsActivity;
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
public class ContactListFragment extends ListFragment {

    private SearchController mSearchController;
    private SearchBarController mSearchBarController;
    private ArrayList<Contact> contactList;
    private ArrayList<FavouriteContact> favouriteContactList;
    private ArrayList<RecentContact> recentContactList;
    private RealmContactTransactions mContactTransactions;
    private RelativeLayout addGlobalContactsContainer;

    private ListView listView;
    private Parcelable state;
    private TextView emptyText;
    private EditText searchView;
    private RecentContactController recentController;

    private String profileId;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private int mIndex;
    private OnFragmentInteractionListener mListener;
    private SharedPreferences sp;
    private Realm realm;

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
        searchView = (EditText) v.findViewById(R.id.et_search);

        listView.addFooterView(new View(getActivity()), null, true);

        addGlobalContactsContainer = (RelativeLayout) v.findViewById(R.id.add_global_contacts_container);

        //TODO: Null Object error, commented
        addGlobalContactsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getActivity(), AddGlobalContactsActivity.class);
                startActivity(in);
            }
        });

        setListAdapterTabs();
        loadSearchBarEventsAndControllers(v);

        if (mIndex == Constants.CONTACTS_ALL)
        {
            if (Constants.isDashboardOrigin) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    public void run() {
                        searchView.requestFocus();
                        showKeyboard();
                    }
                }, 500);
                Constants.isDashboardOrigin = false;
            }

        }
        else{
            hideKeyboard();
        }
        return v;
    }

    private void loadSearchBarEventsAndControllers(View v)
    {
        mSearchBarController = new SearchBarController
                (
                        getActivity()
                        , getContactTransactions()
                        , contactList
                        , mSearchController
                        , mIndex
                        , listView
                        , false
                        , realm
                        , this
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

        realm = Realm.getDefaultInstance();
        if(realm!=null) realm.setAutoRefresh(true);

        if (getArguments() != null) {
            mIndex = getArguments().getInt(ARG_PARAM1);
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
        mSearchController = new SearchController(getActivity().getApplicationContext(),
                profileId, realm);
        recentController = new RecentContactController(getActivity(), profileId);

    }

    public RealmContactTransactions getContactTransactions() {
        if(null == mContactTransactions)
            mContactTransactions = new RealmContactTransactions(profileId);

        return  mContactTransactions;
    }

    @Override
    public void onResume()
    {
        if (!sp.getBoolean(Constants.IS_GLOBAL_CONTACTS_LOADING_ENABLED, false) &&
                mIndex == Constants.CONTACTS_ALL)
            showLDAPSettingsBar(true);
        else showLDAPSettingsBar(false);

        //setListAdapterTabs();
        super.onResume();
    }

    public void showLDAPSettingsBar(boolean show) {
        if(show) addGlobalContactsContainer.setVisibility(View.VISIBLE);
        else addGlobalContactsContainer.setVisibility(View.GONE);
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
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
        if(realm!=null) this.realm.close();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.i(Constants.TAG, "ContactListFragment.onListItemClick: Listclicking");
        if (mListener != null)
        {
            Intent in;

            in = new Intent(getActivity(), ContactDetailMainActivity.class);

            if (mIndex == Constants.CONTACTS_FAVOURITE) {
                in.putExtra(Constants.CONTACT_IS_FAVORITE, true);
            } else {
                in.putExtra(Constants.CONTACT_IS_FAVORITE, false);
            }

            if (mIndex == Constants.CONTACTS_ALL)
            {
                if(null != contactList && !contactList.get(position).getFirstName().equals(getResources().getString(R.string.no_search_records)))
                {
                    MycommsApp.contactViewOrigin = Constants.CONTACTS_ALL;
                    in.putExtra(Constants.CONTACT_CONTACT_ID, contactList.get(position).getContactId());
                    startActivity(in);
                }

            } else if (mIndex == Constants.CONTACTS_RECENT) {
                try {
                    String action = recentContactList.get(position).getAction();
                    MycommsApp.contactViewOrigin = Constants.CONTACTS_RECENT;
                    if (action.compareTo(Constants.CONTACTS_ACTION_CALL) == 0) {
                        String strPhones = recentContactList.get(position).getPhones();
                        if (strPhones != null) {
                            String phone = strPhones;
                            if (!recentContactList.get(position).getPlatform().equals(Constants
                                    .PLATFORM_LOCAL)) {
                                JSONArray jPhones = new JSONArray(strPhones);
                                phone = (String) ((JSONObject) jPhones.get(0)).get(Constants.CONTACT_PHONE);
                            }

                            Utils.launchCall(phone, getActivity());
                            recentController.insertRecent(recentContactList.get(position).getContactId(), action);
                        }
                    } else if (action.compareTo(Constants.CONTACTS_ACTION_SMS) == 0)
                    {
                        if (null != recentContactList.get(position).getPlatform()
                                && (recentContactList.get(position).getPlatform().equals(Constants.PLATFORM_LOCAL)
                                    || recentContactList.get(position).getPlatform().equals(Constants.PLATFORM_GLOBAL_CONTACTS)))
                        {
                            String phone = recentContactList.get(position).getPhones();
                            if (null != phone) {
                                Utils.launchSms(phone, getActivity());
                                recentController.insertRecent(recentContactList.get(position).getContactId(), action);
                            }
                        } else {
                            if (recentContactList.get(position).getId().startsWith("mg_")) {
                                in = new Intent(getActivity(), GroupChatActivity.class);
                                in.putExtra(Constants.GROUP_CHAT_ID, recentContactList.get(position).getId());
                                in.putExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY, Constants.CHAT_VIEW_CONTACT_LIST);
                                in.putExtra(Constants.IS_GROUP_CHAT, true);
                                startActivity(in);
                            } else {
                                in = new Intent(getActivity(), GroupChatActivity.class);
                                in.putExtra(Constants.CHAT_FIELD_CONTACT_ID, recentContactList.get(position).getContactId());
                                in.putExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY, Constants.CHAT_VIEW_CONTACT_LIST);
                                in.putExtra(Constants.IS_GROUP_CHAT, false);
                                startActivity(in);
                            }
                        }

                    } else if (action.compareTo(Constants.CONTACTS_ACTION_EMAIL) == 0) {
                        String strEmails = recentContactList.get(position).getEmails();
                        if (strEmails != null) {
                            String email = strEmails;
                            if (!recentContactList.get(position).getPlatform().equals(Constants
                                    .PLATFORM_LOCAL)) {
                                JSONArray jPhones = new JSONArray(strEmails);
                                email = (String) ((JSONObject) jPhones.get(0)).get(Constants.CONTACT_EMAIL);
                            }

                            Utils.launchEmail(email, getActivity());
                            recentController.insertRecent(recentContactList.get(position).getContactId(), action);
                        }
                    }
                    setListAdapterTabs();
                } catch (Exception ex) {
                    Log.e(Constants.TAG, "ContactListFragment.onListItemClick: ", ex);
                }
            } else if (mIndex == Constants.CONTACTS_FAVOURITE) {
                {
                    if (favouriteContactList.get(position).getContactId() != null && favouriteContactList.get(position).getContactId().equals(profileId))
                        in = new Intent(getActivity(), SettingsMainActivity.class);
                    MycommsApp.contactViewOrigin = Constants.CONTACTS_FAVOURITE;
                    in.putExtra(Constants.CONTACT_CONTACT_ID, favouriteContactList.get(position).getContactId());
                    startActivity(in);
                }
            }


        }
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
        public void onFragmentInteraction(String id);
    }

    public void setListAdapterTabs() {
        Log.i(Constants.TAG, "ContactListFragment.setListAdapterTabs: index " + mIndex);

        if(mIndex == Constants.CONTACTS_FAVOURITE) {
            favouriteContactList = getContactTransactions().getAllFavouriteContacts(realm);
            if (favouriteContactList!=null) {
                setListAdapter(new ContactFavouriteListViewArrayAdapter(getActivity(),
                        favouriteContactList, realm));
            }
        }else if(mIndex == Constants.CONTACTS_RECENT){
            if (emptyText!=null)
                emptyText.setText("");
            recentContactList = getContactTransactions().getAllRecentContacts(realm);
            if (recentContactList!=null)
            {
                recentContactList = filterRecentList(recentContactList);
                RecentListViewArrayAdapter recentAdapter = new RecentListViewArrayAdapter
                        (getActivity(), recentContactList, profileId, realm);
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

    private ArrayList<RecentContact> filterRecentList(ArrayList<RecentContact> items)
    {
        ArrayList<RecentContact> finalList = new ArrayList<>();
        new RealmGroupChatTransactions(getActivity(), profileId);
        for(RecentContact contact : items)
        {
            if(null != contact && null != contact.getId() && contact.getId().startsWith("mg_"))
            {
                if(null != RealmGroupChatTransactions.getGroupChatById(contact.getId(), realm))
                    finalList.add(contact);
            }

            else if(null != contact && null != contact.getId())
                finalList.add(contact);
        }
        return finalList;
    }

    /**
     * Reloads list adapter when data changes
     * @author ---
     */
    private void reloadAdapter()
    {
        ContactListViewArrayAdapter adapter = new ContactListViewArrayAdapter(
                getActivity(), contactList);
        if (contactList!=null) {
            if (listView != null)
                state = listView.onSaveInstanceState();
            if (adapter != null) {
                setListAdapter(adapter);
                if (state != null)
                    listView.onRestoreInstanceState(state);
            } else {
                adapter = new ContactListViewArrayAdapter(getActivity(), contactList);
                setListAdapter(adapter);
                if (state != null)
                    listView.onRestoreInstanceState(state);
            }
            if (contactList.size()!=0) {
                if (emptyText!=null)
                    emptyText.setText(getResources().getString(R.string.no_search_records));
            }
        }
    }

    /**
     * Reloads list adapter when on search
     * @author ---
     */
    private void reloadSearchAdapter()
    {
        ContactListViewArrayAdapter adapter = new ContactListViewArrayAdapter(
                getActivity(), contactList);
        if (contactList!=null) {
            if (listView != null)
                state = listView.onSaveInstanceState();
            if (adapter != null) {
                setListAdapter(adapter);
                if (state != null)
                    listView.onRestoreInstanceState(state);
            } else {
                adapter = new ContactListViewArrayAdapter(getActivity(), contactList);
                setListAdapter(adapter);
                if (state != null)
                    listView.onRestoreInstanceState(state);
            }
            if (contactList.size()!=0) {
                if (emptyText!=null)
                    emptyText.setText(getResources().getString(R.string.no_search_records));
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
            contactArrayList = RealmContactTransactions.getAllContacts(realm, profileId);
        else
            contactArrayList = mSearchController.getContactsByKeyWord(keyWord);

        return contactArrayList;
    }

    private ArrayList<Contact> loadAllContactsFromDB()
    {
        return loadAllContactsFromDB(null);
    }

    @Subscribe
    public void reloadAdapterEvent(ReloadAdapterEvent event){
        Log.i(Constants.TAG, "ContactListPagerFragment.reloadAdapterEvent: ");
        if(mIndex == Constants.CONTACTS_ALL)
        {
            contactList = SearchBarController.getContactList();
            reloadSearchAdapter();
        }
    }

    /**
     * Force to show keyboard in current View
     * @author str_oan
     */
    public void showKeyboard()
    {
        Log.i(Constants.TAG, "ContactListFragment.showKeyboard: ");
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * Force to hide keyboard in current activity
     * @author str_oan
     */
    public void hideKeyboard()
    {
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
    }

    public EditText getSearchView()
    {
        return this.searchView;
    }

    public ArrayList<Contact> getContactList()
    {
        return this.contactList;
    }

    public ArrayList<RecentContact> getRecentContactList()
    {
        return this.recentContactList;
    }

    public ArrayList<FavouriteContact> getFavouriteContactList()
    {
        return this.favouriteContactList;
    }
}
