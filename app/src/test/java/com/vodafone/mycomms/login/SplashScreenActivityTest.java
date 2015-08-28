package com.vodafone.mycomms.login;

import android.app.Activity;
import android.content.Intent;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.constants.Constants;
import com.vodafone.mycomms.main.SplashScreenActivity;
import com.vodafone.mycomms.test.util.Util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.MockRepository;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import io.realm.Realm;

import static org.mockito.Mockito.mockingDetails;
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
@PrepareForTest({Realm.class, Crashlytics.class})
public class SplashScreenActivityTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    Activity activity;
    String mCrashMessage = "mockCrashMessage";

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
        intent.putExtra(com.vodafone.mycomms.util.Constants.IS_APP_CRASHED_EXTRA, false);
        intent.putExtra(com.vodafone.mycomms.util.Constants.APP_CRASH_MESSAGE, mCrashMessage);
        Activity activity = Robolectric.buildActivity(SplashScreenActivity.class).withIntent(intent).create().get();
        Assert.assertTrue(activity.getIntent().hasExtra(com.vodafone.mycomms.util.Constants.IS_APP_CRASHED_EXTRA));
        Assert.assertTrue(activity.getIntent().hasExtra(com.vodafone.mycomms.util.Constants.APP_CRASH_MESSAGE));
        if(activity.getIntent().hasExtra(com.vodafone.mycomms.util.Constants.APP_CRASH_MESSAGE))
        {
            Assert.assertEquals(activity.getIntent().getStringExtra(com.vodafone.mycomms.util.Constants.APP_CRASH_MESSAGE)
                    ,mCrashMessage);
        }
    }

