package com.vodafone.mycomms.contacts;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.vodafone.mycomms.settings.globalcontacts.AddGlobalContactsActivity;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.CustomFragmentActivity;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.MockRepository;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import io.realm.Realm;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
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
    RelativeLayout addGCBar;
    CustomFragmentActivity customFragmentActivity;
    Context context;
    ListView listView;
    TextView emptyText;
    EditText searchView;
    RelativeLayout addGlobalContactsContainer;
    Button cancelButton;
    LinearLayout layCancel;
    LinearLayout laySearchBar;

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
        addGCBar = (RelativeLayout)contactListFragment.getView()
                .findViewById(R.id.add_global_contacts_container);
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
        Assert.assertTrue(addGCBar != null);
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
                .commit();
        System.err.println("******** Test: IS_GLOBAL_CONTACTS_LOADING_ENABLED FALSE ********");

        //addGCBar.setVisibility(View.VISIBLE);
        Assert.assertTrue(addGCBar.getVisibility() == (View.VISIBLE));
        System.err.println("******** Test: Global Contacts Visibility ON CONTACTS LIST OK********");

        startContactListFragment(0);
        ContactListFragment favoriteListFragment = (ContactListFragment)customFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("0");
        addGCBar = (RelativeLayout) favoriteListFragment.getView().findViewById(R.id.add_global_contacts_container);
        Assert.assertTrue(addGCBar.getVisibility() == (View.GONE));
        System.err.println("******** Test: Global Contacts Visibility ON FAVORITE LIST OK********");

        startContactListFragment(1);
        ContactListFragment recentListFragment = (ContactListFragment)customFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("1");
        addGCBar = (RelativeLayout) recentListFragment.getView().findViewById(R.id.add_global_contacts_container);
        Assert.assertTrue(addGCBar.getVisibility() == (View.GONE));
        System.err.println("******** Test: Global Contacts Visibility ON RECENT LIST OK********");
    }

    @Test
    public void testClickBarAndGoToAddGlobalContacts() throws Exception {
        addGCBar.setVisibility(View.VISIBLE);
        addGCBar.performClick();

        Intent expectedIntent = new Intent(contactListFragment.getActivity(), AddGlobalContactsActivity.class);
        Assert.assertTrue(Shadows.shadowOf(contactListFragment.getActivity())
                .getNextStartedActivity().equals(expectedIntent));
        System.err.println("******** Test: Navigation to AddGlobalContactsActivity OK********");

    }

    @Test
    public void testSearchBarVisibility() throws Exception {
        System.err.println("******** Test: Test Search Bar Visibility ********");
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
        System.err.println("******** Test: Test Search Bar Content Visibility ********");
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
    }

    @Test
    public void testSearchViewTouchEvent() throws Exception {
        System.err.println("******** Test: Test Search Bar Touch Events ********");
        searchView.performClick();
        //Show Keyboard Soft Input
        InputMethodManager imm = (InputMethodManager) contactListFragment.getActivity().getSystemService(Context
                .INPUT_METHOD_SERVICE);
        Assert.assertTrue(imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT) == true);
        System.err.println("******** Test: Search Keyboard showing ON CONTACT LIST OK********");
        //Show CancelButton
        Assert.assertTrue(cancelButton.getVisibility() == View.VISIBLE);
        System.err.println("******** Test: Cancel Button Visibility ON CONTACT LIST OK********");
    }

//    @Test
    public void testSearchViewOnTextChangedEvent() throws Exception {
        System.err.println("******** Test: Test Search On Text Changed Events ********");
        //Input 1 letter text
        searchView.setText("1");
        //LayCancel Visible
        Assert.assertTrue(layCancel.getVisibility() == View.VISIBLE);
        Assert.assertTrue(cancelButton.getVisibility() == View.VISIBLE);
        System.err.println("******** Test: Cancel Button and Layout Visibility ON CONTACT LIST OK********");
        //CompoundDrawables

        //Input more than 1 letter text

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
        String serverUrl = webServer.getUrl("/").toString();

        return serverUrl;
    }
}
