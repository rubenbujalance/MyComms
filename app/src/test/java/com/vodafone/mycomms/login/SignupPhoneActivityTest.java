package com.vodafone.mycomms.login;

import android.content.Intent;
import android.widget.EditText;
import android.widget.ImageView;

import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.UserProfile;
import com.vodafone.mycomms.custom.AutoCompleteTVSelectOnly;
import com.vodafone.mycomms.custom.ClearableEditText;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

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
        mCountry.setText("");
        mCountry.setCodeSelected("");
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
    public void testForward() {
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
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }

    @Test
    public void testBack() throws Exception {
        ImageView ivBtBack = (ImageView)activity.findViewById(R.id.ivBtBack);
        ivBtBack.performClick();
        Assert.assertTrue(activity.isFinishing());
    }

}
