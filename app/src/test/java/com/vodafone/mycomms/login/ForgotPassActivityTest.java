package com.vodafone.mycomms.login;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.test.util.Util;

import org.apache.http.HttpResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.MockRepository;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.httpclient.FakeHttp;

import static com.vodafone.mycomms.constants.Constants.INVALID_EMAIL;
import static com.vodafone.mycomms.constants.Constants.VALID_EMAIL;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*"})
@PrepareForTest({Crashlytics.class})
public class ForgotPassActivityTest {

    Activity activity;
    Button btSend;
    EditText etEmail;

    MockWebServer webServer;

    @Before
    public void setUp() throws Exception
    {
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
        activity = Robolectric.buildActivity(ForgotPassActivity.class).create().start().resume().get();
        try {
            Thread.sleep(3000);
        }
        catch (Exception e)
        {
            Assert.fail();
        }
        Robolectric.flushForegroundThreadScheduler();
        etEmail = (EditText) activity.findViewById(R.id.etEmail);
        btSend = (Button) activity.findViewById(R.id.btSend);
    }

    @After
    public void tearDown() throws Exception
    {
        //Try to shutdown server if it was started
        try {
            Robolectric.reset();
            if(webServer!=null) webServer.shutdown();
        } catch (Exception e) {}

        activity = null;
        btSend = null;
        etEmail = null;
        webServer = null;
        System.gc();
    }

    @Test
    public void testSend() throws Exception {
        webServer = Util.newWebMockServer();
        String serverUrl = webServer.getUrl("/").toString();

        HttpResponse httpResponse = Util.buildResponse(204);
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
    }

    @Test
    public void testNoHeadersResponse() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(500);
        FakeHttp.addPendingHttpResponse(httpResponse);
        //Valid e-mail
        etEmail.setText(VALID_EMAIL);
        btSend.performClick();
        Assert.assertEquals(activity.getString(R.string.send_new_password), btSend.getText());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testFinish() throws Exception {
        Activity activity = Robolectric.buildActivity(ForgotPassActivity.class).create().start().resume().pause().stop().destroy().get();
        Assert.assertTrue(activity.isDestroyed());
    }
}
