package com.vodafone.mycomms.login;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.constants.Constants;
import com.vodafone.mycomms.main.SplashScreenActivity;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;

import org.apache.http.HttpResponse;
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
import org.robolectric.shadows.ShadowInputMethodManager;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.httpclient.FakeHttp;

import static com.vodafone.mycomms.constants.Constants.CHECK_PHONE_OK_RESPONSE;
import static com.vodafone.mycomms.constants.Constants.LOGIN_OK_RESPONSE;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*"})
@PrepareForTest({Crashlytics.class})
public class SignupPincodeActivityTest {

    SignupPincodeActivity activity;
    EditText etPin;
    TextView tvPinPhoneNumber;
    Button btResendPin;
    ImageView ivBtFwd;
    ImageView ivBtBack;
    TextView tvPin1;
    TextView tvPin2;
    TextView tvPin3;
    TextView tvPin4;

    View lnPin1;
    View lnPin2;
    View lnPin3;
    View lnPin4;

    @Before
    public void setUp() throws Exception
    {
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());

        activity = Robolectric.buildActivity(SignupPincodeActivity.class).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();
        Shadows.shadowOf(activity).getNextStartedActivity();
        ivBtFwd = (ImageView)activity.findViewById(R.id.ivBtForward);
        ivBtBack = (ImageView)activity.findViewById(R.id.ivBtBack);
        etPin = activity.etPin;
        tvPinPhoneNumber = activity.tvPinPhoneNumber;
        btResendPin = activity.btResendPin;
        lnPin1 = activity.lnPin1;
        lnPin2 = activity.lnPin2;
        lnPin3 = activity.lnPin3;
        lnPin4 = activity.lnPin4;

