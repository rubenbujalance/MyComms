package com.vodafone.mycomms.login;

import com.vodafone.mycomms.BuildConfig;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
public class SignupPincodeActivityTest {
/*
    SignupPincodeActivity activity;
    EditText etPin;
    TextView tvPinPhoneNumber;
    Button btResendPin;
    ImageView ivBtFwd;
    ImageView ivBtBack;
    View lnPin1;

    @Before
      public void setUp() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(200, USER_PHONE_NOT_VERIFIED_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        activity = Robolectric.setupActivity(SignupPincodeActivity.class);
        Shadows.shadowOf(activity).getNextStartedActivity();
        ivBtFwd = (ImageView)activity.findViewById(R.id.ivBtForward);
        ivBtBack = (ImageView)activity.findViewById(R.id.ivBtBack);
        etPin = activity.etPin;
        tvPinPhoneNumber = activity.tvPinPhoneNumber;
        btResendPin = activity.btResendPin;
        lnPin1 = activity.lnPin1;
    }

    @Test
    public void testSendPincode() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(200, CHECK_PHONE_OK_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        KeyEvent keyEvent = new KeyEvent(0,0,KeyEvent.ACTION_UP, KeyEvent.KEYCODE_1, 0, 0);
        etPin.dispatchKeyEvent(keyEvent);
        etPin.dispatchKeyEvent(keyEvent);
        etPin.dispatchKeyEvent(keyEvent);
        etPin.dispatchKeyEvent(keyEvent);
        Intent expectedIntent = new Intent(activity, MailSentActivity.class);
        expectedIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        expectedIntent.putExtra("pin", activity.pin);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }

    @Test
    public void testSendPincodeOAuth() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(201, LOGIN_OK_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        KeyEvent keyEvent = new KeyEvent(0,0,KeyEvent.ACTION_UP, KeyEvent.KEYCODE_1, 0, 0);
        etPin.dispatchKeyEvent(keyEvent);
        etPin.dispatchKeyEvent(keyEvent);
        etPin.dispatchKeyEvent(keyEvent);
        etPin.dispatchKeyEvent(keyEvent);
        Intent expectedIntent = new Intent(activity, ContactListMainActivity.class);
        expectedIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }

    @Test
    public void testSendPincodeInvalidResponseCode() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(400);
        FakeHttp.addPendingHttpResponse(httpResponse);
        KeyEvent keyEvent = new KeyEvent(0,0,KeyEvent.ACTION_UP, KeyEvent.KEYCODE_A, 0, 0);
        etPin.dispatchKeyEvent(keyEvent);
        etPin.dispatchKeyEvent(keyEvent);
        etPin.dispatchKeyEvent(keyEvent);
        etPin.dispatchKeyEvent(keyEvent);
        //Assert.assertTrue(lnPin1.getBackground());
    }

    @Test
    public void testBack() throws Exception {
        ImageView ivBtBack = (ImageView)activity.findViewById(R.id.ivBtBack);
        ivBtBack.performClick();
        Assert.assertTrue(activity.isFinishing());
    }
*/
}
