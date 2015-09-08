package com.vodafone.mycomms.contacts;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.Button;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.thoughtworks.xstream.mapper.Mapper;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.ContactListMainActivity;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.constants.Constants;
import com.vodafone.mycomms.contacts.connection.ContactsController;
import com.vodafone.mycomms.contacts.view.ContactListFragment;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmLDAPSettingsTransactions;
import com.vodafone.mycomms.realm.RealmProfileTransactions;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.CustomFragmentActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
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
import org.robolectric.shadows.ShadowAlertDialog;

import io.realm.Realm;
import model.Contact;
import model.FavouriteContact;
import model.RecentContact;
import model.UserProfile;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Created by str_oan on 07/09/2015.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*"})
@PrepareForTest({RealmContactTransactions.class, Realm.class, EndpointWrapper.class})
public class ContactsControllerTest
{
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    public MockWebServer webServer;
    public Context mContext;
    public ContactsController mContactsController;
    public String mProfileId = "mc_5570340e7eb7c3512f2f9bf2";
    public CustomFragmentActivity mCustomFragmentActivity;
    public ContactListFragment mContactListFragment;

    @Before
    public void setUp()
    {
        mockStatic(Realm.class);
        when(Realm.getDefaultInstance()).thenReturn(null);
        mockStatic(Crashlytics.class);
        try
        {
            whenNew(RealmContactTransactions.class).withAnyArguments()
                    .thenReturn(null);
        }
        catch (Exception e)
        {
            Assert.fail();
            System.err.println("******** Test: ContactsControllerTest********"+e.getMessage());
        }

        mContext = RuntimeEnvironment.application.getApplicationContext();
        startContactListFragment(2);
        mContactListFragment = (ContactListFragment)mCustomFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("2");
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());

    }

    @Test
    public void testIsContactCanBeInvited_False()
    {
        String mockEmail = "mock_email@mockdomain.com";
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        boolean canBeInvited = contactsController.isContactCanBeInvited(MockDataForTests.getMockContactsList().get(0),mockEmail);

        Assert.assertFalse(canBeInvited);
    }

    @Test
    public void testIsContactCanBeInvited_False_mc()
    {
        String mockEmail = "mock_email@vodafone.com";
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        boolean canBeInvited = contactsController.isContactCanBeInvited(MockDataForTests.getMockContactsList().get(0),mockEmail);

        Assert.assertFalse(canBeInvited);
    }

    @Test
    public void testIsContactCanBeInvited_False_same_email()
    {
        String mockEmail = "vdf01@vodafone.com";
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        boolean canBeInvited = contactsController.isContactCanBeInvited(MockDataForTests.getMockContactsList().get(1),mockEmail);

        Assert.assertFalse(canBeInvited);
    }

    @Test
    public void testIsContactCanBeInvited_False_neither_case()
    {
        String mockEmail = "vdf01@vodafone.com";
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        boolean canBeInvited = contactsController.isContactCanBeInvited(MockDataForTests.getMockContactsList().get(2),mockEmail);

        Assert.assertFalse(canBeInvited);
    }

    @Test
    public void testIsContactCanBeInvited_True()
    {
        String mockEmail = "mock_email@vodafone.com";
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        boolean canBeInvited = contactsController.isContactCanBeInvited(MockDataForTests.getMockContactsList().get(1), mockEmail);

        Assert.assertTrue(canBeInvited);
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
        Assert.assertTrue(!alert.isShowing());
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
        Assert.assertTrue(!alert.isShowing());
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
        Assert.assertTrue(!alert.isShowing());
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
        Assert.assertTrue(!alert.isShowing());
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

        PowerMockito.mockStatic(APIWrapper.class);
        PowerMockito.mockStatic(EndpointWrapper.class);
        PowerMockito.when(EndpointWrapper.getBaseURL()).thenReturn(serverUrl);

        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(Constants.VALID_VERSION_RESPONSE));
        String email = "mockemail@vodafone.com";
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        contactsController.sendInvitation(email);

        try {
            Thread.sleep(2000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testSendInvitation Failed due to: Thread.sleep********\n"+e.getMessage());
            Assert.fail();
        }

        Robolectric.flushForegroundThreadScheduler();
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

        PowerMockito.mockStatic(APIWrapper.class);
        PowerMockito.mockStatic(EndpointWrapper.class);
        PowerMockito.when(EndpointWrapper.getBaseURL()).thenReturn(serverUrl);

        webServer.enqueue(new MockResponse().setResponseCode(400).setBody(Constants.VALID_VERSION_RESPONSE));
        String email = "mockemail@vodafone.com";
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        contactsController.sendInvitation(email);

        try {
            Thread.sleep(2000);
        }
        catch (Exception e)
        {
            System.err.println("******** Test: testSendInvitation Failed due to: Thread.sleep********\n"+e.getMessage());
            Assert.fail();
        }

        Robolectric.flushForegroundThreadScheduler();
    }

    @Test
    public void testShowSuccessOrErrorResponse_Success()
    {
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        contactsController.showSuccessOrErrorResponse(true);

        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        Button acceptButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
        acceptButton.performClick();
        Assert.assertTrue(!alert.isShowing());
    }

    @Test
    public void testShowSuccessOrErrorResponse_Error()
    {
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        contactsController.showSuccessOrErrorResponse(false);

        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        Button acceptButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
        acceptButton.performClick();
        Assert.assertTrue(!alert.isShowing());
    }

    @Test
    public void testMapProfileToContact()
    {
        UserProfile mUserProfile = MockDataForTests.getMockUserProfile();
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        Contact contact = contactsController.mapProfileToContact(mUserProfile);

        Assert.assertTrue(mUserProfile.getAvailability().equals(contact.getAvailability()));
        Assert.assertTrue(mUserProfile.getFirstName().equals(contact.getFirstName()));
        Assert.assertTrue(mUserProfile.getLastName().equals(contact.getLastName()));
        Assert.assertTrue(mUserProfile.getPhones().equals(contact.getPhones()));
        Assert.assertTrue(mUserProfile.getEmails().equals(contact.getEmails()));
        Assert.assertTrue(mUserProfile.getCompany().equals(contact.getCompany()));
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
    }

    @Test
    public void testMapContact_OK()
    {
        String mProfileId = "mc_mock_id";
        String searchHelper = "mockMyName mockMyLastName Stratesys ";
        String sortHelper = "mockMyName mockMyLastName Stratesys";
        JSONObject jsonObject = MockDataForTests.getContactJSONObject();
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        Contact contact = contactsController.mapContact(jsonObject, mProfileId);

        Assert.assertTrue(contact.getSearchHelper().startsWith(searchHelper));
        Assert.assertTrue(contact.getSortHelper().contains(sortHelper));
    }

    @Test
    public void testMapContact_Fail()
    {
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        contactsController.mapContact(null, null);
    }

    @Test
    public void testInsertRecentGroupChatIntoRealm_OK()
    {
        Contact contact = MockDataForTests.getMockContact();
        JSONObject jsonObject = MockDataForTests.getContactJSONObject();
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        contactsController.insertRecentGroupChatIntoRealm(contact, jsonObject);
    }

    @Test
    public void testInsertRecentGroupChatIntoRealm_Null_Data()
    {
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        contactsController.insertRecentGroupChatIntoRealm(null, null);
    }

    @Test
    public void testInsertContactListInRealmWithNullContactFromGetter()
    {
        JSONObject jsonObject = MockDataForTests.getContactDATAFromJSONObjectWithJSONArray();
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        contactsController.setRealmContactTransactions(Mockito.mock(RealmContactTransactions.class));
        Mockito.when(contactsController.getRealmContactTransactions().getContactById
                (Matchers.anyString(), Matchers.any(Realm.class))).thenReturn(null);
        contactsController.insertContactListInRealm(jsonObject);
    }

    @Test
    public void testInsertContactListInRealmWithNOTNullContactFromGetter()
    {
        JSONObject jsonObject = MockDataForTests.getContactDATAFromJSONObjectWithJSONArray();
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        contactsController.setRealmContactTransactions(Mockito.mock(RealmContactTransactions.class));
        Mockito.when(contactsController.getRealmContactTransactions().getContactById
                (Matchers.anyString(), Matchers.any(Realm.class))).thenReturn(MockDataForTests.getMockContact());
        contactsController.insertContactListInRealm(jsonObject);
    }

    @Test
    public void testInsertContactListInRealmWithControlledException()
    {
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        contactsController.insertContactListInRealm(null);
    }

    @Test
    public void testInsertFavouriteContactInRealmWithNullContactFromGetter()
    {
        JSONObject jsonObject = MockDataForTests.getContactFavoriteDATAFromJSONObjectWithJSONArray();
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        contactsController.setRealmContactTransactions(Mockito.mock(RealmContactTransactions.class));
        Mockito.when(contactsController.getRealmContactTransactions().getContactById
                (Matchers.anyString(), Matchers.any(Realm.class))).thenReturn(null);
        contactsController.insertFavouriteContactInRealm(jsonObject);
    }

    @Test
    public void testInsertFavouriteContactInRealmWithNOTNullContactFromGetter()
    {
        JSONObject jsonObject = MockDataForTests.getContactFavoriteDATAFromJSONObjectWithJSONArray();
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        contactsController.setRealmContactTransactions(Mockito.mock(RealmContactTransactions.class));
        Mockito.when(contactsController.getRealmContactTransactions().getContactById
                (Matchers.anyString(), Matchers.any(Realm.class))).thenReturn(MockDataForTests.getMockContact());
        contactsController.insertFavouriteContactInRealm(jsonObject);
    }

    @Test
    public void testInsertFavouriteContactInRealmWithControlledException()
    {
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        contactsController.insertFavouriteContactInRealm(null);
    }

    @Test
    public void testInsertContactListInRealmWithoutJSONArray()
    {
        JSONObject jsonObject = MockDataForTests.getContactJSONObject();
        ContactsController contactsController = new ContactsController(this.mProfileId, this.mContext);
        contactsController.setRealmContactTransactions(Mockito.mock(RealmContactTransactions.class));
        Mockito.when(contactsController.getRealmContactTransactions().getContactById
                (Matchers.anyString(), Matchers.any(Realm.class))).thenReturn(MockDataForTests.getMockContact());
        contactsController.insertContactListInRealm(jsonObject);
    }

    public void startContactListFragment(int index)
    {
        Intent in = new Intent(RuntimeEnvironment.application.getApplicationContext(),
                CustomFragmentActivity.class);
        in.putExtra("index", index);
        mCustomFragmentActivity = Robolectric.buildActivity(CustomFragmentActivity.class)
                .withIntent(in)
                .create().start().resume().get();
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
