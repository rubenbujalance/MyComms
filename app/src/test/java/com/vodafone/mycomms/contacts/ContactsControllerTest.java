package com.vodafone.mycomms.contacts;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.constants.Constants;
import com.vodafone.mycomms.contacts.connection.ContactsController;
import com.vodafone.mycomms.contacts.connection.RecentContactController;
import com.vodafone.mycomms.contacts.detail.ContactDetailMainActivity;
import com.vodafone.mycomms.contacts.view.ContactListFragment;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ReloadAdapterEvent;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmProfileTransactions;
import com.vodafone.mycomms.search.SearchBarController;
import com.vodafone.mycomms.search.SearchController;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.CustomFragmentActivity;

import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
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
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowListView;

import java.io.PrintWriter;
import java.io.StringWriter;

import io.realm.Realm;
import model.Contact;
import model.FavouriteContact;
import model.RecentContact;
import model.UserProfile;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by str_oan on 07/09/2015.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*"})
@PrepareForTest({Realm.class, EndpointWrapper.class
        , Crashlytics.class
        , SearchController.class, SearchBarController.class, RecentContactController.class
        , RealmContactTransactions.class})

public class ContactsControllerTest{
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    public MockWebServer webServer;
    public Context mContext;
    public String mProfileId = "mc_5570340e7eb7c3512f2f9bf2";
    public CustomFragmentActivity mCustomFragmentActivity;
    public ContactListFragment mContactListFragment;

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
    public void tearDown() throws Exception
    {
        MockDataForTests.checkThreadSchedulers();
        Robolectric.reset();
        if(webServer!=null) webServer.shutdown();

        mContactListFragment = null;
        mCustomFragmentActivity = null;
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testContactListFragmentLifecycle()
    {
        Intent in = new Intent(RuntimeEnvironment.application.getApplicationContext(),
                CustomFragmentActivity.class);
        in.putExtra("index", 2);
        mCustomFragmentActivity = Robolectric.buildActivity(CustomFragmentActivity.class)
                .withIntent(in)
                .create().start().resume().pause().stop().destroy().get();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(mCustomFragmentActivity.isDestroyed());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testContactListFragment_LoadContactsFromDB_NullKeyWord()
    {
        try
        {
            com.vodafone.mycomms.util.Constants.isDashboardOrigin = true;
            PowerMockito.mockStatic(RealmContactTransactions.class);
            PowerMockito.when(RealmContactTransactions.getAllContacts(Matchers.any(Realm.class), Matchers.anyString()))
                    .thenReturn(MockDataForTests.getMockContactsList());
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testContactListFragment_LoadContactsFromDB Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        startContactListFragment(2);
        mContactListFragment = (ContactListFragment)mCustomFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("2");
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(!mContactListFragment.getContactList().isEmpty());
        Assert.assertTrue(mContactListFragment.getContactList().size() == 9);
        Assert.assertFalse(com.vodafone.mycomms.util.Constants.isDashboardOrigin);

        checkOnItemClick();

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");

    }

    @Test
    public void testReloadAdapterEvent()
    {
        try
        {
            PowerMockito.mockStatic(RealmContactTransactions.class);
            PowerMockito.when(RealmContactTransactions.getAllContacts(Matchers.any(Realm.class), Matchers.anyString()))
                    .thenReturn(MockDataForTests.getMockContactsList());

            PowerMockito.mockStatic(SearchBarController.class);
            PowerMockito.when(SearchBarController.getContactList())
                    .thenReturn(MockDataForTests.getMockContactsList());
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testContactListFragment_LoadContactsFromDB Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        startContactListFragment(2);
        mContactListFragment = (ContactListFragment)mCustomFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("2");
        MockDataForTests.checkThreadSchedulers();

        ReloadAdapterEvent event = new ReloadAdapterEvent();
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(!mContactListFragment.getContactList().isEmpty());
        Assert.assertTrue(mContactListFragment.getContactList().size() == 9);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    private void checkOnItemClick()
    {
        ListView listView = mContactListFragment.getListView();
        View view = mContactListFragment.getView();
        int position = 0;
        long id = listView.getItemIdAtPosition(position);

        listView.performItemClick(view, position, id);
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(mContactListFragment.getActivity());
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(ContactDetailMainActivity.class.getName()));
    }

    @Test
    public void testContactListFragment_LoadListViewElements_Click()
    {
        try
        {
            PowerMockito.mockStatic(RealmContactTransactions.class);
            PowerMockito.when(RealmContactTransactions.getAllContacts(Matchers.any(Realm.class), Matchers.anyString()))
                    .thenReturn(MockDataForTests.getMockContactsList());
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testContactListFragment_LoadContactsFromDB Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        startContactListFragment(2);
        mContactListFragment = (ContactListFragment)mCustomFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("2");
        mockParams();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(!mContactListFragment.getContactList().isEmpty());
        Assert.assertTrue(mContactListFragment.getContactList().size() == 9);

        ShadowListView shadowListView = Shadows.shadowOf(mContactListFragment.getListView());
        shadowListView.populateItems();
        Assert.assertTrue(shadowListView.performItemClick(0));
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(mContactListFragment.getActivity());
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertTrue(shadowIntent.getComponent().getClassName().equals(ContactDetailMainActivity.class.getName()));

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }


    @Test
    public void testContactListFragment_InviteOption_Click()
    {
        try
        {
            PowerMockito.mockStatic(RealmContactTransactions.class);
            PowerMockito.when(RealmContactTransactions.getAllContacts(Matchers.any(Realm.class), Matchers.anyString()))
                    .thenReturn(MockDataForTests.getMockContactsList());
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testContactListFragment_LoadContactsFromDB Failed due to: ********\n"+e.getMessage());
            Assert.fail();
        }

        startContactListFragment(2);
        mContactListFragment = (ContactListFragment)mCustomFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("2");
        mockParams();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(!mContactListFragment.getContactList().isEmpty());
        Assert.assertTrue(mContactListFragment.getContactList().size() == 9);

        ShadowListView shadowListView = Shadows.shadowOf(mContactListFragment.getListView());
        shadowListView.populateItems();
        ListView listView = mContactListFragment.getListView();
        View view = listView.getChildAt(0);
        LinearLayout layInviteMyComms = (LinearLayout) view.findViewById(R.id.lay_invite_mycomms);
        layInviteMyComms.performClick();
        MockDataForTests.checkThreadSchedulers();

        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        Button acceptButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
        acceptButton.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(!alert.isShowing());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testIsContactCanBeInvited_False()
    {
        String mockEmail = "mock_email@mockdomain.com";
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        boolean canBeInvited = contactsController.isContactCanBeInvited(MockDataForTests.getMockContactsList().get(0), mockEmail);

        Assert.assertFalse(canBeInvited);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testIsContactCanBeInvited_False_mc()
    {
        String mockEmail = "mock_email@vodafone.com";
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        boolean canBeInvited = contactsController.isContactCanBeInvited(MockDataForTests.getMockContactsList().get(0), mockEmail);

        Assert.assertFalse(canBeInvited);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testIsContactCanBeInvited_False_same_email()
    {
        String mockEmail = "vdf01@vodafone.com";
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        boolean canBeInvited = contactsController.isContactCanBeInvited(MockDataForTests.getMockContactsList().get(1), mockEmail);

        Assert.assertFalse(canBeInvited);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testIsContactCanBeInvited_False_neither_case()
    {
        String mockEmail = "vdf01@vodafone.com";
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        boolean canBeInvited = contactsController.isContactCanBeInvited(MockDataForTests.getMockContactsList().get(2), mockEmail);

        Assert.assertFalse(canBeInvited);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testIsContactCanBeInvited_True()
    {
        String mockEmail = "mock_email@vodafone.com";
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        boolean canBeInvited = contactsController.isContactCanBeInvited(MockDataForTests.getMockContactsList().get(1), mockEmail);

        Assert.assertTrue(canBeInvited);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testGetUserProfileEmails_OK()
    {
        UserProfile mUserProfile = new UserProfile();
        mUserProfile.setEmails
                (
                        "[\n" +
                                "{\n" +
                                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                                "}\n" +
                                "]"
                );

        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        RealmProfileTransactions mRealmProfileTransactions = Mockito.mock(RealmProfileTransactions.class);
        contactsController.setRealmProfileTransactions(mRealmProfileTransactions);
        Mockito.when(mRealmProfileTransactions.getUserProfile(Matchers.any(String.class), Matchers.any(Realm.class))).thenReturn(mUserProfile);
        String emails = contactsController.getUserProfileEmails();

        Assert.assertEquals(emails, "vdf01@stratesys-ts.com");

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testCreateInviteAlertWithEvents_OK_Invite_Btn_Clicked()
    {
        Contact mContact = MockDataForTests.getMockContactsList().get(0);

        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        contactsController.createInviteAlertWithEvents(mContact);

        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        Button inviteButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
        inviteButton.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(!alert.isShowing());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testCreateInviteAlertWithEvents_OK_Cancel_Btn_Clicked()
    {
        Contact mContact = MockDataForTests.getMockContactsList().get(0);

        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        contactsController.createInviteAlertWithEvents(mContact);

        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        Button cancelButton = alert.getButton(AlertDialog.BUTTON_NEGATIVE);
        cancelButton.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(!alert.isShowing());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testCreateInviteAlertWithEvents_Fail_Void_Email()
    {
        Contact mContact = MockDataForTests.getMockContactsList().get(0);
        mContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"\"\n" +
                "}\n" +
                "]");

        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        contactsController.createInviteAlertWithEvents(mContact);

        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        Button acceptButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
        acceptButton.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(!alert.isShowing());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test (expected = NullPointerException.class)
    public void testCreateInviteAlertWithEvents_Fail_Null_Contact()
    {
        Contact mContact = null;

        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        contactsController.createInviteAlertWithEvents(mContact);

        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        Button acceptButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
        acceptButton.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(!alert.isShowing());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testSendInvitation_On_Success()
    {
        String serverUrl = null;
        try {
            serverUrl = startWebMockServer();
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testSendInvitation Failed due to: startWebMockServer()********\n"+e.getMessage());
            Assert.fail();
        }

        PowerMockito.mockStatic(EndpointWrapper.class);
        PowerMockito.when(EndpointWrapper.getBaseURL()).thenReturn(serverUrl);

        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(Constants.VALID_VERSION_RESPONSE));
        String email = "mockemail@vodafone.com";
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        contactsController.sendInvitation(email);
        MockDataForTests.checkThreadSchedulers();

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testSendInvitation_On_Failure()
    {
        String serverUrl = null;
        try {
            serverUrl = startWebMockServer();
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testSendInvitation Failed due to: startWebMockServer()********\n"+e.getMessage());
            Assert.fail();
        }

        PowerMockito.mockStatic(EndpointWrapper.class);
        PowerMockito.when(EndpointWrapper.getBaseURL()).thenReturn(serverUrl);

        webServer.enqueue(new MockResponse().setResponseCode(400).setBody(Constants.VALID_VERSION_RESPONSE));
        String email = "mockemail@vodafone.com";
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        contactsController.sendInvitation(email);
        MockDataForTests.checkThreadSchedulers();

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testShowSuccessOrErrorResponse_Success()
    {
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        contactsController.showSuccessOrErrorResponse(true);

        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        Button acceptButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
        acceptButton.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(!alert.isShowing());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testShowSuccessOrErrorResponse_Error()
    {
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        contactsController.showSuccessOrErrorResponse(false);

        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        Button acceptButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
        acceptButton.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(!alert.isShowing());

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testMapProfileToContact()
    {
        UserProfile mUserProfile = MockDataForTests.getMockUserProfile();
        new ContactsController(this.mProfileId, this.mContext);
        Contact contact = ContactsController.mapProfileToContact(mUserProfile);

        Assert.assertTrue(mUserProfile.getAvailability().equals(contact.getAvailability()));
        Assert.assertTrue(mUserProfile.getFirstName().equals(contact.getFirstName()));
        Assert.assertTrue(mUserProfile.getLastName().equals(contact.getLastName()));
        Assert.assertTrue(mUserProfile.getPhones().equals(contact.getPhones()));
        Assert.assertTrue(mUserProfile.getEmails().equals(contact.getEmails()));
        Assert.assertTrue(mUserProfile.getCompany().equals(contact.getCompany()));

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testMapContactToRecent_OK()
    {
        Contact mContact = MockDataForTests.getMockContact();
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put(com.vodafone.mycomms.util.Constants.CONTACT_RECENTS_ACTION, "mockAction");
            jsonObject.put(com.vodafone.mycomms.util.Constants.CONTACT_RECENTS_ACTION_TIME, Long.parseLong("123456789"));
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testMapContactToRecent Failed due to: JSON conversions********\n"+e.getMessage());
            Assert.fail();
        }
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        RecentContact contact = contactsController.mapContactToRecent(mContact, jsonObject);

        Assert.assertTrue(mContact.getAvailability().equals(contact.getAvailability()));
        Assert.assertTrue(mContact.getFirstName().equals(contact.getFirstName()));
        Assert.assertTrue(mContact.getLastName().equals(contact.getLastName()));
        Assert.assertTrue(mContact.getPhones().equals(contact.getPhones()));
        Assert.assertTrue(mContact.getEmails().equals(contact.getEmails()));
        Assert.assertTrue(mContact.getCompany().equals(contact.getCompany()));

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testMapContactToRecent_Fail()
    {
        Contact mContact = MockDataForTests.getMockContact();
        JSONObject jsonObject = new JSONObject();

        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        RecentContact contact = contactsController.mapContactToRecent(mContact, jsonObject);

        Assert.assertTrue(mContact.getAvailability().equals(contact.getAvailability()));
        Assert.assertTrue(mContact.getFirstName().equals(contact.getFirstName()));
        Assert.assertTrue(mContact.getLastName().equals(contact.getLastName()));
        Assert.assertTrue(mContact.getPhones().equals(contact.getPhones()));
        Assert.assertTrue(mContact.getEmails().equals(contact.getEmails()));
        Assert.assertTrue(mContact.getCompany().equals(contact.getCompany()));

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testMapContactToFavourite()
    {
        Contact mContact = MockDataForTests.getMockContact();
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        FavouriteContact contact = contactsController.mapContactToFavourite(mContact);

        Assert.assertTrue(mContact.getAvailability().equals(contact.getAvailability()));
        Assert.assertTrue(mContact.getFirstName().equals(contact.getFirstName()));
        Assert.assertTrue(mContact.getLastName().equals(contact.getLastName()));
        Assert.assertTrue(mContact.getPhones().equals(contact.getPhones()));
        Assert.assertTrue(mContact.getEmails().equals(contact.getEmails()));
        Assert.assertTrue(mContact.getCompany().equals(contact.getCompany()));

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testMapContact_OK()
    {
        String mProfileId = "mc_mock_id";
        String searchHelper = "mockMyName mockMyLastName Stratesys ";
        String sortHelper = "mockMyName mockMyLastName Stratesys";
        JSONObject jsonObject = MockDataForTests.getContactJSONObject();
        new ContactsController(this.mProfileId, this.mContext);
        Contact contact = ContactsController.mapContact(jsonObject, mProfileId);

        Assert.assertTrue(contact.getSearchHelper().startsWith(searchHelper));
        Assert.assertTrue(contact.getSortHelper().contains(sortHelper));

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    @Test
    public void testMapContact_Fail()
    {
        new ContactsController(this.mProfileId, this.mContext);
        ContactsController.mapContact(null, null);

        System.out.println("Test " + Thread.currentThread().getStackTrace()[1].getMethodName()
                + " from class " + this.getClass().getSimpleName() + " successfully finished!");
    }

    public void startContactListFragment(int index)
    {
        Intent in = new Intent(RuntimeEnvironment.application.getApplicationContext(),
                CustomFragmentActivity.class);
        in.putExtra("index", index);
        mCustomFragmentActivity = Robolectric.buildActivity(CustomFragmentActivity.class)
                .withIntent(in)
                .create().start().resume().visible().get();
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

    private void mockParams()
    {
        Downloader downloader = new OkHttpDownloader(mContactListFragment.getActivity().getApplicationContext(), Long.MAX_VALUE);
        Picasso.Builder builder = new Picasso.Builder(mContactListFragment.getActivity().getApplicationContext());
        builder.downloader(downloader);
        MycommsApp.picasso = builder.build();
    }
}
