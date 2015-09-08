package com.vodafone.mycomms.login;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.widget.EditText;
import android.widget.ImageView;

import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.UserProfile;
import com.vodafone.mycomms.constants.Constants;
import com.vodafone.mycomms.custom.AutoCompleteTVSelectOnly;
import com.vodafone.mycomms.custom.ClearableEditText;
import com.vodafone.mycomms.main.SplashScreenActivity;
import com.vodafone.mycomms.test.util.Util;

import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.shadows.httpclient.FakeHttp;

import static com.vodafone.mycomms.constants.Constants.PHONE;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
public class SignupPhoneActivityTest {

    SignupPhoneActivity activity;
    ClearableEditText mPhone;
    AutoCompleteTVSelectOnly mCountry;
    ImageView ivBtFwd;
    ImageView ivBtBack;

    @Before
    public void setUp() {
        activity = Robolectric.setupActivity(SignupPhoneActivity.class);
        ivBtFwd = (ImageView)activity.findViewById(R.id.ivBtForward);
        ivBtBack = (ImageView)activity.findViewById(R.id.ivBtBack);
        mPhone = activity.mPhone;
        mCountry = activity.mCountry;
    }

    @Test
    public void testForwardNoCountrySelected() {
        System.err.println("******** Test: Empty Country********");
        mCountry.setText("");
        mCountry.setCodeSelected("");
        ivBtFwd.performClick();
        Assert.assertTrue(mCountry.getError().equals(activity.getString(R.string.select_your_phone_country_to_continue)));
    }

    @Test
    public void testForwardNullCountrySelected() {
        System.err.println("******** Test: Empty Country********");
        mCountry.setText(null);
        mCountry.setCodeSelected(null);
        ivBtFwd.performClick();
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
        EditText innerPhone = (EditText)mPhone.findViewById(R.id.clearable_edit);
        Assert.assertTrue(innerPhone.getError().equals(activity.getString(R.string.enter_your_phone_number_to_continue)));
    }

    @Test
    public void testForwardInvalidResponseCode200() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(200, Constants.USER_PHONE_NOT_VERIFIED_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        String countryName = "United States";
        String countryCode = "US";
        mCountry.setText(countryName);
        mCountry.setCodeSelected(countryCode);
        mPhone.setText(PHONE);
        ivBtFwd.performClick();
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
        String countryName = "United States";
        String countryCode = "US";
        mCountry.setText(countryName);
        mCountry.setCodeSelected(countryCode);
        mPhone.setText(PHONE);
        ivBtFwd.performClick();
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
        String countryName = "United States";
        String countryCode = "US";
        mCountry.setText(countryName);
        mCountry.setCodeSelected(countryCode);
        mPhone.setText(PHONE);
        ivBtFwd.performClick();
        String toast = ShadowToast.getTextOfLatestToast();
        Assert.assertTrue(toast.equals(activity.getResources().getString(R.string.error_reading_data_from_server)));
    }

    @Test
    public void testForwardValidResponseCode() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(201, Constants.LOGIN_OK_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        String countryName = "United States";
        String countryCode = "US";
        mCountry.setText(countryName);
        mCountry.setCodeSelected(countryCode);
        mPhone.setText(PHONE);
        ivBtFwd.performClick();
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
        Assert.assertTrue(activity.isFinishing());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testFinish() throws Exception {
        Activity activity = Robolectric.buildActivity(SignupPhoneActivity.class).create().start().resume().pause().stop().destroy().get();
        Assert.assertTrue(activity.isDestroyed());
    }

}