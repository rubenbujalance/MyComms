package com.vodafone.mycomms.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import java.net.URI;

public class APIWrapper {

    private static final String httpProtocol = "https://";

    public static String getHttpProtocol() {
        return httpProtocol;
    }

    /*
    Method to encapsulate any http post call to the API
    Params
        restRequest: String of url suffix (ex: "/api/profile")
        params: JSONObject of input parameters, to be set into body
        headers: HashMap of key-value pairs for header params
     */
    public static HashMap<String,Object> httpPostAPI (String restRequest, HashMap<String,Object> params, HashMap<String,Object> headers, Context context)
    {
        JSONObject json = null;

        if(params != null)
            json = new JSONObject(params);
        else json = new JSONObject();

        HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000);
        HttpResponse response;
        String textResponse = null;
        JSONObject jsonResponse = null;

        try {
            HttpPost httpPost = new HttpPost(getHttpProtocol() + EndpointWrapper.getBaseURL() + restRequest);

            //Set version and Content-Type in Header
            if(headers == null) headers = new HashMap<>();

            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            int versionCode = pinfo.versionCode;
            String versionName = pinfo.versionName;

            headers.put("x-mycomms-version","android/"+versionName+"."+versionCode);
            headers.put("Content-Type","application/json; charset=utf-8");

            //Set body JSON
            ByteArrayEntity entity = new ByteArrayEntity(json.toString().getBytes("UTF8"));
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
            httpPost.setEntity(entity);

            Iterator headersIt = headers.keySet().iterator();
            String param;
            String value;

            while(headersIt.hasNext())
            {
                param = (String)headersIt.next();
                value = (String)headers.get(param);
                httpPost.addHeader(param, value);
            }

            response = httpClient.execute(httpPost);

        } catch (Exception e) {
            Log.e(Constants.TAG, "APIWrapper.httpPostAPI: \n" + e.toString());
            return null;
        }

        return httpResToHash(response);
    }

    public static HashMap<String,Object> httpGetAPI(String restRequest, HashMap headers, Context context)
    {
        String url = getHttpProtocol() + EndpointWrapper.getBaseURL() + restRequest;
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse httpResponse;
        HttpGet httpGet;

        try {

            httpGet = new HttpGet(url);

            //Set version and Content-Type in Header
            if(headers == null) headers = new HashMap<>();

            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            int versionCode = pinfo.versionCode;
            String versionName = pinfo.versionName;

            headers.put("x-mycomms-version", "android/" + versionName + "." + versionCode);
            headers.put("Content-Type", "application/json; charset=utf-8");

            Iterator headersIt = headers.keySet().iterator();
            String param;
            String value;

            while(headersIt.hasNext())
            {
                param = (String)headersIt.next();
                value = (String)headers.get(param);
                httpGet.addHeader(param, value);
            }

            httpResponse = httpclient.execute(httpGet);

        } catch (Exception ex) {
            Log.e(Constants.TAG, "APIWrapper.httpGetAPI: \n" + ex.toString());
            return null;
        }

        return httpResToHash(httpResponse);
    }

    /*
    Method to encapsulate any http post call to the API
    Params
        restRequest: String of url suffix (ex: "/api/profile")
        params: JSONObject of input parameters, to be set into body
        headers: HashMap of key-value pairs for header params
     */
    public HashMap<String,Object> httpDeleteAPI (String restRequest, HashMap<String,Object> params, HashMap<String,Object> headers, Context context) {
        Log.i(Constants.TAG, "APIWrapper.httpDeleteAPI: ");
        JSONObject json = null;

        if(params != null)
            json = new JSONObject(params);
        else json = new JSONObject();

        HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000);
        HttpResponse response;
        String textResponse = null;
        JSONObject jsonResponse = null;

        String test ="";
        try {
            test = json.getString("id");
            Log.i(Constants.TAG, "APIWrapper.httpDeleteAPI: test" + test);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(getHttpProtocol() + EndpointWrapper.getBaseURL() + restRequest+test);

            //Set version and Content-Type in Header
            if(headers == null) headers = new HashMap<>();

            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            int versionCode = pinfo.versionCode;
            String versionName = pinfo.versionName;

            headers.put("x-mycomms-version","android/"+versionName+"."+versionCode);
            headers.put("Content-Type", "application/json; charset=utf-8");

            //Set body JSON
            ByteArrayEntity entity = new ByteArrayEntity(json.toString().getBytes("UTF8"));
            httpDelete.setEntity(entity);

            Iterator headersIt = headers.keySet().iterator();
            String param;
            String value;

            while(headersIt.hasNext()) {
                param = (String)headersIt.next();
                value = (String)headers.get(param);
                httpDelete.addHeader(param, value);
            }

            response = httpClient.execute(httpDelete);

        } catch (Exception e) {
            Log.e(Constants.TAG, "APIWrapper.httpDeleteAPI: \n" + e.toString());
            return null;
        }

        return httpResToHash(response);
    }

    private static HashMap<String,Object> httpResToHash(HttpResponse response)
    {
        if(response == null) return null;
        HashMap<String, Object> hash = new HashMap<>();
        String textEntity = null;
        try {
            if(response.getEntity() != null)
                textEntity = EntityUtils.toString(response.getEntity());

            String contentType;

            if(textEntity != null && textEntity.length() > 0) {
                contentType = response.getHeaders("Content-Type")[0].getValue();

                if (contentType.compareTo("application/json") == 0) {
                    JSONObject json = new JSONObject(textEntity);
                    hash.put("json", json);
                } else {
                    hash.put("text", textEntity);
                }
            }

            hash.put("status", String.valueOf(response.getStatusLine().getStatusCode()));

        } catch(Exception ex) {
            Log.e(Constants.TAG, "APIWrapper.httpResToHash: \n" + ex.toString());
            return null;
        }

        return hash;
    }

    //Check network connection
    public static boolean isConnected(Context context){
        ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    public static boolean checkConnectionAndAlert(Context context){
        if(isConnected(context))
        {
            return true;
        }
        else
        {
            Utils.showAlert(
                    context,
                    context.getString(R.string.no_internet_connection),
                    context.getString(R.string.no_internet_connection_is_available));

            return false;
        }
    }

    class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
        public static final String METHOD_NAME = "DELETE";
        public String getMethod() { return METHOD_NAME; }

        public HttpDeleteWithBody(final String uri) {
            super();
            setURI(URI.create(uri));
        }
        public HttpDeleteWithBody(final URI uri) {
            super();
            setURI(uri);
        }
        public HttpDeleteWithBody() { super(); }
    }
}

