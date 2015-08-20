package com.vodafone.mycomms.settings.globalcontacts;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.realm.RealmLDAPSettingsTransactions;
import com.vodafone.mycomms.util.Constants;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import model.GlobalContactsSettings;

/**
 * Created by str_rbm on 05/08/2015.
 */
public class GlobalContactsController {

    /*
    PROCESS TO LOGIN IN LDAP
    1. Call "discover" endpoint to get the user URL (get a 200 code)
    2. Call "user" url (receiving a 401-Unauthorized) to get the final auth url as a header
    3. Call the final URL to authenticate user into LDAP (receiving 200)
        3a. If a 401-Unauthorized is received, credentials are incorrect
     */

    //Constants
    private final String LDAP_AUTH_URL_KEY = "MsRtcOAuth";
    private final String LDAP_HEADER_AUTH_KEY = "WWW-Authenticate";
    private final String LDAP_TOKEN_KEY = "access_token";
    private final String LDAP_TOKEN_TYPE_KEY = "token_type";
    private final String LDAP_USERNAME_KEY = "username";
    private final String LDAP_PASSWORD_KEY = "password";
    private final String LDAP_GRANT_TYPE_KEY = "grant_type";
    private final String LDAP_GRANT_TYPE_VALUE = "password";
    private final String LDAP_HEADER_CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded";

    //Aux
    private OkHttpClient client;

    private String tempUrl;
    private String profileId;

    //Params
    private String user, password;
    private GlobalContactsCallback cb;
    private Context context;

    //Public constructor
    public GlobalContactsController() {
        client = new OkHttpClient();
        client.setConnectTimeout(10, TimeUnit.SECONDS);
        client.setReadTimeout(30, TimeUnit.SECONDS);
        client.setRetryOnConnectionFailure(false);
    }

    //Init the process from new loaded data
    public void callLDAPAuthProcess(String user, String password,
                                Context context, GlobalContactsCallback cb) {
        this.user = user;
        this.password = password;
        this.cb = cb;
        this.context = context;

        callLDAPDiscover();
    }

    //Step 1
    private void callLDAPDiscover() {
        Request.Builder builder = new Request.Builder();
        //TODO: Check why this crashes on MASTER
//        Request request = builder.url(EndpointWrapper.getLDAPDiscover()).build();
        Request request = builder.url(Constants.LDAPDISCOVER).build();

        System.err.println("- Discover call: " + request.urlString());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                System.err.println("- Discover onFailure");
                cb.onFailure(context.getString(R.string.connection_error), 0);
            }

