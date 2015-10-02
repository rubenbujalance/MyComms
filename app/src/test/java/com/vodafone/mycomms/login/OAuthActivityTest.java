package com.vodafone.mycomms.login;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.View;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.events.ApplicationAndProfileInitialized;
import com.vodafone.mycomms.events.ApplicationAndProfileReadError;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.Constants;

import org.apache.http.HttpResponse;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
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

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.vodafone.mycomms.constants.Constants.OAUTH_200_RESPONSE;
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
        MockDataForTests.checkThreadSchedulers();
        wvOAuth = activity.wvOAuth;
    }

    @After
    public void tearDown() throws Exception
    {
        MockDataForTests.checkThreadSchedulers();
        Robolectric.reset();
        activity = null;
        incomingIntent = null;
        wvOAuth = null;
        System.gc();
    }

    @BeforeClass
    public static void setUpBeforeClass()
    {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                StringWriter writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                e.printStackTrace(printWriter);
                printWriter.flush();
                System.err.println("Uncaught exception at OAuthActivityTest: \n" + writer.toString());
            }
        });
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        Thread.currentThread().interrupt();
    }

    @Test
    public void testOAuthToSignupMailResponse200() throws Exception
    {
        HttpResponse httpResponse2 = Util.buildResponse(200, OAUTH_200_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse2);
        MockDataForTests.checkThreadSchedulers();
        String url = "https://" + EndpointWrapper.getBaseURL() + "/auth/" + activity.oauthPrefix + "/callback";
        ShadowWebView sWvOAuth = Shadows.shadowOf(wvOAuth);
        boolean didOverrideUrl = sWvOAuth.getWebViewClient().shouldOverrideUrlLoading(activity.wvOAuth, url);
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(didOverrideUrl);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }


    @Test
    public void testOAuthToSignupMail() throws Exception {
        HttpResponse httpResponse2 = Util.buildResponse(203, OAUTH_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse2);
        MockDataForTests.checkThreadSchedulers();
        String url = "https://" + EndpointWrapper.getBaseURL() + "/auth/" + activity.oauthPrefix + "/callback";
        ShadowWebView sWvOAuth = Shadows.shadowOf(wvOAuth);
        boolean didOverrideUrl = sWvOAuth.getWebViewClient().shouldOverrideUrlLoading(activity.wvOAuth, url);
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(didOverrideUrl);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testOAuthToSignupMailWrongURL() throws Exception {
        HttpResponse httpResponse2 = Util.buildResponse(203, OAUTH_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse2);
        MockDataForTests.checkThreadSchedulers();
        String url = "https://" + EndpointWrapper.getBaseURL() + "/auth/" + activity.oauthPrefix + "/call";
        ShadowWebView sWvOAuth = Shadows.shadowOf(wvOAuth);
        boolean didOverrideUrl = sWvOAuth.getWebViewClient().shouldOverrideUrlLoading(activity.wvOAuth, url);
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(didOverrideUrl);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testOAuthToLogin() throws Exception {
        HttpResponse httpResponse2 = Util.buildResponse(400);
        FakeHttp.addPendingHttpResponse(httpResponse2);
        MockDataForTests.checkThreadSchedulers();
        String url = "https://" + EndpointWrapper.getBaseURL() + "/auth/" + activity.oauthPrefix + "/callback";
        ShadowWebView sWvOAuth = Shadows.shadowOf(wvOAuth);
        boolean didOverrideUrl = sWvOAuth.getWebViewClient().shouldOverrideUrlLoading(activity.wvOAuth, url);
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(didOverrideUrl);
        Assert.assertTrue(activity.isFinishing());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testFinish() throws Exception {
        Activity activity = Robolectric.buildActivity(OAuthActivity.class).create().start().resume().pause().stop().destroy().get();
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(activity.isDestroyed());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");

    }

    @Test
    public void testApplicationAndProfileInitialized()
    {
        ApplicationAndProfileInitialized event = new ApplicationAndProfileInitialized();
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(this.activity.isFinishing());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testApplicationAndProfileReadError()
    {
        ApplicationAndProfileReadError event = new ApplicationAndProfileReadError();
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(this.activity.isFinishing());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testOnPageStarted()
    {
        RelativeLayout relativeContainer = (RelativeLayout)activity.findViewById(R.id.relative_container);
        String url = "https://" + EndpointWrapper.getBaseURL() + "/auth/" + activity.oauthPrefix + "/callback";
        ShadowWebView sWvOAuth = Shadows.shadowOf(wvOAuth);
        sWvOAuth.getWebViewClient().onPageStarted(activity.wvOAuth, url, null);
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(relativeContainer.getVisibility() == View.VISIBLE);
        Assert.assertTrue(activity.wvOAuth.getVisibility() == View.INVISIBLE);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testOnPageFinished()
    {
        RelativeLayout relativeContainer = (RelativeLayout)activity.findViewById(R.id.relative_container);
        String url = "https://" + EndpointWrapper.getBaseURL() + "/auth/" + activity.oauthPrefix + "/callback";
        ShadowWebView sWvOAuth = Shadows.shadowOf(wvOAuth);
        sWvOAuth.getWebViewClient().onPageFinished(activity.wvOAuth, url);
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(relativeContainer.getVisibility() == View.INVISIBLE);
        Assert.assertTrue(activity.wvOAuth.getVisibility() == View.VISIBLE);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }
}