////    @Test
////    public void testCheckVersionUserLoggedOk() throws Exception {
////        UserSecurity.setTokens(ACCESS_TOKEN, REFRESH_TOKEN, EXPIRES_IN, RuntimeEnvironment.application);
////        HttpResponse httpResponse = Util.buildResponse(204, VALID_VERSION_RESPONSE);
////        FakeHttp.addPendingHttpResponse(httpResponse);
////        activity = Robolectric.setupActivity(SplashScreenActivity.class);
////        Assert.assertTrue(activity.isFinishing());
////        Intent expectedIntent = new Intent(activity, DashBoardActivity.class);
////        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
////    }
//
//    @Test
//    public void testCheckVersionNoNetworkConnectionUserLoggedOk() throws Exception {
//        Context context = RuntimeEnvironment.application.getApplicationContext();
//        ConnectivityManager connMgr =
//                (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
//        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(false);
//        UserSecurity.setTokens(
//                ACCESS_TOKEN, REFRESH_TOKEN, EXPIRES_IN, RuntimeEnvironment.application);
//
//        //Save fake profile
//        SharedPreferences sp = context.getSharedPreferences(
//                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
//        sp.edit().putString(
//                Constants.PROFILE_ID_SHARED_PREF,"mc_555a0792121ef1695cc7c1c3").commit();
//
//        //HttpResponse httpResponse = Util.buildResponse(204, VALID_VERSION_RESPONSE);
//        //FakeHttp.addPendingHttpResponse(httpResponse);
//        activity = Robolectric.setupActivity(SplashScreenActivity.class);
//        Realm.getDefaultInstance();
//        Assert.assertTrue(activity.isFinishing());
//        Intent expectedIntent = new Intent(activity, DashBoardActivity.class);
//        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
//        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (DashBoardActivity.class.getName()));
//    }
//
//    @Test
//    public void testCheckVersionNoNetworkConnectionUserNotLogged() throws Exception {
//        Context context = RuntimeEnvironment.application.getApplicationContext();
//        UserSecurity.resetTokens(context);
//        ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
//        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(false);
////        HttpResponse httpResponse = Util.buildResponse(204, VALID_VERSION_RESPONSE);
////        FakeHttp.addPendingHttpResponse(httpResponse);
//        activity = Robolectric.setupActivity(SplashScreenActivity.class);
//        Assert.assertTrue(activity.isFinishing());
//        Intent expectedIntent = new Intent(activity, LoginSignupActivity.class);
//        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
//        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (LoginSignupActivity.class.getName()));
//    }
//
////    @Test
////    public void testCheckVersionUserLoggedRenewTokenOk() throws Exception {
////        UserSecurity.setTokens(ACCESS_TOKEN, REFRESH_TOKEN, 0, RuntimeEnvironment.application);
////        HttpResponse httpResponse = Util.buildResponse(204, VALID_VERSION_RESPONSE);
////        FakeHttp.addPendingHttpResponse(httpResponse);
////        HttpResponse httpResponseRenewToken = Util.buildResponse(200, LOGIN_OK_RESPONSE);
////        FakeHttp.addPendingHttpResponse(httpResponseRenewToken);
////        activity = Robolectric.setupActivity(SplashScreenActivity.class);
////        Assert.assertTrue(activity.isFinishing());
////        Intent expectedIntent = new Intent(activity, DashBoardActivity.class);
////        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
////    }
//
//    @Test
//    public void testCheckVersionUserLoggedRenewTokenToLoginSignup() throws Exception {
//        UserSecurity.setTokens(ACCESS_TOKEN, REFRESH_TOKEN, 0, RuntimeEnvironment.application);
////        HttpResponse httpResponse = Util.buildResponse(204, VALID_VERSION_RESPONSE);
////        FakeHttp.addPendingHttpResponse(httpResponse);
////        HttpResponse httpResponseRenewToken = Util.buildResponse(500, VALID_VERSION_RESPONSE);
////        FakeHttp.addPendingHttpResponse(httpResponseRenewToken);
//        activity = Robolectric.setupActivity(SplashScreenActivity.class);
//        Assert.assertTrue(activity.isFinishing());
//        Intent expectedIntent = new Intent(activity, LoginSignupActivity.class);
//        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
//        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (LoginSignupActivity.class.getName()));
//    }
//
//    @Test
//    public void testInvalidVersionResponse() throws Exception {
//        RobolectricPackageManager rpm = (RobolectricPackageManager)Shadows.shadowOf(RuntimeEnvironment.application).getPackageManager();
//        Intent intent = new Intent(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//        intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
//        ResolveInfo info = new ResolveInfo();
//        info.isDefault = true;
//        rpm.addResolveInfoForIntent(intent, info);
////        HttpResponse httpResponse = Util.buildResponse(400, INVALID_VERSION_RESPONSE);
////        FakeHttp.addPendingHttpResponse(httpResponse);
//        activity = Robolectric.setupActivity(SplashScreenActivity.class);
//        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
//        ShadowAlertDialog sAlert = Shadows.shadowOf(alert);
//        Assert.assertTrue(sAlert.getTitle().toString().equals(activity.getString(R.string.new_version_available)));
//        Button updateButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
//        updateButton.performClick();
//        alert = ShadowAlertDialog.getLatestAlertDialog();
//        sAlert = Shadows.shadowOf(alert);
//        Assert.assertTrue(sAlert.getTitle().toString().equals(activity.getString(R.string.update2)));
//        Button okButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
//        okButton.performClick();
//        Assert.assertTrue(activity.isFinishing());
//    }
//
////    @Test
////    public void testEmailLink() throws Exception {
////        //String link = "<a href=\"intent://user/refreshToken/RaHZJLVyVc7ZxyDEJsTZcLpXVxmPnUKzHJ3cofn2HYyTYV0B9wQyCVPsNZuVWRKrtTVen_KnG7mTa_vYKFM4TEv4AIMMYeTcJXvCPQnDAPdaui1dqprrPYxVpCYlqVxOwpdbkx_wwPT7BuxYpfvlG9oirrdxhvB0jQGwnZnrseo/#Intent;scheme=mycomms-i;end\">Link to MyComms</a>";
////        Uri uri = Uri.parse("intent://user/refreshToken/RaHZJLVyVc7ZxyDEJsTZcLpXVxmPnUKzHJ3cofn2HYyTYV0B9wQyCVPsNZuVWRKrtTVen_KnG7mTa_vYKFM4TEv4AIMMYeTcJXvCPQnDAPdaui1dqprrPYxVpCYlqVxOwpdbkx_wwPT7BuxYpfvlG9oirrdxhvB0jQGwnZnrseo/#Intent;scheme=mycomms-i;end\\");
////        Intent incomingIntent = new Intent();
////        incomingIntent.setData(uri);
////        activity = Robolectric.buildActivity(OAuthActivity.class).withIntent(incomingIntent).create().get();
////        UserSecurity.setTokens(ACCESS_TOKEN, REFRESH_TOKEN, EXPIRES_IN, RuntimeEnvironment.application);
////        HttpResponse httpResponse = Util.buildResponse(200, LOGIN_OK_RESPONSE);
////        FakeHttp.addPendingHttpResponse(httpResponse);
////        activity = Robolectric.setupActivity(SplashScreenActivity.class);
////        Assert.assertTrue(activity.isFinishing());
////        Intent expectedIntent = new Intent(activity, DashBoardActivity.class);
////        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
////    }

}