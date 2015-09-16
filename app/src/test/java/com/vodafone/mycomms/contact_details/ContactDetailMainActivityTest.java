package com.vodafone.mycomms.contact_details;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chatgroup.GroupChatActivity;
import com.vodafone.mycomms.contacts.detail.ContactDetailMainActivity;
import com.vodafone.mycomms.contacts.detail.ContactDetailsPlusActivity;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.Constants;

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
import org.robolectric.shadows.ShadowIntent;

import java.util.ArrayList;

import io.realm.Realm;
import model.Contact;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by str_oan on 14/09/2015.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*"})
@PrepareForTest({Realm.class, Crashlytics.class ,EndpointWrapper.class, RealmContactTransactions.class})

public class ContactDetailMainActivityTest
{
    @Rule
    public PowerMockRule rule = new PowerMockRule();
    public ContactDetailMainActivity mActivity;
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
        mockParams();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testActivityLifeCycle() throws Exception
    {
        PowerMockito.mockStatic(RealmContactTransactions.class);
        PowerMockito.when(RealmContactTransactions.getFilteredContacts(Matchers.anyString(), Matchers.anyString(), Matchers.any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContactsList());
        Intent intent = new Intent();
        intent.putExtra(Constants.CONTACT_CONTACT_ID, "mc_55409316799f7e1a109446f4");
        mActivity = Robolectric.buildActivity(ContactDetailMainActivity.class).withIntent(intent)
                .create()
                .start()
                .resume()
                .pause()
                .stop()
                .destroy()
                .get();
        Assert.assertTrue(mActivity.isDestroyed());
    }

    @Test
    public void testConnectionAvailable()
    {
        ConnectivityManager connMgr =
                (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(true);
        setUpParams();
        setUpActivity();
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testConnectionAvailable Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }
        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        Assert.assertTrue(lay_no_connection.getVisibility() == View.GONE);

    }

    @Test
    public void testConnectionNotAvailable()
    {
        ConnectivityManager connMgr =
                (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(false);
        setUpParams();
        setUpActivity();
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testConnectionAvailable Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }
        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        Assert.assertTrue(lay_no_connection.getVisibility() == View.VISIBLE);

    }

    @Test
    public void testDetailsPlusActivity_Visible()
    {
        setUpParams();
        setUpActivity();
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testDetailsPlusActivity_Visible Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        LinearLayout detailsContainer = (LinearLayout) mActivity.findViewById(R.id.details_container);
        detailsContainer.performClick();

        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        Assert.assertTrue(null != startedIntent);
        Assert.assertTrue(startedIntent.hasExtra(com.vodafone.mycomms.util.Constants.CONTACT_DETAIL_INFO));
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(ContactDetailsPlusActivity.class.getName()));

    }

