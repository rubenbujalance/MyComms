package com.vodafone.mycomms.main;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.opengl.Visibility;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.ContactListMainActivity;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import org.junit.Assert;
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
import model.RecentContact;

import static org.mockito.Matchers.any;
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
@PrepareForTest({Realm.class, Crashlytics.class, EndpointWrapper.class, APIWrapper.class})
public class DashBoardActivityTest
{
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    public Activity mActivity;
    public SharedPreferences sp;
    public ArrayList<RecentContact> emptyRecentContactsList;
    public ArrayList<RecentContact> notEmptyRecentContactsList;

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

        emptyRecentContactsList = new ArrayList<>();
        notEmptyRecentContactsList = fillMockRecentContactsList();

    }

    private ArrayList<RecentContact> fillMockRecentContactsList()
    {
        ArrayList<RecentContact> recentList = new ArrayList<>();
        RecentContact mockRecentContact = new RecentContact();

        mockRecentContact.setContactId("mg_55df03a45b04622a4248cc9f");
        mockRecentContact.setId("mg_55df03a45b04622a4248cc9f");
        mockRecentContact.setAction("sms");
        mockRecentContact.setTimestamp(Long.parseLong("1440678843328"));
        mockRecentContact.setAvailability("available");

        recentList.add(mockRecentContact);
        mockRecentContact = new RecentContact();

        mockRecentContact.setContactId("mc_55409316799f7e1a109446f4");
        mockRecentContact.setId("mc_55409316799f7e1a109446f4");
        mockRecentContact.setAction("sms");
        mockRecentContact.setTimestamp(Long.parseLong("1440658545874"));
        mockRecentContact.setAvailability("available");

        recentList.add(mockRecentContact);
        mockRecentContact = new RecentContact();

        mockRecentContact.setContactId("mg_55dc2a35a297b90a726e4cc2");
        mockRecentContact.setId("mg_55dc2a35a297b90a726e4cc2");
        mockRecentContact.setAction("sms");
        mockRecentContact.setTimestamp(Long.parseLong("1440571515241"));
        mockRecentContact.setAvailability("available");

        recentList.add(mockRecentContact);

        return recentList;
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

        Assert.assertEquals(MycommsApp.contactViewOrigin, Constants.CONTACTS_FAVOURITE);

        Intent expectedIntent = new Intent(mActivity, ContactListMainActivity.class);
        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (ContactListMainActivity.class.getName()));
    }

    @Test
    public void testOnResume()
    {
        RealmContactTransactions realmContactTransactions = Mockito.mock(RealmContactTransactions.class);
        Mockito.when(realmContactTransactions.getAllRecentContacts(Mockito.any(Realm.class))).thenReturn(notEmptyRecentContactsList);
        mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().get();
        testLoadRecent();
        testLoadUnreadMessages();
        testLoadNews();
        testResetOfNotificationMessages();

    }

    private void testLoadRecent()
    {

    }

    private void testLoadUnreadMessages()
    {

    }

    private void testLoadNews()
    {

    }

    private void testResetOfNotificationMessages()
    {

    }

}
