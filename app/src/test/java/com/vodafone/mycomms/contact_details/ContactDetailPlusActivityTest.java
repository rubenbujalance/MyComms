package com.vodafone.mycomms.contact_details;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chatgroup.GroupChatActivity;
import com.vodafone.mycomms.contacts.detail.ContactDetailsPlusActivity;
import com.vodafone.mycomms.events.BusProvider;
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

import io.realm.Realm;
import model.Contact;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by str_oan on 15/09/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 18,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*"})
@PrepareForTest({Realm.class, Crashlytics.class})
public class ContactDetailPlusActivityTest
{
    @Rule
    public PowerMockRule rule = new PowerMockRule();
    public ContactDetailsPlusActivity mActivity;

    @Before
    public void setUp()
    {
        mockStatic(Realm.class);
        when(Realm.getDefaultInstance()).thenReturn(null);
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
        mockPicasso();
    }

    @After
    public void tearDown() throws Exception
    {
        MockDataForTests.checkThreadSchedulers();
        Robolectric.reset();
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testActivityLifeCycle() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        Contact contact = MockDataForTests.getMockContact();
        String contactDetail[] = new String[10];

        contactDetail[0] = contact.getFirstName();
        contactDetail[1] = contact.getLastName();
        contactDetail[2] = contact.getPhones();
        contactDetail[3] = contact.getEmails();
        contactDetail[4] = contact.getOfficeLocation();
        contactDetail[5] = contact.getPosition();
        contactDetail[6] = contact.getAvatar();
        contactDetail[7] = contact.getPlatform();
        contactDetail[8] = contact.getContactId();
        contactDetail[9] = contact.getStringField1(); //SFAvatar

        Intent intent = new Intent();
        intent.putExtra(Constants.CONTACT_DETAIL_INFO, contactDetail);
        mActivity = Robolectric.buildActivity(ContactDetailsPlusActivity.class).withIntent(intent)
                .create()
                .start()
                .resume()
                .pause()
                .stop()
                .destroy()
                .get();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(mActivity.isDestroyed());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testConnectionAvailable()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        ConnectivityManager connMgr =
            (ConnectivityManager)(RuntimeEnvironment.application.getApplicationContext()).getSystemService(Context.CONNECTIVITY_SERVICE);
        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(true);
        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        LinearLayout layNoConnection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        Assert.assertTrue(layNoConnection.getVisibility() == View.GONE);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testConnectionNotAvailable()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        ConnectivityManager connMgr =
                (ConnectivityManager)(RuntimeEnvironment.application.getApplicationContext()).getSystemService(Context.CONNECTIVITY_SERVICE);
        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(false);
        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        LinearLayout layNoConnection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
        Assert.assertTrue(layNoConnection.getVisibility() == View.VISIBLE);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testLoadContactInfo_NotLocalContact()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        TextView tvContactName = (TextView) mActivity.findViewById(R.id.contact_contact_name);
        Assert.assertNotNull(tvContactName);
        Assert.assertNotNull(tvContactName.getText().toString());

        TextView tvPhoneNumber = (TextView) mActivity.findViewById(R.id.contact_phone_number);
        Assert.assertNotNull(tvPhoneNumber);
        Assert.assertNotNull(tvPhoneNumber.getText().toString());

        TextView tvEmail = (TextView) mActivity.findViewById(R.id.contact_email);
        Assert.assertNotNull(tvEmail);
        Assert.assertNotNull(tvEmail.getText().toString());

        TextView contactOfficeLabel = (TextView) mActivity.findViewById(R.id.contact_location_label);
        Assert.assertNotNull(contactOfficeLabel);
        Assert.assertNotNull(contactOfficeLabel.getText().toString());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testLoadContactInfo_LocalContact_NoPhone_NoEmail()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        Contact contact = MockDataForTests.getMockContact();
        String contactDetail[] = new String[10];

        contactDetail[0] = contact.getFirstName();
        contactDetail[1] = contact.getLastName();
        contactDetail[2] = null;
        contactDetail[3] = null;
        contactDetail[4] = contact.getOfficeLocation();
        contactDetail[5] = contact.getPosition();
        contactDetail[6] = contact.getAvatar();
        contactDetail[7] = Constants.PLATFORM_LOCAL;
        contactDetail[8] = contact.getContactId();
        contactDetail[9] = contact.getStringField1(); //SFAvatar

        Intent intent = new Intent();
        intent.putExtra(Constants.CONTACT_DETAIL_INFO, contactDetail);

        mActivity = Robolectric.buildActivity(ContactDetailsPlusActivity.class)
                .withIntent(intent).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();

        TextView tvContactName = (TextView) mActivity.findViewById(R.id.contact_contact_name);
        Assert.assertNotNull(tvContactName);
        Assert.assertNotNull(tvContactName.getText().toString());

        LinearLayout layoutPhone = (LinearLayout) mActivity.findViewById(R.id.layout_phone);
        Assert.assertTrue(layoutPhone.getVisibility() == View.GONE);

        LinearLayout layoutEmail = (LinearLayout) mActivity.findViewById(R.id.layout_email);
        Assert.assertTrue(layoutEmail.getVisibility() == View.GONE);

        TextView contactOfficeLabel = (TextView) mActivity.findViewById(R.id.contact_location_label);
        Assert.assertNotNull(contactOfficeLabel);
        Assert.assertNotNull(contactOfficeLabel.getText().toString());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testLoadContactInfo_LocalContact_WithPhone_WithEmail()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        Contact contact = MockDataForTests.getMockContact();
        String contactDetail[] = new String[10];

        contactDetail[0] = contact.getFirstName();
        contactDetail[1] = contact.getLastName();
        contactDetail[2] = contact.getPhones();
        contactDetail[3] = contact.getEmails();
        contactDetail[4] = contact.getOfficeLocation();
        contactDetail[5] = contact.getPosition();
        contactDetail[6] = contact.getAvatar();
        contactDetail[7] = Constants.PLATFORM_LOCAL;
        contactDetail[8] = contact.getContactId();
        contactDetail[9] = contact.getStringField1(); //SFAvatar

        Intent intent = new Intent();
        intent.putExtra(Constants.CONTACT_DETAIL_INFO, contactDetail);

        mActivity = Robolectric.buildActivity(ContactDetailsPlusActivity.class)
                .withIntent(intent).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();

        TextView tvContactName = (TextView) mActivity.findViewById(R.id.contact_contact_name);
        Assert.assertNotNull(tvContactName);
        Assert.assertNotNull(tvContactName.getText().toString());

        TextView tvPhoneNumber = (TextView) mActivity.findViewById(R.id.contact_phone_number);
        Assert.assertNotNull(tvPhoneNumber);
        Assert.assertNotNull(tvPhoneNumber.getText().toString());

        TextView tvEmail = (TextView) mActivity.findViewById(R.id.contact_email);
        Assert.assertNotNull(tvEmail);
        Assert.assertNotNull(tvEmail.getText().toString());

        TextView contactOfficeLabel = (TextView) mActivity.findViewById(R.id.contact_location_label);
        Assert.assertNotNull(contactOfficeLabel);
        Assert.assertNotNull(contactOfficeLabel.getText().toString());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testButtonChatClick_LocalContact()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        Contact contact = MockDataForTests.getMockContact();
        String contactDetail[] = new String[10];

        contactDetail[0] = contact.getFirstName();
        contactDetail[1] = contact.getLastName();
        contactDetail[2] =
            "{\n" +
            "\"country\": \"ES\",\n" +
            "\"phone\": \"+34659562976\"\n" +
            "}\n";
        contactDetail[3] =
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n";
        contactDetail[4] = contact.getOfficeLocation();
        contactDetail[5] = contact.getPosition();
        contactDetail[6] = contact.getAvatar();
        contactDetail[7] = Constants.PLATFORM_LOCAL;
        contactDetail[8] = contact.getContactId();
        contactDetail[9] = contact.getStringField1(); //SFAvatar

        Intent intent = new Intent();
        intent.putExtra(Constants.CONTACT_DETAIL_INFO, contactDetail);

        mActivity = Robolectric.buildActivity(ContactDetailsPlusActivity.class)
                .withIntent(intent).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();

        ImageView btnChat = (ImageView) mActivity.findViewById(R.id.btn_prof_chat);
        btnChat.performClick();
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getAction().equals(Intent.ACTION_VIEW));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testButtonChatClick_NotLocalContact()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        ImageView btnChat = (ImageView) mActivity.findViewById(R.id.btn_prof_chat);
        btnChat.performClick();
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        Assert.assertNotNull(startedIntent);
        Assert.assertTrue(startedIntent.hasExtra(Constants.CHAT_FIELD_CONTACT_ID));
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(GroupChatActivity.class.getName()));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testButtonEmailClick_LocalContact()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        Contact contact = MockDataForTests.getMockContact();
        String contactDetail[] = new String[10];

        contactDetail[0] = contact.getFirstName();
        contactDetail[1] = contact.getLastName();
        contactDetail[2] =
                "{\n" +
                        "\"country\": \"ES\",\n" +
                        "\"phone\": \"+34659562976\"\n" +
                        "}\n";
        contactDetail[3] =
                "{\n" +
                        "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                        "}\n";
        contactDetail[4] = contact.getOfficeLocation();
        contactDetail[5] = contact.getPosition();
        contactDetail[6] = contact.getAvatar();
        contactDetail[7] = Constants.PLATFORM_LOCAL;
        contactDetail[8] = contact.getContactId();
        contactDetail[9] = contact.getStringField1(); //SFAvatar

        Intent intent = new Intent();
        intent.putExtra(Constants.CONTACT_DETAIL_INFO, contactDetail);

        mActivity = Robolectric.buildActivity(ContactDetailsPlusActivity.class)
                .withIntent(intent).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();

        ImageView btnEmail = (ImageView)mActivity.findViewById(R.id.btn_prof_email);
        btnEmail.performClick();
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getAction().equals(Intent.ACTION_SEND));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testButtonEmailClick_NotLocalContact()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        ImageView btnEmail = (ImageView)mActivity.findViewById(R.id.btn_prof_email);
        btnEmail.performClick();
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getAction().equals(Intent.ACTION_SEND));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testButtonPhoneClick_LocalContact()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        Contact contact = MockDataForTests.getMockContact();
        String contactDetail[] = new String[10];

        contactDetail[0] = contact.getFirstName();
        contactDetail[1] = contact.getLastName();
        contactDetail[2] =
                "{\n" +
                        "\"country\": \"ES\",\n" +
                        "\"phone\": \"+34659562976\"\n" +
                        "}\n";
        contactDetail[3] =
                "{\n" +
                        "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                        "}\n";
        contactDetail[4] = contact.getOfficeLocation();
        contactDetail[5] = contact.getPosition();
        contactDetail[6] = contact.getAvatar();
        contactDetail[7] = Constants.PLATFORM_LOCAL;
        contactDetail[8] = contact.getContactId();
        contactDetail[9] = contact.getStringField1(); //SFAvatar

        Intent intent = new Intent();
        intent.putExtra(Constants.CONTACT_DETAIL_INFO, contactDetail);

        mActivity = Robolectric.buildActivity(ContactDetailsPlusActivity.class)
                .withIntent(intent).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();

        ImageView btnPhone = (ImageView)mActivity.findViewById(R.id.btn_prof_phone);
        btnPhone.performClick();
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getAction().equals(Intent.ACTION_CALL));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testButtonPhoneClick_NotLocalContact()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        ImageView btnPhone = (ImageView)mActivity.findViewById(R.id.btn_prof_phone);
        btnPhone.performClick();
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getAction().equals(Intent.ACTION_CALL));

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testLayoutBack_Click()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        LinearLayout linLayoutClose = (LinearLayout) mActivity.findViewById(R.id.layout_close);
        linLayoutClose.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(this.mActivity.isFinishing());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testOnConnectivityChanged_HasInternet_Event()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        ConnectivityChanged event;
        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);

        event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();

        Assert.assertEquals(lay_no_connection.getVisibility(), View.GONE);

        event = new ConnectivityChanged(ConnectivityStatus.OFFLINE);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();

        Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testConnectivityChangedEvent()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        ConnectivityChanged event;
        setUpActivity();
        MockDataForTests.checkThreadSchedulers();

        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);

        event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();

        Assert.assertEquals(lay_no_connection.getVisibility(), View.GONE);

        event = new ConnectivityChanged(ConnectivityStatus.UNKNOWN);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();

        Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);

        event = new ConnectivityChanged(ConnectivityStatus.OFFLINE);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();

        Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);

        event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED_HAS_NO_INTERNET);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();

        Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);

        event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();

        Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);

        event = new ConnectivityChanged(ConnectivityStatus.MOBILE_CONNECTED);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();
        
        Assert.assertEquals(lay_no_connection.getVisibility(), View.GONE);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    private void setUpActivity()
    {
        Contact contact = MockDataForTests.getMockContact();
        String contactDetail[] = new String[10];

        contactDetail[0] = contact.getFirstName();
        contactDetail[1] = contact.getLastName();
        contactDetail[2] = contact.getPhones();
        contactDetail[3] = contact.getEmails();
        contactDetail[4] = contact.getOfficeLocation();
        contactDetail[5] = contact.getPosition();
        contactDetail[6] = contact.getAvatar();
        contactDetail[7] = contact.getPlatform();
        contactDetail[8] = contact.getContactId();
        contactDetail[9] = contact.getStringField1(); //SFAvatar

        Intent intent = new Intent();
        intent.putExtra(Constants.CONTACT_DETAIL_INFO, contactDetail);

        mActivity = Robolectric.buildActivity(ContactDetailsPlusActivity.class)
                .withIntent(intent).create().start().resume().get();
    }

    private void mockPicasso()
    {
        Downloader downloader = new OkHttpDownloader(RuntimeEnvironment.application.getApplicationContext(), Long.MAX_VALUE);
        Picasso.Builder builder = new Picasso.Builder(RuntimeEnvironment.application.getApplicationContext());
        builder.downloader(downloader);
        MycommsApp.picasso = builder.build();
    }


}
