package com.vodafone.mycomms.chat;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.crashlytics.android.Crashlytics;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chatgroup.FullscreenImageActivity;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;

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
import org.robolectric.annotation.Config;

import java.io.PrintWriter;
import java.io.StringWriter;

import io.realm.Realm;

/**
 * Created by str_oan on 30/09/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 18)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.davemorrissey.labs.subscaleview.*"})
@PrepareForTest(
        {
                Realm.class
                , Crashlytics.class
        })
public class FullScreenImageActivityTest
{
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    FullscreenImageActivity activity;

    @Before
    public void setUp()
    {
        PowerMockito.mockStatic(Realm.class);
        PowerMockito.when(Realm.getInstance(Mockito.any(Context.class))).thenReturn(null);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
    }

    @After
    public void tearDown() throws Exception
    {
        MockDataForTests.checkThreadSchedulers();
        Robolectric.reset();
        activity = null;
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
    public void testCreationAndClick()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        SubsamplingScaleImageView ivImage = (SubsamplingScaleImageView)this.activity.findViewById(R.id.image);
        ivImage.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(this.activity.isFinishing());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testLifeCycle()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        MycommsApp.stateCounter = 0;
        Intent intent = new Intent();
        intent.putExtra("imageFilePath", "mockPath");
        this.activity = Robolectric.buildActivity(FullscreenImageActivity.class).withIntent(intent)
                .create().start().resume().stop().destroy().get();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(MycommsApp.stateCounter == 0);
        Assert.assertTrue(this.activity.isDestroyed());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    private void setUpActivity()
    {
        Intent intent = new Intent();
        intent.putExtra("imageFilePath", "mockPath");
        this.activity = Robolectric.buildActivity(FullscreenImageActivity.class).withIntent(intent)
                .create().start().resume().visible().get();

    }
}
