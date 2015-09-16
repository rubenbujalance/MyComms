package com.vodafone.mycomms.login;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.custom.ClearableEditText;
import com.vodafone.mycomms.test.util.Util;

import org.apache.http.HttpResponse;
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
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.httpclient.FakeHttp;

import static com.vodafone.mycomms.constants.Constants.INVALID_EMAIL;
import static com.vodafone.mycomms.constants.Constants.INVALID_VERSION_RESPONSE;
import static com.vodafone.mycomms.constants.Constants.USER_ALREADY_EXISTS_RESPONSE;
import static com.vodafone.mycomms.constants.Constants.USER_DOMAIN_NOT_ALLOWED_RESPONSE;
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
public class SignupMailActivityTest {

    Activity activity;
    ClearableEditText etEmail;
    Drawable errorIcon;
    ImageView ivBtFwd;
    ImageView ivBtBack;

    @Before
    public void setUp()
    {
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());

        activity = Robolectric.buildActivity(SignupMailActivity.class).create().start().resume().get();
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            Assert.fail();
        }
        Robolectric.flushForegroundThreadScheduler();

        etEmail = (ClearableEditText)activity.findViewById(R.id.etSignupEmail);
        ivBtFwd = (ImageView)activity.findViewById(R.id.ivBtForward);
        ivBtBack = (ImageView)activity.findViewById(R.id.ivBtBack);
    }

    @Test
    public void testForwardEmptyEmail() {
        etEmail.setText("");
        ivBtFwd.performClick();
        EditText innerEtEmail = (EditText)etEmail.findViewById(R.id.clearable_edit);
        Assert.assertTrue(innerEtEmail.getError().equals(activity.getString(R.string.enter_your_email_to_continue)));
    }

    @Test
    public void testForwardIncorrectFormatEmail() {
        etEmail.setText(INVALID_EMAIL);
        ivBtFwd.performClick();
        EditText innerEtEmail = (EditText)etEmail.findViewById(R.id.clearable_edit);
        Assert.assertTrue(innerEtEmail.getError().equals(activity.getString(R.string.incorrect_format)));
    }

   @Test
    public void testForwardToSignupName() throws Exception
   {
        HttpResponse httpResponse = Util.buildResponse(204);
        FakeHttp.addPendingHttpResponse(httpResponse);
        etEmail.setText(VALID_EMAIL);
        ivBtFwd.performClick();

       ShadowActivity shadowActivity = Shadows.shadowOf(activity);
       Intent startedIntent = shadowActivity.getNextStartedActivity();
       ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
       Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(SignupNameActivity.class.getName()));
    }

    @Test
    public void testForwardUserAlreadyExists() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(403, USER_ALREADY_EXISTS_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        etEmail.setText(VALID_EMAIL);
        ivBtFwd.performClick();
        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog sAlert = Shadows.shadowOf(alert);
        View view = sAlert.getCustomTitleView();
        String title = ((TextView)view.findViewById(R.id.tvTitle)).getText().toString();
        Assert.assertTrue(title.equals(activity.getString(R.string.user_already_exists)));
        Button okButton = alert.getButton(AlertDialog.BUTTON_NEUTRAL);
        okButton.performClick();
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(LoginActivity.class.getName()));
    }

    @Test
    public void testForwardUserDomainNotAlowed() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(400, USER_DOMAIN_NOT_ALLOWED_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        etEmail.setText(VALID_EMAIL);
        ivBtFwd.performClick();
        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog sAlert = Shadows.shadowOf(alert);
        View view = sAlert.getCustomTitleView();
        String title = ((TextView)view.findViewById(R.id.tvTitle)).getText().toString();
        Assert.assertTrue(title.equals(activity.getString(R.string.uh_oh)));
        Button okButton = alert.getButton(AlertDialog.BUTTON_NEUTRAL);
        okButton.performClick();
    }

    @Test
    public void testForwardInvalidVersion() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(400, INVALID_VERSION_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        etEmail.setText(VALID_EMAIL);
        ivBtFwd.performClick();
        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog sAlert = Shadows.shadowOf(alert);
        View view = sAlert.getCustomTitleView();
        String title = ((TextView)view.findViewById(R.id.tvTitle)).getText().toString();
        Assert.assertTrue(title.equals(activity.getString(R.string.new_version_available)));
        Button okButton = alert.getButton(AlertDialog.BUTTON_NEUTRAL);
        okButton.performClick();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testFinish() throws Exception {
        Activity activity = Robolectric.buildActivity(SignupMailActivity.class).create().start().resume().pause().stop().destroy().get();
        Assert.assertTrue(activity.isDestroyed());
    }

}