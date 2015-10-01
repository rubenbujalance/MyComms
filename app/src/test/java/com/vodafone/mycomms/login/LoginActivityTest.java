package com.vodafone.mycomms.login;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.events.ApplicationAndProfileInitialized;
import com.vodafone.mycomms.events.ApplicationAndProfileReadError;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.main.DashBoardActivity;
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
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.httpclient.FakeHttp;

import static com.vodafone.mycomms.constants.Constants.LOGIN_OK_RESPONSE;
import static com.vodafone.mycomms.constants.Constants.LOGIN_USER_NOT_FOUND_RESPONSE;
import static com.vodafone.mycomms.constants.Constants.PASSWORD;
import static com.vodafone.mycomms.constants.Constants.VALID_EMAIL;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*"})
@PrepareForTest({Crashlytics.class})
public class LoginActivityTest {

    LoginActivity activity;
    Button btLoginSalesforce;
    Button btLogin;
    TextView tvForgotPass;
    EditText etEmail;
    EditText etPassword;
    ImageView ivBack;

    @Before
    public void setUp()
    {
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());

        activity = Robolectric.buildActivity(LoginActivity.class).create().start().resume().get();
        try {
            Thread.sleep(2000);
        }
        catch (Exception e)
        {
            Assert.fail();
        }
        btLogin = (Button) activity.findViewById(R.id.btLogin);
        btLoginSalesforce = (Button) activity.findViewById(R.id.btLoginSalesforce);
        tvForgotPass = (TextView) activity.findViewById(R.id.tvForgotPass);
        etEmail = (EditText) activity.findViewById(R.id.etEmail);
        etPassword = (EditText) activity.findViewById(R.id.etPassword);
        ivBack = (ImageView) activity.findViewById(R.id.ivBack);
    }

    @After
    public void tearDown() throws Exception
    {
        //Try to shutdown server if it was started
        try {
            Robolectric.reset();
        } catch (Exception e) {}

        activity = null;
        btLoginSalesforce = null;
        btLogin = null;
        tvForgotPass = null;
        etEmail = null;
        etPassword = null;
        ivBack = null;
        System.gc();
    }

    @Test
    public void testOnActivityResult()
    {
        this.activity.onActivityResult(1, Activity.RESULT_OK,null);
        AlertDialog dialog = (AlertDialog)ShadowDialog.getLatestDialog();
        Assert.assertNotNull(dialog);
        Assert.assertTrue(dialog.isShowing());
    }

    @Test
    public void testLoginOk()
    {
        try {
            HttpResponse httpResponse = Util.buildResponse(204, LOGIN_OK_RESPONSE);
            FakeHttp.addPendingHttpResponse(httpResponse);
        }
        catch (Exception e)
        {
            Assert.fail();
        }

        etEmail.setText(VALID_EMAIL);
        etPassword.setText(PASSWORD);
        btLogin.performClick();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Robolectric.flushForegroundThreadScheduler();
        Intent expectedIntent = new Intent(activity, DashBoardActivity.class);
        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (DashBoardActivity.class.getName()));
    }

    @Test
    public void testLoginError()
    {
        try {
            HttpResponse httpResponse = Util.buildResponse(409, LOGIN_USER_NOT_FOUND_RESPONSE);
            FakeHttp.addPendingHttpResponse(httpResponse);
        }
        catch (Exception e)
        {
            Assert.fail();
        }
        etEmail.setText(VALID_EMAIL);
        etPassword.setText(PASSWORD);
        btLogin.performClick();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Robolectric.flushForegroundThreadScheduler();
        etPassword.setText("changed_pwd");
        Assert.assertTrue(btLogin.getText().equals(activity.getString(R.string.login)));
    }

    @Test
    public void testLoginConnectionError(){
        Context context = RuntimeEnvironment.application.getApplicationContext();
        ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(false);
        try {
            HttpResponse httpResponse = Util.buildResponse(204, LOGIN_OK_RESPONSE);
            FakeHttp.addPendingHttpResponse(httpResponse);
        }
        catch (Exception e)
        {
            Assert.fail();
        }
        etEmail.setText(VALID_EMAIL);
        etPassword.setText(PASSWORD);
        btLogin.performClick();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Robolectric.flushForegroundThreadScheduler();
        Assert.assertTrue(btLogin.getText().equals(activity.getString(R.string.connection_error)));
        etPassword.setText("changed_pwd");
        Assert.assertTrue(btLogin.getText().equals(activity.getString(R.string.login)));
    }

    @Test
    public void testForgotPass(){
        tvForgotPass.performClick();
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(ForgotPassActivity.class.getName()));
    }

    @Test
    public void testLoginSalesforce(){
        btLoginSalesforce.performClick();
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(OAuthActivity.class.getName()));
    }

    @Test
    public void testApplicationAndProfileInitializedBusEvent() {
        BusProvider.getInstance().post(new ApplicationAndProfileInitialized());
        Intent expectedIntent = new Intent(activity, DashBoardActivity.class);
        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (DashBoardActivity.class.getName()));
    }

    @Test
    public void testProfileConnectionErrorBusEvent() {
        BusProvider.getInstance().post(new ApplicationAndProfileReadError());
        Intent expectedIntent = new Intent(activity, DashBoardActivity.class);
        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (DashBoardActivity.class.getName()));
    }

    @Test
    public void testBack(){
        ivBack.performClick();
        Assert.assertTrue(activity.isFinishing());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testFinish(){
        Activity activity = Robolectric.buildActivity(LoginActivity.class).create().start().resume().pause().stop().destroy().get();
        Assert.assertTrue(activity.isDestroyed());
    }
}