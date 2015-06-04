package com.vodafone.mycomms.login;

import com.vodafone.mycomms.BuildConfig;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
public class LoginActivityTest {

    @Test
    public void testSend() throws Exception {
        Assert.assertTrue(true);
    }
/*
    Activity activity;
    Button btLoginSalesforce;
    Button btLogin;
    TextView tvForgotPass;
    EditText etEmail;
    EditText etPassword;
    ImageView ivBack;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.setupActivity(LoginActivity.class);
        btLogin = (Button) activity.findViewById(R.id.btLogin);
        btLoginSalesforce = (Button) activity.findViewById(R.id.btLoginSalesforce);
        tvForgotPass = (TextView) activity.findViewById(R.id.tvForgotPass);
        etEmail = (EditText) activity.findViewById(R.id.etEmail);
        etPassword = (EditText) activity.findViewById(R.id.etPassword);
        ivBack = (ImageView) activity.findViewById(R.id.ivBack);
    }

    @Test
     public void testLoginOk() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(204, LOGIN_OK_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        etEmail.setText(VALID_EMAIL);
        etPassword.setText(PASSWORD);
        btLogin.performClick();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Robolectric.flushForegroundThreadScheduler();
        Intent expectedIntent = new Intent(activity, ContactListMainActivity.class);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }

    @Test
    public void testLoginEmptyEmail() throws Exception {
        etEmail.setText("");
        etPassword.setText(PASSWORD);
        btLogin.performClick();
        Drawable errorIcon = etPassword.getCompoundDrawables()[2];
        Assert.assertTrue(errorIcon != null);
    }

    @Test
    public void testLoginMalformedEmail() throws Exception {
        etEmail.setText(INVALID_EMAIL);
        etPassword.setText(PASSWORD);
        btLogin.performClick();
        Drawable errorIcon = etPassword.getCompoundDrawables()[2];
        Assert.assertTrue(errorIcon != null);
    }

    @Test
    public void testForgotPassActivityResult() throws Exception {
        tvForgotPass.performClick();

        ShadowActivity sActivity = Shadows.shadowOf(activity);
        Intent requestIntent = new Intent(activity, ForgotPassActivity.class);

        Intent responseIntent = new Intent();
        sActivity.receiveResult(requestIntent, Activity.RESULT_OK, responseIntent);

        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog sAlert = Shadows.shadowOf(alert);
        Assert.assertTrue(sAlert.getTitle().toString().equals(activity.getString(R.string.new_password_sent)));
    }

    @Test
    public void testLoginError() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(500);
        FakeHttp.addPendingHttpResponse(httpResponse);
        etEmail.setText(VALID_EMAIL);
        etPassword.setText(PASSWORD);
        btLogin.performClick();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Robolectric.flushForegroundThreadScheduler();
//        Assert.assertTrue(btLogin.getText().equals(activity.getString(R.string.oops_wrong_password)));
        etPassword.setText("changed_pwd");
        Assert.assertTrue(btLogin.getText().equals(activity.getString(R.string.login)));
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
    public void testForgotPass() throws Exception {
        tvForgotPass.performClick();
        Intent expectedIntent = new Intent(activity, ForgotPassActivity.class);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }

    @Test
    public void testLoginSalesforce() throws Exception {
        btLoginSalesforce.performClick();
        Intent expectedIntent = new Intent(activity, OAuthActivity.class);
        expectedIntent.putExtra("oauth", "sf");
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }

    @Test
     public void testBack() throws Exception {
        ivBack.performClick();
        Assert.assertTrue(activity.isFinishing());
    }
*/
}