        tvPin1 = activity.tvPin1;
        tvPin2 = activity.tvPin2;
        tvPin3 = activity.tvPin3;
        tvPin4 = activity.tvPin4;
    }

    @After
    public void tearDown() throws Exception
    {
        MockDataForTests.checkThreadSchedulers();
        Robolectric.reset();

        activity = null;
        etPin = null;
        tvPinPhoneNumber = null;
        btResendPin = null;
        ivBtFwd = null;
        ivBtBack = null;
        tvPin1 = null;
        tvPin2 = null;
        tvPin3 = null;
        tvPin4 = null;
        lnPin1 = null;
        lnPin2 = null;
        lnPin3 = null;
        lnPin4 = null;
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
    public void testSendPincode() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(200, CHECK_PHONE_OK_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        MockDataForTests.checkThreadSchedulers();

        KeyEvent keyEvent = new KeyEvent(0,0,KeyEvent.ACTION_UP, KeyEvent.KEYCODE_1, 0, 0);
        etPin.dispatchKeyEvent(keyEvent);
        etPin.dispatchKeyEvent(keyEvent);
        etPin.dispatchKeyEvent(keyEvent);
        etPin.dispatchKeyEvent(keyEvent);
        MockDataForTests.checkThreadSchedulers();

        Intent expectedIntent = new Intent(activity, MailSentActivity.class);
        expectedIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        expectedIntent.putExtra("pin", activity.pin);
        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (MailSentActivity.class.getName()));
    }

    @Test
    public void testSendPincodeOAuth() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(201, LOGIN_OK_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        MockDataForTests.checkThreadSchedulers();

        KeyEvent keyEvent = new KeyEvent(0,0,KeyEvent.ACTION_UP, KeyEvent.KEYCODE_1, 0, 0);
        etPin.dispatchKeyEvent(keyEvent);
        etPin.dispatchKeyEvent(keyEvent);
        etPin.dispatchKeyEvent(keyEvent);
        etPin.dispatchKeyEvent(keyEvent);
        MockDataForTests.checkThreadSchedulers();

        Intent expectedIntent = new Intent(activity, SplashScreenActivity.class);
        expectedIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (SplashScreenActivity.class.getName()));
    }

    @Test
    public void testSendPinCodeInvalidResponseCode() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(400);
        FakeHttp.addPendingHttpResponse(httpResponse);
        MockDataForTests.checkThreadSchedulers();

        KeyEvent keyEvent = new KeyEvent(0,0,KeyEvent.ACTION_UP, KeyEvent.KEYCODE_A, 0, 0);
        etPin.dispatchKeyEvent(keyEvent);
        etPin.dispatchKeyEvent(keyEvent);
        etPin.dispatchKeyEvent(keyEvent);
        etPin.dispatchKeyEvent(keyEvent);
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(lnPin1.getBackground().equals(activity.getResources().getDrawable(android.R.color.holo_red_dark)));
    }

    @Test
    public void testSendPinCodeText() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(200, Constants.USER_PHONE_NOT_VERIFIED_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        MockDataForTests.checkThreadSchedulers();

        etPin.setText("1");
        Assert.assertTrue(activity.nextPinPos == 2);
        Assert.assertTrue(tvPin1.getText().equals("1"));

        etPin.setText("12");
        Assert.assertTrue(activity.nextPinPos == 3);
        Assert.assertTrue(tvPin1.getText().equals("1"));
        Assert.assertTrue(tvPin2.getText().equals("2"));

        etPin.setText("123");
        Assert.assertTrue(activity.nextPinPos == 4);
        Assert.assertTrue(tvPin1.getText().equals("1"));
        Assert.assertTrue(tvPin2.getText().equals("2"));
        Assert.assertTrue(tvPin3.getText().equals("3"));

        etPin.setText("1234");
        Assert.assertTrue(activity.nextPinPos == 1);
        Assert.assertTrue(tvPin1.getText().equals("1"));
        Assert.assertTrue(tvPin2.getText().equals("2"));
        Assert.assertTrue(tvPin3.getText().equals("3"));
        Assert.assertTrue(tvPin4.getText().equals("4"));

        //Test of Reset after writing 4 pin code
        tvPin1.setText("1");
        tvPin2.setText("2");
        etPin.setText("9");
        Assert.assertTrue(tvPin1.getCurrentTextColor() == Color.WHITE);
        Assert.assertTrue(lnPin1.getBackground().equals(activity.getResources().getDrawable(android.R.color.white)));
        Assert.assertTrue(activity.nextPinPos == 2);
        Assert.assertTrue(tvPin1.getText().equals("9"));
        Assert.assertTrue(tvPin2.getText().equals("  "));
        Assert.assertTrue(tvPin3.getText().equals("  "));
        Assert.assertTrue(tvPin4.getText().equals("  "));
    }

    @Test
    public void testResendPin() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(200, Constants.USER_PHONE_NOT_VERIFIED_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        MockDataForTests.checkThreadSchedulers();

        btResendPin.performClick();
        MockDataForTests.checkThreadSchedulers();

        Intent expectedIntent = new Intent(activity, MailSentActivity.class);
        expectedIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        expectedIntent.putExtra("pin", activity.pin);
        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (MailSentActivity.class.getName()));
    }

    @Test
    public void testShowKeyboard() throws Exception {
        InputMethodManager inputManager = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        ShadowInputMethodManager shadowInputMethodManager = Shadows.shadowOf(inputManager);

        tvPin1.performClick();
        Thread.sleep(500);
        Robolectric.flushForegroundThreadScheduler();
        Assert.assertTrue(shadowInputMethodManager.isSoftInputVisible());
        lnPin1.performClick();
        Thread.sleep(500);
        Robolectric.flushForegroundThreadScheduler();
        Assert.assertTrue(shadowInputMethodManager.isSoftInputVisible());

        tvPin2.performClick();
        Thread.sleep(500);
        Robolectric.flushForegroundThreadScheduler();
        Assert.assertTrue(shadowInputMethodManager.isSoftInputVisible());
        lnPin2.performClick();
        Thread.sleep(500);
        Robolectric.flushForegroundThreadScheduler();
        Assert.assertTrue(shadowInputMethodManager.isSoftInputVisible());

        tvPin3.performClick();
        Thread.sleep(500);
        Robolectric.flushForegroundThreadScheduler();
        Assert.assertTrue(shadowInputMethodManager.isSoftInputVisible());
        lnPin3.performClick();
        Thread.sleep(500);
        Robolectric.flushForegroundThreadScheduler();
        Assert.assertTrue(shadowInputMethodManager.isSoftInputVisible());

        tvPin4.performClick();
        Thread.sleep(500);
        Robolectric.flushForegroundThreadScheduler();
        Assert.assertTrue(shadowInputMethodManager.isSoftInputVisible());
        lnPin4.performClick();
        Thread.sleep(500);
        Robolectric.flushForegroundThreadScheduler();
        Assert.assertTrue(shadowInputMethodManager.isSoftInputVisible());
    }

    @Test
    public void testSendPincodeOK() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(201, LOGIN_OK_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        MockDataForTests.checkThreadSchedulers();

        etPin.setText("1");
        etPin.setText("12");
        etPin.setText("123");
        etPin.setText("1234");
        Intent expectedIntent = new Intent(activity, SplashScreenActivity.class);
        expectedIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (SplashScreenActivity.class.getName()));
    }

    @Test
    public void testBack() throws Exception {
        ImageView ivBtBack = (ImageView)activity.findViewById(R.id.ivBtBack);
        ivBtBack.performClick();
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(activity.isFinishing());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testFinish() throws Exception {
        Activity activity = Robolectric.buildActivity(SignupPincodeActivity.class).create().start().resume().pause().stop().destroy().get();
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(activity.isDestroyed());
    }

}