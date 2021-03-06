package com.vodafone.mycomms.main;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.crashlytics.android.Crashlytics;
import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.ContactListMainActivity;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ChatsReceivedEvent;
import com.vodafone.mycomms.events.DashboardCreatedEvent;
import com.vodafone.mycomms.events.GlobalContactsAddedEvent;
import com.vodafone.mycomms.events.GroupChatCreatedEvent;
import com.vodafone.mycomms.events.MessageStatusChanged;
import com.vodafone.mycomms.events.NewsReceivedEvent;
import com.vodafone.mycomms.events.RecentContactsReceivedEvent;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
import org.robolectric.shadows.ShadowIntent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import io.realm.Realm;
import model.News;

/**
 * Created by str_oan on 01/09/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 18,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*"})
@PrepareForTest({Realm.class
        , Crashlytics.class
        , BusProvider.class
        , EndpointWrapper.class})
public class DashBoardActivityTest
{
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    public DashBoardActivity mActivity;
    public SharedPreferences sp;
    public MockWebServer webServer;

    @Before
    public void setUp() throws Exception
    {
        PowerMockito.mockStatic(Realm.class);
        PowerMockito.when(Realm.getDefaultInstance()).thenReturn(null);
        PowerMockito.mockStatic(Crashlytics.class);

        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
        Context context = RuntimeEnvironment.application.getApplicationContext();
        sp = context.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
    }

    @After
    public void tearDown() throws Exception
    {
        MockDataForTests.checkThreadSchedulers();
        Robolectric.reset();
        if(webServer!=null)
        {
            try {
                webServer.shutdown();
            }
            catch (Exception e)
            {
                StringWriter writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                e.printStackTrace(printWriter);
                printWriter.flush();
                System.err.println("TearDown Exception at DashBoardActivityTest: \n" + writer.toString());
            }
        }

        mActivity = null;
        webServer = null;
        sp = null;
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
                System.err.println("Uncaught exception at DashBoardActivityTest: \n" + writer.toString());
            }
        });
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        Thread.currentThread().interrupt();
    }

    @Test
    public void testConnectionNotAvailableLayout_Visible() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        Context context = RuntimeEnvironment.application.getApplicationContext();
        ConnectivityManager connMgr =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(false);
        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().get();
        MockDataForTests.checkThreadSchedulers();

        LinearLayout layConnectionAvailable = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        Assert.assertNotNull(layConnectionAvailable);
        Assert.assertFalse(Utils.isConnected(mActivity));
        Assert.assertEquals(layConnectionAvailable.getVisibility(), View.VISIBLE);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testConnectionNotAvailableLayout_NotVisible() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        Context context = RuntimeEnvironment.application.getApplicationContext();
        ConnectivityManager connMgr =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(true);
        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().get();
        MockDataForTests.checkThreadSchedulers();

        LinearLayout layConnectionAvailable = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        Assert.assertNotNull(layConnectionAvailable);
        Assert.assertTrue(Utils.isConnected(mActivity));
        Assert.assertEquals(layConnectionAvailable.getVisibility(), View.GONE);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testInitAllBtnMagnifierOnClick() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().get();
        MockDataForTests.checkThreadSchedulers();

        ImageView btMagnifier = (ImageView) mActivity.findViewById(R.id.magnifier);
        btMagnifier.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(Constants.isSearchBarFocusRequested);
        Assert.assertTrue(Constants.isDashboardOrigin);
        Assert.assertEquals(MycommsApp.contactViewOrigin, Constants.CONTACTS_ALL);
        Intent expectedIntent = new Intent(mActivity, ContactListMainActivity.class);
        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (ContactListMainActivity.class.getName()));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testInitAllBtnFavoriteOnClick() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().get();
        MockDataForTests.checkThreadSchedulers();

        LinearLayout btFavourite = (LinearLayout) mActivity.findViewById(R.id.LayoutFavourite);
        btFavourite.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertEquals(MycommsApp.contactViewOrigin, Constants.CONTACTS_FAVOURITE);
        Intent expectedIntent = new Intent(mActivity, ContactListMainActivity.class);
        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (ContactListMainActivity.class.getName()));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testLoadLocalContacts() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        sp.edit().putBoolean(Constants.IS_LOCAL_CONTACTS_LOADING_ENABLED, true).apply();
        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().get();
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(sp.getBoolean(Constants.IS_LOCAL_CONTACTS_LOADING_ENABLED, false));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testActivityFullLifeCycle() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        Mockito.mock(DashBoardActivityController.class);
        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().pause().stop().destroy().get();
        MockDataForTests.checkThreadSchedulers();
        org.junit.Assert.assertTrue(mActivity.isDestroyed());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void test_onEventNewsReceived()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        News mockNews = new News();
        ArrayList<News> mockNewsList = new ArrayList<>();
        mockNewsList.add(mockNews);
        NewsReceivedEvent event = new NewsReceivedEvent();
        event.setNews(mockNewsList);

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();
        org.junit.Assert.assertNotNull(event);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void test_onEventChatReceived()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        ChatsReceivedEvent event = new ChatsReceivedEvent();
        event.setPendingMessages(0);

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();
        org.junit.Assert.assertEquals(event.getPendingMessages(), 0);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void test_onEventMessageStatusChanged()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        MessageStatusChanged event = new MessageStatusChanged();

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();
        org.junit.Assert.assertNotNull(event);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void test_onRecentContactsReceived()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        RecentContactsReceivedEvent event = new RecentContactsReceivedEvent();

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();
        org.junit.Assert.assertNotNull(event);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void test_onConnectivityChanged_Connected()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET);

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();
        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.GONE);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void test_onConnectivityChanged_NotConnected1()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED_HAS_NO_INTERNET);

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();
        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void test_onConnectivityChanged_NotConnected2()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.OFFLINE);

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();
        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void test_onConnectivityChanged_NotConnected3()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.UNKNOWN);

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();
        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void test_onEventGroupChatCreated()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        GroupChatCreatedEvent event = new GroupChatCreatedEvent();

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();
        org.junit.Assert.assertNotNull(event);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void test_onGlobalContactsAddedEvent_OK()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        GlobalContactsAddedEvent event = new GlobalContactsAddedEvent();
        String serverUrl = null;
        try {
            serverUrl = startWebMockServer();
        }
        catch (Exception e)
        {
            System.err.println("******** Test: test_onGlobalContactsAddedEvent Failed due to: ********\n"+e.getMessage());
            org.junit.Assert.fail();
        }
        PowerMockito.mockStatic(EndpointWrapper.class);
        PowerMockito.when(EndpointWrapper.getBaseURL()).thenReturn(serverUrl);

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();
        webServer.enqueue(new MockResponse().setResponseCode(200)
                .setBody(com.vodafone.mycomms.constants.Constants.VALID_VERSION_RESPONSE));
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();
        org.junit.Assert.assertNotNull(event);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void test_onGlobalContactsAddedEvent_OK_WrongJSON()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        GlobalContactsAddedEvent event = new GlobalContactsAddedEvent();
        String serverUrl = null;
        try {
            serverUrl = startWebMockServer();
        }
        catch (Exception e)
        {
            System.err.println("******** Test: test_onGlobalContactsAddedEvent Failed due to: ********\n"+e.getMessage());
            org.junit.Assert.fail();
        }

        PowerMockito.mockStatic(EndpointWrapper.class);
        PowerMockito.when(EndpointWrapper.getBaseURL()).thenReturn(serverUrl);

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();
        webServer.enqueue(new MockResponse().setResponseCode(200)
                .setBody("mockBody"));
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();
        org.junit.Assert.assertNotNull(event);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void test_onGlobalContactsAddedEvent_OK_NoResponse()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        GlobalContactsAddedEvent event = new GlobalContactsAddedEvent();
        String serverUrl = null;
        try {
            serverUrl = startWebMockServer();
        }
        catch (Exception e)
        {
            System.err.println("******** Test: test_onGlobalContactsAddedEvent Failed due to: ********\n"+e.getMessage());
            org.junit.Assert.fail();
        }

        PowerMockito.mockStatic(EndpointWrapper.class);
        PowerMockito.when(EndpointWrapper.getBaseURL()).thenReturn(serverUrl);

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();
        webServer.enqueue(new MockResponse().setResponseCode(200));
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();
        org.junit.Assert.assertNotNull(event);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void test_onGlobalContactsAddedEvent_Fail()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        GlobalContactsAddedEvent event = new GlobalContactsAddedEvent();
        String serverUrl = null;
        try {
            serverUrl = startWebMockServer();
        }
        catch (Exception e)
        {
            System.err.println("******** Test: test_onGlobalContactsAddedEvent Failed due to: ********\n"+e.getMessage());
            org.junit.Assert.fail();
        }

        PowerMockito.mockStatic(EndpointWrapper.class);
        PowerMockito.when(EndpointWrapper.getBaseURL()).thenReturn(serverUrl);
        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();
        webServer.enqueue(new MockResponse().setResponseCode(400)
                .setBody(com.vodafone.mycomms.constants.Constants.VALID_VERSION_RESPONSE));
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();

        org.junit.Assert.assertNotNull(event);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    private String startWebMockServer() throws Exception
    {
        //OkHttp mocked web server
        webServer = new MockWebServer();
        webServer.useHttps(null, false);

        //Connect OkHttp calls with MockWebServer
        webServer.start();
        String serverUrl = webServer.getUrl("").toString();

        return serverUrl;
    }
}
