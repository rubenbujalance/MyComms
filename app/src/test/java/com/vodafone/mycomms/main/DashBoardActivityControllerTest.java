package com.vodafone.mycomms.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chatgroup.GroupChatActivity;
import com.vodafone.mycomms.contacts.connection.RecentContactController;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.realm.RealmNewsTransactions;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.Constants;

import org.junit.Assert;
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
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

import java.util.ArrayList;

import io.realm.Realm;
import model.News;
import model.RecentContact;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.robolectric.Shadows.shadowOf;

import com.vodafone.mycomms.test.util.MockDataForTests;

/**
 * Created by str_oan on 02/09/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*"})
@PrepareForTest({Realm.class, Crashlytics.class})
public class DashBoardActivityControllerTest
{
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    public DashBoardActivity mActivity;
    public SharedPreferences sp;
    public DashBoardActivityController mDashBoardActivityController;

    @Before
    public void setUp() throws Exception
    {
        mockStatic(Realm.class);
        when(Realm.getDefaultInstance()).thenReturn(null);
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
        Context context = RuntimeEnvironment.application.getApplicationContext();
        this.sp = context.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        this.sp.edit().putString(Constants.PROFILE_ID_SHARED_PREF, "mc_5570340e7eb7c3512f2f9bf2").apply();
        this.mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().visible().get();
        Thread.sleep(1000);
        this.mDashBoardActivityController = this.mActivity.mDashBoardActivityController;
        mockParams();
    }

    @Test
    public void testCorrectlyCreated()
    {
        Assert.assertNotNull(this.mDashBoardActivityController);
        Assert.assertEquals(this.mDashBoardActivityController.mActivity, this.mActivity);
        Assert.assertEquals(this.mDashBoardActivityController.mProfileId, "mc_5570340e7eb7c3512f2f9bf2");
        Assert.assertNotNull(this.mDashBoardActivityController.mRecentContactController);
        Assert.assertNotNull(this.mDashBoardActivityController.mRealmContactTransactions);
        Assert.assertNotNull(this.mDashBoardActivityController.mRealmGroupChatTransactions);
    }

    @Test
    public void testLoadNews_WithClickOnNew_OK() throws Exception
    {
        this.mDashBoardActivityController.mRealmNewsTransactions = Mockito.mock(RealmNewsTransactions.class);
        Mockito.when(this.mDashBoardActivityController.mRealmNewsTransactions.getAllNews(any(Realm.class)))
                .thenReturn(MockDataForTests.getMockNewsArrayList());

        this.mDashBoardActivityController.loadNews();

        Thread.sleep(5000);

        LinearLayout container = (LinearLayout) mActivity.findViewById(R.id.list_news);
        LayoutInflater inflater = LayoutInflater.from(this.mActivity);
        Assert.assertNotNull(inflater);
        View v = container.getChildAt(0);
        LinearLayout btnews = (LinearLayout) v.findViewById(R.id.notice_content);
        int numberOfChild = container.getChildCount();
        btnews.performClick();
        ShadowActivity shadowActivity = shadowOf(this.mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);

        Assert.assertNotNull(this.mDashBoardActivityController.newsArrayList);
        Assert.assertEquals(numberOfChild, MockDataForTests.getMockNewsArrayList().size());
        assertThat(shadowIntent.getComponent().getClassName(), equalTo(NewsDetailActivity.class.getName()));
    }

    @Test
    public void testDrawNews_OK()
    {
        this.mDashBoardActivityController.drawNews(MockDataForTests.getMockNewsArrayList());
        LinearLayout container = (LinearLayout) mActivity.findViewById(R.id.list_news);
        Assert.assertNotNull(container);
    }

    @Test (expected = NullPointerException.class)
    public void testDrawNews_ControlledException()
    {
        this.mDashBoardActivityController.drawNews(null);
        Assert.assertTrue(this.mDashBoardActivityController.newsArrayList.isEmpty());
    }

    @Test
    public void testLoadRecentLayout_MoreThenOneRecent()
    {
        this.mDashBoardActivityController.numberOfRecentContacts = 5;
        this.mDashBoardActivityController.loadRecentLayout();
        Assert.assertTrue(this.mDashBoardActivityController.numberOfRecentContacts == 4);
    }

    @Test
    public void testLoadRecentLayout_OneRecent_FirstContainerIsVisible()
    {
        this.mDashBoardActivityController.isCurrentRecentContainerFirst = true;
        this.mDashBoardActivityController.numberOfRecentContacts = 1;
        this.mDashBoardActivityController.loadRecentLayout();
        Assert.assertTrue(this.mDashBoardActivityController.numberOfRecentContacts == 0);
        Assert.assertFalse(this.mDashBoardActivityController.isCurrentRecentContainerFirst);
        Assert.assertEquals(this.mDashBoardActivityController.mRecentContainer2.getVisibility(), View.GONE);
        Assert.assertEquals(this.mDashBoardActivityController.mRecentContainer.getVisibility(), View.VISIBLE);
    }

    @Test
    public void testLoadRecentLayout_OneRecent_SecondContainerIsVisible()
    {
        this.mDashBoardActivityController.isCurrentRecentContainerFirst = false;
        this.mDashBoardActivityController.numberOfRecentContacts = 1;
        this.mDashBoardActivityController.loadRecentLayout();
        Assert.assertTrue(this.mDashBoardActivityController.numberOfRecentContacts == 0);
        Assert.assertTrue(this.mDashBoardActivityController.isCurrentRecentContainerFirst);
        Assert.assertEquals(this.mDashBoardActivityController.mRecentContainer.getVisibility(), View.GONE);
        Assert.assertEquals(this.mDashBoardActivityController.mRecentContainer2.getVisibility(), View.VISIBLE);
    }

    @Test
    public void testLoadLocalContacts_OK()
    {
        this.mDashBoardActivityController.loadLocalContacts();
        Assert.assertTrue(this.mDashBoardActivityController.mProfileId != null);
        Assert.assertTrue(this.mDashBoardActivityController.mProfileId.length() > 0);
        Assert.assertTrue(this.mDashBoardActivityController.mActivity != null);

    }

    @Test
    public void testLoadLocalContacts_Fail()
    {
        this.mDashBoardActivityController.mProfileId = null;
        this.mDashBoardActivityController.loadLocalContacts();
        Assert.assertTrue(this.mDashBoardActivityController.mProfileId == null);
    }

    @Test
    public void testLoadRecentContactsAndUnreadMessages_FirstContainer()
    {
        this.mDashBoardActivityController.isCurrentRecentContainerFirst = true;
        this.mDashBoardActivityController.loadRecentContactsAndUnreadMessages();
        Assert.assertTrue(this.mDashBoardActivityController.mRecentContainer != null);
    }

    @Test
    public void testLoadRecentContactsAndUnreadMessages_SecondContainer()
    {
        this.mDashBoardActivityController.isCurrentRecentContainerFirst = false;
        this.mDashBoardActivityController.loadRecentContactsAndUnreadMessages();
        Assert.assertTrue(this.mDashBoardActivityController.mRecentContainer2 != null);
    }

    @Test
    public void testLoadRecent_Failed_WithControlledException()
    {
        this.mDashBoardActivityController.mRealmContactTransactions = Mockito.mock(RealmContactTransactions.class);
        Mockito.when(this.mDashBoardActivityController.mRealmContactTransactions.getAllRecentContacts(any(Realm.class)))
                .thenReturn(null);
        this.mDashBoardActivityController.loadRecents(this.mDashBoardActivityController.mRecentContainer);
        Assert.assertTrue(this.mDashBoardActivityController.numberOfRecentContacts == 0);
    }

    @Test
    public void testLoadRecent_OK() throws Exception
    {

        String mockContactId_1 = "mc_5535b2ac13be4b7975c51600", mockContactId_2 = "mc_55409316799f7e1a109446f4";
        this.mDashBoardActivityController.mRealmGroupChatTransactions = Mockito.mock(RealmGroupChatTransactions. class);
        this.mDashBoardActivityController.mRealmContactTransactions = Mockito.mock(RealmContactTransactions. class);
        Mockito.when(this.mDashBoardActivityController.mRealmGroupChatTransactions.getGroupChatById(anyString(), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockGroupChat());
        Mockito.when(this.mDashBoardActivityController.mRealmContactTransactions.getUserProfile(any(Realm.class)))
                .thenReturn(MockDataForTests.getMockUserProfile());
        Mockito.when(this.mDashBoardActivityController.mRealmContactTransactions.getContactById(eq(mockContactId_1), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContactById(mockContactId_1));
        Mockito.when(this.mDashBoardActivityController.mRealmContactTransactions.getContactById(eq(mockContactId_2), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContactById(mockContactId_2));
        Mockito.when(this.mDashBoardActivityController.mRealmContactTransactions.getAllRecentContacts(any(Realm.class)))
                .thenReturn(MockDataForTests.getMockRecentContactsList());


        this.mDashBoardActivityController.loadRecents(this.mDashBoardActivityController.mRecentContainer);
        Thread.sleep(5000);

        View mView = this.mDashBoardActivityController.mRecentContainer.getChildAt(0);
        LinearLayout lay_main_container = (LinearLayout) mView.findViewById(R.id.recent_content);
        boolean isClicked = lay_main_container.performClick();

        ShadowActivity shadowActivity = shadowOf(this.mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);

        Assert.assertTrue(isClicked);
        Assert.assertNull(mView.findViewById(R.id.lay_top_right_image_hide));
        Assert.assertTrue(this.mDashBoardActivityController.numberOfRecentContacts != 0);
        Assert.assertTrue(this.mDashBoardActivityController.mRecentContainer.getChildCount() == 2);
        assertThat(shadowIntent.getComponent().getClassName(), equalTo(GroupChatActivity.class.getName()));
    }

    @Test
    public void testLoadRecent_FailWithNullData() throws Exception
    {

        String mockContactId_1 = "mc_5535b2ac13be4b7975c51600", mockContactId_2 = "mc_55409316799f7e1a109446f4";
        this.mDashBoardActivityController.mRealmGroupChatTransactions = Mockito.mock(RealmGroupChatTransactions. class);
        this.mDashBoardActivityController.mRealmContactTransactions = Mockito.mock(RealmContactTransactions. class);
        Mockito.when(this.mDashBoardActivityController.mRealmGroupChatTransactions.getGroupChatById(anyString(), any(Realm.class)))
                .thenReturn(null);
        Mockito.when(this.mDashBoardActivityController.mRealmContactTransactions.getUserProfile(any(Realm.class)))
                .thenReturn(null);
        Mockito.when(this.mDashBoardActivityController.mRealmContactTransactions.getContactById(eq(mockContactId_1), any(Realm.class)))
                .thenReturn(null);
        Mockito.when(this.mDashBoardActivityController.mRealmContactTransactions.getContactById(eq(mockContactId_2), any(Realm.class)))
                .thenReturn(null);
        Mockito.when(this.mDashBoardActivityController.mRealmContactTransactions.getAllRecentContacts(any(Realm.class)))
                .thenReturn(null);


        this.mDashBoardActivityController.loadRecents(this.mDashBoardActivityController.mRecentContainer);
        Thread.sleep(5000);

        Assert.assertTrue(this.mDashBoardActivityController.numberOfRecentContacts == 0);
    }

    @Test
    public void testLoadRecent_FailWithWrongNotNullData() throws Exception
    {

        String mockContactId_1 = "mc_5535b2ac13be4b7975c51600", mockContactId_2 = "mc_55409316799f7e1a109446f4";
        this.mDashBoardActivityController.mRealmGroupChatTransactions = Mockito.mock(RealmGroupChatTransactions. class);
        this.mDashBoardActivityController.mRealmContactTransactions = Mockito.mock(RealmContactTransactions. class);
        Mockito.when(this.mDashBoardActivityController.mRealmGroupChatTransactions.getGroupChatById(anyString(), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockGroupChat_WithWrongData());
        Mockito.when(this.mDashBoardActivityController.mRealmContactTransactions.getUserProfile(any(Realm.class)))
                .thenReturn(MockDataForTests.getMockUserProfile());
        Mockito.when(this.mDashBoardActivityController.mRealmContactTransactions.getContactById(eq(mockContactId_1), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContactById(mockContactId_1));
        Mockito.when(this.mDashBoardActivityController.mRealmContactTransactions.getContactById(eq(mockContactId_2), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContactById(mockContactId_2));
        Mockito.when(this.mDashBoardActivityController.mRealmContactTransactions.getAllRecentContacts(any(Realm.class)))
                .thenReturn(MockDataForTests.getMockRecentContactsList_WithWrongData());


        this.mDashBoardActivityController.loadRecents(this.mDashBoardActivityController.mRecentContainer);
        Thread.sleep(5000);

        Assert.assertTrue(this.mDashBoardActivityController.numberOfRecentContacts != 0);
    }

    private void mockParams()
    {
        Downloader downloader = new OkHttpDownloader(mActivity.getApplicationContext(), Long.MAX_VALUE);
        Picasso.Builder builder = new Picasso.Builder(mActivity.getApplicationContext());
        builder.downloader(downloader);
        MycommsApp.picasso = builder.build();
    }


}