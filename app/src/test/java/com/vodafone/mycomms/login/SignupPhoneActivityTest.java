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
import com.vodafone.mycomms.constants.Constants;
import com.vodafone.mycomms.custom.AutoCompleteTVSelectOnly;
import com.vodafone.mycomms.custom.ClearableEditText;
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
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.shadows.httpclient.FakeHttp;

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.vodafone.mycomms.constants.Constants.PHONE;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*"})
@PrepareForTest({Crashlytics.class})
public class SignupPhoneActivityTest {

    SignupPhoneActivity activity;
    ClearableEditText mPhone;
    AutoCompleteTVSelectOnly mCountry;
    ImageView ivBtFwd;
    ImageView ivBtBack;

    @Before
    public void setUp()
    {
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());

        activity = Robolectric.buildActivity(SignupPhoneActivity.class).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();
        ivBtFwd = (ImageView)activity.findViewById(R.id.ivBtForward);
        ivBtBack = (ImageView)activity.findViewById(R.id.ivBtBack);
        mPhone = activity.mPhone;
        mCountry = activity.mCountry;
    }

    @After
    public void tearDown() throws Exception
    {
        MockDataForTests.checkThreadSchedulers();
        Robolectric.reset();

        activity = null;
        mPhone = null;
        mCountry = null;
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
                System.err.println("Uncaught exception at SignupPhoneActivityTest: \n" + writer.toString());
            }
        });
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        Thread.currentThread().interrupt();
    }

    @Test
    public void testForwardNoCountrySelected() {
        System.err.println("******** Test: Empty Country********");
        mCountry.setText("");
        mCountry.setCodeSelected("");
        ivBtFwd.performClick();
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(mCountry.getError().equals(activity.getString(R.string.select_your_phone_country_to_continue)));
    }

    @Test
    public void testForwardNullCountrySelected() {
        System.err.println("******** Test: Empty Country********");
        mCountry.setText(null);
        mCountry.setCodeSelected(null);
        ivBtFwd.performClick();
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(mCountry.getError().equals(activity.getString(R.string.select_your_phone_country_to_continue)));
    }

    @Test
    public void testForwardNoPhone() {
        String countryName = "United States";
        String countryCode = "US";
        mCountry.setText(countryName);
        mCountry.setCodeSelected(countryCode);
        mPhone.setText("");
        ivBtFwd.performClick();
        MockDataForTests.checkThreadSchedulers();
        EditText innerPhone = (EditText)mPhone.findViewById(R.id.clearable_edit);
        Assert.assertTrue(innerPhone.getError().equals(activity.getString(R.string.enter_your_phone_number_to_continue)));
    }

    @Test
    public void testForwardInvalidResponseCode200() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(200, Constants.USER_PHONE_NOT_VERIFIED_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        MockDataForTests.checkThreadSchedulers();

        String countryName = "United States";
        String countryCode = "US";
        mCountry.setText(countryName);
        mCountry.setCodeSelected(countryCode);
        mPhone.setText(PHONE);
        ivBtFwd.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(PHONE.equals(UserProfile.getPhone()));
        Assert.assertTrue(countryCode.equals(UserProfile.getCountryISO()));
        Intent expectedIntent = new Intent(activity, SignupPincodeActivity.class);
        String dialCode = mCountry.getText().toString().trim();
        dialCode = dialCode.substring(dialCode.lastIndexOf(" "));
        expectedIntent.putExtra("phoneNumber", dialCode + " " + mPhone.getText().toString());
        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (SignupPincodeActivity.class.getName()));
    }

    @Test
    public void testForwardValidResponseCode403() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(403, Constants.USER_PHONE_NOT_VERIFIED_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        MockDataForTests.checkThreadSchedulers();

        String countryName = "United States";
        String countryCode = "US";
        mCountry.setText(countryName);
        mCountry.setCodeSelected(countryCode);
        mPhone.setText(PHONE);
        ivBtFwd.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(PHONE.equals(UserProfile.getPhone()));
        Assert.assertTrue(countryCode.equals(UserProfile.getCountryISO()));
        Intent expectedIntent = new Intent(activity, SignupPincodeActivity.class);
        String dialCode = mCountry.getText().toString().trim();
        dialCode = dialCode.substring(dialCode.lastIndexOf(" "));
        expectedIntent.putExtra("phoneNumber", dialCode + " " + mPhone.getText().toString());
        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (SignupPincodeActivity.class.getName()));
    }

    @Test
    public void testForwardUnexpectedException() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(500, Constants.USER_PHONE_NOT_VERIFIED_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        MockDataForTests.checkThreadSchedulers();

        String countryName = "United States";
        String countryCode = "US";
        mCountry.setText(countryName);
        mCountry.setCodeSelected(countryCode);
        mPhone.setText(PHONE);
        ivBtFwd.performClick();
        MockDataForTests.checkThreadSchedulers();

        String toast = ShadowToast.getTextOfLatestToast();
        Assert.assertTrue(toast.equals(activity.getResources().getString(R.string.error_reading_data_from_server)));
    }

    @Test
    public void testForwardValidResponseCode() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(201, Constants.LOGIN_OK_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        MockDataForTests.checkThreadSchedulers();

        String countryName = "United States";
        String countryCode = "US";
        mCountry.setText(countryName);
        mCountry.setCodeSelected(countryCode);
        mPhone.setText(PHONE);
        ivBtFwd.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(PHONE.equals(UserProfile.getPhone()));
        Assert.assertTrue(countryCode.equals(UserProfile.getCountryISO()));
        Intent expectedIntent = new Intent(activity, SplashScreenActivity.class);
        String dialCode = mCountry.getText().toString().trim();
        dialCode = dialCode.substring(dialCode.lastIndexOf(" "));
        expectedIntent.putExtra("phoneNumber", dialCode + " " + mPhone.getText().toString());
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
        Activity activity = Robolectric.buildActivity(SignupPhoneActivity.class).create().start().resume().pause().stop().destroy().get();
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(activity.isDestroyed());
    }

}