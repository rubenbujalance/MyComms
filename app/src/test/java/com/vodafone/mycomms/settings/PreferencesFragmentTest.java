package com.vodafone.mycomms.settings;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.constants.Constants;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmProfileTransactions;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.CustomPreferencesFragmentActivity;

import junit.framework.Assert;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
@PrepareForTest({RealmContactTransactions.class, Realm.class, EndpointWrapper.class,
        RealmProfileTransactions.class})

public class PreferencesFragmentTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    MockWebServer webServer;
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
    public void testVacationTimeClick() throws Exception {
        System.err.println("******** Test: Vacation Time Click ********");

        vacationTimeButton.performClick();

        System.err.println("******** Test: Vacation Time Click OK********");
    }

    //    @Test
    public void testVacationTimeActivityResult() throws Exception {
        System.err.println("******** Test: Vacation Time Click ********");

        //        Shadows.shadowOf(mPreferencesFragment.getActivity()).receiveResult(
//                new Intent(mPreferencesFragment.getActivity(), VacationTimeSetterActivity.class),
//                Activity.RESULT_OK,
//                new Intent().putExtra(SettingsMainActivity.VACATION_TIME_END_VALUE, Constants.VACATION_END_DATE));
//
//        Assert.assertEquals(Constants.VACATION_END_DATE, vacationTimeEnds.getText());

        System.err.println("******** Test: Vacation Time Click OK********");
    }

    @Test
    public void testShareCurrentTimeSwitchOn() throws Exception {
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
    public void testDoNotDisturbSwitch() throws Exception {
        System.err.println("******** Test: LOGOUT ********");

        UserProfile userProfile = mockUserProfile(Constants.SETTINGS_JSON_DONOTDISTURB);
        doNotDisturbSwitch.performClick();
        Assert.assertTrue(shareCurrentTimeSwitch.isChecked());

        mPreferencesFragment.onProfileReceived(userProfile);
        Assert.assertTrue(shareCurrentTimeSwitch.isChecked());

        System.err.println("******** Test: LOGOUT OK********");
    }

    @Test
    public void testAboutNavigation() throws Exception {
        System.err.println("******** Test: About navigation ********");

        aboutButton.performClick();
        Intent expectedIntent = new Intent(mPreferencesFragment.getActivity(), AboutActivity.class);
        Assert.assertTrue(Shadows.shadowOf(mPreferencesFragment.getActivity())
                .getNextStartedActivity().equals(expectedIntent));

        System.err.println("******** Test: About navigation OK********");
    }

    //    @Test
    public void testOnProfileReceived() throws Exception {
        System.err.println("******** Test: LOGOUT ********");

        System.err.println("******** Test: LOGOUT OK********");
    }

    //    @Test
    public void testOnDetachFragment() throws Exception {
        System.err.println("******** Test: LOGOUT ********");

        System.err.println("******** Test: LOGOUT OK********");
    }

    //    @Test
    public void testOnPauseFragment() throws Exception {
        System.err.println("******** Test: LOGOUT ********");

        System.err.println("******** Test: LOGOUT OK********");
    }

    //    @Test
    public void testOnStopFragment() throws Exception {
        System.err.println("******** Test: LOGOUT ********");

        System.err.println("******** Test: LOGOUT OK********");
    }

    //    @Test
    public void testOnDestroyFragment() throws Exception {
        System.err.println("******** Test: LOGOUT ********");

        System.err.println("******** Test: LOGOUT OK********");
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

    private String startWebMockServer() throws Exception {
        //OkHttp mocked web server
        webServer = new MockWebServer();
        webServer.useHttps(null, false);

        //Connect OkHttp calls with MockWebServer
        webServer.start();

        return webServer.getUrl("/").toString();
    }

    private String loadJSON() throws Exception {
        InputStream inputStream = context.getResources().openRawResource(R.raw.test_contacts);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int ctr;
        try {
            ctr = inputStream.read();
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr);
                ctr = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            Log.e(com.vodafone.mycomms.util.Constants.TAG, "SearchGlobalContactsTest.loadJSON: e ", e);
        }
        return byteArrayOutputStream.toString();
    }

    private UserProfile mockUserProfile(String settings){
        UserProfile userProfile = new UserProfile();
        userProfile.setId("1234");
        userProfile.setSettings(settings);

        return userProfile;
    }
}
