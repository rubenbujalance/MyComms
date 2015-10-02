package com.vodafone.mycomms.chat;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
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
import com.vodafone.mycomms.chatlist.view.ChatListActivity;
import com.vodafone.mycomms.contacts.detail.ContactDetailMainActivity;
import com.vodafone.mycomms.contacts.view.ContactListViewArrayAdapter;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ReloadAdapterEvent;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.search.SearchBarController;
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
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowListView;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import io.realm.Realm;
import model.Contact;

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
                , RealmGroupChatTransactions.class
                , SearchBarController.class
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

    @Test
    public void testCreateNewChat() throws Exception
    {
        setUpActivity_PreviousGroupChatListActivity();
        MockDataForTests.checkThreadSchedulers();

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
        MockDataForTests.checkThreadSchedulers();

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
        MockDataForTests.checkThreadSchedulers();

        txtWrite.performClick();
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(this.mGroupChatListFragment.getActivity());
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(GroupChatActivity.class.getName()));
        Assert.assertTrue(this.mGroupChatListFragment.getActivity().isFinishing());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testCreateGroupChatWithErrorAndOk() throws Exception
    {
        PowerMockito.mockStatic(GroupChatController.class);
        GroupChatController mController = PowerMockito.mock(GroupChatController.class);
        PowerMockito.when(GroupChatController.newInstance(Mockito.any(Context.class), Mockito.anyString()))
                .thenReturn(mController);
        PowerMockito.when(mController.isCreatedJSONBodyForCreateGroupChat()).thenReturn(true);
        PowerMockito.when(mController.executeRequest()).thenReturn(null);
        PowerMockito.when(mController.getResponseCode()).thenReturn("400");
        PowerMockito.when(mController.getCreatedGroupChatId(Mockito.anyString())).thenReturn(null);

        setUpActivity_PreviousGroupChatListActivity();
        MockDataForTests.checkThreadSchedulers();

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

        //Test error on group chat creation
        txtWrite.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertFalse(this.mGroupChatListFragment.getActivity().isFinishing());
        //Test OK in group chat creation
        PowerMockito.when(mController.isCreatedJSONBodyForCreateGroupChat()).thenReturn(true);
        PowerMockito.when(mController.executeRequest()).thenReturn("{\n \"ok\": \"ok\"\n}");
        PowerMockito.when(mController.getResponseCode()).thenReturn("200");
        PowerMockito.when(mController.getCreatedGroupChatId(Mockito.anyString())).thenReturn("mg_55dc2a35a297b90a726e4cc2");

        txtWrite.performClick();
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(this.mGroupChatListFragment.getActivity());
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(GroupChatActivity.class.getName()));
        Assert.assertTrue(this.mGroupChatListFragment.getActivity().isFinishing());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testUpdateExistentGroupChat() throws Exception
    {
        PowerMockito.mockStatic(GroupChatController.class);
        GroupChatController mController = PowerMockito.mock(GroupChatController.class);
        PowerMockito.when(GroupChatController.newInstance(Mockito.any(Context.class), Mockito.anyString()))
                .thenReturn(mController);

        setUpActivity_PreviousChatListActivity();
        MockDataForTests.checkThreadSchedulers();

        ListView listView = mGroupChatListFragment.getListView();
        LinearLayout chatContactsContainer = (LinearLayout) this.mGroupChatListFragment.getView().findViewById(R.id.list_group_chat_contacts);
        LinearLayout layGroupChatHeader = (LinearLayout) this.mGroupChatListFragment.getView().findViewById(R.id.group_chat_header);
        TextView txtNumberParticipants = (TextView) this.mGroupChatListFragment.getView().findViewById(R.id.txt_participants);
        TextView txtWrite = (TextView) this.mGroupChatListFragment.getView().findViewById(R.id.txt_write);
        int num = listView.getAdapter().getCount();
        Assert.assertTrue(num == 9);
        Assert.assertNotNull(this.mGroupChatListFragment.mSearchBarController);
        Assert.assertTrue(chatContactsContainer.getVisibility() == View.VISIBLE);
        Assert.assertTrue(chatContactsContainer.getChildCount() == 2);
        Assert.assertTrue(layGroupChatHeader.getVisibility() == View.VISIBLE);
        Assert.assertTrue(txtNumberParticipants.getText().toString().compareTo("Participants: 2") == 0);
        Assert.assertTrue(txtWrite.getVisibility() == View.VISIBLE);

        //Check that we cannot take out more contacts, because this converts group chat in chat
        //which is not available here

        Assert.assertTrue(chatContactsContainer.getChildAt(0).performClick());
        MockDataForTests.checkThreadSchedulers();

        Assert.assertNotNull(this.mGroupChatListFragment.mSearchBarController);
        Assert.assertTrue(chatContactsContainer.getVisibility() == View.VISIBLE);
        Assert.assertTrue(chatContactsContainer.getChildCount() == 1);
        Assert.assertTrue(layGroupChatHeader.getVisibility() == View.VISIBLE);
        Assert.assertTrue(txtNumberParticipants.getText().toString().compareTo("Participants: 1") == 0);
        Assert.assertTrue(txtWrite.getVisibility() == View.INVISIBLE);

        ShadowListView shadowListView = Shadows.shadowOf(listView);
        shadowListView.populateItems();

        Assert.assertTrue(shadowListView.performItemClick(0));
        Assert.assertNotNull(this.mGroupChatListFragment.mSearchBarController);
        Assert.assertTrue(chatContactsContainer.getVisibility() == View.VISIBLE);
        Assert.assertTrue(chatContactsContainer.getChildCount() == 2);
        Assert.assertTrue(layGroupChatHeader.getVisibility() == View.VISIBLE);
        Assert.assertTrue(txtNumberParticipants.getText().toString().compareTo("Participants: 2") == 0);
        Assert.assertTrue(txtWrite.getVisibility() == View.VISIBLE);

        //Check one more time to make sure that the same item is not added again
        Assert.assertTrue(shadowListView.performItemClick(0));
        Assert.assertNotNull(this.mGroupChatListFragment.mSearchBarController);
        Assert.assertTrue(chatContactsContainer.getVisibility() == View.VISIBLE);
        Assert.assertTrue(chatContactsContainer.getChildCount() == 2);
        Assert.assertTrue(layGroupChatHeader.getVisibility() == View.VISIBLE);
        Assert.assertTrue(txtNumberParticipants.getText().toString().compareTo("Participants: 2") == 0);
        Assert.assertTrue(txtWrite.getVisibility() == View.VISIBLE);

        //Error response
        PowerMockito.when(mController.isCreatedJSONBodyForUpdateGroupChat()).thenReturn(true);
        PowerMockito.when(mController.executeRequest()).thenReturn("{\n \"err\": \"err\"\n}");
        PowerMockito.when(mController.getResponseCode()).thenReturn("400");
        PowerMockito.when(mController.getCreatedGroupChatId(Mockito.anyString())).thenReturn(null);

        txtWrite.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertFalse(this.mGroupChatListFragment.getActivity().isFinishing());

        //Empty response
        PowerMockito.when(mController.isCreatedJSONBodyForUpdateGroupChat()).thenReturn(true);
        PowerMockito.when(mController.executeRequest()).thenReturn("");
        PowerMockito.when(mController.getResponseCode()).thenReturn("400");
        PowerMockito.when(mController.getCreatedGroupChatId(Mockito.anyString())).thenReturn(null);

        txtWrite.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertFalse(this.mGroupChatListFragment.getActivity().isFinishing());

        //Correct response
        PowerMockito.when(mController.isCreatedJSONBodyForUpdateGroupChat()).thenReturn(true);
        PowerMockito.when(mController.executeRequest()).thenReturn("{\n\"ok\": \"ok\"\n}");
        PowerMockito.when(mController.getResponseCode()).thenReturn("200");
        PowerMockito.when(mController.getCreatedGroupChatId(Mockito.anyString())).thenReturn("mg_55dc2a35a297b90a726e4cc2");

        txtWrite.performClick();
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(this.mGroupChatListFragment.getActivity());
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(GroupChatActivity.class.getName()));
        Assert.assertTrue(this.mGroupChatListFragment.getActivity().isFinishing());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");

    }

    @Test
    public void testOnConnectivityChanged_Connected() throws Exception
    {
        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET);
        setUpActivity_PreviousGroupChatListActivity();
        MockDataForTests.checkThreadSchedulers();

        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();

        LinearLayout lay_no_connection = (LinearLayout) this.mGroupChatListFragment.getActivity().findViewById(R.id.no_connection_layout);
        Assert.assertTrue(lay_no_connection.getVisibility() == View.GONE);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testOnConnectivityChanged_NotConnected() throws Exception
    {
        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.OFFLINE);
        setUpActivity_PreviousGroupChatListActivity();
        MockDataForTests.checkThreadSchedulers();

        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();

        LinearLayout lay_no_connection = (LinearLayout) this.mGroupChatListFragment.getActivity().findViewById(R.id.no_connection_layout);
        Assert.assertTrue(lay_no_connection.getVisibility() == View.VISIBLE);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testReloadAdapterEvent() throws Exception
    {
        ArrayList<Contact> contactArrayList = new ArrayList<>();
        contactArrayList.add(MockDataForTests.getMockContact());

        PowerMockito.mockStatic(SearchBarController.class);
        PowerMockito.when(SearchBarController.getContactList()).thenReturn(contactArrayList);
        ReloadAdapterEvent event = new ReloadAdapterEvent();
        setUpActivity_PreviousGroupChatListActivity();
        MockDataForTests.checkThreadSchedulers();

        ArrayList<Contact> contacts = this.mGroupChatListFragment.contactList;
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();

        Assert.assertNotEquals(contacts.size(), this.mGroupChatListFragment.contactList.size());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testLifeCycle()
    {
        MycommsApp.stateCounter = 0;
        SharedPreferences sp = mContext.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(Constants.PROFILE_ID_SHARED_PREF, "mc_5570340e7eb7c3512f2f9bf2").apply();

        PowerMockito.mockStatic(RealmContactTransactions.class);
        PowerMockito.when(RealmContactTransactions.getAllContacts(Mockito.any(Realm.class), Mockito.anyString()))
                .thenReturn(MockDataForTests.getMockContactsList());
        Intent intent = new Intent();
        intent.putExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY, GroupChatListActivity.class.getSimpleName());
        this.mActivity = Robolectric.buildActivity(GroupChatListActivity.class).withIntent(intent)
                .create().start().resume().stop().destroy().get();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(MycommsApp.stateCounter == 0);
        Assert.assertTrue(this.mActivity.isDestroyed());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    private void setUpActivity_PreviousGroupChatListActivity()
    {
        SharedPreferences sp = mContext.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(Constants.PROFILE_ID_SHARED_PREF, "mc_5570340e7eb7c3512f2f9bf2").apply();

        PowerMockito.mockStatic(RealmContactTransactions.class);
        PowerMockito.when(RealmContactTransactions.getAllContacts(Mockito.any(Realm.class), Mockito.anyString()))
                .thenReturn(MockDataForTests.getMockContactsList());
        Intent intent = new Intent();
        intent.putExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY, GroupChatListActivity.class.getSimpleName());
        this.mActivity = Robolectric.buildActivity(GroupChatListActivity.class).withIntent(intent)
                .create().start().resume().visible().get();
        this.mGroupChatListFragment = (GroupChatListFragment)this.mActivity.getSupportFragmentManager().getFragments().get(0);
    }

    private void setUpActivity_PreviousChatListActivity()
    {
        SharedPreferences sp = mContext.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(Constants.PROFILE_ID_SHARED_PREF, "mc_5570340e7eb7c3512f2f9bf2").apply();

        PowerMockito.mockStatic(RealmContactTransactions.class);
        PowerMockito.mockStatic(RealmGroupChatTransactions.class);
        PowerMockito.when(RealmContactTransactions.getAllContacts(Mockito.any(Realm.class), Mockito.anyString()))
                .thenReturn(MockDataForTests.getMockContactsList());
        PowerMockito.when(RealmGroupChatTransactions.getGroupChatById(Mockito.anyString(), Mockito.any(Realm.class)))
                .thenReturn(MockDataForTests.getMockGroupChat());
        PowerMockito.when(RealmContactTransactions.getContactById(Mockito.anyString(), Mockito.any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContact());
        Intent intent = new Intent();
        intent.putExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY, ChatListActivity.class.getSimpleName());
        intent.putExtra(Constants.GROUP_CHAT_ID, "mg_55dc2a35a297b90a726e4cc2");
        this.mActivity = Robolectric.buildActivity(GroupChatListActivity.class).withIntent(intent)
                .create().start().resume().visible().get();
        this.mGroupChatListFragment = (GroupChatListFragment)this.mActivity.getSupportFragmentManager().getFragments().get(0);
    }

    private void preparePicasso()
    {
        Downloader downloader = new OkHttpDownloader(mContext, Long.MAX_VALUE);
        Picasso.Builder builder = new Picasso.Builder(mContext);
        builder.downloader(downloader);
        MycommsApp.picasso = builder.build();
    }
}
