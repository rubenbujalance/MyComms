package com.vodafone.mycomms.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.vodafone.mycomms.EndpointWrapper;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

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

            headers.put("x-mycomms-version","android/"+versionName+"."+versionCode);
            headers.put("Content-Type","application/json; charset=utf-8");

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

    public static HashMap<String,Object> httpPostAPIMock (String restRequest, HashMap<String,Object> params, HashMap<String,Object> headers, Context context)
    {
        HashMap<String,Object> result = null;
        Pattern p = Pattern.compile("/user/(.*)/password");

        if(restRequest.compareTo("/api/profile")==0)
        {

        }
        else if(restRequest.compareTo("/auth/login")==0)
        {

        }
        else if(p.matcher(restRequest).matches())
        {

        }

        return result;
    }
}

