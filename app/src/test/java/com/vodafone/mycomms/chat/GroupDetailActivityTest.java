package com.vodafone.mycomms.chat;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Downloader;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chatgroup.GroupChatListActivity;
import com.vodafone.mycomms.chatgroup.GroupDetailActivity;
import com.vodafone.mycomms.chatgroup.view.GroupHolder;
import com.vodafone.mycomms.contacts.detail.ContactDetailMainActivity;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
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
import org.robolectric.shadows.ShadowView;

import java.io.PrintWriter;
import java.io.StringWriter;

import io.realm.Realm;
import model.GroupChat;

/**
 * Created by str_oan on 30/09/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 18)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*"})
@PrepareForTest(
        {
                Realm.class
                , RealmGroupChatTransactions.class
                , RealmContactTransactions.class
        })
public class GroupDetailActivityTest
{
    @Rule
    public PowerMockRule rule = new PowerMockRule();
    public GroupDetailActivity activity;
    public Context mContext;
    public MotionEvent motionEvent;

    @Before
    public void setUp()
    {
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
        PowerMockito.mockStatic(Realm.class);
        PowerMockito.when(Realm.getInstance(Mockito.any(Context.class))).thenReturn(null);
        MycommsApp.stateCounter = 0;
        mContext = RuntimeEnvironment.application.getApplicationContext();
        motionEvent = MotionEvent.obtain(500, -1, MotionEvent.ACTION_UP, 30, 30, -1);
        preparePicasso();
    }

    @After
    public void tearDown() throws Exception
    {
        Robolectric.reset();
        activity = null;
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testLifeCycle()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        SharedPreferences sp = mContext.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(Constants.PROFILE_ID_SHARED_PREF, "mc_5570340e7eb7c3512f2f9bf2").apply();
        PowerMockito.mockStatic(RealmGroupChatTransactions.class);
        PowerMockito.mockStatic(RealmContactTransactions.class);
        PowerMockito.when(RealmGroupChatTransactions.getGroupChatById(Mockito.anyString(), Mockito.any(Realm.class)))
                .thenReturn(MockDataForTests.getMockGroupChat());
        PowerMockito.when(RealmContactTransactions.getContactById(Mockito.anyString(), Mockito.any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContact());
        PowerMockito.when(RealmContactTransactions.getUserProfile(Mockito.any(Realm.class), Mockito.anyString()))
                .thenReturn(MockDataForTests.getMockUserProfile());
        Intent intent = new Intent();
        intent.putExtra(Constants.GROUP_CHAT_ID, "mg_55dc2a35a297b90a726e4cc2");
        this.activity = Robolectric.buildActivity(GroupDetailActivity.class).withIntent(intent)
                .create().start().resume().stop().destroy().get();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue( MycommsApp.stateCounter == 0);
        Assert.assertTrue(this.activity.isDestroyed());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testCreationAndModifyGroupButtonClicked() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        RecyclerView mRecyclerView = (RecyclerView) this.activity.findViewById(R.id.recycler_view);
        mRecyclerView.measure(0, 0);
        mRecyclerView.layout(0, 0, 100, 100);

        GroupHolder holder = (GroupHolder)mRecyclerView.findViewHolderForPosition(0);
        Assert.assertNotNull(holder);
        Assert.assertTrue(holder.getTextViewName().getText().toString().length() > 0);

        LinearLayout lay_add_contact = (LinearLayout) this.activity.findViewById(R.id.lay_add_member);
        Assert.assertTrue(lay_add_contact.performClick());
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(this.activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(GroupChatListActivity.class.getName()));
        Assert.assertTrue(this.activity.isFinishing());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testCreationAndClickOnContact() throws Exception {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        RecyclerView mRecyclerView = (RecyclerView) this.activity.findViewById(R.id.recycler_view);
        mRecyclerView.measure(0, 0);
        mRecyclerView.layout(0, 0, 100, 100);

        GroupHolder holder = (GroupHolder) mRecyclerView.findViewHolderForPosition(0);
        Assert.assertNotNull(holder);
        Assert.assertTrue(holder.getTextViewName().getText().toString().length() > 0);

        View view = mRecyclerView.getChildAt(0);
        view.setClickable(true);
        view.setEnabled(true);
        ShadowView shadowView = Shadows.shadowOf(view);
        shadowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startContactDetailMainActivity(0);
            }
        });

        Assert.assertTrue(shadowView.checkedPerformClick());
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(this.activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(ContactDetailMainActivity.class.getName()));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testCreationWithAvatarNameAndClickOnContact() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        PowerMockito.mockStatic(RealmGroupChatTransactions.class);
        PowerMockito.mockStatic(RealmContactTransactions.class);
        GroupChat groupChat = MockDataForTests.getMockGroupChat();
        groupChat.setAvatar("");
        PowerMockito.when(RealmGroupChatTransactions.getGroupChatById(Mockito.anyString(), Mockito.any(Realm.class)))
                .thenReturn(groupChat);
        PowerMockito.when(RealmContactTransactions.getContactById(Mockito.anyString(), Mockito.any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContact());
        PowerMockito.when(RealmContactTransactions.getUserProfile(Mockito.any(Realm.class), Mockito.anyString()))
                .thenReturn(MockDataForTests.getMockUserProfile());
        Intent intent = new Intent();
        intent.putExtra(Constants.GROUP_CHAT_ID, "mg_55dc2a35a297b90a726e4cc2");
        this.activity = Robolectric.buildActivity(GroupDetailActivity.class).withIntent(intent)
                .create().start().resume().visible().get();

        MockDataForTests.checkThreadSchedulers();

        RecyclerView mRecyclerView = (RecyclerView) this.activity.findViewById(R.id.recycler_view);
        mRecyclerView.measure(0, 0);
        mRecyclerView.layout(0, 0, 100, 100);

        GroupHolder holder = (GroupHolder) mRecyclerView.findViewHolderForPosition(0);
        Assert.assertNotNull(holder);
        Assert.assertTrue(holder.getTextViewName().getText().toString().length() > 0);

        View view = mRecyclerView.getChildAt(0);
        view.setClickable(true);
        view.setEnabled(true);
        ShadowView shadowView = Shadows.shadowOf(view);
        shadowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startContactDetailMainActivity(0);
            }
        });

        Assert.assertTrue(shadowView.checkedPerformClick());
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(this.activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(ContactDetailMainActivity.class.getName()));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testCreationAndBackButtonClicked() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        RecyclerView mRecyclerView = (RecyclerView) this.activity.findViewById(R.id.recycler_view);
        mRecyclerView.measure(0, 0);
        mRecyclerView.layout(0, 0, 100, 100);

        GroupHolder holder = (GroupHolder)mRecyclerView.findViewHolderForPosition(0);
        Assert.assertNotNull(holder);
        Assert.assertTrue(holder.getTextViewName().getText().toString().length() > 0);

        ImageView backButton = (ImageView) this.activity.findViewById(R.id.back_button);
        Assert.assertTrue(backButton.performClick());
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(this.activity.isFinishing());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testCreationAndBackLayoutClicked() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        RecyclerView mRecyclerView = (RecyclerView) this.activity.findViewById(R.id.recycler_view);
        mRecyclerView.measure(0, 0);
        mRecyclerView.layout(0, 0, 100, 100);

        GroupHolder holder = (GroupHolder)mRecyclerView.findViewHolderForPosition(0);
        Assert.assertNotNull(holder);
        Assert.assertTrue(holder.getTextViewName().getText().toString().length() > 0);

        LinearLayout backArea = (LinearLayout) this.activity.findViewById(R.id.back_area);
        Assert.assertTrue(backArea.performClick());
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(this.activity.isFinishing());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }



    private void setUpActivity()
    {
        PowerMockito.mockStatic(RealmGroupChatTransactions.class);
        PowerMockito.mockStatic(RealmContactTransactions.class);
        PowerMockito.when(RealmGroupChatTransactions.getGroupChatById(Mockito.anyString(), Mockito.any(Realm.class)))
                .thenReturn(MockDataForTests.getMockGroupChat());
        PowerMockito.when(RealmContactTransactions.getContactById(Mockito.anyString(), Mockito.any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContact());
        PowerMockito.when(RealmContactTransactions.getUserProfile(Mockito.any(Realm.class), Mockito.anyString()))
                .thenReturn(MockDataForTests.getMockUserProfile());
        Intent intent = new Intent();
        intent.putExtra(Constants.GROUP_CHAT_ID, "mg_55dc2a35a297b90a726e4cc2");
        this.activity = Robolectric.buildActivity(GroupDetailActivity.class).withIntent(intent)
                .create().start().resume().visible().get();
    }

    private void preparePicasso()
    {
        Downloader downloader = new OkHttpDownloader(RuntimeEnvironment.application.getApplicationContext(), Long.MAX_VALUE);
        Picasso.Builder builder = new Picasso.Builder(RuntimeEnvironment.application.getApplicationContext());
        builder.downloader(downloader);
        MycommsApp.picasso = builder.build();
    }
}
