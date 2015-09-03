package com.vodafone.mycomms.main;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.crashlytics.android.Crashlytics;
import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.ContactListMainActivity;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.connection.AsyncTaskQueue;
import com.vodafone.mycomms.events.ApplicationAndProfileInitialized;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ChatsReceivedEvent;
import com.vodafone.mycomms.events.GlobalContactsAddedEvent;
import com.vodafone.mycomms.events.GroupChatCreatedEvent;
import com.vodafone.mycomms.events.MessageStatusChanged;
import com.vodafone.mycomms.events.NewsReceivedEvent;
import com.vodafone.mycomms.events.RecentContactsReceivedEvent;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmNewsTransactions;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.util.Utils;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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
import org.robolectric.shadows.ShadowIntent;

import java.util.ArrayList;

import io.realm.Realm;
import model.News;
import model.RecentContact;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by str_oan on 01/09/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*"})
@PrepareForTest({Realm.class, Crashlytics.class, DashBoardActivityController.class})
public class DashBoardActivityTest
{
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    public DashBoardActivity mActivity;
    public SharedPreferences sp;

    @Before
    public void setUp() throws Exception
    {
        mockStatic(Realm.class);
        when(Realm.getDefaultInstance()).thenReturn(null);
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
        Context context = RuntimeEnvironment.application.getApplicationContext();
        sp = context.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
    }

    @Test
    public void testConnectionNotAvailableLayout_Visible() throws Exception
    {
        Context context = RuntimeEnvironment.application.getApplicationContext();
        ConnectivityManager connMgr =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(false);
        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().get();
        Thread.sleep(1000);
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
        Thread.sleep(1000);
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
        Thread.sleep(1000);
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
        Thread.sleep(1000);
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
        Thread.sleep(1000);
        Robolectric.flushForegroundThreadScheduler();

        org.junit.Assert.assertTrue(sp.getBoolean(Constants.IS_LOCAL_CONTACTS_LOADING_ENABLED, false));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testActivityFullLifeCycle() throws Exception
    {
        mock(DashBoardActivityController.class);
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
    public void test_onGlobalContactsAddedEvent()
    {
        GlobalContactsAddedEvent event = new GlobalContactsAddedEvent();

        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        BusProvider.getInstance().post(event);
        org.junit.Assert.assertNotNull(event);
    }
}