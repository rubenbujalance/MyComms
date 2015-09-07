package com.vodafone.mycomms.contacts;

import android.content.Context;
import android.content.Intent;

import com.crashlytics.android.Crashlytics;
import com.thoughtworks.xstream.mapper.Mapper;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.ContactListMainActivity;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.contacts.connection.ContactsController;
import com.vodafone.mycomms.contacts.view.ContactListFragment;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmLDAPSettingsTransactions;
import com.vodafone.mycomms.realm.RealmProfileTransactions;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.CustomFragmentActivity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.core.MockRepository;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import io.realm.Realm;
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
        boolean canBeInvited = contactsController.isContactCanBeInvited(MockDataForTests.getMockContactsList().get(1),mockEmail);

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

    public void startContactListFragment(int index)
    {
        Intent in = new Intent(RuntimeEnvironment.application.getApplicationContext(),
                CustomFragmentActivity.class);
        in.putExtra("index", index);
        mCustomFragmentActivity = Robolectric.buildActivity(CustomFragmentActivity.class)
                .withIntent(in)
                .create().start().resume().get();
    }
}
