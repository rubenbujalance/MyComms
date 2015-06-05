package com.vodafone.mycomms.login;

import android.app.Activity;
import android.widget.Button;
import android.widget.EditText;

import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.constants.Constants;
import com.vodafone.mycomms.test.util.Util;

import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.FakeHttp;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
public class ForgotPassActivityTest {

    Activity activity;
    Button btSend;
    EditText etEmail;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.setupActivity(ForgotPassActivity.class);
        etEmail = (EditText) activity.findViewById(R.id.etEmail);
        btSend = (Button) activity.findViewById(R.id.btSend);
    }

    @Test
    public void testSend() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(204);
        FakeHttp.addPendingHttpResponse(httpResponse);
        //Empty e-mail
        btSend.performClick();
        Assert.assertEquals(activity.getString(R.string.oops_wrong_email), btSend.getText());
        //Invalid e-mail
        etEmail.setText(Constants.INVALID_EMAIL);
        btSend.performClick();
        Assert.assertEquals(activity.getString(R.string.oops_wrong_email), btSend.getText());
        //Valid e-mail
        etEmail.setText(Constants.VALID_EMAIL);
        btSend.performClick();
        Assert.assertEquals(activity.getString(R.string.send_new_password), btSend.getText());
        Assert.assertTrue(activity.isFinishing());
    }

    @Test
    public void testNoHeadersResponse() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(500);
        FakeHttp.addPendingHttpResponse(httpResponse);
        //Valid e-mail
        etEmail.setText(Constants.VALID_EMAIL);
        btSend.performClick();
        Assert.assertEquals(activity.getString(R.string.send_new_password), btSend.getText());
    }

}
