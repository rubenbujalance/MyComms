package com.vodafone.mycomms.login;

import android.app.Activity;
import android.content.Intent;
import android.widget.Button;

import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

/**
 * Created by str_evc on 18/05/2015.
 */
//@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
public class LoginSignupActivityTest {

    Activity activity;
    Button btSignup;
    Button btLogin;

//    @Before
    public void setUp() {
        activity = Robolectric.setupActivity(LoginSignupActivity.class);
        btSignup = (Button)activity.findViewById(R.id.btSignup);
        btLogin = (Button)activity.findViewById(R.id.btLogin);
    }

//    @Test
    public void testLoginSignupToSignupTypeChooseActivity() {
        btSignup.performClick();
        Intent expectedIntent = new Intent(activity, SignupTypeChooseActivity.class);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }

//    @Test
    public void testLoginSignupToLogin() {
        btLogin.performClick();
        Intent expectedIntent = new Intent(activity, LoginActivity.class);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }

}