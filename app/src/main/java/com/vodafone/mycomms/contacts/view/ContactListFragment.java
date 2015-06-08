package com.vodafone.mycomms.contacts.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chat.ChatMainActivity;
import com.vodafone.mycomms.contacts.connection.ContactController;
import com.vodafone.mycomms.contacts.detail.ContactDetailMainActivity;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;
import com.vodafone.mycomms.view.tab.SlidingTabLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

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
public class ContactListFragment extends ListFragment{

    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;
    private Realm realm;
    private ContactController mContactController;
    private ArrayList<Contact> contactList;
    private ArrayList<FavouriteContact> favouriteContactList;
    private ArrayList<RecentContact> recentContactList;
    protected Handler handler = new Handler();

    private ContactListViewArrayAdapter adapter;
    private ListView listView;
    private Parcelable state;
    private TextView emptyText;
    private EditText searchView;
    private Button cancelButton;
    private LinearLayout layCancel;

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
       initiateComponentsForSearchView(v);
       setSearchBarEvents(v);
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
        super.onCreate(savedInstanceState);
        realm = Realm.getInstance(getActivity());
        mContactController = new ContactController(getActivity(),realm);
        if (getArguments() != null) {
            mIndex = getArguments().getInt(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        setListAdapterTabs();
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
            if(mIndex == Constants.CONTACTS_ALL) {
                in.putExtra(Constants.CONTACT_ID,contactList.get(position).getId() );
                startActivity(in);
            } else if (mIndex == Constants.CONTACTS_RECENT) {
                try {
                    String action = recentContactList.get(position).getAction();
                    if (action.compareTo(Constants.CONTACTS_ACTION_CALL) == 0) {
                        String strPhones = recentContactList.get(position).getPhones();
                        if (strPhones != null) {
                            JSONArray jPhones = new JSONArray(strPhones);
                            String phone = (String)((JSONObject) jPhones.get(0)).get(Constants.CONTACT_PHONES);
                            Utils.launchCall(phone, getActivity());
                        }
                    }
                    else if (action.compareTo(Constants.CONTACTS_ACTION_SMS) == 0) {
                        /*String strPhones = recentContactList.get(position).getPhones();
                        if (strPhones != null) {
                            JSONArray jPhones = new JSONArray(strPhones);
                            String phone = (String)((JSONObject) jPhones.get(0)).get(Constants.CONTACT_PHONES);
                            Utils.launchSms(phone, getActivity());
                        }*/
                        in = new Intent(getActivity(), ChatMainActivity.class);
                        in.putExtra(Constants.CHAT_FIELD_CONTACT_ID, recentContactList.get(position).getId());
                        in.putExtra(Constants.CHAT_PREVIOUS_VIEW, Constants.CHAT_VIEW_CONTACT_LIST);
                        startActivity(in);
                    }
                    else if (action.compareTo(Constants.CONTACTS_ACTION_EMAIL) == 0) {
                        String strEmails = recentContactList.get(position).getEmails();
                        if (strEmails != null) {
                            JSONArray jPhones = new JSONArray(strEmails);
                            String email = (String)((JSONObject) jPhones.get(0)).get(Constants.CONTACT_EMAILS);
                            Utils.launchEmail(email, getActivity());
                        }
                    }
                } catch (Exception ex) {
                    Log.e(Constants.TAG, "ContactListFragment.onListItemClick: ", ex);
                }
            } else if (mIndex == Constants.CONTACTS_FAVOURITE) { {
                in.putExtra(Constants.CONTACT_ID,favouriteContactList.get(position).getId() );
                startActivity(in);
            }}

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
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }

    private void initiateComponentsForSearchView(View view)
    {
        searchView = (EditText) view.findViewById(R.id.et_search);
        cancelButton = (Button) view.findViewById(R.id.btn_cancel);
        layCancel = (LinearLayout) view.findViewById(R.id.lay_cancel);

        layCancel.setVisibility(View.GONE);
    }

    private void setSearchBarEvents(View view)
    {
        final int drLeft = R.drawable.btn_nav_bar_search;
        final int drRight = R.drawable.ic_action_remove;
        searchView.setCompoundDrawablesWithIntrinsicBounds(drLeft, 0, 0, 0);


        searchView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                searchView.setFocusable(true);
                showKeyboard(v);
                cancelButton.setVisibility(View.VISIBLE);

                if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    if (null != searchView.getCompoundDrawables()[DRAWABLE_RIGHT] && event.getRawX() >= (searchView.getRight() - searchView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        Log.d("onTouch() -> ", "You have pressed right drawable!");
                        searchView.setText("");
                        searchView.setCompoundDrawablesWithIntrinsicBounds(drLeft, 0, 0, 0);
                        return true;
                    }
                    else
                    {
                        Log.d("onTouch() -> ", "You have pressed other part of ET!");
                        return true;
                    }
                }
                else
                {
                    return true;
                }
            }
        });

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                Log.i(this.getClass().getSimpleName() + " -> onTextChanged", "Input is: " + searchView.getText().toString());
                if (searchView.getText().length() == 1)
                {
                    searchView.setCompoundDrawablesWithIntrinsicBounds(drLeft, 0, drRight, 0);
                    layCancel.setVisibility(View.VISIBLE);
                }
                else if (searchView.getText().length() == 0)
                {
                    searchView.setCompoundDrawablesWithIntrinsicBounds(drLeft, 0, 0, 0);
                }

                if(null != contactList)
                {
                    contactList.clear();
                }

                contactList = loadAllContactsFromBD(searchView.getText().toString());
                contactList.addAll(loadLocalContacts(searchView.getText().toString()));
                reloadAdapter();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layCancel.setVisibility(View.GONE);
                searchView.setCompoundDrawablesWithIntrinsicBounds(drLeft, 0, 0, 0);
                searchView.setText("");
                hideKeyboard(v);

            }
        });

    }

    private void showKeyboard(View view)
    {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(view.getContext().INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard(View view)
    {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(view.getContext().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
    }



    private void reloadAdapter()
    {
        adapter = new ContactListViewArrayAdapter(getActivity().getApplicationContext(), contactList);
        if (contactList!=null) {
            if (listView!=null)
                state = listView.onSaveInstanceState();
            if (adapter != null){
                setListAdapter(adapter);
                if (state!=null)
                    listView.onRestoreInstanceState(state);
            } else {
                adapter = new ContactListViewArrayAdapter(getActivity().getApplicationContext(), contactList);
                setListAdapter(adapter);
                if (state!=null)
                    listView.onRestoreInstanceState(state);
            }
        }
    }

    //TODO Implement this part in case Server Search is needed;
    private void searchContactsFromServer(String keyWord)
    {
        HashMap<String, Object> header = null;
        HashMap<String, Object> body = new HashMap<>();
        body.put("p", "mc");
        body.put("o", "0");

        if(null != keyWord)
        {
            if(keyWord.length() > 0)
            {
                //new DownloadContactsFromServer().execute(header,body);
            }
            else
            {
                loadAllContactsFromBD();
            }
        }
    }

    public void setListAdapterTabs(){
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
               setListAdapter(new RecentListViewArrayAdapter(getActivity().getApplicationContext(), recentContactList));
            }
        }else if(mIndex == Constants.CONTACTS_ALL){
            if (emptyText!=null)
                emptyText.setText("");
            contactList = loadAllContactsFromBD(null);
            reloadAdapter();
        }
    }


    private ArrayList<Contact> loadAllContactsFromBD(String keyWord)
    {
        ArrayList<Contact> contactArrayList;
        if(null == keyWord)
        {
            contactArrayList = mContactController.getAllContacts();
        }
        else
        {
            contactArrayList = mContactController.getContactsByKeyWord(keyWord);
        }

        return contactArrayList;
    }

    private void loadAllContactsFromBD()
    {
        loadAllContactsFromBD(null);
    }

    private ArrayList<Contact> loadLocalContacts(String keyWord)
    {
        ArrayList<Contact> contactArrayList = new ArrayList<>();
        if(keyWord.length() >= 3)
        {
            contactArrayList = mContactController.getLocalContactsByKeyWord(keyWord);
        }
        return contactArrayList;
    }



    private void loadAllContactsFromServer(HashMap<String,Object> contactObjects)
    {

    }

    /**
    private class DownloadContactsFromServer extends AsyncTask<HashMap<String,Object>, Void, HashMap<String,Object>> {
        @Override
        protected HashMap<String,Object> doInBackground(HashMap<String,Object>[] params)
        {
            return APIWrapper.httpPostAPI("/api/profile", params[0], params[1], getActivity());
        }

        @Override
        protected void onPostExecute(HashMap<String,Object> result)
        {
            loadAllContactsFromServer(result);
        }
    }
     **/

}
