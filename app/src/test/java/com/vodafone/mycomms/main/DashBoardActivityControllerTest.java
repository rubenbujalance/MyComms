package com.vodafone.mycomms.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.contacts.connection.RecentContactController;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmNewsTransactions;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.Constants;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
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
import org.robolectric.shadows.ShadowIntent;

import java.util.ArrayList;

import io.realm.Realm;
import model.News;
import model.RecentContact;

import static java.lang.System.exit;
import static java.lang.System.in;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by str_oan on 02/09/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*"})
@PrepareForTest({Realm.class, Crashlytics.class, RealmNewsTransactions.class})
public class DashBoardActivityControllerTest
{
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    public Activity mActivity;
    public String mProfileId;
    public RealmContactTransactions mRealmContactTransactions;
    public RecentContactController mRecentContactController;
    public SharedPreferences sp;
    public ArrayList<RecentContact> emptyRecentContactsList;
    public ArrayList<RecentContact> notEmptyRecentContactsList;
    public int numberOfRecentContacts;
    public LinearLayout mRecentContainer, mRecentContainer2;
    public ArrayList<News> newsArrayList;
    public Realm mRealm;

    public DashBoardActivityController mDashBoardActivityController;

    @Before
    public void setUp() throws Exception
    {
        mockStatic(Realm.class);
        when(Realm.getDefaultInstance()).thenReturn(null);
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
        Context context = RuntimeEnvironment.application.getApplicationContext();
        this.sp = context.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

        this.emptyRecentContactsList = new ArrayList<>();
        this.notEmptyRecentContactsList = fillMockRecentContactsList();
        this.mProfileId = "mc_5570340e7eb7c3512f2f9bf2";
        this.mRealmContactTransactions = new RealmContactTransactions(this.mProfileId);
        this.numberOfRecentContacts = 0;
        this.mActivity = Robolectric.buildActivity(DashBoardActivity.class).create().start().get();
        Thread.sleep(1000);
        Robolectric.flushForegroundThreadScheduler();
        this.mRecentContactController = new RecentContactController(this.mActivity, this.mProfileId);
        this.mRecentContainer = (LinearLayout) mActivity.findViewById(R.id.list_recents);
        this.mRecentContainer2 = (LinearLayout) mActivity.findViewById(R.id.list_recents_2);
        this.newsArrayList = new ArrayList<>();
        this.mRealm = PowerMockito.mock(Realm.class);

        this.mDashBoardActivityController = new DashBoardActivityController
                (
                        this.mActivity
                        , this.mRealm
                        , this.mRealmContactTransactions
                        , this.mProfileId
                        , this.mRecentContactController
                );

        mockParams();
    }

    @Test
    public void testCorrectlyCreated()
    {
        Assert.assertEquals(this.mDashBoardActivityController.mActivity, this.mActivity);
        Assert.assertEquals(this.mDashBoardActivityController.mRealmContactTransactions, this.mRealmContactTransactions);
        Assert.assertEquals(this.mDashBoardActivityController.mProfileId, this.mProfileId);
        Assert.assertEquals(this.mDashBoardActivityController.mRecentContactController, this.mRecentContactController);
    }

    @Test
    public void testLoadNews_OKWithClickOnNew() throws Exception
    {
        this.mDashBoardActivityController.loadNews();
        Thread.sleep(5000);
        Assert.assertNotNull(this.mDashBoardActivityController.newsArrayList);

        LinearLayout container = (LinearLayout) mActivity.findViewById(R.id.list_news);
        LayoutInflater inflater = LayoutInflater.from(this.mActivity);
        Assert.assertNotNull(inflater);
        View mView = inflater.inflate(R.layout.layout_news_dashboard, container, false);
        LinearLayout btnNews = (LinearLayout) mView.findViewById(R.id.notice_content);

        int numberOfChild = container.getChildCount();
        Assert.assertEquals(numberOfChild, getMockNewsArrayList().size());

        btnNews.performClick();

        Intent expectedIntent = new Intent(this.mActivity, NewsDetailActivity.class);
        ShadowIntent shadowIntent = Shadows.shadowOf(expectedIntent);
        Assert.assertEquals(shadowIntent.getComponent().getClassName(), (NewsDetailActivity.class.getName()));
    }

