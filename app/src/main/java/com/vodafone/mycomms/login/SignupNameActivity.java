package com.vodafone.mycomms.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.UserProfile;
import com.vodafone.mycomms.custom.CircleImageView;
import com.vodafone.mycomms.custom.RoundedImageView;

public class SignupNameActivity extends Activity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALLERY = 1;

    CircleImageView mPhoto;
    EditText mFirstName;
    EditText mLastName;
    Bitmap photoBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_name);

        mPhoto = (CircleImageView)findViewById(R.id.ivAddPhoto);
        mFirstName = (EditText)findViewById(R.id.etSignupFirstN);
        mLastName = (EditText)findViewById(R.id.etSignupLastN);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            photoBitmap = imageBitmap;

//            mPhoto.setBackgroundResource(R.drawable.roundedbutton);
//            BitmapDrawable imgDrawable = new BitmapDrawable(getResources(), imageBitmap);
            mPhoto.setImageBitmap(photoBitmap);
            mPhoto.setBorderWidth(2);
            mPhoto.setBorderColor(Color.WHITE);
        }
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
                    startActivityForResult(in, REQUEST_IMAGE_CAPTURE);
                }
                else if(which == 1)
                {
                    in = new Intent();
                    in.setType("image/*");
                    in.setAction(Intent.ACTION_GET_CONTENT);

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

    private boolean checkData()
    {
        boolean ok = true;

        Drawable errorIcon = getResources().getDrawable(R.drawable.ic_error_tooltip);
        errorIcon.setBounds(new Rect(0, 0,
                (int)(errorIcon.getIntrinsicWidth()*0.5),
                (int)(errorIcon.getIntrinsicHeight()*0.5)));

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
        UserProfile profile = ((MycommsApp)getApplication()).getUserProfile();

        profile.setFirstName(mFirstName.getText().toString());
        profile.setLastName(mLastName.getText().toString());
        profile.setPhotoBitmap(photoBitmap);
    }
}