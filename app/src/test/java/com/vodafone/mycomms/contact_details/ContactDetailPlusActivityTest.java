//package com.vodafone.mycomms.contact_details;
//
//import android.annotation.TargetApi;
//import android.content.Context;
//import android.content.Intent;
//import android.net.ConnectivityManager;
//import android.os.Build;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import com.crashlytics.android.Crashlytics;
//import com.github.pwittchen.networkevents.library.ConnectivityStatus;
//import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
//import com.squareup.picasso.Downloader;
//import com.squareup.picasso.OkHttpDownloader;
//import com.squareup.picasso.Picasso;
//import com.vodafone.mycomms.BuildConfig;
//import com.vodafone.mycomms.MycommsApp;
//import com.vodafone.mycomms.R;
//import com.vodafone.mycomms.chatgroup.GroupChatActivity;
//import com.vodafone.mycomms.contacts.detail.ContactDetailsPlusActivity;
//import com.vodafone.mycomms.events.BusProvider;
//import com.vodafone.mycomms.test.util.MockDataForTests;
//import com.vodafone.mycomms.test.util.Util;
//import com.vodafone.mycomms.util.Constants;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.powermock.core.MockRepository;
//import org.powermock.core.classloader.annotations.PowerMockIgnore;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.rule.PowerMockRule;
//import org.robolectric.Robolectric;
//import org.robolectric.RobolectricGradleTestRunner;
//import org.robolectric.RuntimeEnvironment;
//import org.robolectric.Shadows;
//import org.robolectric.annotation.Config;
//import org.robolectric.shadows.ShadowActivity;
//import org.robolectric.shadows.ShadowIntent;
//
//import io.realm.Realm;
//import model.Contact;
//
//import static org.powermock.api.mockito.PowerMockito.mockStatic;
//import static org.powermock.api.mockito.PowerMockito.when;
//
///**
// * Created by str_oan on 15/09/2015.
// */
//@RunWith(RobolectricGradleTestRunner.class)
//@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
//        manifest = "./src/main/AndroidManifest.xml")
//@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
//        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*"})
//@PrepareForTest({Realm.class, Crashlytics.class})
//public class ContactDetailPlusActivityTest
//{
//    @Rule
//    public PowerMockRule rule = new PowerMockRule();
//    public ContactDetailsPlusActivity mActivity;
//
//    @Before
//    public void setUp()
//    {
//        mockStatic(Realm.class);
//        when(Realm.getDefaultInstance()).thenReturn(null);
//        mockStatic(Crashlytics.class);
//        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
//        mockPicasso();
//    }
//
//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
//    @Test
//    public void testActivityLifeCycle() throws Exception
//    {
//        Contact contact = MockDataForTests.getMockContact();
//        String contactDetail[] = new String[10];
//
//        contactDetail[0] = contact.getFirstName();
//        contactDetail[1] = contact.getLastName();
//        contactDetail[2] = contact.getPhones();
//        contactDetail[3] = contact.getEmails();
//        contactDetail[4] = contact.getOfficeLocation();
//        contactDetail[5] = contact.getPosition();
//        contactDetail[6] = contact.getAvatar();
//        contactDetail[7] = contact.getPlatform();
//        contactDetail[8] = contact.getContactId();
//        contactDetail[9] = contact.getStringField1(); //SFAvatar
//
//        Intent intent = new Intent();
//        intent.putExtra(Constants.CONTACT_DETAIL_INFO, contactDetail);
//        mActivity = Robolectric.buildActivity(ContactDetailsPlusActivity.class).withIntent(intent)
//                .create()
//                .start()
//                .resume()
//                .pause()
//                .stop()
//                .destroy()
//                .get();
//        Assert.assertTrue(mActivity.isDestroyed());
//    }
//
//    @Test
//    public void testConnectionAvailable()
//    {
//        ConnectivityManager connMgr =
//            (ConnectivityManager)(RuntimeEnvironment.application.getApplicationContext()).getSystemService(Context.CONNECTIVITY_SERVICE);
//        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(true);
//        setUpActivity();
//
//        try {
//            Thread.sleep(1000);
//        }
//        catch (Exception e)
//        {
//            System.err.println("******** Test: testConnectionAvailable Failed due to: ********\n"+e.getMessage());
//            Assert.fail();
//        }
//        LinearLayout layNoConnection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
//        Assert.assertTrue(layNoConnection.getVisibility() == View.GONE);
//
//    }
//
//    @Test
//    public void testConnectionNotAvailable()
//    {
//        ConnectivityManager connMgr =
//                (ConnectivityManager)(RuntimeEnvironment.application.getApplicationContext()).getSystemService(Context.CONNECTIVITY_SERVICE);
//        Shadows.shadowOf(connMgr.getActiveNetworkInfo()).setConnectionStatus(false);
//        setUpActivity();
//        try {
//            Thread.sleep(1000);
//        }
//        catch (Exception e)
//        {
//            System.err.println("******** Test: testConnectionAvailable Failed due to: ********\n"+e.getMessage());
//            Assert.fail();
//        }
//        LinearLayout layNoConnection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
//        Assert.assertTrue(layNoConnection.getVisibility() == View.VISIBLE);
//
//    }
//
//    @Test
//    public void testLoadContactInfo_NotLocalContact()
//    {
//        setUpActivity();
//        try {
//            Thread.sleep(1000);
//        }
//        catch (Exception e)
//        {
//            System.err.println("******** Test: testConnectionAvailable Failed due to: ********\n"+e.getMessage());
//            Assert.fail();
//        }
//
//        TextView tvContactName = (TextView) mActivity.findViewById(R.id.contact_contact_name);
//        Assert.assertNotNull(tvContactName);
//        Assert.assertNotNull(tvContactName.getText().toString());
//
//        TextView tvPhoneNumber = (TextView) mActivity.findViewById(R.id.contact_phone_number);
//        Assert.assertNotNull(tvPhoneNumber);
//        Assert.assertNotNull(tvPhoneNumber.getText().toString());
//
//        TextView tvEmail = (TextView) mActivity.findViewById(R.id.contact_email);
//        Assert.assertNotNull(tvEmail);
//        Assert.assertNotNull(tvEmail.getText().toString());
//
//        TextView contactOfficeLabel = (TextView) mActivity.findViewById(R.id.contact_location_label);
//        Assert.assertNotNull(contactOfficeLabel);
//        Assert.assertNotNull(contactOfficeLabel.getText().toString());
//    }
//
//    @Test
//    public void testLoadContactInfo_LocalContact_NoPhone_NoEmail()
//    {
//        Contact contact = MockDataForTests.getMockContact();
//        String contactDetail[] = new String[10];
//
//        contactDetail[0] = contact.getFirstName();
//        contactDetail[1] = contact.getLastName();
//        contactDetail[2] = null;
//        contactDetail[3] = null;
//        contactDetail[4] = contact.getOfficeLocation();
//        contactDetail[5] = contact.getPosition();
//        contactDetail[6] = contact.getAvatar();
//        contactDetail[7] = Constants.PLATFORM_LOCAL;
//        contactDetail[8] = contact.getContactId();
//        contactDetail[9] = contact.getStringField1(); //SFAvatar
//
//        Intent intent = new Intent();
//        intent.putExtra(Constants.CONTACT_DETAIL_INFO, contactDetail);
//
//        mActivity = Robolectric.buildActivity(ContactDetailsPlusActivity.class)
//                .withIntent(intent).create().start().resume().get();
//        try {
//            Thread.sleep(1000);
//        }
//        catch (Exception e)
//        {
//            System.err.println("******** Test: testConnectionAvailable Failed due to: ********\n"+e.getMessage());
//            Assert.fail();
//        }
//
//        TextView tvContactName = (TextView) mActivity.findViewById(R.id.contact_contact_name);
//        Assert.assertNotNull(tvContactName);
//        Assert.assertNotNull(tvContactName.getText().toString());
//
//        LinearLayout layoutPhone = (LinearLayout) mActivity.findViewById(R.id.layout_phone);
//        Assert.assertTrue(layoutPhone.getVisibility() == View.GONE);
//
//        LinearLayout layoutEmail = (LinearLayout) mActivity.findViewById(R.id.layout_email);
//        Assert.assertTrue(layoutEmail.getVisibility() == View.GONE);
//
//        TextView contactOfficeLabel = (TextView) mActivity.findViewById(R.id.contact_location_label);
//        Assert.assertNotNull(contactOfficeLabel);
//        Assert.assertNotNull(contactOfficeLabel.getText().toString());
//    }
//
//    @Test
//    public void testLoadContactInfo_LocalContact_WithPhone_WithEmail()
//    {
//        Contact contact = MockDataForTests.getMockContact();
//        String contactDetail[] = new String[10];
//
//        contactDetail[0] = contact.getFirstName();
//        contactDetail[1] = contact.getLastName();
//        contactDetail[2] = contact.getPhones();
//        contactDetail[3] = contact.getEmails();
//        contactDetail[4] = contact.getOfficeLocation();
//        contactDetail[5] = contact.getPosition();
//        contactDetail[6] = contact.getAvatar();
//        contactDetail[7] = Constants.PLATFORM_LOCAL;
//        contactDetail[8] = contact.getContactId();
//        contactDetail[9] = contact.getStringField1(); //SFAvatar
//
//        Intent intent = new Intent();
//        intent.putExtra(Constants.CONTACT_DETAIL_INFO, contactDetail);
//
//        mActivity = Robolectric.buildActivity(ContactDetailsPlusActivity.class)
//                .withIntent(intent).create().start().resume().get();
//        try {
//            Thread.sleep(1000);
//        }
//        catch (Exception e)
//        {
//            System.err.println("******** Test: testConnectionAvailable Failed due to: ********\n"+e.getMessage());
//            Assert.fail();
//        }
//
//        TextView tvContactName = (TextView) mActivity.findViewById(R.id.contact_contact_name);
//        Assert.assertNotNull(tvContactName);
//        Assert.assertNotNull(tvContactName.getText().toString());
//
//        TextView tvPhoneNumber = (TextView) mActivity.findViewById(R.id.contact_phone_number);
//        Assert.assertNotNull(tvPhoneNumber);
//        Assert.assertNotNull(tvPhoneNumber.getText().toString());
//
//        TextView tvEmail = (TextView) mActivity.findViewById(R.id.contact_email);
//        Assert.assertNotNull(tvEmail);
//        Assert.assertNotNull(tvEmail.getText().toString());
//
//        TextView contactOfficeLabel = (TextView) mActivity.findViewById(R.id.contact_location_label);
//        Assert.assertNotNull(contactOfficeLabel);
//        Assert.assertNotNull(contactOfficeLabel.getText().toString());
//    }
//
//    @Test
//    public void testButtonChatClick_LocalContact()
//    {
//        Contact contact = MockDataForTests.getMockContact();
//        String contactDetail[] = new String[10];
//
//        contactDetail[0] = contact.getFirstName();
//        contactDetail[1] = contact.getLastName();
//        contactDetail[2] =
//            "{\n" +
//            "\"country\": \"ES\",\n" +
//            "\"phone\": \"+34659562976\"\n" +
//            "}\n";
//        contactDetail[3] =
//                "{\n" +
//                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
//                "}\n";
//        contactDetail[4] = contact.getOfficeLocation();
//        contactDetail[5] = contact.getPosition();
//        contactDetail[6] = contact.getAvatar();
//        contactDetail[7] = Constants.PLATFORM_LOCAL;
//        contactDetail[8] = contact.getContactId();
//        contactDetail[9] = contact.getStringField1(); //SFAvatar
//
//        Intent intent = new Intent();
//        intent.putExtra(Constants.CONTACT_DETAIL_INFO, contactDetail);
//
//        mActivity = Robolectric.buildActivity(ContactDetailsPlusActivity.class)
//                .withIntent(intent).create().start().resume().get();
//        try {
//            Thread.sleep(1000);
//        }
//        catch (Exception e)
//        {
//            System.err.println("******** Test: testConnectionAvailable Failed due to: ********\n"+e.getMessage());
//            Assert.fail();
//        }
//
//        ImageView btnChat = (ImageView) mActivity.findViewById(R.id.btn_prof_chat);
//        btnChat.performClick();
//
//        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
//        Intent startedIntent = shadowActivity.getNextStartedActivity();
//        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
//        Assert.assertTrue(shadowIntent.getAction().equals(Intent.ACTION_VIEW));
//    }
//
//    @Test
//    public void testButtonChatClick_NotLocalContact()
//    {
//        setUpActivity();
//
//        try {
//            Thread.sleep(1000);
//        }
//        catch (Exception e)
//        {
//            System.err.println("******** Test: testConnectionAvailable Failed due to: ********\n"+e.getMessage());
//            Assert.fail();
//        }
//
//        ImageView btnChat = (ImageView) mActivity.findViewById(R.id.btn_prof_chat);
//        btnChat.performClick();
//
//        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
//        Intent startedIntent = shadowActivity.getNextStartedActivity();
//        Assert.assertNotNull(startedIntent);
//        Assert.assertTrue(startedIntent.hasExtra(Constants.CHAT_FIELD_CONTACT_ID));
//        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
//        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(GroupChatActivity.class.getName()));
//    }
//
//    @Test
//    public void testButtonEmailClick_LocalContact()
//    {
//        Contact contact = MockDataForTests.getMockContact();
//        String contactDetail[] = new String[10];
//
//        contactDetail[0] = contact.getFirstName();
//        contactDetail[1] = contact.getLastName();
//        contactDetail[2] =
//                "{\n" +
//                        "\"country\": \"ES\",\n" +
//                        "\"phone\": \"+34659562976\"\n" +
//                        "}\n";
//        contactDetail[3] =
//                "{\n" +
//                        "\"email\": \"vdf01@stratesys-ts.com\"\n" +
//                        "}\n";
//        contactDetail[4] = contact.getOfficeLocation();
//        contactDetail[5] = contact.getPosition();
//        contactDetail[6] = contact.getAvatar();
//        contactDetail[7] = Constants.PLATFORM_LOCAL;
//        contactDetail[8] = contact.getContactId();
//        contactDetail[9] = contact.getStringField1(); //SFAvatar
//
//        Intent intent = new Intent();
//        intent.putExtra(Constants.CONTACT_DETAIL_INFO, contactDetail);
//
//        mActivity = Robolectric.buildActivity(ContactDetailsPlusActivity.class)
//                .withIntent(intent).create().start().resume().get();
//        try {
//            Thread.sleep(1000);
//        }
//        catch (Exception e)
//        {
//            System.err.println("******** Test: testConnectionAvailable Failed due to: ********\n"+e.getMessage());
//            Assert.fail();
//        }
//
//        ImageView btnEmail = (ImageView)mActivity.findViewById(R.id.btn_prof_email);
//        btnEmail.performClick();
//
//        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
//        Intent startedIntent = shadowActivity.getNextStartedActivity();
//        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
//        Assert.assertTrue(shadowIntent.getAction().equals(Intent.ACTION_SEND));
//    }
//
//    @Test
//    public void testButtonEmailClick_NotLocalContact()
//    {
//        setUpActivity();
//
//        try {
//            Thread.sleep(1000);
//        }
//        catch (Exception e)
//        {
//            System.err.println("******** Test: testConnectionAvailable Failed due to: ********\n"+e.getMessage());
//            Assert.fail();
//        }
//
//        ImageView btnEmail = (ImageView)mActivity.findViewById(R.id.btn_prof_email);
//        btnEmail.performClick();
//
//        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
//        Intent startedIntent = shadowActivity.getNextStartedActivity();
//        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
//        Assert.assertTrue(shadowIntent.getAction().equals(Intent.ACTION_SEND));
//    }
//
//    @Test
//    public void testButtonPhoneClick_LocalContact()
//    {
//        Contact contact = MockDataForTests.getMockContact();
//        String contactDetail[] = new String[10];
//
//        contactDetail[0] = contact.getFirstName();
//        contactDetail[1] = contact.getLastName();
//        contactDetail[2] =
//                "{\n" +
//                        "\"country\": \"ES\",\n" +
//                        "\"phone\": \"+34659562976\"\n" +
//                        "}\n";
//        contactDetail[3] =
//                "{\n" +
//                        "\"email\": \"vdf01@stratesys-ts.com\"\n" +
//                        "}\n";
//        contactDetail[4] = contact.getOfficeLocation();
//        contactDetail[5] = contact.getPosition();
//        contactDetail[6] = contact.getAvatar();
//        contactDetail[7] = Constants.PLATFORM_LOCAL;
//        contactDetail[8] = contact.getContactId();
//        contactDetail[9] = contact.getStringField1(); //SFAvatar
//
//        Intent intent = new Intent();
//        intent.putExtra(Constants.CONTACT_DETAIL_INFO, contactDetail);
//
//        mActivity = Robolectric.buildActivity(ContactDetailsPlusActivity.class)
//                .withIntent(intent).create().start().resume().get();
//        try {
//            Thread.sleep(1000);
//        }
//        catch (Exception e)
//        {
//            System.err.println("******** Test: testConnectionAvailable Failed due to: ********\n"+e.getMessage());
//            Assert.fail();
//        }
//
//        ImageView btnPhone = (ImageView)mActivity.findViewById(R.id.btn_prof_phone);
//        btnPhone.performClick();
//
//        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
//        Intent startedIntent = shadowActivity.getNextStartedActivity();
//        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
//        Assert.assertTrue(shadowIntent.getAction().equals(Intent.ACTION_CALL));
//    }
//
//    @Test
//    public void testButtonPhoneClick_NotLocalContact()
//    {
//        setUpActivity();
//
//        try {
//            Thread.sleep(1000);
//        }
//        catch (Exception e)
//        {
//            System.err.println("******** Test: testConnectionAvailable Failed due to: ********\n"+e.getMessage());
//            Assert.fail();
//        }
//
//        ImageView btnPhone = (ImageView)mActivity.findViewById(R.id.btn_prof_phone);
//        btnPhone.performClick();
//
//        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);
//        Intent startedIntent = shadowActivity.getNextStartedActivity();
//        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
//        Assert.assertTrue(shadowIntent.getAction().equals(Intent.ACTION_CALL));
//    }
//
//    @Test
//    public void testLayoutBack_Click()
//    {
//        setUpActivity();
//
//        try {
//            Thread.sleep(1000);
//        }
//        catch (Exception e)
//        {
//            System.err.println("******** Test: testConnectionAvailable Failed due to: ********\n"+e.getMessage());
//            Assert.fail();
//        }
//
//        LinearLayout linLayoutClose = (LinearLayout) mActivity.findViewById(R.id.layout_close);
//        linLayoutClose.performClick();
//        Assert.assertTrue(this.mActivity.isFinishing());
//    }
//
//    @Test
//    public void testOnConnectivityChanged_HasInternet_Event()
//    {
//        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET);
//        setUpActivity();
//        try {
//            Thread.sleep(1000);
//        }
//        catch (Exception e)
//        {
//            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
//            Assert.fail();
//        }
//
//        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
//        BusProvider.getInstance().post(event);
//        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.GONE);
//    }
//
//    @Test
//    public void testOnConnectivityChanged_Unknown_Event()
//    {
//        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.UNKNOWN);
//        setUpActivity();
//        try {
//            Thread.sleep(1000);
//        }
//        catch (Exception e)
//        {
//            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
//            Assert.fail();
//        }
//
//        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
//        BusProvider.getInstance().post(event);
//        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);
//    }
//
//    @Test
//    public void testOnConnectivityChanged_Offline_Event()
//    {
//        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.OFFLINE);
//        setUpActivity();
//        try {
//            Thread.sleep(1000);
//        }
//        catch (Exception e)
//        {
//            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
//            Assert.fail();
//        }
//
//        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
//        BusProvider.getInstance().post(event);
//        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);
//    }
//
//    @Test
//    public void testOnConnectivityChanged_ConnectedNoInternet_Event()
//    {
//        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED_HAS_NO_INTERNET);
//        setUpActivity();
//        try {
//            Thread.sleep(1000);
//        }
//        catch (Exception e)
//        {
//            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
//            Assert.fail();
//        }
//
//        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
//        BusProvider.getInstance().post(event);
//        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);
//    }
//
//    @Test
//    public void testOnConnectivityChanged_WifiConnected_Event()
//    {
//        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.WIFI_CONNECTED);
//        setUpActivity();
//        try {
//            Thread.sleep(1000);
//        }
//        catch (Exception e)
//        {
//            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
//            Assert.fail();
//        }
//
//        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
//        BusProvider.getInstance().post(event);
//        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.VISIBLE);
//    }
//
//    @Test
//    public void testOnConnectivityChanged_MobileConnected_Event()
//    {
//        ConnectivityChanged event = new ConnectivityChanged(ConnectivityStatus.MOBILE_CONNECTED);
//        setUpActivity();
//        try {
//            Thread.sleep(1000);
//        }
//        catch (Exception e)
//        {
//            System.err.println("******** Test: testBackButton_Clicked Failed due to: ********\n"+e.getMessage());
//            Assert.fail();
//        }
//
//        LinearLayout lay_no_connection = (LinearLayout) mActivity.findViewById(R.id.no_connection_layout);
//        BusProvider.getInstance().post(event);
//        org.junit.Assert.assertEquals(lay_no_connection.getVisibility(), View.GONE);
//    }
//
//    private void setUpActivity()
//    {
//        Contact contact = MockDataForTests.getMockContact();
//        String contactDetail[] = new String[10];
//
//        contactDetail[0] = contact.getFirstName();
//        contactDetail[1] = contact.getLastName();
//        contactDetail[2] = contact.getPhones();
//        contactDetail[3] = contact.getEmails();
//        contactDetail[4] = contact.getOfficeLocation();
//        contactDetail[5] = contact.getPosition();
//        contactDetail[6] = contact.getAvatar();
//        contactDetail[7] = contact.getPlatform();
//        contactDetail[8] = contact.getContactId();
//        contactDetail[9] = contact.getStringField1(); //SFAvatar
//
//        Intent intent = new Intent();
//        intent.putExtra(Constants.CONTACT_DETAIL_INFO, contactDetail);
//
//        mActivity = Robolectric.buildActivity(ContactDetailsPlusActivity.class)
//                .withIntent(intent).create().start().resume().get();
//    }
//
//    private void mockPicasso()
//    {
//        Downloader downloader = new OkHttpDownloader(RuntimeEnvironment.application.getApplicationContext(), Long.MAX_VALUE);
//        Picasso.Builder builder = new Picasso.Builder(RuntimeEnvironment.application.getApplicationContext());
//        builder.downloader(downloader);
//        MycommsApp.picasso = builder.build();
//    }
//
//
//}
