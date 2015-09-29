package com.vodafone.mycomms.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.connection.BaseConnection;
import com.vodafone.mycomms.custom.CircleImageView;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.EnableEditProfileEvent;
import com.vodafone.mycomms.settings.connection.FilePushToServerController;
import com.vodafone.mycomms.settings.connection.IProfileConnectionCallback;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import io.realm.Realm;
import model.UserProfile;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class ProfileFragment extends Fragment implements IProfileConnectionCallback{

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private int mIndex;

    public boolean isEditing = false;

    private ProfileController profileController;
    public boolean isUpdating = false;

    private CircleImageView profilePicture, opaqueFilter;
    private ImageView imgTakePhoto;
    private TextView textAvatar;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;

    public String photoPath = null;
    private Bitmap photoBitmap = null;

    public FilePushToServerController filePushToServerController;

    private UserProfile userProfile;
    private String profileId;
    private boolean isFirstLoadNeed = true;
    private String avatarNewURL = null;
    private SharedPreferences sp;

    private LinearLayout layout_error_edit_profile;
    private TextView tv_error_on_edit;

    private Realm realm;
    private TextView editProfile;

    private TextView
            tv_password_indicator, tv_confirm_password_indicator,
            tv_first_name_indicator, tv_last_name_indicator, tv_job_title_indicator,
            tv_company_indicator, tv_home_indicator;

    private EditText
            et_password_content, et_confirm_password_content, et_first_name_content,
            et_last_name_content, et_job_title_content, et_company_content, et_home_content;


    public boolean
            isAvatarHasChangedAfterSelection = false
            , isProfileLoadedAtLeastOnce = false
            , isPasswordHasChanged = false;

    private ProgressBar progressBar;

    public static ProfileFragment newInstance(int index, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, index);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

   @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
   {
       BusProvider.getInstance().register(this);
       Log.i(Constants.TAG, "ProfileFragment.onCreateView: ");
       View v = inflater.inflate(R.layout.layout_set_profile, container, false);
       initSpinners();
       createViewsAndEvents(v);
       profileController = new ProfileController(getActivity());
       profileController.setConnectionCallback(this);
       return v;
   }

    private void createViewsAndEvents(View v)
    {
        //getActivity() in this case because takes view from ToolBarActivity
        editProfile = (TextView) getActivity().findViewById(R.id.edit_profile);
        editProfile.setVisibility(View.INVISIBLE);

        profilePicture = (CircleImageView) v.findViewById(R.id.profile_picture);
        opaqueFilter = (CircleImageView) v.findViewById(R.id.opaque_filter);
        textAvatar = (TextView) v.findViewById(R.id.avatarText);
        imgTakePhoto = (ImageView) v.findViewById(R.id.img_take_photo);
        imgTakePhoto.setVisibility(View.GONE);

        layout_error_edit_profile = (LinearLayout) v.findViewById(R.id.lay_error_edit);
        tv_error_on_edit = (TextView) v.findViewById(R.id.tv_error_on_edit);
        progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);

        tv_password_indicator = (TextView) v.findViewById(R.id.tv_password_indicator);
        et_password_content = (EditText) v.findViewById(R.id.et_password_content);
        et_password_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isPasswordHasChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        tv_confirm_password_indicator = (TextView) v.findViewById(R.id.tv_confirm_password_indicator);
        et_confirm_password_content = (EditText) v.findViewById(R.id.et_confirm_password_content);
        et_confirm_password_content.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                isPasswordHasChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        tv_first_name_indicator = (TextView) v.findViewById(R.id.tv_first_name_indicator);
        tv_last_name_indicator = (TextView) v.findViewById(R.id.tv_last_name_indicator);
        tv_job_title_indicator = (TextView) v.findViewById(R.id.tv_job_title_indicator);
        tv_company_indicator = (TextView) v.findViewById(R.id.tv_company_indicator);
        tv_home_indicator = (TextView) v.findViewById(R.id.tv_home_indicator);

        et_first_name_content = (EditText) v.findViewById(R.id.et_first_name_content);
        et_last_name_content = (EditText) v.findViewById(R.id.et_last_name_content);
        et_job_title_content = (EditText) v.findViewById(R.id.et_job_title_content);
        et_company_content = (EditText) v.findViewById(R.id.et_company_content);
        et_home_content = (EditText) v.findViewById(R.id.et_home_content);

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Log.i(Constants.TAG, "ProfileFragment.onClick: editProfile, isEditing= " + isEditing + "isUpdating=" + isUpdating);
                if (isUpdating && !isEditing)
                    return;

                if (isEditing)
                    new UpdateProfile().execute();
                else
                {
                    profileEditMode(true);
                    isEditing = !isEditing;
                }

            }
        });

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditing)
                    dispatchTakePictureIntent(getString(R.string.how_would_you_like_to_add_a_photo));
            }
        });

    }

    private boolean updateContactData() {
        Log.d(Constants.TAG, "ProfileFragment.updateContactData: ");
        if (!BaseConnection.isConnected(this.getActivity())) {
            profileController.showToast("Not connected. Can't save details.");
            return false;
        }

        if (isShowErrorOnEditProfile())
        {
            profileEditMode(true);
            isEditing = true;
            isUpdating = true;
            return true;
        }
        else
        {
            profileEditMode(false);
            isEditing = !isEditing;
            //Update password if needed
            if(isPasswordHasChanged)
            {
                String password = et_password_content.getText().toString();
                HashMap<String, String> newPasswordHashMap = new HashMap<>();
                newPasswordHashMap.put("password", password);
                profileController.updatePassword(newPasswordHashMap);
            }
        }

        String firstName = et_first_name_content.getText().toString();
        String lastName = et_last_name_content.getText().toString();

        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Constants.PROFILE_FULLNAME_SHARED_PREF, firstName + " " + lastName);
        editor.apply();

        String company = et_company_content.getText().toString();
        String position = et_job_title_content.getText().toString();
        String officeLocation = et_home_content.getText().toString();


        if(null != avatarNewURL)
        {
            profileController.updateUserAvatarInDB(avatarNewURL);
            avatarNewURL = null;
        }

        if(!ProfileController.isUserProfileChanged(firstName, lastName, company, position,
                officeLocation))
        {
            Log.d(Constants.TAG, "ProfileFragment.updateContactData: profile details not changed");
            return false;
        }

        UserProfile newProfile = new UserProfile();
        newProfile.setFirstName(firstName);
        newProfile.setLastName(lastName);
        newProfile.setCompany(company);
        newProfile.setPosition(position);
        newProfile.setOfficeLocation(officeLocation);

        Log.d(Constants.TAG, "ProfileFragment.updateContactData:" + ProfileController.printUserProfile(newProfile));

        profileController.updateUserProfileInDB(firstName, lastName, company, position,
                officeLocation);

        HashMap newProfileHashMap = profileController.getProfileHashMap(newProfile);

        boolean isValid  = Utils.validateStringHashMap(newProfileHashMap);
        if(!isValid) {
            profileController.showToast("Info not valid");
            return false;
        }

        profileController.updateContactData(newProfileHashMap);
        return  true;
    }

    private boolean isShowErrorOnEditProfile() {
        String password = et_password_content.getText().toString();
        String repeatPassword = et_confirm_password_content.getText().toString();
        String firstName = et_first_name_content.getText().toString();
        String lastName = et_last_name_content.getText().toString();
        String job = et_job_title_content.getText().toString();
        String company = et_company_content.getText().toString();
        String home = et_home_content.getText().toString();
        layout_error_edit_profile.setVisibility(View.GONE);

        return
                !isPasswordCorrect(password, repeatPassword)
                || !isFirstNameCorrect(firstName)
                || !isLastNameCorrect(lastName)
                || !isJobTitleNameCorrect(job)
                || !isCompanyCorrect(company)
                || !isHomeCorrect(home);
    }

    private boolean isHomeCorrect(String home)
    {
        if(home != null && home.length() > 0)
        {
            layout_error_edit_profile.setVisibility(View.GONE);
            tv_home_indicator.setTextColor(getResources().getColor(R.color.contact_soft_grey));
            et_home_content.setTextColor(getResources().getColor(R.color.ac_subtitle));
            return true;
        }
        else
        {
            tv_error_on_edit.setText(getResources().getString(R.string.error_home_not_null));
            layout_error_edit_profile.setVisibility(View.VISIBLE);
            tv_home_indicator.setTextColor(getResources().getColor(R.color.red_action));
            et_home_content.setTextColor(getResources().getColor(R.color.red_action));
            return false;
        }
    }

    private boolean isCompanyCorrect(String company)
    {
        if(company != null && company.length() > 0)
        {
            layout_error_edit_profile.setVisibility(View.GONE);
            tv_company_indicator.setTextColor(getResources().getColor(R.color.contact_soft_grey));
            et_company_content.setTextColor(getResources().getColor(R.color.ac_subtitle));
            return true;
        }
        else
        {
            tv_error_on_edit.setText(getResources().getString(R.string.error_company_not_null));
            layout_error_edit_profile.setVisibility(View.VISIBLE);
            tv_company_indicator.setTextColor(getResources().getColor(R.color.red_action));
            et_company_content.setTextColor(getResources().getColor(R.color.red_action));
            return false;
        }
    }

    private boolean isJobTitleNameCorrect(String job)
    {
        if(job != null && job.length() > 0)
        {
            layout_error_edit_profile.setVisibility(View.GONE);
            tv_job_title_indicator.setTextColor(getResources().getColor(R.color.contact_soft_grey));
            et_job_title_content.setTextColor(getResources().getColor(R.color.ac_subtitle));
            return true;
        }
        else
        {
            tv_error_on_edit.setText(getResources().getString(R.string.error_job_title_not_null));
            layout_error_edit_profile.setVisibility(View.VISIBLE);
            tv_job_title_indicator.setTextColor(getResources().getColor(R.color.red_action));
            et_job_title_content.setTextColor(getResources().getColor(R.color.red_action));
            return false;
        }
    }

    private boolean isLastNameCorrect(String secondName)
    {
        if(secondName != null && secondName.length() > 0)
        {
            layout_error_edit_profile.setVisibility(View.GONE);
            tv_last_name_indicator.setTextColor(getResources().getColor(R.color.contact_soft_grey));
            et_last_name_content.setTextColor(getResources().getColor(R.color.ac_subtitle));
            return true;
        }
        else
        {
            tv_error_on_edit.setText(getResources().getString(R.string.error_last_name_not_null));
            layout_error_edit_profile.setVisibility(View.VISIBLE);
            tv_last_name_indicator.setTextColor(getResources().getColor(R.color.red_action));
            et_last_name_content.setTextColor(getResources().getColor(R.color.red_action));
            return false;
        }
    }

    private boolean isFirstNameCorrect(String firstName)
    {
        if(firstName != null && firstName.length() > 0)
        {
            layout_error_edit_profile.setVisibility(View.GONE);
            tv_first_name_indicator.setTextColor(getResources().getColor(R.color.contact_soft_grey));
            et_first_name_content.setTextColor(getResources().getColor(R.color.ac_subtitle));
            return true;
        }
        else
        {
            tv_error_on_edit.setText(getResources().getString(R.string.error_first_name_not_null));
            layout_error_edit_profile.setVisibility(View.VISIBLE);
            tv_first_name_indicator.setTextColor(getResources().getColor(R.color.red_action));
            et_first_name_content.setTextColor(getResources().getColor(R.color.red_action));
            return false;
        }
    }

    private boolean isPasswordCorrect(String password, String repeatPassword)
    {
        if((password != null && password.length() > 0) && (repeatPassword == null || repeatPassword.length() == 0))
        {
            tv_error_on_edit.setText(getResources().getString(R.string.error_password_do_not_match));
            layout_error_edit_profile.setVisibility(View.VISIBLE);
            tv_password_indicator.setTextColor(getResources().getColor(R.color.red_action));
            et_password_content.setTextColor(getResources().getColor(R.color.red_action));
            tv_confirm_password_indicator.setTextColor(getResources().getColor(R.color.red_action));
            et_confirm_password_content.setTextColor(getResources().getColor(R.color.red_action));
            return false;
        }
        else if ((repeatPassword != null && repeatPassword.length() > 0) && (password == null || password.length() == 0))
        {
            tv_error_on_edit.setText(getResources().getString(R.string.error_password_do_not_match));
            layout_error_edit_profile.setVisibility(View.VISIBLE);
            tv_password_indicator.setTextColor(getResources().getColor(R.color.red_action));
            et_password_content.setTextColor(getResources().getColor(R.color.red_action));
            tv_confirm_password_indicator.setTextColor(getResources().getColor(R.color.red_action));
            et_confirm_password_content.setTextColor(getResources().getColor(R.color.red_action));
            return false;
        }
        else if( password != null && password.length() > 0 && repeatPassword != null && repeatPassword.length() > 0 && !password.equals(repeatPassword))
        {
            tv_error_on_edit.setText(getResources().getString(R.string.error_password_do_not_match));
            layout_error_edit_profile.setVisibility(View.VISIBLE);
            tv_password_indicator.setTextColor(getResources().getColor(R.color.red_action));
            et_password_content.setTextColor(getResources().getColor(R.color.red_action));
            tv_confirm_password_indicator.setTextColor(getResources().getColor(R.color.red_action));
            et_confirm_password_content.setTextColor(getResources().getColor(R.color.red_action));
            return false;
        }
        else if(password != null && password.length() > 0 && repeatPassword != null && repeatPassword.length() > 0  && password.equals(repeatPassword) )
        {
            layout_error_edit_profile.setVisibility(View.GONE);
            tv_password_indicator.setTextColor(getResources().getColor(R.color.contact_soft_grey));
            et_password_content.setTextColor(getResources().getColor(R.color.ac_subtitle));
            tv_confirm_password_indicator.setTextColor(getResources().getColor(R.color.contact_soft_grey));
            et_confirm_password_content.setTextColor(getResources().getColor(R.color.ac_subtitle));
            return true;
        }
        else
        {
            layout_error_edit_profile.setVisibility(View.VISIBLE);
            tv_password_indicator.setTextColor(getResources().getColor(R.color.red_action));
            et_password_content.setTextColor(getResources().getColor(R.color.red_action));
            tv_confirm_password_indicator.setTextColor(getResources().getColor(R.color.red_action));
            et_confirm_password_content.setTextColor(getResources().getColor(R.color.red_action));
            return false;
        }
    }

    private void profileEditMode(boolean isEditing) {
        Log.i(Constants.TAG, "ProfileFragment.profileEditMode: " + isEditing);

        if (isEditing){
            editProfile.setText(getActivity().getString(R.string.profile_edit_mode_done));
            opaqueFilter.setVisibility(View.VISIBLE);
            imgTakePhoto.setVisibility(View.VISIBLE);

        }else{
            editProfile.setText(getActivity().getString(R.string.profile_edit_mode_edit));
            imgTakePhoto.setVisibility(View.GONE);
            opaqueFilter.setVisibility(View.GONE);
        }

        et_first_name_content.setEnabled(isEditing);
        et_first_name_content.setEllipsize(TextUtils.TruncateAt.END);
        et_last_name_content.setEnabled(isEditing);
        et_last_name_content.setEllipsize(TextUtils.TruncateAt.END);
        et_company_content.setEnabled(isEditing);
        et_company_content.setEllipsize(TextUtils.TruncateAt.END);
        et_job_title_content.setEnabled(isEditing);
        et_job_title_content.setEllipsize(TextUtils.TruncateAt.END);
        et_home_content.setEnabled(isEditing);
        et_home_content.setEllipsize(TextUtils.TruncateAt.END);
        et_password_content.setEnabled(isEditing);
        et_password_content.setEllipsize(TextUtils.TruncateAt.END);
        et_confirm_password_content.setEnabled(isEditing);
        et_confirm_password_content.setEllipsize(TextUtils.TruncateAt.END);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(null != this.photoBitmap)
        {
            if(!this.photoBitmap.isRecycled())
                this.photoBitmap.recycle();
        }


        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK)
        {
            isAvatarHasChangedAfterSelection = true;
            new DecodeAndLoadBitmapAvatar().execute();
        }

        else if(requestCode == REQUEST_IMAGE_GALLERY && resultCode == Activity.RESULT_OK)
        {
            isAvatarHasChangedAfterSelection = true;
            Uri selectedImage = data.getData();
            photoPath = Utils.getRealPathFromUri(selectedImage, getActivity());
            if (null != photoPath)
                new DecodeAndLoadBitmapAvatar().execute();
        }
        else
            isAvatarHasChangedAfterSelection = false;
    }

    private void loadAvatarIntoImageView()
    {
        textAvatar.setText("");
        profilePicture.setImageBitmap(photoBitmap);
        profilePicture.setBorderWidth(2);
        profilePicture.setBorderColor(Color.WHITE);
    }

    private void initSpinners()
    {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.phones_array, android.R.layout.simple_spinner_item);
        if (adapter != null)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.email_array, android.R.layout.simple_spinner_item);

        if (adapter != null)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    private void loadProfileImage()
    {
        Utils.loadContactAvatar
                (
                        userProfile.getFirstName()
                        , userProfile.getLastName()
                        , this.profilePicture
                        , this.textAvatar
                        , userProfile.getAvatar()
                        , 50
                );
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
            mIndex = getArguments().getInt(ARG_PARAM1);

        this.realm = Realm.getDefaultInstance();

        if(mIndex == 0) {
            Log.i(Constants.TAG, "ProfileFragment.onCreate: " + mIndex);
        }else if(mIndex == 1 ){
            Log.i(Constants.TAG, "ProfileFragment.onCreate: " + mIndex);
        }else{
            Log.i(Constants.TAG, "ProfileFragment.onCreate: " + mIndex);
        }

        this.sp = getActivity().getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(Constants.TAG, "ProfileFragment.onResume: ");
        if(!isProfileLoadedAtLeastOnce)
        {
            profileController.getProfile(realm);
            isProfileLoadedAtLeastOnce = true;
        }
    }

    @Override
    public void onProfileReceived(final UserProfile userProfile)
    {
        if(userProfile != null)
        {
            if(isFirstLoadNeed)
            {
                this.userProfile = userProfile;
                this.profileId = userProfile.getId();
                Log.d(Constants.TAG, "ProfileFragment.onProfileReceived: ");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        et_first_name_content.setText(userProfile.getFirstName());
                        et_last_name_content.setText(userProfile.getLastName());
                        et_company_content.setText(userProfile.getCompany());
                        et_job_title_content.setText(userProfile.getPosition());
                        et_home_content.setText(userProfile.getOfficeLocation());
                    }
                });
                loadProfileImage();
                isFirstLoadNeed = false;
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    layout_error_edit_profile.setVisibility(View.GONE);
                }
            });
        }
    }

     public void onPause(){
        super.onPause();
        profileController.setConnectionCallback(null);
         Log.d(Constants.TAG, "ProfileFragment.onPause: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
        if(null != realm)
            this.realm.close();
    }

    @Override
    public void onProfileConnectionError() {
        Log.d(Constants.TAG, "ProfileFragment.onProfileConnectionError: ");
        isUpdating = false;
    }

    @Override
    public void onUpdateProfileConnectionError() {
        Log.d(Constants.TAG, "ProfileFragment.onProfileConnectionUpdateError: ");
        profileController.showToast(getString(R.string.wrong_profile_update));
        isUpdating = false;
    }

    @Override
    public void onUpdateProfileConnectionCompleted() {
        Log.d(Constants.TAG, "ProfileFragment.onUpdateProfileConnectionCompleted: ");

        isUpdating = false;
    }

    @Override
    public void onPasswordChangeError(String error) {
        Log.d(Constants.TAG, "ProfileFragment.onPasswordChangeError: " + error);
        profileController.showToast(error);
    }

    @Override
    public void onPasswordChangeCompleted() {
        Log.d(Constants.TAG, "ProfileFragment.onPasswordChangeCompleted: ");
    }


    @Override
    public void onConnectionNotAvailable() {
        Log.w(Constants.TAG, "ProfileFragment.onConnectionNotAvailable: ");
        isUpdating = false;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String id);
    }


    private void dispatchTakePictureIntent(String title)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
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

    public Uri setImageUri()
    {
        // Store image in dcim
        File file = new File(Environment.getExternalStorageDirectory() + "/DCIM/", "image" + new
                Date().getTime() + ".png");
        Uri imgUri = Uri.fromFile(file);
        photoPath = file.getAbsolutePath();
        return imgUri;
    }

    public class UpdateProfile extends AsyncTask<Void, Void, String>
    {
        private ProgressDialog pdia;
        private String responseCode;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pdia = new ProgressDialog(getActivity());
            pdia.setMessage(getActivity().getString(R.string.progress_dialog_uploading_file));
            pdia.show();
        }

        @Override
        protected String doInBackground(Void... params)
        {
            if(isAvatarHasChangedAfterSelection)
            {
                try
                {
                    filePushToServerController =  FilePushToServerController.newInstance(getActivity());
                    photoBitmap = Utils.adjustBitmapAsSquare(photoBitmap);
                    photoBitmap = Utils.resizeBitmapToStandardValue(photoBitmap, Constants
                            .MAX_AVATAR_WIDTH_OR_HEIGHT);
                    filePushToServerController.storeProfileAvatar(photoBitmap,profileId);
                    filePushToServerController.prepareRequestForPushAvatar
                            (
                                    Constants.CONTACT_API_POST_AVATAR,
                                    Constants.MULTIPART_AVATAR,
                                    Constants.MEDIA_TYPE_JPG,
                                    profileId
                            );

                    String response = filePushToServerController.executeRequest();
                    this.responseCode = filePushToServerController.getResponseCode();
                    return  response;
                }
                catch (Exception e)
                {
                    Log.e(Constants.TAG, "ProfileFragment -> pushFileInBackground ERROR",e);
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            if(pdia.isShowing()) pdia.dismiss();
            if(isAvatarHasChangedAfterSelection)
            {
                Log.e(Constants.TAG, "FilePushToServerController.sendFile: Response content: " + result);

                if(this.responseCode.startsWith("2"))
                {
                    loadNewAvatarURL(result);
                    if(null != photoBitmap && !photoBitmap.isRecycled())
                    {
                        photoBitmap.recycle();
                        photoBitmap = null;
                    }
                }
            }
            isUpdating = updateContactData();
            isAvatarHasChangedAfterSelection = false;
        }
    }


    public class DecodeAndLoadBitmapAvatar extends AsyncTask<Void, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            editProfile.setClickable(false);
        }

        @Override
        protected String doInBackground(Void... params)
        {
            photoBitmap = Utils.decodeFile(photoPath);
            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            progressBar.setVisibility(View.GONE);
            editProfile.setClickable(true);
            loadAvatarIntoImageView();
        }
    }

    private void loadNewAvatarURL(String result)
    {
        String URL = filePushToServerController.getAvatarURL(result);
        if(null != URL)
        {
            avatarNewURL = URL;
        }
        else
        {
            avatarNewURL = null;
        }
    }

    @Subscribe
    public void enableEditProfile(EnableEditProfileEvent event) {
        boolean enable = event.getEnable();
        if (enable){
            editProfile.setVisibility(View.VISIBLE);
        } else{
            editProfile.setVisibility(View.GONE);
        }
    }
}
