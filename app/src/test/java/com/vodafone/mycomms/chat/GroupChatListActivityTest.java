package com.vodafone.mycomms.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chatgroup.GroupChatActivity;
import com.vodafone.mycomms.chatgroup.GroupChatController;
import com.vodafone.mycomms.chatgroup.GroupChatListActivity;
import com.vodafone.mycomms.chatgroup.GroupChatListFragment;
import com.vodafone.mycomms.contacts.detail.ContactDetailMainActivity;
import com.vodafone.mycomms.contacts.view.ContactListViewArrayAdapter;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.Constants;

import org.junit.After;
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
import org.robolectric.shadows.ShadowListView;

import io.realm.Realm;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by str_oan on 29/09/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*"})
@PrepareForTest(
        {
                Realm.class
                , Crashlytics.class
                , RealmContactTransactions.class
                , EndpointWrapper.class
                , GroupChatController.class
        })
public class GroupChatListActivityTest
{
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    public GroupChatListActivity mActivity;
    public GroupChatListFragment mGroupChatListFragment;
    public Context mContext;

    @Before
    public void setUp()
    {
        mockStatic(Realm.class);
        when(Realm.getDefaultInstance()).thenReturn(null);
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
        mContext = RuntimeEnvironment.application.getApplicationContext();
        preparePicasso();
    }

    @After
    public void tearDown() throws Exception
    {
        Robolectric.reset();
        this.mActivity = null;
        this.mGroupChatListFragment = null;
        System.gc();
    }

