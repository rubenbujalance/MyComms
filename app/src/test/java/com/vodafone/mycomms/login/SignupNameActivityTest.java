package com.vodafone.mycomms.login;

import com.vodafone.mycomms.BuildConfig;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
public class SignupNameActivityTest {
/*
    SignupNameActivity activity;
    CircleImageView mPhoto;
    ClearableEditText mFirstName;
    ClearableEditText mLastName;
    Bitmap photoBitmap = null;
    String photoPath = null;
    ImageView ivBtFwd;

    @Before
      public void setUp() {
        activity = Robolectric.setupActivity(SignupNameActivity.class);
        mFirstName = activity.mFirstName;
        mLastName = activity.mLastName;
        ivBtFwd = (ImageView)activity.findViewById(R.id.ivBtForward);
    }

    @Test
     public void testForwardEmptyName() {
        mFirstName.setText("");
        mLastName.setText("");
        ivBtFwd.performClick();
        EditText innerFirstName = (EditText)mFirstName.findViewById(R.id.clearable_edit);
        Assert.assertTrue(innerFirstName.getError().equals(activity.getString(R.string.enter_your_first_name_to_continue)));
        EditText innerLastName = (EditText)mLastName.findViewById(R.id.clearable_edit);
        Assert.assertTrue(innerLastName.getError().equals(activity.getString(R.string.enter_your_last_name_to_continue)));
    }

    @Test
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
    }
*/
}
