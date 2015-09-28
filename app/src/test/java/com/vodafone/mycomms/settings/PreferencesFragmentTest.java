package com.vodafone.mycomms.settings;

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
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.CustomPreferencesFragmentActivity;
import com.vodafone.mycomms.util.Utils;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
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
        //Try to shutdown server if it was started
        try {
            Robolectric.reset();
        } catch (Exception e) {}

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

    @Test
    public void shouldNotBeNull() throws Exception {
        System.err.println("******** Test: NOT NULL OBJECTS ********");
        Assert.assertTrue(vacationTimeEnds != null);
        Assert.assertTrue(vacationTimeArrow != null);
        Assert.assertTrue(btLogout != null);
        Assert.assertTrue(vacationTimeButton != null);
        Assert.assertTrue(shareCurrentTimeSwitch != null);
        Assert.assertTrue(doNotDisturbSwitch != null);
        Assert.assertTrue(aboutButton != null);
        System.err.println("******** Test: NO NULL OBJECTS OK ********");
    }

    @Test
    public void testReceivedProfile() throws Exception {
        System.err.println("******** Test: On Received Profile ********");

        //Private Time Zone
        UserProfile userProfile = mockUserProfile(Constants.SETTINGS_JSON_TIMEZONE_PRIVATE);
        mPreferencesFragment.onProfileReceived(userProfile);
        Assert.assertTrue(shareCurrentTimeSwitch.isChecked());

        //Public Time Zone
        userProfile = mockUserProfile(Constants.SETTINGS_JSON_TIMEZONE_PUBLIC);
        mPreferencesFragment.onProfileReceived(userProfile);
        Assert.assertTrue(shareCurrentTimeSwitch.isChecked());

        System.err.println("******** Test: On Received Profile OK********");
    }

    @Test
    public void testLogout() throws Exception {
        System.err.println("******** Test: LOGOUT ********");

        ProfileController spyProfileController = Mockito.spy(new ProfileController(mPreferencesFragment.getActivity()));
        Mockito.doNothing().when(spyProfileController).logoutToAPI();

        btLogout.performClick();
        Assert.assertFalse(MycommsApp.appIsInitialized);

        System.err.println("******** Test: LOGOUT OK********");
    }

    @Test
    public void testShareCurrentTimeSwitch() throws Exception {
        System.err.println("******** Test: Share Current Time ********");

        UserProfile userProfile = mockUserProfile(Constants.SETTINGS_JSON_TIMEZONE_PUBLIC);
        ProfileController spyProfileController = Mockito.spy(new ProfileController(mPreferencesFragment.getActivity()));
        Mockito.doNothing().when(spyProfileController).updateSettingsData(Mockito.any(HashMap.class));

        shareCurrentTimeSwitch.performClick();

        mPreferencesFragment.onProfileReceived(userProfile);

        Assert.assertFalse(shareCurrentTimeSwitch.isChecked());

        System.err.println("******** Test: Share Current Time OK********");
    }

    @Test
    public void testShareCurrentTimeSwitchOff() throws Exception {
        System.err.println("******** Test: Share Current Time ********");

        shareCurrentTimeSwitch.setChecked(true);
        shareCurrentTimeSwitch.performClick();
        Assert.assertTrue(!shareCurrentTimeSwitch.isChecked());

        System.err.println("******** Test: Share Current Time OK********");
    }

    @Test
    public void testShareCurrentTimeSwitchOn() throws Exception {
        System.err.println("******** Test: Share Current Time ********");

        shareCurrentTimeSwitch.setChecked(false);
        shareCurrentTimeSwitch.performClick();
        Assert.assertTrue(shareCurrentTimeSwitch.isChecked());

        System.err.println("******** Test: Share Current Time OK********");
    }

    @Test
    public void testDoNotDisturbSwitch() throws Exception {
        System.err.println("******** Test: Do not disturb ********");

        UserProfile userProfile = mockUserProfile(Constants.SETTINGS_JSON_DONOTDISTURB);
        doNotDisturbSwitch.performClick();
        Assert.assertTrue(shareCurrentTimeSwitch.isChecked());

        mPreferencesFragment.onProfileReceived(userProfile);
        Assert.assertTrue(shareCurrentTimeSwitch.isChecked());

        System.err.println("******** Test: Do not disturb OK********");
    }

    @Test
    public void testDoNotDisturbSwitchOn() throws Exception {
        System.err.println("******** Test: Do not disturb ********");

        doNotDisturbSwitch.setChecked(false);
        doNotDisturbSwitch.performClick();
        Assert.assertTrue(doNotDisturbSwitch.isChecked());

        System.err.println("******** Test: Do not disturb OK********");
    }

    @Test
    public void testDoNotDisturbSwitchOff() throws Exception {
        System.err.println("******** Test: Do not disturb ********");

        doNotDisturbSwitch.setChecked(true);
        doNotDisturbSwitch.performClick();
        Assert.assertTrue(!doNotDisturbSwitch.isChecked());

        System.err.println("******** Test: Do not disturb OK********");
    }

