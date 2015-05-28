package com.vodafone.mycomms.login;

import android.app.Activity;
import android.widget.Button;
import android.widget.EditText;

import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.test.util.Util;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.vodafone.mycomms.constants.Constants.*;
import static org.mockito.Mockito.*;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.FakeHttp;
import org.robolectric.shadows.ShadowActivity;

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
        HttpResponse httpResponse = Util.buildOkResponse();
        FakeHttp.addPendingHttpResponse(httpResponse);
        //Empty e-mail
        btSend.performClick();
        Assert.assertEquals(activity.getString(R.string.oops_wrong_email), btSend.getText());
        //Invalid e-mail
        etEmail.setText(INVALID_EMAIL);
        btSend.performClick();
        Assert.assertEquals(activity.getString(R.string.oops_wrong_email), btSend.getText());
        //Valid e-mail
        etEmail.setText(VALID_EMAIL);
        btSend.performClick();
        Assert.assertEquals(activity.getString(R.string.send_new_password), btSend.getText());
        Assert.assertTrue(activity.isFinishing());
        Assert.assertTrue(true);
    }

    //@Test
      public void testNoOkResponse() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(500);
        FakeHttp.addPendingHttpResponse(httpResponse);
        //Valid e-mail
        etEmail.setText("valid@test.com");
        btSend.performClick();
        Assert.assertEquals("Must update to last application version", btSend.getText());
        Assert.assertTrue(activity.isFinishing());
        Assert.assertTrue(true);
    }

    //@Test
    public void testNoHeadersResponse() throws Exception {
        HttpResponse httpResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, 500, "OK");
        FakeHttp.addPendingHttpResponse(httpResponse);
        //Valid e-mail
        etEmail.setText(VALID_EMAIL);
        btSend.performClick();
        Assert.assertEquals(activity.getString(R.string.send_new_password), btSend.getText());
        Assert.assertTrue(activity.isFinishing());
        Assert.assertTrue(true);
    }

}
