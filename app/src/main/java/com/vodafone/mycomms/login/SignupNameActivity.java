package com.vodafone.mycomms.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.UserProfile;
import com.vodafone.mycomms.custom.CircleImageView;
import com.vodafone.mycomms.custom.ClearableEditText;
import com.vodafone.mycomms.settings.connection.FilePushToServerController;
import com.vodafone.mycomms.util.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class SignupNameActivity extends Activity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALLERY = 2;

    CircleImageView mPhoto;
    ClearableEditText mFirstName;
    ClearableEditText mLastName;
    Bitmap photoBitmap = null;
    String photoPath = null;

    private FilePushToServerController filePushToServerController;
    private File multiPartFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_name);

        mPhoto = (CircleImageView)findViewById(R.id.ivAddPhoto);
        mFirstName = (ClearableEditText)findViewById(R.id.etSignupFirstN);
        mLastName = (ClearableEditText)findViewById(R.id.etSignupLastN);

        mFirstName.setHint(R.string.first_name);
        mFirstName.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        mLastName.setHint(R.string.last_name);
        mLastName.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent(getString(R.string.how_would_you_like_to_add_a_photo),null);
            }
        });

        //Button forward
        ImageView ivBtFwd = (ImageView)findViewById(R.id.ivBtForward);
        ivBtFwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkData()) {
                    saveData();
                    Intent in = new Intent(SignupNameActivity.this, SignupCompanyActivity.class);
                    startActivity(in);
                }
            }
        });

        //Button back
        ImageView ivBtBack = (ImageView)findViewById(R.id.ivBtBack);
        ivBtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

//        //Load data if comes from Salesforce signup
//        UserProfile.setAvatar("https://www.iconaholic.com/work/bartender-icon.png");

        if(UserProfile.getFirstName() != null) mFirstName.setText(UserProfile.getFirstName());
        if(UserProfile.getLastName() != null) mLastName.setText(UserProfile.getLastName());
//        if(UserProfile.getAvatar() != null) new LoadAvatarFromUrl().execute(UserProfile.getAvatar());

        //Force the focus of the first field and opens the keyboard
        InputMethodManager mgr = ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE));
        mgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        mFirstName.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_signup_name, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void dispatchTakePictureIntent(String title, String subtitle) {

        //Build the alert dialog to let the user choose the origin of the picture

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        if(subtitle != null) {
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.cv_title_subtitle, null);
            ((TextView) view.findViewById(R.id.tvTitle)).setText(title);
            ((TextView) view.findViewById(R.id.tvSubtitle)).setText(subtitle);
            builder.setCustomTitle(view);
        }
        else
        {
            builder.setTitle(title);
        }

        builder.setItems(R.array.add_photo_chooser, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                Intent in;

                if(which == 0)
                {
                    in = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    in.putExtra(MediaStore.EXTRA_OUTPUT, setImageUri());
                    startActivityForResult(in, REQUEST_IMAGE_CAPTURE);
                }
                else if(which == 1)
                {
                    in = new Intent();
                    in.setType("image/*");
                    in.setAction(Intent.ACTION_PICK);

                    startActivityForResult(in, REQUEST_IMAGE_GALLERY);
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        builder.create();
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            photoBitmap = decodeFile(photoPath);
            mPhoto.setImageBitmap(photoBitmap);
            mPhoto.setBorderWidth(2);
            mPhoto.setBorderColor(Color.WHITE);

            //Reset avatar from user profile
            UserProfile.setAvatar(null);
            new sendFile().execute();
        }
        else if(requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK)
        {
            Uri selectedImage = data.getData();
            photoPath = getRealPathFromURI(selectedImage);
            photoBitmap = decodeFile(photoPath);
            mPhoto.setImageBitmap(photoBitmap);
            mPhoto.setBorderWidth(2);
            mPhoto.setBorderColor(Color.WHITE);

            //Reset avatar from user profile
            UserProfile.setAvatar(null);
            new sendFile().execute();
        }
    }

    public Uri setImageUri()
    {
        // Store image in dcim
        File file = new File(Environment.getExternalStorageDirectory() + "/DCIM/", "image" + new
                Date().getTime() + ".png");
        Uri imgUri = Uri.fromFile(file);
        photoPath = file.getAbsolutePath();
        return imgUri;
    }

    public Bitmap decodeFile(String path) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, o);
