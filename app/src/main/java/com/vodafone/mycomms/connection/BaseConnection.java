package com.vodafone.mycomms.connection;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.framework.library.connection.ConnectionInterfaces;
import com.framework.library.connection.DefaultConnection;
import com.framework.library.connection.HttpConnection;
import com.framework.library.model.IModel;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.Constants;

/**
 * Created by str_vig on 15/05/2015.
 */
public class BaseConnection extends DefaultConnection {

    private static String appVersion = null;
    private static final String KEY_APP_VERSION = "x-mycomms-version";
    private static final String KEY_CONTENT_TYPE = "Content-Type";
    private static final String VALUE_CONTENT_TYPE = "application/json; charset=utf-8";

    public static final String VALUE_UNAUTHORIZED = "unauthorized";

    private static int numOfFailedAuthRequest = 0;
    public static final int MAX_AUTH_RETRY = 5;

    /**
     *
     * @param URL
     * @param context
     * @param listener
     * @param method HTTP
     */
    protected BaseConnection(String URL, Context context, ConnectionInterfaces.ConnectionListener listener , int method){

        setUrl(APIWrapper.getHttpProtocol() + EndpointWrapper.getBaseURL() + URL);

        setConnectionHandler(new ConnectionHandler(this, listener));

        setConnection(new HttpConnection<IModel>(
                context,
                method,
                getUrl(),
                getConnectionHandler()));

        getConnection().setTypeResult(HttpConnection.TypeResult.JSON);

        setDefaultHeaders(context);
    }

    public static int getNumOfFailedAuthRequest() {
        return numOfFailedAuthRequest;
    }

    public static void setNumOfFailedAuthRequest(int numOfFailedAuthRequest) {
        BaseConnection.numOfFailedAuthRequest = numOfFailedAuthRequest;
    }

    /**
     * Set the default headers needed for every API call like:
     *      - application version
     *      - content type
     *
     * @param context
     */
    protected void setDefaultHeaders(Context context)  {
        HttpConnection connection = getConnection();
        if(connection != null) {
            if(appVersion == null) {
                PackageInfo pinfo = null;
                try {
                    pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                }catch (PackageManager.NameNotFoundException e){
                    Log.wtf(Constants.TAG, "BaseConnection.setDefaultHeaders: Couldn't get application version:" , e);
                    return;
                }

                int versionCode = pinfo.versionCode;
                String versionName = pinfo.versionName;
                appVersion = "android/" + versionName + "." + versionCode;
            }

            connection.addHeader(KEY_APP_VERSION, appVersion);
            connection.addHeader(KEY_CONTENT_TYPE, VALUE_CONTENT_TYPE);

        } else{
            Log.wtf(Constants.TAG, "BaseConnection.setDefaultHeaders: Connection is not inicialized...");
        }

    }
}
