package com.vodafone.mycomms.settings.globalcontacts;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.constants.Constants;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.realm.RealmLDAPSettingsTransactions;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
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
import org.robolectric.annotation.Config;

import java.io.PrintWriter;
import java.io.StringWriter;

import io.realm.Realm;
import model.GlobalContactsSettings;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 18,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*"})
@PrepareForTest({EndpointWrapper.class, RealmLDAPSettingsTransactions.class})

public class AddGlobalContactsActivityTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    Activity activity;
    Button btAddAccount;
    ImageView btBack;
    EditText etUser;
    EditText etPassword;
    LinearLayout layoutErrorBar;
    TextView tvError;
    MockWebServer webServer;

    @Before
    public void setUp() throws Exception
    {
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
        activity = Robolectric.setupActivity(AddGlobalContactsActivity.class);
        MockDataForTests.checkThreadSchedulers();

        etUser = (EditText)activity.findViewById(R.id.etEmail);
        etPassword = (EditText)activity.findViewById(R.id.etPassword);
        btAddAccount = (Button)activity.findViewById(R.id.btAddAccount);
        btBack = (ImageView)activity.findViewById(R.id.ivBtBack);
        layoutErrorBar = (LinearLayout)activity.findViewById(R.id.layoutErrorBar);
        tvError = (TextView)activity.findViewById(R.id.tvError);
    }

    @After
    public void tearDown() throws Exception
    {
        MockDataForTests.checkThreadSchedulers();

        if (webServer != null) webServer.shutdown();
        Robolectric.reset();

        activity = null;
        etUser = null;
        etPassword = null;
        btAddAccount = null;
        btBack = null;
        layoutErrorBar = null;
        tvError = null;
        webServer = null;

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
                System.err.println("Uncaught exception at :"+this.getClass().getSimpleName()+" \n" + writer.toString());
            }
        });
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        Thread.currentThread().interrupt();
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        junit.framework.Assert.assertTrue(activity != null);
        junit.framework.Assert.assertTrue(etUser != null);
        junit.framework.Assert.assertTrue(etPassword != null);
        junit.framework.Assert.assertTrue(btAddAccount != null);
        junit.framework.Assert.assertTrue(btBack != null);
        junit.framework.Assert.assertTrue(layoutErrorBar != null);
        junit.framework.Assert.assertTrue(tvError != null);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testSendFormatErrorAndReset() throws Exception {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        //User empty
        etPassword.setText("12345");
        etUser.setText("");
        btAddAccount.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        Assert.assertEquals(tvError.getText().toString(),
                activity.getString(R.string.credentials_are_incorrect));
        //Change user
        etUser.setText("userChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        //Email empty
        etPassword.setText("");
        etUser.setText("testUser");
        btAddAccount.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        Assert.assertEquals(tvError.getText().toString(),
                activity.getString(R.string.credentials_are_incorrect));
        //Change password
        etPassword.setText("passChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testDiscoverErrors() throws Exception {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        webServer = Util.newWebMockServer();
        String serverUrl = webServer.getUrl("/").toString();
        PowerMockito.mockStatic(EndpointWrapper.class);

        resetScreen();
        PowerMockito.when(EndpointWrapper.getLDAPDiscover()).thenReturn("http://localhost:12345");
        Assert.assertTrue(btAddAccount.performClick());
        MockDataForTests.checkThreadSchedulers(3000);

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        etUser.setText("testUserChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        //Discover response !=200
        System.err.println("******** Test: Discover Response !=200 ********");
        PowerMockito.when(EndpointWrapper.getLDAPDiscover()).thenReturn(serverUrl);
        webServer.enqueue(new MockResponse().setResponseCode(500));
        btAddAccount.performClick();
        MockDataForTests.checkThreadSchedulers(3000);

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        etUser.setText("testUserChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        //Discover incorrect JSON
        System.err.println("******** Test: Discover Incorrect JSON ********");
        PowerMockito.when(EndpointWrapper.getLDAPDiscover()).thenReturn(serverUrl);
        webServer.enqueue(new MockResponse().setResponseCode(200).setBody("{'incorrectJSON':'0'}"));
        btAddAccount.performClick();
        MockDataForTests.checkThreadSchedulers(3000);

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        etUser.setText("testUserChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testUserError() throws Exception {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        webServer = Util.newWebMockServer();
        String serverUrl = webServer.getUrl("/").toString();
        PowerMockito.mockStatic(EndpointWrapper.class);
        PowerMockito.when(EndpointWrapper.getLDAPDiscover()).thenReturn(serverUrl);

        String mockedDiscoverResponse =
                Constants.LDAP_DISCOVER_RESPONSE_OK.replace("mockUrl", serverUrl);

        //User connection error
        System.err.println("******** Test: User Connection Error ********");

        resetScreen();
        webServer.enqueue(new MockResponse().setResponseCode(200)
                .setBody(Constants.LDAP_DISCOVER_RESPONSE_OK
                        .replace("mockUrl", "http://localhost:12345")));

        btAddAccount.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        etUser.setText("testUserChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        //User response <400 || >=500
        System.err.println("******** Test: User Response!=401 ********");

        resetScreen();
        webServer.enqueue(new MockResponse().setResponseCode(200)
                .setBody(mockedDiscoverResponse));
        webServer.enqueue(new MockResponse().setResponseCode(500));
        btAddAccount.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        etUser.setText("testUserChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        //User response 401 without LDAP_HEADER_AUTH_KEY header
        System.err.println("******** Test: " +
                "User response 401 without LDAP_HEADER_AUTH_KEY header ********");

        resetScreen();
        webServer.enqueue(new MockResponse().setResponseCode(200)
                .setBody(mockedDiscoverResponse));
        webServer.enqueue(new MockResponse().setResponseCode(401));
        btAddAccount.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        etUser.setText("testUserChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testAuthErrors() throws Exception {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        webServer = Util.newWebMockServer();
        String serverUrl = webServer.getUrl("/").toString();
        PowerMockito.mockStatic(EndpointWrapper.class);
        PowerMockito.when(EndpointWrapper.getLDAPDiscover()).thenReturn(serverUrl);

        Request.Builder builder = Mockito.spy(Request.Builder.class);
        Mockito.when(builder.url(Constants.LDAP_AUTH_URL))
                .thenReturn(new Request.Builder().url(serverUrl));

        String mockedDiscoverResponse =
                Constants.LDAP_DISCOVER_RESPONSE_OK.replace("mockUrl",serverUrl);
        String mockedUserResponseHeader =
                Constants.LDAP_USER_RESPONSE_HEADER_OK.replace("mockUrl",serverUrl);

        //Auth connection error
        System.err.println("******** Test: Auth Connection Error ********");

        resetScreen();
        webServer.enqueue(new MockResponse().setResponseCode(200)
                .setBody(mockedDiscoverResponse));
        webServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setHeader(Constants.LDAP_USER_RESPONSE_HEADER_KEY,
                        Constants.LDAP_USER_RESPONSE_HEADER_OK
                                .replace("mockUrl", "http://localhost:12345")));

        btAddAccount.performClick();
        MockDataForTests.checkThreadSchedulers(3000);

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        etUser.setText("testUserChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        //Auth response >=400 || <500
        System.err.println("******** Test: Auth Response >=400 && <500 ********");

        resetScreen();
        PowerMockito.when(EndpointWrapper.getLDAPDiscover()).thenReturn(serverUrl);
        webServer.enqueue(new MockResponse().setResponseCode(200)
                .setBody(mockedDiscoverResponse));
        webServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setHeader(Constants.LDAP_USER_RESPONSE_HEADER_KEY,
                        mockedUserResponseHeader));
        webServer.enqueue(new MockResponse()
                .setResponseCode(401));

        btAddAccount.performClick();
        MockDataForTests.checkThreadSchedulers(3000);

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        etUser.setText("testUserChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        //Auth response >=500
        System.err.println("******** Test: Auth response >=500 ********");

        resetScreen();
        PowerMockito.when(EndpointWrapper.getLDAPDiscover()).thenReturn(serverUrl);
        webServer.enqueue(new MockResponse().setResponseCode(200)
                .setBody(mockedDiscoverResponse));
        webServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setHeader(Constants.LDAP_USER_RESPONSE_HEADER_KEY,
                        mockedUserResponseHeader));
        webServer.enqueue(new MockResponse()
                .setResponseCode(500));

        btAddAccount.performClick();
        MockDataForTests.checkThreadSchedulers(3000);

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        etUser.setText("testUserChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        //Auth response with malformed JSON
        System.err.println("******** Test: Auth response with malformed JSON ********");

        resetScreen();
        PowerMockito.when(EndpointWrapper.getLDAPDiscover()).thenReturn(serverUrl);
        webServer.enqueue(new MockResponse().setResponseCode(200)
                .setBody(mockedDiscoverResponse));
        webServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setHeader(Constants.LDAP_USER_RESPONSE_HEADER_KEY,
                        mockedUserResponseHeader));
        webServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(Constants.LDAP_USER_RESPONSE_HEADER_KEY,
                        Constants.LDAP_USER_RESPONSE_HEADER_OK)
                .setBody(Constants.LDAP_AUTH_RESPONSE_ERROR));

        //Save fake profile
        Context context = RuntimeEnvironment.application.getApplicationContext();
        SharedPreferences sp = context.getSharedPreferences(
                com.vodafone.mycomms.util.Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(
                com.vodafone.mycomms.util.Constants.PROFILE_ID_SHARED_PREF,
                Constants.PROFILE_ID)
                .apply();

        btAddAccount.performClick();
        MockDataForTests.checkThreadSchedulers(3000);

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        etUser.setText("testUserChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testOK()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        try {
            webServer = Util.newWebMockServer();
        }
        catch (Exception e)
        {
            Assert.fail();
        }

        String serverUrl = webServer.getUrl("/").toString();
        PowerMockito.mockStatic(EndpointWrapper.class);

        String mockedDiscoverResponse =
                Constants.LDAP_DISCOVER_RESPONSE_OK.replace("mockUrl",serverUrl);
        String mockedUserResponseHeader =
                Constants.LDAP_USER_RESPONSE_HEADER_OK.replace("mockUrl",serverUrl);

        System.err.println("******** Test: Everything OK ********");
        resetScreen();

        //Mock web server responses
        PowerMockito.when(EndpointWrapper.getLDAPDiscover()).thenReturn(serverUrl);
        webServer.enqueue(new MockResponse().setResponseCode(200)
                .setBody(mockedDiscoverResponse));
        webServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setHeader(Constants.LDAP_USER_RESPONSE_HEADER_KEY,
                        mockedUserResponseHeader));
        webServer.enqueue(new MockResponse()
                        .setResponseCode(200)
                        .setBody(Constants.LDAP_AUTH_RESPONSE_OK)
        );

        //Mock save settings
        GlobalContactsSettings settings = new GlobalContactsSettings(
                Constants.PROFILE_ID,
                Constants.LDAP_USER,
                Constants.LDAP_PASSWORD,
                Constants.LDAP_TOKEN,
                Constants.LDAP_TOKEN_TYPE,
                Constants.LDAP_RETURN_URL
        );

        PowerMockito.mockStatic(RealmLDAPSettingsTransactions.class);
        PowerMockito.when(RealmLDAPSettingsTransactions
                .createOrUpdateData(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.any(Realm.class)))
                .thenReturn(settings);

        //Save fake profile
        Context context = RuntimeEnvironment.application.getApplicationContext();
        SharedPreferences sp = context.getSharedPreferences(
                com.vodafone.mycomms.util.Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(
                com.vodafone.mycomms.util.Constants.PROFILE_ID_SHARED_PREF,
                Constants.PROFILE_ID)
                .apply();

        //Execute
        layoutErrorBar.setVisibility(View.VISIBLE);
        btAddAccount.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);
        Assert.assertTrue(activity.isFinishing());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testConnectivityChanged() throws Exception {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        resetScreen();

        System.err.println("******** Test: Connectivity Offline received ********");
        BusProvider.getInstance().post(new ConnectivityChanged(ConnectivityStatus.OFFLINE));
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);

        System.err.println("******** Test: Connectivity Online received ********");
        BusProvider.getInstance().post(
                new ConnectivityChanged(ConnectivityStatus.MOBILE_CONNECTED));
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testGoBack() throws Exception {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                ,Thread.currentThread().getStackTrace()[1].getMethodName());

        btBack.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(activity.isFinishing());
        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    private void resetScreen() {
        etUser.setText("testUser");
        etPassword.setText("testPassword");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);
    }
}
