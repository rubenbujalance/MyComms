package com.vodafone.mycomms.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.OKHttpErrorReceivedEvent;
import com.vodafone.mycomms.login.LoginSignupActivity;
import com.vodafone.mycomms.main.SplashScreenActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OKHttpWrapper {

    public static void get(String url, Context context, HttpCallback cb) {
        call("GET", url, null, cb, EndpointWrapper.getBaseURL(), context);
    }

    public static void getNews(String url, Context context, HttpCallback cb) {
        call("GET", url, null, cb, EndpointWrapper.getBaseNewsURL(), context);
    }

    public static void post(String url, Context context, HttpCallback cb, JSONObject jsonObject) {
        call("POST", url, jsonObject, cb, EndpointWrapper.getBaseURL(), context);
    }

    public static void postNews(String url, Context context,
                                HttpCallback cb, JSONObject jsonObject) {
        call("POST", url, jsonObject, cb, EndpointWrapper.getBaseNewsURL(), context);
    }

    public static void getLDAP(String url, Context context, HttpCallback cb) {
        call("GET", url, null, cb, null, context);
    }

    public static void postLDAP(String url, JSONObject jsonObject,
                                Context context, HttpCallback cb) {
        call("POST", url, jsonObject, cb, null, context);
    }

    private static void call(String method, final String url, JSONObject jsonObject,
                             final HttpCallback cb, final String endPointWrapper,
                             final Context context) {
        Log.i(Constants.TAG, "OKHttpWrapper.call: " + url);
        OkHttpClient client = new OkHttpClient();

        client.setConnectTimeout(10, TimeUnit.SECONDS);
        client.setReadTimeout(30, TimeUnit.SECONDS);
        client.setRetryOnConnectionFailure(false);

        String finalUrl;

        if(endPointWrapper==null) finalUrl = url;
        else finalUrl = "https://" + endPointWrapper + url;

        Request.Builder builder = new Request.Builder();
        builder.url(finalUrl)
                .addHeader(Constants.API_HTTP_HEADER_VERSION,
                        Utils.getHttpHeaderVersion(context))
                .addHeader(Constants.API_HTTP_HEADER_CONTENTTYPE,
                        Utils.getHttpHeaderContentType())
                .addHeader(Constants.API_HTTP_HEADER_AUTHORIZATION,
                        Utils.getHttpHeaderAuth(context))
                .addHeader(Constants.API_HTTP_HEADER_USER_AGENT,
                        Utils.getUserAgent(context));

        if(method.compareTo("POST")==0) {
            String jsonStr = "{}";
            if(jsonObject!=null) jsonStr = jsonObject.toString();

            RequestBody body = RequestBody.create(
                    MediaType.parse(Utils.getHttpHeaderContentType()), jsonStr);
            builder.post(body);
        }

        Request request = builder.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
//                Toast.makeText(context, "Error connecting", Toast.LENGTH_LONG).show();
                Log.e(Constants.TAG, "OKHttpWrapper.onFailure: error " , e);

                OKHttpErrorReceivedEvent errorEvent = new OKHttpErrorReceivedEvent();
                errorEvent.setErrorMessage("Connection Error");//TODO: Hardcoded Strings
                BusProvider.getInstance().post(errorEvent);

                cb.onFailure(null, e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if(response==null) {
                    cb.onFailure(null, null);
                    return;
                }

                int code = response.code();
                Log.i(Constants.TAG, "OKHttpWrapper.onResponse: " + url + " ("+code+")");

                Response test = response;

                if (!response.isSuccessful()) {
                    //Si es la llamada para el API version, devolver la respuesta y salir
                    if(code==400 && url.compareTo(Constants.API_VERSION)==0) {
                        cb.onFailure(response, null);
                        return;
                    }

                    OKHttpErrorReceivedEvent errorEvent = new OKHttpErrorReceivedEvent();
                    errorEvent.setErrorCode(code);
                    errorEvent.setUrl(url);
                    if (code >= 500){
                        //Backend Error
                        Log.e(Constants.TAG, "OKHttpWrapper.onResponse: error code " + code);
                        errorEvent.setErrorMessage("Error code " + code);//TODO: Hardcoded Strings

                    } else if (code >=400 && code < 500){
                        if (code == 400){
                            //New version
                            errorEvent.setErrorMessage("Download new MyComms Version. Error code " + code);//TODO: Hardcoded Strings
                            Intent in = new Intent(context, SplashScreenActivity.class);
                            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(in);
                        } else if (code == 401){
                            //Unauthorized
                            errorEvent.setErrorMessage("Unauthorized User. Error code " + code);//TODO: Hardcoded Strings
                            Intent in = new Intent(context, LoginSignupActivity.class);
                            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(in);
                        } else if (code == 403){
                            //Profile is not member of the group
                            errorEvent.setErrorMessage("Profile is not member of the group " + code);//TODO: Hardcoded Strings
                        } else {
                            errorEvent.setErrorMessage("Error code " + code);//TODO: Hardcoded Strings
                        }
                        if (errorEvent!=null)
                            BusProvider.getInstance().post(errorEvent);

                        Log.e(Constants.TAG, "OKHttpWrapper.onResponse: error code " + code);
                    }
                    cb.onFailure(response, null);
                    return;
                }

                cb.onSuccess(response);
            }
        });
    }

    public interface HttpCallback  {
        /**
         * called when the server response was not 2xx or when an exception was thrown in the process
         * @param response - in case of server error (4xx, 5xx) this contains the server response
         *                 in case of IO exception this is null
         * @param e - contains the exception. in case of server error (4xx, 5xx) this is null
         */
        public void onFailure(Response response, IOException e);

        /**
         * contains the server response
         * @param response
         */
        public void onSuccess(Response response);
    }

}
