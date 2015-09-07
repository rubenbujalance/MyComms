package com.vodafone.mycomms.login;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.widget.Button;
import android.widget.ImageView;

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
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
public class SignupTypeChooseActivityTest {

    Activity activity;
    Button mSignupEmail;
    Button mSignupSalesforce;
    ImageView mBack;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.setupActivity(SignupTypeChooseActivity.class);
        mSignupEmail = (Button)activity.findViewById(R.id.btSignupMail);
        mSignupSalesforce = (Button)activity.findViewById(R.id.btSignupSalesforce);
        mBack = (ImageView)activity.findViewById(R.id.btBack);
    }

    @Test
    public void testSignupTypeChooseToSignupMail() throws Exception {
        mSignupEmail.performClick();
        Intent expectedIntent = new Intent(activity, SignupMailActivity.class);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }

    @Test
    public void testSignupTypeChooseToSignupOAuth() throws Exception {
        mSignupSalesforce.performClick();
        Intent expectedIntent = new Intent(activity, OAuthActivity.class);
        expectedIntent.putExtra("oauth", "sf");
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }

    @Test
    public void testBack() throws Exception {
        mBack.performClick();
        Assert.assertTrue(activity.isFinishing());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testFinish() throws Exception {
        Activity activity = Robolectric.buildActivity(SignupTypeChooseActivity.class).create().start().resume().pause().stop().destroy().get();
        Assert.assertTrue(activity.isDestroyed());
    }

}