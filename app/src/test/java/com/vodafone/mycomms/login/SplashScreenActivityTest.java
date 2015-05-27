package com.vodafone.mycomms.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;

import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.ContactListMainActivity;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.main.SplashScreenActivity;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.UserSecurity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.res.builder.RobolectricPackageManager;
import org.robolectric.shadows.FakeHttp;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowResolveInfo;

import java.util.ArrayList;
import java.util.List;

import static com.vodafone.mycomms.constants.Constants.VALID_VERSION_RESPONSE;
import static com.vodafone.mycomms.constants.Constants.INVALID_VERSION_RESPONSE;
import static com.vodafone.mycomms.constants.Constants.LOGIN_OK_RESPONSE;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
public class SplashScreenActivityTest {

    Activity activity;

    @Before
    public void setUp() throws Exception {

    }

    @Test
        public void testCheckVersionUserLoggedOk() throws Exception {
        String accessToken = "c7n_CO-qva-s9vgLfbljwQKLqf9hQVhMFNvWgP-ula0O-SG0DYdXPgI6zt1cgdZuBfvLSFXdjc_T2hpGNJ_mv3M_IClqDYqUAUNCFeiLPtUJIvvoO5IKYXlPgYHkCZsZ0Maf6bGXhLXLIyZQcjPvLtovTLgEN0tQZIpfMIVFpG4";
        String refreshToken = "RaHZJLVyVc7ZxyDEJsTZcLpXVxmPnUKzHJ3cofn2HYyTYV0B9wQyCVPsNZuVWRKr8o99gutaENceAfVbeSPvcHeiQaiAeQmcZpxwEXj7aza8t7jjTlImw59f6sj6RVhokHtrokRCNIzxC7Jfe8qhJoGZ6WnSaEJlh1EFJFqag0M";
        long expiresIn = 1000L;
        UserSecurity.setTokens(accessToken, refreshToken, expiresIn, RuntimeEnvironment.application);
        HttpResponse httpResponse = Util.buildOkResponse(VALID_VERSION_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        activity = Robolectric.setupActivity(SplashScreenActivity.class);
        Assert.assertTrue(activity.isFinishing());
        Intent expectedIntent = new Intent(activity, ContactListMainActivity.class);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }

    @Test
    public void testCheckVersionNoNetworkConnectionUserLoggedOk() throws Exception {
        Context context = RuntimeEnvironment.application.getApplicationContext();
        ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(false);
                String accessToken = "c7n_CO-qva-s9vgLfbljwQKLqf9hQVhMFNvWgP-ula0O-SG0DYdXPgI6zt1cgdZuBfvLSFXdjc_T2hpGNJ_mv3M_IClqDYqUAUNCFeiLPtUJIvvoO5IKYXlPgYHkCZsZ0Maf6bGXhLXLIyZQcjPvLtovTLgEN0tQZIpfMIVFpG4";
        String refreshToken = "RaHZJLVyVc7ZxyDEJsTZcLpXVxmPnUKzHJ3cofn2HYyTYV0B9wQyCVPsNZuVWRKr8o99gutaENceAfVbeSPvcHeiQaiAeQmcZpxwEXj7aza8t7jjTlImw59f6sj6RVhokHtrokRCNIzxC7Jfe8qhJoGZ6WnSaEJlh1EFJFqag0M";
        long expiresIn = 1000L;
        UserSecurity.setTokens(accessToken, refreshToken, expiresIn, RuntimeEnvironment.application);
        HttpResponse httpResponse = Util.buildOkResponse(VALID_VERSION_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        activity = Robolectric.setupActivity(SplashScreenActivity.class);
        Assert.assertTrue(activity.isFinishing());
        Intent expectedIntent = new Intent(activity, ContactListMainActivity.class);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }

    @Test
    public void testCheckVersionNoNetworkConnectionUserNotLogged() throws Exception {
        Context context = RuntimeEnvironment.application.getApplicationContext();
        ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(false);
        HttpResponse httpResponse = Util.buildOkResponse(VALID_VERSION_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        activity = Robolectric.setupActivity(SplashScreenActivity.class);
        Assert.assertTrue(activity.isFinishing());
        Intent expectedIntent = new Intent(activity, LoginSignupActivity.class);
        //Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }

    @Test
    public void testCheckVersionUserLoggedRenewTokenOk() throws Exception {
        String accessToken = "c7n_CO-qva-s9vgLfbljwQKLqf9hQVhMFNvWgP-ula0O-SG0DYdXPgI6zt1cgdZuBfvLSFXdjc_T2hpGNJ_mv3M_IClqDYqUAUNCFeiLPtUJIvvoO5IKYXlPgYHkCZsZ0Maf6bGXhLXLIyZQcjPvLtovTLgEN0tQZIpfMIVFpG4";
        String refreshToken = "RaHZJLVyVc7ZxyDEJsTZcLpXVxmPnUKzHJ3cofn2HYyTYV0B9wQyCVPsNZuVWRKr8o99gutaENceAfVbeSPvcHeiQaiAeQmcZpxwEXj7aza8t7jjTlImw59f6sj6RVhokHtrokRCNIzxC7Jfe8qhJoGZ6WnSaEJlh1EFJFqag0M";
        long expiresIn = 1L;
        UserSecurity.setTokens(accessToken, refreshToken, expiresIn, RuntimeEnvironment.application);
        HttpResponse httpResponse = Util.buildOkResponse(VALID_VERSION_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        HttpResponse httpResponseRenewToken = Util.buildResponse(200, LOGIN_OK_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponseRenewToken);
        activity = Robolectric.setupActivity(SplashScreenActivity.class);
        Assert.assertTrue(activity.isFinishing());
        Intent expectedIntent = new Intent(activity, ContactListMainActivity.class);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }

    @Test
    public void testCheckVersionUserLoggedRenewTokenToLoginSignup() throws Exception {
        String accessToken = "c7n_CO-qva-s9vgLfbljwQKLqf9hQVhMFNvWgP-ula0O-SG0DYdXPgI6zt1cgdZuBfvLSFXdjc_T2hpGNJ_mv3M_IClqDYqUAUNCFeiLPtUJIvvoO5IKYXlPgYHkCZsZ0Maf6bGXhLXLIyZQcjPvLtovTLgEN0tQZIpfMIVFpG4";
        String refreshToken = "RaHZJLVyVc7ZxyDEJsTZcLpXVxmPnUKzHJ3cofn2HYyTYV0B9wQyCVPsNZuVWRKr8o99gutaENceAfVbeSPvcHeiQaiAeQmcZpxwEXj7aza8t7jjTlImw59f6sj6RVhokHtrokRCNIzxC7Jfe8qhJoGZ6WnSaEJlh1EFJFqag0M";
        long expiresIn = 1L;
        UserSecurity.setTokens(accessToken, refreshToken, expiresIn, RuntimeEnvironment.application);
        HttpResponse httpResponse = Util.buildOkResponse(VALID_VERSION_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        HttpResponse httpResponseRenewToken = Util.buildResponse(500, VALID_VERSION_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponseRenewToken);
        activity = Robolectric.setupActivity(SplashScreenActivity.class);
        Assert.assertTrue(activity.isFinishing());
        Intent expectedIntent = new Intent(activity, LoginSignupActivity.class);
        System.out.println("NextStartedActivity: " + Shadows.shadowOf(activity).getNextStartedActivity());
        //Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }

    @Test
      public void testInvalidVersionResponse() throws Exception {
        RobolectricPackageManager rpm = (RobolectricPackageManager)Shadows.shadowOf(RuntimeEnvironment.application).getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
        ResolveInfo info = new ResolveInfo();
        info.isDefault = true;
        rpm.addResolveInfoForIntent(intent, info);
        HttpResponse httpResponse = Util.buildResponse(400, INVALID_VERSION_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        activity = Robolectric.setupActivity(SplashScreenActivity.class);
        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog sAlert = Shadows.shadowOf(alert);
        Assert.assertTrue(sAlert.getTitle().toString().equals(activity.getString(R.string.new_version_available)));
        Button updateButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
        updateButton.performClick();
        alert = ShadowAlertDialog.getLatestAlertDialog();
        sAlert = Shadows.shadowOf(alert);
        Assert.assertTrue(sAlert.getTitle().toString().equals(activity.getString(R.string.update2)));
        Button okButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
        okButton.performClick();
        Assert.assertTrue(activity.isFinishing());
    }

    //@Test
    public void testEmailLink() throws Exception {
        String link = "<a href=\"intent://user/refreshToken/RaHZJLVyVc7ZxyDEJsTZcLpXVxmPnUKzHJ3cofn2HYyTYV0B9wQyCVPsNZuVWRKrtTVen_KnG7mTa_vYKFM4TEv4AIMMYeTcJXvCPQnDAPdaui1dqprrPYxVpCYlqVxOwpdbkx_wwPT7BuxYpfvlG9oirrdxhvB0jQGwnZnrseo/#Intent;scheme=mycomms-i;end\">Link to MyComms</a>";
        String accessToken = "c7n_CO-qva-s9vgLfbljwQKLqf9hQVhMFNvWgP-ula0O-SG0DYdXPgI6zt1cgdZuBfvLSFXdjc_T2hpGNJ_mv3M_IClqDYqUAUNCFeiLPtUJIvvoO5IKYXlPgYHkCZsZ0Maf6bGXhLXLIyZQcjPvLtovTLgEN0tQZIpfMIVFpG4";
        String refreshToken = "RaHZJLVyVc7ZxyDEJsTZcLpXVxmPnUKzHJ3cofn2HYyTYV0B9wQyCVPsNZuVWRKr8o99gutaENceAfVbeSPvcHeiQaiAeQmcZpxwEXj7aza8t7jjTlImw59f6sj6RVhokHtrokRCNIzxC7Jfe8qhJoGZ6WnSaEJlh1EFJFqag0M";
        long expiresIn = 1000L;
        UserSecurity.setTokens(accessToken, refreshToken, expiresIn, RuntimeEnvironment.application);
        HttpResponse httpResponse = Util.buildOkResponse(VALID_VERSION_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        activity = Robolectric.setupActivity(SplashScreenActivity.class);
        Assert.assertTrue(activity.isFinishing());
        Intent expectedIntent = new Intent(activity, ContactListMainActivity.class);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }

}
