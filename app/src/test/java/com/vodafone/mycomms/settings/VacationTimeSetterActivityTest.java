package com.vodafone.mycomms.settings;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.contacts.detail.ContactDetailMainActivity;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.view.tab.MyCommsDatePickerFragment;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
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
import org.robolectric.shadows.ShadowDatePickerDialog;
import org.robolectric.shadows.ShadowDialog;

import java.util.zip.DeflaterInputStream;

import io.realm.Realm;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.support.membermodification.MemberMatcher.methodsDeclaredIn;

/**
 * Created by str_oan on 22/09/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*"})
@PrepareForTest({Realm.class, Crashlytics.class ,EndpointWrapper.class})
public class VacationTimeSetterActivityTest
{
    @Rule
    public PowerMockRule rule = new PowerMockRule();
    public VacationTimeSetterActivity mActivity;
    public Context mContext;
    public MockWebServer webServer;

    @Before
    public void setUp()
    {
        mockStatic(Realm.class);
        when(Realm.getDefaultInstance()).thenReturn(null);
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
        mContext = RuntimeEnvironment.application.getApplicationContext();
    }

    @After
    public void tearDown()
    {
        //Try to shutdown server if it was started
        try {
            if (webServer != null) webServer.shutdown();
            Robolectric.reset();
        } catch (Exception e) {}
    }

    @Test
    public void testStop()
    {
        MycommsApp.stateCounter = 0;
        mActivity = Robolectric.buildActivity(VacationTimeSetterActivity.class)
                .create().start().resume().stop().get();
        Assert.assertTrue(MycommsApp.stateCounter == 0);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testDestroy()
    {
        MycommsApp.stateCounter = 0;
        mActivity = Robolectric.buildActivity(VacationTimeSetterActivity.class)
                .create().start().resume().stop().destroy().get();
        Assert.assertTrue(MycommsApp.stateCounter == 0);
        Assert.assertTrue(mActivity.isDestroyed());
    }

    @Test
    public void testWithoutIntent_BackPressed()
    {
        setUpActivity();
        LinearLayout btBackArea = (LinearLayout)mActivity.findViewById(R.id.back_area);
        btBackArea.performClick();
        Assert.assertTrue(mActivity.isFinishing());
    }

    @Test
    public void testWithIntent_BackPressed_Check_CorrectUpdate() throws Exception
    {
        String serverUrl = null;
        try {
            serverUrl = startWebMockServer();
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testServerWithErrorResponse Failed due to: startWebMockServer()********\n"+e.getMessage());
            Assert.fail();
        }

        PowerMockito.mockStatic(EndpointWrapper.class);
        PowerMockito.when(EndpointWrapper.getBaseURL()).thenReturn(serverUrl);
        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(com.vodafone.mycomms.constants.Constants.VALID_VERSION_RESPONSE));
        setUpActivity();

        mActivity.holidayEndDate = "2014-07-04T12:08:56.235-0700";
        mActivity.initialHolidayEndDate = "2014-06-04T12:08:56.235-0700";
        LinearLayout btBackArea = (LinearLayout)mActivity.findViewById(R.id.back_area);
        btBackArea.performClick();
        Thread.sleep(2000);
        checkThreadSchedulers();
        Assert.assertTrue(mActivity.isFinishing());
    }

    @Test
    public void testWithIntent_BackPressed_Check_WrongUpdate() throws Exception
    {
        String serverUrl = null;
        try {
            serverUrl = startWebMockServer();
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testServerWithErrorResponse Failed due to: startWebMockServer()********\n"+e.getMessage());
            Assert.fail();
        }

        PowerMockito.mockStatic(EndpointWrapper.class);
        PowerMockito.when(EndpointWrapper.getBaseURL()).thenReturn(serverUrl);
        webServer.enqueue(new MockResponse().setResponseCode(400).setBody(com.vodafone.mycomms.constants.Constants.VALID_VERSION_RESPONSE));
        setUpActivity();

        mActivity.holidayEndDate = "2014-07-04T12:08:56.235-0700";
        mActivity.initialHolidayEndDate = "2014-06-04T12:08:56.235-0700";
        LinearLayout btBackArea = (LinearLayout)mActivity.findViewById(R.id.back_area);
        btBackArea.performClick();
        Thread.sleep(2000);
        checkThreadSchedulers();
        Assert.assertTrue(mActivity.isFinishing());
    }

    @Test
    public void testWithIntentCorrectData()
    {
        setUpActivityWithExtraIntent();
        TextView tvVacTime = (TextView)mActivity.findViewById(R.id.settings_textview_vacation_time);
        LinearLayout layVacTime = (LinearLayout)mActivity.findViewById(R.id.vacation_time_ends_layout);
        TextView endDateTextView  =  (TextView)mActivity.findViewById(R.id.vacation_setter_vacation_date_ends_text);

        Assert.assertNotNull(mActivity.holidayEndDate);
        Assert.assertTrue(mActivity.holidayEndDate.length() > 0);
        Assert.assertTrue(tvVacTime.getVisibility() == View.GONE);
        Assert.assertTrue(layVacTime.getVisibility() == View.VISIBLE);
        Assert.assertNotNull(endDateTextView);
        Assert.assertNotNull(endDateTextView.getText().toString());
        Assert.assertTrue(endDateTextView.getText().toString().length()>0);
    }

    @Test
    public void testVacationTime_SwitchNotChecked_AND_SwitchChecked()
    {
        setUpActivityWithExtraIntent();

        Switch vacationTimeSwitch = (Switch) mActivity.findViewById(R.id.switch_vacation_time);
        vacationTimeSwitch.setChecked(false);
        TextView tvVacTime = (TextView)mActivity.findViewById(R.id.settings_textview_vacation_time);
        LinearLayout layVacTime = (LinearLayout)mActivity.findViewById(R.id.vacation_time_ends_layout);

        Assert.assertFalse(vacationTimeSwitch.isChecked());
        Assert.assertTrue(tvVacTime.getVisibility() == View.VISIBLE);
        Assert.assertTrue(layVacTime.getVisibility() == View.GONE);
        Assert.assertTrue(mActivity.holidayEndDate.length() == 0);

        mActivity.holidayEndDate = "2014-07-04T12:08:56.235-0700";
        vacationTimeSwitch.setChecked(true);
        Robolectric.flushForegroundThreadScheduler();
        DatePickerDialog mDialog = (DatePickerDialog)ShadowDialog.getLatestDialog();
        Assert.assertNotNull(mDialog);
        Assert.assertTrue(mDialog.isShowing());
        ShadowDatePickerDialog shadowDatePickerDialog = Shadows.shadowOf(mDialog);

        Assert.assertNotNull(shadowDatePickerDialog);
        Assert.assertTrue(shadowDatePickerDialog.getDayOfMonth() == 4);
        Assert.assertTrue(shadowDatePickerDialog.getMonthOfYear() == 6);
        Assert.assertTrue(shadowDatePickerDialog.getYear() == 2014);
        shadowDatePickerDialog.getOnDateSetListenerCallback().onDateSet(mDialog.getDatePicker(), 2014, 5, 1);
        Button button = mDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE);
        button.performClick();
        Assert.assertFalse(mDialog.isShowing());
    }

    @Test
    public void testWithIntentWrongData()
    {
        setUpActivityWithExtraIntentWrongData();
        TextView tvVacTime = (TextView)mActivity.findViewById(R.id.settings_textview_vacation_time);
        LinearLayout layVacTime = (LinearLayout)mActivity.findViewById(R.id.vacation_time_ends_layout);
        TextView endDateTextView  =  (TextView)mActivity.findViewById(R.id.vacation_setter_vacation_date_ends_text);

        Assert.assertNotNull(mActivity.holidayEndDate);
        Assert.assertTrue(mActivity.holidayEndDate.length() > 0);
        Assert.assertTrue(tvVacTime.getVisibility() == View.GONE);
        Assert.assertTrue(layVacTime.getVisibility() == View.VISIBLE);
        Assert.assertNotNull(endDateTextView);
        Assert.assertNotNull(endDateTextView.getText().toString());
        Assert.assertTrue(endDateTextView.getText().toString().length() == 0);
    }

    @Test
    public void testWithIntentEmptyData()
    {
        setUpActivityWithExtraIntentEmptyData();
        TextView tvVacTime = (TextView)mActivity.findViewById(R.id.settings_textview_vacation_time);
        LinearLayout layVacTime = (LinearLayout)mActivity.findViewById(R.id.vacation_time_ends_layout);
        Switch aSwitch = ((Switch) mActivity.findViewById(R.id.switch_vacation_time));

        Assert.assertNotNull(mActivity.holidayEndDate);
        Assert.assertTrue(mActivity.holidayEndDate.length() == 0);
        Assert.assertTrue(tvVacTime.getVisibility() == View.VISIBLE);
        Assert.assertTrue(layVacTime.getVisibility() == View.GONE);
        Assert.assertNotNull(aSwitch);
        Assert.assertFalse(aSwitch.isChecked());
    }

    private void setUpActivity()
    {
        mActivity = Robolectric.setupActivity(VacationTimeSetterActivity.class);
    }

    private void setUpActivityWithExtraIntent()
    {
        Intent intent = new Intent();
        intent.putExtra("EXTRA_VACATION_TIME_ID", "2014-07-04T12:08:56.235-0700");
        mActivity = Robolectric.buildActivity(VacationTimeSetterActivity.class)
                .withIntent(intent).create().start().resume().visible().get();
    }

    private void setUpActivityWithExtraIntentWrongData()
    {
        Intent intent = new Intent();
        intent.putExtra("EXTRA_VACATION_TIME_ID", "wrongData");
        mActivity = Robolectric.buildActivity(VacationTimeSetterActivity.class)
                .withIntent(intent).create().start().resume().visible().get();
    }

    private void setUpActivityWithExtraIntentEmptyData()
    {
        Intent intent = new Intent();
        intent.putExtra("EXTRA_VACATION_TIME_ID", "");
        mActivity = Robolectric.buildActivity(VacationTimeSetterActivity.class)
                .withIntent(intent).create().start().resume().visible().get();
    }
    private String startWebMockServer() throws Exception {
        //OkHttp mocked web server
        webServer = new MockWebServer();
        webServer.useHttps(null, false);

        //Connect OkHttp calls with MockWebServer
        webServer.start();
        String serverUrl = webServer.getUrl("").toString();

        return serverUrl;
    }

    private void checkThreadSchedulers()
    {
        if(Robolectric.getBackgroundThreadScheduler().areAnyRunnable())
            Robolectric.flushBackgroundThreadScheduler();
        if(Robolectric.getForegroundThreadScheduler().areAnyRunnable())
            Robolectric.flushForegroundThreadScheduler();
    }

}
