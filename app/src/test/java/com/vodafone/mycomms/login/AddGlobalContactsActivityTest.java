package com.vodafone.mycomms.login;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.okhttp.Response;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.settings.globalcontacts.AddGlobalContactsActivity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "/app/src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
@PrepareForTest(Response.class)
public class AddGlobalContactsActivityTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    Activity activity;
    Button btAddAccount;
    EditText etUser;
    EditText etPassword;
    LinearLayout layoutErrorBar;
    TextView tvError;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.setupActivity(AddGlobalContactsActivity.class);
        etUser = (EditText)activity.findViewById(R.id.etEmail);
        etPassword = (EditText)activity.findViewById(R.id.etPassword);
        btAddAccount = (Button)activity.findViewById(R.id.btAddAccount);
        layoutErrorBar = (LinearLayout)activity.findViewById(R.id.layoutErrorBar);
        tvError = (TextView)activity.findViewById(R.id.tvError);
    }

    @Test
    public void testSendFormatErrorAndReset() throws Exception {
        //User empty
        etPassword.setText("12345");
        etUser.setText("");
        btAddAccount.performClick();
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        Assert.assertEquals(tvError.getText().toString(),
                activity.getString(R.string.credentials_are_incorrect));
        //Change user
        etUser.setText("userChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        //Email empty
        etPassword.setText("");
        etUser.setText("testUser");
        btAddAccount.performClick();
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        Assert.assertEquals(tvError.getText().toString(),
                activity.getString(R.string.credentials_are_incorrect));
        //Change password
        etPassword.setText("passChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);
    }

    @Test
    public void testHttpResponseNotExpected() throws Exception {
        //Discover response !=401
        etUser.setText("testUser");
        etPassword.setText("testPassword");

        Response res = PowerMockito.mock(Response.class);
        Mockito.when(res.code()).thenReturn(500);

//        Response httpResponse = Util.buildResponse(500);
//        FakeHttp.addPendingHttpResponse(httpResponse);
        btAddAccount.performClick();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Robolectric.flushForegroundThreadScheduler();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        etUser.setText("testUserChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        //User URL error

        //Auth error
    }

//    @Test
//    public void testHttpResponseOk() throws Exception {
//        //Valid e-mail
//        etEmail.setText(VALID_EMAIL);
//        btSend.performClick();
//        Assert.assertEquals(activity.getString(R.string.send_new_password), btSend.getText());
//        Assert.assertTrue(activity.isFinishing());
//    }
}
