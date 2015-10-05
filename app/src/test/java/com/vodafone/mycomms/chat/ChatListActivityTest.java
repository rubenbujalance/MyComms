package com.vodafone.mycomms.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.crashlytics.android.Crashlytics;
import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chatlist.view.ChatListActivity;
import com.vodafone.mycomms.chatlist.view.ChatListFragment;
import com.vodafone.mycomms.chatlist.view.ChatListHolder;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ChatsReceivedEvent;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.util.PacketParserUtils;
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

import java.util.ArrayList;

import io.realm.Realm;
import model.Chat;
import model.ChatMessage;
import model.GroupChat;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "org.kxml2.io.*", "org.xmlpull.*"})
@PrepareForTest(
        {
                Realm.class
                , RealmChatTransactions.class
                , RealmGroupChatTransactions.class
                , RealmContactTransactions.class
                , Crashlytics.class
                , BusProvider.class
        })
public class ChatListActivityTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    ChatListActivity activity;
    RealmChatTransactions mockChatTx;
    RealmGroupChatTransactions mockGroupChatTx;
    RecyclerView recyclerView;
    ChatListFragment mChatListFragment;
    Context context;
    SharedPreferences sp;
//    BusProvider.MainThreadBus busProvider;

    @Before
    public void setUp()
    {
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());

        //Mock static: Realm, RealmGroupChatTransactions, RealmChatTransactions, RealmContactTransactions
        PowerMockito.mockStatic(Realm.class);
        PowerMockito.mockStatic(RealmGroupChatTransactions.class);
        PowerMockito.mockStatic(RealmChatTransactions.class);
        PowerMockito.mockStatic(RealmContactTransactions.class);
        PowerMockito.mockStatic(Crashlytics.class);

        PowerMockito.when(Realm.getInstance(Mockito.any(Context.class))).thenReturn(null);

        //Save profile into SharedPreferences
        context = RuntimeEnvironment.application.getApplicationContext();
        sp = context.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(Constants.PROFILE_ID_SHARED_PREF, "mc_55409316799f7e1a109446f4").commit();

        //Spy instances: RealmChatTransactions, RealmGroupChatTransactions
        RealmChatTransactions chatTx = new RealmChatTransactions(context);
        RealmGroupChatTransactions groupChatTx = new RealmGroupChatTransactions(context, "mc_55409316799f7e1a109446f4");
        mockChatTx = Mockito.spy(chatTx);
        mockGroupChatTx = Mockito.spy(groupChatTx);

        Mockito.doReturn(MockDataForTests.getEmptyChatList())
                .when(mockChatTx).getAllChatsFromExistingContacts(Mockito.any(Realm.class));
        Mockito.doReturn(MockDataForTests.getEmptyGroupChatList())
                .when(mockGroupChatTx).getAllGroupChats(Mockito.any(Realm.class));
        Mockito.doReturn(Long.valueOf(0)).when(mockChatTx)
                .getAllChatPendingMessagesCount(Mockito.any(Realm.class));

        PowerMockito.when(RealmChatTransactions.getChatPendingMessagesCount(Mockito.any(String.class), Mockito.any(Realm.class)))
                .thenReturn(Long.valueOf(0));

        //Set mocked instances when newInstance is called
        PowerMockito.when(RealmChatTransactions.getInstance(Mockito.any(Context.class)))
                .thenReturn(mockChatTx);
        PowerMockito.when(RealmGroupChatTransactions.getInstance(Mockito.any(Context.class), Mockito.any(String.class)))
                .thenReturn(mockGroupChatTx);

        //MyCommsApp configuration
        MycommsApp.stateCounter = 0;

        //Picasso configuration
        context = RuntimeEnvironment.application.getApplicationContext();
        Downloader downloader = new OkHttpDownloader(context, Long.MAX_VALUE);
        Picasso.Builder builder = new Picasso.Builder(context);
        builder.downloader(downloader);
        MycommsApp.picasso = builder.build();

        //Activity initialization
        activity = Robolectric.buildActivity(ChatListActivity.class).create().start().resume().visible().get();
        mChatListFragment = (ChatListFragment)activity.getSupportFragmentManager().getFragments().get(0);
        recyclerView = (RecyclerView)mChatListFragment.getView().findViewById(R.id.recycler_view);

        MockDataForTests.checkThreadSchedulers();
    }

    @After
    public void tearDown() throws Exception
    {
        Robolectric.reset();
        activity = null;
        mockChatTx = null;
        mockGroupChatTx = null;
        recyclerView = null;
//        busProvider = null;
        System.gc();
    }

    @Test
    public void testCreateActivity()
    {
        FrameLayout fragmentContent = (FrameLayout)activity.findViewById(R.id.sample_content_fragment);
        Assert.assertNotNull(fragmentContent);

        RelativeLayout footer = (RelativeLayout)activity.findViewById(R.id.footer);
        Assert.assertNotNull(footer);

        Toolbar toolbar = (Toolbar)activity.findViewById(R.id.app_inbox);
        Assert.assertNotNull(toolbar);

        RecyclerView recyclerView = (RecyclerView)activity.findViewById(R.id.recycler_view);
        Assert.assertNotNull(recyclerView);
    }

    @Test
    public void testOnBackPressed()
    {
        this.mChatListFragment.getActivity().onBackPressed();
        Assert.assertFalse(this.mChatListFragment.getActivity().isFinishing());
    }

    @Test
    public void testStopActivity()
    {
        this.mChatListFragment.onStop();
        Assert.assertTrue(MycommsApp.stateCounter != 0);
    }

    @Test
    public void testLoadSomeChats() {
        //Mock Chat and GroupChat Transactions to return filled lists
        Mockito.doReturn(MockDataForTests.getMockChatList())
                .when(mockChatTx).getAllChatsFromExistingContacts(Mockito.any(Realm.class));
        Mockito.doReturn(MockDataForTests.getMockGroupChatList())
                .when(mockGroupChatTx).getAllGroupChats(Mockito.any(Realm.class));

        //Mock to return a Contact and a UserProfile when requested to Realm
        PowerMockito.when(RealmContactTransactions.getContactById(Mockito.anyString(), Mockito.any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContact());
        PowerMockito.when(RealmContactTransactions.getUserProfile(Mockito.any(Realm.class), Mockito.anyString()))
                .thenReturn(MockDataForTests.getMockUserProfile());

        //Draw list
        BusProvider.getInstance().post(new ChatsReceivedEvent());
        recyclerView.measure(0, 0);
        recyclerView.layout(0, 0, 100, 10000);

        MockDataForTests.checkThreadSchedulers();

        //Check number of chats
        Assert.assertTrue(recyclerView.getAdapter().getItemCount() == 8);

        //Check order
        ChatListHolder holder;
        holder = (ChatListHolder)recyclerView.findViewHolderForPosition(0);
        Assert.assertTrue(holder.textViewMessage.getText().toString().compareTo("chat_message_4")==0);
        holder = (ChatListHolder)recyclerView.findViewHolderForPosition(1);
        Assert.assertTrue(holder.textViewMessage.getText().toString().compareTo("groupchat_message_4")==0);
        holder = (ChatListHolder)recyclerView.findViewHolderForPosition(2);
        Assert.assertTrue(holder.textViewMessage.getText().toString().compareTo("chat_message_3")==0);
        holder = (ChatListHolder)recyclerView.findViewHolderForPosition(3);
        Assert.assertTrue(holder.textViewMessage.getText().toString().compareTo("groupchat_message_3")==0);
        holder = (ChatListHolder)recyclerView.findViewHolderForPosition(4);
        Assert.assertTrue(holder.textViewMessage.getText().toString().compareTo("chat_message_2")==0);
        holder = (ChatListHolder)recyclerView.findViewHolderForPosition(5);
        Assert.assertTrue(holder.textViewMessage.getText().toString().compareTo("groupchat_message_2")==0);
        holder = (ChatListHolder)recyclerView.findViewHolderForPosition(6);
        Assert.assertTrue(holder.textViewMessage.getText().toString().compareTo("chat_message_1")==0);
        holder = (ChatListHolder)recyclerView.findViewHolderForPosition(7);
        Assert.assertTrue(holder.textViewMessage.getText().toString().compareTo("groupchat_message_1") == 0);
    }

    @Test
    public void testMessageReceived() {
//        PowerMockito.mockStatic(BusProvider.class);
//        BusProvider.MainThreadBus mockBusProvider = Mockito.mock(BusProvider.MainThreadBus.class);
//        PowerMockito.when(BusProvider.getInstance()).thenReturn(mockBusProvider);

        XMPPTCPConnection xmppConnection = Mockito.mock(XMPPTCPConnection.class);
        XMPPTransactions.setXMPPConnection(xmppConnection);

        ArrayList<Chat> chatList = MockDataForTests.getMockChatList();

        //Mock Chat and GroupChat Transactions to return filled lists
        Mockito.doReturn(chatList)
                .when(mockChatTx).getAllChatsFromExistingContacts(Mockito.any(Realm.class));
        Mockito.doReturn(MockDataForTests.getMockGroupChatList())
                .when(mockGroupChatTx).getAllGroupChats(Mockito.any(Realm.class));

        //Mock to return a Contact and a UserProfile when requested to Realm
        PowerMockito.when(RealmContactTransactions.getContactById(Mockito.anyString(), Mockito.any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContact());
        PowerMockito.when(RealmContactTransactions.getUserProfile(Mockito.any(Realm.class), Mockito.anyString()))
                .thenReturn(MockDataForTests.getMockUserProfile());

//        //Draw list
//        BusProvider.getInstance().post(new ChatsReceivedEvent());
//        recyclerView.measure(0, 0);
//        recyclerView.layout(0, 0, 100, 10000);
//        MockDataForTests.checkThreadSchedulers();

        //The ChatMessage doesn't exist initially
        Mockito.doReturn(null).when(mockChatTx)
                .getChatMessageById(Mockito.any(String.class), Mockito.eq(true), Mockito.any(Realm.class));

        //Same Chat as in list
        Mockito.doReturn(chatList.get(3)).when(mockChatTx)
                .getChatByContactId(Mockito.any(String.class), Mockito.any(Realm.class));

        //Mock insertChat and insertChatMessage
        Mockito.doNothing().when(mockChatTx).insertChat(Mockito.any(Chat.class), Mockito.any(Realm.class));
        Mockito.doReturn(true).when(mockChatTx).insertChatMessage(Mockito.any(ChatMessage.class), Mockito.any(Realm.class));

        //Mock number of unread messages
        Mockito.doReturn(Long.valueOf(1))
                .when(mockChatTx).getAllChatPendingMessagesCount(Mockito.any(Realm.class));
        PowerMockito.when(RealmChatTransactions.getChatPendingMessagesCount(Mockito.any(String.class), Mockito.any(Realm.class)))
                .thenReturn(Long.valueOf(1));

        //Build stanza for chat in position 0
        PacketParserUtils.saveAndNotifyStanzaReceived(MockDataForTests.getMockChatMessageStanza());

        //Refresh list
        recyclerView.measure(0, 0);
        recyclerView.layout(0, 0, 100, 10000);
        MockDataForTests.checkThreadSchedulers();

        //Check order
        ChatListHolder holder;
        holder = (ChatListHolder)recyclerView.findViewHolderForPosition(0);
        Assert.assertTrue(((ChatListHolder)recyclerView.findViewHolderForPosition(0))
                .badgeUnread.getText().toString().compareTo("1")==0);
    }

    @Test
    public void testGroupMessageReceived() {
        XMPPTCPConnection xmppConnection = Mockito.mock(XMPPTCPConnection.class);
        XMPPTransactions.setXMPPConnection(xmppConnection);

        ArrayList<Chat> chatList = MockDataForTests.getMockChatList();
        ArrayList<GroupChat> groupChatList = MockDataForTests.getMockGroupChatList();

        //Mock Chat and GroupChat Transactions to return filled lists
        Mockito.doReturn(chatList)
                .when(mockChatTx).getAllChatsFromExistingContacts(Mockito.any(Realm.class));
        Mockito.doReturn(groupChatList)
                .when(mockGroupChatTx).getAllGroupChats(Mockito.any(Realm.class));

        //Mock to return a Contact and a UserProfile when requested to Realm
        PowerMockito.when(RealmContactTransactions.getContactById(Mockito.anyString(), Mockito.any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContact());
        PowerMockito.when(RealmContactTransactions.getUserProfile(Mockito.any(Realm.class), Mockito.anyString()))
                .thenReturn(MockDataForTests.getMockUserProfile());

        //The ChatMessage doesn't exist initially
        Mockito.doReturn(null).when(mockGroupChatTx)
                .getGroupChatMessageById(Mockito.any(String.class), Mockito.any(Realm.class));

        //Same Chat as in list
        PowerMockito.when(RealmGroupChatTransactions.getGroupChatById(Mockito.any(String.class), Mockito.any(Realm.class)))
                .thenReturn(groupChatList.get(3));

        //Mock insertChat and insertChatMessage
        Mockito.doNothing().when(mockGroupChatTx).insertOrUpdateGroupChat(
                Mockito.any(GroupChat.class), Mockito.any(Realm.class));
        Mockito.doReturn(true).when(mockGroupChatTx).insertGroupChatMessage(
                Mockito.any(String.class), Mockito.any(ChatMessage.class), Mockito.any(Realm.class));

        //Mock number of unread messages
        Mockito.doReturn(Long.valueOf(1))
                .when(mockChatTx).getAllChatPendingMessagesCount(Mockito.any(Realm.class));
        PowerMockito.when(RealmGroupChatTransactions.getGroupChatPendingMessagesCount(
                Mockito.any(String.class), Mockito.any(Realm.class)))
                .thenReturn(Long.valueOf(1));

        //Build stanza for chat in position 0
        PacketParserUtils.saveAndNotifyStanzaReceived(MockDataForTests.getMockGroupChatMessageStanza());

        //Refresh list
        recyclerView.measure(0, 0);
        recyclerView.layout(0, 0, 100, 10000);
        MockDataForTests.checkThreadSchedulers();

        //Check order
        Assert.assertTrue(((ChatListHolder)recyclerView.findViewHolderForPosition(1))
                .badgeUnread.getText().toString().compareTo("1")==0);
    }

    @Test
    public void testCreateGroupChatClicked()
    {
        ImageView btCreateGroup = (ImageView)activity.findViewById(R.id.chat_add);
        btCreateGroup.performClick();

        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().compareTo(ChatListActivity.class.getName())!=0);
    }

    @Test
    public void testConnectivityChanged()
    {
        LinearLayout lay_no_connection = (LinearLayout)activity.findViewById(R.id.no_connection_layout);
        ConnectivityChanged eventDisconnected = new ConnectivityChanged(ConnectivityStatus.OFFLINE);
        ConnectivityChanged eventConnected = new ConnectivityChanged(ConnectivityStatus.MOBILE_CONNECTED);

        //Trying disconnect
        BusProvider.getInstance().post(eventDisconnected);
        Assert.assertTrue(lay_no_connection.getVisibility() == View.VISIBLE);

        //Trying connect
        BusProvider.getInstance().post(eventConnected);
        Assert.assertTrue(lay_no_connection.getVisibility()== View.GONE);
    }

    private void checkThreadSchedulers()
    {
        if(Robolectric.getBackgroundThreadScheduler().areAnyRunnable())
            Robolectric.flushBackgroundThreadScheduler();
        if(Robolectric.getForegroundThreadScheduler().areAnyRunnable())
            Robolectric.flushForegroundThreadScheduler();
    }
}
