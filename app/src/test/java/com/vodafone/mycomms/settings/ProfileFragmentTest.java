package com.vodafone.mycomms.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.connection.BaseConnection;
import com.vodafone.mycomms.constants.Constants;
import com.vodafone.mycomms.custom.CircleImageView;
import com.vodafone.mycomms.settings.connection.FilePushToServerController;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.CustomPreferencesFragmentActivity;
import com.vodafone.mycomms.util.OKHttpWrapper;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
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

import io.realm.Realm;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by str_oan on 23/09/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*", "com.vodafone.mycomms.view.tab.*"
        , "com.vodafone.mycomms.custom.*"})
@PrepareForTest({Realm.class
        , Crashlytics.class
        , EndpointWrapper.class
        , FilePushToServerController.class
        , BaseConnection.class})
public class ProfileFragmentTest
{
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    public Context mContext;
    public CustomPreferencesFragmentActivity mCustomPreferencesFragmentActivity;
    public ProfileFragment mProfileFragment;
    public MockWebServer webServer;
    public String mProfileId = "mc_5570340e7eb7c3512f2f9bf2";

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
    public void tearDown()
    {
        mContext = null;
        mProfileFragment = null;
        mCustomPreferencesFragmentActivity = null;
    }

    @Test
    public void testCorrectCreationWithEditButtonPressed_TakePictureButtonPressed_DoneButtonPressed() throws Exception {
        String serverUrl = null;
        try {
            serverUrl = startWebMockServer();
        }
        catch (Exception e)
        {
            Assert.fail();
        }

        PowerMockito.mockStatic(EndpointWrapper.class);
        PowerMockito.when(EndpointWrapper.getBaseURL()).thenReturn(serverUrl);
        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(Constants.VALID_VERSION_RESPONSE));

        PowerMockito.mockStatic(BaseConnection.class);
        PowerMockito.when(BaseConnection.isConnected(Mockito.any(Context.class))).thenReturn(true);


        setUpProfileFragment();
        EditText et_password_content = (EditText) mProfileFragment.getView().findViewById(R.id.et_password_content);
        EditText et_confirm_password_content = (EditText) mProfileFragment.getView().findViewById(R.id.et_confirm_password_content);
        et_password_content.setText("newPwd");
        et_confirm_password_content.setText("newPwd");
        Assert.assertTrue(mProfileFragment.isPasswordHasChanged);

        TextView editProfile = (TextView)mProfileFragment.getActivity().findViewById(R.id.edit_profile);
        CircleImageView opaqueFilter = (CircleImageView) mProfileFragment.getView().findViewById(R.id.opaque_filter);
        ImageView imgTakePhoto = (ImageView) mProfileFragment.getView().findViewById(R.id.img_take_photo);
        EditText et_first_name_content = (EditText) mProfileFragment.getView().findViewById(R.id.et_first_name_content);
        EditText et_last_name_content = (EditText) mProfileFragment.getView().findViewById(R.id.et_last_name_content);
        EditText et_job_title_content = (EditText) mProfileFragment.getView().findViewById(R.id.et_job_title_content);
        EditText et_company_content = (EditText) mProfileFragment.getView().findViewById(R.id.et_company_content);
        EditText et_home_content = (EditText) mProfileFragment.getView().findViewById(R.id.et_home_content);
        Assert.assertTrue(editProfile.getText().toString()
                .compareTo(mProfileFragment.getActivity().getString(R.string.profile_edit_mode_edit)) == 0);

        //Test edit button clicked
        editProfile.performClick();
        checkThreadSchedulers();
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
        checkThreadSchedulers();
        AlertDialog mDialog = (AlertDialog) ShadowDialog.getLatestDialog();
        Assert.assertNotNull(mDialog);
        Assert.assertTrue(mDialog.isShowing());

        //Test item take picture as photo
        ShadowAlertDialog shadowAlertDialog = Shadows.shadowOf(mDialog);
        shadowAlertDialog.clickOnItem(0);
        checkThreadSchedulers();
        ShadowActivity shadowActivity = Shadows.shadowOf(mProfileFragment.getActivity());
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        Assert.assertTrue(startedIntent.getAction().compareTo(MediaStore.ACTION_IMAGE_CAPTURE)==0);

        //Test item tak picture as internal image
        shadowAlertDialog = Shadows.shadowOf(mDialog);
        shadowAlertDialog.clickOnItem(1);
        checkThreadSchedulers();
        shadowActivity = Shadows.shadowOf(mProfileFragment.getActivity());
        startedIntent = shadowActivity.getNextStartedActivity();
        Assert.assertTrue(startedIntent.getAction().compareTo(Intent.ACTION_PICK) == 0);

        //Test cancel button on alert dialog clicked
        Button btnClose = mDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        btnClose.performClick();
        checkThreadSchedulers();
        Assert.assertFalse(mDialog.isShowing());

        mProfileFragment.isAvatarHasChangedAfterSelection = true;
        Thread.sleep(2000);


        PowerMockito.mockStatic(FilePushToServerController.class);
        FilePushToServerController controller = PowerMockito.mock(FilePushToServerController.class);
        PowerMockito.when(FilePushToServerController.newInstance(Mockito.any(Context.class))).thenReturn(controller);
        PowerMockito.when(controller.executeRequest()).thenReturn("{\n \"file\": \"http://mockHttp\"\n}");
        PowerMockito.when(controller.getResponseCode()).thenReturn("201");

        //Test done button clicked
        editProfile.performClick();
        Thread.sleep(2000);
        checkThreadSchedulers();
        Assert.assertTrue(editProfile.getText().toString()
                .compareTo(mProfileFragment.getActivity().getString(R.string.profile_edit_mode_edit)) == 0);

        //TODO here I should create the rest of asserts;
    }

    private void setUpProfileFragment()
    {
        Intent in = new Intent(RuntimeEnvironment.application.getApplicationContext(),
                CustomPreferencesFragmentActivity.class);
        in.putExtra("index", 1);
        mCustomPreferencesFragmentActivity = Robolectric.buildActivity(CustomPreferencesFragmentActivity.class)
                .withIntent(in)
                .create().start().resume().get();

        mProfileFragment = (ProfileFragment) mCustomPreferencesFragmentActivity
                .getSupportFragmentManager().findFragmentByTag("1");
    }

    private void checkThreadSchedulers()
    {
        if(Robolectric.getBackgroundThreadScheduler().areAnyRunnable())
            Robolectric.flushBackgroundThreadScheduler();
        if(Robolectric.getForegroundThreadScheduler().areAnyRunnable())
            Robolectric.flushForegroundThreadScheduler();
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
