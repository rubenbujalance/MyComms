package com.vodafone.mycomms.profile;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.view.tab.SlidingTabLayout;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class ProfileFragment extends Fragment{

    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private int mIndex;
    private String mParam2;

    private boolean enabled = false;

    private OnFragmentInteractionListener mListener;

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
       TextView editProfile = (TextView) getActivity().findViewById(R.id.edit_profile);
       editProfile.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Log.i(Constants.TAG, "ProfileFragment.onClick: editProfile");
               enabled = !enabled;
               profileEditMode(enabled);
           }
       });
       return v;
   }

    private void profileEditMode(boolean enabled) {
        Log.i(Constants.TAG, "ProfileFragment.profileEditMode: " + enabled);
        TextView editProfile = (TextView) getActivity().findViewById(R.id.edit_profile);
        TextView editPhoto = (TextView) getActivity().findViewById(R.id.edit_photo);
        if (enabled){
            editProfile.setText("Done");
            editPhoto.setVisibility(View.VISIBLE);
        }else{
            editProfile.setText("Edit");
            editPhoto.setVisibility(View.GONE);
        }
        EditText profileName = (EditText) getActivity().findViewById(R.id.profile_name);
        profileName.setEnabled(enabled);
        EditText profileSurname = (EditText) getActivity().findViewById(R.id.profile_surname);
        profileSurname.setEnabled(enabled);
        EditText profilePhoneNumber = (EditText) getActivity().findViewById(R.id.phone_number1);
        profilePhoneNumber.setEnabled(enabled);
        EditText profileEmail = (EditText) getActivity().findViewById(R.id.email1);
        profileEmail.setEnabled(enabled);
        EditText profileCompany = (EditText) getActivity().findViewById(R.id.company);
        profileCompany.setEnabled(enabled);
        EditText profilePosition = (EditText) getActivity().findViewById(R.id.position);
        profilePosition.setEnabled(enabled);
        EditText profileDepartment = (EditText) getActivity().findViewById(R.id.department);
        profileDepartment.setEnabled(enabled);
        EditText profileOfficeLocation = (EditText) getActivity().findViewById(R.id.office_location);
        profileOfficeLocation.setEnabled(enabled);
        EditText profileInfo = (EditText) getActivity().findViewById(R.id.additional_info);
        profileInfo.setEnabled(enabled);
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
