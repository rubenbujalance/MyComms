package com.vodafone.mycomms.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by str_rbm on 01/07/2015.
 */
public class SaveAndShowImageAsyncTask extends AsyncTask<Void, Void, Void> {

    Context context;
    ImageView image;
    File file;
    Bitmap bitmap;
    TextView textView;

    public SaveAndShowImageAsyncTask(ImageView image, File file, Bitmap bitmap) {
        this.image = image;
        this.file = file;
        this.bitmap = bitmap;
        this.textView = null;
    }

    public SaveAndShowImageAsyncTask(ImageView image, File file, Bitmap bitmap, TextView textView) {
        this.image = image;
        this.file = file;
        this.bitmap = bitmap;
        this.textView = textView;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.e(Constants.TAG, "SaveAndShowImageAsyncTask.doInBackground: START " + file);

        try {
            //Descarga del archivo local
            Log.e(Constants.TAG, "SaveAndShowImageAsyncTask.doInBackground (INICIO DownloadAvatar by Picasso: "
                    + file.toString());

            file.createNewFile();
            FileOutputStream ostream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, ostream);
            ostream.close();

            Log.e(Constants.TAG, "SaveAndShowImageAsyncTask.doInBackground (FIN DownloadAvatar by Picasso: "
                    + file.toString());

        } catch (Exception e) {
            Log.e(Constants.TAG, "SaveAndShowImageAsyncTask.doInBackground: ",e);
            if(file.exists()) file.delete();
        }

        Log.e(Constants.TAG, "SaveAndShowImageAsyncTask.doInBackground: END "+file);

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.e(Constants.TAG, "SaveAndShowImageAsyncTask.onPostExecute: " + file);

        //Load bitmap to ImageView
        if(textView!=null) textView.setVisibility(View.INVISIBLE);

        Picasso.with(context)
                .load(file)
                .fit().centerCrop()
                .into(image);
    }
}
