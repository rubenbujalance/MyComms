package com.vodafone.mycomms.login;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.webkit.WebView;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.events.ApplicationAndProfileInitialized;
import com.vodafone.mycomms.events.ApplicationAndProfileReadError;
import com.vodafone.mycomms.events.BusProvider;
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
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowWebView;
import org.robolectric.shadows.httpclient.FakeHttp;

import static com.vodafone.mycomms.constants.Constants.OAUTH_RESPONSE;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms",
    manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*"})
@PrepareForTest({Crashlytics.class})
public class OAuthActivityTest {

    OAuthActivity activity;
    Intent incomingIntent;
    WebView wvOAuth;

    @Before
    public void setUp() throws Exception
    {
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
        incomingIntent = new Intent();
        incomingIntent.putExtra("oauth", "sf");
        activity = Robolectric.buildActivity(OAuthActivity.class).withIntent(incomingIntent).create().start().resume().get();
        try {
            Thread.sleep(2000);
        }
        catch (Exception e)
        {
            Assert.fail();
        }
        if(null != Robolectric.getForegroundThreadScheduler())
            Robolectric.flushForegroundThreadScheduler();
        wvOAuth = activity.wvOAuth;
    }

    @After
    public void tearDown() throws Exception
    {
        Robolectric.reset();
        activity = null;
        incomingIntent = null;
        wvOAuth = null;
        System.gc();
    }

    @Test
    public void testOAuthToSignupMail() throws Exception {
        HttpResponse httpResponse2 = Util.buildResponse(203, OAUTH_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse2);
        String url = "https://" + EndpointWrapper.getBaseURL() + "/auth/" + activity.oauthPrefix + "/callback";
        ShadowWebView sWvOAuth = Shadows.shadowOf(wvOAuth);
        boolean didOverrideUrl = sWvOAuth.getWebViewClient().shouldOverrideUrlLoading(activity.wvOAuth, url);
        Assert.assertTrue(didOverrideUrl);
    }

    @Test
    public void testOAuthToLogin() throws Exception {
        HttpResponse httpResponse2 = Util.buildResponse(400);
        FakeHttp.addPendingHttpResponse(httpResponse2);
        String url = "https://" + EndpointWrapper.getBaseURL() + "/auth/" + activity.oauthPrefix + "/callback";
        ShadowWebView sWvOAuth = Shadows.shadowOf(wvOAuth);
        boolean didOverrideUrl = sWvOAuth.getWebViewClient().shouldOverrideUrlLoading(activity.wvOAuth, url);
        Assert.assertTrue(didOverrideUrl);
        Assert.assertTrue(activity.isFinishing());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testFinish() throws Exception {
        Activity activity = Robolectric.buildActivity(OAuthActivity.class).create().start().resume().pause().stop().destroy().get();
        Assert.assertTrue(activity.isDestroyed());
    }

    @Test
    public void testApplicationAndProfileInitialized()
    {
        ApplicationAndProfileInitialized event = new ApplicationAndProfileInitialized();
        BusProvider.getInstance().post(event);
        Assert.assertTrue(this.activity.isFinishing());
    }

    @Test
    public void testApplicationAndProfileReadError()
    {
        ApplicationAndProfileReadError event = new ApplicationAndProfileReadError();
        BusProvider.getInstance().post(event);
        Assert.assertTrue(this.activity.isFinishing());
    }
}