package com.vodafone.mycomms.main;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chatgroup.GroupChatActivity;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.realm.RealmNewsTransactions;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.Constants;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
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
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import io.realm.Realm;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

/**
 * Created by str_oan on 02/09/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*"})
@PrepareForTest({Realm.class
        , Crashlytics.class
        , RealmContactTransactions.class
        , RealmGroupChatTransactions.class
        , RealmChatTransactions.class})

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
        PowerMockito.mockStatic(Realm.class);
        PowerMockito.when(Realm.getDefaultInstance()).thenReturn(null);
        PowerMockito.mockStatic(Crashlytics.class);
        PowerMockito.mockStatic(RealmContactTransactions.class);
        PowerMockito.mockStatic(RealmGroupChatTransactions.class);
        PowerMockito.mockStatic(RealmChatTransactions.class);

        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
        Context context = RuntimeEnvironment.application.getApplicationContext();
        this.sp = context.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        this.sp.edit().putString(Constants.PROFILE_ID_SHARED_PREF, "mc_5570340e7eb7c3512f2f9bf2").apply();
        this.mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().resume().visible().get();
        MockDataForTests.checkThreadSchedulers();
        this.mDashBoardActivityController = this.mActivity.mDashBoardActivityController;
        mockParams();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @After
    public void tearDown() throws Exception
    {
        MockDataForTests.checkThreadSchedulers();

        Robolectric.reset();

        mActivity = null;
        mDashBoardActivityController = null;
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
                System.err.println("Uncaught exception at DashBoardActivityControllerTest: \n" + writer.toString());
            }
        });
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        Thread.currentThread().interrupt();
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
        MockDataForTests.checkThreadSchedulers();

        LinearLayout container = (LinearLayout) mActivity.findViewById(R.id.list_news);
        LayoutInflater inflater = LayoutInflater.from(this.mActivity);
        Assert.assertNotNull(inflater);
        View v = container.getChildAt(0);
        LinearLayout btnews = (LinearLayout) v.findViewById(R.id.notice_content);
        int numberOfChild = container.getChildCount();
        btnews.performClick();
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(this.mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertNotNull(this.mDashBoardActivityController.newsArrayList);
        Assert.assertEquals(numberOfChild, MockDataForTests.getMockNewsArrayList().size());
        assertThat(shadowIntent.getComponent().getClassName(), equalTo(NewsDetailActivity.class.getName()));
    }

    @Test
    public void testDrawNews_OK()
    {
        this.mDashBoardActivityController.drawNews(MockDataForTests.getMockNewsArrayList());
        MockDataForTests.checkThreadSchedulers();
        LinearLayout container = (LinearLayout) mActivity.findViewById(R.id.list_news);
        Assert.assertNotNull(container);
    }

    @Test (expected = NullPointerException.class)
    public void testDrawNews_ControlledException()
    {
        this.mDashBoardActivityController.drawNews(null);
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(this.mDashBoardActivityController.newsArrayList.isEmpty());
    }

    @Test
    public void testLoadRecentLayout_MoreThenOneRecent()
    {
        this.mDashBoardActivityController.numberOfRecentContacts = 5;
        this.mDashBoardActivityController.loadRecentLayout();
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(this.mDashBoardActivityController.numberOfRecentContacts == 4);
    }

    @Test
    public void testLoadRecentLayout_OneRecent_FirstContainerIsVisible()
    {
        this.mDashBoardActivityController.isCurrentRecentContainerFirst = true;
        this.mDashBoardActivityController.numberOfRecentContacts = 1;
        this.mDashBoardActivityController.loadRecentLayout();
        MockDataForTests.checkThreadSchedulers();

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
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(this.mDashBoardActivityController.numberOfRecentContacts == 0);
        Assert.assertTrue(this.mDashBoardActivityController.isCurrentRecentContainerFirst);
        Assert.assertEquals(this.mDashBoardActivityController.mRecentContainer.getVisibility(), View.GONE);
        Assert.assertEquals(this.mDashBoardActivityController.mRecentContainer2.getVisibility(), View.VISIBLE);
    }

    @Test
    public void testLoadLocalContacts_OK()
    {
        this.mDashBoardActivityController.loadLocalContacts();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(this.mDashBoardActivityController.mProfileId != null);
        Assert.assertTrue(this.mDashBoardActivityController.mProfileId.length() > 0);
        Assert.assertTrue(this.mDashBoardActivityController.mActivity != null);

    }

    @Test
    public void testLoadLocalContacts_Fail()
    {
        this.mDashBoardActivityController.mProfileId = null;
        this.mDashBoardActivityController.loadLocalContacts();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(this.mDashBoardActivityController.mProfileId == null);
    }

    @Test
    public void testLoadRecentContactsAndUnreadMessages_FirstContainer()
    {
        this.mDashBoardActivityController.isCurrentRecentContainerFirst = true;
        this.mDashBoardActivityController.loadRecentContactsAndUnreadMessages();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(this.mDashBoardActivityController.mRecentContainer != null);
    }

    @Test
    public void testLoadRecentContactsAndUnreadMessages_SecondContainer()
    {
        this.mDashBoardActivityController.isCurrentRecentContainerFirst = false;
        this.mDashBoardActivityController.loadRecentContactsAndUnreadMessages();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(this.mDashBoardActivityController.mRecentContainer2 != null);
    }

    @Test
    public void testLoadRecent_Failed_WithControlledException()
    {
        PowerMockito.when(RealmContactTransactions.getAllRecentContacts(any(Realm.class)))
                .thenReturn(null);
        this.mDashBoardActivityController.loadRecents(this.mDashBoardActivityController.mRecentContainer);
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(this.mDashBoardActivityController.numberOfRecentContacts == 0);
    }

    @Test
    public void testLoadRecent_OK_OnlyGroupChat() throws Exception
    {
        String mockContactId_1 = "mc_5535b2ac13be4b7975c51600", mockContactId_2 = "mc_55409316799f7e1a109446f4";
        PowerMockito.when(RealmGroupChatTransactions.getGroupChatById(anyString(), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockGroupChat());
        PowerMockito.when(RealmContactTransactions.getUserProfile(any(Realm.class), any(String.class)))
                .thenReturn(MockDataForTests.getMockUserProfile());
        PowerMockito.when(RealmContactTransactions.getContactById(eq(mockContactId_1), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContactById(mockContactId_1));
        PowerMockito.when(RealmContactTransactions.getContactById(eq(mockContactId_2), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContactById(mockContactId_2));
        PowerMockito.when(RealmContactTransactions.getAllRecentContacts(any(Realm.class)))
                .thenReturn(MockDataForTests.getMockRecentContactsList_OnlyOneGroupChat());

        this.mDashBoardActivityController.loadRecents(this.mDashBoardActivityController.mRecentContainer);
        MockDataForTests.checkThreadSchedulers();

        View mView = this.mDashBoardActivityController.mRecentContainer.getChildAt(0);
        LinearLayout btRecent = (LinearLayout) mView.findViewById(R.id.recent_content);
        Assert.assertTrue(btRecent.performClick());
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(this.mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(this.mDashBoardActivityController.numberOfRecentContacts == 0);
        Assert.assertTrue(this.mDashBoardActivityController.mRecentContainer.getChildCount() == 1);
        assertThat(shadowIntent.getComponent().getClassName(), equalTo(GroupChatActivity.class.getName()));
    }

    @Test
    public void testLoadRecent_OK_NoGroup_ActionCall() throws Exception
    {

        String mockContactId_1 = "mc_5535b2ac13be4b7975c51600", mockContactId_2 = "mc_55409316799f7e1a109446f4";
        PowerMockito.when(RealmGroupChatTransactions.getGroupChatById(anyString(), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockGroupChat());
        PowerMockito.when(RealmContactTransactions.getUserProfile(any(Realm.class), anyString()))
                .thenReturn(MockDataForTests.getMockUserProfile());
        PowerMockito.when(RealmContactTransactions.getContactById(eq(mockContactId_1), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContactById(mockContactId_1));
        PowerMockito.when(RealmContactTransactions.getContactById(eq(mockContactId_2), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContactById(mockContactId_2));
        PowerMockito.when(RealmContactTransactions.getAllRecentContacts(any(Realm.class)))
                .thenReturn(MockDataForTests.getMockRecentContactsList_OneRecent_ActionCall());


        this.mDashBoardActivityController.loadRecents(this.mDashBoardActivityController.mRecentContainer);
        MockDataForTests.checkThreadSchedulers();

        View mView = this.mDashBoardActivityController.mRecentContainer.getChildAt(0);
        LinearLayout lay_main_container = (LinearLayout) mView.findViewById(R.id.recent_content);
        boolean isClicked = lay_main_container.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(isClicked);
        Assert.assertNull(mView.findViewById(R.id.lay_top_right_image_hide));
        Assert.assertTrue(this.mDashBoardActivityController.numberOfRecentContacts == 0);
        Assert.assertTrue(this.mDashBoardActivityController.mRecentContainer.getChildCount() == 1);
    }

    @Test
    public void testLoadRecent_OK_NoGroup_ActionSMS_MyComms() throws Exception
    {

        String mockContactId_1 = "mc_5535b2ac13be4b7975c51600", mockContactId_2 = "mc_55409316799f7e1a109446f4";
        PowerMockito.when(RealmGroupChatTransactions.getGroupChatById(anyString(), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockGroupChat());
        PowerMockito.when(RealmContactTransactions.getUserProfile(any(Realm.class), anyString()))
                .thenReturn(MockDataForTests.getMockUserProfile());
        PowerMockito.when(RealmContactTransactions.getContactById(eq(mockContactId_1), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContactById(mockContactId_1));
        PowerMockito.when(RealmContactTransactions.getContactById(eq(mockContactId_2), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContactById(mockContactId_2));
        PowerMockito.when(RealmContactTransactions.getAllRecentContacts(any(Realm.class)))
                .thenReturn(MockDataForTests.getMockRecentContactsList_OneRecent_ActionSMS_MyComms());


        this.mDashBoardActivityController.loadRecents(this.mDashBoardActivityController.mRecentContainer);
        MockDataForTests.checkThreadSchedulers();

        View mView = this.mDashBoardActivityController.mRecentContainer.getChildAt(0);
        LinearLayout lay_main_container = (LinearLayout) mView.findViewById(R.id.recent_content);
        boolean isClicked = lay_main_container.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(isClicked);
        Assert.assertNull(mView.findViewById(R.id.lay_top_right_image_hide));
        Assert.assertTrue(this.mDashBoardActivityController.numberOfRecentContacts == 0);
        Assert.assertTrue(this.mDashBoardActivityController.mRecentContainer.getChildCount() == 1);
    }

    @Test
    public void testLoadRecent_OK_NoGroup_ActionSMS_GlobalContacts() throws Exception
    {

        String mockContactId_1 = "mc_5535b2ac13be4b7975c51600", mockContactId_2 = "mc_55409316799f7e1a109446f4";
        PowerMockito.when(RealmGroupChatTransactions.getGroupChatById(anyString(), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockGroupChat());
        PowerMockito.when(RealmContactTransactions.getUserProfile(any(Realm.class), anyString()))
                .thenReturn(MockDataForTests.getMockUserProfile());
        PowerMockito.when(RealmContactTransactions.getContactById(eq(mockContactId_1), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContactById(mockContactId_1));
        PowerMockito.when(RealmContactTransactions.getContactById(eq(mockContactId_2), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContactById(mockContactId_2));
        PowerMockito.when(RealmContactTransactions.getAllRecentContacts(any(Realm.class)))
                .thenReturn(MockDataForTests.getMockRecentContactsList_OneRecent_ActionSMS_Global());


        this.mDashBoardActivityController.loadRecents(this.mDashBoardActivityController.mRecentContainer);
        MockDataForTests.checkThreadSchedulers();

        View mView = this.mDashBoardActivityController.mRecentContainer.getChildAt(0);
        LinearLayout lay_main_container = (LinearLayout) mView.findViewById(R.id.recent_content);
        boolean isClicked = lay_main_container.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(isClicked);
        Assert.assertNull(mView.findViewById(R.id.lay_top_right_image_hide));
        Assert.assertTrue(this.mDashBoardActivityController.numberOfRecentContacts == 0);
        Assert.assertTrue(this.mDashBoardActivityController.mRecentContainer.getChildCount() == 1);

    }

    @Test
    public void testLoadRecent_OK_NoGroup_ActionEmail() throws Exception
    {
        String mockContactId_1 = "mc_5535b2ac13be4b7975c51600", mockContactId_2 = "mc_55409316799f7e1a109446f4";
        PowerMockito.when(RealmGroupChatTransactions.getGroupChatById(anyString(), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockGroupChat());
        PowerMockito.when(RealmContactTransactions.getUserProfile(any(Realm.class), anyString()))
                .thenReturn(MockDataForTests.getMockUserProfile());
        PowerMockito.when(RealmContactTransactions.getContactById(eq(mockContactId_1), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContactById(mockContactId_1));
        PowerMockito.when(RealmContactTransactions.getContactById(eq(mockContactId_2), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContactById(mockContactId_2));
        PowerMockito.when(RealmContactTransactions.getAllRecentContacts(any(Realm.class)))
                .thenReturn(MockDataForTests.getMockRecentContactsList_OneRecent_ActionEMAIL());

        this.mDashBoardActivityController.loadRecents(this.mDashBoardActivityController.mRecentContainer);
        MockDataForTests.checkThreadSchedulers();

        View mView = this.mDashBoardActivityController.mRecentContainer.getChildAt(0);
        LinearLayout lay_main_container = (LinearLayout) mView.findViewById(R.id.recent_content);
        boolean isClicked = lay_main_container.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(isClicked);
        Assert.assertNull(mView.findViewById(R.id.lay_top_right_image_hide));
        Assert.assertTrue(this.mDashBoardActivityController.numberOfRecentContacts == 0);
        Assert.assertTrue(this.mDashBoardActivityController.mRecentContainer.getChildCount() == 1);
    }


    @Test
    public void testLoadRecent_FailWithNullData() throws Exception
    {

        String mockContactId_1 = "mc_5535b2ac13be4b7975c51600", mockContactId_2 = "mc_55409316799f7e1a109446f4";
        PowerMockito.when(RealmGroupChatTransactions.getGroupChatById(anyString(), any(Realm.class)))
                .thenReturn(null);
        PowerMockito.when(RealmContactTransactions.getUserProfile(any(Realm.class), anyString()))
                .thenReturn(null);
        PowerMockito.when(RealmContactTransactions.getContactById(eq(mockContactId_1), any(Realm.class)))
                .thenReturn(null);
        PowerMockito.when(RealmContactTransactions.getContactById(eq(mockContactId_2), any(Realm.class)))
                .thenReturn(null);
        PowerMockito.when(RealmContactTransactions.getAllRecentContacts(any(Realm.class)))
                .thenReturn(null);

        this.mDashBoardActivityController.loadRecents(this.mDashBoardActivityController.mRecentContainer);
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(this.mDashBoardActivityController.numberOfRecentContacts == 0);
    }

    @Test
    public void testLoadRecent_FailWithWrongNotNullData() throws Exception
    {

        String mockContactId_1 = "mc_5535b2ac13be4b7975c51600", mockContactId_2 = "mc_55409316799f7e1a109446f4";
        PowerMockito.when(RealmGroupChatTransactions.getGroupChatById(anyString(), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockGroupChat_WithWrongData());
        PowerMockito.when(RealmContactTransactions.getUserProfile(any(Realm.class), anyString()))
                .thenReturn(MockDataForTests.getMockUserProfile());
        PowerMockito.when(RealmContactTransactions.getContactById(eq(mockContactId_1), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContactById(mockContactId_1));
        PowerMockito.when(RealmContactTransactions.getContactById(eq(mockContactId_2), any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContactById(mockContactId_2));
        PowerMockito.when(RealmContactTransactions.getAllRecentContacts(any(Realm.class)))
                .thenReturn(MockDataForTests.getMockRecentContactsList_WithWrongData());


        this.mDashBoardActivityController.loadRecents(this.mDashBoardActivityController.mRecentContainer);
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(this.mDashBoardActivityController.numberOfRecentContacts != 0);
    }

    @Test
    public void testLoadUnreadMessages_GroupChat_OK_MoreThan0Messages() throws Exception
    {
        this.mDashBoardActivityController.mRecentContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this.mActivity);
        View view = inflater.inflate(R.layout.layout_recents_dashboard, this.mDashBoardActivityController.mRecentContainer, false);
        this.mDashBoardActivityController.mRecentContainer.addView(view);
        this.mDashBoardActivityController.hashMapRecentIdView = new HashMap<>();
        this.mDashBoardActivityController.hashMapRecentIdView.put(view, MockDataForTests.getMockRecentContactsList_OnlyOneGroupChat().get(0));

        PowerMockito.when(RealmGroupChatTransactions.getGroupChatPendingMessagesCount(eq("mg_55dc2a35a297b90a726e4cc2"), any(Realm.class)))
                .thenReturn((long) 5);
        PowerMockito.when(RealmChatTransactions.getChatPendingMessagesCount(Mockito.anyString(), Mockito.any(Realm.class)))
                .thenReturn((long) 5);

        this.mDashBoardActivityController.loadUnreadMessages(this.mDashBoardActivityController.mRecentContainer);
        MockDataForTests.checkThreadSchedulers();

        TextView unread_messages = (TextView) view.findViewById(R.id.unread_messages);
        Assert.assertNotNull(unread_messages);
        Assert.assertNotNull(unread_messages.getText().toString());
    }

    @Test
    public void testLoadUnreadMessages_GroupChat_OK_0Messages() throws Exception
    {
        long amountMessages = 0;
        mockUnreadMessages(amountMessages);

        this.mDashBoardActivityController.loadUnreadMessages(this.mDashBoardActivityController.mRecentContainer);
        MockDataForTests.checkThreadSchedulers();

        View view = this.mDashBoardActivityController.mRecentContainer.getChildAt(0);
        ImageView typeRecent = (ImageView) view.findViewById(R.id.type_recent);
        TextView unread_messages = (TextView) view.findViewById(R.id.unread_messages);

        Assert.assertTrue(typeRecent.getVisibility() == View.VISIBLE);
        Assert.assertTrue(unread_messages.getVisibility() != View.VISIBLE);
    }

    private void mockUnreadMessages(long amountMessages)
    {
        this.mDashBoardActivityController.mRecentContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this.mActivity);
        View view = inflater.inflate(R.layout.layout_recents_dashboard, this.mDashBoardActivityController.mRecentContainer, false);
        this.mDashBoardActivityController.mRecentContainer.addView(view);
        this.mDashBoardActivityController.hashMapRecentIdView = new HashMap<>();
        this.mDashBoardActivityController.hashMapRecentIdView.put(view, MockDataForTests.getMockRecentContactsList_OnlyOneGroupChat().get(0));

        PowerMockito.when(RealmGroupChatTransactions.getGroupChatPendingMessagesCount(eq("mg_55dc2a35a297b90a726e4cc2"), any(Realm.class)))
                .thenReturn(amountMessages);
        PowerMockito.when(RealmChatTransactions.getChatPendingMessagesCount(Mockito.anyString(), Mockito.any(Realm.class)))
                .thenReturn(amountMessages);
    }

    private void mockParams()
    {
        Downloader downloader = new OkHttpDownloader(mActivity.getApplicationContext(), Long.MAX_VALUE);
        Picasso.Builder builder = new Picasso.Builder(mActivity.getApplicationContext());
        builder.downloader(downloader);
        MycommsApp.picasso = builder.build();
    }
}
