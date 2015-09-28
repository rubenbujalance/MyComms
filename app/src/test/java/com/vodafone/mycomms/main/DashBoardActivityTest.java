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
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
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

import java.util.ArrayList;

import io.realm.Realm;
import model.News;

/**
 * Created by str_oan on 01/09/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
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
        BusProvider.MainThreadBus bus = BusProvider.getInstance();
        BusProvider.MainThreadBus busSpy = Mockito.spy(bus);
        PowerMockito.doNothing().when(busSpy).post(Mockito.any(DashboardCreatedEvent.class));
        PowerMockito.mockStatic(BusProvider.class);
        PowerMockito.when(BusProvider.getInstance()).thenReturn(bus);

        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
        Context context = RuntimeEnvironment.application.getApplicationContext();
        sp = context.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
    }

    @After
    public void tearDown() throws Exception
    {
        //Try to shutdown server if it was started
        try {
            Robolectric.reset();
            if(webServer!=null) webServer.shutdown();
        } catch (Exception e) {}

        mActivity = null;
        webServer = null;
        sp = null;
        System.gc();
    }

    @Test
    public void testConnectionNotAvailableLayout_Visible() throws Exception
    {
        Context context = RuntimeEnvironment.application.getApplicationContext();
        ConnectivityManager connMgr =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(false);
        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().get();
        Thread.sleep(3000);
        Robolectric.flushForegroundThreadScheduler();

        LinearLayout layConnectionAvailable = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);

        Assert.assertNotNull(layConnectionAvailable);
        Assert.assertFalse(Utils.isConnected(mActivity));
        Assert.assertEquals(layConnectionAvailable.getVisibility(), View.VISIBLE);
    }

    @Test
    public void testConnectionNotAvailableLayout_NotVisible() throws Exception
    {
        Context context = RuntimeEnvironment.application.getApplicationContext();
        ConnectivityManager connMgr =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(true);
        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().get();
        Thread.sleep(3000);
        Robolectric.flushForegroundThreadScheduler();

        LinearLayout layConnectionAvailable = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);

        Assert.assertNotNull(layConnectionAvailable);
        Assert.assertTrue(Utils.isConnected(mActivity));
        Assert.assertEquals(layConnectionAvailable.getVisibility(), View.GONE);
    }

    @Test
    public void testInitAllBtnMagnifierOnClick() throws Exception
    {
        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().get();
        Thread.sleep(3000);
        Robolectric.flushForegroundThreadScheduler();

        ImageView btMagnifier = (ImageView) mActivity.findViewById(R.id.magnifier);
        btMagnifier.performClick();
        Assert.assertTrue(Constants.isSearchBarFocusRequested);
        Assert.assertTrue(Constants.isDashboardOrigin);
        Assert.assertEquals(MycommsApp.contactViewOrigin, Constants.CONTACTS_ALL);

        Intent expectedIntent = new Intent(mActivity, ContactListMainActivity.class);
        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (ContactListMainActivity.class.getName()));
    }

    @Test
    public void testInitAllBtnFavoriteOnClick() throws Exception
    {

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().get();
        Thread.sleep(3000);
        Robolectric.flushForegroundThreadScheduler();

        LinearLayout btFavourite = (LinearLayout) mActivity.findViewById(R.id.LayoutFavourite);
        btFavourite.performClick();

        Assert.assertEquals(MycommsApp.contactViewOrigin, Constants.CONTACTS_FAVOURITE);

        Intent expectedIntent = new Intent(mActivity, ContactListMainActivity.class);
        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (ContactListMainActivity.class.getName()));
    }

    @Test
    public void testLoadLocalContacts() throws Exception
    {
        sp.edit().putBoolean(Constants.IS_LOCAL_CONTACTS_LOADING_ENABLED, true).apply();
        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().get();
        Thread.sleep(3000);
        Robolectric.flushForegroundThreadScheduler();

        org.junit.Assert.assertTrue(sp.getBoolean(Constants.IS_LOCAL_CONTACTS_LOADING_ENABLED, false));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testActivityFullLifeCycle() throws Exception
    {
        Mockito.mock(DashBoardActivityController.class);
        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().pause().stop().destroy().get();
        org.junit.Assert.assertTrue(mActivity.isDestroyed());
    }

    @Test
    public void test_onEventNewsReceived()
    {
        News mockNews = new News();
        ArrayList<News> mockNewsList = new ArrayList<>();
        mockNewsList.add(mockNews);
        NewsReceivedEvent event = new NewsReceivedEvent();
        event.setNews(mockNewsList);

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        BusProvider.getInstance().post(event);
        org.junit.Assert.assertNotNull(event);
    }

    @Test
    public void test_onEventChatReceived()
    {
        ChatsReceivedEvent event = new ChatsReceivedEvent();
        event.setPendingMessages(0);

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        BusProvider.getInstance().post(event);
        org.junit.Assert.assertEquals(event.getPendingMessages(), 0);
    }

    @Test
    public void test_onEventMessageStatusChanged()
    {
        MessageStatusChanged event = new MessageStatusChanged();

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        BusProvider.getInstance().post(event);
        org.junit.Assert.assertNotNull(event);
    }

    @Test
    public void test_onRecentContactsReceived()
    {
        RecentContactsReceivedEvent event = new RecentContactsReceivedEvent();

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        BusProvider.getInstance().post(event);
        org.junit.Assert.assertNotNull(event);
    }

    @Test
    public void test_onConnectivityChanged_Connected()
    {
        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET);

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        BusProvider.getInstance().post(event);
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.GONE);
    }

    @Test
    public void test_onConnectivityChanged_NotConnected1()
    {
        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED_HAS_NO_INTERNET);

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        BusProvider.getInstance().post(event);
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);
    }

    @Test
    public void test_onConnectivityChanged_NotConnected2()
    {
        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.OFFLINE);

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        BusProvider.getInstance().post(event);
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);
    }

    @Test
    public void test_onConnectivityChanged_NotConnected3()
    {
        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.UNKNOWN);

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        BusProvider.getInstance().post(event);
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);
    }

    @Test
    public void test_onEventGroupChatCreated()
    {
        GroupChatCreatedEvent event = new GroupChatCreatedEvent();

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        BusProvider.getInstance().post(event);
        org.junit.Assert.assertNotNull(event);
    }

    @Test
    public void test_onGlobalContactsAddedEvent_OK()
    {
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
        webServer.enqueue(new MockResponse().setResponseCode(200)
                .setBody(com.vodafone.mycomms.constants.Constants.VALID_VERSION_RESPONSE));

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        try {
            Thread.sleep(3000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: test_onGlobalContactsAddedEvent Failed due to: ********\n"+e.getMessage());
            org.junit.Assert.fail();
        }

        BusProvider.getInstance().post(event);

        try {
            Thread.sleep(3000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: test_onGlobalContactsAddedEvent Failed due to: ********\n"+e.getMessage());
            org.junit.Assert.fail();
        }
        Robolectric.flushForegroundThreadScheduler();

        org.junit.Assert.assertNotNull(event);
    }

    @Test
    public void test_onGlobalContactsAddedEvent_OK_WrongJSON()
    {
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
        webServer.enqueue(new MockResponse().setResponseCode(200)
                .setBody("mockBody"));

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        try {
            Thread.sleep(3000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: test_onGlobalContactsAddedEvent Failed due to: ********\n"+e.getMessage());
            org.junit.Assert.fail();
        }

        BusProvider.getInstance().post(event);

        try {
            Thread.sleep(3000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: test_onGlobalContactsAddedEvent Failed due to: ********\n"+e.getMessage());
            org.junit.Assert.fail();
        }
        Robolectric.flushForegroundThreadScheduler();

        org.junit.Assert.assertNotNull(event);
    }

    @Test
    public void test_onGlobalContactsAddedEvent_OK_NoResponse()
    {
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
        webServer.enqueue(new MockResponse().setResponseCode(200));

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        try {
            Thread.sleep(3000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: test_onGlobalContactsAddedEvent Failed due to: ********\n"+e.getMessage());
            org.junit.Assert.fail();
        }

        BusProvider.getInstance().post(event);

        try {
            Thread.sleep(3000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: test_onGlobalContactsAddedEvent Failed due to: ********\n"+e.getMessage());
            org.junit.Assert.fail();
        }
        Robolectric.flushForegroundThreadScheduler();

        org.junit.Assert.assertNotNull(event);
    }

    @Test
    public void test_onGlobalContactsAddedEvent_Fail()
    {
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
        webServer.enqueue(new MockResponse().setResponseCode(400)
                .setBody(com.vodafone.mycomms.constants.Constants.VALID_VERSION_RESPONSE));

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        try {
            Thread.sleep(3000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: test_onGlobalContactsAddedEvent Failed due to: ********\n"+e.getMessage());
            org.junit.Assert.fail();
        }

        BusProvider.getInstance().post(event);

        try {
            Thread.sleep(3000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: test_onGlobalContactsAddedEvent Failed due to: ********\n"+e.getMessage());
            org.junit.Assert.fail();
        }
        Robolectric.flushForegroundThreadScheduler();

        org.junit.Assert.assertNotNull(event);
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
