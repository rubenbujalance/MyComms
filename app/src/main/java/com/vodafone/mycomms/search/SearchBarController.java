package com.vodafone.mycomms.search;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.contacts.connection.ISearchConnectionCallback;
import com.vodafone.mycomms.contacts.view.ContactListViewArrayAdapter;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ReloadAdapterEvent;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import model.Contact;

/**
 * Created by str_oan on 26/06/2015.
 */
public class SearchBarController implements ISearchConnectionCallback
{
    private EditText searchView;
    private Activity mActivity;
    private Button cancelButton;
    private LinearLayout layCancel;
    private SearchController mSearchController;
    private ArrayList<Contact> contactList;
    private String apiCall;
    private SharedPreferences sp;
    private LinearLayout laySearchBar;
    private ContactListViewArrayAdapter mAdapter;
    private int mIndex;
    private ListView mListView;
    private boolean isGroupChatSearch;
    private RealmContactTransactions mContactTransactions;

    private final int drLeft = android.R.drawable.ic_menu_search;
    private final int drRight = R.drawable.ic_action_remove;


    public SearchBarController
            (
                    Activity activity
                    , RealmContactTransactions contactTransactions
                    , ArrayList<Contact> contactList
                    , SearchController searchController
                    , ContactListViewArrayAdapter adapter
                    , int index
                    , ListView listView
                    , boolean isGroupChatSearch
            )
    {
        this.mActivity = activity;
        this.mContactTransactions = contactTransactions;
        this.contactList = contactList;
        this.mSearchController = searchController;
        this.mAdapter = adapter;
        this.mIndex = index;
        this.mListView = listView;
        this.isGroupChatSearch = isGroupChatSearch;

        sp = mActivity.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

    }

