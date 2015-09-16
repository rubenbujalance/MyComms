package com.vodafone.mycomms.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.UserProfile;
import com.vodafone.mycomms.custom.CircleImageView;
import com.vodafone.mycomms.custom.ClearableEditText;
import com.vodafone.mycomms.main.MainActivity;
import com.vodafone.mycomms.settings.connection.FilePushToServerController;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UncaughtExceptionHandlerController;
import com.vodafone.mycomms.util.Utils;

import java.io.File;
import java.util.Date;

public class SignupNameActivity extends MainActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALLERY = 2;

    CircleImageView mPhoto;
    ClearableEditText mFirstName;
    ClearableEditText mLastName;
    Bitmap photoBitmap = null;
    String photoPath = null;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler
                (
                        new UncaughtExceptionHandlerController(this, null)
                );
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

        if(UserProfile.getFirstName() != null) mFirstName.setText(UserProfile.getFirstName());
        if(UserProfile.getLastName() != null) mLastName.setText(UserProfile.getLastName());
//        if(UserProfile.getAvatar() != null) new LoadAvatarFromUrl().execute(UserProfile.getAvatar());

        sp = getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(Constants.FIRST_TIME_AVATAR_DELIVERY, false) ;
        editor.apply();
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

                if (which == 0) {
                    in = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    in.putExtra(MediaStore.EXTRA_OUTPUT, setImageUri());
                    startActivityForResult(in, REQUEST_IMAGE_CAPTURE);
                } else if (which == 1) {
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
        }
        else if(requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK)
        {
            Uri selectedImage = data.getData();
            photoPath = Utils.getRealPathFromUri(selectedImage, this);
            photoBitmap = decodeFile(photoPath);
            mPhoto.setImageBitmap(photoBitmap);
            mPhoto.setBorderWidth(2);
            mPhoto.setBorderColor(Color.WHITE);
        }

        if(null != photoBitmap)
        {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(Constants.FIRST_TIME_AVATAR_DELIVERY, true) ;
            editor.commit();

            FilePushToServerController filePushToServerController = new FilePushToServerController
                    (SignupNameActivity.this);
            photoBitmap = Utils.adjustBitmapAsSquare(photoBitmap);
            photoBitmap = Utils.resizeBitmapToStandardValue(photoBitmap, Constants.MAX_AVATAR_WIDTH_OR_HEIGHT);
            filePushToServerController.prepareFileToSend(photoBitmap,Constants.MULTIPART_AVATAR, null);
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

    public Bitmap decodeFile(String path)
    {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, o);
            return BitmapFactory.decodeFile(path);
        } catch (Exception e) {
            Log.e(Constants.TAG, "GroupChatActivity.decodeFile: ", e);
            Crashlytics.logException(e);
        }

        return null;
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

    @Override
    public void onStart()
    {
        super.onStart();
        MycommsApp.activityStarted();
    }

    @Override
    public void onStop()
    {
        MycommsApp.activityStopped();
        super.onStop();
    }

}
