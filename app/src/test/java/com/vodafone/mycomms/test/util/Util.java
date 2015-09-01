package com.vodafone.mycomms.test.util;

import com.squareup.okhttp.mockwebserver.MockWebServer;

import android.content.Context;
import android.content.SharedPreferences;

import com.vodafone.mycomms.constants.Constants;
import com.vodafone.mycomms.realm.RealmLDAPSettingsTransactions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.mockito.Mockito;
import org.mockito.internal.configuration.GlobalConfiguration;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.ClassLoaderUtil;
import org.powermock.reflect.Whitebox;
import org.robolectric.RuntimeEnvironment;

import io.realm.Realm;
import model.GlobalContactsSettings;

/**
 * Created by str_evc on 21/05/2015.
 */
public class Util {

    private static final ProtocolVersion DEFAULT_HTTP_VERSION = HttpVersion.HTTP_1_1;
    private static final String DEFAULT_HTTP_RETURN_REASON = "OK";


    public static HttpResponse buildResponse(int code) throws Exception {
        return buildResponse(code, null);
    }

    public static HttpResponse buildResponse(int code, String stringEntity) throws Exception {
        return buildResponse(DEFAULT_HTTP_VERSION, code, DEFAULT_HTTP_RETURN_REASON, stringEntity);
    }

    public static HttpResponse buildResponse(ProtocolVersion ver, int code, String reason, String stringEntity) throws Exception {
        HttpResponse httpResponse = new BasicHttpResponse(ver, code, reason);
        httpResponse.setHeader("Content-Type", "application/json");
        if (stringEntity != null && stringEntity.length() > 0) {
            HttpEntity entity = new StringEntity(stringEntity);
            httpResponse.setEntity(entity);
        }
        return httpResponse;
    }

    public static class MockitoStateCleaner implements Runnable {
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

    public static String startWebMockServer(MockWebServer webServer) throws Exception {
        //OkHttp mocked web server
        webServer = new MockWebServer();
        webServer.useHttps(null, false);

        //Connect OkHttp calls with MockWebServer
        webServer.start();
        String serverUrl = webServer.getUrl("/").toString();

        return serverUrl;
    }

    public static void mockGlobalSettings(){
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
    }

    public static void saveFakeProfile(){
        Context context = RuntimeEnvironment.application.getApplicationContext();
        SharedPreferences sp = context.getSharedPreferences(
                com.vodafone.mycomms.util.Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(
                com.vodafone.mycomms.util.Constants.PROFILE_ID_SHARED_PREF,
                Constants.PROFILE_ID)
                .commit();
    }




}
