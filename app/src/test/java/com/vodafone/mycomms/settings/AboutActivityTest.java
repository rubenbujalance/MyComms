package com.vodafone.mycomms.settings;

import android.annotation.TargetApi;
import android.os.Build;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.test.util.MockDataForTests;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.PrintWriter;
import java.io.StringWriter;

import io.realm.Realm;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by str_oan on 21/09/2015.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 18,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*"})
@PrepareForTest({Realm.class, Crashlytics.class})
public class AboutActivityTest
{
    @Rule
    public PowerMockRule rule = new PowerMockRule();
    public AboutActivity mActivity;

    @Before
    public void setUp() throws Exception {
        mockStatic(Realm.class);
        when(Realm.getDefaultInstance()).thenReturn(null);
        mockStatic(Crashlytics.class);
        MycommsApp.stateCounter = 0;
    }

    @After
    public void tearDown() throws Exception
    {
        MockDataForTests.checkThreadSchedulers();
        Robolectric.reset();
        mActivity = null;
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
                System.err.println("Uncaught exception at "+this.getClass().getSimpleName()+": \n" + writer.toString());
            }
        });
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        Thread.currentThread().interrupt();
    }

    @Test
    public void testStarted()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        mActivity = Robolectric.buildActivity(AboutActivity.class).create().start().get();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(MycommsApp.stateCounter != 0);
        MycommsApp.activityStopped();

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testStopped()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        mActivity = Robolectric.buildActivity(AboutActivity.class).create().start().resume().stop().get();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(MycommsApp.stateCounter == 0);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testDestroyed()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        mActivity = Robolectric.buildActivity(AboutActivity.class)
                .create().start().resume().stop().destroy().get();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(mActivity.isDestroyed());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testBackPressed()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        mActivity = Robolectric.buildActivity(AboutActivity.class).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();

        mActivity.onBackPressed();
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(mActivity.isFinishing());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

    }

    @Test
    public void testCorrectlyCreated()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        mActivity = Robolectric.buildActivity(AboutActivity.class).create().start().resume().visible().get();
        MockDataForTests.checkThreadSchedulers();

        TextView textVersion = (TextView) mActivity.findViewById(R.id.text_version);
        TextView textBuild = (TextView) mActivity.findViewById(R.id.text_build);
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);

        Assert.assertTrue(textVersion.getText().toString()
                .compareTo(mActivity.getResources().getString(R.string.about_version) + BuildConfig.VERSION_NAME)==0);
        Assert.assertTrue(textBuild.getText().toString()
                .compareTo(mActivity.getResources().getString(R.string.about_build) + versionCode)==0);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

}
