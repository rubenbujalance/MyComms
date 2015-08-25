package com.vodafone.mycomms.contacts;

import android.content.Intent;
import android.view.View;
import android.widget.RelativeLayout;

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

    @Before
    public void setUp() throws Exception {
        mockStatic(Realm.class);
        when(Realm.getDefaultInstance()).thenReturn(null);
        mockStatic(Crashlytics.class);
        whenNew(RealmContactTransactions.class).withAnyArguments()
                .thenReturn(null);

        startContactListFragment(2);
        contactListFragment = (ContactListFragment)customFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("2");
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
    }

    @Test
    public void testShowAddGlobalContactsBarInContacts() throws Exception {
//        RealmContactTransactions realmContactTransactions =
//                Mockito.mock(RealmContactTransactions.class);
//        Mockito.when(realmContactTransactions.getAllContacts(null))
//                .thenReturn(new ArrayList<Contact>());
//        Mockito.spy(RealmContactTransactions.class);

        addGCBar = (RelativeLayout)contactListFragment.getView()
                .findViewById(R.id.add_global_contacts_container);
        //Save fake Global Contacts loading to false
//        Context context = RuntimeEnvironment.application.getApplicationContext();
//        SharedPreferences sp = context.getSharedPreferences(
//                com.vodafone.mycomms.util.Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
//        sp.edit().putBoolean(
//                com.vodafone.mycomms.util.Constants.IS_GLOBAL_CONTACTS_LOADING_ENABLED,
//                true)
//                .commit();
        System.err.println("******** Test: IS_GLOBAL_CONTACTS_LOADING_ENABLED FALSE ********");

        //addGCBar.setVisibility(View.VISIBLE);
        Assert.assertTrue(addGCBar.getVisibility() == (View.VISIBLE));
    }

    @Test
    public void testClickBarAndGoToAddGlobalContacts() throws Exception {

        addGCBar = (RelativeLayout)contactListFragment.getView()
                .findViewById(R.id.add_global_contacts_container);
        addGCBar.setVisibility(View.VISIBLE);
        addGCBar.performClick();

        Intent expectedIntent = new Intent(contactListFragment.getActivity(), AddGlobalContactsActivity.class);
        Assert.assertTrue(Shadows.shadowOf(contactListFragment.getActivity())
                .getNextStartedActivity().equals(expectedIntent));
        System.err.println("******** Test: Navigation to AddGlobalContactsActivity ********");

    }

    public void startContactListFragment(int index)
    {
        //Test
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
