package com.vodafone.mycomms.settings.globalcontacts;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.constants.Constants;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.configuration.GlobalConfiguration;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.ClassLoaderUtil;
import org.powermock.core.MockRepository;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.powermock.reflect.Whitebox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*"})
@PrepareForTest(EndpointWrapper.class)

public class AddGlobalContactsActivityTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    Activity activity;
    Button btAddAccount;
    EditText etUser;
    EditText etPassword;
    LinearLayout layoutErrorBar;
    TextView tvError;
    MockWebServer webServer;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.setupActivity(AddGlobalContactsActivity.class);
        etUser = (EditText)activity.findViewById(R.id.etEmail);
        etPassword = (EditText)activity.findViewById(R.id.etPassword);
        btAddAccount = (Button)activity.findViewById(R.id.btAddAccount);
        layoutErrorBar = (LinearLayout)activity.findViewById(R.id.layoutErrorBar);
        tvError = (TextView)activity.findViewById(R.id.tvError);

        MockRepository.addAfterMethodRunner(new MockitoStateCleaner());
    }

    @Test
    public void testSendFormatErrorAndReset() throws Exception {
        //User empty
        etPassword.setText("12345");
        etUser.setText("");
        btAddAccount.performClick();
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
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        Assert.assertEquals(tvError.getText().toString(),
                activity.getString(R.string.credentials_are_incorrect));
        //Change password
        etPassword.setText("passChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);
    }

    @Test
    public void testDiscoverErrors() throws Exception {
        String serverUrl = startWebMockServer();
        PowerMockito.mockStatic(EndpointWrapper.class);

        //Discover connection error
        System.err.println("******** Test: Discover Connection Error ********");

        resetScreen();
        PowerMockito.when(EndpointWrapper.getLDAPDiscover()).thenReturn("http://localhost:12345");
        btAddAccount.performClick();
        Thread.sleep(2000);
        Robolectric.flushForegroundThreadScheduler();

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        etUser.setText("testUserChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        //Discover response !=200
        System.err.println("******** Test: Discover Response !=200 ********");
        PowerMockito.when(EndpointWrapper.getLDAPDiscover()).thenReturn(serverUrl);
        webServer.enqueue(new MockResponse().setResponseCode(500));
        btAddAccount.performClick();
        Thread.sleep(2000);
        Robolectric.flushForegroundThreadScheduler();

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        etUser.setText("testUserChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        //Discover incorrect JSON
        System.err.println("******** Test: Discover Incorrect JSON ********");
        PowerMockito.when(EndpointWrapper.getLDAPDiscover()).thenReturn(serverUrl);
        webServer.enqueue(new MockResponse().setResponseCode(200).setBody("{'incorrectJSON':'0'}"));
        btAddAccount.performClick();
        Thread.sleep(2000);
        Robolectric.flushForegroundThreadScheduler();

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        etUser.setText("testUserChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        webServer.shutdown();
    }

    @Test
    public void testUserError() throws Exception {
        String serverUrl = startWebMockServer();
        PowerMockito.mockStatic(EndpointWrapper.class);

        //User connection error
        System.err.println("******** Test: User Connection Error ********");

        resetScreen();
        PowerMockito.when(EndpointWrapper.getLDAPDiscover())
                .thenReturn(serverUrl)
                .thenReturn("http://localhost:12345");
        webServer.enqueue(new MockResponse().setResponseCode(200)
                .setBody(Constants.LDAP_DISCOVER_RESPONSE_OK));

        btAddAccount.performClick();
        Thread.sleep(3000);
        Robolectric.flushForegroundThreadScheduler();

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        etUser.setText("testUserChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        //User response <400 || >=500
        System.err.println("******** Test: User Response!=401 ********");

        resetScreen();
        PowerMockito.when(EndpointWrapper.getLDAPDiscover()).thenReturn(serverUrl);
        webServer.enqueue(new MockResponse().setResponseCode(200)
                .setBody(Constants.LDAP_DISCOVER_RESPONSE_OK));
        webServer.enqueue(new MockResponse().setResponseCode(500));
        btAddAccount.performClick();
        Thread.sleep(3000);
        Robolectric.flushForegroundThreadScheduler();

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        etUser.setText("testUserChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        //User response 401 without LDAP_HEADER_AUTH_KEY header
        System.err.println("******** Test: " +
                "User response 401 without LDAP_HEADER_AUTH_KEY header ********");

        resetScreen();
        webServer.enqueue(new MockResponse().setResponseCode(200)
                .setBody(Constants.LDAP_DISCOVER_RESPONSE_OK));
        webServer.enqueue(new MockResponse().setResponseCode(401));
        btAddAccount.performClick();
        Thread.sleep(3000);
        Robolectric.flushForegroundThreadScheduler();

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        etUser.setText("testUserChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        webServer.shutdown();
    }

    @Test
    public void testAuthErrors() throws Exception {
        String serverUrl = startWebMockServer();
        PowerMockito.mockStatic(EndpointWrapper.class);

        //User connection error
        System.err.println("******** Test: Auth Connection Error ********");

        resetScreen();
        PowerMockito.when(EndpointWrapper.getLDAPDiscover()).thenReturn(serverUrl);
        PowerMockito.when(EndpointWrapper.getLDAPDiscover()).thenReturn(serverUrl);
        PowerMockito.when(EndpointWrapper.getLDAPDiscover()).thenReturn("http://localhost:12345");
        webServer.enqueue(new MockResponse().setResponseCode(200)
                .setBody(Constants.LDAP_DISCOVER_RESPONSE_OK));
        webServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setHeader(Constants.LDAP_USER_RESPONSE_HEADER_KEY,
                        Constants.LDAP_USER_RESPONSE_HEADER_OK));

        btAddAccount.performClick();
        Thread.sleep(4000);
        Robolectric.flushForegroundThreadScheduler();

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        etUser.setText("testUserChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        //User response >=400 || <500
        System.err.println("******** Test: Auth Response >=400 && <500 ********");

        resetScreen();
        PowerMockito.when(EndpointWrapper.getLDAPDiscover()).thenReturn(serverUrl);
        webServer.enqueue(new MockResponse().setResponseCode(200)
                .setBody(Constants.LDAP_DISCOVER_RESPONSE_OK));
        webServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setHeader(Constants.LDAP_USER_RESPONSE_HEADER_KEY,
                        Constants.LDAP_USER_RESPONSE_HEADER_OK));
        webServer.enqueue(new MockResponse()
                .setResponseCode(401));

        btAddAccount.performClick();
        Thread.sleep(4000);
        Robolectric.flushForegroundThreadScheduler();

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        etUser.setText("testUserChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        //User response >=500 without LDAP_HEADER_AUTH_KEY header
        System.err.println("******** Test: User response 401 without LDAP_HEADER_AUTH_KEY header ********");

        resetScreen();
        PowerMockito.when(EndpointWrapper.getLDAPDiscover()).thenReturn(serverUrl);
        webServer.enqueue(new MockResponse().setResponseCode(200)
                .setBody(Constants.LDAP_DISCOVER_RESPONSE_OK));
        webServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setHeader(Constants.LDAP_USER_RESPONSE_HEADER_KEY,
                        Constants.LDAP_USER_RESPONSE_HEADER_OK));
        webServer.enqueue(new MockResponse()
                .setResponseCode(500));

        btAddAccount.performClick();
        Thread.sleep(60000);
        Robolectric.flushForegroundThreadScheduler();

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.VISIBLE);
        etUser.setText("testUserChanged");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);

        webServer.shutdown();
    }

    @Test
    public void testOK() throws Exception {
        String serverUrl = startWebMockServer();
        PowerMockito.mockStatic(EndpointWrapper.class);

        System.err.println("******** Test: Everything OK ********");

        resetScreen();
        PowerMockito.when(EndpointWrapper.getLDAPDiscover()).thenReturn(serverUrl);
        webServer.enqueue(new MockResponse().setResponseCode(200)
                .setBody(Constants.LDAP_DISCOVER_RESPONSE_OK));
        webServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setHeader(Constants.LDAP_USER_RESPONSE_HEADER_KEY,
                        Constants.LDAP_USER_RESPONSE_HEADER_OK));
        btAddAccount.performClick();
        Thread.sleep(2000);
        Robolectric.flushForegroundThreadScheduler();

        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);
    }

    private String startWebMockServer() throws Exception {
        //OkHttp mocked web server
        webServer = new MockWebServer();
        webServer.useHttps(null, false);

        //Connect OkHttp calls with MockWebServer
        webServer.start();
        String serverUrl = webServer.getUrl("/").toString();

        return serverUrl;
    }

    private void resetScreen() {
        etUser.setText("testUser");
        etPassword.setText("testPassword");
        Assert.assertTrue(layoutErrorBar.getVisibility() == View.GONE);
    }

    private static class MockitoStateCleaner implements Runnable {
        public void run() {
            clearMockProgress();
            clearConfiguration();
        }

        private void clearMockProgress() {
            clearThreadLocalIn(ThreadSafeMockingProgress.class);
        }

        private void clearConfiguration() {
            clearThreadLocalIn(GlobalConfiguration.class);
        }

        private void clearThreadLocalIn(Class<?> cls) {
            Whitebox.getInternalState(cls, ThreadLocal.class).set(null);
            final Class<?> clazz = ClassLoaderUtil.loadClass(cls, ClassLoader.getSystemClassLoader());
            Whitebox.getInternalState(clazz, ThreadLocal.class).set(null);
        }
    }
}