//    @Test
//    public void testAboutNavigation() throws Exception {
//        System.err.println("******** Test: About navigation ********");
//
//        aboutButton.performClick();
//        Intent expectedIntent = new Intent(mPreferencesFragment.getActivity(), AboutActivity.class);
//        Assert.assertTrue(Shadows.shadowOf(mPreferencesFragment.getActivity())
//                .getNextStartedActivity().getComponent().getClassName().compareTo(AboutActivity.class.getName())==0);
//
//        System.err.println("******** Test: About navigation OK********");
//    }

    @Test
    public void testOnProfileReceivedWithVacation() throws Exception {
        System.err.println("******** Test: On Profile Received ********");

        UserProfile userProfile = mockUserProfile(Constants.SETTINGS_JSON_FULL);
        ProfileController spyProfileController = Mockito.spy(new ProfileController(mPreferencesFragment.getActivity()));
        Mockito.doNothing().when(spyProfileController).updateSettingsData(Mockito.any(HashMap.class));
        mPreferencesFragment.onProfileReceived(userProfile);
        String date = getHolidayDateToSet(Constants.SETTINGS_JSON_FULL);

        Assert.assertTrue(vacationTimeEnds.getText().equals(date));
        Assert.assertTrue(vacationTimeEnds.getVisibility() == View.VISIBLE);
        Assert.assertTrue(vacationTimeArrow.getVisibility() == View.GONE);

        System.err.println("******** Test: On Profile Received OK********");
    }

    @Test
    public void testOnProfileReceivedWithNoHoliday() throws Exception {
        System.err.println("******** Test: On Profile Received ********");

        UserProfile userProfile = mockUserProfile(Constants.SETTINGS_JSON_NO_HOLIDAY);
        ProfileController spyProfileController = Mockito.spy(new ProfileController(mPreferencesFragment.getActivity()));
        Mockito.doNothing().when(spyProfileController).updateSettingsData(Mockito.any(HashMap.class));
        mPreferencesFragment.profileController = spyProfileController;
        mPreferencesFragment.onProfileReceived(userProfile);

        Assert.assertTrue(vacationTimeEnds.getVisibility() == View.GONE);
        Assert.assertTrue(vacationTimeArrow.getVisibility() == View.VISIBLE);

        System.err.println("******** Test: On Profile Received OK********");
    }

    @Test
    public void testOnProfileReceivedWithPublicTimeZone() throws Exception {
        System.err.println("******** Test: On Profile Received ********");

        UserProfile userProfile = mockUserProfile(Constants.SETTINGS_JSON_NO_TIMEZONE);
        ProfileController spyProfileController = Mockito.spy(new ProfileController(mPreferencesFragment.getActivity()));
        Mockito.doNothing().when(spyProfileController).updateSettingsData(Mockito.any(HashMap.class));
        mPreferencesFragment.profileController = spyProfileController;
        mPreferencesFragment.onProfileReceived(userProfile);

        Assert.assertTrue(shareCurrentTimeSwitch.isChecked());

        System.err.println("******** Test: On Profile Received OK********");
    }

    @Test
    public void testOnProfileReceivedWithNoDoNotDisturb() throws Exception {
        System.err.println("******** Test: On Profile Received ********");

        UserProfile userProfile = mockUserProfile(Constants.SETTINGS_JSON_NO_DONOTDISTURB);
        mPreferencesFragment.onProfileReceived(userProfile);

        Assert.assertTrue(!doNotDisturbSwitch.isChecked());

        System.err.println("******** Test: On Profile Received OK********");
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
        System.err.println("******** Test: Vacation Time Click ********");

        vacationTimeButton.performClick();
//        Intent expectedIntent = new Intent(mPreferencesFragment.getActivity(), VacationTimeSetterActivity.class);
//        Assert.assertTrue(Shadows.shadowOf(mPreferencesFragment.getActivity())
//                .getNextStartedActivityForResult().equals(expectedIntent));

        System.err.println("******** Test: Vacation Time Click OK********");
    }

