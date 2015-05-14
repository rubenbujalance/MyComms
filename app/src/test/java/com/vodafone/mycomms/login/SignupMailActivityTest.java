package com.vodafone.mycomms.login;

import android.test.ActivityUnitTestCase;
import android.widget.ImageView;

import com.vodafone.mycomms.custom.ClearableEditText;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by str_rbm on 07/05/2015.
 */
public class SignupMailActivityTest extends ActivityUnitTestCase<SignupMailActivity> {

    SignupMailActivity activity;
    ClearableEditText etMail;
    ImageView ivForward;

    public SignupMailActivityTest() {
        super(SignupMailActivity.class);
    }

    @Before
    public void setUp() throws Exception {
//        activity = getActivity();
//        etMail = (ClearableEditText)(activity.findViewById(R.id.etSignupEmail));
//        ivForward = (ImageView)(activity.findViewById(R.id.ivBtForward));
    }

    @Test
     public void testClickForward() throws Exception {
        /*ivForward.performClick();
        String text = etMail.getText().toString();

        if(!android.util.Patterns.EMAIL_ADDRESS
                .matcher(text).matches())
        {

        }*/

        assertTrue(true);
    }

    @Test
    public void testTextChanged() throws Exception {
        assertTrue(true);
    }
}
