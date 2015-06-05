package com.vodafone.mycomms.login;

import com.vodafone.mycomms.BuildConfig;

import org.robolectric.annotation.Config;

/**
 * Created by str_evc on 18/05/2015.
 */
//@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
public class SignupPassActivityTest {
/*
    SignupPassActivity activity;
    ClearableEditText mPassword;
    ClearableEditText mConfirmPass;
    ImageView ivBtFwd;
    ImageView ivBtBack;

    @Before
      public void setUp() {
        activity = Robolectric.setupActivity(SignupPassActivity.class);
        ivBtFwd = (ImageView)activity.findViewById(R.id.ivBtForward);
        ivBtBack = (ImageView)activity.findViewById(R.id.ivBtBack);
        mPassword = activity.mPassword;
        mConfirmPass = activity.mConfirmPass;
    }

    @Test
     public void testForwardBadPassword() {
        ivBtFwd.performClick();
        EditText innerPassword = (EditText)mPassword.findViewById(R.id.clearable_edit);
        Assert.assertTrue(innerPassword.getError().equals(activity.getString(R.string.incorrect_format)));
    }

    @Test
    public void testForwardPasswordAndPasswordConfirmNoMatching() {
        mPassword.setText(PASSWORD);
        mConfirmPass.setText(ANOTHER_PASSWORD);
        ivBtFwd.performClick();
        EditText innerConfirmPass = (EditText)mConfirmPass.findViewById(R.id.clearable_edit);
        Assert.assertTrue(innerConfirmPass.getError().equals(activity.getString(R.string.passwords_do_not_match)));
    }

    @Test
    public void testForward() {
        mPassword.setText(PASSWORD);
        mConfirmPass.setText(PASSWORD);
        ivBtFwd.performClick();
        Assert.assertTrue(PASSWORD.equals(UserProfile.getPassword()));
        Intent expectedIntent = new Intent(activity, SignupPhoneActivity.class);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }

    @Test
    public void testBack() throws Exception {
        ImageView ivBtBack = (ImageView)activity.findViewById(R.id.ivBtBack);
        ivBtBack.performClick();
        Assert.assertTrue(activity.isFinishing());
    }
*/
}
