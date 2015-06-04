package com.vodafone.mycomms.login;

import com.vodafone.mycomms.BuildConfig;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
public class SignupCompanyActivityTest {
/*
    SignupCompanyActivity activity;
    ImageView ivBtFwd;
    AutoCompleteTVSelectOnly mCompany;
    ClearableEditText mPosition;
    ClearableEditText mOfficeLoc;


    @Before
      public void setUp() {
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
//        mCompany.setListSelection(0);
//        mCompany.setSelection(0);
//        mCompany.isPopupShowing();
//        mCompany.callOnClick();
//        mCompany.getAdapter();
//        Shadows.shadowOf(mCompany.getDropDownBackground());
//        Shadows.shadowOf(mCompany).checkedPerformClick();
        ivBtFwd.performClick();
        Assert.assertTrue(companyName.equals(UserProfile.getCompanyName()));
        Intent expectedIntent = new Intent(activity, SignupPassActivity.class);
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
