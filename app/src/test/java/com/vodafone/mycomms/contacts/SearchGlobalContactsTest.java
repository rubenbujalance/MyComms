package com.vodafone.mycomms.contacts;

import android.app.Activity;
import android.view.View;
import android.widget.RelativeLayout;

import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.ContactListMainActivity;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.realm.RealmLDAPSettingsTransactions;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.configuration.GlobalConfiguration;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.powermock.api.support.ClassLoaderUtil;
import org.powermock.core.MockRepository;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.powermock.reflect.Whitebox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*"})
@PrepareForTest({EndpointWrapper.class, RealmLDAPSettingsTransactions.class})

public class SearchGlobalContactsTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    Activity activity;
    MockWebServer webServer;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.setupActivity(ContactListMainActivity.class);
//        activity = Robolectric.setupActivity(AddGlobalContactsActivity.class);

        MockRepository.addAfterMethodRunner(new MockitoStateCleaner());
    }

    @Test
    public void testSendFormatErrorAndReset() throws Exception {
        RelativeLayout addGlobalContactsContainer =
                (RelativeLayout)activity.findViewById(R.id.add_global_contacts_container);

        addGlobalContactsContainer.setVisibility(View.VISIBLE);
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
}
