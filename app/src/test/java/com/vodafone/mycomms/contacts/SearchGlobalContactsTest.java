package com.vodafone.mycomms.contacts;

import android.content.Intent;
import android.view.View;
import android.widget.RelativeLayout;

import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.util.CustomFragmentActivity;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.contacts.view.ContactListFragment;
import com.vodafone.mycomms.realm.RealmContactTransactions;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.configuration.GlobalConfiguration;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.ClassLoaderUtil;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.powermock.reflect.Whitebox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import io.realm.Realm;
import model.Contact;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*"})
@PrepareForTest({Realm.class})

public class SearchGlobalContactsTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    MockWebServer webServer;
    ContactListFragment contactListFragment;
    RelativeLayout addGCBar;
    CustomFragmentActivity customFragmentActivity;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Realm.class);
        Mockito.when(Realm.getDefaultInstance()).thenReturn(null);
    }

    @Test
    public void testShowAddGlobalContactsBarInContacts() throws Exception {
        RealmContactTransactions contactTx = Mockito.mock(RealmContactTransactions.class);
        ArrayList<Contact> contactList = new ArrayList<>();
        Mockito.when(contactTx.getAllContacts(Mockito.any(Realm.class))).thenReturn(contactList);

        startContactListFragment(2);
        contactListFragment = (ContactListFragment)customFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("2");

        addGCBar = (RelativeLayout)contactListFragment.getView()
                .findViewById(R.id.add_global_contacts_container);
        addGCBar.setVisibility(View.VISIBLE);
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

    private void resetScreen() {
    }

    private static class MockitoStateCleaner implements Runnable {
        public void run() {
            clearMockProgress();
            clearConfiguration();
        }

        private void clearMockProgress() {
            clearThreadLocalIn(ThreadSafeMockingProgress.class);
        }

        private void clearConfiguration() {
            clearThreadLocalIn(GlobalConfiguration.class);
        }

        private void clearThreadLocalIn(Class<?> cls) {
            Whitebox.getInternalState(cls, ThreadLocal.class).set(null);
            final Class<?> clazz = ClassLoaderUtil.loadClass(cls, ClassLoader.getSystemClassLoader());
            Whitebox.getInternalState(clazz, ThreadLocal.class).set(null);
        }
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

}
