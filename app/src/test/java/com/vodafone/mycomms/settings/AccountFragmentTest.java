package com.vodafone.mycomms.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.settings.globalcontacts.AddGlobalContactsActivity;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.CustomPreferencesFragmentActivity;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
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

import java.io.PrintWriter;
import java.io.StringWriter;

import io.realm.Realm;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by str_oan on 22/09/2015.
 */


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*"})
@PrepareForTest({Realm.class
        , Crashlytics.class
        , RealmContactTransactions.class})
public class AccountFragmentTest
{
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    public Context mContext;
    public CustomPreferencesFragmentActivity mCustomPreferencesFragmentActivity;
    public AccountsFragment mAccountsFragment;
    public String mProfileId = "mc_5570340e7eb7c3512f2f9bf2";

    @Before
    public void setUp()
    {
        mockStatic(Realm.class);
        when(Realm.getDefaultInstance()).thenReturn(null);
        mockStatic(Crashlytics.class);
        mContext = RuntimeEnvironment.application.getApplicationContext();
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
    }

    @After
    public void tearDown()
    {
        MockDataForTests.checkThreadSchedulers();
        Robolectric.reset();
        mAccountsFragment = null;
        mCustomPreferencesFragmentActivity = null;
        mContext = null;
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
    public void testLocalContactsEnabled()
    {
        mockStatics_ContactPlatformExists();
        setSharedPreferencesWithLocal_NOGlobalContactsEnabled();
        startAccountsFragment();
        MockDataForTests.checkThreadSchedulers();

        TextView tvAddLocalContacts = (TextView) mAccountsFragment.getView().findViewById(R.id.btn_add_local_contacts);
        ImageView imgCheckLocalContacts = (ImageView) mAccountsFragment.getView().findViewById(R.id.acc_check_local_contacts);
        TextView vAddGlobalContacts = (TextView) mAccountsFragment.getView().findViewById(R.id.btn_add_vodafone_global);
        ImageView imgCheckGlobalContacts = (ImageView) mAccountsFragment.getView().findViewById(R.id.acc_check_vodafone_global);

        Assert.assertTrue(tvAddLocalContacts.getVisibility() == View.GONE);
        Assert.assertTrue(imgCheckLocalContacts.getVisibility() == View.VISIBLE);
        Assert.assertTrue(vAddGlobalContacts.getVisibility() == View.VISIBLE);
        Assert.assertTrue(imgCheckGlobalContacts.getVisibility() == View.GONE);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testGlobalContactsEnabled()
    {
        mockStatics_ContactPlatformNotExists();
        setSharedPreferencesNOLocal_AndGlobalContactsEnabled();
        startAccountsFragment();
        MockDataForTests.checkThreadSchedulers();

        TextView tvAddLocalContacts = (TextView) mAccountsFragment.getView().findViewById(R.id.btn_add_local_contacts);
        ImageView imgCheckLocalContacts = (ImageView) mAccountsFragment.getView().findViewById(R.id.acc_check_local_contacts);
        TextView vAddGlobalContacts = (TextView) mAccountsFragment.getView().findViewById(R.id.btn_add_vodafone_global);
        ImageView imgCheckGlobalContacts = (ImageView) mAccountsFragment.getView().findViewById(R.id.acc_check_vodafone_global);

        Assert.assertTrue(tvAddLocalContacts.getVisibility() == View.VISIBLE);
        Assert.assertTrue(imgCheckLocalContacts.getVisibility() == View.GONE);
        Assert.assertTrue(vAddGlobalContacts.getVisibility() == View.GONE);
        Assert.assertTrue(imgCheckGlobalContacts.getVisibility() == View.VISIBLE);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testAddGlobalContacts() throws Exception
    {
        mockStatics_ContactPlatformExists();
        setSharedPreferencesNoLocal_AndNoGlobalContactsEnabled();
        startAccountsFragment();
        MockDataForTests.checkThreadSchedulers();

        TextView vAddGlobalContacts = (TextView) mAccountsFragment.getView().findViewById(R.id.btn_add_vodafone_global);
        vAddGlobalContacts.performClick();
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(mAccountsFragment.getActivity());
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        Assert.assertTrue(startedIntent.getComponent().getClassName().compareTo(AddGlobalContactsActivity.class.getName()) == 0);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testAddLocalContacts() throws Exception
    {
        mockStatics_ContactPlatformExists();
        setSharedPreferencesNoLocal_AndNoGlobalContactsEnabled();
        startAccountsFragment();
        MockDataForTests.checkThreadSchedulers();

        TextView tvAddLocalContacts = (TextView) mAccountsFragment.getView().findViewById(R.id.btn_add_local_contacts);
        ImageView imgCheckLocalContacts = (ImageView) mAccountsFragment.getView().findViewById(R.id.acc_check_local_contacts);
        TextView vAddGlobalContacts = (TextView) mAccountsFragment.getView().findViewById(R.id.btn_add_vodafone_global);
        ImageView imgCheckGlobalContacts = (ImageView) mAccountsFragment.getView().findViewById(R.id.acc_check_vodafone_global);
        tvAddLocalContacts.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(tvAddLocalContacts.getVisibility() == View.GONE);
        Assert.assertTrue(imgCheckLocalContacts.getVisibility() == View.VISIBLE);
        Assert.assertTrue(vAddGlobalContacts.getVisibility() == View.VISIBLE);
        Assert.assertTrue(imgCheckGlobalContacts.getVisibility() == View.GONE);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    private void mockStatics_ContactPlatformExists()
    {
        PowerMockito.mockStatic(RealmContactTransactions.class);
        PowerMockito.when(RealmContactTransactions
                .validateContactPlatformExists(Matchers.any(Realm.class), Matchers.anyString())).thenReturn(true);
    }

    private void mockStatics_ContactPlatformNotExists()
    {
        PowerMockito.mockStatic(RealmContactTransactions.class);
        PowerMockito.when(RealmContactTransactions
                .validateContactPlatformExists(Matchers.any(Realm.class), Matchers.anyString())).thenReturn(false);
    }

    private void startAccountsFragment()
    {
        Intent in = new Intent(RuntimeEnvironment.application.getApplicationContext(),
                CustomPreferencesFragmentActivity.class);
        in.putExtra("index", 2);
        mCustomPreferencesFragmentActivity = Robolectric.buildActivity(CustomPreferencesFragmentActivity.class)
                .withIntent(in)
                .create().start().resume().get();

        mAccountsFragment = (AccountsFragment) mCustomPreferencesFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("2");
    }

    private void setSharedPreferencesWithLocal_NOGlobalContactsEnabled()
    {
        SharedPreferences sp = mContext.getSharedPreferences(
                com.vodafone.mycomms.util.Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        sp.edit().putBoolean(
                com.vodafone.mycomms.util.Constants.IS_LOCAL_CONTACTS_LOADING_ENABLED,
                true)
                .apply();
        sp.edit().putBoolean(
                com.vodafone.mycomms.util.Constants.IS_GLOBAL_CONTACTS_LOADING_ENABLED,
                false)
                .apply();
        sp.edit().putString(
                com.vodafone.mycomms.util.Constants.PROFILE_ID_SHARED_PREF,
                mProfileId)
                .apply();
    }

    private void setSharedPreferencesNOLocal_AndGlobalContactsEnabled()
    {
        SharedPreferences sp = mContext.getSharedPreferences(
                com.vodafone.mycomms.util.Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        sp.edit().putBoolean(
                com.vodafone.mycomms.util.Constants.IS_LOCAL_CONTACTS_LOADING_ENABLED,
                false)
                .apply();
        sp.edit().putBoolean(
                com.vodafone.mycomms.util.Constants.IS_GLOBAL_CONTACTS_LOADING_ENABLED,
                true)
                .apply();
        sp.edit().putString(
                com.vodafone.mycomms.util.Constants.PROFILE_ID_SHARED_PREF,
                mProfileId)
                .apply();
    }

    private void setSharedPreferencesNoLocal_AndNoGlobalContactsEnabled()
    {
        SharedPreferences sp = mContext.getSharedPreferences(
                com.vodafone.mycomms.util.Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        sp.edit().putBoolean(
                com.vodafone.mycomms.util.Constants.IS_LOCAL_CONTACTS_LOADING_ENABLED,
                false)
                .apply();
        sp.edit().putBoolean(
                com.vodafone.mycomms.util.Constants.IS_GLOBAL_CONTACTS_LOADING_ENABLED,
                false)
                .apply();
        sp.edit().putString(
                com.vodafone.mycomms.util.Constants.PROFILE_ID_SHARED_PREF,
                mProfileId)
                .apply();
    }
}
