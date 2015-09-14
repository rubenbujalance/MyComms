package com.vodafone.mycomms.login;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.UserProfile;
import com.vodafone.mycomms.contacts.detail.ContactDetailsPlusActivity;
import com.vodafone.mycomms.custom.AutoCompleteTVSelectOnly;
import com.vodafone.mycomms.custom.ClearableEditText;
import com.vodafone.mycomms.test.util.Util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.MockRepository;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

import io.realm.Realm;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
public class SignupCompanyActivityTest {

    SignupCompanyActivity activity;
    ImageView ivBtFwd;
    AutoCompleteTVSelectOnly mCompany;
    ClearableEditText mPosition;
    ClearableEditText mOfficeLoc;


    @Before
    public void setUp()
    {
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
        activity = Robolectric.setupActivity(SignupCompanyActivity.class);
        ivBtFwd = (ImageView)activity.findViewById(R.id.ivBtForward);
        mCompany = activity.mCompany;
        mPosition = activity.mPosition;
        mOfficeLoc = activity.mOfficeLoc;
    }

    @Test
    public void testForwardEmptyCompany() {
        ivBtFwd.performClick();
        Assert.assertTrue(mCompany.getError().equals(activity.getString(R.string.select_your_company_to_continue)));
    }

    @Test
    public void testForward() {
        String companyName = "Stratesys";
        String companyCode = "001";
        mCompany.setText(companyName);
        mCompany.setCodeSelected(companyCode);
        mCompany.callOnClick();
        /*mCompany.setListSelection(0);
        mCompany.setSelection(0);
        mCompany.isPopupShowing();
        mCompany.callOnClick();
        mCompany.getAdapter();
        Shadows.shadowOf(mCompany.getDropDownBackground());
        Shadows.shadowOf(mCompany).checkedPerformClick();*/
        ivBtFwd.performClick();
        Assert.assertTrue(companyName.equals(UserProfile.getCompanyName()));

        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(SignupPassActivity.class.getName()));
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
        Activity activity = Robolectric.buildActivity(SignupCompanyActivity.class).create().start().resume().pause().stop().destroy().get();
        Assert.assertTrue(activity.isDestroyed());
    }
}