    @Test
    public void testButtonPhone_Clicked()
    {
        setUpParams();
        setUpActivity();
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testButtonPhone_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        ImageView btnPhone = (ImageView)mActivity.findViewById(R.id.btn_prof_phone);
        btnPhone.performClick();

        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getAction().equals(Intent.ACTION_CALL));

    }

    @Test
    public void testButtonEmail_Clicked()
    {
        setUpParams();
        setUpActivity();
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testButtonEmail_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        ImageView btnEmail = (ImageView) mActivity.findViewById(R.id.btn_prof_email);
        btnEmail.performClick();

        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getAction().equals(Intent.ACTION_SEND));
    }

    @Test
    public void testButtonChat_Clicked()
    {
        setUpParams();
        setUpActivity();
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testButtonEmail_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        ImageView btnChat = (ImageView) mActivity.findViewById(R.id.btn_prof_chat);
        btnChat.performClick();

        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        Assert.assertTrue(null != startedIntent);
        Assert.assertTrue(startedIntent.hasExtra(Constants.CHAT_FIELD_CONTACT_ID));
        Assert.assertTrue(startedIntent.hasExtra(Constants.CHAT_PREVIOUS_VIEW));
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(GroupChatActivity.class.getName()));
    }

    @Test
    public void testButtonChatLocal_Clicked()
    {
        SharedPreferences sp = mContext.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(Constants.PROFILE_ID_SHARED_PREF, "mc_5570340e7eb7c3512f2f9bf2").apply();
        PowerMockito.mockStatic(RealmContactTransactions.class);
        ArrayList<Contact> mockContactList = MockDataForTests.getMockContactsList();
        mockContactList.get(0).setPlatform(Constants.PLATFORM_LOCAL);
        PowerMockito.when(RealmContactTransactions.getFilteredContacts(Matchers.anyString(), Matchers.anyString(), Matchers.any(Realm.class)))
                .thenReturn(mockContactList);

        setUpActivity();
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testButtonEmail_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        ImageView btnChat = (ImageView) mActivity.findViewById(R.id.btn_prof_chat);
        btnChat.performClick();

        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getAction().equals(Intent.ACTION_VIEW));
    }

    @Test
    public void testContactWithoutPhone_NoEmail_NoOfficeLocation_WithPresence_WithLastSeen()
    {
        SharedPreferences sp = mContext.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(Constants.PROFILE_ID_SHARED_PREF, "mc_5570340e7eb7c3512f2f9bf2").apply();
        PowerMockito.mockStatic(RealmContactTransactions.class);
        ArrayList<Contact> mockContactList = MockDataForTests.getMockContactsList();
        mockContactList.get(0).setPhones(null);
        mockContactList.get(0).setEmails(null);
        mockContactList.get(0).setOfficeLocation(null);
        mockContactList.get(0).setPresence(
                "{\n" +
                        "\"icon\": \"mockIcon\"\n," +
                        "\"detail\": \"#LOCAL_TIME#\"\n" +
                        "}");
        mockContactList.get(0).setLastSeen(Long.parseLong("123456"));
        PowerMockito.when(RealmContactTransactions.getFilteredContacts(Matchers.anyString(), Matchers.anyString(), Matchers.any(Realm.class)))
                .thenReturn(mockContactList);

        setUpActivity();
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testButtonEmail_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        int phoneDrawable = R.drawable.btn_prof_phone_off;
        int chatDrawable = R.drawable.btn_prof_chat_off;
        Drawable imageButton = mActivity.getResources().getDrawable(phoneDrawable);
        Drawable imageChat = mActivity.getResources().getDrawable(chatDrawable);
        ImageView btnPhone = (ImageView)mActivity.findViewById(R.id.btn_prof_phone);
        ImageView btnChat = (ImageView)mActivity.findViewById(R.id.btn_prof_chat);
        Assert.assertTrue(btnPhone.getDrawable().equals(imageButton));
        Assert.assertTrue(btnChat.getDrawable().equals(imageChat));

        int emailDrawable = R.drawable.btn_prof_email_off;
        Drawable emailButton = mActivity.getResources().getDrawable(emailDrawable);
        ImageView btnEmail = (ImageView)mActivity.findViewById(R.id.btn_prof_email);
        Assert.assertTrue(btnEmail.getDrawable().equals(emailButton));

        TextView tvOfficeLocation = (TextView)mActivity.findViewById(R.id.contact_office_location);
        Assert.assertTrue(tvOfficeLocation.getVisibility() == View.GONE);
    }


    @Test
    public void testButtonCalendar_Clicked()
    {
        setUpParams();
        setUpActivity();
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testButtonEmail_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        ImageView btnCalendar = (ImageView) mActivity.findViewById(R.id.btn_prof_calendar);
        btnCalendar.performClick();

        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        Assert.assertTrue(null != startedIntent);
        Assert.assertTrue(startedIntent.hasExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME));
        Assert.assertTrue(startedIntent.hasExtra(CalendarContract.EXTRA_EVENT_END_TIME));
        Assert.assertTrue(startedIntent.hasExtra(CalendarContract.Events.TITLE));
        Assert.assertTrue(startedIntent.hasExtra(CalendarContract.Events.DESCRIPTION));
        Assert.assertTrue(startedIntent.hasExtra(CalendarContract.Events.AVAILABILITY));
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getAction().equals(Intent.ACTION_INSERT));
    }

    @Test
    public void testContactIsFavorite_StarVisibility()
    {
        setUpParams();
        PowerMockito.when(RealmContactTransactions.favouriteContactIsInRealm(Matchers.anyString(), Matchers.any(Realm.class)))
                .thenReturn(true);
        setUpActivity();
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testContactIsFavorite_StarVisibility Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        ImageView btFavourite = (ImageView)mActivity.findViewById(R.id.btFavourite);
        Assert.assertTrue(null != btFavourite);

        int imageStarOn = R.mipmap.icon_favorite_colour;
        Drawable imageStar = mActivity.getResources().getDrawable(imageStarOn);
        Assert.assertTrue(null != imageStar);

        Assert.assertTrue(btFavourite.getDrawable().equals(imageStar));
    }

    @Test
    public void testContactIsNOTFavorite_StarVisibility()
    {
        setUpParams();
        PowerMockito.when(RealmContactTransactions.favouriteContactIsInRealm(Matchers.anyString(), Matchers.any(Realm.class)))
                .thenReturn(false);
        setUpActivity();
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testContactIsNOTFavorite_StarVisibility Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        ImageView btFavourite = (ImageView)mActivity.findViewById(R.id.btFavourite);
        Assert.assertTrue(null != btFavourite);

        int imageStarOff = R.mipmap.icon_favorite_grey;
        Drawable imageStar = mActivity.getResources().getDrawable(imageStarOff);
        Assert.assertTrue(null != imageStar);

        Assert.assertTrue(btFavourite.getDrawable().equals(imageStar));
    }

    @Test
    public void testFavoriteStarOn_Clicked()
    {
        setUpParams();
        PowerMockito.when(RealmContactTransactions.favouriteContactIsInRealm(Matchers.anyString(), Matchers.any(Realm.class)))
                .thenReturn(true);
        setUpActivity();
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testFavoriteStarOn_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }
        ImageView btFavourite = (ImageView)mActivity.findViewById(R.id.btFavourite);
        btFavourite.performClick();

        int imageStarOff = R.mipmap.icon_favorite_grey;
        Drawable imageStar = mActivity.getResources().getDrawable(imageStarOff);
        Assert.assertTrue(null != imageStar);
        Assert.assertTrue(btFavourite.getDrawable().equals(imageStar));
    }

    @Test
    public void testFavoriteStarOff_Clicked()
    {
        setUpParams();
        PowerMockito.when(RealmContactTransactions.favouriteContactIsInRealm(Matchers.anyString(), Matchers.any(Realm.class)))
                .thenReturn(false);
        setUpActivity();
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testFavoriteStarOff_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }
        ImageView btFavourite = (ImageView)mActivity.findViewById(R.id.btFavourite);
        btFavourite.performClick();

        int imageStarOff = R.mipmap.icon_favorite_colour;
        Drawable imageStar = mActivity.getResources().getDrawable(imageStarOff);
        Assert.assertTrue(null != imageStar);
        Assert.assertTrue(btFavourite.getDrawable().equals(imageStar));
    }

    @Test
    public void testBackButton_Clicked()
    {
        setUpParams();
        PowerMockito.when(RealmContactTransactions.favouriteContactIsInRealm(Matchers.anyString(), Matchers.any(Realm.class)))
                .thenReturn(true);
        setUpActivity();
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }
        ImageView ivBtBack = (ImageView)mActivity.findViewById(R.id.ivBtBack);
        ivBtBack.performClick();

        Assert.assertTrue(mActivity.isFinishing());
    }

    @Test
    public void testBackLayout_Clicked()
    {
        setUpParams();
        PowerMockito.when(RealmContactTransactions.favouriteContactIsInRealm(Matchers.anyString(), Matchers.any(Realm.class)))
                .thenReturn(true);
        setUpActivity();
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }
        LinearLayout backArea = (LinearLayout)mActivity.findViewById(R.id.back_area);
        backArea.performClick();

        Assert.assertTrue(mActivity.isFinishing());
    }

    @Test
    public void testServerWithErrorResponse()
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

        setUpParams();
        setUpActivity();
        Contact contact = mActivity.getContact();
        try {
            Thread.sleep(3000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }
        Contact contact2 = mActivity.getContact();
        Assert.assertTrue(contact.equals(contact2));
    }

    @Test
    public void testServerWithCorrectResponse()
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
        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(MockDataForTests.getContactJSONObjectWithDataTagAsJSONArray().toString()));

        setUpParams();
        setUpActivity();
        try {
            Thread.sleep(3000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }
        Contact contact = mActivity.getContact();
        Assert.assertTrue(contact.getFirstName().equals(MockDataForTests.getMockContact().getFirstName()));
    }

    @Test
    public void testOnConnectivityChanged_HasInternet_Event()
    {
        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET);
        setUpParams();
        PowerMockito.when(RealmContactTransactions.favouriteContactIsInRealm(Matchers.anyString(), Matchers.any(Realm.class)))
                .thenReturn(true);
        setUpActivity();
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        BusProvider.getInstance().post(event);
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.GONE);
    }

    @Test
    public void testOnConnectivityChanged_Unknown_Event()
    {
        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.UNKNOWN);
        setUpParams();
        PowerMockito.when(RealmContactTransactions.favouriteContactIsInRealm(Matchers.anyString(), Matchers.any(Realm.class)))
                .thenReturn(true);
        setUpActivity();
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        BusProvider.getInstance().post(event);
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);
    }

    @Test
    public void testOnConnectivityChanged_Offline_Event()
    {
        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.OFFLINE);
        setUpParams();
        PowerMockito.when(RealmContactTransactions.favouriteContactIsInRealm(Matchers.anyString(), Matchers.any(Realm.class)))
                .thenReturn(true);
        setUpActivity();
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        BusProvider.getInstance().post(event);
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);
    }

    @Test
    public void testOnConnectivityChanged_ConnectedNoInternet_Event()
    {
        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED_HAS_NO_INTERNET);
        setUpParams();
        PowerMockito.when(RealmContactTransactions.favouriteContactIsInRealm(Matchers.anyString(), Matchers.any(Realm.class)))
                .thenReturn(true);
        setUpActivity();
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        BusProvider.getInstance().post(event);
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);
    }

    @Test
    public void testOnConnectivityChanged_WifiConnected_Event()
    {
        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED);
        setUpParams();
        PowerMockito.when(RealmContactTransactions.favouriteContactIsInRealm(Matchers.anyString(), Matchers.any(Realm.class)))
                .thenReturn(true);
        setUpActivity();
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        BusProvider.getInstance().post(event);
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);
    }

    @Test
    public void testOnConnectivityChanged_MobileConnected_Event()
    {
        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.MOBILE_CONNECTED);
        setUpParams();
        PowerMockito.when(RealmContactTransactions.favouriteContactIsInRealm(Matchers.anyString(), Matchers.any(Realm.class)))
                .thenReturn(true);
        setUpActivity();
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        BusProvider.getInstance().post(event);
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.GONE);
    }

    private void setUpParams()
    {
        SharedPreferences sp = mContext.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(Constants.PROFILE_ID_SHARED_PREF, "mc_5570340e7eb7c3512f2f9bf2").apply();
        PowerMockito.mockStatic(RealmContactTransactions.class);
        PowerMockito.when(RealmContactTransactions.getFilteredContacts(Matchers.anyString(), Matchers.anyString(), Matchers.any(Realm.class)))
                .thenReturn(MockDataForTests.getMockContactsList());
    }

    private void setUpActivity()
    {
        Intent intent = new Intent();
        intent.putExtra(Constants.CONTACT_CONTACT_ID, "mc_55409316799f7e1a109446f4");
        mActivity = Robolectric.buildActivity(ContactDetailMainActivity.class)
                .withIntent(intent).create().start().get();
    }

    private void mockParams()
    {
        Downloader downloader = new OkHttpDownloader(mContext, Long.MAX_VALUE);
        Picasso.Builder builder = new Picasso.Builder(mContext);
        builder.downloader(downloader);
        MycommsApp.picasso = builder.build();
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
}
