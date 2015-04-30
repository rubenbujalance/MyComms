package com.vodafone.mycomms;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;

public class APIWrapper {

    private static final String baseURL = "int.my-comms.com";
    private static final String httpProtocol = "https://";

    public static String getBaseURL() {
        return baseURL;
    }

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
            HttpPost httpPost = new HttpPost(getHttpProtocol() + getBaseURL() + restRequest);

            /*
             * Testing Code
             */
/*
            params = new JSONObject();
            params.put("firstName", "rubenF1");
            params.put("lastName", "rubenL1");
            params.put("country", "ES");
            params.put("phone", "661350545");
            params.put("email", "ruben_bujalance@stratesys-ts.com");
            params.put("password", "123456aA");
            params.put("avatar", "");
            params.put("company", "Stratesys");
            params.put("position", "Technical Senior Consultant");
            params.put("officeLocation", "Barcelona");

            headers = new HashMap<String,String>();
//            headers.put("x-otp-pin","1234");
*/
            /*
             * END Testing Code
             */

            //Set version in Header
            if(headers == null) headers = new HashMap<>();

            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            int versionCode = pinfo.versionCode;
            String versionName = pinfo.versionName;

            headers.put("x-mycomms-version","android/"+versionName+"."+versionCode);
            headers.put("Content-Type","application/json; charset=utf-8");

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
            e.printStackTrace();
            return null;
        }

        return httpResToHash(response);
    }

    public static JSONObject httpGetAPI(String restRequest, JSONObject params, HashMap headers)
    {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(restRequest);
        JSONObject response = null;

        try {
            HttpResponse execute = client.execute(httpGet);
            InputStream content = execute.getEntity().getContent();

            BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
            String s = "";

            while ((s = buffer.readLine()) != null) {
//                response += s;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    private static HashMap<String,Object> httpResToHash(HttpResponse response)
    {
        if(response == null) return null;
        HashMap<String, Object> hash = new HashMap<>();

        try {
            String textEntity = EntityUtils.toString(response.getEntity());
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
            ex.printStackTrace();
            return null;
        }

        return hash;
    }
}

