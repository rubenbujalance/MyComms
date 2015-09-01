package com.vodafone.mycomms.login;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.Log;
import android.widget.Button;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.constants.Constants;
import com.vodafone.mycomms.main.DashBoardActivity;
import com.vodafone.mycomms.main.SplashScreenActivity;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.UserSecurity;
import com.vodafone.mycomms.util.Utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.MockRepository;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.res.builder.RobolectricPackageManager;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowIntent;

import io.realm.Realm;
import okio.Buffer;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*"})
@PrepareForTest({Realm.class, Crashlytics.class, EndpointWrapper.class})
public class SplashScreenActivityTest{

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    Activity activity;
    String mCrashMessage = "mockCrashMessage";
    MockWebServer webServer;

    @Before
    public void setUp() throws Exception
    {
        mockStatic(Realm.class);
        when(Realm.getDefaultInstance()).thenReturn(null);
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
    }

    @Test
    public void testOnCreate()
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra(com.vodafone.mycomms.util.Constants.IS_APP_CRASHED_EXTRA, true);
        intent.putExtra(com.vodafone.mycomms.util.Constants.APP_CRASH_MESSAGE, mCrashMessage);
        activity = Robolectric.buildActivity(SplashScreenActivity.class).withIntent(intent).create().start().resume().visible().get();

        Assert.assertTrue(activity.getIntent().hasExtra(com.vodafone.mycomms.util.Constants.IS_APP_CRASHED_EXTRA));
        if(activity.getIntent().hasExtra(com.vodafone.mycomms.util.Constants.IS_APP_CRASHED_EXTRA))
        {
            Assert.assertTrue(activity.getIntent().getBooleanExtra(com.vodafone.mycomms.util.Constants.IS_APP_CRASHED_EXTRA, false));
        }
        Assert.assertTrue(activity.getIntent().hasExtra(com.vodafone.mycomms.util.Constants.APP_CRASH_MESSAGE));
        if(activity.getIntent().hasExtra(com.vodafone.mycomms.util.Constants.APP_CRASH_MESSAGE))
        {
            Assert.assertEquals(activity.getIntent().getStringExtra(com.vodafone.mycomms.util.Constants.APP_CRASH_MESSAGE)
                    ,mCrashMessage);
        }
        testAlertWithNegativeButton();
        testAlertWithPositiveButton();

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testAllLifeCycleEvents()
    {
        activity = Robolectric.buildActivity(SplashScreenActivity.class).create().start().resume().pause().resume().pause().stop().destroy().get();
        Assert.assertTrue(activity.isDestroyed());
    }

    public void testSendSupportEmail()
    {
        String mockErrorMessage = "errorMessage";
        Utils.launchSupportEmail
                (
                        activity
                        , activity.getApplicationContext().getResources().getString(R.string.support_subject_crash)
                        , activity.getApplicationContext().getResources().getString(R.string.support_text_crash)
                                + "\n\n" + mockErrorMessage
                        , activity.getApplicationContext().getResources().getString(R.string.support_email)
                        , com.vodafone.mycomms.util.Constants.REQUEST_START_ACTIVITY_FOR_APP_CRASH
                );
    }

    private void testAlertWithNegativeButton()
    {
        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        Button btnNegative = alert.getButton(AlertDialog.BUTTON_NEGATIVE);
        btnNegative.performClick();
        Assert.assertTrue(!alert.isShowing());
    }

    private void testAlertWithPositiveButton()
    {
        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        Button btnNegative = alert.getButton(AlertDialog.BUTTON_POSITIVE);
        btnNegative.performClick();
        Assert.assertTrue(!alert.isShowing());
        testSendSupportEmail();
    }

    @Test
    public void testOnCreateWithoutExtras()
    {
        activity = Robolectric.buildActivity(SplashScreenActivity.class).create().get();
        Assert.assertFalse(activity.getIntent().hasExtra(com.vodafone.mycomms.util.Constants.IS_APP_CRASHED_EXTRA));
        Assert.assertFalse(activity.getIntent().hasExtra(com.vodafone.mycomms.util.Constants.APP_CRASH_MESSAGE));
    }



