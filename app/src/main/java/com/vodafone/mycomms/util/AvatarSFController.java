package com.vodafone.mycomms.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.vodafone.mycomms.connection.ConnectionsQueue;

import java.io.File;
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

    public void avatarSFCallback(String responseUrl) {
        Log.e(Constants.TAG, "AvatarSFController.avatarSFCallback: ");
        try {
            if (responseUrl != null && !responseUrl.equals("")) {
                try {
                    File avatarsDir = new File(mContext.getFilesDir() + Constants.CONTACT_AVATAR_DIR);
                    //Image avatar
                    final File avatarFile = new File(mContext.getFilesDir(), Constants.CONTACT_AVATAR_DIR +
                            "avatar_"+contactId+".jpg");
                    if(!avatarsDir.exists()) avatarsDir.mkdirs();

                    final Target target = new Target() {
                        @Override
                        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                            mImageView.setImageBitmap(bitmap);
                            textView.setVisibility(View.INVISIBLE);

                            SaveAndShowImageAsyncTask task =
                                    new SaveAndShowImageAsyncTask(
                                            mImageView, avatarFile, bitmap, textView);
//                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            task.execute();
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            if(avatarFile.exists()) avatarFile.delete();
                            ConnectionsQueue.removeConnection(avatarFile.toString());
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    };

                    mImageView.setTag(target);

                    //Add this download to queue, to avoid duplicated downloads
                    ConnectionsQueue.putConnection(avatarFile.toString(), target);
                    Picasso.with(mContext)
                            .load(responseUrl)
                            .into(target);

                } catch (Exception e) {
                    Log.e(Constants.TAG, "AvatarSFController.avatarSFCallback: ", e);
                }
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "AvatarSFController.avatarSFCallback: ",e);
        }
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
                avatarSFCallback(responseUrl);
            }
        }
    }
}
