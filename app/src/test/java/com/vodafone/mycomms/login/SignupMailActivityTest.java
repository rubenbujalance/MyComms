package com.vodafone.mycomms.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.custom.ClearableEditText;
import com.vodafone.mycomms.test.util.Util;

import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.httpclient.FakeHttp;

import static com.vodafone.mycomms.constants.Constants.INVALID_EMAIL;
import static com.vodafone.mycomms.constants.Constants.INVALID_VERSION_RESPONSE;
import static com.vodafone.mycomms.constants.Constants.USER_ALREADY_EXISTS_RESPONSE;
import static com.vodafone.mycomms.constants.Constants.USER_DOMAIN_NOT_ALLOWED_RESPONSE;
import static com.vodafone.mycomms.constants.Constants.VALID_EMAIL;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
public class SignupMailActivityTest {

    Activity activity;
    ClearableEditText etEmail;
    Drawable errorIcon;
    ImageView ivBtFwd;
    ImageView ivBtBack;

    @Before
    public void setUp() {
        activity = Robolectric.setupActivity(SignupMailActivity.class);
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
    public void testForwardToSignupName() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(204);
        FakeHttp.addPendingHttpResponse(httpResponse);
        etEmail.setText(VALID_EMAIL);
        ivBtFwd.performClick();
        Intent expectedIntent = new Intent(activity, SignupNameActivity.class);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
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
        Intent expectedIntent = new Intent(activity, LoginActivity.class);
        expectedIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        expectedIntent.putExtra("email", etEmail.getText().toString());
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
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

}