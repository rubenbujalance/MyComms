package com.vodafone.mycomms.settings;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.connection.BaseConnection;
import com.vodafone.mycomms.constants.Constants;
import com.vodafone.mycomms.custom.CircleImageView;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.EnableEditProfileEvent;
import com.vodafone.mycomms.settings.connection.FilePushToServerController;
import com.vodafone.mycomms.test.util.MockDataForTests;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.CustomPreferencesFragmentActivity;

import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.MockRepository;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowToast;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import io.realm.Realm;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by str_oan on 23/09/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 18,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*", "com.vodafone.mycomms.view.tab.*"
        , "com.vodafone.mycomms.custom.*"})
@PrepareForTest({Realm.class
        , Crashlytics.class
        , EndpointWrapper.class
        , FilePushToServerController.class
        , BaseConnection.class
        , ProfileController.class
        })
public class ProfileFragmentTest
{
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    public Context mContext;
    public CustomPreferencesFragmentActivity mCustomPreferencesFragmentActivity;
    public ProfileFragment mProfileFragment;
    public MockWebServer webServer;

    @Before
    public void setUp()
    {
        mockStatic(Realm.class);
        when(Realm.getDefaultInstance()).thenReturn(null);
        mockStatic(Crashlytics.class);
        mContext = RuntimeEnvironment.application.getApplicationContext();
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
    }

    @After
    public void tearDown() throws Exception
    {
        MockDataForTests.checkThreadSchedulers();
        if (webServer != null)
            webServer.shutdown();
        Robolectric.reset();
        mContext = null;
        mProfileFragment = null;
        mCustomPreferencesFragmentActivity = null;
        System.gc();
    }

