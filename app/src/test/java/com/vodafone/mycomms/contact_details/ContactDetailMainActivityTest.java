package com.vodafone.mycomms.contact_details;

import android.app.Activity;
import android.content.Intent;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.contacts.connection.RecentContactController;
import com.vodafone.mycomms.contacts.detail.ContactDetailMainActivity;
import com.vodafone.mycomms.main.DashBoardActivity;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.search.SearchBarController;
import com.vodafone.mycomms.search.SearchController;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.Constants;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.MockRepository;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import io.realm.Realm;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by str_oan on 14/09/2015.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*"})
@PrepareForTest({Realm.class, EndpointWrapper.class
        , SearchController.class, SearchBarController.class, RecentContactController.class
        , RealmContactTransactions.class})

public class ContactDetailMainActivityTest
{
    @Rule
    public PowerMockRule rule = new PowerMockRule();
    public ContactDetailMainActivity mActivity;

    @Before
    public void setUp()
    {
        mockStatic(Realm.class);
        when(Realm.getDefaultInstance()).thenReturn(null);
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
    }

    @Test
    public void testActivityLifeCycle() throws Exception
    {
//        PowerMockito.mockStatic(RealmContactTransactions.class);
//        PowerMockito.when(RealmContactTransactions.getFilteredContacts(Matchers.anyString(), Matchers.anyString(), Matchers.any(Realm.class)))
//                .thenReturn(MockDataForTests.getMockContactsList());
//        Intent intent = new Intent();
//        intent.putExtra(Constants.CONTACT_CONTACT_ID, "mc_123456789");
//        mActivity = Robolectric.buildActivity(ContactDetailMainActivity.class).withIntent(intent)
//                .create()
//                .start()
//                .resume()
//                .pause()
//                .stop()
//                .destroy()
//                .get();
//        Assert.assertTrue(mActivity.isFinishing());
    }

    private void setUpActivity()
    {
        mActivity = Robolectric.buildActivity(ContactDetailMainActivity.class).create().start().get();
    }
}
