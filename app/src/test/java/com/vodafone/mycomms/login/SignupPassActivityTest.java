package com.vodafone.mycomms.login;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.widget.EditText;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.UserProfile;
import com.vodafone.mycomms.custom.ClearableEditText;
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
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.vodafone.mycomms.constants.Constants.ANOTHER_PASSWORD;
import static com.vodafone.mycomms.constants.Constants.PASSWORD;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 18)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*"})
@PrepareForTest({Crashlytics.class})
public class SignupPassActivityTest {

    SignupPassActivity activity;
    ClearableEditText mPassword;
    ClearableEditText mConfirmPass;
    ImageView ivBtFwd;
    ImageView ivBtBack;

    @Before
    public void setUp()
    {
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());

        activity = Robolectric.buildActivity(SignupPassActivity.class).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();
        ivBtFwd = (ImageView)activity.findViewById(R.id.ivBtForward);
        ivBtBack = (ImageView)activity.findViewById(R.id.ivBtBack);
        mPassword = activity.mPassword;
        mConfirmPass = activity.mConfirmPass;
    }

    @After
    public void tearDown() throws Exception
    {
        MockDataForTests.checkThreadSchedulers();
        Robolectric.reset();
        activity = null;
        mPassword = null;
        mConfirmPass = null;
        ivBtFwd = null;
        ivBtBack = null;
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
                System.err.println("Uncaught exception at SignupPassActivityTest: \n" + writer.toString());
            }
        });
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        Thread.currentThread().interrupt();
    }

    @Test
    public void testForwardBadPassword() {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        ivBtFwd.performClick();
        MockDataForTests.checkThreadSchedulers();
        EditText innerPassword = (EditText)mPassword.findViewById(R.id.clearable_edit);
        Assert.assertTrue(innerPassword.getError().equals(activity.getString(R.string.incorrect_format)));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testForwardPasswordAndPasswordConfirmNoMatching() {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        mPassword.setText(PASSWORD);
        mConfirmPass.setText(ANOTHER_PASSWORD);
        ivBtFwd.performClick();
        MockDataForTests.checkThreadSchedulers();
        EditText innerConfirmPass = (EditText)mConfirmPass.findViewById(R.id.clearable_edit);
        Assert.assertTrue(innerConfirmPass.getError().equals(activity.getString(R.string.passwords_do_not_match)));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testForward() {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        mPassword.setText(PASSWORD);
        mConfirmPass.setText(PASSWORD);
        ivBtFwd.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(PASSWORD.equals(UserProfile.getPassword()));
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(SignupPhoneActivity.class.getName()));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testBack() throws Exception {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        ImageView ivBtBack = (ImageView)activity.findViewById(R.id.ivBtBack);
        ivBtBack.performClick();
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

        Activity activity = Robolectric.buildActivity(SignupPassActivity.class).create().start().resume().pause().stop().destroy().get();
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(activity.isDestroyed());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

}