    @BeforeClass
    public static void setUpBeforeClass()
    {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                StringWriter writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                e.printStackTrace(printWriter);
                printWriter.flush();
                System.err.println("Uncaught exception at " + this.getClass().getSimpleName() + ": \n" + writer.toString());
            }
        });
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        Thread.currentThread().interrupt();
    }

    @Test
    public void testCorrectCreationWithEditButtonPressed_TakePictureButtonPressed_DoneButtonPressed() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setUpProfileFragment();
        MockDataForTests.checkThreadSchedulers();

        EditText et_password_content = (EditText) mProfileFragment.getView().findViewById(R.id.et_password_content);
        EditText et_confirm_password_content = (EditText) mProfileFragment.getView().findViewById(R.id.et_confirm_password_content);
        EditText et_first_name_content = (EditText) mProfileFragment.getView().findViewById(R.id.et_first_name_content);
        EditText et_last_name_content = (EditText) mProfileFragment.getView().findViewById(R.id.et_last_name_content);
        EditText et_job_title_content = (EditText) mProfileFragment.getView().findViewById(R.id.et_job_title_content);
        EditText et_company_content = (EditText) mProfileFragment.getView().findViewById(R.id.et_company_content);
        EditText et_home_content = (EditText) mProfileFragment.getView().findViewById(R.id.et_home_content);
        LinearLayout layout_error_edit_profile = (LinearLayout) mProfileFragment.getView().findViewById(R.id.lay_error_edit);

        et_password_content.setText("newPwd");
        et_confirm_password_content.setText("newPwd");

        Assert.assertTrue(mProfileFragment.isPasswordHasChanged);
        Assert.assertTrue(layout_error_edit_profile.getVisibility() == View.GONE);

        TextView editProfile = (TextView)mProfileFragment.getActivity().findViewById(R.id.edit_profile);
        CircleImageView opaqueFilter = (CircleImageView) mProfileFragment.getView().findViewById(R.id.opaque_filter);
        ImageView imgTakePhoto = (ImageView) mProfileFragment.getView().findViewById(R.id.img_take_photo);
        Assert.assertTrue(editProfile.getText().toString()
                .compareTo(mProfileFragment.getActivity().getString(R.string.profile_edit_mode_edit)) == 0);

        //Test edit button clicked
        editProfile.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(editProfile.getText().toString()
                .compareTo(mProfileFragment.getActivity().getString(R.string.profile_edit_mode_done)) == 0);
        Assert.assertTrue(opaqueFilter.getVisibility() == View.VISIBLE);
        Assert.assertTrue(imgTakePhoto.getVisibility() == View.VISIBLE);
        Assert.assertTrue(et_first_name_content.isEnabled());
        Assert.assertTrue(et_last_name_content.isEnabled());
        Assert.assertTrue(et_job_title_content.isEnabled());
        Assert.assertTrue(et_company_content.isEnabled());
        Assert.assertTrue(et_home_content.isEnabled());

        //Test profile picture clicked, alert dialog is showing
        CircleImageView profilePicture = (CircleImageView) mProfileFragment.getView().findViewById(R.id.profile_picture);
        profilePicture.performClick();
        MockDataForTests.checkThreadSchedulers();

        AlertDialog mDialog = (AlertDialog) ShadowDialog.getLatestDialog();
        Assert.assertNotNull(mDialog);
        Assert.assertTrue(mDialog.isShowing());

        //Test item take picture as photo
        ShadowAlertDialog shadowAlertDialog = Shadows.shadowOf(mDialog);
        shadowAlertDialog.clickOnItem(0);
        MockDataForTests.checkThreadSchedulers();

        ShadowActivity shadowActivity = Shadows.shadowOf(mProfileFragment.getActivity());
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        Assert.assertTrue(startedIntent.getAction().compareTo(MediaStore.ACTION_IMAGE_CAPTURE)==0);

        //Test item take picture as internal image
        shadowAlertDialog = Shadows.shadowOf(mDialog);
        shadowAlertDialog.clickOnItem(1);
        MockDataForTests.checkThreadSchedulers();

        shadowActivity = Shadows.shadowOf(mProfileFragment.getActivity());
        startedIntent = shadowActivity.getNextStartedActivity();
        Assert.assertTrue(startedIntent.getAction().compareTo(Intent.ACTION_PICK) == 0);

        //Test cancel button on alert dialog clicked
        Button btnClose = mDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        btnClose.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertFalse(mDialog.isShowing());

        mProfileFragment.isAvatarHasChangedAfterSelection = true;

        PowerMockito.mockStatic(FilePushToServerController.class);
        FilePushToServerController controller = PowerMockito.mock(FilePushToServerController.class);
        PowerMockito.when(FilePushToServerController.newInstance(Mockito.any(Context.class))).thenReturn(controller);
        PowerMockito.when(controller.executeRequest()).thenReturn("{\n \"file\": \"http://mockHttp\"\n}");
        PowerMockito.when(controller.getResponseCode()).thenReturn("201");

        //Test done button clicked

        //Test wrong Email
        et_password_content.setText("mock");
        et_confirm_password_content.setText("");
        editProfile.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(editProfile.getText().toString()
                .compareTo(mProfileFragment.getActivity().getString(R.string.profile_edit_mode_done)) == 0);
        Assert.assertTrue(opaqueFilter.getVisibility() == View.VISIBLE);
        Assert.assertTrue(imgTakePhoto.getVisibility() == View.VISIBLE);
        Assert.assertTrue(et_first_name_content.isEnabled());
        Assert.assertTrue(et_last_name_content.isEnabled());
        Assert.assertTrue(et_job_title_content.isEnabled());
        Assert.assertTrue(et_company_content.isEnabled());
        Assert.assertTrue(et_home_content.isEnabled());
        Assert.assertTrue(mProfileFragment.isEditing);
        Assert.assertTrue(mProfileFragment.isUpdating);
        Assert.assertTrue(et_confirm_password_content.getCurrentTextColor() == mProfileFragment.getResources().getColor(R.color.red_action));
        Assert.assertTrue(et_password_content.getCurrentTextColor() == mProfileFragment.getResources().getColor(R.color.red_action));
        Assert.assertTrue(layout_error_edit_profile.getVisibility() == View.VISIBLE);

        //Test wrong Email 2
        et_password_content.setText("");
        et_confirm_password_content.setText("mock");
        editProfile.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(editProfile.getText().toString()
                .compareTo(mProfileFragment.getActivity().getString(R.string.profile_edit_mode_done)) == 0);
        Assert.assertTrue(opaqueFilter.getVisibility() == View.VISIBLE);
        Assert.assertTrue(imgTakePhoto.getVisibility() == View.VISIBLE);
        Assert.assertTrue(et_first_name_content.isEnabled());
        Assert.assertTrue(et_last_name_content.isEnabled());
        Assert.assertTrue(et_job_title_content.isEnabled());
        Assert.assertTrue(et_company_content.isEnabled());
        Assert.assertTrue(et_home_content.isEnabled());
        Assert.assertTrue(mProfileFragment.isEditing);
        Assert.assertTrue(mProfileFragment.isUpdating);
        Assert.assertTrue(et_confirm_password_content.getCurrentTextColor() == mProfileFragment.getResources().getColor(R.color.red_action));
        Assert.assertTrue(et_password_content.getCurrentTextColor() == mProfileFragment.getResources().getColor(R.color.red_action));
        Assert.assertTrue(layout_error_edit_profile.getVisibility() == View.VISIBLE);

        //Test wrong Email 3
        et_password_content.setText("mock");
        et_confirm_password_content.setText("mock2");
        editProfile.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(editProfile.getText().toString()
                .compareTo(mProfileFragment.getActivity().getString(R.string.profile_edit_mode_done)) == 0);
        Assert.assertTrue(opaqueFilter.getVisibility() == View.VISIBLE);
        Assert.assertTrue(imgTakePhoto.getVisibility() == View.VISIBLE);
        Assert.assertTrue(et_first_name_content.isEnabled());
        Assert.assertTrue(et_last_name_content.isEnabled());
        Assert.assertTrue(et_job_title_content.isEnabled());
        Assert.assertTrue(et_company_content.isEnabled());
        Assert.assertTrue(et_home_content.isEnabled());
        Assert.assertTrue(mProfileFragment.isEditing);
        Assert.assertTrue(mProfileFragment.isUpdating);
        Assert.assertTrue(et_confirm_password_content.getCurrentTextColor() == mProfileFragment.getResources().getColor(R.color.red_action));
        Assert.assertTrue(et_password_content.getCurrentTextColor() == mProfileFragment.getResources().getColor(R.color.red_action));
        Assert.assertTrue(layout_error_edit_profile.getVisibility() == View.VISIBLE);

        //Test wrong Email 4
        et_password_content.setText("");
        et_confirm_password_content.setText("");
        editProfile.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(editProfile.getText().toString()
                .compareTo(mProfileFragment.getActivity().getString(R.string.profile_edit_mode_done)) == 0);
        Assert.assertTrue(opaqueFilter.getVisibility() == View.VISIBLE);
        Assert.assertTrue(imgTakePhoto.getVisibility() == View.VISIBLE);
        Assert.assertTrue(et_first_name_content.isEnabled());
        Assert.assertTrue(et_last_name_content.isEnabled());
        Assert.assertTrue(et_job_title_content.isEnabled());
        Assert.assertTrue(et_company_content.isEnabled());
        Assert.assertTrue(et_home_content.isEnabled());
        Assert.assertTrue(mProfileFragment.isEditing);
        Assert.assertTrue(mProfileFragment.isUpdating);
        Assert.assertTrue(et_confirm_password_content.getCurrentTextColor() == mProfileFragment.getResources().getColor(R.color.red_action));
        Assert.assertTrue(et_password_content.getCurrentTextColor() == mProfileFragment.getResources().getColor(R.color.red_action));
        Assert.assertTrue(layout_error_edit_profile.getVisibility() == View.VISIBLE);

        //Test wrong FirstName
        et_password_content.setText("mock");
        et_confirm_password_content.setText("mock");
        et_first_name_content.setText("");
        editProfile.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(editProfile.getText().toString()
                .compareTo(mProfileFragment.getActivity().getString(R.string.profile_edit_mode_done)) == 0);
        Assert.assertTrue(opaqueFilter.getVisibility() == View.VISIBLE);
        Assert.assertTrue(imgTakePhoto.getVisibility() == View.VISIBLE);
        Assert.assertTrue(et_first_name_content.isEnabled());
        Assert.assertTrue(et_last_name_content.isEnabled());
        Assert.assertTrue(et_job_title_content.isEnabled());
        Assert.assertTrue(et_company_content.isEnabled());
        Assert.assertTrue(et_home_content.isEnabled());
        Assert.assertTrue(mProfileFragment.isEditing);
        Assert.assertTrue(mProfileFragment.isUpdating);
        Assert.assertTrue(et_first_name_content.getCurrentTextColor() == mProfileFragment.getResources().getColor(R.color.red_action));
        Assert.assertTrue(layout_error_edit_profile.getVisibility() == View.VISIBLE);

        //Test wrong LastName
        et_password_content.setText("mock");
        et_confirm_password_content.setText("mock");
        et_first_name_content.setText("mockName");
        et_last_name_content.setText("");
        editProfile.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(editProfile.getText().toString()
                .compareTo(mProfileFragment.getActivity().getString(R.string.profile_edit_mode_done)) == 0);
        Assert.assertTrue(opaqueFilter.getVisibility() == View.VISIBLE);
        Assert.assertTrue(imgTakePhoto.getVisibility() == View.VISIBLE);
        Assert.assertTrue(et_first_name_content.isEnabled());
        Assert.assertTrue(et_last_name_content.isEnabled());
        Assert.assertTrue(et_job_title_content.isEnabled());
        Assert.assertTrue(et_company_content.isEnabled());
        Assert.assertTrue(et_home_content.isEnabled());
        Assert.assertTrue(mProfileFragment.isEditing);
        Assert.assertTrue(mProfileFragment.isUpdating);
        Assert.assertTrue(et_last_name_content.getCurrentTextColor() == mProfileFragment.getResources().getColor(R.color.red_action));
        Assert.assertTrue(layout_error_edit_profile.getVisibility() == View.VISIBLE);

        //Test wrong JobTitle name
        et_password_content.setText("mock");
        et_confirm_password_content.setText("mock");
        et_first_name_content.setText("mockName");
        et_last_name_content.setText("mockLastName");
        et_job_title_content.setText("");
        editProfile.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(editProfile.getText().toString()
                .compareTo(mProfileFragment.getActivity().getString(R.string.profile_edit_mode_done)) == 0);
        Assert.assertTrue(opaqueFilter.getVisibility() == View.VISIBLE);
        Assert.assertTrue(imgTakePhoto.getVisibility() == View.VISIBLE);
        Assert.assertTrue(et_first_name_content.isEnabled());
        Assert.assertTrue(et_last_name_content.isEnabled());
        Assert.assertTrue(et_job_title_content.isEnabled());
        Assert.assertTrue(et_company_content.isEnabled());
        Assert.assertTrue(et_home_content.isEnabled());
        Assert.assertTrue(mProfileFragment.isEditing);
        Assert.assertTrue(mProfileFragment.isUpdating);
        Assert.assertTrue(et_job_title_content.getCurrentTextColor() == mProfileFragment.getResources().getColor(R.color.red_action));
        Assert.assertTrue(layout_error_edit_profile.getVisibility() == View.VISIBLE);

        //Test wrong Company name
        et_password_content.setText("mock");
        et_confirm_password_content.setText("mock");
        et_first_name_content.setText("mockName");
        et_last_name_content.setText("mockLastName");
        et_job_title_content.setText("mockJobTitle");
        et_company_content.setText("");
        editProfile.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(editProfile.getText().toString()
                .compareTo(mProfileFragment.getActivity().getString(R.string.profile_edit_mode_done)) == 0);
        Assert.assertTrue(opaqueFilter.getVisibility() == View.VISIBLE);
        Assert.assertTrue(imgTakePhoto.getVisibility() == View.VISIBLE);
        Assert.assertTrue(et_first_name_content.isEnabled());
        Assert.assertTrue(et_last_name_content.isEnabled());
        Assert.assertTrue(et_job_title_content.isEnabled());
        Assert.assertTrue(et_company_content.isEnabled());
        Assert.assertTrue(et_home_content.isEnabled());
        Assert.assertTrue(mProfileFragment.isEditing);
        Assert.assertTrue(mProfileFragment.isUpdating);
        Assert.assertTrue(et_company_content.getCurrentTextColor() == mProfileFragment.getResources().getColor(R.color.red_action));
        Assert.assertTrue(layout_error_edit_profile.getVisibility() == View.VISIBLE);

        //Test wrong Home name
        et_password_content.setText("mock");
        et_confirm_password_content.setText("mock");
        et_first_name_content.setText("mockName");
        et_last_name_content.setText("mockLastName");
        et_job_title_content.setText("mockJobTitle");
        et_company_content.setText("mockCompany");
        et_home_content.setText("");
        editProfile.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(editProfile.getText().toString()
                .compareTo(mProfileFragment.getActivity().getString(R.string.profile_edit_mode_done)) == 0);
        Assert.assertTrue(opaqueFilter.getVisibility() == View.VISIBLE);
        Assert.assertTrue(imgTakePhoto.getVisibility() == View.VISIBLE);
        Assert.assertTrue(et_first_name_content.isEnabled());
        Assert.assertTrue(et_last_name_content.isEnabled());
        Assert.assertTrue(et_job_title_content.isEnabled());
        Assert.assertTrue(et_company_content.isEnabled());
        Assert.assertTrue(et_home_content.isEnabled());
        Assert.assertTrue(mProfileFragment.isEditing);
        Assert.assertTrue(mProfileFragment.isUpdating);
        Assert.assertTrue(et_home_content.getCurrentTextColor() == mProfileFragment.getResources().getColor(R.color.red_action));
        Assert.assertTrue(layout_error_edit_profile.getVisibility() == View.VISIBLE);

        //Test OK
        et_password_content.setText("mock");
        et_confirm_password_content.setText("mock");
        et_first_name_content.setText("mockName");
        et_last_name_content.setText("mockLastName");
        et_job_title_content.setText("mockJobTitle");
        et_company_content.setText("mockCompany");
        et_home_content.setText("MockHome");
        editProfile.performClick();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(editProfile.getText().toString()
                .compareTo(mProfileFragment.getActivity().getString(R.string.profile_edit_mode_edit)) == 0);
        Assert.assertTrue(opaqueFilter.getVisibility() == View.GONE);
        Assert.assertTrue(imgTakePhoto.getVisibility() == View.GONE);
        Assert.assertFalse(et_first_name_content.isEnabled());
        Assert.assertFalse(et_last_name_content.isEnabled());
        Assert.assertFalse(et_job_title_content.isEnabled());
        Assert.assertFalse(et_company_content.isEnabled());
        Assert.assertFalse(et_home_content.isEnabled());
        Assert.assertFalse(mProfileFragment.isAvatarHasChangedAfterSelection);
        Assert.assertTrue(layout_error_edit_profile.getVisibility() == View.GONE);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testLifeCycle()
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        String serverUrl = null;
        try {
            serverUrl = startWebMockServer();
        }
        catch (Exception e)
        {
            Assert.fail();
        }

        PowerMockito.mockStatic(ProfileController.class);
        PowerMockito.mockStatic(EndpointWrapper.class);
        PowerMockito.mockStatic(BaseConnection.class);
        PowerMockito.when(ProfileController.mapUserProfile(Mockito.any(JSONObject.class))).thenReturn(MockDataForTests.getMockUserProfile());
        PowerMockito.when(ProfileController
                .isUserProfileChanged(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        PowerMockito.when(EndpointWrapper.getBaseURL()).thenReturn(serverUrl);
        PowerMockito.when(BaseConnection.isConnected(Mockito.any(Context.class))).thenReturn(true);
        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(Constants.VALID_VERSION_RESPONSE));

        Intent in = new Intent(RuntimeEnvironment.application.getApplicationContext(),
                CustomPreferencesFragmentActivity.class);
        in.putExtra("index", 1);
        mCustomPreferencesFragmentActivity = Robolectric.buildActivity(CustomPreferencesFragmentActivity.class)
                .withIntent(in)
                .create().start().resume().pause().stop().destroy().get();

        mProfileFragment = (ProfileFragment) mCustomPreferencesFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("1");
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(mCustomPreferencesFragmentActivity.isDestroyed());

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testOnProfile() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setUpProfileFragment();
        MockDataForTests.checkThreadSchedulers();

        this.mProfileFragment.onProfileConnectionError();
        MockDataForTests.checkThreadSchedulers();
        Assert.assertFalse(mProfileFragment.isUpdating);

        this.mProfileFragment.onUpdateProfileConnectionError();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(ShadowToast.getTextOfLatestToast()
                .equals(this.mProfileFragment.getResources().getString(R.string.wrong_profile_update)));
        Assert.assertFalse(mProfileFragment.isUpdating);

        this.mProfileFragment.onUpdateProfileConnectionCompleted();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertFalse(mProfileFragment.isUpdating);

        this.mProfileFragment.onPasswordChangeError("error");
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(ShadowToast.getTextOfLatestToast().equals("error"));

        this.mProfileFragment.onPasswordChangeCompleted();
        MockDataForTests.checkThreadSchedulers();

        this.mProfileFragment.onConnectionNotAvailable();
        MockDataForTests.checkThreadSchedulers();

        Assert.assertFalse(mProfileFragment.isUpdating);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

    }

    @Test
    public void testEnableEditProfileEvent_True() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setUpProfileFragment();
        MockDataForTests.checkThreadSchedulers();

        EnableEditProfileEvent event = new EnableEditProfileEvent();
        event.setMessage(true);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();

        TextView editProfile = (TextView) mProfileFragment.getActivity().findViewById(R.id.edit_profile);
        Assert.assertTrue(editProfile.getVisibility() == View.VISIBLE);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testEnableEditProfileEvent_False() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        setUpProfileFragment();
        MockDataForTests.checkThreadSchedulers();

        EnableEditProfileEvent event = new EnableEditProfileEvent();
        event.setMessage(false);
        BusProvider.getInstance().post(event);
        MockDataForTests.checkThreadSchedulers();

        TextView editProfile = (TextView) mProfileFragment.getActivity().findViewById(R.id.edit_profile);
        Assert.assertTrue(editProfile.getVisibility() == View.GONE);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Test
    public void testOnActivityResult() throws Exception
    {
        MockDataForTests.printStartTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());

        File file = new File(Environment.getExternalStorageDirectory() + "/DCIM/", "image" + new
                Date().getTime() + ".png");
        Uri imgUri = Uri.fromFile(file);
        Intent in = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        in.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);

        setUpProfileFragment();
        MockDataForTests.checkThreadSchedulers();

        this.mProfileFragment.onActivityResult(1, Activity.RESULT_OK, in);
        MockDataForTests.checkThreadSchedulers();

        TextView editProfile = (TextView) mProfileFragment.getActivity().findViewById(R.id.edit_profile);
        CircleImageView profilePicture = (CircleImageView) mProfileFragment.getView().findViewById(R.id.profile_picture);
        TextView textAvatar = (TextView) mProfileFragment.getView().findViewById(R.id.avatarText);

        Assert.assertTrue(mProfileFragment.isAvatarHasChangedAfterSelection);
        Assert.assertTrue(editProfile.isClickable());
        Assert.assertTrue(profilePicture.getBorderWidth() == 2);
        Assert.assertTrue(profilePicture.getBorderColor() == Color.WHITE);
        Assert.assertTrue(textAvatar.getText().toString().length() == 0);

        this.mProfileFragment.onActivityResult(2, Activity.RESULT_OK, in);
        MockDataForTests.checkThreadSchedulers();

        Assert.assertTrue(mProfileFragment.isAvatarHasChangedAfterSelection);

        this.mProfileFragment.onActivityResult(0, Activity.RESULT_CANCELED, in);
        MockDataForTests.checkThreadSchedulers();

        Assert.assertFalse(mProfileFragment.isAvatarHasChangedAfterSelection);

        MockDataForTests.printEndTest(this.getClass().getSimpleName()
                , Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    private void setUpProfileFragment() throws Exception
    {
        String serverUrl = null;
        try {
            serverUrl = startWebMockServer();
        }
        catch (Exception e)
        {
            Assert.fail();
        }
        PowerMockito.mockStatic(ProfileController.class);
        PowerMockito.mockStatic(EndpointWrapper.class);
        PowerMockito.mockStatic(BaseConnection.class);
        PowerMockito.when(ProfileController.mapUserProfile(Mockito.any(JSONObject.class))).thenReturn(MockDataForTests.getMockUserProfile());
        PowerMockito.when(ProfileController
                .isUserProfileChanged(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        PowerMockito.when(EndpointWrapper.getBaseURL()).thenReturn(serverUrl);
        PowerMockito.when(BaseConnection.isConnected(Mockito.any(Context.class))).thenReturn(true);
        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(Constants.VALID_VERSION_RESPONSE));

        Intent in = new Intent(RuntimeEnvironment.application.getApplicationContext(),
                CustomPreferencesFragmentActivity.class);
        in.putExtra("index", 1);
        mCustomPreferencesFragmentActivity = Robolectric.buildActivity(CustomPreferencesFragmentActivity.class)
                .withIntent(in)
                .create().start().resume().get();

        mProfileFragment = (ProfileFragment) mCustomPreferencesFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("1");
    }

    private String startWebMockServer() throws Exception {
        //OkHttp mocked web server
        webServer = new MockWebServer();
        webServer.useHttps(null, false);

        //Connect OkHttp calls with MockWebServer
        webServer.start();
        String serverUrl = webServer.getUrl("").toString();

        return serverUrl;
    }

}
