package com.vodafone.mycomms.settings;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmProfileTransactions;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.CustomFragmentActivity;

import junit.framework.Assert;

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
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;

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
    CustomFragmentActivity customFragmentActivity;
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
        mPreferencesFragment = (PreferencesFragment) customFragmentActivity
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
    public void testBack() throws Exception {
//        mBack.performClick();
//        Assert.assertTrue(activity.isFinishing());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testFinish() throws Exception {
        Activity activity = Robolectric.buildActivity(SettingsMainActivity.class).create().start().resume().pause().stop().destroy().get();
        Assert.assertTrue(activity.isDestroyed());
    }

    public void startPreferencesFragment(int index)
    {
        Intent in = new Intent(RuntimeEnvironment.application.getApplicationContext(),
                CustomFragmentActivity.class);
        in.putExtra("index", index);
        customFragmentActivity = Robolectric.buildActivity(CustomFragmentActivity.class)
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
            Log.e(Constants.TAG, "SearchGlobalContactsTest.loadJSON: e ", e);
        }
        return byteArrayOutputStream.toString();
    }
}
