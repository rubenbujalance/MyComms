package com.vodafone.mycomms.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.RecentContactsReceivedEvent;
import com.vodafone.mycomms.realm.RealmAvatarTransactions;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

import io.realm.Realm;
import model.Contact;
import model.ContactAvatar;

public class DownloadImagesAsyncTask extends AsyncTask<Void, String, Void> {
    private Context mContext;
    private ArrayList<Contact> mContactArrayList;
    private Realm realm;

    public DownloadImagesAsyncTask(Context context, ArrayList<Contact> contactArrayList){
        this.mContext = context;
        this.mContactArrayList = contactArrayList;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.e(Constants.TAG, "DownloadImagesAsyncTask.doInBackground: Downloading a pool of avatars");
        realm = Realm.getInstance(mContext);
        RealmAvatarTransactions realmAvatarTransactions = new RealmAvatarTransactions(realm);
        long init = Calendar.getInstance().getTimeInMillis();
        Log.i(Constants.TAG, "DownloadImagesAsyncTask.okHttpDownloadFile: INIT 0" );
        for(int i=0; i<mContactArrayList.size(); i++) {
            try {
                Contact contact = mContactArrayList.get(i);

                if (contact.getAvatar() != null && contact.getAvatar().length() != 0) {
                    String avatarFileName = "avatar_" + contact.getContactId() + ".jpg";
                    ContactAvatar avatar = realmAvatarTransactions.getContactAvatarByContactId(contact.getContactId());

                    if (avatar == null || avatar.getUrl().compareTo(contact.getAvatar()) != 0) {
                        if(downloadContactAvatar(contact)) {
                            if (avatar == null) {
                                avatar = new ContactAvatar(contact.getContactId(), contact.getAvatar(), avatarFileName);
                            } else {
                                realm.beginTransaction();
                                avatar.setUrl(contact.getAvatar());
                                realm.commitTransaction();
                            }
                            realmAvatarTransactions.insertAvatar(avatar);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(Constants.TAG, "DownloadImagesAsyncTask.doInBackground: ", e);
            }
        }
        Log.i(Constants.TAG, "DownloadImagesAsyncTask.okHttpDownloadFile: FINISHED AT " + ((Calendar.getInstance().getTimeInMillis()-init)/1000));
        return null;
    }

    @Override
    protected void onPostExecute(Void nothing) {
        super.onPostExecute(nothing);
        BusProvider.getInstance().post(new RecentContactsReceivedEvent());
        realm.close();
    }

    private boolean downloadContactAvatar(Contact contact)
    {
        try {
            URL url = new URL(contact.getAvatar());
            String avatarFileName = "avatar_" + contact.getContactId() + ".jpg";
            String dir = Constants.CONTACT_AVATAR_DIR;

            File file = new File(mContext.getFilesDir() + dir);
            file.mkdirs();

            Log.i(Constants.TAG, "DownloadImagesAsyncTask.doInBackground: downloading avatar " + avatarFileName + "...");
            //if (!downloadFile(String.valueOf(url), dir, avatarFileName)) {
            if (!okHttpDownloadFile(String.valueOf(url), dir, avatarFileName)) {
                File badAvatar = new File(mContext.getFilesDir() + dir, avatarFileName);
                badAvatar.delete();
                return false;
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "DownloadImagesAsyncTask.downloadContactAvatar: ",e);
            return false;
        }

        return true;
    }

    public boolean okHttpDownloadFile(final String path, String dir, String avatarFileName){
        OkHttpClient client = new OkHttpClient();
        try {
            Request request = new Request.Builder().url(path).build();
            Response response = client.newCall(request).execute();
            InputStream in = response.body().byteStream();
            BufferedInputStream inStream = new BufferedInputStream(in, 1024 * 5);
            File file = new File(mContext.getFilesDir() + dir, avatarFileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream outStream = new FileOutputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(inStream);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outStream);
            outStream.flush();
            outStream.close();
            inStream.close();
        } catch (IOException e){
            Log.e(Constants.TAG, "DownloadImagesAsyncTask.okHttpDownloadFile: ",e);
            return false;
        }
        return true;
    }
}