    /**
     * Sets events to search bar. Makes response on touch, onKey and on text change.
     * @author str_oan
     */
    public void setSearchBarEvents()
    {

        searchView.setCompoundDrawablesWithIntrinsicBounds(drLeft, 0, 0, 0);


        searchView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                searchView.requestFocus();
                showKeyboard();
                cancelButton.setVisibility(View.VISIBLE);

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Will hide X button for delete searched text
                    if (null != searchView.getCompoundDrawables()[DRAWABLE_RIGHT] && event.getRawX() >= (searchView.getRight() - searchView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        Log.d("onTouch() -> ", "You have pressed right drawable!");
                        searchView.setText("");
                        searchView.setCompoundDrawablesWithIntrinsicBounds(drLeft, 0, 0, 0);
                        return true;
                    } else {
                        Log.d("onTouch() -> ", "You have pressed other part of ET!");
                        return true;
                    }
                } else {
                    return true;
                }
            }
        });

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(this.getClass().getSimpleName() + " -> onTextChanged", "Input is: " + searchView.getText().toString());
                if (searchView.getText().length() == 1) {
                    searchView.setCompoundDrawablesWithIntrinsicBounds(drLeft, 0, drRight, 0);
                    layCancel.setVisibility(View.VISIBLE);
                } else if (searchView.getText().length() == 0) {
                    searchView.setCompoundDrawablesWithIntrinsicBounds(drLeft, 0, 0, 0);
                }

                searchAllContacts(searchView.getText().toString());
                BusProvider.getInstance().post(new ReloadAdapterEvent());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        searchView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    hideKeyboard();
                }
                return false;
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSearchBarContent();
                hideKeyboard();

            }
        });

    }
    /**
     * Initiate each component what belong to Search View
     * @author str_oan
     */
    public void initiateComponentsForSearchView(View v)
    {
        searchView = (EditText) v.findViewById(R.id.et_search);
        cancelButton = (Button) v.findViewById(R.id.btn_cancel);
        layCancel = (LinearLayout) v.findViewById(R.id.lay_cancel);

        laySearchBar = (LinearLayout) v.findViewById(R.id.lay_search_bar_container);

        if(mIndex != Constants.CONTACTS_ALL)
        {
            laySearchBar.setVisibility(View.GONE);
            hideSearchBarContent();
        }

        layCancel.setVisibility(View.GONE);

        if(mIndex == Constants.CONTACTS_ALL && Constants.isSearchBarFocusRequested)
        {
            showKeyboard();
            //Constants.isSearchBarFocusRequested = false;
        }
    }


    /**
     * Force to show keyboard in current View
     * @author str_oan
     */
    public void showKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context
            .INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);

    }

    /**
     * Force to hide keyboard in current activity
     * @author str_oan
     */
    public void hideKeyboard()
    {
        InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(mActivity
                .INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
    }

    /**
     * Searches all contacts by given key word. Calls the rest of functions for perform each type
     * of search action
     * @param keyWord (String) -> key word for make search
     */
    private void searchAllContacts(String keyWord)
    {
        if(keyWord.length() == 0)
        {
            loadAllContactsFromDB();
        }
        else if(keyWord.length() > 0 && keyWord.length() < 3)
        {
            loadAllContactsFromDB(keyWord);
        }
        else if(keyWord.length() >= 3)
        {
            loadAllContactsFromServer(keyWord);
            if(!isGroupChatSearch)
            {
                //loadLocalContacts(keyWord);
            }
            loadAllContactsFromDB(keyWord);
        }
    }

    /**
     * Gets all contacts from Realm DB by given key word
     * @author str_oan
     * @param keyWord (String) -> key word for make search
     */
    private void loadAllContactsFromDB(String keyWord)
    {
        if(null == keyWord)
        {
            contactList = mContactTransactions.getAllContacts();
        }
        else
        {
            if(isGroupChatSearch)
            {
                contactList = mSearchController.getContactsByKeyWordWithoutLocalsAndSalesForce(keyWord);
            }
            else
            {
                contactList = mSearchController.getContactsByKeyWord(keyWord);
            }
        }

    }

    private void loadAllContactsFromDB()
    {
        loadAllContactsFromDB(null);
    }

    /**
     * Loads all contacts from server by given key word storing them into Realm
     * @author str_oan
     * @param keyWord -> key word for make search
     */
    private void loadAllContactsFromServer(String keyWord)
    {
        buildRequestForSearchContacts(keyWord);
        mSearchController.getContactList(apiCall);
        mSearchController.setConnectionCallback(this);
    }

    /**
     * Builds request string for search the contacts
     * @author str_oan
     * @param keyWord (String) -> key word for make search;
     */
    private void buildRequestForSearchContacts(String keyWord)
    {
        String basicCall = Constants.CONTACT_API_GET_CONTACTS_BASIC_CALL;
        String content = sp.getString(Constants.PLATFORMS_SHARED_PREF, "mc");
        content = content.replace("[","").replace("]","").replace("\"","");
        apiCall = basicCall+content+"&t="+keyWord;
    }

    /**
     * Gets all contacts from Local Device DB by given key word
     * @author str_oan
     * @param keyWord (String) -> key word for make search
     * @return (ArrayList<Contact>) -> local contacts if any, otherwise returns empty list
     * @deprecated Now all contacts are saved on Realm at the application start
     */
    private ArrayList<Contact> loadLocalContacts(String keyWord)
    {
        ArrayList<Contact> contactArrayList = mSearchController.getLocalContactsByKeyWord(keyWord);
        mSearchController.storeContactsIntoRealm(contactArrayList);
        return contactArrayList;
    }

    public void hideSearchBarContent()
    {
        layCancel.setVisibility(View.GONE);
        searchView.setCompoundDrawablesWithIntrinsicBounds(drLeft, 0, 0, 0);
        searchView.setText("");
    }


    @Override
    public void onSearchContactsResponse(ArrayList<Contact> contactList, boolean morePages, int offsetPaging) {

    }

    @Override
    public void onConnectionNotAvailable() {

    }

    public ArrayList<Contact> getContactList()
    {
        return this.contactList;
    }

    /**
     * Sorts list of contacts by First Name + Last Name alphabetically
     * @param contactList (List<Contact>) -> list which will be sorted
     */
    private void sortContacts(List<Contact> contactList)
    {
        Collections.sort(contactList, new Comparator<Contact>() {
            @Override
            public int compare(Contact lhs, Contact rhs) {
                String name1 = lhs.getFirstName() + lhs.getLastName();
                String name2 = rhs.getFirstName() + rhs.getLastName();
                return name1.compareTo(name2);
            }
        });
    }
}
