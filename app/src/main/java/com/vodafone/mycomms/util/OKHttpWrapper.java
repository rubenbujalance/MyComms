package com.vodafone.mycomms.util;

import android.content.Context;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.EndpointWrapper;

import java.io.IOException;

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

    private static void call(String method, String url, final Context context, final HttpCallback cb, final String endPointWrapper) {
        OkHttpClient client = new OkHttpClient();
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
//                .addHeader(Constants.API_HTTP_HEADER_AUTHORIZATION,
//                        Utils.getHttpHeaderAuth(context))
                .addHeader(Constants.API_HTTP_HEADER_AUTHORIZATION,
                        "aasa")
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
                cb.onFailure(null, e);

                /*
                1. Sin conexion: Si no hay conexion o ha dado un timeout,
                se debe devolver un onError
                2. Error de backend (creo que es un 500): Si el backend devuelve
                un mensaje en formato HTML (buscar el string de enconding) debe devolver un onError
                3. Update de version: En caso de capturarlo, debe ir a la SplashScreen (esta ya se
                ocupara de actualizar)
                4. Unauthorized (status 4XX creo): Es porque ha expirado las credenciales.
                Se debe lanzar un renew de usuario y si da error mandarlo a la LoginSignUpActivity
                 */
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Response test = response;
                if (!response.isSuccessful()) {
                    int code = response.code();
                    if (code >= 500){
                        Log.e(Constants.TAG, "OKHttpWrapper.onResponse: error code " + code);
                    } else if (code >=400 && code < 500){
                        //New version
                        if (code == 400){
                            //New version
                            //TEST OK
                        } else if (code == 401){
                            //Unauthorized
                            //TEST OK
                        } else {

                        }
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