    @Test
    public void testCheckVersionFailedWithNullResponse() throws Exception
    {
        String serverUrl = startWebMockServer()+"/";
        PowerMockito.mockStatic(EndpointWrapper.class);
        PowerMockito.when(EndpointWrapper.getBaseURL()).thenReturn(serverUrl);
        webServer.enqueue(new MockResponse().setResponseCode(9999));
        activity = Robolectric.setupActivity(SplashScreenActivity.class);
        Thread.sleep(5000);
        Robolectric.flushForegroundThreadScheduler();
        Intent expectedIntent = new Intent(activity, LoginSignupActivity.class);
        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (LoginSignupActivity.class.getName()));

    }

    @Test
    public void testCheckVersionFailedWithResponse500() throws Exception
    {
        String serverUrl = startWebMockServer();
        PowerMockito.mockStatic(EndpointWrapper.class);
        PowerMockito.when(EndpointWrapper.getBaseURL()).thenReturn(serverUrl);
        webServer.enqueue(new MockResponse().setResponseCode(500));
        activity = Robolectric.setupActivity(SplashScreenActivity.class);
        Thread.sleep(5000);
        Robolectric.flushForegroundThreadScheduler();
        Intent expectedIntent = new Intent(activity, LoginSignupActivity.class);
        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (LoginSignupActivity.class.getName()));

    }

    @Test
    public void testCheckVersionNoNetworkConnectionUserLoggedOk() throws Exception
    {
        Context context = RuntimeEnvironment.application.getApplicationContext();
        ConnectivityManager connMgr =
                (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(false);
        UserSecurity.setTokens(
                Constants.ACCESS_TOKEN, Constants.REFRESH_TOKEN, Constants.EXPIRES_IN, RuntimeEnvironment.application);

        //Save fake profile
        SharedPreferences sp = context.getSharedPreferences(
                com.vodafone.mycomms.util.Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(
                com.vodafone.mycomms.util.Constants.PROFILE_ID_SHARED_PREF, "mc_555a0792121ef1695cc7c1c3").commit();

        activity = Robolectric.setupActivity(SplashScreenActivity.class);
        Realm.getDefaultInstance();
        Assert.assertTrue(activity.isFinishing());
        Intent expectedIntent = new Intent(activity, DashBoardActivity.class);
        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (DashBoardActivity.class.getName()));
    }

    @Test
    public void testCheckVersionNoNetworkConnectionUserNotLogged() throws Exception {
        Context context = RuntimeEnvironment.application.getApplicationContext();
        UserSecurity.resetTokens(context);
        ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(false);
        activity = Robolectric.setupActivity(SplashScreenActivity.class);
        Assert.assertTrue(activity.isFinishing());
        Intent expectedIntent = new Intent(activity, LoginSignupActivity.class);
        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (LoginSignupActivity.class.getName()));
    }

    @Test
    public void testCheckVersionUserLoggedRenewTokenToLoginSignup() throws Exception
    {
        String serverUrl = startWebMockServer();
        PowerMockito.mockStatic(EndpointWrapper.class);
        UserSecurity.setTokens(Constants.ACCESS_TOKEN, Constants.REFRESH_TOKEN, 0, RuntimeEnvironment.application);
        PowerMockito.when(EndpointWrapper.getBaseURL()).thenReturn(serverUrl);
        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(Constants.VALID_VERSION_RESPONSE));
        webServer.enqueue(new MockResponse().setResponseCode(500).setBody(Constants.VALID_VERSION_RESPONSE));
        activity = Robolectric.setupActivity(SplashScreenActivity.class);
        Thread.sleep(5000);
        Robolectric.flushForegroundThreadScheduler();
        Intent expectedIntent = new Intent(activity, LoginSignupActivity.class);
        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (LoginSignupActivity.class.getName()));
    }

    @Test
    public void testInvalidVersionResponseWithUpdateButtonClicked() throws Exception
    {
        String serverUrl = startWebMockServer();
        PowerMockito.mockStatic(EndpointWrapper.class);

        RobolectricPackageManager rpm = (RobolectricPackageManager)Shadows.shadowOf(RuntimeEnvironment.application).getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
        ResolveInfo info = new ResolveInfo();
        info.isDefault = true;
        rpm.addResolveInfoForIntent(intent, info);
        PowerMockito.when(EndpointWrapper.getBaseURL()).thenReturn(serverUrl);
        webServer.enqueue(new MockResponse().setResponseCode(400).setBody(Constants.VALID_VERSION_RESPONSE));
        activity = Robolectric.setupActivity(SplashScreenActivity.class);
        Robolectric.flushForegroundThreadScheduler();
        Thread.sleep(5000);
        Robolectric.flushForegroundThreadScheduler();
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

    @Test
    public void testInvalidVersionResponseWithSupportButtonClicked() throws Exception
    {
        String serverUrl = startWebMockServer();
        PowerMockito.mockStatic(EndpointWrapper.class);

        RobolectricPackageManager rpm = (RobolectricPackageManager)Shadows.shadowOf(RuntimeEnvironment.application).getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
        ResolveInfo info = new ResolveInfo();
        info.isDefault = true;
        rpm.addResolveInfoForIntent(intent, info);
        PowerMockito.when(EndpointWrapper.getBaseURL()).thenReturn(serverUrl);
        webServer.enqueue(new MockResponse().setResponseCode(400).setBody(Constants.VALID_VERSION_RESPONSE));
        activity = Robolectric.setupActivity(SplashScreenActivity.class);
        Robolectric.flushForegroundThreadScheduler();
        Thread.sleep(5000);
        Robolectric.flushForegroundThreadScheduler();
        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog sAlert = Shadows.shadowOf(alert);
        Assert.assertTrue(sAlert.getTitle().toString().equals(activity.getString(R.string.new_version_available)));

        Button supportButton = alert.getButton(AlertDialog.BUTTON_NEGATIVE);
        supportButton.performClick();
        Assert.assertTrue(!alert.isShowing());
    }

    private String startWebMockServer() throws Exception {
        //OkHttp mocked web server
        webServer = new MockWebServer();
        webServer.useHttps(null, false);

        //Connect OkHttp calls with MockWebServer
        webServer.start();
        String serverUrl = webServer.getUrl("").toString();

        return serverUrl;
    }

//    @Test
//    public void testEmailLink() throws Exception {
//        //String link = "<a href=\"intent://user/refreshToken/RaHZJLVyVc7ZxyDEJsTZcLpXVxmPnUKzHJ3cofn2HYyTYV0B9wQyCVPsNZuVWRKrtTVen_KnG7mTa_vYKFM4TEv4AIMMYeTcJXvCPQnDAPdaui1dqprrPYxVpCYlqVxOwpdbkx_wwPT7BuxYpfvlG9oirrdxhvB0jQGwnZnrseo/#Intent;scheme=mycomms-i;end\">Link to MyComms</a>";
//        Uri uri = Uri.parse("intent://user/refreshToken/RaHZJLVyVc7ZxyDEJsTZcLpXVxmPnUKzHJ3cofn2HYyTYV0B9wQyCVPsNZuVWRKrtTVen_KnG7mTa_vYKFM4TEv4AIMMYeTcJXvCPQnDAPdaui1dqprrPYxVpCYlqVxOwpdbkx_wwPT7BuxYpfvlG9oirrdxhvB0jQGwnZnrseo/#Intent;scheme=mycomms-i;end\\");
//        Intent incomingIntent = new Intent();
//        incomingIntent.setData(uri);
//        activity = Robolectric.buildActivity(OAuthActivity.class).withIntent(incomingIntent).create().get();
//        UserSecurity.setTokens(ACCESS_TOKEN, REFRESH_TOKEN, EXPIRES_IN, RuntimeEnvironment.application);
//        HttpResponse httpResponse = Util.buildResponse(200, LOGIN_OK_RESPONSE);
//        FakeHttp.addPendingHttpResponse(httpResponse);
//        activity = Robolectric.setupActivity(SplashScreenActivity.class);
//        Assert.assertTrue(activity.isFinishing());
//        Intent expectedIntent = new Intent(activity, DashBoardActivity.class);
//        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
//    }

}