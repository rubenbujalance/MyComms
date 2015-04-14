package com.vodafone.mycomms.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.UserProfile;

public class SignupNameActivity extends Activity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALLERY = 1;

    ImageView mPhoto;
    EditText mFirstName;
    EditText mLastName;
    Bitmap photoBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_name);

        mPhoto = (ImageView)findViewById(R.id.ivAddPhoto);
        mFirstName = (EditText)findViewById(R.id.etSignupFirstN);
        mLastName = (EditText)findViewById(R.id.etSignupLastN);

        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mFirstName.getWindowToken(), 0);
                dispatchTakePictureIntent();
            }
        });

        //Button forward
        ImageView ivBtFwd = (ImageView)findViewById(R.id.ivBtForward);
        ivBtFwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkData()) {
                    saveData();
                    Intent in = new Intent(SignupNameActivity.this, SignupPassActivity.class);
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
        mFirstName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager mgr = ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE));
//                mgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                mgr.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
            }
        });

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

            mPhoto.setBackgroundResource(R.drawable.roundedbutton);
            BitmapDrawable imgDrawable = new BitmapDrawable(getResources(), imageBitmap);
            mPhoto.setImageDrawable(imgDrawable);
        }
    }

    private void dispatchTakePictureIntent() {

        //Build the alert dialog to let the user choose the origin of the picture

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.how_would_you_like_to_add_a_photo);

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

        if(photoBitmap == null)
        {
            Toast.makeText(getApplicationContext(),R.string.add_a_photo_to_continue,Toast.LENGTH_SHORT).show();
            ok = false;
        }

        if(mLastName.getText().toString().trim().length() <= 0)
        {
            mLastName.setError(
                    getString(R.string.enter_your_last_name_to_continue));

            ok = false;
        }

        if(mFirstName.getText().toString().trim().length() <= 0)
        {
            mFirstName.setError(
                    getString(R.string.enter_your_first_name_to_continue),
                    getResources().getDrawable(R.drawable.ic_error_tooltip));

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
