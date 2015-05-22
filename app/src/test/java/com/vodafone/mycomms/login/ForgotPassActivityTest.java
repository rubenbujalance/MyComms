package com.vodafone.mycomms.login;

import android.app.Activity;
import android.widget.Button;
import android.widget.EditText;

import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;


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

    private final String RESPONSE_INVALID_VERSION = "{\"err\":\"invalid_version\",\"des\":\"Must update to last application version\",\"data\":\"https://s3-us-west-2.amazonaws.com/mycomms-android/MyComms/android/dev/91/MyComms-i.apk\"}";

    Activity activity;
    Button btSend;
    EditText etEmail;

    @Before
    public void setUp() throws Exception {
        HttpResponse httpResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, 204, "OK");
        httpResponse.setHeader("Content-Type", "application/json");
        FakeHttp.setDefaultHttpResponse(httpResponse);
        activity = Robolectric.setupActivity(ForgotPassActivity.class);
        etEmail = (EditText) activity.findViewById(R.id.etEmail);
        btSend = (Button) activity.findViewById(R.id.btSend);
        //FakeHttp.addPendingHttpResponse(200, "OK");
        //FakeHttp.setDefaultHttpResponse(200, "OK");
        //DefaultHttpClient httpclient = spy(new DefaultHttpClient());
        //HttpClient httpclient = new DefaultHttpClient();
        //HttpResponse httpResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
        //HttpResponse httpResponse = mock(HttpResponse.class);
        //doReturn(httpResponse).when(httpclient).execute(any(HttpGet.class));
        //when(httpclient.execute(any(HttpGet.class))).thenReturn(httpResponse);
    }

    @Test
    public void testSend() throws Exception {
        //Empty e-mail
        btSend.performClick();
        Assert.assertEquals(activity.getString(R.string.oops_wrong_email), btSend.getText());
        //Invalid e-mail
        etEmail.setText("invalid_email");
        btSend.performClick();
        Assert.assertEquals(activity.getString(R.string.oops_wrong_email), btSend.getText());
        //Valid e-mail
        etEmail.setText("valid@test.com");
        btSend.performClick();
        Assert.assertEquals(activity.getString(R.string.send_new_password), btSend.getText());
        Assert.assertTrue(activity.isFinishing());
        Assert.assertTrue(true);
    }

    @Test
      public void testNoOkResponse() throws Exception {
        HttpResponse httpResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, 500, "OK");
        HttpEntity entity = new StringEntity(RESPONSE_INVALID_VERSION);
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
