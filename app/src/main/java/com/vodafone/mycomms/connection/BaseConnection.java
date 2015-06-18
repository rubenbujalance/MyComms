package com.vodafone.mycomms.connection;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.framework.library.connection.ConnectionInterfaces.ConnectionListener;
import com.framework.library.connection.DefaultConnection;
import com.framework.library.connection.HttpConnection;
import com.framework.library.exception.ConnectionException;
import com.framework.library.model.IModel;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UserSecurity;

/**
 * Created by str_vig on 15/05/2015.
 */
public class BaseConnection extends DefaultConnection {

    private static final String KEY_AUTHORIZATION = "Authorization";
    private static final String TAG_VALUE_AUTHORIZATION = "Bearer ";

    private static String appVersion = null;
    private static final String KEY_APP_VERSION = "x-mycomms-version";
    private static final String KEY_CONTENT_TYPE = "Content-Type";
    private static final String VALUE_CONTENT_TYPE = "application/json; charset=utf-8";
    public static final String VALUE_UNAUTHORIZED = "unauthorized";
    //TODO: HARDCODED
    public static final String AUTHORIZATION = "Authorization";
    public static final String AUTHORIZATION_CODE = "Bearer 53A85tTGPZXmyOFsugkDI42Z8_p0V_hElTMg6D1T1XfvbDTxXOsOYsAH4v0ygFiqSSNKvlzeCKOlj8ht3BqrMUShPu8PhfxblJEy3S2rZvEZfupmh_uJ6WeHtX1MVt5g8p6ds4Et8xXUJ9CKLbRxuVJI-V0aGHQ9mgi0_eI5IFE";

    public static final String VALUE_INVALID_ACCESS_TOKEN = "invalid_access_token";
    public static final String VALUE_INVALID_TOKEN = "invalid_token";
    public static final String VALUE_INVALID_VERSION = "invalid_version";
    private static int numOfFailedAuthRequest = 0;
    public static final int MAX_AUTH_RETRY = 1;
    private final Context context;
    private final ConnectionListener listener;

    /**
     * @param URL
     * @param context
     * @param listener
     * @param method   HTTP
     * @param news
     */
    protected BaseConnection(String URL, Context context, ConnectionListener listener, int method, boolean news) {

        if (news == true) {
            setUrl(APIWrapper.getHttpProtocol() + EndpointWrapper.getBaseNewsURL() + URL);
        } else {
            setUrl(APIWrapper.getHttpProtocol() + EndpointWrapper.getBaseURL() + URL);
        }

        this.listener = listener;
        setConnectionHandler(new ConnectionHandler(this, listener));

        setConnection(new HttpConnection<IModel>(
                context,
                method,
                getUrl(),
                getConnectionHandler()));

        getConnection().setTypeResult(HttpConnection.TypeResult.JSON);

        setDefaultHeaders(context);
        this.context = context;
    }

    public static int getNumOfFailedAuthRequest() {
        return numOfFailedAuthRequest;
    }

    public static void setNumOfFailedAuthRequest(int numOfFailedAuthRequest) {
        BaseConnection.numOfFailedAuthRequest = numOfFailedAuthRequest;
    }

    /**
     * Set the default headers needed for every API call like:
     * - application version
     * - content type
     *
     * @param context
     */
    private void setDefaultHeaders(Context context) {
        HttpConnection connection = getConnection();
        if (connection != null) {
            if (appVersion == null) {
                PackageInfo pinfo = null;
                try {
                    pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.wtf(Constants.TAG, "BaseConnection.setDefaultHeaders: Couldn't get application version:", e);
                    return;
                }

                int versionCode = pinfo.versionCode;
                String versionName = pinfo.versionName;
                appVersion = "android/" + versionName + "." + versionCode;
            }

            connection.addHeader(KEY_APP_VERSION, appVersion);
            connection.addHeader(KEY_CONTENT_TYPE, VALUE_CONTENT_TYPE);

            String authHeader = UserSecurity.getAccessToken(context);
            if (authHeader != null && authHeader.length() > 0) {
                Log.d(Constants.TAG, "BaseConnection.setDefaultHeaders: Setting auth header for: " + this + ", " +  authHeader);
               // authHeader = "rXN_WMVUp6OZew0HHJn9QACqWjZ1tNgcFCJROD6IgvMsa0MPMzGpjqA9BQz1VGQjFjfgSdFIMGxBPgy9qziJ4XLx7w6Rr6I8NOyxLN9IQX2UuLwC8J9MgtUW1RZRkLTW4VOrrj5zvecpOlv8m_FeUfX_oQSiFIzQEjtTe1cretL";
                connection.addHeader(KEY_AUTHORIZATION, TAG_VALUE_AUTHORIZATION + authHeader);

               // Log.d(Constants.TAG, "BaseConnection.setDefaultHeaders: Setting auth header for: " + this + ", " + authHeader);

            }

        } else {
            Log.wtf(Constants.TAG, "BaseConnection.setDefaultHeaders: Connection is not inicialized...");
        }

    }

    /**
     * Set the payload for the
     *
     * @param json
     */
    public void setPayLoad(String json) {
        this.getConnection().setEncoding("UTF-8");
        this.getConnection().setData(json.toString());
    }

    @Override
    public void setUrl(String url) {
        Log.d(Constants.TAG, "BaseConnection.setUrl: " + url);
        super.setUrl(url);
    }

    public void request() {
        if (isConnected()) {
            super.request();
        } else {
            listener.onConnectionError(new ConnectionException(getUrl(), new ConnectionNotAvailableException(), null, null, getConnection()  ));
        }

    }

    public boolean isConnected() {
        boolean isConnected = false;
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        Log.d(Constants.TAG, "BaseConnection.isConnected: " + isConnected);
        return isConnected;
    }

    public static boolean isConnected(Context context) {
        boolean isConnected = false;
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        Log.d(Constants.TAG, "BaseConnection.isConnected: " + isConnected);
        return isConnected;
    }

    public Context getContext() {
        return context;
    }

    public void addMultipartData()
    {

    }
    
}
