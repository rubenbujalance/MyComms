package com.vodafone.mycomms;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
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
    public static JSONObject httpPostAPI (String restRequest, JSONObject params, HashMap headers)
    {
        HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000);
        HttpResponse response;
        params = new JSONObject();
        String textResponse;
        JSONObject jsonResponse = null;

        try {
            HttpPost httpPost = new HttpPost(getHttpProtocol() + getBaseURL() + restRequest);

            /*
            Testing Code
             */
            params.put("firstName", "ruben1");
            params.put("lastName", "ruben1");
            params.put("country", "ES");
            params.put("phone", "661350545");
            params.put("email", "ruben_bujalance@stratesys-ts.com");
            params.put("password", "12345678");
            params.put("avatar", "");
            params.put("company", "Stratesys");
            params.put("position", "Technical Senior Consultant");
            params.put("officeLocation", "Barcelona");

            headers = new HashMap<String,String>();
            headers.put("x-otp-pin","1234");
            /*
             * END Testing Code
             */

            ByteArrayEntity entity = new ByteArrayEntity(params.toString().getBytes("UTF8"));
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
            httpPost.setEntity(entity);
            httpPost.addHeader("host", getHttpProtocol() + getBaseURL() + restRequest);

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

            if (response != null) {
                textResponse = EntityUtils.toString(response.getEntity());
                jsonResponse = new JSONObject(textResponse);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return jsonResponse;
    }
}
