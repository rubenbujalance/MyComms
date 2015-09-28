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
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.CustomPreferencesFragmentActivity;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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
        mAccountsFragment.getActivity().finish();
        mAccountsFragment = null;
        mCustomPreferencesFragmentActivity = null;
        mContext = null;
        System.gc();
    }

    @Test
    public void testLocalContactsEnabled()
    {
        mockStatics_ContactPlatformExists();
        setSharedPreferencesWithLocal_NOGlobalContactsEnabled();
        startAccountsFragment();
        TextView tvAddLocalContacts = (TextView) mAccountsFragment.getView().findViewById(R.id.btn_add_local_contacts);
        ImageView imgCheckLocalContacts = (ImageView) mAccountsFragment.getView().findViewById(R.id.acc_check_local_contacts);
        TextView vAddGlobalContacts = (TextView) mAccountsFragment.getView().findViewById(R.id.btn_add_vodafone_global);
        ImageView imgCheckGlobalContacts = (ImageView) mAccountsFragment.getView().findViewById(R.id.acc_check_vodafone_global);

        Assert.assertTrue(tvAddLocalContacts.getVisibility() == View.GONE);
        Assert.assertTrue(imgCheckLocalContacts.getVisibility() == View.VISIBLE);
        Assert.assertTrue(vAddGlobalContacts.getVisibility() == View.VISIBLE);
        Assert.assertTrue(imgCheckGlobalContacts.getVisibility() == View.GONE);
    }

    @Test
    public void testGlobalContactsEnabled()
    {
        mockStatics_ContactPlatformNotExists();
        setSharedPreferencesNOLocal_AndGlobalContactsEnabled();
        startAccountsFragment();
        TextView tvAddLocalContacts = (TextView) mAccountsFragment.getView().findViewById(R.id.btn_add_local_contacts);
        ImageView imgCheckLocalContacts = (ImageView) mAccountsFragment.getView().findViewById(R.id.acc_check_local_contacts);
        TextView vAddGlobalContacts = (TextView) mAccountsFragment.getView().findViewById(R.id.btn_add_vodafone_global);
        ImageView imgCheckGlobalContacts = (ImageView) mAccountsFragment.getView().findViewById(R.id.acc_check_vodafone_global);

        Assert.assertTrue(tvAddLocalContacts.getVisibility() == View.VISIBLE);
        Assert.assertTrue(imgCheckLocalContacts.getVisibility() == View.GONE);
        Assert.assertTrue(vAddGlobalContacts.getVisibility() == View.GONE);
        Assert.assertTrue(imgCheckGlobalContacts.getVisibility() == View.VISIBLE);
    }

    @Test
    public void testAddGlobalContacts() throws Exception
    {
        mockStatics_ContactPlatformExists();
        setSharedPreferencesNoLocal_AndNoGlobalContactsEnabled();
        startAccountsFragment();
        TextView vAddGlobalContacts = (TextView) mAccountsFragment.getView().findViewById(R.id.btn_add_vodafone_global);
        vAddGlobalContacts.performClick();
        checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(mAccountsFragment.getActivity());
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        Assert.assertTrue(startedIntent.getComponent().getClassName().compareTo(AddGlobalContactsActivity.class.getName()) == 0);
    }

    @Test
    public void testAddLocalContacts() throws Exception
    {
        mockStatics_ContactPlatformExists();
        setSharedPreferencesNoLocal_AndNoGlobalContactsEnabled();
        startAccountsFragment();
        TextView tvAddLocalContacts = (TextView) mAccountsFragment.getView().findViewById(R.id.btn_add_local_contacts);
        ImageView imgCheckLocalContacts = (ImageView) mAccountsFragment.getView().findViewById(R.id.acc_check_local_contacts);
        TextView vAddGlobalContacts = (TextView) mAccountsFragment.getView().findViewById(R.id.btn_add_vodafone_global);
        ImageView imgCheckGlobalContacts = (ImageView) mAccountsFragment.getView().findViewById(R.id.acc_check_vodafone_global);
        tvAddLocalContacts.performClick();
        checkThreadSchedulers();
        Thread.sleep(2000);

        Assert.assertTrue(tvAddLocalContacts.getVisibility() == View.GONE);
        Assert.assertTrue(imgCheckLocalContacts.getVisibility() == View.VISIBLE);
        Assert.assertTrue(vAddGlobalContacts.getVisibility() == View.VISIBLE);
        Assert.assertTrue(imgCheckGlobalContacts.getVisibility() == View.GONE);
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

    private void checkThreadSchedulers()
    {
        if(Robolectric.getBackgroundThreadScheduler().areAnyRunnable())
            Robolectric.flushBackgroundThreadScheduler();
        if(Robolectric.getForegroundThreadScheduler().areAnyRunnable())
            Robolectric.flushForegroundThreadScheduler();
    }


}
