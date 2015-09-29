package com.vodafone.mycomms.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chatlist.view.ChatListActivity;
import com.vodafone.mycomms.chatlist.view.ChatListHolder;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ChatsReceivedEvent;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;

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
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowViewGroup;

import io.realm.Realm;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*"})
@PrepareForTest({Realm.class, RealmChatTransactions.class, RealmGroupChatTransactions.class})
public class ChatListActivityTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    Activity activity;
    RealmChatTransactions mockChatTx;
    RealmGroupChatTransactions mockGroupChatTx;
    RecyclerView recyclerView;

    @Before
    public void setUp()
    {
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
        //Mock Realm
        PowerMockito.mockStatic(Realm.class);

        //Mock Chat and GroupChat Transactions
        mockChatTx = PowerMockito.mock(RealmChatTransactions.class);
        mockGroupChatTx = PowerMockito.mock(RealmGroupChatTransactions.class);
        PowerMockito.when(mockGroupChatTx.getAllGroupChats(Mockito.any(Realm.class)))
                .thenReturn(MockDataForTests.getEmptyGroupChatList());
        PowerMockito.when(mockChatTx.getAllChatsFromExistingContacts(Mockito.any(Realm.class)))
                .thenReturn(MockDataForTests.getEmptyChatList());

        PowerMockito.mockStatic(RealmGroupChatTransactions.class);
        PowerMockito.when(RealmGroupChatTransactions.getInstance(Mockito.any(Context.class), Mockito.any(String.class)))
                .thenReturn(mockGroupChatTx);
        PowerMockito.mockStatic(RealmChatTransactions.class);
        PowerMockito.when(RealmChatTransactions.getInstance(Mockito.any(Context.class)))
                .thenReturn(mockChatTx);

        activity = Robolectric.setupActivity(ChatListActivity.class);
        recyclerView = (RecyclerView)activity.findViewById(R.id.recycler_view);
        recyclerView.measure(0, 0);
        recyclerView.layout(0, 0, 100, 10000);
    }

    @After
    public void tearDown() throws Exception
    {
        //Try to shutdown server if it was started
        try {
            Robolectric.reset();
        } catch (Exception e) {}

        activity = null;
        mockChatTx = null;
        mockGroupChatTx = null;
        recyclerView = null;
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
    public void testLoadSomeChats()
    {
        PowerMockito.when(mockGroupChatTx.getAllGroupChats(Mockito.any(Realm.class)))
                .thenReturn(MockDataForTests.getMockGroupChatList());
        PowerMockito.when(mockChatTx.getAllChatsFromExistingContacts(Mockito.any(Realm.class)))
                .thenReturn(MockDataForTests.getMockChatList());

        //Check number of chats
        BusProvider.getInstance().post(new ChatsReceivedEvent());
        Assert.assertTrue(recyclerView.getAdapter().getItemCount() == 8);

        //Check order
        ChatListHolder holder;
        holder = (ChatListHolder)recyclerView.findViewHolderForPosition(1);
        Assert.assertTrue(holder.textViewMessage.toString().compareTo("groupchat_message_1")==0);
        holder = (ChatListHolder)recyclerView.findViewHolderForPosition(2);
        Assert.assertTrue(holder.textViewMessage.toString().compareTo("chat_message_1")==0);
        holder = (ChatListHolder)recyclerView.findViewHolderForPosition(3);
        Assert.assertTrue(holder.textViewMessage.toString().compareTo("groupchat_message_2")==0);
        holder = (ChatListHolder)recyclerView.findViewHolderForPosition(4);
        Assert.assertTrue(holder.textViewMessage.toString().compareTo("chat_message_2")==0);
        holder = (ChatListHolder)recyclerView.findViewHolderForPosition(5);
        Assert.assertTrue(holder.textViewMessage.toString().compareTo("groupchat_message_3")==0);
        holder = (ChatListHolder)recyclerView.findViewHolderForPosition(6);
        Assert.assertTrue(holder.textViewMessage.toString().compareTo("chat_message_3")==0);
        holder = (ChatListHolder)recyclerView.findViewHolderForPosition(7);
        Assert.assertTrue(holder.textViewMessage.toString().compareTo("groupchat_message_4")==0);
        holder = (ChatListHolder)recyclerView.findViewHolderForPosition(8);
        Assert.assertTrue(holder.textViewMessage.toString().compareTo("chat_message_4")==0);

        //Check data of 1st and 2nd position
        ChatListHolder firstPos = (ChatListHolder)recyclerView.findViewHolderForPosition(1);
        Assert.assertTrue(firstPos.top_left_avatar.getVisibility()==View.VISIBLE);
        Assert.assertTrue(firstPos.top_right_avatar.getVisibility()==View.VISIBLE);
        Assert.assertTrue(firstPos.bottom_left_avatar.getVisibility()==View.VISIBLE);
        Assert.assertTrue(firstPos.bottom_right_avatar.getVisibility()==View.GONE);
        Assert.assertTrue(firstPos.top_left_chat_availability.getVisibility()==View.VISIBLE);
        Assert.assertTrue(firstPos.top_right_chat_availability.getVisibility()==View.VISIBLE);
        Assert.assertTrue(firstPos.bottom_left_chat_availability.getVisibility()==View.VISIBLE);
        Assert.assertTrue(firstPos.bottom_right_chat_availability.getVisibility()==View.GONE);

        //Check scroll
        ShadowViewGroup shadowRecycler = Shadows.shadowOf(recyclerView);
        shadowRecycler.scrollTo(5000, 0);
        recyclerView.scrollToPosition(7);

        //Check click
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
}
