package com.vodafone.mycomms.login;

import com.vodafone.mycomms.BuildConfig;

import org.robolectric.annotation.Config;

/**
 * Created by str_evc on 18/05/2015.
 */
//@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
public class SplashScreenActivityTest {
/*
    Activity activity;

    @Before
    public void setUp() throws Exception {

    }

    @Test
        public void testCheckVersionUserLoggedOk() throws Exception {
        UserSecurity.setTokens(ACCESS_TOKEN, REFRESH_TOKEN, EXPIRES_IN, RuntimeEnvironment.application);
        HttpResponse httpResponse = Util.buildResponse(204, VALID_VERSION_RESPONSE);
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
        UserSecurity.setTokens(ACCESS_TOKEN, REFRESH_TOKEN, EXPIRES_IN, RuntimeEnvironment.application);
        HttpResponse httpResponse = Util.buildResponse(204, VALID_VERSION_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        activity = Robolectric.setupActivity(SplashScreenActivity.class);
        Assert.assertTrue(activity.isFinishing());
        Intent expectedIntent = new Intent(activity, ContactListMainActivity.class);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }

    @Test
    public void testCheckVersionNoNetworkConnectionUserNotLogged() throws Exception {
        Context context = RuntimeEnvironment.application.getApplicationContext();
        UserSecurity.resetTokens(context);
        ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(false);
        HttpResponse httpResponse = Util.buildResponse(204, VALID_VERSION_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        activity = Robolectric.setupActivity(SplashScreenActivity.class);
        Assert.assertTrue(activity.isFinishing());
        Intent expectedIntent = new Intent(activity, LoginSignupActivity.class);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }

    @Test
     public void testCheckVersionUserLoggedRenewTokenOk() throws Exception {
        UserSecurity.setTokens(ACCESS_TOKEN, REFRESH_TOKEN, 0, RuntimeEnvironment.application);
        HttpResponse httpResponse = Util.buildResponse(204, VALID_VERSION_RESPONSE);
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
        UserSecurity.setTokens(ACCESS_TOKEN, REFRESH_TOKEN, 0, RuntimeEnvironment.application);
        HttpResponse httpResponse = Util.buildResponse(204, VALID_VERSION_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        HttpResponse httpResponseRenewToken = Util.buildResponse(500, VALID_VERSION_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponseRenewToken);
        activity = Robolectric.setupActivity(SplashScreenActivity.class);
        Assert.assertTrue(activity.isFinishing());
        Intent expectedIntent = new Intent(activity, LoginSignupActivity.class);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
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

//    @Test
    public void testEmailLink() throws Exception {
        //String link = "<a href=\"intent://user/refreshToken/RaHZJLVyVc7ZxyDEJsTZcLpXVxmPnUKzHJ3cofn2HYyTYV0B9wQyCVPsNZuVWRKrtTVen_KnG7mTa_vYKFM4TEv4AIMMYeTcJXvCPQnDAPdaui1dqprrPYxVpCYlqVxOwpdbkx_wwPT7BuxYpfvlG9oirrdxhvB0jQGwnZnrseo/#Intent;scheme=mycomms-i;end\">Link to MyComms</a>";
        Uri uri = Uri.parse("intent://user/refreshToken/RaHZJLVyVc7ZxyDEJsTZcLpXVxmPnUKzHJ3cofn2HYyTYV0B9wQyCVPsNZuVWRKrtTVen_KnG7mTa_vYKFM4TEv4AIMMYeTcJXvCPQnDAPdaui1dqprrPYxVpCYlqVxOwpdbkx_wwPT7BuxYpfvlG9oirrdxhvB0jQGwnZnrseo/#Intent;scheme=mycomms-i;end\\");
        Intent incomingIntent = new Intent();
        incomingIntent.setData(uri);
        activity = Robolectric.buildActivity(OAuthActivity.class).withIntent(incomingIntent).create().get();
        UserSecurity.setTokens(ACCESS_TOKEN, REFRESH_TOKEN, EXPIRES_IN, RuntimeEnvironment.application);
        HttpResponse httpResponse = Util.buildResponse(200, LOGIN_OK_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        activity = Robolectric.setupActivity(SplashScreenActivity.class);
        Assert.assertTrue(activity.isFinishing());
        Intent expectedIntent = new Intent(activity, ContactListMainActivity.class);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }
*/
}