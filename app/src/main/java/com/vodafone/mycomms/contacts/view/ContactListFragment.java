package com.vodafone.mycomms.contacts.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.fortysevendeg.swipelistview.SwipeListView;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.contacts.connection.ContactController;
import com.vodafone.mycomms.contacts.connection.IContactsConnectionCallback;
import com.vodafone.mycomms.contacts.detail.ContactDetailMainActivity;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UserSecurity;
import com.vodafone.mycomms.view.tab.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;

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
public class ContactListFragment extends ListFragment implements IContactsConnectionCallback {

    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;
    private Realm realm;
    private ArrayList<Contact> contactList;
    private ArrayList<FavouriteContact> favouriteContactList;
    private ArrayList<RecentContact> recentContactList;
    protected Handler handler = new Handler();
    private ContactController mContactController;
    private ContactListViewArrayAdapter adapter;
    private SwipeListView swipeListView;
    private String accessToken;
    private String apiCall;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private int mIndex;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

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

       /*final SwipeListView swipeListView = (SwipeListView) v.findViewById(android.R.id.list);
       swipeListView.setSwipeListViewListener(new BaseSwipeListViewListener() {
           @Override
           public void onOpened(int position, boolean toRight) {
               View v = swipeListView.getChildAt(position);
               swipeListView.getChildAt(position).setBackgroundColor(Color.CYAN);
               final ImageView favContact = (ImageView) swipeListView.findViewById(R.id.fav_contact);
               favContact.setOnClickListener((new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       Log.i(Constants.TAG, "ContactListFragment.onClick: TESTING");
                       favContact.setImageDrawable(getResources().getDrawable(R.drawable.abc_btn_rating_star_on_mtrl_alpha));
                   }
               }));
           }

           @Override
           public void onClosed(int position, boolean fromRight) {
           }

           @Override
           public void onListChanged() {
           }
       });*/

       return v;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ContactListFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(Constants.TAG, "ContactListFragment.onCreate: ");
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mIndex = getArguments().getInt(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        accessToken = UserSecurity.getAccessToken(getActivity());
        realm = Realm.getInstance(getActivity());
        mContactController = new ContactController(this,realm);
        apiCall = Constants.CONTACT_API_GET_CONTACTS;
        mContactController.getContactList(accessToken, apiCall);
        mContactController.setConnectionCallback(this);

        setListAdapterTabs();
        //TODO: CALL A LA API PARA GET DE CONTACTOS. CUANDO ACABE LLAMAR CARGA DE LISTA POR SEGUNDA VEZ + setListAdapter UNDER DEVELOPMENT
        //TODO: CALL A LA API PARA GET DE FAVORITOS. CUANDO ACABE LLAMAR CARGA DE LISTA POR SEGUNDA VEZ + setListAdapter
        //TODO: CALL A LA API PARA GET DE RECIENTES. CUANDO ACABE LLAMAR CARGA DE LISTA POR SEGUNDA VEZ + setListAdapter
        //TODO: CALL A LA BD PARA CARGAR LISTA DE CONTACTOS (VIEJOS)
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
            Intent in = new Intent(getActivity(), ContactDetailMainActivity.class);
            //TODO: Implement back navigation
            if(mIndex == Constants.CONTACTS_ALL) {
                in.putExtra(Constants.CONTACT_ID,contactList.get(position).getId() );
            } else if (mIndex == Constants.CONTACTS_RECENT) {
                in.putExtra(Constants.CONTACT_ID,recentContactList.get(position).getId() );
            } else if (mIndex == Constants.CONTACTS_FAVOURITE) { {
                in.putExtra(Constants.CONTACT_ID,favouriteContactList.get(position).getId() );
            }}
            startActivity(in);
        }
    }
/*
    @Override
    public void onRefresh() {
        //TODO: Refresh function here. Filter tab
        handler.postDelayed(testIsGood, 5000);
    }

    public Runnable testIsGood = new Runnable() {
        @Override
        public void run() {
            Log.wtf(Constants.TAG, "ContactListFragment.run: TEEEEESTINGGGG");
            mSwipeRefreshLayout.setRefreshing(false);
        }
    };*/

    @Override
    public void onConnectionNotAvailable() {
        Log.d(Constants.TAG, "ContactListFragment.onConnectionNotAvailable: ");
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
    }

    public void setListAdapterTabs(){
        Log.i(Constants.TAG, "ContactListFragment.setListAdapterTabs: apiCall " + apiCall);
        if(mIndex == Constants.CONTACTS_FAVOURITE) {
            favouriteContactList = mContactController.getAllFavouriteContacts();
            if (favouriteContactList.size()!=0) {
                setListAdapter(new ContactFavouriteListViewArrayAdapter(getActivity().getApplicationContext(),
                        favouriteContactList));
            }
        }else if(mIndex == Constants.CONTACTS_RECENT){
            recentContactList = mContactController.getAllRecentContacts();
            if (recentContactList.size()!=0) {
               setListAdapter(new RecentListViewArrayAdapter(getActivity().getApplicationContext(), recentContactList));
            }
        }else if(mIndex == Constants.CONTACTS_ALL){
            contactList = mContactController.getAllContacts();
            if (contactList.size()!=0) {
                setListAdapter(new ContactListViewArrayAdapter(getActivity().getApplicationContext(), contactList));
            }
        }
    }

    @Override
    public void onContactsResponse(List<Contact> contactList) {
        Log.i(Constants.TAG, "onContactsResponse: " + apiCall);

        if(apiCall == Constants.CONTACT_API_GET_FAVOURITES) {

        }else if(apiCall == Constants.CONTACT_API_GET_CONTACTS){
            apiCall = Constants.CONTACT_API_GET_FAVOURITES;
            mContactController.getFavouritesList(accessToken, apiCall);
        }

        setListAdapterTabs();
    }
}