//            // The new size we want to scale to
//            final int REQUIRED_SIZE = 70;
//
//            // Find the correct scale value. It should be the power of 2.
//            int scale = 1;
//            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
//                scale *= 2;
//
//            // Decode with inSampleSize
//            BitmapFactory.Options o2 = new BitmapFactory.Options();
//            o2.inSampleSize = scale;
//            return BitmapFactory.decodeFile(path, o2);
            return BitmapFactory.decodeFile(path);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private void loadAvatarFromUrl(String filepath)
    {
        //Just load the image, don't save in UserProfile, we already have the url
        if(filepath == null) return;

        Bitmap bitmapTemp = decodeFile(filepath);
        mPhoto.setImageBitmap(bitmapTemp);
        mPhoto.setBorderWidth(2);
        mPhoto.setBorderColor(Color.WHITE);
    }

    private String saveImageFromUrl(String strUrl)
    {
        //Reads and saves image from URL, and return de filepath
        String filepath = null;

        try
        {
            URL url = new URL(strUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.connect();

            String filename="downloadedFile.png";
//            File cacheDirRoot = getExternalCacheDir().getAbsoluteFile();
//            File file = new File(cacheDirRoot,filename);
//            if(!file.createNewFile())
//                return null;
            File file = new File(getCacheDir(), filename);

            FileOutputStream fileOutput = new FileOutputStream(file);
            InputStream inputStream = urlConnection.getInputStream();
            int totalSize = urlConnection.getContentLength();
            int downloadedSize = 0;
            byte[] buffer = new byte[1024];
            int bufferLength;

            while ( (bufferLength = inputStream.read(buffer)) > 0 )
            {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                Log.i("Progress:","downloadedSize:"+downloadedSize+"totalSize:"+ totalSize);
            }

            fileOutput.flush();
            fileOutput.close();

            if(downloadedSize == totalSize) filepath = file.getPath();
        }
        catch (MalformedURLException e)
        {
            Log.e(Constants.TAG, "SignupNameActivity.loadAvatarFromUrl: ", e);
            return null;
        }
        catch (IOException e)
        {
            Log.e(Constants.TAG, "SignupNameActivity.loadAvatarFromUrl: ", e);
            return null;
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "SignupNameActivity.loadAvatarFromUrl: ", e);
            return null;
        }

        Log.i("filepath:", " " + filepath) ;

        return filepath;
    }

    private boolean checkData()
    {
        boolean ok = true;

        Drawable errorIcon = getResources().getDrawable(R.drawable.ic_error_tooltip);
        errorIcon.setBounds(new Rect(0, 0,
                (int) (errorIcon.getIntrinsicWidth() * 0.5),
                (int) (errorIcon.getIntrinsicHeight() * 0.5)));

        if(mLastName.getText().toString().trim().length() <= 0)
        {
            mLastName.setError(
                    getString(R.string.enter_your_last_name_to_continue),
                    errorIcon);

            ok = false;
        }

        if(mFirstName.getText().toString().trim().length() <= 0)
        {
            mFirstName.setError(
                    getString(R.string.enter_your_first_name_to_continue),
                    errorIcon);

            ok = false;
        }

        if(photoBitmap == null && ok)
        {
//            Toast.makeText(getApplicationContext(),R.string.add_a_photo_to_continue,Toast.LENGTH_SHORT).show();
            dispatchTakePictureIntent(getString(R.string.we_need_your_picture),
                    getString(R.string.please_share_your_smile));
            ok = false;
        }

        return ok;
    }

    private void saveData ()
    {
        UserProfile.setFirstName(mFirstName.getText().toString());
        UserProfile.setLastName(mLastName.getText().toString());
        UserProfile.setPhotoBitmap(photoBitmap);
        UserProfile.setPhotoPath(photoPath);
    }

    private class sendFile extends AsyncTask<Void, Void, String> {
        private ProgressDialog pdia;



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia = new ProgressDialog(getApplicationContext());
            pdia.setMessage(getApplicationContext().getString(R.string.progress_dialog_uploading_file));
            pdia.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try
            {
                filePushToServerController =  new FilePushToServerController
                        (SignupNameActivity.this);
                multiPartFile = filePushToServerController.prepareFileToSend(multiPartFile,
                        photoBitmap, SignupNameActivity.this, Constants.MULTIPART_AVATAR, null);
                filePushToServerController.sendImageRequest(Constants.CONTACT_API_POST_AVATAR,
                        Constants.MULTIPART_AVATAR, multiPartFile);

                String response = filePushToServerController.executeRequest();
                return response;
            }
            catch (Exception e)
            {
                Log.e(Constants.TAG, "FilePushToServerController.sendFile -> doInBackground: ERROR " + e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            if(pdia.isShowing())pdia.dismiss();
            Log.d(Constants.TAG, "FilePushToServerController.sendFile: Response content: " + result);

            mPhoto.setImageBitmap(photoBitmap);

        }
    }
}
