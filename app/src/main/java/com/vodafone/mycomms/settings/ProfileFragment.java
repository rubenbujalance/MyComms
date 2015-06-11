package com.vodafone.mycomms.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.connection.BaseConnection;
import com.vodafone.mycomms.settings.connection.IProfileConnectionCallback;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;
import com.vodafone.mycomms.view.tab.SlidingTabLayout;

import java.io.File;
import java.util.HashMap;

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

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private final int REQUEST_CAMERA = 0;
    private final int SELECT_FILE = 1;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static int RESULT_LOAD_IMAGE = 1;
    private static int TAKE_OR_PICK = 0;
    private static int RESULT_OK = 1;

    // TODO: Rename and change types of parameters
    private int mIndex;
    private String mParam2;

    private boolean isEditing = false;

    private OnFragmentInteractionListener mListener;
    private ProfileController profileController;
    private boolean isUpdating = false;

    private ImageView profilePicture;
    private TextView textAvatar;
    private TextView editPhoto;
    private TextView editProfile;

    // TODO: Rename and change types of parameters
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
       View v = inflater.inflate(R.layout.layout_fragment_profile, container, false);

       initSpinners(v);
       editProfile = (TextView) getActivity().findViewById(R.id.edit_profile);
       editProfile.setVisibility(View.VISIBLE);
       profilePicture = (ImageView) v.findViewById(R.id.profile_picture);
       textAvatar = (TextView) v.findViewById(R.id.avatarText);

       Log.i(Constants.TAG, "ProfileFragment.onCreateView: ");
       editProfile.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Log.i(Constants.TAG, "ProfileFragment.onClick: editProfile, isEditing= " + isEditing + "isUpdating=" + isUpdating);
               profileEditMode(isEditing);
               if (isEditing && !isUpdating) {
                   //TODO verify if details have actually changed and send update accordingly
                   isUpdating = updateContactData();
                   if (!isUpdating) {
                       return;
                   }
               } else if (!isEditing && !isUpdating) {
                   isEditing = !isEditing;
                   profileEditMode(isEditing);
               }
           }
       });

       editPhoto = (TextView) v.findViewById(R.id.edit_photo);
       editPhoto.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               //TODO: Do things
               //Take Picture or Camera Roll
               //selectImage();
           }
       });
       return v;
   }

    private boolean updateContactData() {
        Log.d(Constants.TAG, "ProfileFragment.updateContactData: ");
        if(!BaseConnection.isConnected(this.getActivity())){
            profileController.showToast("Not connected. Can't save details.");
            return false;
        }

        String password = ((EditText) getActivity().findViewById(R.id.profile_password)).getText().toString();
        String repeatPassword = ((EditText) getActivity().findViewById(R.id.profile_password_repeat)).getText().toString();

        if((password != null && password.length() > 0) && (repeatPassword == null || repeatPassword.length() == 0)){
            profileController.showToast("Passwords don't match");
            return false;
        }else if ((repeatPassword != null && repeatPassword.length() > 0) && (password == null || password.length() == 0)){
            profileController.showToast("Passwords don't match");
            return false;
        }else if( password != null && password.length() > 0 && repeatPassword != null && repeatPassword.length() > 0 && !password.equals(repeatPassword)){
            profileController.showToast("Passwords don't match");
            return false;
        }else if(password != null && password.length() > 0 && repeatPassword != null && repeatPassword.length() > 0  && password.equals(repeatPassword) ){
            HashMap newPasswordHashMap = new HashMap<>();
            newPasswordHashMap.put("password", password);
            profileController.updatePassword(newPasswordHashMap);
        }

        String name = ((EditText) getActivity().findViewById(R.id.profile_name)).getText().toString();
        String surname = ((EditText) getActivity().findViewById(R.id.profile_surname)).getText().toString();

//      EditText profilePhoneNumber = (EditText) getActivity().findViewById(R.id.phone_number1);
//      EditText profileEmail = (EditText) getActivity().findViewById(R.id.email1);

        String company = ((EditText) getActivity().findViewById(R.id.contact_company)).getText().toString();
        String position = ((EditText) getActivity().findViewById(R.id.contact_position)).getText().toString();
        String officeLocation = ((EditText) getActivity().findViewById(R.id.office_location)).getText().toString();

        UserProfile newProfile = new UserProfile();
        newProfile.setFirstName(name);
        newProfile.setLastName(surname);
        newProfile.setCompany(company);
        newProfile.setPosition(position);
        newProfile.setOfficeLocation(officeLocation);

        Log.d(Constants.TAG, "ProfileFragment.updateContactData:" + profileController.printUserProfile(newProfile));

        HashMap newProfileHashMap = profileController.getProfileHashMap(newProfile);

        boolean isValid  = Utils.validateStringHashMap(newProfileHashMap);
        if(!isValid) {
            profileController.showToast("Info not valid");
            return false;
        }

        profileController.updateContactData(newProfileHashMap);
        return  true;
    }

    private void profileEditMode(boolean isEditing) {
        Log.i(Constants.TAG, "ProfileFragment.profileEditMode: " + isEditing);

        LinearLayout passwordLayout = (LinearLayout) getActivity().findViewById(R.id.password_layout);
        if (isEditing){
            passwordLayout.setVisibility(View.VISIBLE);
            editProfile.setText(getActivity().getString(R.string.profile_edit_mode_done));
            editPhoto.setVisibility(View.VISIBLE);

        }else{
            editProfile.setText(getActivity().getString(R.string.profile_edit_mode_edit));
            editPhoto.setVisibility(View.GONE);
            passwordLayout.setVisibility(View.GONE);
        }

        EditText profileName = (EditText) getActivity().findViewById(R.id.profile_name);
        profileName.setEnabled(isEditing);
        EditText profileSurname = (EditText) getActivity().findViewById(R.id.profile_surname);
        profileSurname.setEnabled(isEditing);
       // EditText profilePhoneNumber = (EditText) getActivity().findViewById(R.id.phone_number1);
       // profilePhoneNumber.setEnabled(isEditing);
       // EditText profileEmail = (EditText) getActivity().findViewById(R.id.email1);
      //  profileEmail.setEnabled(isEditing);
        EditText profileCompany = (EditText) getActivity().findViewById(R.id.contact_company);
        profileCompany.setEnabled(isEditing);
        EditText profilePosition = (EditText) getActivity().findViewById(R.id.contact_position);
        profilePosition.setEnabled(isEditing);

//        EditText profileDepartment = (EditText) getActivity().findViewById(R.id.department);
//        profileDepartment.setEnabled(isEditing);
        EditText profileOfficeLocation = (EditText) getActivity().findViewById(R.id.office_location);
        profileOfficeLocation.setEnabled(isEditing);
//        EditText profileInfo = (EditText) getActivity().findViewById(R.id.contact_additional_info);
//        profileInfo.setEnabled(isEditing);

        EditText password = (EditText) getActivity().findViewById(R.id.profile_password);
        password.setEnabled(isEditing);
        EditText repeatPassword = (EditText) getActivity().findViewById(R.id.profile_password_repeat);
        repeatPassword.setEnabled(isEditing);
    }

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    /*Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image*//*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);*/
                    String IMAGE_TYPE = "image/*";
                    Intent intent = new Intent();
                    intent.setType(IMAGE_TYPE);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,
                            "Select Picture"), RESULT_LOAD_IMAGE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if taking
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode != 0) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            // my ImageView
            profilePicture.setImageBitmap(imageBitmap);

            // if choosing
        } else if (requestCode == RESULT_LOAD_IMAGE && resultCode != 0) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            // my ImageView
            profilePicture.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }
    }

    private void initSpinners(View v) {
        Spinner spinnerPhone = (Spinner) v.findViewById(R.id.spinner_phone);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.phones_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        if (adapter != null) {
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            spinnerPhone.setAdapter(adapter);
        }

        Spinner spinnerEmail = (Spinner) v.findViewById(R.id.spinner_email);
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.email_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        if (adapter != null) {
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            spinnerEmail.setAdapter(adapter);
        }
    }

    private void loadProfileImage(UserProfile userProfile) {
        File avatarFile = null;
        String profileId = userProfile.getId();

        if (profileId!=null && !profileId.equals(""))
            avatarFile = new File(getActivity().getFilesDir(), Constants.CONTACT_AVATAR_DIR + "avatar_"+profileId+".jpg");

        if (avatarFile!= null && avatarFile.exists()) {
            Picasso.with(getActivity())
                    .load(avatarFile)
                    .into(profilePicture);
        } else{
            String initials = userProfile.getFirstName().substring(0,1) +
                    userProfile.getLastName().substring(0,1);
            profilePicture.setImageResource(R.color.grey_middle);
            textAvatar.setText(initials);
        }
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

        if(mIndex == 0) {
            Log.i(Constants.TAG, "ProfileFragment.onCreate: " + mIndex);
        }else if(mIndex == 1 ){
            Log.i(Constants.TAG, "ProfileFragment.onCreate: " + mIndex);
        }else{
            Log.i(Constants.TAG, "ProfileFragment.onCreate: " + mIndex);
        }

        profileController = new ProfileController(this);
        profileController.setConnectionCallback(this);
    }
    
    @Override
    public void onResume(){
        super.onResume();
        Log.d(Constants.TAG, "ProfileFragment.onResume: ");
//        TextView editProfile = (TextView) getActivity().findViewById(R.id.edit_profile);
//        editProfile.setVisibility(View.VISIBLE);

        profileController.setConnectionCallback(this);
        profileController.getProfile();
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
    public void onPause(){
        super.onPause();
        profileController.setConnectionCallback(null);
        Log.d(Constants.TAG, "ProfileFragment.onPause: ");

    }




    @Override
    public void onProfileReceived(UserProfile userProfile) {
        if(userProfile != null) {
            Log.d(Constants.TAG, "ProfileFragment.onProfileReceived: ");
            EditText profileName = (EditText) getActivity().findViewById(R.id.profile_name);
            profileName.setText(userProfile.getFirstName());
            EditText profileSurname = (EditText) getActivity().findViewById(R.id.profile_surname);
            profileSurname.setText(userProfile.getLastName());
            EditText profilePhoneNumber = (EditText) getActivity().findViewById(R.id.phone_number1);
            String phone = Utils.getElementFromJsonArrayString(userProfile.getPhones(), Constants.PROFILE_PHONE);
            profilePhoneNumber.setText(phone);
            EditText profileEmail = (EditText) getActivity().findViewById(R.id.email1);
            String email = Utils.getElementFromJsonArrayString(userProfile.getEmails(), Constants.PROFILE_EMAIL);
            profileEmail.setText(email);
            EditText profileCompany = (EditText) getActivity().findViewById(R.id.contact_company);
            profileCompany.setText(userProfile.getCompany());
            EditText profilePosition = (EditText) getActivity().findViewById(R.id.contact_position);
            profilePosition.setText(userProfile.getPosition());

//          EditText profileDepartment = (EditText) getActivity().findViewById(R.id.department);
//          profileDepartment.setText("????????");

            EditText profileOfficeLocation = (EditText) getActivity().findViewById(R.id.office_location);
            profileOfficeLocation.setText(userProfile.getOfficeLocation());
//            EditText profileInfo = (EditText) getActivity().findViewById(R.id.contact_additional_info);
//            profileInfo.setText("????????");

        }
    }

    @Override
    public void onProfileConnectionError() {
        Log.d(Constants.TAG, "ProfileFragment.onProfileConnectionError: ");
    }



    @Override
    public void onUpdateProfileConnectionError() {
        Log.d(Constants.TAG, "ProfileFragment.onProfileConnectionUpdateError: ");
        profileController.showToast("Profile is not updated");
        isUpdating = false;
    }

    @Override
    public void onUpdateProfileConnectionCompleted() {
        Log.d(Constants.TAG, "ProfileFragment.onUpdateProfileConnectionCompleted: ");
        if(isEditing) {
            isEditing = !isEditing;
            profileEditMode(isEditing);
        }
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
        Toast.makeText(getActivity().getApplicationContext(), "Connection not available", Toast.LENGTH_LONG);
        profileController.showToast("Connection not available");
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
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
