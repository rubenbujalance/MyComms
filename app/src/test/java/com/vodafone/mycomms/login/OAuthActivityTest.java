package com.vodafone.mycomms.login;

import android.content.Intent;
import android.webkit.WebView;

import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.main.DashBoardActivity;
import com.vodafone.mycomms.test.util.Util;

import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowWebView;
import org.robolectric.shadows.httpclient.FakeHttp;

import static com.vodafone.mycomms.constants.Constants.LOGIN_OK_RESPONSE;
import static com.vodafone.mycomms.constants.Constants.OAUTH_RESPONSE;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
public class OAuthActivityTest {

    OAuthActivity activity;
    Intent incomingIntent;
    WebView wvOAuth;


    @Before
    public void setUp() {
        incomingIntent = new Intent();
        incomingIntent.putExtra("oauth", "sf");
        activity = Robolectric.buildActivity(OAuthActivity.class).withIntent(incomingIntent).create().get();
        wvOAuth = activity.wvOAuth;
    }

//    @Test
//    public void testOAuthToContactList() throws Exception {
//        HttpResponse httpResponse = Util.buildResponse(200, LOGIN_OK_RESPONSE);
//        FakeHttp.addPendingHttpResponse(httpResponse);
//        String url = "https://" + EndpointWrapper.getBaseURL() + "/auth/" + activity.oauthPrefix + "/callback";
//        ShadowWebView sWvOAuth = Shadows.shadowOf(wvOAuth);
//        boolean didOverrideUrl = sWvOAuth.getWebViewClient().shouldOverrideUrlLoading(activity.wvOAuth, url);
//        Assert.assertTrue(didOverrideUrl);
//        Intent expectedIntent = new Intent(activity, DashBoardActivity.class);
//        expectedIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
//    }

    @Test
    public void testOAuthToSignupMail() throws Exception {
        HttpResponse httpResponse2 = Util.buildResponse(203, OAUTH_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse2);
        String url = "https://" + EndpointWrapper.getBaseURL() + "/auth/" + activity.oauthPrefix + "/callback";
        ShadowWebView sWvOAuth = Shadows.shadowOf(wvOAuth);
        boolean didOverrideUrl = sWvOAuth.getWebViewClient().shouldOverrideUrlLoading(activity.wvOAuth, url);
        Assert.assertTrue(didOverrideUrl);
        Intent expectedIntent = new Intent(activity, SignupMailActivity.class);
        expectedIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
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

}