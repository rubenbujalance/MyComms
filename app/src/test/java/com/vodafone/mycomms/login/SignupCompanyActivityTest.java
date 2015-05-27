package com.vodafone.mycomms.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.UserProfile;
import com.vodafone.mycomms.custom.AutoCompleteTVSelectOnly;
import com.vodafone.mycomms.custom.CircleImageView;
import com.vodafone.mycomms.custom.ClearableEditText;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.vodafone.mycomms.constants.Constants.FIRSTNAME;
import static com.vodafone.mycomms.constants.Constants.LASTNAME;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
public class SignupCompanyActivityTest {

    SignupCompanyActivity activity;
    AutoCompleteTVSelectOnly mCompany;
    ClearableEditText mPosition;
    ClearableEditText mOfficeLoc;
    ArrayList<HashMap<String,String>> companies;

    @Before
      public void setUp() {
        activity = Robolectric.setupActivity(SignupCompanyActivity.class);
        mCompany = activity.mCompany;
        mPosition = activity.mPosition;
        mOfficeLoc = activity.mOfficeLoc;
    }

    @Test
     public void testForwardEmptyCompany() {
        ImageView ivBtFwd = (ImageView)activity.findViewById(R.id.ivBtForward);
        ivBtFwd.performClick();
        Assert.assertTrue(mCompany.getError().equals(activity.getString(R.string.select_your_company_to_continue)));
    }

    @Test
    public void testForward() {
        mCompany.setText("S");
        mCompany.callOnClick();
        mCompany.setListSelection(0);
        Shadows.shadowOf(mCompany.getDropDownBackground());
        Shadows.shadowOf(mCompany).checkedPerformClick();
        ImageView ivBtFwd = (ImageView)activity.findViewById(R.id.ivBtForward);
        ivBtFwd.performClick();
        Assert.assertTrue(mCompany.getError().equals(activity.getString(R.string.select_your_company_to_continue)));
    }

    /*@Test
        public void testForwardEmptyPictureTakeAPhoto() {
        mFirstName.setText(FIRSTNAME);
        mLastName.setText(LASTNAME);
        ivBtFwd.performClick();
        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog sAlert = Shadows.shadowOf(alert);
        View view = sAlert.getCustomTitleView();
        String title = ((TextView)view.findViewById(R.id.tvTitle)).getText().toString();
        Assert.assertTrue(title.equals(activity.getString(R.string.we_need_your_picture)));
        sAlert.clickOnItem(0);
        Assert.assertNotNull(activity.photoPath);
        ShadowActivity sActivity = Shadows.shadowOf(activity);
        Intent requestIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(activity.photoPath);
        Uri imgUri = Uri.fromFile(file);
        requestIntent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        Intent responseIntent = new Intent();
        sActivity.receiveResult(requestIntent, Activity.RESULT_OK, responseIntent);
        Assert.assertNotNull(activity.photoBitmap);
    }

    @Test
         public void testForwardEmptyPictureChooseAPhoto() {
        mFirstName.setText(FIRSTNAME);
        mLastName.setText(LASTNAME);
        ivBtFwd.performClick();
        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog sAlert = Shadows.shadowOf(alert);
        View view = sAlert.getCustomTitleView();
        String title = ((TextView)view.findViewById(R.id.tvTitle)).getText().toString();
        Assert.assertTrue(title.equals(activity.getString(R.string.we_need_your_picture)));
        sAlert.clickOnItem(1);
        ShadowActivity sActivity = Shadows.shadowOf(activity);
        Intent requestIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        requestIntent.setType("image/*");
        requestIntent.setAction(Intent.ACTION_PICK);
        Intent responseIntent = new Intent();
        File file = new File(Environment.getExternalStorageDirectory() + "/DCIM/", "image" + new Date().getTime() + ".jpg");
        Uri imgUri = Uri.fromFile(file);
        responseIntent.setData(imgUri);
        sActivity.receiveResult(requestIntent, Activity.RESULT_OK, responseIntent);
        Assert.assertNotNull(activity.photoPath);
        Assert.assertNotNull(activity.photoBitmap);
    }

    @Test
    public void testForwardToSignupCompany() {
        mFirstName.setText(FIRSTNAME);
        mLastName.setText(LASTNAME);
        ivBtFwd.performClick();
        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog sAlert = Shadows.shadowOf(alert);
        View view = sAlert.getCustomTitleView();
        String title = ((TextView)view.findViewById(R.id.tvTitle)).getText().toString();
        Assert.assertTrue(title.equals(activity.getString(R.string.we_need_your_picture)));
        sAlert.clickOnItem(1);
        ShadowActivity sActivity = Shadows.shadowOf(activity);
        Intent requestIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        requestIntent.setType("image/*");
        requestIntent.setAction(Intent.ACTION_PICK);
        Intent responseIntent = new Intent();
        File file = new File(Environment.getExternalStorageDirectory() + "/DCIM/", "image" + new Date().getTime() + ".jpg");
        Uri imgUri = Uri.fromFile(file);
        responseIntent.setData(imgUri);
        sActivity.receiveResult(requestIntent, Activity.RESULT_OK, responseIntent);
        Assert.assertNotNull(activity.photoPath);
        Assert.assertNotNull(activity.photoBitmap);
        ivBtFwd.performClick();
        Assert.assertTrue(UserProfile.getFirstName() != null);
        Shadows.shadowOf(activity).getNextStartedActivity();
        Intent expectedIntent = new Intent(activity, SignupCompanyActivity.class);
        Assert.assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().equals(expectedIntent));
    }*/

}
