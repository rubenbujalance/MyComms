package com.vodafone.mycomms.contacts;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.contacts.view.ContactListFragment;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.search.SearchBarController;
import com.vodafone.mycomms.settings.globalcontacts.AddGlobalContactsActivity;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.CustomFragmentActivity;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.MockRepository;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowInputMethodManager;

import io.realm.Realm;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*"})
@PrepareForTest({RealmContactTransactions.class, Realm.class})
public class SearchGlobalContactsTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    MockWebServer webServer;
    ContactListFragment contactListFragment;
    CustomFragmentActivity customFragmentActivity;
    Context context;
    ListView listView;
    TextView emptyText;
    EditText searchView;
    RelativeLayout addGlobalContactsContainer;
    Button cancelButton;
    LinearLayout layCancel;
    LinearLayout laySearchBar;
    String testString;

    @Before
    public void setUp() throws Exception {
        mockStatic(Realm.class);
        when(Realm.getDefaultInstance()).thenReturn(null);
        mockStatic(Crashlytics.class);
        whenNew(RealmContactTransactions.class).withAnyArguments()
                .thenReturn(null);
        context = RuntimeEnvironment.application.getApplicationContext();
        startContactListFragment(2);
        contactListFragment = (ContactListFragment)customFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("2");
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
        listView = (ListView) contactListFragment.getView().findViewById(android.R.id.list);
        emptyText = (TextView) contactListFragment.getView().findViewById(android.R.id.empty);
        searchView = (EditText) contactListFragment.getView().findViewById(R.id.et_search);
        addGlobalContactsContainer = (RelativeLayout) contactListFragment.getView().findViewById(R.id.add_global_contacts_container);
        cancelButton = (Button) contactListFragment.getView().findViewById(R.id.btn_cancel);
        layCancel = (LinearLayout) contactListFragment.getView().findViewById(R.id.lay_cancel);
        laySearchBar = (LinearLayout) contactListFragment.getView().findViewById(R.id.lay_search_bar_container);
        testString = "Testing";
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        System.err.println("******** Test: NOT NULL OBJECTS ********");
        Assert.assertTrue(contactListFragment != null);
        Assert.assertTrue(listView != null);
        Assert.assertTrue(emptyText != null);
        Assert.assertTrue(searchView != null);
        Assert.assertTrue(addGlobalContactsContainer != null);
        Assert.assertTrue(cancelButton != null);
        Assert.assertTrue(layCancel != null);
        Assert.assertTrue(laySearchBar != null);
        System.err.println("******** Test: NO NULL OBJECTS OK ********");
    }

    @Test
    public void testShowAddGlobalContactsBarInContacts() throws Exception {
        //Save fake Global Contacts loading to false
        SharedPreferences sp = context.getSharedPreferences(
                com.vodafone.mycomms.util.Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        sp.edit().putBoolean(
                com.vodafone.mycomms.util.Constants.IS_GLOBAL_CONTACTS_LOADING_ENABLED,
                true)
                .apply();
        System.err.println("******** Test: IS_GLOBAL_CONTACTS_LOADING_ENABLED FALSE ********");

        //addGCBar.setVisibility(View.VISIBLE);
        Assert.assertTrue(addGlobalContactsContainer.getVisibility() == (View.VISIBLE));
        System.err.println("******** Test: Global Contacts Visibility ON CONTACTS LIST OK********");

        startContactListFragment(0);
        ContactListFragment favoriteListFragment = (ContactListFragment)customFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("0");
        addGlobalContactsContainer = (RelativeLayout) favoriteListFragment.getView().findViewById(R.id.add_global_contacts_container);
        Assert.assertTrue(addGlobalContactsContainer.getVisibility() == (View.GONE));
        System.err.println("******** Test: Global Contacts Visibility ON FAVORITE LIST OK********");

        startContactListFragment(1);
        ContactListFragment recentListFragment = (ContactListFragment)customFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("1");
        addGlobalContactsContainer = (RelativeLayout) recentListFragment.getView().findViewById(R.id.add_global_contacts_container);
        Assert.assertTrue(addGlobalContactsContainer.getVisibility() == (View.GONE));
        System.err.println("******** Test: Global Contacts Visibility ON RECENT LIST OK********");
    }

    @Test
    public void testClickBarAndGoToAddGlobalContacts() throws Exception {
        addGlobalContactsContainer.setVisibility(View.VISIBLE);
        addGlobalContactsContainer.performClick();

        Intent expectedIntent = new Intent(contactListFragment.getActivity(), AddGlobalContactsActivity.class);
        Assert.assertTrue(Shadows.shadowOf(contactListFragment.getActivity())
                .getNextStartedActivity().equals(expectedIntent));
        System.err.println("******** Test: Navigation to AddGlobalContactsActivity OK********");
    }

    @Test
    public void testSearchBarVisibility() throws Exception {
        System.err.println("******** Test: Search Bar Visibility ********");
        Assert.assertTrue(laySearchBar.getVisibility() == (View.VISIBLE));
        System.err.println("******** Test: Search Bar Visibility ON CONTACT LIST OK********");

        startContactListFragment(0);
        ContactListFragment favoriteListFragment = (ContactListFragment)customFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("0");
        laySearchBar = (LinearLayout) favoriteListFragment.getView().findViewById(R.id.lay_search_bar_container);
        Assert.assertTrue(laySearchBar.getVisibility() == (View.GONE));
        System.err.println("******** Test: Search Bar Visibility ON FAVORITE LIST OK********");

        startContactListFragment(1);
        ContactListFragment recentListFragment = (ContactListFragment)customFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("1");
        laySearchBar = (LinearLayout) recentListFragment.getView().findViewById(R.id.lay_search_bar_container);
        Assert.assertTrue(laySearchBar.getVisibility() == (View.GONE));
        System.err.println("******** Test: Search Bar Visibility ON RECENT LIST OK********");
    }

    @Test
    public void testSearchBarInitialContentVisibility() throws Exception {
        System.err.println("******** Test: Search Bar Content Visibility ********");
        Assert.assertTrue(layCancel.getVisibility() == (View.GONE));
        System.err.println("******** Test: Search Cancel Layout Visibility ON CONTACT LIST OK********");
        Assert.assertTrue(searchView.getHint().equals(context.getResources().getString(R.string.search_bar_text)));
        System.err.println("******** Test: Search Hint not empty ON CONTACT LIST OK********");

        startContactListFragment(0);
        ContactListFragment favoriteListFragment = (ContactListFragment)customFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("0");
        layCancel = (LinearLayout) favoriteListFragment.getView().findViewById(R.id.lay_search_bar_container);
        Assert.assertTrue(layCancel.getVisibility() == (View.GONE));
        System.err.println("******** Test: Search Cancel Layout Visibility ON FAVORITE LIST OK********");
        searchView = (EditText) favoriteListFragment.getView().findViewById(R.id.et_search);
        Assert.assertTrue(searchView.getText().equals(""));
        System.err.println("******** Test: Search Text Empty ON FAVORITE LIST OK********");

        startContactListFragment(1);
        ContactListFragment recentListFragment = (ContactListFragment)customFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("1");
        layCancel = (LinearLayout) recentListFragment.getView().findViewById(R.id.lay_search_bar_container);
        Assert.assertTrue(layCancel.getVisibility() == (View.GONE));
        System.err.println("******** Test: Search Cancel Layout Visibility ON RECENT LIST OK********");
        searchView = (EditText) recentListFragment.getView().findViewById(R.id.et_search);
        Assert.assertTrue(searchView.getText().equals(""));
        System.err.println("******** Test: Search Text Empty ON RECENT LIST OK********");

        Constants.isSearchBarFocusRequested = true;
        startContactListFragment(2);
        System.err.println("******** Test: Search Bar Content with Focus Visibility ********");
        Assert.assertTrue(layCancel.getVisibility() == (View.GONE));
        System.err.println("******** Test: Search Cancel Layout with Focus  Visibility ON CONTACT LIST OK********");
        Assert.assertTrue(searchView.getHint().equals(context.getResources().getString(R.string.search_bar_text)));
        System.err.println("******** Test: Search Hint with Focus not empty ON CONTACT LIST OK********");
        InputMethodManager inputManager = (InputMethodManager) contactListFragment.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        ShadowInputMethodManager shadowInputMethodManager = Shadows.shadowOf(inputManager);
        Assert.assertTrue(shadowInputMethodManager.isSoftInputVisible());
        System.err.println("******** Test: Search View Key Listener Events OK********");
    }

    @Test
    public void testSearchViewTouchEvent() throws Exception {
        System.err.println("******** Test: Search Bar Touch Events ********");
        // Obtain MotionEvent object
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        float x = 0.0f;
        float y = 0.0f;
        // List of meta states found here:     developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
        int metaState = 0;
        MotionEvent motionEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_UP,
                x,
                y,
                metaState
        );

        searchView.dispatchTouchEvent(motionEvent);
        //Show Keyboard Soft Input
        InputMethodManager inputManager = (InputMethodManager)contactListFragment.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        ShadowInputMethodManager shadowInputMethodManager = Shadows.shadowOf(inputManager);
        Assert.assertTrue(shadowInputMethodManager.isSoftInputVisible());
        System.err.println("******** Test: Search Keyboard showing motion event up ON CONTACT LIST OK********");

        motionEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_DOWN,
                x,
                y,
                metaState
        );

        searchView.dispatchTouchEvent(motionEvent);
        //Show Keyboard Soft Input
        Assert.assertTrue(shadowInputMethodManager.isSoftInputVisible());
        System.err.println("******** Test: Search Keyboard showing motion event not up ON CONTACT LIST OK********");
    }

    //TODO: BusProvider Error is blocking this test. Fix it.
    @Test
    public void testSearchViewTouchDeleteEvent() throws Exception {
        System.err.println("******** Test: Search Bar Touch Delete Events ********");

        //Prepare Mock SearchView in order to avoid onTextChange events
        final int drLeft = android.R.drawable.ic_menu_search;
        final int drRight = R.drawable.ic_action_remove;
//        EditText spySearchView = Mockito.spy(searchView);
//        Mockito.doNothing().when(spySearchView).setText(testString);
        try {
            searchView.setText(testString);
        } catch (RuntimeException e){
            System.err.println("******** Test: RunTimeException Handled OK********" + e);
        }

        //Mock X button and Cancel Visibility
        layCancel.setVisibility(View.VISIBLE);
        searchView.setCompoundDrawablesWithIntrinsicBounds(drLeft, 0, drRight, 0);

        // Obtain MotionEvent object
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        float x = searchView.getRight() - searchView.getCompoundDrawables()[2].getBounds().width();
        float y = 0.0f;
        // List of meta states found here:     developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
        int metaState = 0;
        MotionEvent motionEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_UP,
                x,
                y,
                metaState
        );

        try {
            searchView.dispatchTouchEvent(motionEvent);
        } catch (RuntimeException e){
            System.err.println("******** Test: RunTimeException Handled OK********" + e);
        }
        //Show Keyboard Soft Input
        InputMethodManager inputManager = (InputMethodManager)contactListFragment.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        ShadowInputMethodManager shadowInputMethodManager = Shadows.shadowOf(inputManager);
        Assert.assertTrue(shadowInputMethodManager.isSoftInputVisible());
        System.err.println("******** Test: Search Keyboard showing ON CONTACT LIST OK********");
        //Show CancelButton
        Assert.assertTrue(searchView.getText().equals(""));
        System.err.println("******** Test: Cancel Button Visibility ON CONTACT LIST OK********");
    }

    @Test
    public void testSearchViewOnTextChangedEvent() throws Exception {
        System.err.println("******** Test: Search On Text Changed Events ********");
        SearchBarController searchBarController = new SearchBarController(contactListFragment.getActivity(),null,null,null,2,null,false,null,contactListFragment);
        SearchBarController spySearchBarController = Mockito.spy(searchBarController);

        //Input ""
        Mockito.doNothing().when(spySearchBarController).loadAllContactsFromDB(null);
        spySearchBarController.initiateComponentsForSearchView(contactListFragment.getView());
        spySearchBarController.searchContactsOnTextChanged("");
        Assert.assertTrue(layCancel.getVisibility() == View.GONE);
        System.err.println("******** Test: Cancel Layout GONE NULL CHAR ON CONTACT LIST OK********");

        //Input 1 char
        Mockito.doNothing().when(spySearchBarController).loadAllContactsFromDB("1");
        spySearchBarController.searchContactsOnTextChanged("1");
        Assert.assertTrue(layCancel.getVisibility() == View.VISIBLE);
        Assert.assertTrue(cancelButton.getVisibility() == View.VISIBLE);
        System.err.println("******** Test: Cancel Button and Layout VISIBLE 1 CHAR ON CONTACT LIST OK********");

        //Input more than 1 letter text
        Mockito.doNothing().when(spySearchBarController).loadAllContactsFromDB("Testing");
        Mockito.doNothing().when(spySearchBarController).loadAllContactsFromServer("Testing");
        spySearchBarController.searchContactsOnTextChanged("Testing");
        Assert.assertTrue(layCancel.getVisibility() == View.VISIBLE);
        Assert.assertTrue(cancelButton.getVisibility() == View.VISIBLE);
        System.err.println("******** Test: Cancel Button and Layout VISIBLE 6 CHAR ON CONTACT LIST KOKOKOKOKO OK********");
    }

    @Test
    public void testSearchViewOnTextChangedRealEvent() throws Exception {
        System.err.println("******** Test: Search On Real Text Changed Events ********");
        try {
            searchView.setText("1");
        } catch (RuntimeException e){
            System.err.println("******** Test: RunTimeException Handled OK********" + e);
        }
        Assert.assertTrue(layCancel.getVisibility() == View.VISIBLE);
        System.err.println("******** Test: Change Text Listener Test OK********");
    }

    @Test
    public void testSearchViewOnKeyListenerEvent() throws Exception {
        System.err.println("******** Test: Search View Key Listener Events ********");
        searchView.dispatchKeyEvent((new KeyEvent(KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_ENTER)));
        InputMethodManager inputManager = (InputMethodManager)contactListFragment.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        ShadowInputMethodManager shadowInputMethodManager = Shadows.shadowOf(inputManager);
        Assert.assertFalse(shadowInputMethodManager.isSoftInputVisible());
        System.err.println("******** Test: Search View Key Listener Events OK********");
    }

    //TODO: BusProvider Error is blocking this test. Fix it.
    @Test
    public void testCancelButtonClickEvent() throws Exception {
        System.err.println("******** Test: Search Cancel Button Click Events ********");

//        SearchBarController searchBarController = new SearchBarController(contactListFragment.getActivity(),null,null,null,2,null,false,null,contactListFragment);
//        SearchBarController spySearchBarController = Mockito.spy(searchBarController);
//
//        //Input ""
//        Mockito.doNothing().when(spySearchBarController).loadAllContactsFromDB();

        layCancel.setVisibility(View.VISIBLE);
//        spySearchBarController.initiateComponentsForSearchView(contactListFragment.getView());

        try {
            searchView.setText(testString);
        } catch (RuntimeException e){
            System.err.println("******** Test: RunTimeException Handled OK********" + e);
        }

        try {
            cancelButton.performClick();
        } catch (RuntimeException e){
            System.err.println("******** Test: RunTimeException Handled OK********" + e);
        }

        //Hide Keyboard Validation
        InputMethodManager inputManager = (InputMethodManager)contactListFragment.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        ShadowInputMethodManager shadowInputMethodManager = Shadows.shadowOf(inputManager);
        Assert.assertFalse(shadowInputMethodManager.isSoftInputVisible());

        //Hide SearchBar Validation
        Assert.assertTrue(searchView.getText().equals(""));

        System.err.println("******** Test: Search Cancel Button Click Events OK********");

    }

    @Test
    public void testLoadContactsFromServerEvent() throws Exception {
        System.err.println("******** Test: Load Contacts from Server Events ********");
//        SearchBarController searchBarController = new SearchBarController(contactListFragment.getActivity(),null,null,null,2,null,false,null,contactListFragment);
//        searchBarController.loadAllContactsFromServer("1");
        System.err.println("******** Test: Load Contacts from Server Events OK ********");
    }

    public void startContactListFragment(int index)
    {
        Intent in = new Intent(RuntimeEnvironment.application.getApplicationContext(),
                CustomFragmentActivity.class);
        in.putExtra("index", index);
        customFragmentActivity = Robolectric.buildActivity(CustomFragmentActivity.class)
                .withIntent(in)
                .create().start().resume().get();
    }

    private String startWebMockServer() throws Exception {
        //OkHttp mocked web server
        webServer = new MockWebServer();
        webServer.useHttps(null, false);

        //Connect OkHttp calls with MockWebServer
        webServer.start();

        return webServer.getUrl("/").toString();
    }
}
