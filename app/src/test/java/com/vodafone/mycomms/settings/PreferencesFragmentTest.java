package com.vodafone.mycomms.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.constants.Constants;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmProfileTransactions;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.CustomPreferencesFragmentActivity;
import com.vodafone.mycomms.util.Utils;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import io.realm.Realm;
import model.UserProfile;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*"})
@PrepareForTest({Realm.class, Crashlytics.class})

public class PreferencesFragmentTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    PreferencesFragment mPreferencesFragment;
    CustomPreferencesFragmentActivity customPreferencesFragmentActivity;
    Context context;
    TextView vacationTimeEnds;
    ImageView vacationTimeArrow;
    Button btLogout;
    LinearLayout vacationTimeButton;
    Switch shareCurrentTimeSwitch;
    Switch doNotDisturbSwitch;
    TextView aboutButton;

    @Before
    public void setUp() throws Exception {
        mockStatic(Realm.class);
        when(Realm.getDefaultInstance()).thenReturn(null);
        mockStatic(Crashlytics.class);
        whenNew(RealmContactTransactions.class).withAnyArguments()
                .thenReturn(null);
        whenNew(RealmProfileTransactions.class).withAnyArguments()
                .thenReturn(null);
        context = RuntimeEnvironment.application.getApplicationContext();
        startPreferencesFragment(0);
        mPreferencesFragment = (PreferencesFragment) customPreferencesFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("0");
        MockDataForTests.checkThreadSchedulers();

        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
        vacationTimeEnds = (TextView) mPreferencesFragment.getView().findViewById(R.id.settings_preferences_vacation_time_value);
        vacationTimeArrow = (ImageView) mPreferencesFragment.getView().findViewById(R.id.about_arrow_right_top);
        btLogout = (Button) mPreferencesFragment.getView().findViewById(R.id.btLogout);
        vacationTimeButton = (LinearLayout) mPreferencesFragment.getView().findViewById(R.id.button_settings_vacation_time);
        shareCurrentTimeSwitch = (Switch) mPreferencesFragment.getView().findViewById(R.id.setting_share_current_time_switch);
        doNotDisturbSwitch = (Switch) mPreferencesFragment.getView().findViewById(R.id.settings_do_not_disturb_switch);
        aboutButton = (TextView) mPreferencesFragment.getView().findViewById(R.id.btnAbout);
    }

    @After
    public void tearDown() throws Exception
    {
        MockDataForTests.checkThreadSchedulers();
        Robolectric.reset();
        mPreferencesFragment = null;
        customPreferencesFragmentActivity = null;
        context = null;
        vacationTimeEnds = null;
        vacationTimeArrow = null;
        btLogout = null;
        vacationTimeButton = null;
        shareCurrentTimeSwitch = null;
        doNotDisturbSwitch = null;
        aboutButton = null;
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
                System.err.println("Uncaught exception at " + this.getClass().getSimpleName() + ": \n" + writer.toString());
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
        TextView vacationTimeEnds = (TextView) this.mPreferencesFragment.getView().findViewById(R.id.settings_preferences_vacation_time_value);
        ImageView vacationTimeArrow = (ImageView) this.mPreferencesFragment.getView().findViewById(R.id.about_arrow_right_top);
        Intent intent = new Intent();
        intent.putExtra(SettingsMainActivity.VACATION_TIME_END_VALUE, "2014-07-04T12:08:56.235-0700");
        this.mPreferencesFragment.onActivityResult(1, Activity.RESULT_OK, intent);
        Assert.assertTrue(this.mPreferencesFragment.holidayEndDate.compareTo("2014-07-04T12:08:56.235-0700") == 0);
        Assert.assertTrue(vacationTimeEnds.getVisibility() == View.VISIBLE);
        Assert.assertTrue(vacationTimeArrow.getVisibility() == View.GONE);
        this.mPreferencesFragment.onDestroy();

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        Assert.assertTrue(vacationTimeEnds != null);
        Assert.assertTrue(vacationTimeArrow != null);
        Assert.assertTrue(btLogout != null);
        Assert.assertTrue(vacationTimeButton != null);
        Assert.assertTrue(shareCurrentTimeSwitch != null);
        Assert.assertTrue(doNotDisturbSwitch != null);
        Assert.assertTrue(aboutButton != null);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testReceivedProfile() throws Exception {

        //Private Time Zone
        UserProfile userProfile = mockUserProfile(Constants.SETTINGS_JSON_TIMEZONE_PRIVATE);
        mPreferencesFragment.onProfileReceived(userProfile);
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(shareCurrentTimeSwitch.isChecked());

        //Public Time Zone
        userProfile = mockUserProfile(Constants.SETTINGS_JSON_TIMEZONE_PUBLIC);
        mPreferencesFragment.onProfileReceived(userProfile);
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(shareCurrentTimeSwitch.isChecked());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testLogout() throws Exception {
        ProfileController spyProfileController = Mockito.spy(new ProfileController(mPreferencesFragment.getActivity()));
        Mockito.doNothing().when(spyProfileController).logoutToAPI();

        btLogout.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertFalse(MycommsApp.appIsInitialized);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testShareCurrentTimeSwitch() throws Exception {

        UserProfile userProfile = mockUserProfile(Constants.SETTINGS_JSON_TIMEZONE_PUBLIC);
        ProfileController spyProfileController = Mockito.spy(new ProfileController(mPreferencesFragment.getActivity()));
        Mockito.doNothing().when(spyProfileController).updateSettingsData(Mockito.any(HashMap.class));

        shareCurrentTimeSwitch.performClick();
        MockDataForTests.checkThreadSchedulers();

        mPreferencesFragment.onProfileReceived(userProfile);
        MockDataForTests.checkThreadSchedulers();

        Assert.assertFalse(shareCurrentTimeSwitch.isChecked());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testShareCurrentTimeSwitchOff() throws Exception {

        shareCurrentTimeSwitch.setChecked(true);
        shareCurrentTimeSwitch.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(!shareCurrentTimeSwitch.isChecked());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testShareCurrentTimeSwitchOn() throws Exception {

        shareCurrentTimeSwitch.setChecked(false);
        shareCurrentTimeSwitch.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(shareCurrentTimeSwitch.isChecked());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testDoNotDisturbSwitch() throws Exception {

        UserProfile userProfile = mockUserProfile(Constants.SETTINGS_JSON_DONOTDISTURB);
        doNotDisturbSwitch.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(shareCurrentTimeSwitch.isChecked());

        mPreferencesFragment.onProfileReceived(userProfile);
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(shareCurrentTimeSwitch.isChecked());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testDoNotDisturbSwitchOn() throws Exception {

        doNotDisturbSwitch.setChecked(false);
        doNotDisturbSwitch.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(doNotDisturbSwitch.isChecked());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testDoNotDisturbSwitchOff() throws Exception {

        doNotDisturbSwitch.setChecked(true);
        doNotDisturbSwitch.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(!doNotDisturbSwitch.isChecked());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testOnProfileReceivedWithVacation() throws Exception {

        UserProfile userProfile = mockUserProfile(Constants.SETTINGS_JSON_FULL);
        ProfileController spyProfileController = Mockito.spy(new ProfileController(mPreferencesFragment.getActivity()));
        Mockito.doNothing().when(spyProfileController).updateSettingsData(Mockito.any(HashMap.class));
        mPreferencesFragment.onProfileReceived(userProfile);
        MockDataForTests.checkThreadSchedulers();

        String date = getHolidayDateToSet(Constants.SETTINGS_JSON_FULL);

        Assert.assertTrue(vacationTimeEnds.getText().equals(date));
        Assert.assertTrue(vacationTimeEnds.getVisibility() == View.VISIBLE);
        Assert.assertTrue(vacationTimeArrow.getVisibility() == View.GONE);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testOnProfileReceivedWithNoHoliday() throws Exception {
        UserProfile userProfile = mockUserProfile(Constants.SETTINGS_JSON_NO_HOLIDAY);
        ProfileController spyProfileController = Mockito.spy(new ProfileController(mPreferencesFragment.getActivity()));
        Mockito.doNothing().when(spyProfileController).updateSettingsData(Mockito.any(HashMap.class));
        mPreferencesFragment.profileController = spyProfileController;
        mPreferencesFragment.onProfileReceived(userProfile);
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(vacationTimeEnds.getVisibility() == View.GONE);
        Assert.assertTrue(vacationTimeArrow.getVisibility() == View.VISIBLE);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }


    @Test
    public void testOnProfileReceivedWithPublicTimeZone() throws Exception {
        UserProfile userProfile = mockUserProfile(Constants.SETTINGS_JSON_NO_TIMEZONE);
        ProfileController spyProfileController = Mockito.spy(new ProfileController(mPreferencesFragment.getActivity()));
        Mockito.doNothing().when(spyProfileController).updateSettingsData(Mockito.any(HashMap.class));
        mPreferencesFragment.profileController = spyProfileController;
        mPreferencesFragment.onProfileReceived(userProfile);
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(shareCurrentTimeSwitch.isChecked());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testOnProfileReceivedWithNoDoNotDisturb() throws Exception {
        UserProfile userProfile = mockUserProfile(Constants.SETTINGS_JSON_NO_DONOTDISTURB);
        mPreferencesFragment.onProfileReceived(userProfile);
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(!doNotDisturbSwitch.isChecked());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    public void startPreferencesFragment(int index)
    {
        Intent in = new Intent(RuntimeEnvironment.application.getApplicationContext(),
                CustomPreferencesFragmentActivity.class);
        in.putExtra("index", index);
        customPreferencesFragmentActivity = Robolectric.buildActivity(CustomPreferencesFragmentActivity.class)
                .withIntent(in)
                .create().start().resume().get();
    }

    @Test
    public void testVacationTimeClick() throws Exception {
        vacationTimeButton.performClick();
        MockDataForTests.checkThreadSchedulers();

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void shouldSupportGetStartedActivitiesForResult() throws Exception {
        ShadowActivity shadowActivity = Shadows.shadowOf(mPreferencesFragment.getActivity());
        Intent intent = new Intent().setClass(mPreferencesFragment.getActivity(), VacationTimeSetterActivity.class);
        mPreferencesFragment.getActivity().startActivityForResult(intent, 142);
        ShadowActivity.IntentForResult intentForResult = shadowActivity.getNextStartedActivityForResult();
        Assert.assertTrue(intentForResult!=null);
        Assert.assertNull(shadowActivity.getNextStartedActivityForResult());
        Assert.assertTrue(intentForResult.intent != null);
        Assert.assertTrue(intentForResult.intent==intent);
        Assert.assertTrue(intentForResult.requestCode == 142);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    private UserProfile mockUserProfile(String settings){
        UserProfile userProfile = new UserProfile();
        userProfile.setId("1234");
        userProfile.setSettings(settings);

        return userProfile;
    }

    private String getHolidayDateToSet(String json){
        String endDateStr = "";
        try {
            JSONObject jsonSettings = new JSONObject(json);
            JSONObject jsonHoliday = jsonSettings.getJSONObject(com.vodafone.mycomms.util.Constants.PROFILE_HOLIDAY);
            endDateStr = jsonHoliday.getString(com.vodafone.mycomms.util.Constants.PROFILE_HOLIDAY_END_DATE);
        } catch (JSONException e){
            Log.e(com.vodafone.mycomms.util.Constants.TAG, "PreferencesFragmentTest.getHolidayDateToSet: e ",e);
        }
        endDateStr = endDateStr.replaceAll("Z", "+0000");
        String holidayEndDate = Utils.isoDateToTimezone(endDateStr);
        SimpleDateFormat sdf = new SimpleDateFormat(com.vodafone.mycomms.util.Constants.API_DATE_FULL_FORMAT);
        Date endDate = null;
        try {
            endDate = sdf.parse(holidayEndDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        final String holidayDateToSet =
                com.vodafone.mycomms.util.Constants.SIMPLE_DATE_FORMAT_DISPLAY.format(endDate.getTime());
        return holidayDateToSet;
    }
}
