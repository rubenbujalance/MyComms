package com.vodafone.mycomms.contact_details;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
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
import com.vodafone.mycomms.contacts.connection.FavouriteController;
import com.vodafone.mycomms.contacts.detail.ContactDetailMainActivity;
import com.vodafone.mycomms.contacts.detail.ContactDetailsPlusActivity;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.Constants;

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
import org.robolectric.shadows.ShadowIntent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import io.realm.Realm;
import model.Contact;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by str_oan on 14/09/2015.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 18,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*"})
@PrepareForTest({Realm.class, Crashlytics.class ,EndpointWrapper.class, RealmContactTransactions.class, FavouriteController.class})

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

    @After
    public void tearDown() throws Exception
    {
        MockDataForTests.checkThreadSchedulers();
        //MockDataForTests.resetWindowManager(this.getClass());
        if (webServer != null) webServer.shutdown();
        Robolectric.reset();
        webServer = null;
        mActivity = null;
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
    public void testConnectionAvailable()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        ConnectivityManager connMgr =
                (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(true);

        setUpParams();
        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        Assert.assertTrue(lay_no_connection.getVisibility() == View.GONE);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testConnectionNotAvailable()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        ConnectivityManager connMgr =
                (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(false);
        setUpParams();
        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        Assert.assertTrue(lay_no_connection.getVisibility() == View.VISIBLE);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testDetailsPlusActivity_Visible() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setUpParams();
        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        LinearLayout detailsContainer = (LinearLayout) mActivity.findViewById(R.id.details_container);
        detailsContainer.performClick();
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(ContactDetailsPlusActivity.class.getName()));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testButton_Phone_Clicked() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setUpParams();
        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        ImageView btnPhone = (ImageView)mActivity.findViewById(R.id.btn_prof_phone);
        btnPhone.performClick();
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getAction().compareTo(Intent.ACTION_CALL) == 0);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testButton_Email_Clicked() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setUpParams();
        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        ImageView btnEmail = (ImageView) mActivity.findViewById(R.id.btn_prof_email);
        btnEmail.performClick();
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getAction().compareTo(Intent.ACTION_SEND) == 0);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testButton_Calendar_Clicked() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setUpParams();
        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        ImageView btnCalendar = (ImageView) mActivity.findViewById(R.id.btn_prof_calendar);
        btnCalendar.performClick();
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getAction().compareTo(Intent.ACTION_INSERT) == 0);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testButton_Back_Clicked() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setUpParams();
        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        ImageView ivBtBack = (ImageView)mActivity.findViewById(R.id.ivBtBack);
        ivBtBack.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(mActivity.isFinishing());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testBackLayout_Clicked() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setUpParams();
        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        LinearLayout backArea = (LinearLayout)mActivity.findViewById(R.id.back_area);
        backArea.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(mActivity.isFinishing());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testButtonChat_Clicked() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setUpParams();
        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        ImageView btnChat = (ImageView) mActivity.findViewById(R.id.btn_prof_chat);
        btnChat.performClick();
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(GroupChatActivity.class.getName()));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testButtonChatLocal_Clicked() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        SharedPreferences sp = mContext.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(Constants.PROFILE_ID_SHARED_PREF, "mc_5570340e7eb7c3512f2f9bf2").apply();
        PowerMockito.mockStatic(RealmContactTransactions.class);
        ArrayList<Contact> mockContactList = MockDataForTests.getMockContactsList();
        mockContactList.get(0).setPlatform(Constants.PLATFORM_LOCAL);
        PowerMockito.when(RealmContactTransactions.getFilteredContacts(Matchers.anyString(), Matchers.anyString(), Matchers.any(Realm.class)))
                .thenReturn(mockContactList);

        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        ImageView btnChat = (ImageView) mActivity.findViewById(R.id.btn_prof_chat);
        Assert.assertTrue(btnChat.performClick());
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getAction().equals(Intent.ACTION_VIEW));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testContactWithoutPhone_NoEmail_WithOfficeLocation_WithPresence_WithLastSeen() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        SharedPreferences sp = mContext.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(Constants.PROFILE_ID_SHARED_PREF, "mc_5570340e7eb7c3512f2f9bf2").apply();
        PowerMockito.mockStatic(RealmContactTransactions.class);
        ArrayList<Contact> mockContactList = MockDataForTests.getMockContactsList();
        mockContactList.get(0).setPhones(null);
        mockContactList.get(0).setEmails(null);
        mockContactList.get(0).setOfficeLocation("mockLocation");
        mockContactList.get(0).setPresence(
                "{\n" +
                        "\"icon\": \"mockIcon\"\n," +
                        "\"detail\": \"#LOCAL_TIME#\"\n" +
                        "}");
        mockContactList.get(0).setLastSeen(Long.parseLong("123456"));
        PowerMockito.when(RealmContactTransactions.getFilteredContacts(Matchers.anyString(), Matchers.anyString(), Matchers.any(Realm.class)))
                .thenReturn(mockContactList);

        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

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
        Assert.assertTrue(tvOfficeLocation.getVisibility() == View.VISIBLE);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testContactIsFavorite_StarVisibility() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setUpParams();
        PowerMockito.mockStatic(FavouriteController.class);
        PowerMockito.when(FavouriteController.contactIsFavourite(Matchers.anyString()))
                .thenReturn(true);
        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        ImageView btFavourite = (ImageView)mActivity.findViewById(R.id.btFavourite);
        Assert.assertTrue(null != btFavourite);

        int imageStarOn = R.mipmap.icon_favorite_colour;
        Drawable imageStar = mActivity.getResources().getDrawable(imageStarOn);
        Assert.assertTrue(null != imageStar);

        Assert.assertTrue(btFavourite.getDrawable().equals(imageStar));

        btFavourite.performClick();
        MockDataForTests.checkThreadSchedulers();

        int imageStarOff = R.mipmap.icon_favorite_grey;
        imageStar = mActivity.getResources().getDrawable(imageStarOff);
        Assert.assertTrue(null != imageStar);
        Assert.assertTrue(btFavourite.getDrawable().equals(imageStar));

        btFavourite.performClick();
        MockDataForTests.checkThreadSchedulers();

        imageStar = mActivity.getResources().getDrawable(imageStarOn);
        Assert.assertTrue(null != imageStar);
        Assert.assertTrue(btFavourite.getDrawable().equals(imageStar));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testContactIsNOTFavorite_StarVisibility()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setUpParams();
        PowerMockito.mockStatic(FavouriteController.class);
        PowerMockito.when(FavouriteController.contactIsFavourite(Matchers.anyString()))
                .thenReturn(false);
        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        ImageView btFavourite = (ImageView)mActivity.findViewById(R.id.btFavourite);
        Assert.assertTrue(null != btFavourite);

        int imageStarOff = R.mipmap.icon_favorite_grey;
        Drawable imageStar = mActivity.getResources().getDrawable(imageStarOff);
        Assert.assertTrue(null != imageStar);

        Assert.assertTrue(btFavourite.getDrawable().equals(imageStar));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testServerWithErrorResponse()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

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
        MockDataForTests.checkThreadSchedulers();

        Contact contact = mActivity.getContact();
        Contact contact2 = mActivity.getContact();
        Assert.assertTrue(contact.equals(contact2));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testServerWithCorrectResponse()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setCorrectOKHTPResponse();
        setUpParams();
        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        Contact contact = mActivity.getContact();
        Assert.assertTrue(contact.getFirstName().equals(MockDataForTests.getMockContact().getFirstName()));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testOnConnectivityEvent()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET);
        setUpParams();
        PowerMockito.when(RealmContactTransactions.favouriteContactIsInRealm(Matchers.anyString(), Matchers.any(Realm.class)))
                .thenReturn(true);
        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.GONE);

        event = new ConnectivityChanged(ConnectivityStatus.UNKNOWN);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);

        event = new ConnectivityChanged(ConnectivityStatus.OFFLINE);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);

        event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED_HAS_NO_INTERNET);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);

        event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);

        event = new ConnectivityChanged(ConnectivityStatus.MOBILE_CONNECTED);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();
        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.GONE);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    private void setCorrectOKHTPResponse()
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
                .withIntent(intent).create().start().resume().visible().get();
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