    @Test
    public void testDrawNews_OK()
    {
        this.mDashBoardActivityController.drawNews(getMockNewsArrayList());
        LinearLayout container = (LinearLayout) mActivity.findViewById(R.id.list_news);
        Assert.assertNotNull(container);
    }

    @Test
    public void testDrawNews_ControlledException()
    {
        this.mDashBoardActivityController.drawNews(null);
        Assert.assertTrue(this.mDashBoardActivityController.newsArrayList.isEmpty());
    }

    @Test
    public void testLoadRecentLayout_MoreThenOneRecent()
    {
        this.mDashBoardActivityController.numberOfRecentContacts = 5;
        this.mDashBoardActivityController.loadRecentLayout();
        Assert.assertTrue(this.mDashBoardActivityController.numberOfRecentContacts == 4);
    }

    @Test
    public void testLoadRecentLayout_OneRecent_FirstContainerIsVisible()
    {
        this.mDashBoardActivityController.isCurrentRecentContainerFirst = true;
        this.mDashBoardActivityController.numberOfRecentContacts = 1;
        this.mDashBoardActivityController.loadRecentLayout();
        Assert.assertTrue(this.mDashBoardActivityController.numberOfRecentContacts == 0);
        Assert.assertFalse(this.mDashBoardActivityController.isCurrentRecentContainerFirst);
        Assert.assertEquals(this.mDashBoardActivityController.mRecentContainer2.getVisibility(), View.GONE);
        Assert.assertEquals(this.mDashBoardActivityController.mRecentContainer.getVisibility(), View.VISIBLE);
    }

    @Test
    public void testLoadRecentLayout_OneRecent_SecondContainerIsVisible()
    {
        this.mDashBoardActivityController.isCurrentRecentContainerFirst = false;
        this.mDashBoardActivityController.numberOfRecentContacts = 1;
        this.mDashBoardActivityController.loadRecentLayout();
        Assert.assertTrue(this.mDashBoardActivityController.numberOfRecentContacts == 0);
        Assert.assertTrue(this.mDashBoardActivityController.isCurrentRecentContainerFirst);
        Assert.assertEquals(this.mDashBoardActivityController.mRecentContainer.getVisibility(), View.GONE);
        Assert.assertEquals(this.mDashBoardActivityController.mRecentContainer2.getVisibility(), View.VISIBLE);
    }

    @Test
    public void testLoadLocalContacts_OK()
    {
        this.mDashBoardActivityController.loadLocalContacts();
        Assert.assertTrue(this.mDashBoardActivityController.mProfileId != null);
    }

    @Test
    public void testLoadLocalContacts_Fail()
    {
        this.mDashBoardActivityController.mProfileId = null;
        this.mDashBoardActivityController.loadLocalContacts();
        Assert.assertTrue(this.mDashBoardActivityController.mProfileId == null);
    }

    @Test
    public void testLoadRecentContactsAndUnreadMessages_FirstContainer()
    {
        this.mDashBoardActivityController.isCurrentRecentContainerFirst = true;
        this.mDashBoardActivityController.loadRecentContactsAndUnreadMessages();
        Assert.assertTrue(this.mDashBoardActivityController.mRecentContainer != null);
    }

    @Test
    public void testLoadRecentContactsAndUnreadMessages_SecondContainer()
    {
        this.mDashBoardActivityController.isCurrentRecentContainerFirst = false;
        this.mDashBoardActivityController.loadRecentContactsAndUnreadMessages();
        Assert.assertTrue(this.mDashBoardActivityController.mRecentContainer2 != null);
    }

    @Test
    public void testLoadRecent_Fail()
    {

    }

