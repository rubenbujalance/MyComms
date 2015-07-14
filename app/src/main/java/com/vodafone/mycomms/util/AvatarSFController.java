package com.vodafone.mycomms.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Callback;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;

import java.io.IOException;

public class AvatarSFController {
    private Context mContext;
    ImageView mImageView;
    TextView textView;
    String contactId;

    public AvatarSFController(Context context, ImageView image, TextView textView, String contactId) {
        this.mContext = context;
        this.mImageView = image;
        this.textView = textView;
        this.contactId = contactId;
    }

    public void getSFAvatar(String imageURL){
        Log.i(Constants.TAG, "AvatarSFController.getSFAvatar: " + imageURL);
        new AvatarSFAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                (String) imageURL);
    }

    public class AvatarSFAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            Response response;
            String responseUrl = null;
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(params[0])
                        .addHeader(Constants.API_HTTP_HEADER_VERSION,
                                Utils.getHttpHeaderVersion(mContext))
                        .addHeader(Constants.API_HTTP_HEADER_CONTENTTYPE,
                                Utils.getHttpHeaderContentType())
                        .addHeader(Constants.API_HTTP_HEADER_AUTHORIZATION,
                                Utils.getHttpHeaderAuth(mContext))
                        .build();

                response = client.newCall(request).execute();
                responseUrl = response.request().httpUrl().toString();

            } catch (IOException e) {
                Log.e(Constants.TAG, "AvatarSFController.doInBackground: ",e);
            }
            return responseUrl;
        }

        @Override
        protected void onPostExecute(String responseUrl) {
            if (responseUrl!=null){
//                avatarSFCallback(responseUrl);
                MycommsApp.picasso
                        .load(responseUrl)
                        .placeholder(R.color.grey_middle)
                        .noFade()
                        .fit().centerCrop()
                        .into(mImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                textView.setVisibility(View.INVISIBLE);
                            }

                            @Override
                            public void onError() {
                                mImageView.setImageResource(R.color.grey_middle);
                                textView.setVisibility(View.VISIBLE);
                                textView.setText("XM");
                            }
                        });
            }
        }
    }
}
