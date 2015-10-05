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
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;

import org.apache.http.HttpResponse;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.MockRepository;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.httpclient.FakeHttp;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutionException;

import io.realm.Realm;

import static com.vodafone.mycomms.constants.Constants.LOGIN_OK_RESPONSE;
import static com.vodafone.mycomms.constants.Constants.LOGIN_USER_NOT_FOUND_RESPONSE;
import static com.vodafone.mycomms.constants.Constants.PASSWORD;
import static com.vodafone.mycomms.constants.Constants.VALID_EMAIL;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 18)
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
    public void setUp() throws Exception
    {
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());

        activity = Robolectric.buildActivity(LoginActivity.class).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();
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
        MockDataForTests.checkThreadSchedulers();
        Robolectric.reset();
        activity = null;
        btLoginSalesforce = null;
        btLogin = null;
        tvForgotPass = null;
        etEmail = null;
        etPassword = null;
        ivBack = null;
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
                System.err.println("Uncaught exception at LoginActivityTest: \n" + writer.toString());
            }
        });
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        Thread.currentThread().interrupt();
    }

    @Test
    public void testOnActivityResult()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        this.activity.onActivityResult(1, Activity.RESULT_OK,null);
        AlertDialog dialog = (AlertDialog)ShadowDialog.getLatestDialog();
        Assert.assertNotNull(dialog);
        Assert.assertTrue(dialog.isShowing());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

//    @Test
//    public void testLoginOk() throws Exception
//    {
//        try {
//            HttpResponse httpResponse = Util.buildResponse(204, LOGIN_OK_RESPONSE);
//            FakeHttp.addPendingHttpResponse(httpResponse);
//        }
//        catch (Exception e)
//        {
//            Assert.fail();
//        }
//
//        etEmail.setText(VALID_EMAIL);
//        etPassword.setText(PASSWORD);
//        btLogin.performClick();
//        MockDataForTests.checkThreadSchedulers();
//        Intent expectedIntent = new Intent(activity, DashBoardActivity.class);
//        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
//        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (DashBoardActivity.class.getName()));
//
//        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
//                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
//    }

    @Test
    public void testLoginError() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

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
        MockDataForTests.checkThreadSchedulers();
        etPassword.setText("changed_pwd");
        Assert.assertTrue(btLogin.getText().equals(activity.getString(R.string.login)));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testLoginConnectionError() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        Context context = RuntimeEnvironment.application.getApplicationContext();
        ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
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
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(btLogin.getText().equals(activity.getString(R.string.connection_error)));
        etPassword.setText("changed_pwd");
        Assert.assertTrue(btLogin.getText().equals(activity.getString(R.string.login)));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testForgotPass() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        tvForgotPass.performClick();
        MockDataForTests.checkThreadSchedulers();
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(ForgotPassActivity.class.getName()));
        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testLoginSalesforce() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        btLoginSalesforce.performClick();
        MockDataForTests.checkThreadSchedulers();
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(OAuthActivity.class.getName()));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testApplicationAndProfileInitializedBusEvent() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        BusProvider.getInstance().post(new ApplicationAndProfileInitialized());
        MockDataForTests.checkThreadSchedulers();
        Intent expectedIntent = new Intent(activity, DashBoardActivity.class);
        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (DashBoardActivity.class.getName()));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testProfileConnectionErrorBusEvent() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        BusProvider.getInstance().post(new ApplicationAndProfileReadError());
        MockDataForTests.checkThreadSchedulers();
        Intent expectedIntent = new Intent(activity, DashBoardActivity.class);
        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (DashBoardActivity.class.getName()));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testBack() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        ivBack.performClick();
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(activity.isFinishing());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testFinish() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        Activity activity = Robolectric.buildActivity(LoginActivity.class).create().start().resume().pause().stop().destroy().get();
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(activity.isDestroyed());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }
}