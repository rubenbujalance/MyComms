package com.vodafone.mycomms.login;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.UserProfile;
import com.vodafone.mycomms.custom.ClearableEditText;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.MockRepository;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;

import java.io.File;
import java.util.Date;

import static com.vodafone.mycomms.constants.Constants.FIRSTNAME;
import static com.vodafone.mycomms.constants.Constants.LASTNAME;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*"})
@PrepareForTest({Crashlytics.class})
public class SignupNameActivityTest {

    SignupNameActivity activity;
    ClearableEditText mFirstName;
    ClearableEditText mLastName;
    ImageView ivBtFwd;

    @Before
    public void setUp()
    {
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());

        activity = Robolectric.buildActivity(SignupNameActivity.class).create().start().resume().get();
        MockDataForTests.checkThreadSchedulers();
        mFirstName = activity.mFirstName;
        mLastName = activity.mLastName;
        ivBtFwd = (ImageView)activity.findViewById(R.id.ivBtForward);
    }

    @After
    public void tearDown() throws Exception
    {
        MockDataForTests.checkThreadSchedulers();
        Robolectric.reset();
        activity = null;
        mFirstName = null;
        mLastName = null;
        ivBtFwd = null;
        System.gc();
    }

    @BeforeClass
    public static void setUpBeforeClass()
    {
        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException (Thread thread, Throwable e)
            {
                e.printStackTrace();
            }
        });
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        Thread.currentThread().interrupt();
    }

   @Test
    public void testForwardEmptyName() {
        mFirstName.setText("");
        mLastName.setText("");
        ivBtFwd.performClick();
        MockDataForTests.checkThreadSchedulers();
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
        MockDataForTests.checkThreadSchedulers();
        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog sAlert = Shadows.shadowOf(alert);
        View view = sAlert.getCustomTitleView();
        String title = ((TextView)view.findViewById(R.id.tvTitle)).getText().toString();
        Assert.assertTrue(title.equals(activity.getString(R.string.we_need_your_picture)));
        sAlert.clickOnItem(0);
        MockDataForTests.checkThreadSchedulers();
        Assert.assertNotNull(activity.photoPath);
        ShadowActivity sActivity = Shadows.shadowOf(activity);
        Intent requestIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(activity.photoPath);
        Uri imgUri = Uri.fromFile(file);
        requestIntent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        Intent responseIntent = new Intent();
        sActivity.receiveResult(requestIntent, Activity.RESULT_OK, responseIntent);
        MockDataForTests.checkThreadSchedulers();
        Assert.assertNotNull(activity.photoBitmap);
    }

    @Test
    public void testForwardEmptyPictureChooseAPhoto() {
        mFirstName.setText(FIRSTNAME);
        mLastName.setText(LASTNAME);
        ivBtFwd.performClick();
        MockDataForTests.checkThreadSchedulers();

        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog sAlert = Shadows.shadowOf(alert);
        View view = sAlert.getCustomTitleView();
        String title = ((TextView)view.findViewById(R.id.tvTitle)).getText().toString();
        Assert.assertTrue(title.equals(activity.getString(R.string.we_need_your_picture)));
        sAlert.clickOnItem(1);
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity sActivity = Shadows.shadowOf(activity);
        Intent requestIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        requestIntent.setType("image/*");
        requestIntent.setAction(Intent.ACTION_PICK);
        Intent responseIntent = new Intent();
        File file = new File(Environment.getExternalStorageDirectory() + "/DCIM/", "image" + new Date().getTime() + ".jpg");
        Uri imgUri = Uri.fromFile(file);
        responseIntent.setData(imgUri);
        sActivity.receiveResult(requestIntent, Activity.RESULT_OK, responseIntent);
        MockDataForTests.checkThreadSchedulers();

        Assert.assertNotNull(activity.photoPath);
        Assert.assertNotNull(activity.photoBitmap);
    }

    @Test
    public void testForwardToSignupCompany() {
        mFirstName.setText(FIRSTNAME);
        mLastName.setText(LASTNAME);
        ivBtFwd.performClick();
        MockDataForTests.checkThreadSchedulers();

        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog sAlert = Shadows.shadowOf(alert);
        View view = sAlert.getCustomTitleView();
        String title = ((TextView)view.findViewById(R.id.tvTitle)).getText().toString();
        Assert.assertTrue(title.equals(activity.getString(R.string.we_need_your_picture)));
        sAlert.clickOnItem(1);
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity sActivity = Shadows.shadowOf(activity);
        Intent requestIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        requestIntent.setType("image/*");
        requestIntent.setAction(Intent.ACTION_PICK);
        Intent responseIntent = new Intent();
        File file = new File(Environment.getExternalStorageDirectory() + "/DCIM/", "image" + new Date().getTime() + ".jpg");
        Uri imgUri = Uri.fromFile(file);
        responseIntent.setData(imgUri);
        sActivity.receiveResult(requestIntent, Activity.RESULT_OK, responseIntent);
        MockDataForTests.checkThreadSchedulers();

        Assert.assertNotNull(activity.photoPath);
        Assert.assertNotNull(activity.photoBitmap);
        ivBtFwd.performClick();
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(UserProfile.getFirstName() != null);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testFinish() throws Exception {
        Activity activity = Robolectric.buildActivity(SignupNameActivity.class).create().start().resume().pause().stop().destroy().get();
        MockDataForTests.checkThreadSchedulers();
        Assert.assertTrue(activity.isDestroyed());
    }

}