//    @Test
//    public void shouldSupportStartActivityForResult() throws Exception {
//        ShadowActivity shadowActivity = Shadows.shadowOf(mPreferencesFragment.getActivity());
//        Intent intent = new Intent().setClass(mPreferencesFragment.getActivity(), VacationTimeSetterActivity.class);
//        Assert.assertTrue(shadowActivity.getNextStartedActivity() == null);
//        mPreferencesFragment.getActivity().startActivityForResult(intent, 142);
//        Intent startedIntent = shadowActivity.getNextStartedActivity();
//        Assert.assertTrue(startedIntent != null);
//        Assert.assertTrue(startedIntent.getComponent().getClassName().compareTo(VacationTimeSetterActivity.class.getName())==0);
//    }

    @Test
    public void shouldSupportGetStartedActivitiesForResult() throws Exception {
        ShadowActivity shadowActivity = Shadows.shadowOf(mPreferencesFragment.getActivity());
        Intent intent = new Intent().setClass(mPreferencesFragment.getActivity(), VacationTimeSetterActivity.class);
        mPreferencesFragment.getActivity().startActivityForResult(intent, 142);
        ShadowActivity.IntentForResult intentForResult = shadowActivity.getNextStartedActivityForResult();
        Assert.assertTrue(intentForResult!=null);
        Assert.assertTrue(shadowActivity.getNextStartedActivityForResult()==null);
        Assert.assertTrue(intentForResult.intent!=null);
        Assert.assertTrue(intentForResult.intent==intent);
        Assert.assertTrue(intentForResult.requestCode == 142);
    }

//    @Test
    public void testVacationTimeActivityResult() throws Exception {
        System.err.println("******** Test: Vacation Time Click ********");

        vacationTimeButton.performClick();

//        ShadowActivity sActivity = Shadows.shadowOf(mPreferencesFragment.getActivity());
//        Intent requestIntent = new Intent(mPreferencesFragment.getActivity(), VacationTimeSetterActivity.class);
//        requestIntent.putExtra(SettingsMainActivity.VACATION_TIME_END_VALUE, "Fake Date");
//        Intent responseIntent = new Intent().putExtra("vacationTimeEndValue","Fake Date");
//        sActivity.receiveResult(requestIntent, Activity.RESULT_OK, responseIntent);
//        Assert.assertNotNull(vacationTimeEnds.getText());
        //        Shadows.shadowOf(mPreferencesFragment.getActivity()).receiveResult(
//                new Intent(mPreferencesFragment.getActivity(), VacationTimeSetterActivity.class),
//                Activity.RESULT_OK,
//                new Intent().putExtra(SettingsMainActivity.VACATION_TIME_END_VALUE, Constants.VACATION_END_DATE));
//
//        Assert.assertEquals(Constants.VACATION_END_DATE, vacationTimeEnds.getText());

        System.err.println("******** Test: Vacation Time Click OK********");
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
