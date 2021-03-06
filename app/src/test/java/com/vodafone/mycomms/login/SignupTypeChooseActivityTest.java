package com.vodafone.mycomms.login;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.widget.Button;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.MockRepository;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 18)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*"})
@PrepareForTest({Crashlytics.class})
public class SignupTypeChooseActivityTest {

    Activity activity;
    Button mSignupEmail;
    Button mSignupSalesforce;
    ImageView mBack;

    @Before
    public void setUp() throws Exception
    {
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());

        activity = Robolectric.buildActivity(SignupTypeChooseActivity.class).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();
        mSignupEmail = (Button)activity.findViewById(R.id.btSignupMail);
        mSignupSalesforce = (Button)activity.findViewById(R.id.btSignupSalesforce);
        mBack = (ImageView)activity.findViewById(R.id.btBack);
    }

    @After
    public void tearDown() throws Exception
    {
        MockDataForTests.checkThreadSchedulers();
        Robolectric.reset();

        activity = null;
        mSignupEmail = null;
        mSignupSalesforce = null;
        mBack = null;
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
                System.err.println("Uncaught exception at SignupTypeChooseActivityTest: \n" + writer.toString());
            }
        });
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        Thread.currentThread().interrupt();
    }

    @Test
    public void testSignupTypeChooseToSignupMail() throws Exception {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        mSignupEmail.performClick();
        MockDataForTests.checkThreadSchedulers();

        Intent expectedIntent = new Intent(activity, SignupMailActivity.class);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testSignupTypeChooseToSignupOAuth() throws Exception {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        mSignupSalesforce.performClick();
        MockDataForTests.checkThreadSchedulers();

        Intent expectedIntent = new Intent(activity, OAuthActivity.class);
        expectedIntent.putExtra("oauth", "sf");
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testBack() throws Exception {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        mBack.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(activity.isFinishing());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testFinish() throws Exception {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        Activity activity = Robolectric.buildActivity(SignupTypeChooseActivity.class).create().start().resume().pause().stop().destroy().get();
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(activity.isDestroyed());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

}