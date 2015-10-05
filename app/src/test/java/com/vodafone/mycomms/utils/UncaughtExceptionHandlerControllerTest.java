package com.vodafone.mycomms.utils;

import android.app.Activity;
import android.content.Intent;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.main.SplashScreenActivity;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.CustomSimpleActivity;
import com.vodafone.mycomms.util.UncaughtExceptionHandlerController;

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
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by str_oan on 26/08/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 18,
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
        MockDataForTests.checkThreadSchedulers();
        Robolectric.reset();

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

    private CustomSimpleActivity createCustomDashboardActivity()
    {
        Intent in = new Intent(RuntimeEnvironment.application.getApplicationContext(),
                CustomSimpleActivity.class);
        in.putExtra("index", 2);
        mActivity = Robolectric.buildActivity(CustomSimpleActivity.class)
                .withIntent(in)
                .create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();

        return mActivity;
    }

    @Test
    public void testGetStringFromThrowableOK()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        String exceptionMessage1 = this.mUncaughtExceptionHandlerController.getStringFromThrowable(mThrowable);
        String exceptionMessage2 = this.mUncaughtExceptionHandlerControllerForEmptyConstructor.getStringFromThrowable(mThrowable);
        Assert.assertNotNull(exceptionMessage1);
        Assert.assertNotNull(exceptionMessage2);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testGetStringFromThrowableWithException()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        String exceptionMessage1 = this.mUncaughtExceptionHandlerController.getStringFromThrowable(mThrowableAsNull);
        String exceptionMessage2 = this.mUncaughtExceptionHandlerControllerForEmptyConstructor.getStringFromThrowable(mThrowableAsNull);
        Assert.assertNull(exceptionMessage1);
        Assert.assertNull(exceptionMessage2);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testStartRecoverIntentOK()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        String errorMessage = "mockErrorMessage";
        mUncaughtExceptionHandlerController.startRecoverIntent(errorMessage);
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (mClass.getName()));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test (expected = Exception.class)
    public void testStartRecoverIntentKO()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        String errorMessage = "mockErrorMessage";
        mUncaughtExceptionHandlerControllerForEmptyConstructor.startRecoverIntent(errorMessage);
        MockDataForTests.checkThreadSchedulers();

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testUncaughtException_ConstructorWithoutParams() throws InterruptedException
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

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

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testUncaughtException_ConstructorWithParams() throws InterruptedException
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

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

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testUncaughtException_ConstructorWithParamsNullMessage() throws InterruptedException
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

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

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

}
