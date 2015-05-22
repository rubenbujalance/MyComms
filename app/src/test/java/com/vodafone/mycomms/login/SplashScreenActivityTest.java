package com.vodafone.mycomms.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;

import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.ContactListMainActivity;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.main.SplashScreenActivity;
import com.vodafone.mycomms.test.util.Util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.FakeHttp;
import org.robolectric.shadows.ShadowAlertDialog;
import static com.vodafone.mycomms.constants.Constants.INVALID_VERSION_RESPONSE;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
public class SplashScreenActivityTest {

    Activity activity;
    Button btSend;
    EditText etEmail;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testCheckVersionToLoginOk() throws Exception {
        HttpResponse httpResponse = Util.buildOkResponse();
        FakeHttp.addPendingHttpResponse(httpResponse);
        activity = Robolectric.setupActivity(SplashScreenActivity.class);
        Assert.assertTrue(activity.isFinishing());
        Intent expectedIntent = new Intent(activity, ContactListMainActivity.class);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }

    @Test
     public void testCheckVersionToLoginSignupActivity() throws Exception {
        HttpResponse httpResponse = Util.buildOkResponse();
        FakeHttp.addPendingHttpResponse(httpResponse);
        activity = Robolectric.setupActivity(SplashScreenActivity.class);
        Assert.assertTrue(activity.isFinishing());
        Intent expectedIntent = new Intent(activity, LoginSignupActivity.class);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }

    @Test
      public void testInvalidVersionResponse() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(400, INVALID_VERSION_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        activity = Robolectric.setupActivity(SplashScreenActivity.class);
        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog sAlert = Shadows.shadowOf(alert);
        Assert.assertTrue(sAlert.getTitle().toString().equals(activity.getString(R.string.new_version_available)));
    }

    @Test
      public void testNoOkResponse() throws Exception {
        HttpResponse httpResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, 500, "OK");
        HttpEntity entity = new StringEntity(INVALID_VERSION_RESPONSE);
        httpResponse.setEntity(entity);
        httpResponse.setHeader("Content-Type", "application/json");
        FakeHttp.addPendingHttpResponse(httpResponse);
        //Valid e-mail
        etEmail.setText("valid@test.com");
        btSend.performClick();
        Assert.assertEquals("Must update to last application version", btSend.getText());
        Assert.assertTrue(activity.isFinishing());
        Assert.assertTrue(true);
    }

    @Test
    public void testNoHeadersResponse() throws Exception {
        HttpResponse httpResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, 500, "OK");
        //httpResponse.setHeader("Content-Type", "application/json");
        FakeHttp.addPendingHttpResponse(httpResponse);
        //Valid e-mail
        etEmail.setText("valid@test.com");
        btSend.performClick();
        Assert.assertEquals(activity.getString(R.string.send_new_password), btSend.getText());
        Assert.assertTrue(activity.isFinishing());
        Assert.assertTrue(true);
    }

}
