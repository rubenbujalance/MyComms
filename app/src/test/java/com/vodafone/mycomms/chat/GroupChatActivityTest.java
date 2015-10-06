//package com.vodafone.mycomms.chat;
//
//import android.content.Context;
//import android.content.Intent;
//import android.view.MotionEvent;
//
//import com.squareup.picasso.Downloader;
//import com.squareup.picasso.OkHttpDownloader;
//import com.squareup.picasso.Picasso;
//import com.vodafone.mycomms.BuildConfig;
//import com.vodafone.mycomms.MycommsApp;
//import com.vodafone.mycomms.chatgroup.GroupChatActivity;
//import com.vodafone.mycomms.chatgroup.GroupDetailActivity;
//import com.vodafone.mycomms.realm.RealmContactTransactions;
//import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
//import com.vodafone.mycomms.test.util.Util;
//import com.vodafone.mycomms.util.Constants;
//
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Rule;
//import org.junit.runner.RunWith;
//import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.MockRepository;
//import org.powermock.core.classloader.annotations.PowerMockIgnore;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.rule.PowerMockRule;
//import org.robolectric.Robolectric;
//import org.robolectric.RobolectricGradleTestRunner;
//import org.robolectric.RuntimeEnvironment;
//import org.robolectric.annotation.Config;
//
//import java.io.PrintWriter;
//import java.io.StringWriter;
//
//import io.realm.Realm;
//
///**
// * Created by str_oan on 05/10/2015.
// */
//@RunWith(RobolectricGradleTestRunner.class)
//@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 18)
//@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
//        "javax.net.ssl.*", "org.json.*"})
//@PrepareForTest(
//        {
//                Realm.class
//                , RealmGroupChatTransactions.class
//                , RealmContactTransactions.class
//        })
//
//public class GroupChatActivityTest
//{
//    @Rule
//    public PowerMockRule rule = new PowerMockRule();
//    public GroupChatActivity activity;
//    public Context mContext;
//    public MotionEvent motionEvent;
//
//    @Before
//    public void setUp()
//    {
//        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
//        PowerMockito.mockStatic(Realm.class);
//        PowerMockito.when(Realm.getInstance(Mockito.any(Context.class))).thenReturn(null);
//        MycommsApp.stateCounter = 0;
//        mContext = RuntimeEnvironment.application.getApplicationContext();
//        preparePicasso();
//    }
//
//    @After
//    public void tearDown() throws Exception
//    {
//        Robolectric.reset();
//        activity = null;
//        mContext = null;
//        System.gc();
//    }
//
//    @BeforeClass
//    public static void setUpBeforeClass()
//    {
//        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//            @Override
//            public void uncaughtException(Thread thread, Throwable e) {
//                StringWriter writer = new StringWriter();
//                PrintWriter printWriter = new PrintWriter(writer);
//                e.printStackTrace(printWriter);
//                printWriter.flush();
//                System.err.println("Uncaught exception at " + this.getClass().getSimpleName() + ": \n" + writer.toString());
//            }
//        });
//    }
//
//    @AfterClass
//    public static void tearDownAfterClass() throws Exception
//    {
//        Thread.currentThread().interrupt();
//    }
//
//    private void preparePicasso()
//    {
//        Downloader downloader = new OkHttpDownloader(RuntimeEnvironment.application.getApplicationContext(), Long.MAX_VALUE);
//        Picasso.Builder builder = new Picasso.Builder(RuntimeEnvironment.application.getApplicationContext());
//        builder.downloader(downloader);
//        MycommsApp.picasso = builder.build();
//    }
//
//    private void setUpActivity()
//    {
//        Intent intent = new Intent();
//        intent.putExtra(Constants.GROUP_CHAT_ID, "mg_55dc2a35a297b90a726e4cc2");
//        intent.putExtra(Constants.IS_GROUP_CHAT, false);
//        intent.putExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY, Constants.GROUP_CHAT_LIST_ACTIVITY);
//        this.activity = Robolectric.buildActivity(GroupChatActivity.class).withIntent(intent)
//                .create().start().resume().visible().get();
//    }
//
//}
