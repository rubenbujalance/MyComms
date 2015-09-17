package com.vodafone.mycomms.contacts;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.chatgroup.GroupChatActivity;
import com.vodafone.mycomms.contacts.view.ContactListFragment;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.CustomFragmentActivity;

import org.junit.Assert;
import org.junit.Before;
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
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowListView;

import io.realm.Realm;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by str_oan on 10/09/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*"})
@PrepareForTest({Realm.class
        , Crashlytics.class
        , RealmGroupChatTransactions.class, RealmContactTransactions.class})
public class RecentContactsControllerTest
{
    @Rule
    public PowerMockRule rule = new PowerMockRule();
    public Context mContext;
    public CustomFragmentActivity mCustomFragmentActivity;
    public ContactListFragment mContactListFragment;

    @Before
    public void setUp()
    {
        mockStatic(Realm.class);
        when(Realm.getDefaultInstance()).thenReturn(null);
        mockStatic(Crashlytics.class);
        mContext = RuntimeEnvironment.application.getApplicationContext();
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testContactListFragmentLifecycle()
    {
        Intent in = new Intent(RuntimeEnvironment.application.getApplicationContext(),
                CustomFragmentActivity.class);
        in.putExtra("index", 1);
        mCustomFragmentActivity = Robolectric.buildActivity(CustomFragmentActivity.class)
                .withIntent(in)
                .create().start().resume().pause().stop().destroy().get();
        Assert.assertTrue(mCustomFragmentActivity.isDestroyed());
    }

    @Test
    public void testContactListFragment_LoadRecentContacts()
    {
        try
        {
            PowerMockito.mockStatic(RealmContactTransactions.class);
            PowerMockito.when(RealmContactTransactions.getAllRecentContacts(Matchers.any(Realm.class)))
                    .thenReturn(MockDataForTests.getMockRecentContactsList());

            PowerMockito.mockStatic(RealmGroupChatTransactions.class);
            PowerMockito.when(RealmGroupChatTransactions.getGroupChatById(Matchers.anyString(), Matchers.any(Realm.class)))
                    .thenReturn(MockDataForTests.getMockGroupChat());
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testContactListFragment_LoadRecentContacts Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        startContactListFragment(1);
        mContactListFragment = (ContactListFragment)mCustomFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("1");
        try
        {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testContactListFragment_LoadContactsFromDB Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }
        Robolectric.flushForegroundThreadScheduler();

        Assert.assertTrue(!mContactListFragment.getRecentContactList().isEmpty());
        Assert.assertTrue(mContactListFragment.getRecentContactList().size() == 4);

    }

    @Test
    public void testContactListFragment_LoadListViewElements_Click_ActionSMS_SimpleChat()
    {
        try
        {
            PowerMockito.mockStatic(RealmContactTransactions.class);
            PowerMockito.when(RealmContactTransactions.getAllRecentContacts(Matchers.any(Realm.class)))
                    .thenReturn(MockDataForTests.getMockRecentContactsList());
            PowerMockito.when(RealmContactTransactions.getContactById(Matchers.anyString(), Matchers.any(Realm.class)))
                    .thenReturn(MockDataForTests.getMockContactsList().get(0));
            PowerMockito.when(RealmContactTransactions.getUserProfile(Matchers.any(Realm.class), Matchers.anyString()))
                    .thenReturn(MockDataForTests.getMockUserProfile());

            PowerMockito.mockStatic(RealmGroupChatTransactions.class);
            PowerMockito.when(RealmGroupChatTransactions.getGroupChatById(Matchers.anyString(), Matchers.any(Realm.class)))
                    .thenReturn(MockDataForTests.getMockGroupChat());
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testContactListFragment_LoadListViewElements_Click_ActionSMS_SimpleChat Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        startContactListFragment(1);
        mContactListFragment = (ContactListFragment)mCustomFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("1");
        mockParams();
        try
        {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testContactListFragment_LoadListViewElements_Click_ActionSMS_SimpleChat Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }
        Robolectric.flushForegroundThreadScheduler();

        ShadowListView shadowListView = Shadows.shadowOf(mContactListFragment.getListView());
        shadowListView.populateItems();
        Assert.assertTrue(shadowListView.performItemClick(0));

        ShadowActivity shadowActivity = Shadows.shadowOf(mContactListFragment.getActivity());
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(GroupChatActivity.class.getName()));
    }

    @Test
    public void testContactListFragment_LoadListViewElements_Click_ActionSMS_GroupChat()
    {
        try
        {
            PowerMockito.mockStatic(RealmContactTransactions.class);
            PowerMockito.when(RealmContactTransactions.getAllRecentContacts(Matchers.any(Realm.class)))
                    .thenReturn(MockDataForTests.getMockRecentContactsList());
            PowerMockito.when(RealmContactTransactions.getContactById(Matchers.anyString(), Matchers.any(Realm.class)))
                    .thenReturn(MockDataForTests.getMockContactsList().get(0));
            PowerMockito.when(RealmContactTransactions.getUserProfile(Matchers.any(Realm.class), Matchers.anyString()))
                    .thenReturn(MockDataForTests.getMockUserProfile());

            PowerMockito.mockStatic(RealmGroupChatTransactions.class);
            PowerMockito.when(RealmGroupChatTransactions.getGroupChatById(Matchers.anyString(), Matchers.any(Realm.class)))
                    .thenReturn(MockDataForTests.getMockGroupChat());
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testContactListFragment_LoadListViewElements_Click_ActionSMS_GroupChat Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        startContactListFragment(1);
        mContactListFragment = (ContactListFragment)mCustomFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("1");
        mockParams();
        try
        {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testContactListFragment_LoadListViewElements_Click_ActionSMS_GroupChat Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }
        Robolectric.flushForegroundThreadScheduler();

        ShadowListView shadowListView = Shadows.shadowOf(mContactListFragment.getListView());
        shadowListView.populateItems();
        Assert.assertTrue(shadowListView.performItemClick(1));

        ShadowActivity shadowActivity = Shadows.shadowOf(mContactListFragment.getActivity());
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(GroupChatActivity.class.getName()));
    }

    @Test
    public void testContactListFragment_LoadListViewElements_Click_ActionCall()
    {
        try
        {
            PowerMockito.mockStatic(RealmContactTransactions.class);
            PowerMockito.when(RealmContactTransactions.getAllRecentContacts(Matchers.any(Realm.class)))
                    .thenReturn(MockDataForTests.getMockRecentContactsList());
            PowerMockito.when(RealmContactTransactions.getContactById(Matchers.anyString(), Matchers.any(Realm.class)))
                    .thenReturn(MockDataForTests.getMockContactsList().get(0));
            PowerMockito.when(RealmContactTransactions.getUserProfile(Matchers.any(Realm.class), Matchers.anyString()))
                    .thenReturn(MockDataForTests.getMockUserProfile());

            PowerMockito.mockStatic(RealmGroupChatTransactions.class);
            PowerMockito.when(RealmGroupChatTransactions.getGroupChatById(Matchers.anyString(), Matchers.any(Realm.class)))
                    .thenReturn(MockDataForTests.getMockGroupChat());
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testContactListFragment_LoadListViewElements_Click_ActionCall Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        startContactListFragment(1);
        mContactListFragment = (ContactListFragment)mCustomFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("1");
        mockParams();
        try
        {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testContactListFragment_LoadListViewElements_Click_ActionCall Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }
        Robolectric.flushForegroundThreadScheduler();

        ShadowListView shadowListView = Shadows.shadowOf(mContactListFragment.getListView());
        shadowListView.populateItems();
        Assert.assertTrue(shadowListView.performItemClick(2));

        ShadowActivity shadowActivity = Shadows.shadowOf(mContactListFragment.getActivity());
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getAction().equals(Intent.ACTION_CALL));
    }

    @Test
    public void testContactListFragment_LoadListViewElements_Click_ActionEmail()
    {
        try
        {
            PowerMockito.mockStatic(RealmContactTransactions.class);
            PowerMockito.when(RealmContactTransactions.getAllRecentContacts(Matchers.any(Realm.class)))
                    .thenReturn(MockDataForTests.getMockRecentContactsList());
            PowerMockito.when(RealmContactTransactions.getContactById(Matchers.anyString(), Matchers.any(Realm.class)))
                    .thenReturn(MockDataForTests.getMockContactsList().get(0));
            PowerMockito.when(RealmContactTransactions.getUserProfile(Matchers.any(Realm.class), Matchers.anyString()))
                    .thenReturn(MockDataForTests.getMockUserProfile());

            PowerMockito.mockStatic(RealmGroupChatTransactions.class);
            PowerMockito.when(RealmGroupChatTransactions.getGroupChatById(Matchers.anyString(), Matchers.any(Realm.class)))
                    .thenReturn(MockDataForTests.getMockGroupChat());
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testContactListFragment_LoadListViewElements_Click_ActionCall Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        startContactListFragment(1);
        mContactListFragment = (ContactListFragment)mCustomFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("1");
        mockParams();
        try
        {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testContactListFragment_LoadListViewElements_Click_ActionCall Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }
        Robolectric.flushForegroundThreadScheduler();

        ShadowListView shadowListView = Shadows.shadowOf(mContactListFragment.getListView());
        shadowListView.populateItems();
        Assert.assertTrue(shadowListView.performItemClick(3));

        ShadowActivity shadowActivity = Shadows.shadowOf(mContactListFragment.getActivity());
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getAction().equals(Intent.ACTION_SEND));
    }

    public void startContactListFragment(int index)
    {
        Intent in = new Intent(RuntimeEnvironment.application.getApplicationContext(),
                CustomFragmentActivity.class);
        in.putExtra("index", index);
        mCustomFragmentActivity = Robolectric.buildActivity(CustomFragmentActivity.class)
                .withIntent(in)
                .create().start().resume().visible().get();
    }

    private void mockParams()
    {
        Downloader downloader = new OkHttpDownloader(mContactListFragment.getActivity().getApplicationContext(), Long.MAX_VALUE);
        Picasso.Builder builder = new Picasso.Builder(mContactListFragment.getActivity().getApplicationContext());
        builder.downloader(downloader);
        MycommsApp.picasso = builder.build();
    }
}
