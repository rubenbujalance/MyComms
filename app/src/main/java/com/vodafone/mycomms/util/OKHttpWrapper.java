package com.vodafone.mycomms.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.OKHttpErrorReceivedEvent;
import com.vodafone.mycomms.login.LoginSignupActivity;
import com.vodafone.mycomms.main.SplashScreenActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OKHttpWrapper {

    public static void get(String url, Context context, HttpCallback cb) {
        call("GET", url, context, cb, EndpointWrapper.getBaseURL());
    }

    public static void getNews(String url, Context context, HttpCallback cb) {
        call("GET", url, context, cb, EndpointWrapper.getBaseNewsURL());
    }

    public static void post(String url, Context context, HttpCallback cb) {
        call("POST", url, context, cb, EndpointWrapper.getBaseURL());
    }

    public static void postNews(String url, Context context, HttpCallback cb) {
        call("POST", url, context, cb, EndpointWrapper.getBaseNewsURL());
    }

    private static void call(String method, final String url, final Context context, final HttpCallback cb, final String endPointWrapper) {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(10, TimeUnit.SECONDS);
        client.setReadTimeout(15, TimeUnit.SECONDS);
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .url("https://" + endPointWrapper +
                        url)
                .addHeader(Constants.API_HTTP_HEADER_VERSION,
                        Utils.getHttpHeaderVersion(context))
//                .addHeader(Constants.API_HTTP_HEADER_VERSION,
//                        "150")
                .addHeader(Constants.API_HTTP_HEADER_CONTENTTYPE,
                        Utils.getHttpHeaderContentType())
                .addHeader(Constants.API_HTTP_HEADER_AUTHORIZATION,
                        Utils.getHttpHeaderAuth(context))
                .addHeader(Constants.API_HTTP_HEADER_USER_AGENT,
                        Utils.getUserAgent(context))

//                .addHeader(Constants.API_HTTP_HEADER_AUTHORIZATION,
//                        "aasa")
//                .method(method, method.equals("GET") ? null : new RequestBody() {
//                    @Override
//                    public MediaType contentType() {
//                        return null;
//                    }
//
//                    @Override
//                    public void writeTo(BufferedSink sink) throws IOException {
//
//                    }
//                })
                .build();

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
                Response test = response;
                if (!response.isSuccessful()) {
                    int code = response.code();
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
