package com.vodafone.mycomms.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;

import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chatlist.view.ChatListActivity;
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

    @Before
    public void setUp()
    {
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
        //Mock Realm
        PowerMockito.mockStatic(Realm.class);

        //Mock Chat and GroupChat Transactions
        RealmChatTransactions mockChatTx = PowerMockito.mock(RealmChatTransactions.class);
        RealmGroupChatTransactions mockGroupChatTx = PowerMockito.mock(RealmGroupChatTransactions.class);
        PowerMockito.when(mockGroupChatTx.getAllGroupChats(Mockito.any(Realm.class)))
                .thenReturn(MockDataForTests.getMockGroupChatList());
        PowerMockito.when(mockChatTx.getAllChatsFromExistingContacts(Mockito.any(Realm.class)))
                .thenReturn(MockDataForTests.getMockChatList());

        PowerMockito.mockStatic(RealmGroupChatTransactions.class);
        PowerMockito.when(RealmGroupChatTransactions.getInstance(Mockito.any(Context.class), Mockito.any(String.class)))
                .thenReturn(mockGroupChatTx);
        PowerMockito.mockStatic(RealmChatTransactions.class);
        PowerMockito.when(RealmChatTransactions.getInstance(Mockito.any(Context.class)))
                .thenReturn(mockChatTx);

        activity = Robolectric.setupActivity(ChatListActivity.class);
    }

    @After
    public void tearDown() throws Exception
    {
        //Try to shutdown server if it was started
        try {
            Robolectric.reset();
        } catch (Exception e) {}

        activity = null;

        System.gc();
    }

    @Test
    public void testCreateGroupClicked()
    {
        ImageView btCreateGroup = (ImageView)activity.findViewById(R.id.chat_add);
        btCreateGroup.performClick();

        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().compareTo(ChatListActivity.class.getName())!=0);
    }

//    @Test
//    public void testConnectivityChanged()
//    {
//        LinearLayout lay_no_connection = (LinearLayout)activity.findViewById(R.id.no_connection_layout);
//        ConnectivityChanged event = new ConnectivityChanged();
//        BusProvider.getInstance().post();
//
//        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
//        Intent startedIntent = shadowActivity.getNextStartedActivity();
//        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
//        Assert.assertTrue(shadowIntent.getComponent().getClassName().compareTo(ChatListActivity.class.getName())!=0);
//    }
}
