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
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.vodafone.mycomms.view.tab.SlidingTabLayout;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

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

    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private final int REQUEST_CAMERA = 0;
    private final int SELECT_FILE = 1;

    private static int RESULT_LOAD_IMAGE = 1;
    private static int TAKE_OR_PICK = 0;
    private static int RESULT_OK = 1;

    private int mIndex;
    private String mParam2;

    private boolean isEditing = false;

    private OnFragmentInteractionListener mListener;
    private ProfileController profileController;
    private boolean isUpdating = false;

    private CircleImageView profilePicture, opaqueFilter;
    private ImageView imgTakePhoto;
    private TextView textAvatar;
    private TextView editProfile;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;

    private String photoPath = null;
    private Bitmap photoBitmap = null;

    private FilePushToServerController filePushToServerController;

    private UserProfile userProfile;
    private String profileId;
    private boolean isFirstLoadNeed = true;
    private String avatarNewURL = null;
    private SharedPreferences sp;

    private LinearLayout layout_error_edit_profile;
    private TextView tv_error_on_edit;

    private Realm realm;

    private boolean isAvatarHasChangedAfterSelection = false, isProfileLoadedAtLeastOnce = false;

    public static ProfileFragment newInstance(int index, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, index);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

   @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
       BusProvider.getInstance().register(this);

       View v = inflater.inflate(R.layout.layout_set_profile, container, false);

       initSpinners(v);
       editProfile = (TextView) getActivity().findViewById(R.id.edit_profile);
       editProfile.setVisibility(View.INVISIBLE);

       profilePicture = (CircleImageView) v.findViewById(R.id.profile_picture);
       opaqueFilter = (CircleImageView) v.findViewById(R.id.opaque_filter);
       textAvatar = (TextView) v.findViewById(R.id.avatarText);
       imgTakePhoto = (ImageView) v.findViewById(R.id.img_take_photo);
       imgTakePhoto.setVisibility(View.GONE);


       Log.i(Constants.TAG, "ProfileFragment.onCreateView: ");
       editProfile.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Log.i(Constants.TAG, "ProfileFragment.onClick: editProfile, isEditing= " + isEditing + "isUpdating=" + isUpdating);

               if (isUpdating && !isEditing) return;

               profileEditMode(!isEditing);

               if (isEditing)
                   new UpdateProfile().execute();

               isEditing = !isEditing;
           }
       });

       profilePicture.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if (isEditing) {
                   dispatchTakePictureIntent(getString(R.string.how_would_you_like_to_add_a_photo), null);
               }
           }
       });

       profileController = new ProfileController(getActivity());
       profileController.setConnectionCallback(this);

       return v;
   }

    private boolean updateContactData()
    {
        Log.d(Constants.TAG, "ProfileFragment.updateContactData: ");
        if(!BaseConnection.isConnected(this.getActivity())){
            profileController.showToast("Not connected. Can't save details.");
            return false;
        }

        if(isShowErrorOnEditProfile())
        {
            isEditing = true;
            isUpdating = true;
            return false;
        }

        String firstName = ((EditText) getActivity().findViewById(R.id.et_first_name_content)).getText().toString();
        String lastName = ((EditText) getActivity().findViewById(R.id.et_last_name_content)).getText().toString();
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Constants.PROFILE_FULLNAME_SHARED_PREF, firstName + " " + lastName);
        editor.apply();
        String company = ((EditText) getActivity().findViewById(R.id.et_company_content)).getText().toString();
        String position = ((EditText) getActivity().findViewById(R.id.et_job_title_content)).getText().toString();
        String officeLocation = ((EditText) getActivity().findViewById(R.id.et_home_content)).getText().toString();


        if(null != avatarNewURL)
        {
            profileController.updateUserAvatarInDB(avatarNewURL);
            avatarNewURL = null;
        }

        if(!profileController.isUserProfileChanged(firstName, lastName, company, position,
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

        Log.d(Constants.TAG, "ProfileFragment.updateContactData:" + profileController.printUserProfile(newProfile));

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

    private boolean isShowErrorOnEditProfile()
    {
        String password = ((EditText) getActivity().findViewById(R.id.et_password_content)).getText().toString();
        String repeatPassword = ((EditText) getActivity().findViewById(R.id.et_confirm_password_content)).getText().toString();
        layout_error_edit_profile.setVisibility(View.GONE);


        if((password != null && password.length() > 0) && (repeatPassword == null || repeatPassword.length() == 0)){
            tv_error_on_edit.setText("Passwords don't match");
            layout_error_edit_profile.setVisibility(View.VISIBLE);
            return true;
        }else if ((repeatPassword != null && repeatPassword.length() > 0) && (password == null || password.length() == 0)){
            tv_error_on_edit.setText("Passwords don't match");
            layout_error_edit_profile.setVisibility(View.VISIBLE);
            return true;
        }else if( password != null && password.length() > 0 && repeatPassword != null && repeatPassword.length() > 0 && !password.equals(repeatPassword)){
            tv_error_on_edit.setText("Passwords don't match");
            layout_error_edit_profile.setVisibility(View.VISIBLE);
            return true;
        }else if(password != null && password.length() > 0 && repeatPassword != null && repeatPassword.length() > 0  && password.equals(repeatPassword) ){
            HashMap newPasswordHashMap = new HashMap<>();
            newPasswordHashMap.put("password", password);
            profileController.updatePassword(newPasswordHashMap);
            layout_error_edit_profile.setVisibility(View.GONE);
            return false;
        }
        else
            return true;
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

        EditText profileName = (EditText) getActivity().findViewById(R.id.et_first_name_content);
        profileName.setEnabled(isEditing);
        EditText profileSurname = (EditText) getActivity().findViewById(R.id.et_last_name_content);
        profileSurname.setEnabled(isEditing);
        EditText profileCompany = (EditText) getActivity().findViewById(R.id.et_company_content);
        profileCompany.setEnabled(isEditing);
        EditText profilePosition = (EditText) getActivity().findViewById(R.id.et_job_title_content);
        profilePosition.setEnabled(isEditing);
        EditText profileOfficeLocation = (EditText) getActivity().findViewById(R.id.et_home_content);
        profileOfficeLocation.setEnabled(isEditing);
        EditText password = (EditText) getActivity().findViewById(R.id.et_password_content);
        password.setEnabled(isEditing);
        EditText repeatPassword = (EditText) getActivity().findViewById(R.id.et_confirm_password_content);
        repeatPassword.setEnabled(isEditing);
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

    private void initSpinners(View v) {
//        Spinner spinnerPhone = (Spinner) v.findViewById(R.id.spinner_phone);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.phones_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        if (adapter != null) {
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
//            spinnerPhone.setAdapter(adapter);
        }

//        Spinner spinnerEmail = (Spinner) v.findViewById(R.id.spinner_email);
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.email_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        if (adapter != null) {
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
//            spinnerEmail.setAdapter(adapter);
        }
    }

    private void loadProfileImage()
    {
        Utils.loadContactAvatar(userProfile.getFirstName(), userProfile.getLastName(), this
                .profilePicture, this.textAvatar, userProfile.getAvatar(), 25);
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

        if (getArguments() != null) {
            mIndex = getArguments().getInt(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

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
//        TextView editProfile = (TextView) getActivity().findViewById(R.id.edit_profile);
//        editProfile.setVisibility(View.VISIBLE);

        if(!isProfileLoadedAtLeastOnce)
        {
            profileController.getProfile(realm);
            isProfileLoadedAtLeastOnce = true;
        }


    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onProfileReceived(UserProfile userProfile)
    {
        if(userProfile != null)
        {
            if(isFirstLoadNeed)
            {
                this.userProfile = userProfile;
                this.profileId = userProfile.getId();
                Log.d(Constants.TAG, "ProfileFragment.onProfileReceived: ");
                EditText profileName = (EditText) getActivity().findViewById(R.id.et_first_name_content);
                profileName.setText(userProfile.getFirstName());
                EditText profileSurname = (EditText) getActivity().findViewById(R.id.et_last_name_content);
                profileSurname.setText(userProfile.getLastName());
                EditText profileCompany = (EditText) getActivity().findViewById(R.id.et_company_content);
                profileCompany.setText(userProfile.getCompany());
                EditText profilePosition = (EditText) getActivity().findViewById(R.id.et_job_title_content);
                profilePosition.setText(userProfile.getPosition());
                EditText profileOfficeLocation = (EditText) getActivity().findViewById(R.id.et_home_content);
                profileOfficeLocation.setText(userProfile.getOfficeLocation());
                loadProfileImage();

                isFirstLoadNeed = false;
            }
            layout_error_edit_profile = (LinearLayout) getActivity().findViewById(R.id.lay_error_edit);
            tv_error_on_edit = (TextView) getActivity().findViewById(R.id.tv_error_on_edit);
            layout_error_edit_profile.setVisibility(View.GONE);

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
        Toast.makeText(getActivity().getApplicationContext(), "Connection not available", Toast
                .LENGTH_LONG).show();
        profileController.showToast("Connection not available");
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


    private void dispatchTakePictureIntent(String title, String subtitle)
    {

        //Build the alert dialog to let the user choose the origin of the picture

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);

        if(subtitle != null) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
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
                    filePushToServerController =  new FilePushToServerController(getActivity());
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
                    photoBitmap.recycle();
                    photoBitmap = null;
                }
            }
            isUpdating = updateContactData();
            isAvatarHasChangedAfterSelection = false;
        }
    }


    public class DecodeAndLoadBitmapAvatar extends AsyncTask<Void, Void, String>
    {

        private ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.progress_bar);
        private TextView editProfile = (TextView) getActivity().findViewById(R.id.edit_profile);

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
