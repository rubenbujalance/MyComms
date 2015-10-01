package com.vodafone.mycomms.chat;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.widget.RecyclerView;

import com.squareup.picasso.Downloader;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chatgroup.GroupDetailActivity;
import com.vodafone.mycomms.chatgroup.view.GroupHolder;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.Constants;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.MockRepository;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import io.realm.Realm;

/**
 * Created by str_oan on 30/09/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21)
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

    @Before
    public void setUp()
    {
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
        PowerMockito.mockStatic(Realm.class);
        PowerMockito.when(Realm.getInstance(Mockito.any(Context.class))).thenReturn(null);
        MycommsApp.stateCounter = 0;
        mContext = RuntimeEnvironment.application.getApplicationContext();
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testLifeCycle()
    {
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
        Assert.assertTrue( MycommsApp.stateCounter == 0);
        Assert.assertTrue(this.activity.isDestroyed());
    }

    @Test
    public void testCreationAndModifyGroupButtonClicked() throws Exception
    {
        setUpActivity();
        ShadowLooper.pauseMainLooper();
        checkThreadSchedulers();
        ShadowLooper.unPauseMainLooper();

        RecyclerView mRecyclerView = (RecyclerView) this.activity.findViewById(R.id.recycler_view);
        mRecyclerView.measure(0, 0);
        mRecyclerView.layout(0, 0, 100, 100);

        GroupHolder holder = (GroupHolder)mRecyclerView.findViewHolderForPosition(0);
        Assert.assertNotNull(holder);
        Assert.assertTrue(holder.getTextViewName().getText().toString().length() > 0);
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

    private void checkThreadSchedulers()
    {
        if(Robolectric.getBackgroundThreadScheduler().areAnyRunnable())
            Robolectric.flushBackgroundThreadScheduler();
        if(Robolectric.getForegroundThreadScheduler().areAnyRunnable())
            Robolectric.flushForegroundThreadScheduler();
    }
}
