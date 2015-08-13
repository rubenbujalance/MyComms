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
import android.widget.Toast;

import com.squareup.okhttp.Response;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.contacts.view.ContactListFragment;
import com.vodafone.mycomms.contacts.view.ContactListViewArrayAdapter;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ReloadAdapterEvent;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmLDAPSettingsTransactions;
import com.vodafone.mycomms.settings.globalcontacts.GlobalContactsController;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.OKHttpWrapper;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.realm.Realm;
import model.Contact;
import model.GlobalContactsSettings;

/**
 * Created by str_oan on 26/06/2015.
 */
public class SearchBarController {
    private EditText searchView;
    private Activity mActivity;
    private Button cancelButton;
    private LinearLayout layCancel;
    private SearchController mSearchController;
    private ArrayList<Contact> contactList;
    private SharedPreferences sp;
    private LinearLayout laySearchBar;
    private ContactListViewArrayAdapter mAdapter;
    private int mIndex;
    private ListView mListView;
    private boolean isGroupChatSearch;
    private RealmContactTransactions mContactTransactions;
    private ContactListFragment contactListFragment;
    private String profileId;

    private final int drLeft = android.R.drawable.ic_menu_search;
    private final int drRight = R.drawable.ic_action_remove;

    private Realm realm;

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
                    , Realm realm
                    , ContactListFragment contactListFragment
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
        this.realm = realm;
        this.contactListFragment = contactListFragment;

        sp = mActivity.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

        profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);

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
        if(keyWord!=null) {
            if (keyWord.length() == 0) {
                loadAllContactsFromDB();
            } else if (keyWord.length() > 0 && keyWord.length() < 3) {
                loadAllContactsFromDB(keyWord);
            } else if (keyWord.length() >= 3) {
                loadAllContactsFromServer(keyWord);
                loadAllContactsFromDB(keyWord);
            }

            BusProvider.getInstance().post(new ReloadAdapterEvent());
        }
    }

    /**
     * Gets all contacts from Realm DB by given key word
     * @author str_oan
     * @param keyWord (String) -> key word for make search
     */
    private void loadAllContactsFromDB(String keyWord)
    {
        Log.i(Constants.TAG, "SearchBarController.loadAllContactsFromDB: Keyword>" + keyWord);

        if(null == keyWord)
        {
            contactList = mContactTransactions.getAllContacts(realm);
        }
        else
        {
            if(isGroupChatSearch)
            {
                contactList = mSearchController
                        .getContactsByKeyWordWithoutLocalsAndSalesForce(keyWord);
            }
            else
            {
                contactList = mSearchController.getContactsByKeyWord(keyWord);
            }
        }

        validateNoPlatformRecords(contactList);

    }

    private void loadAllContactsFromDB()
    {
        Log.i(Constants.TAG, "SearchBarController.loadAllContactsFromDB: <No Filter>");
        loadAllContactsFromDB(null);
    }

    /**
     * Loads all contacts from server by given key word storing them into Realm
     * @author str_oan
     * @param keyWord -> key word for make search
     */
    private void loadAllContactsFromServer(String keyWord)
    {
        Log.i(Constants.TAG, "SearchBarController.loadAllContactsFromServer: Keyword>" + keyWord);

        //Other platforms: SalesForce, ...
        loadAllContactsFromPlatforms(keyWord);

        //Global Contacts
        if(RealmLDAPSettingsTransactions.haveSettings(profileId, realm)) {
            GlobalContactsSettings settings =
                    RealmLDAPSettingsTransactions.getSettings(profileId, realm);

            String user = settings.getUser();
            String password = settings.getPassword();

            String apiCall = buildRequestForSearchLDAPContacts(keyWord, realm, null);

            loadAllContactsFromLDAP(apiCall, keyWord, false, user, password);
        }
    }

    private void loadAllContactsFromPlatforms(final String keyWord) {
        Log.i(Constants.TAG, "SearchBarController.loadAllContactsFromPlatforms: Keyword>"+keyWord);

        String apiCall = buildRequestForSearchContacts(keyWord);
//        mSearchController.getContactList(apiCall);
//        mSearchController.setConnectionCallback(this);


        OKHttpWrapper.get(apiCall, mActivity, new OKHttpWrapper.HttpCallback() {
            @Override
            public void onFailure(Response response, IOException e) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(Constants.TAG, "SearchBarController.run: ", new Exception(
                                mActivity.getString(R.string.error_reading_data_from_server)));
                        Toast.makeText(mActivity,
                                R.string.error_reading_data_from_server, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onSuccess(Response response) {
                Log.i(Constants.TAG, "SearchBarController.loadAllContactsFromPlatforms: Success - " +
                        response.request().urlString());

                try {
                    String result = response.body().string();

                    if (result != null && result.trim().length() > 0) {

                        JSONObject jsonResponse = new JSONObject(result);

                        ArrayList<Contact> realmContactList =
                                mSearchController.insertContactListInRealm(jsonResponse);

                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadAllContactsFromDB(keyWord);
                                BusProvider.getInstance().post(new ReloadAdapterEvent());
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(Constants.TAG, "SearchBarController.loadAllContactsFromPlatforms: ", e);
                }
            }
        });
    }

    private void loadAllContactsFromLDAP(final String apiCall, final String keyWord,
                                         final boolean retrying, final String user,
                                         final String password)
    {
        Log.i(Constants.TAG, "SearchBarController.loadAllContactsFromLDAP: " +
                "KeyWord>"+keyWord+"; Retrying>"+retrying);

        OKHttpWrapper.get(apiCall, mActivity, new OKHttpWrapper.HttpCallback() {
            @Override
            public void onFailure(Response response, IOException e) {
                Log.i(Constants.TAG, "SearchBarController.loadAllContactsFromLDAP - Failure: " +
                        "KeyWord>" + keyWord + "; Retrying>" + retrying);

                //If have received an Unauthorized response, renew LDAP credentials and retry
                //If already have retried, show LDAP Warning Bar
                if(response!=null && response.code()==401 && !retrying) {

                    //In case of Unauthorized, do a renew LDAP credentials
                    GlobalContactsController gcController = new GlobalContactsController();

                    gcController.callLDAPAuthProcess(user, password, mActivity,
                        new GlobalContactsController.GlobalContactsCallback() {
                            @Override
                            public void onFailure(final String error, final int errorCode) {
                                //If renew failure, delete local token and show bar
                                Log.i(Constants.TAG,
                                        "SearchBarController.loadAllContactsFromLDAP: RENEW");

                                if(errorCode>=400 && errorCode<500) {
                                    RealmLDAPSettingsTransactions.deleteTokenData(profileId, null);
                                    contactListFragment.showLDAPSettingsBar(true);
                                }
                                else {
                                    mActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.e(Constants.TAG, "SearchBarController." +
                                                    "loadAllContactsFromLDAP: (" + errorCode + ") ",
                                                    new Exception(error));
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onSuccess(GlobalContactsSettings settings) {
                                //If renew success, retry search
                                String apiCall =
                                        buildRequestForSearchLDAPContacts(keyWord, null, settings);
                                loadAllContactsFromLDAP(apiCall, keyWord, true, user, password);
                            }
                        });
                }
                else {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(Constants.TAG, "SearchBarController.loadAllContactsFromLDAP: ", new Exception(
                                    mActivity.getString(R.string.error_reading_data_from_server)));
                            Toast.makeText(mActivity, R.string.error_reading_data_from_server,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onSuccess(Response response) {
                Log.i(Constants.TAG, "SearchBarController.loadAllContactsFromLDAP: Success - " +
                        response.request().urlString());
                try
                {
                    String result = response.body().string();

                    if (result != null && result.trim().length()>0)
                    {
                        JSONObject jsonResponse = new JSONObject(result);
                        ArrayList<Contact> realmContactList =
                                mSearchController.insertContactListInRealm(jsonResponse);

                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadAllContactsFromDB(keyWord);
                                BusProvider.getInstance().post(new ReloadAdapterEvent());
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(Constants.TAG, "SearchBarController.loadAllContactsFromLDAP: ", e);
                }
            }
        });
    }

    /**
     * Builds request string for search the contacts
     * @author str_oan
     * @param keyWord (String) -> key word for make search;
     */
    private String buildRequestForSearchContacts(String keyWord)
    {
        String basicCall = Constants.CONTACT_API_GET_CONTACTS_BASIC_CALL;
        String content = sp.getString(Constants.PLATFORMS_SHARED_PREF, "mc");
        content = content.replace("[","").replace("]","").replace("\"","");

        return basicCall+content+"&t="+keyWord;
    }

    /**
     * Builds request string for search the contacts in LDAP
     * @author str_rbm
     * @param keyWord (String) -> key word for make search;
     */
    private String buildRequestForSearchLDAPContacts(String keyWord, Realm realm,
                                                     GlobalContactsSettings ldapSettings)
    {

        if(ldapSettings==null) {
            String profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);

            ldapSettings =
                    RealmLDAPSettingsTransactions.getSettings(profileId, realm);
        }

        String apiCall = Constants.CONTACT_API_GET_CONTACTS_BASIC_CALL;

        try {
            apiCall += Constants.LDAP_API_CALL_PLATFORM;
            apiCall += "&lt=" + URLEncoder.encode(ldapSettings.getToken(), "utf-8");
            apiCall += "&tt=" + URLEncoder.encode(ldapSettings.getTokenType(), "utf-8");
            apiCall += "&url=" + URLEncoder.encode(ldapSettings.getUrl(), "utf-8");
            apiCall += "&t=" + URLEncoder.encode(keyWord, "utf-8");
        } catch (Exception e) {
            Log.e(Constants.TAG, "SearchBarController.buildRequestForSearchLDAPContacts: ", e);
        }

        return apiCall;
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

    private void validateNoPlatformRecords(ArrayList<Contact> contactList) {
        boolean isMyComms = false;
        boolean isSalesForce = false;
        boolean isGlobal = false;
        boolean isLocal = false;
        int salesForcePosition = 0;
        int globalPosition = 0;
        int localPosition = 0;
        String platform;

        for (int i=0;i<contactList.size();i++){
            platform = contactList.get(i).getPlatform();

            if (!isMyComms && platform.equals(Constants.PLATFORM_MY_COMMS)) {
                isMyComms = true;
            } else if (!isSalesForce && platform.equals(Constants.PLATFORM_SALES_FORCE)){
                isSalesForce = true;
                salesForcePosition = i;
            } else if (!isGlobal && platform.equals(Constants.PLATFORM_GLOBAL_CONTACTS)){
                isGlobal = true;
                globalPosition = i;
            } else if (!isLocal && platform.equals(Constants.PLATFORM_LOCAL)) {
                isLocal = true;
                localPosition = i;
            }
        }

        int size =  contactList.size();
        if (!isLocal){
            contactList.add(size, createNoRecordsContact(Constants.PLATFORM_LOCAL));
        }
        if (!isGlobal){
            if (localPosition == 0){
                localPosition = salesForcePosition;
                if (localPosition == 0){
                    localPosition = size;
                }
            }
            contactList.add(localPosition, createNoRecordsContact(Constants.PLATFORM_GLOBAL_CONTACTS));
        }
        if (!isSalesForce){
            if (globalPosition == 0){
                globalPosition = localPosition;
                if (globalPosition == 0){
                    globalPosition = size;
                }
            }
            contactList.add(globalPosition, createNoRecordsContact(Constants.PLATFORM_SALES_FORCE));
        }
        if (!isMyComms) {
            contactList.add(0, createNoRecordsContact(Constants.PLATFORM_MY_COMMS));
        }
    }

    private Contact createNoRecordsContact(String platform) {
        Contact contact = new Contact();
        contact.setProfileId(profileId);
        contact.setPlatform(platform);
        contact.setContactId(platform);
        contact.setId(profileId + "_" + platform);
        contact.setFirstName(mActivity.getResources().getString(R.string.no_search_records));
        contact.setLastName("");

        return contact;
    }
}
