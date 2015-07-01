package com.vodafone.mycomms.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.NewsImagesReceivedEvent;
import com.vodafone.mycomms.events.RecentAvatarsReceivedEvent;
import com.vodafone.mycomms.realm.RealmAvatarTransactions;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import io.realm.Realm;
import model.Contact;
import model.ContactAvatar;
import model.News;

public class DownloadImagesAsyncTask extends AsyncTask<Void, String, Void> {
    private Context mContext;
    private ArrayList<Contact> mContactArrayList;
    private ArrayList<News> mNewsArrayList;
    private Realm realm;
    private int imageType;

    //HashMap to control currently downloading avatars
    private static HashMap<String, String> downloadingAvatars = new HashMap<>();

    public DownloadImagesAsyncTask(Context context, ArrayList<Contact> contactArrayList){
        this.mContext = context;
        this.mContactArrayList = contactArrayList;
        imageType = Constants.IMAGE_TYPE_AVATAR;
    }

    public DownloadImagesAsyncTask(Context context, ArrayList<News> newsArrayList, int foo){
        this.mContext = context;
        this.mNewsArrayList = newsArrayList;
        imageType = Constants.IMAGE_TYPE_NEWS;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
//            //Mark this contact list as downloading...
//            if (imageType == Constants.IMAGE_TYPE_AVATAR) {
//                Iterator<Contact> itContact = mContactArrayList.iterator();
//                Contact c;
//
//                while (itContact.hasNext()) {
//                    c = itContact.next();
//                    downloadingAvatars.put(c.getContactId(), null);
//                }
//            }
//            //-----------------------------------------

            long init = Calendar.getInstance().getTimeInMillis();
            Log.i(Constants.TAG, "DownloadImagesAsyncTask.okHttpDownloadFile: INIT 0");
            if (imageType == Constants.IMAGE_TYPE_AVATAR) {
                Log.e(Constants.TAG, "DownloadImagesAsyncTask.doInBackground: Downloading a pool of avatars");

                realm = Realm.getInstance(mContext);
                RealmAvatarTransactions realmAvatarTransactions = new RealmAvatarTransactions(realm);
                for (int i = 0; i < mContactArrayList.size(); i++) {
                    try {
                        Contact contact = mContactArrayList.get(i);
                        //Check if avatar is currently being downloaded
                        //TODO: De momento solo descargaremos los avatares de MC porque los SF dan error
                        if (contact.getPlatform().equalsIgnoreCase("mc")) {
                            if (!downloadingAvatars.containsKey(contact.getContactId())) {
                                if (contact.getAvatar() != null && contact.getAvatar().length() != 0) {
                                    String avatarFileName = "avatar_" + contact.getContactId() + ".jpg";
                                    ContactAvatar avatar = realmAvatarTransactions.getContactAvatarByContactId(contact.getContactId());

                                    if (avatar == null || avatar.getUrl().compareTo(contact.getAvatar()) != 0) {
                                        //                            if (downloadContactAvatar(contact)) {
                                        URL url = new URL(contact.getAvatar());
                                        if (downloadImage(url, avatarFileName, Constants.CONTACT_AVATAR_DIR)) {
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
                            }
                        }
                    } catch (Exception e) {
                        Log.e(Constants.TAG, "DownloadImagesAsyncTask.doInBackground: ", e);
                    }
                }
            }else if (imageType == Constants.IMAGE_TYPE_NEWS){
                Log.e(Constants.TAG, "DownloadImagesAsyncTask.doInBackground: Downloading a pool of images");

                for (int i = 0; i < mNewsArrayList.size(); i++) {
                    try {
                        News news = mNewsArrayList.get(i);

                        if (news.getImage() != null && news.getImage().length() != 0) {
                            String newsFileName = "news_"+ mNewsArrayList.get(i).getUuid()+".jpg";

                            String dir = Constants.CONTACT_NEWS_DIR;
                            File file = new File(mContext.getFilesDir() + dir+newsFileName);
                            if (!file.exists()) {
                                URL url = new URL("https://" + EndpointWrapper.getBaseNewsURL() + mNewsArrayList.get(i).getImage());
                                downloadImage(url, newsFileName, dir);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(Constants.TAG, "DownloadImagesAsyncTask.doInBackground: ", e);
                    }
                }
            }
            Log.i(Constants.TAG, "DownloadImagesAsyncTask.okHttpDownloadFile: FINISHED AT " + ((Calendar.getInstance().getTimeInMillis()-init)/1000));
            return null;
        } catch (Exception e) {
            Log.e(Constants.TAG, "DownloadImagesAsyncTask.doInBackground: ",e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(Void nothing) {
        super.onPostExecute(nothing);
        Log.i(Constants.TAG, "DownloadImagesAsyncTask.onPostExecute: imageType" + imageType);
        if (imageType == Constants.IMAGE_TYPE_AVATAR) {
            //UNmark this contact list as downloading...
            Iterator<Contact> itContact = mContactArrayList.iterator();
            Contact c;

            while (itContact.hasNext()) {
                c = itContact.next();
                downloadingAvatars.remove(c.getContactId());
            }
            //-----------------------------------------
            BusProvider.getInstance().post(new RecentAvatarsReceivedEvent());
        } else if (imageType == Constants.IMAGE_TYPE_NEWS) {
            BusProvider.getInstance().post(new NewsImagesReceivedEvent());
        }
        if (realm!=null)
            realm.close();
    }

    private boolean downloadImage(URL url, String fileName, String dir)
    {
        try {
            File file = new File(mContext.getFilesDir() + dir);
            file.mkdirs();

            Log.i(Constants.TAG, "DownloadImagesAsyncTask.doInBackground: downloading image " + fileName + "...");
            if (!okHttpDownloadFile(String.valueOf(url), dir, fileName)) {
                File badAvatar = new File(mContext.getFilesDir() + dir, fileName);
                badAvatar.delete();
                return false;
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "DownloadImagesAsyncTask.downloadContactAvatar: ",e);
            return false;
        }

        return true;
    }

    public boolean okHttpDownloadFile(final String path, String dir, String fileName){
        OkHttpClient client = new OkHttpClient();
        try {
            Request request = new Request.Builder().url(path).build();
            Response response = client.newCall(request).execute();
            InputStream in = response.body().byteStream();
            BufferedInputStream inStream = new BufferedInputStream(in, 1024 * 5);
            File file = new File(mContext.getFilesDir() + dir, fileName);
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
