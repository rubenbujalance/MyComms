package com.vodafone.mycomms.settings;

import android.content.Context;
import android.net.ConnectivityManager;
import android.view.View;
import android.widget.LinearLayout;

import com.crashlytics.android.Crashlytics;
import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
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
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import java.io.PrintWriter;
import java.io.StringWriter;

import io.realm.Realm;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;

/**
 * Created by str_oan on 23/09/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*", "com.vodafone.mycomms.view.tab.*"
        , "com.vodafone.mycomms.custom.*"})
@PrepareForTest({Realm.class, Crashlytics.class})
public class SettingsMainActivityTest
{
    @Rule
    public PowerMockRule rule = new PowerMockRule();
    public SettingsMainActivity mActivity;
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
    public void tearDown()
    {
        MockDataForTests.checkThreadSchedulers();
        Robolectric.reset();
        mActivity.finish();
        mActivity = null;
        mContext = null;
        System.gc();
    }

    @BeforeClass
    public static void setUpBeforeClass()
    {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                StringWriter writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                e.printStackTrace(printWriter);
                printWriter.flush();
                System.err.println("Uncaught exception at " + this.getClass().getSimpleName() + ": \n" + writer.toString());
            }
        });
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        Thread.currentThread().interrupt();
    }

    @Test
    public void testConnectionAvailable()
    {
        ConnectivityManager connMgr =
                (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(true);
        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        Assert.assertTrue(lay_no_connection.getVisibility() == View.GONE);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testConnectionNotAvailable()
    {
        ConnectivityManager connMgr =
                (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(false);
        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        Assert.assertTrue(lay_no_connection.getVisibility() == View.VISIBLE);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testOnConnectivityEvent()
    {
        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET);
        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();

        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.GONE);

        event = new ConnectivityChanged(ConnectivityStatus.UNKNOWN);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();

        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);

        event = new ConnectivityChanged(ConnectivityStatus.OFFLINE);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();

        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);

        event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED_HAS_NO_INTERNET);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();

        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);

        event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();

        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);

        event = new ConnectivityChanged(ConnectivityStatus.MOBILE_CONNECTED);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();

        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.GONE);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testOnExitPreferencesClicked()
    {
        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        LinearLayout lay_exit_preferences = (LinearLayout) mActivity.findViewById(R.id.lay_exit_preferences);
        lay_exit_preferences.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(mActivity.isFinishing());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testOnBackPressed()
    {
        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        mActivity.onBackPressed();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(mActivity.isFinishing());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testOnStop()
    {
        MycommsApp.stateCounter = 0;
        mActivity = Robolectric
                .buildActivity(SettingsMainActivity.class).create().start().resume().stop().get();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(MycommsApp.stateCounter == 0);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testOnStart()
    {
        MycommsApp.stateCounter = 0;
        mActivity = Robolectric
                .buildActivity(SettingsMainActivity.class).create().start().get();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(MycommsApp.stateCounter == 1);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    private void setUpActivity()
    {
        mActivity = Robolectric.setupActivity(SettingsMainActivity.class);
    }
}
