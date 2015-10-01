package com.vodafone.mycomms.login;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.widget.Button;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
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
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.httpclient.FakeHttp;

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.vodafone.mycomms.constants.Constants.CHECK_PHONE_OK_RESPONSE;
import static com.vodafone.mycomms.constants.Constants.PIN;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*"})
@PrepareForTest({Crashlytics.class})
public class MailSentActivityTest {

    MailSentActivity activity;
    TextView mWeSent;
    Button mResendEmail;


    @Before
    public void setUp()
    {
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());

        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("pin", PIN);
        activity = Robolectric.buildActivity(MailSentActivity.class).withIntent(intent).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();
        mWeSent = activity.mWeSent;
        mResendEmail = activity.mResendEmail;
    }

    @After
    public void tearDown() throws Exception
    {
        MockDataForTests.checkThreadSchedulers();
        Robolectric.reset();

        activity = null;
        mWeSent = null;
        mResendEmail = null;
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
                System.err.println("Uncaught exception at MailSentActivityTest: \n" + writer.toString());
            }
        });
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        Thread.currentThread().interrupt();
    }

    @Test
    public void testCheckMessage() {
        Assert.assertTrue(mWeSent.getText().toString().startsWith(activity.getString(R.string.we_sent_an_email_to)));
    }

    @Test
    public void testResendEmail() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(200, CHECK_PHONE_OK_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        MockDataForTests.checkThreadSchedulers();
        mResendEmail.performClick();
        MockDataForTests.checkThreadSchedulers();
        HttpPost latestSentHttpPost = (HttpPost)FakeHttp.getLatestSentHttpRequest();
        Header header = latestSentHttpPost.getFirstHeader("x-otp-pin");
        Assert.assertNotNull(header);
        Assert.assertTrue(header.getValue().equals(PIN));
        Assert.assertTrue("/api/profile".equals(latestSentHttpPost.getURI().getPath()));
    }

    @Test
    public void shouldCallFinishInOnBackPressed() {
        activity.onBackPressed();
        MockDataForTests.checkThreadSchedulers();
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Assert.assertTrue(shadowActivity.isFinishing());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testFinish() throws Exception {
        Activity activity = Robolectric.buildActivity(MailSentActivity.class).create().start().resume().pause().stop().destroy().get();
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(activity.isDestroyed());
    }
}