    @Test
    public void testCreateNewChat() throws Exception
    {
        setUpActivity_PreviousGroupChatListActivity();
        Thread.sleep(2000);
        checkThreadSchedulers();
        ListView listView = mGroupChatListFragment.getListView();
        LinearLayout chatContactsContainer = (LinearLayout) this.mGroupChatListFragment.getView().findViewById(R.id.list_group_chat_contacts);
        LinearLayout layGroupChatHeader = (LinearLayout) this.mGroupChatListFragment.getView().findViewById(R.id.group_chat_header);
        TextView txtNumberParticipants = (TextView) this.mGroupChatListFragment.getView().findViewById(R.id.txt_participants);
        TextView txtWrite = (TextView) this.mGroupChatListFragment.getView().findViewById(R.id.txt_write);
        int num = listView.getAdapter().getCount();
        Assert.assertTrue(num == 9);
        Assert.assertNotNull(this.mGroupChatListFragment.mSearchBarController);
        Assert.assertTrue(chatContactsContainer.getVisibility() == View.VISIBLE);
        Assert.assertTrue(chatContactsContainer.getChildCount() == 1);
        Assert.assertTrue(layGroupChatHeader.getVisibility() == View.GONE);

        ShadowListView shadowListView = Shadows.shadowOf(listView);
        shadowListView.populateItems();
        Assert.assertTrue(shadowListView.performItemClick(0));

        Assert.assertTrue(layGroupChatHeader.getVisibility() == View.VISIBLE);
        Assert.assertTrue(txtNumberParticipants.getText().toString().length() > 0);
        Assert.assertTrue(txtNumberParticipants.getText().toString().compareTo("Participants: 1") == 0);
        Assert.assertTrue(chatContactsContainer.getChildCount() == 1);
        Assert.assertTrue(txtWrite.isClickable());

        Assert.assertTrue(chatContactsContainer.getChildAt(0).performClick());
        Assert.assertTrue(chatContactsContainer.getVisibility() == View.VISIBLE);
        Assert.assertTrue(chatContactsContainer.getChildCount() == 1);
        Assert.assertTrue(layGroupChatHeader.getVisibility() == View.GONE);

        Assert.assertTrue(shadowListView.performItemClick(0));

        txtWrite.performClick();
        checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(this.mGroupChatListFragment.getActivity());
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(GroupChatActivity.class.getName()));
        Assert.assertTrue(this.mGroupChatListFragment.getActivity().isFinishing());
    }

    @Test
    public void testCreateGroupChat() throws Exception
    {
        SharedPreferences sp = mContext.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(Constants.PROFILE_ID_SHARED_PREF, "mc_5570340e7eb7c3512f2f9bf2").apply();

        PowerMockito.mockStatic(GroupChatController.class);
        GroupChatController mController = PowerMockito.mock(GroupChatController.class);
        PowerMockito.when(GroupChatController.newInstance(Mockito.any(Context.class), Mockito.anyString()))
                .thenReturn(mController);
        PowerMockito.when(mController.isCreatedJSONBodyForCreateGroupChat()).thenReturn(true);
        PowerMockito.when(mController.executeRequest()).thenReturn("{\n \"ok\": \"ok\"\n}");
        PowerMockito.when(mController.getResponseCode()).thenReturn("200");
        PowerMockito.when(mController.getCreatedGroupChatId(Mockito.anyString())).thenReturn("mg_55dc2a35a297b90a726e4cc2");

        setUpActivity_PreviousGroupChatListActivity();
        Thread.sleep(2000);
        checkThreadSchedulers();
        ListView listView = mGroupChatListFragment.getListView();
        LinearLayout chatContactsContainer = (LinearLayout) this.mGroupChatListFragment.getView().findViewById(R.id.list_group_chat_contacts);
        LinearLayout layGroupChatHeader = (LinearLayout) this.mGroupChatListFragment.getView().findViewById(R.id.group_chat_header);
        TextView txtNumberParticipants = (TextView) this.mGroupChatListFragment.getView().findViewById(R.id.txt_participants);
        TextView txtWrite = (TextView) this.mGroupChatListFragment.getView().findViewById(R.id.txt_write);
        int num = listView.getAdapter().getCount();
        Assert.assertTrue(num == 9);
        Assert.assertNotNull(this.mGroupChatListFragment.mSearchBarController);
        Assert.assertTrue(chatContactsContainer.getVisibility() == View.VISIBLE);
        Assert.assertTrue(chatContactsContainer.getChildCount() == 1);
        Assert.assertTrue(layGroupChatHeader.getVisibility() == View.GONE);

        ShadowListView shadowListView = Shadows.shadowOf(listView);
        shadowListView.populateItems();
        Assert.assertTrue(shadowListView.performItemClick(0));
        Assert.assertTrue(shadowListView.performItemClick(1));
        Assert.assertTrue(shadowListView.performItemClick(2));

        Assert.assertTrue(layGroupChatHeader.getVisibility() == View.VISIBLE);
        Assert.assertTrue(txtNumberParticipants.getText().toString().length() > 0);
        Assert.assertTrue(txtNumberParticipants.getText().toString().compareTo("Participants: 3") == 0);
        Assert.assertTrue(chatContactsContainer.getChildCount() == 3);
        Assert.assertTrue(txtWrite.isClickable());

        txtWrite.performClick();
        Thread.sleep(2000);
        checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(this.mGroupChatListFragment.getActivity());
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(GroupChatActivity.class.getName()));
        Assert.assertTrue(this.mGroupChatListFragment.getActivity().isFinishing());
    }

    private void setUpActivity_PreviousGroupChatListActivity()
    {
        PowerMockito.mockStatic(RealmContactTransactions.class);
        PowerMockito.when(RealmContactTransactions.getAllContacts(Mockito.any(Realm.class), Mockito.anyString()))
                .thenReturn(MockDataForTests.getMockContactsList());
        Intent intent = new Intent();
        intent.putExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY, GroupChatListActivity.class.getSimpleName());
        this.mActivity = Robolectric.buildActivity(GroupChatListActivity.class).withIntent(intent)
                .create().start().resume().visible().get();
        this.mGroupChatListFragment = (GroupChatListFragment)this.mActivity.getSupportFragmentManager().getFragments().get(0);
    }

    private void checkThreadSchedulers()
    {
        if(Robolectric.getBackgroundThreadScheduler().areAnyRunnable())
            Robolectric.flushBackgroundThreadScheduler();
        if(Robolectric.getForegroundThreadScheduler().areAnyRunnable())
            Robolectric.flushForegroundThreadScheduler();
    }

    private void preparePicasso()
    {
        Downloader downloader = new OkHttpDownloader(mContext, Long.MAX_VALUE);
        Picasso.Builder builder = new Picasso.Builder(mContext);
        builder.downloader(downloader);
        MycommsApp.picasso = builder.build();
    }
}