            @Override
            public void onResponse(Response response) {
                try {
                    int code = response.code();
                    System.err.println("- Discover onResponse (" + code + ")");

                    if (code != 200) {
                        cb.onFailure(context.getString(R.string.error_reading_data_from_server), code);
                    } else {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        jsonObject = jsonObject.getJSONObject("_links");
                        String href = ((JSONObject) jsonObject.get("user"))
                                .getString("href");

                        //If everything is OK, save url and continue the process
                        tempUrl = href;
                        callLDAPUser(href);
                    }
                } catch (Exception ex) {
                    System.err.println("- Discover onResponse Exception (" + ex.getMessage() + ")");
                    Log.e(Constants.TAG,
                            "GlobalContactsController.callLDAPDiscover: ", ex);
                    cb.onFailure(context.getString(R.string.error_reading_data_from_server), 0);
                }
            }
        });
    }

    //Step 2
    private void callLDAPUser(String url) {
        Request.Builder builder = new Request.Builder();
        Request request = builder.url(url).build();

        System.err.println("- User call: " + request.urlString());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                System.err.println("- User onFailure");
                cb.onFailure(context.getString(R.string.connection_error), 0);
            }

            @Override
            public void onResponse(Response response) {
                try {
                    int code = response.code();
                    System.err.println("- User onResponse ("+code+")");

                    if (code >= 400 && code < 500) {
                        Headers headers = response.headers();
                        if (headers == null || headers.size() == 0 ||
                                headers.get(LDAP_HEADER_AUTH_KEY) == null) {
                            throw new Exception(
                                    context.getString(R.string.error_reading_data_from_server));
                        } else {
                            String[] authHeader = headers.get(LDAP_HEADER_AUTH_KEY).split(",");
                            String hrefHeader = null;

                            for (int i = 0; i < authHeader.length; i++) {
                                if (authHeader[i].trim().startsWith(LDAP_AUTH_URL_KEY)) {
                                    hrefHeader = authHeader[i].trim();
                                    break;
                                }
                            }

                            if (hrefHeader == null) throw new Exception(
                                    context.getString(R.string.error_reading_data_from_server));

                            String href = hrefHeader.split("=")[1]
                                    .replaceAll("\"", "");

                            //If evething it's ok, continue the process
                            callLDAPAuth(href);
                        }
                    }
                    else {
                        throw new Exception(
                                context.getString(R.string.error_reading_data_from_server));
                    }
                } catch (Exception ex) {
                    System.err.println("- User onResponse Exception (" + ex.getMessage() + ")");
                    Log.e(Constants.TAG, "GlobalContactsController.callLDAPUser: ", ex);
                    cb.onFailure(context.getString(R.string.error_reading_data_from_server), 0);
                }
            }
        });
    }

    //Step 3
    private void callLDAPAuth(String url) {
        RequestBody body;

        try {
            String params = LDAP_GRANT_TYPE_KEY + "=" + LDAP_GRANT_TYPE_VALUE + "&" +
                    LDAP_USERNAME_KEY + "=" + user + "&" +
                    LDAP_PASSWORD_KEY + "=" + password;

            body = RequestBody.create(
                    MediaType.parse(LDAP_HEADER_CONTENT_TYPE_VALUE), params);
        } catch (Exception e) {
            Log.e(Constants.TAG, "GlobalContactsController.callLDAPAuth: ",e);
            cb.onFailure(context.getString(R.string.error_reading_data_from_server), 0);
            return;
        }

        Request.Builder builder = new Request.Builder();
        Request request = builder
                .url(url)
                .addHeader(Constants.API_HTTP_HEADER_CONTENTTYPE,
                        LDAP_HEADER_CONTENT_TYPE_VALUE)
                .post(body)
                .build();

        System.err.println("- Auth call: " + request.urlString());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                System.err.println("- Auth onFailure");
                cb.onFailure(context.getString(R.string.connection_error), 0);
            }

            @Override
            public void onResponse(Response response) {
                try {
                    int code = response.code();
                    System.err.println("- Auth onResponse ("+code+")");

                    if (code >= 400 && code < 500) {
                        cb.onFailure(context.getString(R.string.credentials_are_incorrect), code);
                    } else if (code >= 200 && code < 300) {
                        SharedPreferences sp = context.getSharedPreferences(
                                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
                        String profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);

                        if (profileId == null) {
                            throw new Exception("Error getting profileId");
                        }

                        String token, tokenType;

                        JSONObject jsonObject = new JSONObject(response.body().string());
                        token = jsonObject.getString(LDAP_TOKEN_KEY);
                        tokenType = jsonObject.getString(LDAP_TOKEN_TYPE_KEY);

                        if (token == null || tokenType == null || tempUrl==null)
                            throw new Exception("Error getting token from response");

                        GlobalContactsSettings settingsDB = RealmLDAPSettingsTransactions
                                .createOrUpdateData(profileId, user, password,
                                        token, tokenType, tempUrl, null);

                        if(settingsDB!=null) {
                            cb.onSuccess(settingsDB);
                        }
                        else {
                            cb.onFailure(context.getString(
                                    R.string.error_reading_data_from_server), code);
                        }
                    } else {
                        cb.onFailure(context.getString(R.string.error_reading_data_from_server), code);
                    }
                } catch (Exception ex) {
                    System.err.println("- Auth onResponse Exception (" + ex.getMessage() + ")");
                    Log.e(Constants.TAG, "GlobalContactsController.callLDAPAuth: ", ex);
                    cb.onFailure(context.getString(R.string.error_reading_data_from_server), 0);
                }
            }
        });
    }

    public interface GlobalContactsCallback  {
        /**
         * called when the process has failed
         * @param error - in case of failure, error message to show
         * @param errorCode - the error code
         */
        public void onFailure(String error, int errorCode);

        /**
         * called when the process has been finished successfully
         */
        public void onSuccess(GlobalContactsSettings settings);
    }
}