    private void mockParams()
    {
        this.mDashBoardActivityController.mRealmNewsTransactions = Mockito.mock(RealmNewsTransactions.class);
        Mockito.when(this.mDashBoardActivityController.mRealmNewsTransactions.getAllNews(any(Realm.class))).thenReturn(getMockNewsArrayList());

        this.mDashBoardActivityController.mRealmContactTransactions = Mockito.mock(RealmContactTransactions.class);
        Mockito.when(this.mDashBoardActivityController.mRealmNewsTransactions.getAllNews(any(Realm.class))).thenReturn(getMockNewsArrayList());
    }

    private ArrayList<News> getMockNewsArrayList()
    {
        News mockNews = new News();
        ArrayList<News> mockNewsArrayList = new ArrayList<>();
        mockNews.setAuthor_avatar("mockAvatar");
        mockNews.setAuthor_name("mockName");
        mockNews.setCreated_at(Long.parseLong("12345678"));
        mockNews.setHtml("mockHtml");
        mockNews.setImage("mockURL");
        mockNews.setLink("mockLink");
        mockNews.setTitle("mockTitle");
        mockNews.setUpdated_at(Long.parseLong("123456789"));
        mockNews.setUuid("mockUID");

        mockNewsArrayList.add(mockNews);
        mockNews = new News();
        mockNews.setAuthor_avatar("mockAvatar2");
        mockNews.setAuthor_name("mockName2");
        mockNews.setCreated_at(Long.parseLong("12345678"));
        mockNews.setHtml("mockHtml2");
        mockNews.setImage("mockURL2");
        mockNews.setLink("mockLink2");
        mockNews.setTitle("mockTitle2");
        mockNews.setUpdated_at(Long.parseLong("123456789"));
        mockNews.setUuid("mockUID2");

        mockNewsArrayList.add(mockNews);
        mockNews = new News();
        mockNews.setAuthor_avatar("mockAvatar3");
        mockNews.setAuthor_name("mockName3");
        mockNews.setCreated_at(Long.parseLong("12345678"));
        mockNews.setHtml("mockHtml3");
        mockNews.setImage("mockURL3");
        mockNews.setLink("mockLink3");
        mockNews.setTitle("mockTitle3");
        mockNews.setUpdated_at(Long.parseLong("123456789"));
        mockNews.setUuid("mockUID3");

        mockNewsArrayList.add(mockNews);

        return mockNewsArrayList;
    }

    private ArrayList<RecentContact> fillMockRecentContactsList()
    {
        ArrayList<RecentContact> recentList = new ArrayList<>();
        RecentContact mockRecentContact = new RecentContact();

        mockRecentContact.setContactId("mc_55409316799f7e1a109446f4");
        mockRecentContact.setId("mc_55409316799f7e1a109446f4");
        mockRecentContact.setAction("sms");
        mockRecentContact.setPlatform("mc");
        mockRecentContact.setFirstName("Albert");
        mockRecentContact.setLastName("Mialet");
        mockRecentContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockRecentContact.setPhones("{\"phones\": [\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]}");
        mockRecentContact.setEmails("{\"emails\": [\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]}");
        mockRecentContact.setCompany("Stratesys");
        mockRecentContact.setPosition("Senior Developer Consultant");
        mockRecentContact.setOfficeLocation("Barcelona");
        mockRecentContact.setTimezone("Europe/Madrid");
        mockRecentContact.setAvailability("DADFE1");
        mockRecentContact.setTimestamp(Long.parseLong("1440658545874"));

        recentList.add(mockRecentContact);
        mockRecentContact = new RecentContact();

        mockRecentContact.setContactId("mg_55dc2a35a297b90a726e4cc2");
        mockRecentContact.setId("mg_55dc2a35a297b90a726e4cc2");
        mockRecentContact.setUniqueId("unique_mg_55dc2a35a297b90a726e4cc2");
        mockRecentContact.setAction("sms");
        mockRecentContact.setTimestamp(Long.parseLong("1440571515241"));
        mockRecentContact.setAvailability("available");

        recentList.add(mockRecentContact);

        return recentList;
    }
}
