package com.vodafone.mycomms.utils;

import android.app.Activity;
import android.content.Intent;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.main.SplashScreenActivity;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.CustomSimpleActivity;
import com.vodafone.mycomms.util.UncaughtExceptionHandlerController;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.MockRepository;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by str_oan on 26/08/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*"})
@PrepareForTest({Crashlytics.class})
public class UncaughtExceptionHandlerControllerTest
{
    Activity mActivityAsNull;
    CustomSimpleActivity mActivity;
    Class<?> mClassAsNull, mClass;
    UncaughtExceptionHandlerController mUncaughtExceptionHandlerController, mUncaughtExceptionHandlerControllerForEmptyConstructor;
    Throwable mThrowable, mThrowableAsNull;
    Thread.UncaughtExceptionHandler mUncaughtExceptionHandler;

    @Before
    public void setUp() throws Exception
    {
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());

        this.mActivity = createCustomDashboardActivity();
        this.mClass = SplashScreenActivity.class;

        this.mClassAsNull = null;
        this.mActivityAsNull = null;

        this.mUncaughtExceptionHandlerControllerForEmptyConstructor = new UncaughtExceptionHandlerController();
        this.mUncaughtExceptionHandlerController = new UncaughtExceptionHandlerController(mActivity, mClass);

        mUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        this.mThrowable = new Throwable();
        StackTraceElement[] trace = new StackTraceElement[]
                {
                    new StackTraceElement("mockClass", "mockMethod", "mockFileName", 10)
                };
        this.mThrowable.setStackTrace(trace);
        this.mThrowableAsNull = null;
    }

    @After
    public void tearDown() throws Exception
    {
        //Try to shutdown server if it was started
        try {
            Robolectric.reset();
        } catch (Exception e) {}

        mActivityAsNull = null;
        mActivity = null;
        mClassAsNull = null;
        mClass = null;
        mUncaughtExceptionHandlerController = null;
        mUncaughtExceptionHandlerControllerForEmptyConstructor = null;
        mThrowable = null;
        mThrowableAsNull = null;
        mUncaughtExceptionHandler = null;
        System.gc();
    }

    private CustomSimpleActivity createCustomDashboardActivity()
    {
        Intent in = new Intent(RuntimeEnvironment.application.getApplicationContext(),
                CustomSimpleActivity.class);
        in.putExtra("index", 2);
        mActivity = Robolectric.buildActivity(CustomSimpleActivity.class)
                .withIntent(in)
                .create().start().resume().get();
        return mActivity;
    }

    @Test
    public void testGetStringFromThrowableOK()
    {
        String exceptionMessage1 = this.mUncaughtExceptionHandlerController.getStringFromThrowable(mThrowable);
        String exceptionMessage2 = this.mUncaughtExceptionHandlerControllerForEmptyConstructor.getStringFromThrowable(mThrowable);
        Assert.assertNotNull(exceptionMessage1);
        Assert.assertNotNull(exceptionMessage2);
    }

    @Test
    public void testGetStringFromThrowableWithException()
    {
        String exceptionMessage1 = this.mUncaughtExceptionHandlerController.getStringFromThrowable(mThrowableAsNull);
        String exceptionMessage2 = this.mUncaughtExceptionHandlerControllerForEmptyConstructor.getStringFromThrowable(mThrowableAsNull);
        Assert.assertNull(exceptionMessage1);
        Assert.assertNull(exceptionMessage2);
    }

    @Test
    public void testStartRecoverIntentOK()
    {
        String errorMessage = "mockErrorMessage";
        mUncaughtExceptionHandlerController.startRecoverIntent(errorMessage);

        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (mClass.getName()));
        System.err.println("******** Test: Navigation to testStartRecoverIntentOK OK********");
    }

    @Test (expected = Exception.class)
    public void testStartRecoverIntentKO()
    {
        String errorMessage = "mockErrorMessage";
        mUncaughtExceptionHandlerControllerForEmptyConstructor.startRecoverIntent(errorMessage);
        System.err.println("******** Test: Navigation to testStartRecoverIntentOK OK********");
    }

    @Test
    public void testUncaughtException_ConstructorWithoutParams() throws InterruptedException
    {
        Thread testThread = new Thread()
        {
            public void run()
            {
                throw new RuntimeException("Expected!");
            }
            @Override
            public UncaughtExceptionHandler getUncaughtExceptionHandler()
            {
                mUncaughtExceptionHandlerControllerForEmptyConstructor.uncaughtException(this, mThrowable);
                return super.getUncaughtExceptionHandler();
            }
        };
        testThread.setUncaughtExceptionHandler(mUncaughtExceptionHandlerControllerForEmptyConstructor);
        testThread.start();
        testThread.join();
    }

    @Test
    public void testUncaughtException_ConstructorWithParams() throws InterruptedException
    {
        Thread testThread = new Thread()
        {
            public void run()
            {
                throw new RuntimeException("Expected!");
            }
            @Override
            public UncaughtExceptionHandler getUncaughtExceptionHandler()
            {
                mUncaughtExceptionHandlerController.uncaughtException(this, mThrowable);
                Assert.assertTrue(mActivity.isFinishing());
                return super.getUncaughtExceptionHandler();
            }
        };
        testThread.setUncaughtExceptionHandler(mUncaughtExceptionHandlerController);
        testThread.start();
        testThread.join();
    }

    @Test
    public void testUncaughtException_ConstructorWithParamsNullMessage() throws InterruptedException
    {
        Thread testThread = new Thread()
        {
            public void run()
            {
                throw new RuntimeException("Expected!");
            }
            @Override
            public UncaughtExceptionHandler getUncaughtExceptionHandler()
            {
                mUncaughtExceptionHandlerController.uncaughtException(this, mThrowableAsNull);
                Assert.assertTrue(mActivity.isFinishing());
                return super.getUncaughtExceptionHandler();
            }
        };
        testThread.setUncaughtExceptionHandler(mUncaughtExceptionHandlerController);
        testThread.start();
        testThread.join();
    }

}
