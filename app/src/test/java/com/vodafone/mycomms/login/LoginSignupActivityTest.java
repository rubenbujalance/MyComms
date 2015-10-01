package com.vodafone.mycomms.login;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.widget.Button;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.OKHttpErrorReceivedEvent;
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
import org.robolectric.shadows.ShadowToast;

import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*"})
@PrepareForTest({Crashlytics.class})
public class LoginSignupActivityTest {

    Activity activity;
    Button btSignup;
    Button btLogin;

    @Before
    public void setUp()
    {
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
        activity = Robolectric.buildActivity(LoginSignupActivity.class).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();
        btSignup = (Button)activity.findViewById(R.id.btSignup);
        btLogin = (Button)activity.findViewById(R.id.btLogin);
    }

    @After
    public void tearDown() throws Exception
    {
        MockDataForTests.checkThreadSchedulers();
        Robolectric.reset();
        activity = null;
        btSignup = null;
        btLogin = null;
        System.gc();
    }

    @BeforeClass
    public static void setUpBeforeClass()
    {
        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException (Thread thread, Throwable e)
            {
                e.printStackTrace();
            }
        });
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        Thread.currentThread().interrupt();
    }

    @Test
    public void testLoginSignupToSignupTypeChooseActivity() {
        btSignup.performClick();
        MockDataForTests.checkThreadSchedulers();
        Intent expectedIntent = new Intent(activity, SignupTypeChooseActivity.class);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }

    @Test
    public void testLoginSignupToLogin() {
        btLogin.performClick();
        MockDataForTests.checkThreadSchedulers();
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(LoginActivity.class.getName()));
    }

    @Test
    public void testErrorReceivedBusEvent() {
        OKHttpErrorReceivedEvent errorEvent = new OKHttpErrorReceivedEvent();
        String errorMessage = activity.getString(R.string.wrapper_connection_error);
        errorEvent.setErrorMessage(errorMessage);
        BusProvider.getInstance().post(errorEvent);
        MockDataForTests.checkThreadSchedulers();
        String toast = ShadowToast.getTextOfLatestToast();
        Assert.assertTrue(toast.equals(errorMessage));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testFinish() throws Exception {
        Activity activity = Robolectric.buildActivity(LoginSignupActivity.class).create().start().resume().pause().stop().destroy().get();
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(activity.isDestroyed());
    }

}