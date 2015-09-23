package com.vodafone.mycomms.contacts;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.view.View;
import android.widget.LinearLayout;

import com.crashlytics.android.Crashlytics;
import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.ContactListMainActivity;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.test.util.Util;

import org.junit.After;
import org.junit.Assert;
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

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by str_oan on 16/09/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*", "com.vodafone.mycomms.view.tab.*"})
@PrepareForTest({Realm.class, Crashlytics.class})
public class ContactListMainActivityTest
{
    @Rule
    public PowerMockRule rule = new PowerMockRule();
    public ContactListMainActivity mActivity;
    public Context mContext;

    @Before
    public void setUp()
    {
        mockStatic(Realm.class);
        when(Realm.getDefaultInstance()).thenReturn(null);
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
        mContext = RuntimeEnvironment.application.getApplicationContext();
    }

    @After
    public void tearDown() throws Exception
    {
        //Try to shutdown server if it was started
        try {
            Robolectric.reset();
        } catch (Exception e) {}

        mActivity = null;
        mContext = null;
    }

    @Test
    public void testConnectionAvailable()
    {
        ConnectivityManager connMgr =
                (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(true);

        mActivity = Robolectric.setupActivity(ContactListMainActivity.class);
        try {
            Thread.sleep(3000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testConnectionAvailable Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }
        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        Assert.assertTrue(lay_no_connection.getVisibility() == View.GONE);
    }

    @Test
    public void testConnectionNotAvailable()
    {
        ConnectivityManager connMgr =
                (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(false);

        mActivity = Robolectric.setupActivity(ContactListMainActivity.class);
        try {
            Thread.sleep(3000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testConnectionAvailable Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }
        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        Assert.assertTrue(lay_no_connection.getVisibility() == View.VISIBLE);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testActivityLifeCycle() throws Exception
    {
        mActivity = Robolectric.buildActivity(ContactListMainActivity.class)
                .create()
                .start()
                .resume()
                .pause()
                .stop()
                .destroy()
                .get();
        Assert.assertTrue(mActivity.isDestroyed());
    }


    @Test
    public void testOnConnectivityChanged_HasInternet_Event()
    {
        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET);
        mActivity = Robolectric.setupActivity(ContactListMainActivity.class);
        try {
            Thread.sleep(3000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        BusProvider.getInstance().post(event);
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.GONE);
    }

    @Test
    public void testOnConnectivityChanged_Unknown_Event()
    {
        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.UNKNOWN);
        mActivity = Robolectric.setupActivity(ContactListMainActivity.class);
        try {
            Thread.sleep(3000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        BusProvider.getInstance().post(event);
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);
    }

    @Test
    public void testOnConnectivityChanged_Offline_Event()
    {
        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.OFFLINE);
        mActivity = Robolectric.setupActivity(ContactListMainActivity.class);
        try {
            Thread.sleep(3000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        BusProvider.getInstance().post(event);
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);
    }

    @Test
    public void testOnConnectivityChanged_ConnectedNoInternet_Event()
    {
        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED_HAS_NO_INTERNET);
        mActivity = Robolectric.setupActivity(ContactListMainActivity.class);
        try {
            Thread.sleep(3000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        BusProvider.getInstance().post(event);
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);
    }

    @Test
    public void testOnConnectivityChanged_WifiConnected_Event()
    {
        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED);
        mActivity = Robolectric.setupActivity(ContactListMainActivity.class);
        try {
            Thread.sleep(3000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        BusProvider.getInstance().post(event);
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);
    }

    @Test
    public void testOnConnectivityChanged_MobileConnected_Event()
    {
        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.MOBILE_CONNECTED);
        mActivity = Robolectric.setupActivity(ContactListMainActivity.class);
        try {
            Thread.sleep(3000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        BusProvider.getInstance().post(event);
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.GONE);
    }


}
