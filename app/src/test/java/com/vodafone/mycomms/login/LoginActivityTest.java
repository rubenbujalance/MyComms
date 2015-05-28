package com.vodafone.mycomms.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.ContactListMainActivity;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.main.SplashScreenActivity;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.Constants;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.FakeHttp;
import org.robolectric.shadows.ShadowAlertDialog;

import static com.vodafone.mycomms.constants.Constants.*;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
public class LoginActivityTest {

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
        Assert.assertTrue(btLogin.getText().equals(activity.getString(R.string.oops_wrong_password)));
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

}
