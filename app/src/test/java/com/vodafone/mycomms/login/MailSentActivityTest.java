package com.vodafone.mycomms.login;

import com.vodafone.mycomms.BuildConfig;

import org.robolectric.annotation.Config;

/**
 * Created by str_evc on 18/05/2015.
 */
//@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
public class MailSentActivityTest {
/*
    MailSentActivity activity;
    TextView mWeSent;
    Button mResendEmail;


    @Before
      public void setUp() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("pin",PIN);
        activity = Robolectric.buildActivity(MailSentActivity.class).withIntent(intent).create().get();
        mWeSent = activity.mWeSent;
        mResendEmail = activity.mResendEmail;
    }

    @Test
     public void testCheckMessage() {
        Assert.assertTrue(mWeSent.getText().toString().startsWith(activity.getString(R.string.we_sent_an_email_to)));
    }

    @Test
     public void testResendEmail() throws Exception {
        HttpResponse httpResponse = Util.buildResponse(200, CHECK_PHONE_OK_RESPONSE);
        FakeHttp.addPendingHttpResponse(httpResponse);
        mResendEmail.performClick();
        HttpPost latestSentHttpPost = (HttpPost)FakeHttp.getLatestSentHttpRequest();
        Header header = latestSentHttpPost.getFirstHeader("x-otp-pin");
        Assert.assertNotNull(header);
        Assert.assertTrue(header.getValue().equals(PIN));
        Assert.assertTrue("/api/profile".equals(latestSentHttpPost.getURI().getPath()));
    }

    //@Test
    public void testBack() throws Exception {
        KeyEvent keyEvent = new KeyEvent(0,0,KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK, 0, 0);
        activity.dispatchKeyEvent(keyEvent);
        Intent expectedIntent = new Intent(activity, LoginSignupActivity.class);
        expectedIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }
*/
}