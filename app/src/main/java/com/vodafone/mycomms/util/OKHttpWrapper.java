package com.vodafone.mycomms.util;

import android.content.Context;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.EndpointWrapper;

import java.io.IOException;

public class OKHttpWrapper {

    public static void get(String url, Context context, HttpCallback cb) {
        call("GET", url, context, cb);
    }

    public static void post(String url, Context context, HttpCallback cb) {
        call("POST", url, context, cb);
    }

    private static void call(String method, String url, Context context, final HttpCallback cb) {
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .url("https://" + EndpointWrapper.getBaseNewsURL() +
                        url)
                .addHeader(Constants.API_HTTP_HEADER_VERSION,
                        Utils.getHttpHeaderVersion(context))
                .addHeader(Constants.API_HTTP_HEADER_CONTENTTYPE,
                        Utils.getHttpHeaderContentType())
                .addHeader(Constants.API_HTTP_HEADER_AUTHORIZATION,
                        Utils.getHttpHeaderAuth(context))
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
                cb.onFailure(null, e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
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
